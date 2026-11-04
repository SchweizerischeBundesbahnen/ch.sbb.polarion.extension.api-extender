package ch.sbb.polarion.extension.api_extender.velocity.wiki;

import ch.sbb.polarion.extension.generic.test_extensions.BundleJarsPrioritizingRunnableMockExtension;
import com.polarion.alm.tracker.internal.WikiRenderingProvider;
import com.polarion.alm.tracker.model.IModule;
import com.polarion.alm.wiki.IWikiService;
import com.polarion.platform.core.IPlatform;
import com.polarion.platform.core.PlatformContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(BundleJarsPrioritizingRunnableMockExtension.class)
class Velocity2WikiRenderingAccessorTest {

    private final WikiRenderingProvider.IWikiRenderingAccessor delegate = mock(WikiRenderingProvider.IWikiRenderingAccessor.class);
    private final Velocity2WikiRenderingAccessor accessor = new Velocity2WikiRenderingAccessor(delegate);
    private MockedStatic<PlatformContext> platformContext;

    @BeforeEach
    void setUp() {
        IWikiService wikiService = mock(IWikiService.class);
        when(wikiService.getWikiRenderingContextMap()).thenReturn(Map.of("greeting", "Hello"));
        IPlatform platform = mock(IPlatform.class);
        when(platform.lookupService(IWikiService.class)).thenReturn(wikiService);
        platformContext = mockStatic(PlatformContext.class);
        platformContext.when(PlatformContext::getPlatform).thenReturn(platform);

        // The delegate stands in for Velocity 1.4 and wiki markup: it wraps the source in a paragraph.
        when(delegate.render(any(), anyString(), any(), anyBoolean(), any(), any()))
                .thenAnswer(invocation -> "<p>" + invocation.getArgument(1) + "</p>");
    }

    @AfterEach
    void tearDown() {
        platformContext.close();
    }

    @Test
    void sourceWithoutMarkerGoesToDelegateUnchanged() {
        assertEquals("<p>#set($x = 1)$x</p>", accessor.render(null, "#set($x = 1)$x", null, false, null, null));
        verify(delegate).render(null, "#set($x = 1)$x", null, false, null, null);
    }

    @Test
    void rendersRegionsWithVelocity2() {
        String html = accessor.render(null, "1.4: $x {velocity2}#set($d = 1.5)$greeting $d{velocity2}", null, false, null, null);
        assertEquals("<p>1.4: $x Hello 1.5</p>", html);
    }

    @Test
    void regionsShareOneContext() {
        String html = accessor.render(null, "{velocity2}#set($a = 'A'){velocity2}|{velocity2}$a{velocity2}", null, false, null, null);
        assertEquals("<p>|A</p>", html);
    }

    @Test
    void documentIsAVariable() {
        IModule document = mock(IModule.class, RETURNS_DEEP_STUBS);
        when(document.getModuleName()).thenReturn("Spec");
        String html = accessor.render(document, "{velocity2}$document.moduleName{velocity2}", null, false, null, null);
        assertEquals("<p>Spec</p>", html);
    }

    @Test
    void errorRendersMessageInPlaceOfRegion() {
        String html = accessor.render(null, "ok {velocity2}#if({velocity2}", null, false, null, null);
        assertTrue(html.startsWith("<p>ok <div class=\"polarion-errormessage\">"), html);
    }

    @Test
    void delegateResultNullStaysNull() {
        when(delegate.render(any(), anyString(), any(), anyBoolean(), any(), any())).thenReturn(null);
        assertNull(accessor.render(null, "{velocity2}x{velocity2}", null, false, null, null));
    }

    @Test
    void errorMessageIsEscaped() {
        assertEquals("<div class=\"polarion-errormessage\"><span class=\"polarion-messagetype\">Velocity 2 error: </span>&lt;b&gt; &amp; &quot;</div>",
                Velocity2WikiScripts.renderError("<b> & \""));
    }

    @Test
    void noScriptsNoRendering() {
        assertEquals(List.of(), Velocity2WikiScripts.render(List.of(), Map.of(), "test"));
    }
}
