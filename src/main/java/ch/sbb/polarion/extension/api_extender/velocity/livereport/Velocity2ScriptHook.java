package ch.sbb.polarion.extension.api_extender.velocity.livereport;

import ch.sbb.polarion.extension.api_extender.velocity.wiki.Velocity2WikiBlocks;
import ch.sbb.polarion.extension.api_extender.velocity.wiki.Velocity2WikiOutputs;
import ch.sbb.polarion.extension.api_extender.velocity.wiki.Velocity2WikiScripts;
import com.polarion.core.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Renders the {@code {velocityV2}} regions of a LiveReport script before Polarion renders the script with Velocity 1.4.
 * <p>
 * {@link Velocity2ScriptWeaver} makes {@code AbstractScriptRenderer.render()} pass the renderer and its script through
 * the {@link BiFunction} stored in the system properties under {@link #PROPERTY}. The woven code refers to JDK types only,
 * so a hot redeploy of this bundle swaps the function and does not touch the woven Polarion class.
 * <p>
 * The regions render before Velocity 1.4, so a region does not see variables which Velocity 1.4 sets.
 * Each region becomes a {@link Velocity2WikiOutputs#reference(String)}, which Velocity 1.4 replaces with the output.
 */
public final class Velocity2ScriptHook implements BiFunction<Object, String, String> {

    public static final String PROPERTY = "ch.sbb.polarion.extension.api_extender.velocity2.scriptHook";
    static final String RENDERER_CONTEXT_METHOD = "velocityContext";
    private static final String LOG_TAG = "Velocity 2 region in LiveReport script";
    private static final Logger logger = Logger.getLogger(Velocity2ScriptHook.class);

    public static void install() {
        System.getProperties().put(PROPERTY, new Velocity2ScriptHook());
    }

    /**
     * The woven code does not check for {@code null}, so a pass-through function stays in place after the bundle stops.
     */
    public static void uninstall() {
        System.getProperties().put(PROPERTY, (BiFunction<Object, String, String>) (renderer, script) -> script);
    }

    /**
     * Called by the woven {@code AbstractScriptRenderer.render()}.
     *
     * @param renderer the {@code AbstractScriptRenderer} instance
     * @param script   the script of the widget
     * @return the script for Velocity 1.4
     */
    @Override
    public String apply(@NotNull Object renderer, @Nullable String script) {
        if (script == null || !Velocity2WikiBlocks.containsMarker(script)) {
            return script;
        }
        try {
            return preprocess(renderer, script);
        } catch (RuntimeException e) {
            logger.error("Cannot render the {velocityV2} regions of a LiveReport script, the script goes to Velocity 1.4 unchanged", e);
            return script;
        }
    }

    private static @NotNull String preprocess(@NotNull Object renderer, @NotNull String script) {
        Object velocityContext = velocityContextOf(renderer);
        Velocity2WikiBlocks blocks = Velocity2WikiBlocks.extract(script);
        List<String> htmls = Velocity2WikiScripts.render(blocks.getScripts(), Velocity14Context.toMap(velocityContext), LOG_TAG);

        Map<String, String> outputs = new HashMap<>();
        String source = blocks.getSource();
        for (int i = 0; i < htmls.size(); i++) {
            String token = blocks.getTokens().get(i);
            outputs.put(token, htmls.get(i));
            source = source.replace(token, Velocity2WikiOutputs.reference(token));
        }
        Velocity14Context.put(velocityContext, Velocity2WikiOutputs.CONTEXT_KEY, new Velocity2ScriptOutputs(outputs));
        return source;
    }

    private static @NotNull Object velocityContextOf(@NotNull Object renderer) {
        try {
            return renderer.getClass().getMethod(RENDERER_CONTEXT_METHOD).invoke(renderer);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("The script renderer has no " + RENDERER_CONTEXT_METHOD + "() method", e);
        }
    }
}
