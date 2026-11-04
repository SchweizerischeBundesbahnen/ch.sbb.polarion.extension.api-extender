package ch.sbb.polarion.extension.api_extender.velocity.wiki;

import ch.sbb.polarion.extension.api_extender.velocity.engine_v2.Velocity2ScriptRenderer;
import ch.sbb.polarion.extension.generic.util.BundleJarsPrioritizingRunnable;
import com.polarion.alm.server.api.transaction.TransactionalExecutorImpl;
import com.polarion.alm.wiki.IWikiService;
import com.polarion.platform.core.PlatformContext;
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
 * Renders the {@code {velocity2}} regions of LiveDocs and Classic Wiki Pages with the embedded Velocity 2 engine.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class Velocity2WikiScripts {

    static final String VARIABLE_TRANSACTION = "transaction";

    /**
     * @return the wiki rendering context and the current transaction, if one exists
     */
    static @NotNull Map<String, Object> createVariables() {
        Map<String, Object> variables = new HashMap<>(PlatformContext.getPlatform().lookupService(IWikiService.class).getWikiRenderingContextMap());
        Object transaction = TransactionalExecutorImpl.currentTransaction();
        if (transaction != null) {
            variables.put(VARIABLE_TRANSACTION, transaction);
        }
        return variables;
    }

    /**
     * Renders the scripts in order with one shared context.
     *
     * @return the HTML of each script, or an error message in its place
     */
    static @NotNull List<String> render(@NotNull List<String> scripts, @NotNull Map<String, Object> variables, @NotNull String logTag) {
        if (scripts.isEmpty()) {
            return List.of();
        }
        Supplier<Map<String, Object>> variablesSupplier = () -> variables;
        Map<String, Object> result = BundleJarsPrioritizingRunnable.executeCached(Velocity2ScriptRenderer.class, Map.of(
                Velocity2ScriptRenderer.PARAM_SCRIPTS, scripts,
                Velocity2ScriptRenderer.PARAM_LOG_TAG, logTag,
                Velocity2ScriptRenderer.PARAM_VARIABLES, variablesSupplier));

        List<String> htmls = new ArrayList<>();
        if (result.get(Velocity2ScriptRenderer.RESULT_HTMLS) instanceof List<?> resultHtmls
                && result.get(Velocity2ScriptRenderer.RESULT_ERRORS) instanceof List<?> resultErrors) {
            for (int i = 0; i < scripts.size(); i++) {
                Object html = resultHtmls.get(i);
                htmls.add(html != null ? (String) html : renderError((String) resultErrors.get(i)));
            }
        } else {
            String error = renderError(String.valueOf(result.get(BundleJarsPrioritizingRunnable.ERROR_KEY)));
            scripts.forEach(script -> htmls.add(error));
        }
        return htmls;
    }

    static @NotNull String renderError(@Nullable String message) {
        return "<div class=\"polarion-errormessage\"><span class=\"polarion-messagetype\">Velocity 2 error: </span>" + escapeHtml(String.valueOf(message)) + "</div>";
    }

    private static @NotNull String escapeHtml(@NotNull String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
