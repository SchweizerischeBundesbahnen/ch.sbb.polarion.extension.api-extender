package ch.sbb.polarion.extension.api_extender.velocity.livedoc;

import ch.sbb.polarion.extension.api_extender.velocity.engine_v2.Velocity2ScriptRenderer;
import ch.sbb.polarion.extension.generic.util.BundleJarsPrioritizingRunnable;
import com.polarion.alm.server.api.transaction.TransactionalExecutorImpl;
import com.polarion.alm.tracker.internal.WikiRenderingProvider;
import com.polarion.alm.tracker.model.IModule;
import com.polarion.alm.wiki.IWikiService;
import com.polarion.platform.core.PlatformContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Wraps the accessor which renders the Wiki Content blocks of LiveDocs, and renders the
 * {@code {velocity2}...{velocity2}} regions in them with the embedded Velocity 2 engine.
 * <p>
 * The regions render before Polarion renders the rest of the source with Velocity 1.4 and wiki markup.
 * So a region does not see variables which Velocity 1.4 sets, and its output is HTML without wiki markup processing.
 */
public class Velocity2WikiRenderingAccessor implements WikiRenderingProvider.IWikiRenderingAccessor {

    static final String VARIABLE_DOCUMENT = "document";
    static final String VARIABLE_TRANSACTION = "transaction";

    private final WikiRenderingProvider.IWikiRenderingAccessor delegate;

    public Velocity2WikiRenderingAccessor(@NotNull WikiRenderingProvider.IWikiRenderingAccessor delegate) {
        this.delegate = delegate;
    }

    /**
     * Called by reflection on hot redeploy, when the installed instance comes from the class loader of the previous bundle.
     */
    public @NotNull WikiRenderingProvider.IWikiRenderingAccessor getDelegate() {
        return delegate;
    }

    @Override
    public String render(IModule document, String input, @Nullable String pdfExportId, boolean isCompare, String query, String language) {
        if (input == null || !Velocity2WikiBlocks.containsMarker(input)) {
            return delegate.render(document, input, pdfExportId, isCompare, query, language);
        }
        Velocity2WikiBlocks blocks = Velocity2WikiBlocks.extract(input);
        List<String> htmls = renderScripts(blocks.getScripts(), createVariables(document), createLogTag(document));
        String rendered = delegate.render(document, blocks.getSource(), pdfExportId, isCompare, query, language);
        return rendered == null ? null : blocks.restore(rendered, htmls);
    }

    static @NotNull List<String> renderScripts(@NotNull List<String> scripts, @NotNull Map<String, Object> variables, @NotNull String logTag) {
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

    private static @NotNull Map<String, Object> createVariables(@Nullable IModule document) {
        Map<String, Object> variables = new HashMap<>(PlatformContext.getPlatform().lookupService(IWikiService.class).getWikiRenderingContextMap());
        Object transaction = TransactionalExecutorImpl.currentTransaction();
        if (transaction != null) {
            variables.put(VARIABLE_TRANSACTION, transaction);
        }
        if (document != null) {
            variables.put(VARIABLE_DOCUMENT, document);
        }
        return variables;
    }

    private static @NotNull String createLogTag(@Nullable IModule document) {
        return document != null ? "Velocity 2 wiki block in: " + document.getProject().getId() + "/" + document.getModuleFolder() + "/" + document.getModuleName() : "Velocity 2 wiki block";
    }
}
