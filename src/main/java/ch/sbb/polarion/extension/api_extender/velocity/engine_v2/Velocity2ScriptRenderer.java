package ch.sbb.polarion.extension.api_extender.velocity.engine_v2;

import ch.sbb.polarion.extension.generic.util.BundleJarsPrioritizingRunnable;
import com.polarion.core.util.logging.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Renders a script with the embedded Velocity 2 engine, inside the class loader of {@link BundleJarsPrioritizingRunnable}.
 * <p>
 * The engine has no file or class path resource loader, so {@code #parse} and {@code #include} cannot read files.
 * Introspection is not restricted, the same as in the Polarion Script - Block widget.
 */
public class Velocity2ScriptRenderer implements BundleJarsPrioritizingRunnable {

    public static final String PARAM_SCRIPT = "script";
    public static final String PARAM_LOG_TAG = "logTag";
    /**
     * A {@link Supplier} of the variables map. A lambda is not serializable, so generic passes the variables as they are
     * and does not try to serialize the Polarion services in them.
     */
    public static final String PARAM_VARIABLES = "variables";

    public static final String RESULT_HTML = "html";
    public static final String RESULT_ERROR = "error";

    /**
     * Generic creates the runner instance before it sets the context class loader, and Velocity loads its classes through it.
     * The engine must initialize on the first {@link #run(Map)}, not when the class initializes.
     */
    private static final class EngineHolder {
        private static final VelocityEngine ENGINE = createEngine();

        private static VelocityEngine createEngine() {
            VelocityEngine engine = new VelocityEngine();
            engine.setProperty(RuntimeConstants.RESOURCE_LOADERS, "string");
            engine.setProperty("resource.loader.string.class", StringResourceLoader.class.getName());
            // The engine is shared by all users, and inline macros are global by default.
            engine.setProperty(RuntimeConstants.VM_PERM_INLINE_LOCAL, true);
            engine.init();
            return engine;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> run(Map<String, Object> params) {
        String script = (String) params.get(PARAM_SCRIPT);
        String logTag = (String) params.get(PARAM_LOG_TAG);
        Supplier<Map<String, Object>> variables = (Supplier<Map<String, Object>>) params.get(PARAM_VARIABLES);

        VelocityContext context = new VelocityContext(new HashMap<>(variables.get()));
        StringWriter writer = new StringWriter();
        try {
            EngineHolder.ENGINE.evaluate(context, writer, logTag, script);
        } catch (RuntimeException e) {
            // The engine logs to the SLF4J no-operation logger, so this is the only trace of the error.
            Logger.getLogger(Velocity2ScriptRenderer.class).error("Exception while evaluating the Velocity 2 script:\n" + script + "\n", e);
            return Map.of(RESULT_ERROR, String.valueOf(e.getMessage()));
        }
        return Map.of(RESULT_HTML, writer.toString());
    }
}
