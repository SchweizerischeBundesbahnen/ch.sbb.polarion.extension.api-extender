package ch.sbb.polarion.extension.api_extender.velocity.wiki;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.render.XWikiRenderer;
import com.xpn.xwiki.render.XWikiVirtualMacro;

/**
 * The renderer of Classic Wiki Pages right after Velocity v1.4. Velocity v1.4 has inserted the output of the
 * {@code {velocityV2}} regions, so this renderer removes the outputs of {@link Velocity2WikiPageExtractRenderer} from the thread.
 * The outputs stay while an outer page, which included this page in its Velocity v1.4 rendering, still renders.
 */
public class Velocity2WikiPageRestoreRenderer implements XWikiRenderer {

    @Override
    public String render(String content, XWikiDocument contentdoc, XWikiDocument contextdoc, XWikiContext context) {
        int depth = Velocity2WikiPageExtractRenderer.depth(context);
        if (depth > 0) {
            context.put(Velocity2WikiPageExtractRenderer.DEPTH_KEY, depth - 1);
            if (depth == 1) {
                Velocity2WikiOutputs.setOutputs(null);
            }
        }
        return content;
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
