package ch.sbb.polarion.extension.api_extender;

import ch.sbb.polarion.extension.api_extender.velocity.livedoc.Velocity2WikiRenderingInstaller;
import ch.sbb.polarion.extension.generic.GenericBundleActivator;
import com.polarion.alm.ui.server.forms.extensions.IFormExtension;
import org.osgi.framework.BundleContext;

import java.util.Map;

public class ExtensionBundleActivator extends GenericBundleActivator {

    @Override
    protected Map<String, IFormExtension> getExtensions() {
        return Map.of();
    }

    @Override
    protected void onStart(BundleContext context) {
        Velocity2WikiRenderingInstaller.installWhenReady();
    }

    @Override
    public void stop(BundleContext context) {
        super.stop(context);
        Velocity2WikiRenderingInstaller.uninstall();
    }
}
