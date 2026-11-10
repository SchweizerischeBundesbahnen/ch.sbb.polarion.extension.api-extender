package ch.sbb.polarion.extension.api_extender.velocity.wiki;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Gives Velocity v1.4 the output of the {@code {velocityV2}} regions of a LiveDoc or a Classic Wiki Page,
 * as {@code $velocityV2Output} in the wiki rendering context.
 * <p>
 * {@link Velocity2WikiRenderingAccessor} replaces each region with {@link #reference(String)} and renders the source
 * inside {@link #withOutputs(Map, Supplier)}. On Classic Wiki Pages, {@link Velocity2WikiPageExtractRenderer} and
 * {@link Velocity2WikiPageRestoreRenderer} set and remove the outputs around Velocity v1.4 with {@link #setOutputs(Map)}.
 * Velocity v1.4 inserts the output and does not evaluate it again,
 * and the wiki markup renderer, which runs after Velocity v1.4, processes it the same way as any Velocity output.
 */
public class Velocity2WikiOutputs {

    public static final String CONTEXT_KEY = "velocityV2Output";

    private static final ThreadLocal<Map<String, String>> OUTPUTS = new ThreadLocal<>();

    /**
     * Called by Velocity v1.4.
     *
     * @return the output of the region with the token, or {@code null} outside a rendering, so Velocity shows the reference as text
     */
    public @Nullable String get(@Nullable String token) {
        Map<String, String> outputs = OUTPUTS.get();
        return outputs != null ? outputs.get(token) : null;
    }

    /**
     * The formal reference notation keeps text which follows the region, for example {@code .txt}, out of the reference.
     */
    public static @NotNull String reference(@NotNull String token) {
        return "${" + CONTEXT_KEY + ".get('" + token + "')}";
    }

    /**
     * For renderer chains, where Velocity v1.4 runs in a call which this code does not wrap.
     *
     * @param outputs the outputs for the following Velocity v1.4 rendering on this thread, or {@code null} to remove them
     */
    static void setOutputs(@Nullable Map<String, String> outputs) {
        if (outputs != null) {
            OUTPUTS.set(outputs);
        } else {
            OUTPUTS.remove();
        }
    }

    static <T> T withOutputs(@NotNull Map<String, String> outputs, @NotNull Supplier<T> rendering) {
        Map<String, String> previous = OUTPUTS.get();
        OUTPUTS.set(outputs);
        try {
            return rendering.get();
        } finally {
            if (previous != null) {
                OUTPUTS.set(previous);
            } else {
                OUTPUTS.remove();
            }
        }
    }
}
