package ch.sbb.polarion.extension.api_extender.velocity.tools_v3;

import ch.sbb.polarion.extension.generic.util.BundleJarsPrioritizingRunnable;
import org.apache.velocity.tools.generic.CollectionTool;
import org.apache.velocity.tools.generic.ComparisonDateTool;
import org.apache.velocity.tools.generic.DisplayTool;
import org.apache.velocity.tools.generic.EscapeTool;
import org.apache.velocity.tools.generic.LinkTool;
import org.apache.velocity.tools.generic.LoopTool;
import org.apache.velocity.tools.generic.MathTool;
import org.apache.velocity.tools.generic.NumberTool;
import org.apache.velocity.tools.generic.SafeConfig;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Creates Velocity Tools 3 instances inside the class loader of {@link BundleJarsPrioritizingRunnable},
 * so they link against the embedded Velocity 2 engine and not against the Velocity 1.4 classes of Polarion.
 */
public class VelocityToolsV3Factory implements BundleJarsPrioritizingRunnable {

    public static final String PARAM_TOOL = "tool";
    public static final String RESULT_TOOL = "tool";

    // Keys must match VelocityToolsV3.TOOL_NAMES.
    // ContextTool needs the running page context, which Velocity 1.4 never gives to a tool.
    // RenderTool needs a Velocity 2 context, so SandboxedRenderTool replaces it.
    // Tools with free reflection (class, field) or URL loading (import) are left out for security reasons.
    private static final Map<String, Supplier<SafeConfig>> TOOLS = Map.ofEntries(
            Map.entry("collection", CollectionTool::new),
            Map.entry("date", ComparisonDateTool::new),
            Map.entry("display", DisplayTool::new),
            Map.entry("esc", EscapeTool::new),
            Map.entry("math", MathTool::new),
            Map.entry("number", NumberTool::new),
            Map.entry("json", RestrictedJsonTool::new),
            Map.entry("xml", RestrictedXmlTool::new),
            Map.entry("link", LinkTool::new),
            Map.entry("loop", LoopTool::new),
            Map.entry("render", SandboxedRenderTool::new),
            Map.entry("log", PolarionLogTool::new)
    );

    static Set<String> getToolNames() {
        return TOOLS.keySet();
    }

    @Override
    public Map<String, Object> run(Map<String, Object> params) {
        String name = (String) params.get(PARAM_TOOL);
        Supplier<SafeConfig> supplier = TOOLS.get(name);
        if (supplier == null) {
            throw new IllegalArgumentException("Unknown Velocity Tools 3 tool: " + name);
        }
        SafeConfig tool = supplier.get();
        tool.configure(Map.of());
        return Map.of(RESULT_TOOL, tool);
    }
}
