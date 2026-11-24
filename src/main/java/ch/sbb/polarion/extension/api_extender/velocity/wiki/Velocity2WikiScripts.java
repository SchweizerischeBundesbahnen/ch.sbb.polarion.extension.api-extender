package ch.sbb.polarion.extension.api_extender.velocity.wiki;

import ch.sbb.polarion.extension.api_extender.velocity.engine_v2.Velocity2ScriptRenderer;
import ch.sbb.polarion.extension.api_extender.velocity.livereport.Velocity14Context;
import ch.sbb.polarion.extension.generic.util.BundleJarsPrioritizingRunnable;
import com.polarion.alm.server.api.transaction.TransactionalExecutorImpl;
import com.polarion.alm.server.api.transaction.WriteTransactionImpl;
import com.polarion.alm.wiki.IWikiService;
import com.polarion.core.util.logging.Logger;
import com.polarion.platform.core.PlatformContext;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.api.Api;
import com.xpn.xwiki.render.XWikiVelocityRenderer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Renders the {@code {velocityV2}} regions of LiveDocs and Classic Wiki Pages with the embedded Velocity 2 engine.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Velocity2WikiScripts {

    static final String VARIABLE_TRANSACTION = "transaction";
    /**
     * The variables which XWikiVelocityRenderer.prepareContext gives Velocity v1.4.
     * $formatter is left out: it writes into the Velocity v1.4 context.
     */
    static final List<String> XWIKI_VARIABLES = List.of("request", "response", "xwiki", "context", "pageParameters", "esc", "msg", "localization");
    private static final String VELOCITY_CONTEXT_KEY = "vcontext";

    /**
     * @return the wiki rendering context and the transaction, like Velocity v1.4 gets them in XWikiVelocityRenderer
     */
    static @NotNull Map<String, Object> createVariables() {
        Map<String, Object> variables = new HashMap<>(PlatformContext.getPlatform().lookupService(IWikiService.class).getWikiRenderingContextMap());
        Object transaction = currentOrNewTransaction();
        if (transaction != null) {
            variables.put(VARIABLE_TRANSACTION, transaction);
        }
        return variables;
    }

    private static @Nullable Object currentOrNewTransaction() {
        Object transaction = TransactionalExecutorImpl.currentTransaction();
        if (transaction != null) {
            return transaction;
        }
        try {
            return new WriteTransactionImpl();
        } catch (RuntimeException | LinkageError e) {
            Logger.getLogger(Velocity2WikiScripts.class).warn("Cannot give $transaction to Velocity 2 regions", e);
            return null;
        }
    }

    /**
     * Prepares the Velocity v1.4 context of the rendering the same way as Velocity v1.4 does it right after,
     * and copies its {@link #XWIKI_VARIABLES}.
     */
    static void putXWikiVariables(@NotNull Map<String, Object> variables, @NotNull XWikiContext context) {
        try {
            // Called by reflection, because the return type is the Velocity v1.4 VelocityContext.
            XWikiVelocityRenderer.class.getMethod("prepareContext", XWikiContext.class).invoke(null, context);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            Logger.getLogger(Velocity2WikiScripts.class).warn("Cannot prepare the Velocity v1.4 context for Velocity 2 regions", e);
        }
        copyXWikiVariables(variables, context);
    }

    /**
     * Copies only the {@link #XWIKI_VARIABLES} of the Velocity v1.4 context, because Velocity v1.4 sets its own variables in the same context.
     */
    static void copyXWikiVariables(@NotNull Map<String, Object> variables, @NotNull XWikiContext context) {
        Object velocityContext = context.get(VELOCITY_CONTEXT_KEY);
        if (velocityContext == null) {
            return;
        }
        try {
            for (String name : XWIKI_VARIABLES) {
                Object value = Velocity14Context.get(velocityContext, name);
                if (value != null) {
                    variables.put(name, value);
                }
            }
        } catch (RuntimeException e) {
            Logger.getLogger(Velocity2WikiScripts.class).warn("Cannot give the XWiki objects of Velocity v1.4 to Velocity 2 regions", e);
        }
    }

    /**
     * @param xwikiContextApi the $context of Velocity v1.4
     */
    static @Nullable XWikiContext toXWikiContext(@Nullable Object xwikiContextApi) {
        return xwikiContextApi instanceof Api api ? api.context : null;
    }

    /**
     * Renders the scripts in order with one shared context.
     *
     * @return the HTML of each script, or an error message in its place
     */
    public static @NotNull List<String> render(@NotNull List<String> scripts, @NotNull Map<String, Object> variables, @NotNull String logTag) {
        List<String> htmls = new ArrayList<>();
        for (Result result : renderResults(scripts, variables, logTag)) {
            htmls.add(result.isError() ? renderError(result.error()) : result.output());
        }
        return htmls;
    }

    /**
     * Renders the scripts in order with one shared context.
     *
     * @return the output or the error message of each script, without HTML around an error
     */
    public static @NotNull List<Result> renderResults(@NotNull List<String> scripts, @NotNull Map<String, Object> variables, @NotNull String logTag) {
        if (scripts.isEmpty()) {
            return List.of();
        }
        Supplier<Map<String, Object>> variablesSupplier = () -> variables;
        Map<String, Object> result = BundleJarsPrioritizingRunnable.executeCached(Velocity2ScriptRenderer.class, Map.of(
                Velocity2ScriptRenderer.PARAM_SCRIPTS, scripts,
                Velocity2ScriptRenderer.PARAM_LOG_TAG, logTag,
                Velocity2ScriptRenderer.PARAM_VARIABLES, variablesSupplier));

        List<Result> results = new ArrayList<>();
        if (result.get(Velocity2ScriptRenderer.RESULT_HTMLS) instanceof List<?> resultHtmls
                && result.get(Velocity2ScriptRenderer.RESULT_ERRORS) instanceof List<?> resultErrors) {
            for (int i = 0; i < scripts.size(); i++) {
                Object html = resultHtmls.get(i);
                results.add(html != null ? new Result((String) html, null) : new Result(null, String.valueOf(resultErrors.get(i))));
            }
        } else {
            Result error = new Result(null, String.valueOf(result.get(BundleJarsPrioritizingRunnable.ERROR_KEY)));
            scripts.forEach(script -> results.add(error));
        }
        return results;
    }

    /**
     * The result of one script: either its output or its error message.
     */
    public record Result(@Nullable String output, @Nullable String error) {
        public boolean isError() {
            return output == null;
        }
    }

    static @NotNull String renderError(@Nullable String message) {
        return "<div class=\"polarion-errormessage\"><span class=\"polarion-messagetype\">Velocity 2 error: </span>" + escapeHtml(String.valueOf(message)) + "</div>";
    }

    private static @NotNull String escapeHtml(@NotNull String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
