package ch.sbb.polarion.extension.api_extender.velocity.tools_v3;

import com.polarion.core.util.logging.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.tools.generic.SafeConfig;
import org.apache.velocity.util.introspection.SecureUberspector;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Renders a template string with the embedded Velocity 2 engine.
 * <p>
 * Velocity 1.4 never gives the page context to a tool, so the template sees only the variables given in the call.
 * Velocity 1.4 has no map literals, so variables are given as up to five key-value pairs or as one {@link Map}.
 * <p>
 * The engine has no file or class path resource loader, so {@code #parse} and {@code #include} cannot read files.
 * {@link SecureUberspector} blocks reflection, class loaders and {@link System}.
 * Errors return {@code null}: an exception in a Wiki Content block breaks the rendering of the whole block.
 */
public class SandboxedRenderTool extends SafeConfig {

    private static final String LOG_TAG = "velocityToolsV3.render";
    private static final VelocityEngine ENGINE = createEngine();

    private static VelocityEngine createEngine() {
        VelocityEngine engine = new VelocityEngine();
        engine.setProperty(RuntimeConstants.RESOURCE_LOADERS, "string");
        engine.setProperty("resource.loader.string.class", StringResourceLoader.class.getName());
        engine.setProperty(RuntimeConstants.UBERSPECT_CLASSNAME, SecureUberspector.class.getName());
        engine.init();
        return engine;
    }

    // Polarion's Velocity 1.4 does not call varargs methods, so the key-value pairs have fixed overloads.

    public String eval(String template) {
        return evalWith(template);
    }

    public String eval(String template, Map<?, ?> variables) {
        return evalWith(template, variables);
    }

    public String eval(String template, Object k1, Object v1) {
        return evalWith(template, k1, v1);
    }

    public String eval(String template, Object k1, Object v1, Object k2, Object v2) {
        return evalWith(template, k1, v1, k2, v2);
    }

    public String eval(String template, Object k1, Object v1, Object k2, Object v2, Object k3, Object v3) {
        return evalWith(template, k1, v1, k2, v2, k3, v3);
    }

    public String eval(String template, Object k1, Object v1, Object k2, Object v2, Object k3, Object v3, Object k4, Object v4) {
        return evalWith(template, k1, v1, k2, v2, k3, v3, k4, v4);
    }

    @SuppressWarnings("java:S107")
    public String eval(String template, Object k1, Object v1, Object k2, Object v2, Object k3, Object v3, Object k4, Object v4, Object k5, Object v5) {
        return evalWith(template, k1, v1, k2, v2, k3, v3, k4, v4, k5, v5);
    }

    /**
     * @param variables key-value pairs ({@code 'name', $value, ...}) or one {@link Map}
     */
    private String evalWith(String template, Object... variables) {
        if (template == null) {
            return null;
        }
        Map<String, Object> values = toMap(variables);
        StringWriter writer = new StringWriter();
        try {
            ENGINE.evaluate(new VelocityContext(values), writer, LOG_TAG, template);
        } catch (RuntimeException e) {
            // The engine logs to the SLF4J no-operation logger, so this is the only trace of the error.
            Logger.getLogger(SandboxedRenderTool.class).warn("Cannot render template: " + template, e);
            return null;
        }
        return writer.toString();
    }

    private static Map<String, Object> toMap(Object[] variables) {
        Map<String, Object> values = new HashMap<>();
        if (variables.length == 1 && variables[0] instanceof Map<?, ?> map) {
            map.forEach((key, value) -> values.put(String.valueOf(key), value));
            return values;
        }
        for (int i = 0; i < variables.length; i += 2) {
            values.put(String.valueOf(variables[i]), variables[i + 1]);
        }
        return values;
    }
}
