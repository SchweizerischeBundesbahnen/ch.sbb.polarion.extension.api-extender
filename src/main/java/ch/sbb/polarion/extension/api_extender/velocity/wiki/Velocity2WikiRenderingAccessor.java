package ch.sbb.polarion.extension.api_extender.velocity.wiki;

import com.polarion.alm.tracker.internal.WikiRenderingProvider;
import com.polarion.alm.tracker.model.IModule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wraps the accessor which renders the Wiki Content blocks of LiveDocs, and renders the
 * {@code {velocityV2}...{velocityV2}} regions in them with the embedded Velocity 2 engine.
 * <p>
 * The regions render before Polarion renders the rest of the source with Velocity v1.4 and wiki markup,
 * so a region does not see variables which Velocity v1.4 sets.
 * This accessor wraps the whole Polarion rendering and cannot act between Velocity v1.4 and wiki markup.
 * So each region becomes a {@link Velocity2WikiOutputs} reference, which Velocity v1.4 replaces with the output,
 * and wiki markup then applies to the output.
 */
public class Velocity2WikiRenderingAccessor implements WikiRenderingProvider.IWikiRenderingAccessor {

    static final String VARIABLE_DOCUMENT = "document";

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
        List<String> htmls = Velocity2WikiScripts.render(blocks.getScripts(), createVariables(document), createLogTag(document));

        Map<String, String> outputs = new HashMap<>();
        String source = blocks.getSource();
        for (int i = 0; i < htmls.size(); i++) {
            String token = blocks.getTokens().get(i);
            outputs.put(token, htmls.get(i));
            source = source.replace(token, Velocity2WikiOutputs.reference(token));
        }
        String velocitySource = source;
        return Velocity2WikiOutputs.withOutputs(outputs,
                () -> delegate.render(document, velocitySource, pdfExportId, isCompare, query, language));
    }

    private static @NotNull Map<String, Object> createVariables(@Nullable IModule document) {
        Map<String, Object> variables = Velocity2WikiScripts.createVariables();
        if (document != null) {
            variables.put(VARIABLE_DOCUMENT, document);
        }
        return variables;
    }

    private static @NotNull String createLogTag(@Nullable IModule document) {
        return document != null ? "Velocity 2 wiki block in: " + document.getProject().getId() + "/" + document.getModuleFolder() + "/" + document.getModuleName() : "Velocity 2 wiki block";
    }
}
