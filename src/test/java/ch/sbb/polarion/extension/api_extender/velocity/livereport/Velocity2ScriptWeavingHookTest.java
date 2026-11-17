package ch.sbb.polarion.extension.api_extender.velocity.livereport;

import org.junit.jupiter.api.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.hooks.weaving.WeavingHook;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

class Velocity2ScriptWeavingHookTest {

    private final Velocity2ScriptWeavingHook hook = new Velocity2ScriptWeavingHook();

    @Test
    @SuppressWarnings("unchecked")
    void registersAsWeavingHook() {
        BundleContext context = mock(BundleContext.class);
        ServiceRegistration<WeavingHook> registration = mock(ServiceRegistration.class);
        when(context.registerService(eq(WeavingHook.class), any(Velocity2ScriptWeavingHook.class), isNull())).thenReturn(registration);

        assertSame(registration, Velocity2ScriptWeavingHook.register(context));
    }

    @Test
    void weavesTargetClass() throws IOException {
        byte[] original = classBytes("/" + Velocity2ScriptWeaver.TARGET_CLASS.replace('.', '/') + ".class");
        byte[] woven = hook.weave(original);

        assertNotNull(woven);
        assertFalse(Arrays.equals(original, woven));
    }

    @Test
    void classWithoutScriptFieldStaysUnchanged() throws IOException {
        assertNull(hook.weave(classBytes("/java/lang/String.class")));
    }

    @Test
    void invalidClassBytesStayUnchanged() {
        assertNull(hook.weave(new byte[]{1, 2, 3}));
    }

    private static byte[] classBytes(String resource) throws IOException {
        try (InputStream stream = Objects.requireNonNull(Velocity2ScriptWeavingHookTest.class.getResourceAsStream(resource), resource)) {
            return stream.readAllBytes();
        }
    }
}
