package ch.sbb.polarion.extension.api_extender.velocity.tools_v3;

import org.apache.velocity.tools.generic.JsonTool;

import java.io.Reader;

/**
 * Any page editor can run scripts, so loading from URLs (server-side request forgery)
 * and from the class path of the bundle is blocked. Only {@link #parse(String)} stays available.
 * <p>
 * Blocked methods return {@code null} instead of throwing: an exception in a Wiki Content block
 * breaks the rendering of the whole block, a {@code null} only leaves the reference as text.
 */
public class RestrictedJsonTool extends JsonTool {

    @Override
    public JsonTool fetch(String url) {
        return null;
    }

    @Override
    public JsonTool read(String resource) {
        return null;
    }

    @Override
    public String acquireString(String url) {
        return null;
    }

    @Override
    public Reader acquireReader(String url) {
        return null;
    }

    @Override
    public Reader getResourceReader(String resource) {
        return null;
    }
}
