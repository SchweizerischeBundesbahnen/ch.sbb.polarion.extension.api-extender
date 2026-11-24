package ch.sbb.polarion.extension.api_extender.velocity.livereport;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.hooks.weaving.WeavingHook;
import org.osgi.framework.hooks.weaving.WovenClass;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

class Velocity2WeavingHookTest {

    private final Velocity2WeavingHook hook = new Velocity2WeavingHook();

    @Test
    @SuppressWarnings("unchecked")
    void registersAsWeavingHook() {
        BundleContext context = mock(BundleContext.class);
        ServiceRegistration<WeavingHook> registration = mock(ServiceRegistration.class);
        when(context.registerService(eq(WeavingHook.class), any(Velocity2WeavingHook.class), isNull())).thenReturn(registration);

        assertSame(registration, Velocity2WeavingHook.register(context));
    }

    @ParameterizedTest
    @ValueSource(strings = {Velocity2ScriptWeaver.TARGET_CLASS, Velocity2QueryWeaver.TARGET_CLASS})
    void weavesTargetClass(String className) throws IOException {
        byte[] original = classBytes(className);
        byte[] woven = hook.weave(className, original);

        assertNotNull(woven);
        assertFalse(Arrays.equals(original, woven));
    }

    @Test
    void setsWovenBytesOfTargetClass() throws IOException {
        WovenClass wovenClass = mock(WovenClass.class);
        when(wovenClass.getClassName()).thenReturn(Velocity2QueryWeaver.TARGET_CLASS);
        when(wovenClass.getBytes()).thenReturn(classBytes(Velocity2QueryWeaver.TARGET_CLASS));

        hook.weave(wovenClass);

        verify(wovenClass).setBytes(any(byte[].class));
    }

    @Test
    void leavesBytesOfOtherClassUnchanged() throws IOException {
        WovenClass wovenClass = mock(WovenClass.class);
        when(wovenClass.getClassName()).thenReturn(String.class.getName());
        when(wovenClass.getBytes()).thenReturn(classBytes(String.class.getName()));

        hook.weave(wovenClass);

        verify(wovenClass, never()).setBytes(any());
    }

    @Test
    void otherClassStaysUnchanged() throws IOException {
        assertNull(hook.weave(String.class.getName(), classBytes(String.class.getName())));
    }

    @ParameterizedTest
    @ValueSource(strings = {Velocity2ScriptWeaver.TARGET_CLASS, Velocity2QueryWeaver.TARGET_CLASS})
    void targetClassWithOtherShapeStaysUnchanged(String className) throws IOException {
        assertNull(hook.weave(className, classBytes(String.class.getName())));
    }

    @ParameterizedTest
    @ValueSource(strings = {Velocity2ScriptWeaver.TARGET_CLASS, Velocity2QueryWeaver.TARGET_CLASS})
    void invalidClassBytesStayUnchanged(String className) {
        assertNull(hook.weave(className, new byte[]{1, 2, 3}));
    }

    private static byte[] classBytes(String className) throws IOException {
        String resource = "/" + className.replace('.', '/') + ".class";
        try (InputStream stream = Objects.requireNonNull(Velocity2WeavingHookTest.class.getResourceAsStream(resource), resource)) {
            return stream.readAllBytes();
        }
    }
}
