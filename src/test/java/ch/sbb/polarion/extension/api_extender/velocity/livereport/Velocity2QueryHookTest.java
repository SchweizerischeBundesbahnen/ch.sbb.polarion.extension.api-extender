package ch.sbb.polarion.extension.api_extender.velocity.livereport;

import ch.sbb.polarion.extension.generic.test_extensions.BundleJarsPrioritizingRunnableMockExtension;
import com.polarion.core.util.exceptions.UserFriendlyRuntimeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(BundleJarsPrioritizingRunnableMockExtension.class)
class Velocity2QueryHookTest {

    private final Velocity2QueryHook hook = new Velocity2QueryHook();
    private final FakeQueryRenderer renderer = new FakeQueryRenderer();

    @Test
    void queryWithoutMarkerIsUnchanged() {
        String query = "status:$x";
        assertSame(query, hook.apply(renderer, query));
        assertNull(hook.apply(renderer, null));
    }

    @Test
    void regionBecomesItsOutput() {
        renderer.velocityContext().put("type", "requirement");

        String result = hook.apply(renderer, "type:$type AND {velocityV2}#set($m = {'s': 'draft'})status:$m.s AND type:$type{velocityV2} AND id:x");

        assertEquals("type:$type AND status:draft AND type:requirement AND id:x", result);
    }

    @Test
    void regionsShareOneContext() {
        assertEquals("a:2", hook.apply(renderer, "{velocityV2}#set($a = 2){velocityV2}a:{velocityV2}$a{velocityV2}"));
    }

    @Test
    void outputIsEscapedForVelocity14() {
        String result = hook.apply(renderer, "{velocityV2}#set($s = 'a$b#c\\$d')$s{velocityV2}");

        assertEquals("a${esc.d}b${esc.h}c${esc.b}${esc.d}d", result);
    }

    @Test
    void failingRegionThrowsUserFriendlyException() {
        UserFriendlyRuntimeException e = assertThrows(UserFriendlyRuntimeException.class, () -> hook.apply(renderer, "{velocityV2}#if({velocityV2}"));
        assertTrue(e.getMessage().startsWith(Velocity2QueryHook.ERROR_PREFIX), e.getMessage());
    }

    @Test
    void rendererWithoutRenderingContextThrowsUserFriendlyException() {
        Object other = new Object();
        UserFriendlyRuntimeException e = assertThrows(UserFriendlyRuntimeException.class, () -> hook.apply(other, "{velocityV2}x{velocityV2}"));
        assertTrue(e.getMessage().startsWith(Velocity2QueryHook.ERROR_PREFIX), e.getMessage());
    }

    @Test
    void installAndUninstallSetTheSystemProperty() {
        try {
            Velocity2QueryHook.install();
            assertInstanceOf(Velocity2QueryHook.class, System.getProperties().get(Velocity2QueryHook.PROPERTY));
            Velocity2QueryHook.uninstall();
            assertFalse(System.getProperties().get(Velocity2QueryHook.PROPERTY) instanceof Velocity2QueryHook);
        } finally {
            System.getProperties().remove(Velocity2QueryHook.PROPERTY);
        }
    }
}
