package ch.sbb.polarion.extension.api_extender.velocity.livereport;

import ch.sbb.polarion.extension.api_extender.velocity.wiki.Velocity2WikiBlocks;
import ch.sbb.polarion.extension.api_extender.velocity.wiki.Velocity2WikiScripts;
import com.polarion.core.util.exceptions.UserFriendlyRuntimeException;
import com.polarion.core.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Renders the {@code {velocityV2}} regions of a "Lucene + Velocity" or "SQL + Velocity" widget query
 * before Polarion renders the query with Velocity v1.4.
 * <p>
 * {@link Velocity2QueryWeaver} makes {@code VelocityQueryRenderer.renderImpl(String)} pass the renderer and the query through
 * the {@link BiFunction} stored in the system properties under {@link #PROPERTY}. The woven code refers to JDK types only,
 * so a hot redeploy of this bundle swaps the function and does not touch the woven Polarion class.
 * <p>
 * {@code renderImpl} creates its Velocity v1.4 context inside the method, so the hook cannot give the output to that context.
 * Each region becomes its output as query text, with the Polarion escape of {@code $} and {@code #}, so Velocity v1.4 prints it unchanged.
 */
public final class Velocity2QueryHook implements BiFunction<Object, String, String> {

    public static final String PROPERTY = "ch.sbb.polarion.extension.api_extender.velocity2.queryHook";
    static final String RENDERING_CONTEXT_FIELD = "renderingContext";
    static final String CONTEXT_FACTORY_METHOD = "createVelocityContext";
    static final String ERROR_PREFIX = "Velocity 2 error: ";
    private static final String LOG_TAG = "Velocity 2 region in widget query";
    private static final Logger logger = Logger.getLogger(Velocity2QueryHook.class);

    public static void install() {
        System.getProperties().put(PROPERTY, new Velocity2QueryHook());
    }

    /**
     * The woven code does not check for {@code null}, so a pass-through function stays in place after the bundle stops.
     */
    public static void uninstall() {
        System.getProperties().put(PROPERTY, (BiFunction<Object, String, String>) (renderer, query) -> query);
    }

    /**
     * Called by the woven {@code VelocityQueryRenderer.renderImpl(String)}.
     * A Lucene error does not name a failed region, so a failure throws an exception which Polarion shows in the widget.
     *
     * @param renderer the {@code VelocityQueryRenderer} instance
     * @param query    the query of the widget parameter
     * @return the query for Velocity v1.4
     */
    @Override
    public String apply(@NotNull Object renderer, @Nullable String query) {
        if (query == null || !Velocity2WikiBlocks.containsMarker(query)) {
            return query;
        }
        Velocity2WikiBlocks blocks = Velocity2WikiBlocks.extract(query);
        List<Velocity2WikiScripts.Result> results = Velocity2WikiScripts.renderResults(blocks.getScripts(), variablesOf(renderer), LOG_TAG);

        String source = blocks.getSource();
        for (int i = 0; i < results.size(); i++) {
            Velocity2WikiScripts.Result result = results.get(i);
            if (result.isError()) {
                logger.error("Cannot render a {velocityV2} region of the widget query:\n" + query);
                throw new UserFriendlyRuntimeException(ERROR_PREFIX + result.error());
            }
            source = source.replace(blocks.getTokens().get(i), escape(result.output()));
        }
        return source;
    }

    private static @NotNull Map<String, Object> variablesOf(@NotNull Object renderer) {
        try {
            Field field = renderer.getClass().getDeclaredField(RENDERING_CONTEXT_FIELD);
            field.setAccessible(true);
            Object renderingContext = field.get(renderer);
            return Velocity14Context.toMap(renderingContext.getClass().getMethod(CONTEXT_FACTORY_METHOD).invoke(renderingContext));
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            logger.error("Cannot read the Velocity v1.4 context of the widget query renderer", e);
            throw new UserFriendlyRuntimeException(ERROR_PREFIX + "cannot read the variables of the widget query", e);
        }
    }

    /**
     * Uses the {@code $esc} tool, which Polarion puts into every Velocity v1.4 context, and extends the Polarion escape of
     * {@code $} and {@code #} to {@code \}: Velocity v1.4 treats {@code \${esc.d}} as an escaped reference and prints it as text.
     */
    static @NotNull String escape(@NotNull String text) {
        StringBuilder result = new StringBuilder(text.length());
        for (char c : text.toCharArray()) {
            switch (c) {
                case '$' -> result.append("${esc.d}");
                case '#' -> result.append("${esc.h}");
                case '\\' -> result.append("${esc.b}");
                default -> result.append(c);
            }
        }
        return result.toString();
    }
}
