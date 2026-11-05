package ch.sbb.polarion.extension.api_extender.velocity.wiki;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Gives Velocity 1.4 the output of the {@code {velocityV2}} regions of a LiveDoc, as {@code $velocityV2Output}
 * in the wiki rendering context.
 * <p>
 * {@link Velocity2WikiRenderingAccessor} replaces each region with {@link #reference(String)} and renders the source
 * inside {@link #withOutputs(Map, Supplier)}. Velocity 1.4 inserts the output and does not evaluate it again,
 * and the wiki markup renderer, which runs after Velocity 1.4, processes it the same way as any Velocity output.
 */
public class Velocity2WikiOutputs {

    public static final String CONTEXT_KEY = "velocityV2Output";

    private static final ThreadLocal<Map<String, String>> OUTPUTS = new ThreadLocal<>();

    /**
     * Called by Velocity 1.4.
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
    static @NotNull String reference(@NotNull String token) {
        return "${" + CONTEXT_KEY + ".get('" + token + "')}";
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
