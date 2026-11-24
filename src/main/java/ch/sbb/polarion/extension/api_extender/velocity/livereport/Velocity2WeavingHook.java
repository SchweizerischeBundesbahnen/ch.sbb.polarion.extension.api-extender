package ch.sbb.polarion.extension.api_extender.velocity.livereport;

import com.polarion.core.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.hooks.weaving.WeavingHook;
import org.osgi.framework.hooks.weaving.WovenClass;

import java.util.List;
import java.util.function.UnaryOperator;

/**
 * Weaves {@link Velocity2ScriptWeaver#TARGET_CLASS} and {@link Velocity2QueryWeaver#TARGET_CLASS} when Equinox loads them.
 * <p>
 * Equinox calls weaving hooks only for classes it has not loaded yet. The hook must be registered before the
 * first LiveReport renders, so the activator registers it in {@code start()}, before Polarion starts up.
 * After a hot redeploy onto a running Polarion the classes are already loaded, and the weave applies after a restart.
 */
public final class Velocity2WeavingHook implements WeavingHook {

    private static final Logger logger = Logger.getLogger(Velocity2WeavingHook.class);

    private final List<Target> targets = List.of(
            new Target(Velocity2ScriptWeaver.TARGET_CLASS, new Velocity2ScriptWeaver(Velocity2ScriptWeaver.TARGET_CLASS)::weave,
                    "{velocityV2} regions in the built-in Script widgets",
                    ".render() does not read the script field as expected",
                    "Velocity 2 rendering of {velocityV2} regions in LiveReport scripts is installed"),
            new Target(Velocity2QueryWeaver.TARGET_CLASS, new Velocity2QueryWeaver()::weave,
                    "{velocityV2} regions in widget queries",
                    " has no renderImpl(String) method",
                    "Velocity 2 rendering of {velocityV2} regions in widget queries is installed"));

    public static @NotNull ServiceRegistration<WeavingHook> register(@NotNull BundleContext context) {
        ServiceRegistration<WeavingHook> registration = context.registerService(WeavingHook.class, new Velocity2WeavingHook(), null);
        logger.info("Weaving hook for {velocityV2} regions in LiveReport scripts and widget queries is registered. It applies when Polarion loads "
                + Velocity2ScriptWeaver.TARGET_CLASS + " and " + Velocity2QueryWeaver.TARGET_CLASS + ", so a hot redeploy onto a running Polarion needs a restart.");
        return registration;
    }

    @Override
    public void weave(WovenClass wovenClass) {
        byte[] woven = weave(wovenClass.getClassName(), wovenClass.getBytes());
        if (woven != null) {
            wovenClass.setBytes(woven);
        }
    }

    /**
     * Tests call this method directly: Mockito cannot mock {@link WovenClass}, because {@code org.osgi.resource} is not on the test class path.
     *
     * @return the woven class, or {@code null} if the class stays unchanged
     */
    byte @Nullable [] weave(@NotNull String className, byte @NotNull [] classBytes) {
        for (Target target : targets) {
            if (target.className().equals(className)) {
                return target.weave(classBytes);
            }
        }
        return null;
    }

    private record Target(@NotNull String className, @NotNull UnaryOperator<byte[]> weaver,
                          @NotNull String feature, @NotNull String mismatch, @NotNull String installed) {

        private byte @Nullable [] weave(byte @NotNull [] classBytes) {
            try {
                byte[] woven = weaver.apply(classBytes);
                if (woven == null) {
                    logger.warn(className + mismatch + ", " + feature + " are not available");
                    return null;
                }
                logger.info(installed);
                return woven;
            } catch (RuntimeException e) {
                logger.error("Cannot weave " + className + ", " + feature + " are not available", e);
                return null;
            }
        }
    }
}
