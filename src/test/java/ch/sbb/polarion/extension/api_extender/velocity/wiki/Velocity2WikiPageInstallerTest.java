package ch.sbb.polarion.extension.api_extender.velocity.wiki;

import com.xpn.xwiki.render.DefaultXWikiRenderingEngine;
import com.xpn.xwiki.render.XWikiRenderer;
import jakarta.servlet.ServletContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class Velocity2WikiPageInstallerTest {

    private final XWikiRenderer velocity = mock(XWikiRenderer.class);
    private final XWikiRenderer wiki = mock(XWikiRenderer.class);
    private DefaultXWikiRenderingEngine engine;

    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        engine = mock(DefaultXWikiRenderingEngine.class);
        Map<String, XWikiRenderer> map = new LinkedHashMap<>();
        map.put("velocity", velocity);
        map.put("wiki", wiki);
        setField("renderers", new ArrayList<>(List.of(velocity, wiki)));
        setField("renderermap", map);
    }

    @Test
    void installAddsExtractFirstAndRestoreAfterVelocity() throws ReflectiveOperationException {
        assertTrue(Velocity2WikiPageInstaller.install(engine));

        List<?> renderers = (List<?>) getField("renderers");
        assertEquals(4, renderers.size());
        assertInstanceOf(Velocity2WikiPageExtractRenderer.class, renderers.get(0));
        assertSame(velocity, renderers.get(1));
        assertInstanceOf(Velocity2WikiPageRestoreRenderer.class, renderers.get(2));
        assertSame(wiki, renderers.get(3));
        assertEquals(List.of("velocity2", "velocity", "velocity2restore", "wiki"), new ArrayList<>(((Map<?, ?>) getField("renderermap")).keySet()));
    }

    @Test
    void withoutVelocityRendererRestoreFollowsExtract() throws ReflectiveOperationException {
        setField("renderers", new ArrayList<>(List.of(wiki)));
        setField("renderermap", new LinkedHashMap<>(Map.of("wiki", wiki)));
        assertTrue(Velocity2WikiPageInstaller.install(engine));

        List<?> renderers = (List<?>) getField("renderers");
        assertInstanceOf(Velocity2WikiPageExtractRenderer.class, renderers.get(0));
        assertInstanceOf(Velocity2WikiPageRestoreRenderer.class, renderers.get(1));
        assertSame(wiki, renderers.get(2));
        assertFalse(Velocity2WikiPageInstaller.install(engine));
    }

    @Test
    void installMovesRestoreOfOlderVersionAfterVelocity() throws ReflectiveOperationException {
        // An older version put the restore renderer at the end of the chain.
        XWikiRenderer extract = new Velocity2WikiPageExtractRenderer();
        XWikiRenderer restore = new Velocity2WikiPageRestoreRenderer();
        setField("renderers", new ArrayList<>(List.of(extract, velocity, wiki, restore)));
        Map<String, XWikiRenderer> map = new LinkedHashMap<>();
        map.put("velocity2", extract);
        map.put("velocity", velocity);
        map.put("wiki", wiki);
        map.put("velocity2restore", restore);
        setField("renderermap", map);
        assertTrue(Velocity2WikiPageInstaller.install(engine));

        List<?> renderers = (List<?>) getField("renderers");
        assertEquals(4, renderers.size());
        assertInstanceOf(Velocity2WikiPageRestoreRenderer.class, renderers.get(2));
        assertSame(wiki, renderers.get(3));
    }

    @Test
    void installReplacesListInsteadOfChangingIt() throws ReflectiveOperationException {
        Object before = getField("renderers");
        Velocity2WikiPageInstaller.install(engine);
        assertNotSame(before, getField("renderers"));
        assertEquals(2, ((List<?>) before).size());
    }

    @Test
    void secondInstallDoesNothing() throws ReflectiveOperationException {
        Velocity2WikiPageInstaller.install(engine);
        assertFalse(Velocity2WikiPageInstaller.install(engine));
        assertEquals(4, ((List<?>) getField("renderers")).size());
    }

    @Test
    void uninstallRestoresOriginalChain() throws ReflectiveOperationException {
        Velocity2WikiPageInstaller.install(engine);
        Velocity2WikiPageInstaller.uninstall(engine);

        assertEquals(List.of(velocity, wiki), getField("renderers"));
        assertEquals(List.of("velocity", "wiki"), new ArrayList<>(((Map<?, ?>) getField("renderermap")).keySet()));
    }

    @Test
    void startChecksWikiContextAndStopEndsChecks() {
        ServletContext context = mock(ServletContext.class);
        Velocity2WikiPageInstaller.start(context);
        Velocity2WikiPageInstaller.start(context);
        verify(context, timeout(5000)).getContext(Velocity2WikiPageInstaller.WIKI_CONTEXT_PATH);
        Velocity2WikiPageInstaller.stop();
        Velocity2WikiPageInstaller.stop();
    }

    @Test
    void contextWithoutXWikiIsSkipped() {
        ServletContext wikiContext = mock(ServletContext.class);
        when(wikiContext.getAttribute(Velocity2WikiPageInstaller.XWIKI_ATTRIBUTE)).thenReturn("not an XWiki");
        ServletContext context = mock(ServletContext.class);
        when(context.getContext(Velocity2WikiPageInstaller.WIKI_CONTEXT_PATH)).thenReturn(wikiContext);

        Velocity2WikiPageInstaller.start(context);
        verify(wikiContext, timeout(5000)).getAttribute(Velocity2WikiPageInstaller.XWIKI_ATTRIBUTE);
        assertDoesNotThrow(Velocity2WikiPageInstaller::stop);
    }

    @Test
    void failingCheckIsLogged() {
        ServletContext wikiContext = mock(ServletContext.class);
        when(wikiContext.getAttribute(Velocity2WikiPageInstaller.XWIKI_ATTRIBUTE)).thenThrow(new IllegalStateException("not ready")).thenReturn(null);
        ServletContext context = mock(ServletContext.class);
        when(context.getContext(Velocity2WikiPageInstaller.WIKI_CONTEXT_PATH)).thenReturn(wikiContext);

        Velocity2WikiPageInstaller.start(context);
        verify(wikiContext, timeout(5000)).getAttribute(Velocity2WikiPageInstaller.XWIKI_ATTRIBUTE);
        assertDoesNotThrow(Velocity2WikiPageInstaller::stop);
    }

    private void setField(String name, Object value) throws ReflectiveOperationException {
        Field field = DefaultXWikiRenderingEngine.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(engine, value);
    }

    private Object getField(String name) throws ReflectiveOperationException {
        Field field = DefaultXWikiRenderingEngine.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(engine);
    }
}
