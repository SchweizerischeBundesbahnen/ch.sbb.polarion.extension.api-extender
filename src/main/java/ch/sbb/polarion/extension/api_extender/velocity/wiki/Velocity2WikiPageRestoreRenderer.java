package ch.sbb.polarion.extension.api_extender.velocity.wiki;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.render.XWikiRenderer;
import com.xpn.xwiki.render.XWikiVirtualMacro;

import java.util.Map;

/**
 * The last renderer of Classic Wiki Pages. It puts the output of the {@code {velocityV2}} regions
 * in place of the tokens which {@link Velocity2WikiPageExtractRenderer} wrote.
 */
public class Velocity2WikiPageRestoreRenderer implements XWikiRenderer {

    @Override
    public String render(String content, XWikiDocument contentdoc, XWikiDocument contextdoc, XWikiContext context) {
        if (content == null || !(context.get(Velocity2WikiPageExtractRenderer.OUTPUTS_KEY) instanceof Map<?, ?> outputs)) {
            return content;
        }
        String result = content;
        for (Map.Entry<?, ?> entry : outputs.entrySet()) {
            String token = (String) entry.getKey();
            if (result.contains(token)) {
                result = result.replace(token, (String) entry.getValue());
            }
        }
        return result;
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
