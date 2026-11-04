package ch.sbb.polarion.extension.api_extender;

import ch.sbb.polarion.extension.api_extender.velocity.wiki.Velocity2WikiPageInstaller;
import ch.sbb.polarion.extension.generic.GenericUiServlet;

import java.io.Serial;

public class ApiExtenderAppServlet extends GenericUiServlet {

    @Serial
    private static final long serialVersionUID = 4272543916738749821L;

    public ApiExtenderAppServlet() {
        super("api-extender-app");
    }

    /**
     * The servlet loads on startup, so this is the earliest servlet context of the extension.
     */
    @Override
    public void init() {
        Velocity2WikiPageInstaller.start(getServletContext());
    }

    @Override
    public void destroy() {
        Velocity2WikiPageInstaller.stop();
        super.destroy();
    }

}
