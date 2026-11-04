package ch.sbb.polarion.extension.api_extender.velocity.wiki;

import com.xpn.xwiki.render.DefaultXWikiRenderingEngine;
import com.xpn.xwiki.render.XWikiRenderer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

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
    void installAddsRenderersAtBothEnds() throws ReflectiveOperationException {
        assertTrue(Velocity2WikiPageInstaller.install(engine));

        List<?> renderers = (List<?>) getField("renderers");
        assertEquals(4, renderers.size());
        assertInstanceOf(Velocity2WikiPageExtractRenderer.class, renderers.get(0));
        assertSame(velocity, renderers.get(1));
        assertSame(wiki, renderers.get(2));
        assertInstanceOf(Velocity2WikiPageRestoreRenderer.class, renderers.get(3));
        assertEquals(List.of("velocity2", "velocity", "wiki", "velocity2restore"), new ArrayList<>(((Map<?, ?>) getField("renderermap")).keySet()));
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
