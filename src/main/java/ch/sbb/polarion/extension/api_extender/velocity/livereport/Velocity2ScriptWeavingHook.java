package ch.sbb.polarion.extension.api_extender.velocity.livereport;

import com.polarion.core.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.hooks.weaving.WeavingHook;
import org.osgi.framework.hooks.weaving.WovenClass;

/**
 * Weaves {@link Velocity2ScriptWeaver#TARGET_CLASS} when Equinox loads it.
 * <p>
 * Equinox calls weaving hooks only for classes it has not loaded yet. The hook must be registered before the
 * first LiveReport renders, so the activator registers it in {@code start()}, before Polarion starts up.
 * After a hot redeploy onto a running Polarion the class is already loaded, and the weave applies after a restart.
 */
public final class Velocity2ScriptWeavingHook implements WeavingHook {

    private static final Logger logger = Logger.getLogger(Velocity2ScriptWeavingHook.class);

    private final Velocity2ScriptWeaver weaver = new Velocity2ScriptWeaver(Velocity2ScriptWeaver.TARGET_CLASS);

    public static @NotNull ServiceRegistration<WeavingHook> register(@NotNull BundleContext context) {
        ServiceRegistration<WeavingHook> registration = context.registerService(WeavingHook.class, new Velocity2ScriptWeavingHook(), null);
        logger.info("Weaving hook for {velocityV2} regions in LiveReport scripts is registered. It applies when Polarion loads "
                + Velocity2ScriptWeaver.TARGET_CLASS + ", so a hot redeploy onto a running Polarion needs a restart.");
        return registration;
    }

    @Override
    public void weave(WovenClass wovenClass) {
        if (!Velocity2ScriptWeaver.TARGET_CLASS.equals(wovenClass.getClassName())) {
            return;
        }
        try {
            byte[] woven = weaver.weave(wovenClass.getBytes());
            if (woven == null) {
                logger.warn(Velocity2ScriptWeaver.TARGET_CLASS + ".render() does not read the script field as expected, "
                        + "{velocityV2} regions in the built-in Script widgets are not available");
                return;
            }
            wovenClass.setBytes(woven);
            logger.info("Velocity 2 rendering of {velocityV2} regions in LiveReport scripts is installed");
        } catch (RuntimeException e) {
            logger.error("Cannot weave " + Velocity2ScriptWeaver.TARGET_CLASS + ", {velocityV2} regions in the built-in Script widgets are not available", e);
        }
    }
}
