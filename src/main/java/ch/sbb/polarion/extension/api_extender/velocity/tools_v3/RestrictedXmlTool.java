package ch.sbb.polarion.extension.api_extender.velocity.tools_v3;

import org.apache.velocity.tools.generic.XmlTool;

/**
 * Any page editor can run scripts, so loading from URLs (server-side request forgery)
 * and from the class path of the bundle is blocked. Only {@link #parse(String)} stays available.
 * <p>
 * The XML parser of Velocity Tools 3 already blocks external entities and DTDs.
 * Blocked methods return {@code null}, see {@link RestrictedJsonTool}.
 */
public class RestrictedXmlTool extends XmlTool {

    @Override
    public XmlTool read(String resource) {
        return null;
    }

    @Override
    public XmlTool fetch(String url) {
        return null;
    }
}
