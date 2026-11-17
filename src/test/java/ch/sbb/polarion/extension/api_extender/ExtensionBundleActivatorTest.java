package ch.sbb.polarion.extension.api_extender;

import ch.sbb.polarion.extension.api_extender.velocity.livereport.Velocity2ScriptHook;
import ch.sbb.polarion.extension.api_extender.velocity.wiki.Velocity2WikiRenderingAccessor;
import com.polarion.alm.tracker.internal.WikiRenderingProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.hooks.weaving.WeavingHook;

import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

class ExtensionBundleActivatorTest {

    /**
     * Runs nothing in the background, so the test calls {@link #onStart(BundleContext)} itself.
     */
    private static final class TestActivator extends ExtensionBundleActivator {
        @Override
        protected void runAsync(Runnable task) {
            // The deferred startup waits for the Guice platform, which does not exist in tests.
        }
    }

    @AfterEach
    void tearDown() {
        System.getProperties().remove(Velocity2ScriptHook.PROPERTY);
        WikiRenderingProvider.wikiRenderingAccessor = null;
    }

    @Test
    @SuppressWarnings("unchecked")
    void startInstallsVelocity2AndStopRemovesIt() {
        BundleContext context = mock(BundleContext.class);
        ServiceRegistration<WeavingHook> registration = mock(ServiceRegistration.class);
        when(context.registerService(eq(WeavingHook.class), any(WeavingHook.class), isNull())).thenReturn(registration);
        WikiRenderingProvider.IWikiRenderingAccessor original = mock(WikiRenderingProvider.IWikiRenderingAccessor.class);
        WikiRenderingProvider.wikiRenderingAccessor = original;
        TestActivator activator = new TestActivator();

        activator.start(context);
        activator.onStart(context);
        assertTrue(activator.getExtensions().isEmpty());
        assertInstanceOf(Velocity2WikiRenderingAccessor.class, WikiRenderingProvider.wikiRenderingAccessor);

        activator.stop(context);
        verify(registration).unregister();
        assertSame(original, WikiRenderingProvider.wikiRenderingAccessor);
        assertEquals("unchanged", ((BiFunction<Object, String, String>) System.getProperties().get(Velocity2ScriptHook.PROPERTY)).apply(null, "unchanged"));

        assertDoesNotThrow(() -> activator.stop(context));
    }
}
