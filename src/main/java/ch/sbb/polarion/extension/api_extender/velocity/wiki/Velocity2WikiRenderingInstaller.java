package ch.sbb.polarion.extension.api_extender.velocity.wiki;

import com.polarion.alm.tracker.internal.WikiRenderingProvider;
import com.polarion.core.util.logging.Logger;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;

/**
 * Puts {@link Velocity2WikiRenderingAccessor} in front of the accessor which Polarion's wiki plugin sets at its startup.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Velocity2WikiRenderingInstaller {

    private static final Logger logger = Logger.getLogger(Velocity2WikiRenderingInstaller.class);

    private static final long WAIT_TIMEOUT_MS = TimeUnit.MINUTES.toMillis(5);
    private static final long POLL_INTERVAL_MS = 1000;

    /**
     * Waits until the wiki plugin sets its accessor, then wraps it. Call it from a background thread.
     */
    public static void installWhenReady() {
        installWhenReady(WAIT_TIMEOUT_MS);
    }

    static void installWhenReady(long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        try {
            while (WikiRenderingProvider.wikiRenderingAccessor == null) {
                if (System.currentTimeMillis() > deadline) {
                    logger.warn("Polarion did not set the wiki rendering accessor in " + timeoutMs + " ms. Velocity 2 is not available in LiveDocs.");
                    return;
                }
                Thread.sleep(POLL_INTERVAL_MS);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        install();
    }

    static synchronized void install() {
        WikiRenderingProvider.IWikiRenderingAccessor current = unwrap(WikiRenderingProvider.wikiRenderingAccessor);
        if (current == null) {
            return;
        }
        WikiRenderingProvider.wikiRenderingAccessor = new Velocity2WikiRenderingAccessor(current);
        logger.info("Velocity 2 rendering of LiveDoc Wiki Content blocks is installed");
    }

    public static synchronized void uninstall() {
        WikiRenderingProvider.IWikiRenderingAccessor current = WikiRenderingProvider.wikiRenderingAccessor;
        if (current instanceof Velocity2WikiRenderingAccessor accessor) {
            WikiRenderingProvider.wikiRenderingAccessor = accessor.getDelegate();
        }
    }

    /**
     * After a hot redeploy without {@link #uninstall()}, the installed wrapper comes from the class loader of the previous bundle.
     * It is not an instance of this bundle's class, so it is found by name.
     */
    static @Nullable WikiRenderingProvider.IWikiRenderingAccessor unwrap(@Nullable WikiRenderingProvider.IWikiRenderingAccessor accessor) {
        WikiRenderingProvider.IWikiRenderingAccessor current = accessor;
        while (current != null && current.getClass().getName().equals(Velocity2WikiRenderingAccessor.class.getName())) {
            current = getDelegate(current);
        }
        return current;
    }

    private static @Nullable WikiRenderingProvider.IWikiRenderingAccessor getDelegate(@NotNull WikiRenderingProvider.IWikiRenderingAccessor wrapper) {
        try {
            return (WikiRenderingProvider.IWikiRenderingAccessor) wrapper.getClass().getMethod("getDelegate").invoke(wrapper);
        } catch (ReflectiveOperationException e) {
            logger.error("Cannot get the original wiki rendering accessor", e);
            return null;
        }
    }
}
