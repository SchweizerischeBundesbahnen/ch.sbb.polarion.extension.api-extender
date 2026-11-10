package ch.sbb.polarion.extension.api_extender.velocity.wiki;

import com.polarion.alm.wiki.IWikiService;
import com.polarion.core.util.logging.Logger;
import com.polarion.platform.core.PlatformContext;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.render.XWikiRenderer;
import com.xpn.xwiki.render.XWikiVirtualMacro;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The first renderer of Classic Wiki Pages. It renders the {@code {velocityV2}} regions with Velocity 2
 * and replaces each region with a {@link Velocity2WikiOutputs} reference, so Velocity v1.4 gets the output as a value.
 * {@link Velocity2WikiPageRestoreRenderer} removes the outputs from the thread right after Velocity v1.4.
 */
public class Velocity2WikiPageExtractRenderer implements XWikiRenderer {

    /**
     * The key of the token-to-HTML map in the {@link XWikiContext} of the request.
     * Included pages render with the same context, so the map collects the outputs of all of them.
     */
    static final String OUTPUTS_KEY = "ch.sbb.polarion.extension.api_extender.velocity2.outputs";
    /**
     * The key of the number of renderings in the {@link XWikiContext} which wait for their restore rendering.
     * A page which Velocity v1.4 includes renders inside the Velocity v1.4 rendering of the outer page,
     * so the restore rendering of the inner page must leave the outputs for the outer page.
     */
    static final String DEPTH_KEY = "ch.sbb.polarion.extension.api_extender.velocity2.depth";
    static final String VARIABLE_PAGE = "page";
    static final String VARIABLE_DOC = "doc";

    @Override
    public String render(String content, XWikiDocument contentdoc, XWikiDocument contextdoc, XWikiContext context) {
        // Every rendering counts, also one without regions, so each restore rendering matches its own extract rendering.
        context.put(DEPTH_KEY, depth(context) + 1);
        if (content == null || !Velocity2WikiBlocks.containsMarker(content)) {
            return content;
        }
        Velocity2WikiBlocks blocks = Velocity2WikiBlocks.extract(content);
        List<String> htmls = Velocity2WikiScripts.render(blocks.getScripts(), createVariables(contextdoc, context), createLogTag(contentdoc));

        Map<String, String> outputs = outputs(context);
        String source = blocks.getSource();
        for (int i = 0; i < htmls.size(); i++) {
            String token = blocks.getTokens().get(i);
            outputs.put(token, htmls.get(i));
            source = source.replace(token, Velocity2WikiOutputs.reference(token));
        }
        Velocity2WikiOutputs.setOutputs(outputs);
        return source;
    }

    static int depth(@NotNull XWikiContext context) {
        return context.get(DEPTH_KEY) instanceof Integer depth ? depth : 0;
    }

    @SuppressWarnings("unchecked")
    static @NotNull Map<String, String> outputs(@NotNull XWikiContext context) {
        Object outputs = context.get(OUTPUTS_KEY);
        if (outputs instanceof Map<?, ?> map) {
            return (Map<String, String>) map;
        }
        Map<String, String> created = new HashMap<>();
        context.put(OUTPUTS_KEY, created);
        return created;
    }

    // The same page objects as Velocity v1.4 gets in XWikiVelocityRenderer.
    private static @NotNull Map<String, Object> createVariables(@Nullable XWikiDocument contextdoc, @NotNull XWikiContext context) {
        Map<String, Object> variables = Velocity2WikiScripts.createVariables();
        if (contextdoc != null) {
            try {
                IWikiService wikiService = PlatformContext.getPlatform().lookupService(IWikiService.class);
                variables.put(VARIABLE_PAGE, wikiService.getPage(contextdoc.getName(), contextdoc.getSpace(), contextdoc.getProject(), contextdoc.getRevision()));
                variables.put(VARIABLE_DOC, contextdoc.newDocument(context));
            } catch (RuntimeException e) {
                Logger.getLogger(Velocity2WikiPageExtractRenderer.class).warn("Cannot give $page and $doc to Velocity 2 regions of " + contextdoc.getFullName(), e);
            }
        }
        return variables;
    }

    private static @NotNull String createLogTag(@Nullable XWikiDocument contentdoc) {
        return contentdoc != null ? "Velocity 2 wiki page: " + contentdoc.getFullName() : "Velocity 2 wiki page";
    }

    @Override
    public void flushCache() {
        // Nothing is cached.
    }

    @Override
    public String convertMultiLine(String macroname, String params, String data, String allcontent, XWikiVirtualMacro macro, XWikiContext context) {
        return allcontent;
    }

    @Override
    public String convertSingleLine(String macroname, String params, String allcontent, XWikiVirtualMacro macro, XWikiContext context) {
        return allcontent;
    }
}
