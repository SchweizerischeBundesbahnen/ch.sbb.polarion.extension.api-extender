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
 * and replaces them with tokens, which Velocity v1.4 leaves unchanged.
 * {@link Velocity2WikiPageRestoreRenderer} puts the output in place of the tokens right after Velocity v1.4.
 */
public class Velocity2WikiPageExtractRenderer implements XWikiRenderer {

    /**
     * The key of the token-to-HTML map in the {@link XWikiContext} of the request.
     * Included pages render with the same context, so the map collects the tokens of all of them.
     */
    static final String OUTPUTS_KEY = "ch.sbb.polarion.extension.api_extender.velocity2.outputs";
    static final String VARIABLE_PAGE = "page";
    static final String VARIABLE_DOC = "doc";

    @Override
    public String render(String content, XWikiDocument contentdoc, XWikiDocument contextdoc, XWikiContext context) {
        if (content == null || !Velocity2WikiBlocks.containsMarker(content)) {
            return content;
        }
        Velocity2WikiBlocks blocks = Velocity2WikiBlocks.extract(content);
        List<String> htmls = Velocity2WikiScripts.render(blocks.getScripts(), createVariables(contextdoc, context), createLogTag(contentdoc));
        outputs(context).putAll(toMap(blocks.getTokens(), htmls));
        return blocks.getSource();
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

    private static @NotNull Map<String, String> toMap(@NotNull List<String> tokens, @NotNull List<String> htmls) {
        Map<String, String> result = new HashMap<>();
        for (int i = 0; i < tokens.size(); i++) {
            result.put(tokens.get(i), htmls.get(i));
        }
        return result;
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
