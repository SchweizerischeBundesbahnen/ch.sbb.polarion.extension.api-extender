package ch.sbb.polarion.extension.api_extender.velocity.wiki;

import com.polarion.core.util.logging.Logger;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.render.DefaultXWikiRenderingEngine;
import com.xpn.xwiki.render.XWikiRenderer;
import jakarta.servlet.ServletContext;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Adds {@link Velocity2WikiPageExtractRenderer} and {@link Velocity2WikiPageRestoreRenderer} to the rendering engine of Classic Wiki Pages.
 * <p>
 * The engine belongs to the XWiki instance which the {@code /polarion/wiki} web application keeps in its servlet context.
 * It exists only after the first wiki request, and XWiki replaces it on a cache reset. So a background task checks it periodically.
 * The engine has no API to add a renderer at the start of the chain, so its private fields are replaced by reflection.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Velocity2WikiPageInstaller {

    private static final Logger logger = Logger.getLogger(Velocity2WikiPageInstaller.class);

    static final String WIKI_CONTEXT_PATH = "/polarion/wiki";
    static final String XWIKI_ATTRIBUTE = "xwiki";
    static final String EXTRACT_RENDERER_NAME = "velocity2";
    static final String RESTORE_RENDERER_NAME = "velocity2restore";
    private static final Set<String> OWN_RENDERER_CLASSES = Set.of(
            Velocity2WikiPageExtractRenderer.class.getName(), Velocity2WikiPageRestoreRenderer.class.getName());
    private static final long CHECK_INTERVAL_SECONDS = 10;

    private static ScheduledExecutorService scheduler;
    private static ServletContext servletContext;

    /**
     * @param context any servlet context of this extension; Polarion allows cross-context access, so it reaches the wiki web application
     */
    public static synchronized void start(@NotNull ServletContext context) {
        servletContext = context;
        if (scheduler == null) {
            scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
                Thread thread = new Thread(runnable, "velocity2-wiki-page-installer");
                thread.setDaemon(true);
                return thread;
            });
            scheduler.scheduleWithFixedDelay(Velocity2WikiPageInstaller::check, 0, CHECK_INTERVAL_SECONDS, TimeUnit.SECONDS);
        }
    }

    public static synchronized void stop() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
        DefaultXWikiRenderingEngine engine = findEngine();
        if (engine != null) {
            uninstall(engine);
        }
        servletContext = null;
    }

    private static void check() {
        try {
            DefaultXWikiRenderingEngine engine = findEngine();
            if (engine != null) {
                install(engine);
            }
        } catch (RuntimeException e) {
            logger.error("Cannot add Velocity 2 renderers to Classic Wiki Pages", e);
        }
    }

    private static synchronized @Nullable DefaultXWikiRenderingEngine findEngine() {
        ServletContext wikiContext = servletContext != null ? servletContext.getContext(WIKI_CONTEXT_PATH) : null;
        if (wikiContext != null && wikiContext.getAttribute(XWIKI_ATTRIBUTE) instanceof XWiki xwiki
                && xwiki.getRenderingEngine() instanceof DefaultXWikiRenderingEngine engine) {
            return engine;
        }
        return null;
    }

    /**
     * @return {@code true} if the renderers were added, {@code false} if this bundle's renderers were already there
     */
    static synchronized boolean install(@NotNull DefaultXWikiRenderingEngine engine) {
        List<XWikiRenderer> renderers = getRenderers(engine);
        if (!renderers.isEmpty() && renderers.get(0) instanceof Velocity2WikiPageExtractRenderer
                && renderers.get(renderers.size() - 1) instanceof Velocity2WikiPageRestoreRenderer) {
            return false;
        }
        List<XWikiRenderer> newRenderers = new ArrayList<>();
        newRenderers.add(new Velocity2WikiPageExtractRenderer());
        renderers.stream().filter(renderer -> !isOwn(renderer)).forEach(newRenderers::add);
        newRenderers.add(new Velocity2WikiPageRestoreRenderer());

        Map<String, XWikiRenderer> newRendererMap = new LinkedHashMap<>();
        newRendererMap.put(EXTRACT_RENDERER_NAME, newRenderers.get(0));
        getRendererMap(engine).forEach((name, renderer) -> {
            if (!isOwn(renderer)) {
                newRendererMap.put(name, renderer);
            }
        });
        newRendererMap.put(RESTORE_RENDERER_NAME, newRenderers.get(newRenderers.size() - 1));

        setRenderers(engine, newRenderers, newRendererMap);
        logger.info("Velocity 2 rendering of Classic Wiki Pages is installed");
        return true;
    }

    static synchronized void uninstall(@NotNull DefaultXWikiRenderingEngine engine) {
        List<XWikiRenderer> newRenderers = new ArrayList<>(getRenderers(engine).stream().filter(renderer -> !isOwn(renderer)).toList());
        Map<String, XWikiRenderer> newRendererMap = new LinkedHashMap<>();
        getRendererMap(engine).forEach((name, renderer) -> {
            if (!isOwn(renderer)) {
                newRendererMap.put(name, renderer);
            }
        });
        setRenderers(engine, newRenderers, newRendererMap);
    }

    /**
     * Renderers of a previous bundle version come from another class loader, so they are found by class name.
     */
    private static boolean isOwn(@NotNull XWikiRenderer renderer) {
        return OWN_RENDERER_CLASSES.contains(renderer.getClass().getName());
    }

    @SuppressWarnings("unchecked")
    private static @NotNull List<XWikiRenderer> getRenderers(@NotNull DefaultXWikiRenderingEngine engine) {
        return new ArrayList<>((List<XWikiRenderer>) readField(engine, "renderers"));
    }

    @SuppressWarnings("unchecked")
    private static @NotNull Map<String, XWikiRenderer> getRendererMap(@NotNull DefaultXWikiRenderingEngine engine) {
        return new LinkedHashMap<>((Map<String, XWikiRenderer>) readField(engine, "renderermap"));
    }

    /**
     * Rendering threads iterate over the renderer list without a lock.
     * New collections replace the old ones, so a running rendering keeps iterating over a list which does not change.
     */
    private static void setRenderers(@NotNull DefaultXWikiRenderingEngine engine, @NotNull List<XWikiRenderer> renderers, @NotNull Map<String, XWikiRenderer> rendererMap) {
        // getRendererList() casts the list to ArrayList, and the map field is declared as HashMap.
        writeField(engine, "renderermap", new LinkedHashMap<>(rendererMap));
        writeField(engine, "renderers", new ArrayList<>(renderers));
    }

    private static Object readField(@NotNull Object target, @NotNull String name) {
        try {
            Field field = DefaultXWikiRenderingEngine.class.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(target);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot read " + name + " of the XWiki rendering engine", e);
        }
    }

    private static void writeField(@NotNull Object target, @NotNull String name, @NotNull Object value) {
        try {
            Field field = DefaultXWikiRenderingEngine.class.getDeclaredField(name);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot write " + name + " of the XWiki rendering engine", e);
        }
    }
}
