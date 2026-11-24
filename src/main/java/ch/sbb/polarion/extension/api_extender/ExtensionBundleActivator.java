package ch.sbb.polarion.extension.api_extender;

import ch.sbb.polarion.extension.api_extender.velocity.livereport.Velocity2QueryHook;
import ch.sbb.polarion.extension.api_extender.velocity.livereport.Velocity2ScriptHook;
import ch.sbb.polarion.extension.api_extender.velocity.livereport.Velocity2WeavingHook;
import ch.sbb.polarion.extension.api_extender.velocity.wiki.Velocity2WikiRenderingInstaller;
import ch.sbb.polarion.extension.generic.GenericBundleActivator;
import com.polarion.alm.ui.server.forms.extensions.IFormExtension;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.hooks.weaving.WeavingHook;

import java.util.Map;

public class ExtensionBundleActivator extends GenericBundleActivator {

    private ServiceRegistration<WeavingHook> weavingHook;

    @Override
    protected Map<String, IFormExtension> getExtensions() {
        return Map.of();
    }

    /**
     * The weaving hook must exist before Polarion loads the LiveReport script and widget query renderers.
     * {@link #onStart(BundleContext)} runs later, after the Guice platform is ready.
     */
    @Override
    public void start(BundleContext context) {
        Velocity2ScriptHook.install();
        Velocity2QueryHook.install();
        weavingHook = Velocity2WeavingHook.register(context);
        super.start(context);
    }

    @Override
    protected void onStart(BundleContext context) {
        Velocity2WikiRenderingInstaller.installWhenReady();
    }

    @Override
    public void stop(BundleContext context) {
        super.stop(context);
        if (weavingHook != null) {
            weavingHook.unregister();
            weavingHook = null;
        }
        Velocity2ScriptHook.uninstall();
        Velocity2QueryHook.uninstall();
        Velocity2WikiRenderingInstaller.uninstall();
    }
}
