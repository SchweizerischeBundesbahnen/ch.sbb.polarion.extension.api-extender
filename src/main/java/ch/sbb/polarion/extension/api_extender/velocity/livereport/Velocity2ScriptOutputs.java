package ch.sbb.polarion.extension.api_extender.velocity.livereport;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * The output of the {@code {velocityV2}} regions of one LiveReport script, as {@code $velocityV2Output} in its Velocity 1.4 context.
 * <p>
 * Velocity 1.4 inserts the output through {@link #get(String)} and does not evaluate it again.
 */
public final class Velocity2ScriptOutputs {

    private final Map<String, String> outputs;

    Velocity2ScriptOutputs(@NotNull Map<String, String> outputs) {
        this.outputs = outputs;
    }

    /**
     * Called by Velocity 1.4.
     */
    public @Nullable String get(@Nullable String token) {
        return outputs.get(token);
    }
}
