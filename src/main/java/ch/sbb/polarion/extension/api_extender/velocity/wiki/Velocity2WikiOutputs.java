package ch.sbb.polarion.extension.api_extender.velocity.wiki;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Gives Velocity v1.4 the output of the {@code {velocityV2}} regions of a LiveDoc or a Classic Wiki Page,
 * as {@code $velocityV2Output} in the wiki rendering context.
 * <p>
 * {@link Velocity2WikiRenderingAccessor} replaces each region with {@link #referenceWithContext(String)} and renders the source
 * inside {@link #withSource(Source, Supplier)}. On Classic Wiki Pages, {@link Velocity2WikiPageExtractRenderer} and
 * {@link Velocity2WikiPageRestoreRenderer} set and remove the outputs around Velocity v1.4 with {@link #setOutputs(Map)}.
 * Velocity v1.4 inserts the output and does not evaluate it again,
 * and the wiki markup renderer, which runs after Velocity v1.4, processes it the same way as any Velocity output.
 */
public class Velocity2WikiOutputs {

    public static final String CONTEXT_KEY = "velocityV2Output";

    /**
     * Gives the output of a region.
     */
    @FunctionalInterface
    interface Source {
        /**
         * @param xwikiContextApi the {@code $context} of Velocity v1.4, or {@code null}
         */
        @Nullable String get(@Nullable String token, @Nullable Object xwikiContextApi);
    }

    private static final ThreadLocal<Source> SOURCE = new ThreadLocal<>();

    /**
     * Called by Velocity v1.4.
     *
     * @return the output of the region with the token, or {@code null} outside a rendering, so Velocity shows the reference as text
     */
    public @Nullable String get(@Nullable String token) {
        return get(token, null);
    }

    /**
     * Called by Velocity v1.4 in LiveDocs, with its {@code $context}.
     */
    public @Nullable String get(@Nullable String token, @Nullable Object xwikiContextApi) {
        Source source = SOURCE.get();
        return source != null ? source.get(token, xwikiContextApi) : null;
    }

    /**
     * The formal reference notation keeps text which follows the region, for example {@code .txt}, out of the reference.
     */
    public static @NotNull String reference(@NotNull String token) {
        return "${" + CONTEXT_KEY + ".get('" + token + "')}";
    }

    /**
     * Like {@link #reference(String)}, and also gives the {@code $context} of Velocity v1.4 to {@link #get(String, Object)}.
     */
    public static @NotNull String referenceWithContext(@NotNull String token) {
        return "${" + CONTEXT_KEY + ".get('" + token + "', $context)}";
    }

    /**
     * For renderer chains, where Velocity v1.4 runs in a call which this code does not wrap.
     *
     * @param outputs the outputs for the following Velocity v1.4 rendering on this thread, or {@code null} to remove them
     */
    static void setOutputs(@Nullable Map<String, String> outputs) {
        if (outputs != null) {
            SOURCE.set((token, xwikiContextApi) -> outputs.get(token));
        } else {
            SOURCE.remove();
        }
    }

    static <T> T withSource(@NotNull Source source, @NotNull Supplier<T> rendering) {
        Source previous = SOURCE.get();
        SOURCE.set(source);
        try {
            return rendering.get();
        } finally {
            if (previous != null) {
                SOURCE.set(previous);
            } else {
                SOURCE.remove();
            }
        }
    }
}
