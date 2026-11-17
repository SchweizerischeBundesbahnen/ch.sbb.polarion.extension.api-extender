package ch.sbb.polarion.extension.api_extender.velocity.wiki;

import com.polarion.alm.tracker.internal.WikiRenderingProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class Velocity2WikiRenderingInstallerTest {

    private final WikiRenderingProvider.IWikiRenderingAccessor original = mock(WikiRenderingProvider.IWikiRenderingAccessor.class);

    @BeforeEach
    void setUp() {
        WikiRenderingProvider.wikiRenderingAccessor = original;
    }

    @AfterEach
    void tearDown() {
        WikiRenderingProvider.wikiRenderingAccessor = null;
    }

    @Test
    void installWrapsAndUninstallRestores() {
        Velocity2WikiRenderingInstaller.install();
        Velocity2WikiRenderingAccessor installed = assertInstanceOf(Velocity2WikiRenderingAccessor.class, WikiRenderingProvider.wikiRenderingAccessor);
        assertSame(original, installed.getDelegate());

        Velocity2WikiRenderingInstaller.uninstall();
        assertSame(original, WikiRenderingProvider.wikiRenderingAccessor);
    }

    @Test
    void secondInstallDoesNotWrapTwice() {
        Velocity2WikiRenderingInstaller.install();
        Velocity2WikiRenderingInstaller.install();
        Velocity2WikiRenderingAccessor installed = assertInstanceOf(Velocity2WikiRenderingAccessor.class, WikiRenderingProvider.wikiRenderingAccessor);
        assertSame(original, installed.getDelegate());
    }

    @Test
    void uninstallKeepsForeignAccessor() {
        Velocity2WikiRenderingInstaller.uninstall();
        assertSame(original, WikiRenderingProvider.wikiRenderingAccessor);
    }

    @Test
    void installWithoutAccessorDoesNothing() {
        WikiRenderingProvider.wikiRenderingAccessor = null;
        Velocity2WikiRenderingInstaller.install();
        assertNull(WikiRenderingProvider.wikiRenderingAccessor);
    }

    @Test
    void installWhenReadyInstallsImmediatelyIfAccessorIsSet() {
        Velocity2WikiRenderingInstaller.installWhenReady();
        assertInstanceOf(Velocity2WikiRenderingAccessor.class, WikiRenderingProvider.wikiRenderingAccessor);
    }

    @Test
    void installWhenReadyWaitsForAccessor() throws InterruptedException {
        WikiRenderingProvider.wikiRenderingAccessor = null;
        Thread installer = new Thread(Velocity2WikiRenderingInstaller::installWhenReady);
        installer.start();
        while (installer.getState() != Thread.State.TIMED_WAITING) {
            Thread.onSpinWait();
        }
        WikiRenderingProvider.wikiRenderingAccessor = original;
        installer.join(5000);

        assertFalse(installer.isAlive());
        Velocity2WikiRenderingAccessor installed = assertInstanceOf(Velocity2WikiRenderingAccessor.class, WikiRenderingProvider.wikiRenderingAccessor);
        assertSame(original, installed.getDelegate());
    }

    @Test
    void installWhenReadyStopsOnInterrupt() throws InterruptedException {
        WikiRenderingProvider.wikiRenderingAccessor = null;
        Thread installer = new Thread(Velocity2WikiRenderingInstaller::installWhenReady);
        installer.start();
        installer.interrupt();
        installer.join(5000);

        assertFalse(installer.isAlive());
        assertNull(WikiRenderingProvider.wikiRenderingAccessor);
    }

    @Test
    void installWhenReadyGivesUpAfterTimeout() {
        WikiRenderingProvider.wikiRenderingAccessor = null;
        Velocity2WikiRenderingInstaller.installWhenReady(0);
        assertNull(WikiRenderingProvider.wikiRenderingAccessor);
    }
}
