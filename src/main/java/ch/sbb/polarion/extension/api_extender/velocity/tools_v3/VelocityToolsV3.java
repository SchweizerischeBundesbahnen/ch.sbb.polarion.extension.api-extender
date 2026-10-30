package ch.sbb.polarion.extension.api_extender.velocity.tools_v3;

import ch.sbb.polarion.extension.generic.util.BundleJarsPrioritizingRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gives Velocity scripts access to Velocity Tools 3, for example {@code $velocityToolsV3.date.format('yyyy-MM-dd', $date.date)}.
 * <p>
 * The tool objects come from an isolated class loader. Polarion's Velocity 1.4 calls their methods by reflection.
 * This class is loaded by the bundle class loader, so it must not load {@link VelocityToolsV3Factory}
 * or any Velocity Tools 3 type. The bundle class loader would link them against Polarion's Velocity Tools 2.
 */
@SuppressWarnings("unused")
public class VelocityToolsV3 {

    public static final Set<String> TOOL_NAMES = Set.of("collection", "date", "display", "esc", "math", "number", "json", "xml", "link", "loop", "render", "log");

    /**
     * Tools which keep state between calls. They must not be shared between scripts.
     */
    static final Set<String> REQUEST_SCOPED_TOOLS = Set.of("json", "xml", "link", "loop");

    private final Map<String, Object> sharedTools = new ConcurrentHashMap<>();

    public Object get(@NotNull String name) {
        if (!TOOL_NAMES.contains(name)) {
            return null;
        }
        if (REQUEST_SCOPED_TOOLS.contains(name)) {
            return createTool(name);
        }
        return sharedTools.computeIfAbsent(name, VelocityToolsV3::createTool);
    }

    public @NotNull Set<String> getNames() {
        return TOOL_NAMES;
    }

    private static Object createTool(@NotNull String name) {
        // Without rethrow, generic logs the full stack trace. Velocity would only log the reflection wrapper.
        Map<String, Object> result = BundleJarsPrioritizingRunnable.executeCached(
                VelocityToolsV3Factory.class, Map.of(VelocityToolsV3Factory.PARAM_TOOL, name), false);
        if (result.get(BundleJarsPrioritizingRunnable.ERROR_KEY) instanceof Throwable error) {
            throw new IllegalStateException("Cannot create Velocity Tools 3 tool '" + name + "'", error);
        }
        return result.get(VelocityToolsV3Factory.RESULT_TOOL);
    }
}
