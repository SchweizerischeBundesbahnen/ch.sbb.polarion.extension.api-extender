package ch.sbb.polarion.extension.api_extender.velocity.wiki;

import ch.sbb.polarion.extension.generic.test_extensions.BundleJarsPrioritizingRunnableMockExtension;
import com.polarion.alm.tracker.internal.WikiRenderingProvider;
import com.polarion.alm.tracker.model.IModule;
import com.polarion.alm.wiki.IWikiService;
import com.polarion.platform.core.IPlatform;
import com.polarion.platform.core.PlatformContext;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.api.Context;
import org.apache.velocity.VelocityContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(BundleJarsPrioritizingRunnableMockExtension.class)
class Velocity2WikiRenderingAccessorTest {

    private final WikiRenderingProvider.IWikiRenderingAccessor delegate = mock(WikiRenderingProvider.IWikiRenderingAccessor.class);
    private final Velocity2WikiRenderingAccessor accessor = new Velocity2WikiRenderingAccessor(delegate);
    private final IWikiService wikiService = mock(IWikiService.class);
    private MockedStatic<PlatformContext> platformContext;

    @BeforeEach
    void setUp() {
        when(wikiService.getWikiRenderingContextMap()).thenReturn(Map.of("greeting", "Hello"));
        IPlatform platform = mock(IPlatform.class);
        when(platform.lookupService(IWikiService.class)).thenReturn(wikiService);
        platformContext = mockStatic(PlatformContext.class);
        platformContext.when(PlatformContext::getPlatform).thenReturn(platform);

        // The delegate stands in for Velocity v1.4 and wiki markup: it resolves the $velocityV2Output references,
        // then wraps the result in a paragraph.
        Velocity2WikiOutputs outputs = new Velocity2WikiOutputs();
        when(delegate.render(any(), anyString(), any(), anyBoolean(), any(), any()))
                .thenAnswer(invocation -> "<p>" + REFERENCE.matcher((String) invocation.getArgument(1))
                        .replaceAll(match -> Matcher.quoteReplacement(String.valueOf(outputs.get(match.group(1))))) + "</p>");
    }

    private static final Pattern REFERENCE = Pattern.compile("\\$\\{velocityV2Output\\.get\\('([a-z0-9]+)', \\$context\\)}");

    @Test
    void regionBecomesVelocityReference() {
        accessor.render(null, "a {velocityV2}x{velocityV2}.txt", null, false, null, null);
        verify(delegate).render(isNull(), matches("a \\$\\{velocityV2Output\\.get\\('velocitytwo[a-z0-9]+', \\$context\\)}\\.txt"), isNull(), eq(false), isNull(), isNull());
    }

    @Test
    void regionsGetXWikiVariablesFromVelocity14Context() {
        VelocityContext velocity14Context = new VelocityContext();
        velocity14Context.put("request", "the request");
        velocity14Context.put("pageParameters", "the parameters");
        velocity14Context.put("v14only", "hidden");
        XWikiContext xwikiContext = new XWikiContext();
        xwikiContext.put("vcontext", velocity14Context);
        Context contextApi = new Context(xwikiContext);
        Velocity2WikiOutputs outputs = new Velocity2WikiOutputs();
        when(delegate.render(any(), anyString(), any(), anyBoolean(), any(), any()))
                .thenAnswer(invocation -> REFERENCE.matcher((String) invocation.getArgument(1))
                        .replaceAll(match -> Matcher.quoteReplacement(String.valueOf(outputs.get(match.group(1), contextApi)))));

        String html = accessor.render(null, "{velocityV2}$request|$pageParameters|$!v14only{velocityV2}", null, false, null, null);
        assertEquals("the request|the parameters|", html);
    }

    @Test
    void regionsRenderOnceOnFirstReference() {
        assertEquals("<p>A|B</p>", accessor.render(null, "{velocityV2}A{velocityV2}|{velocityV2}B{velocityV2}", null, false, null, null));
        verify(wikiService, times(1)).getWikiRenderingContextMap();
    }

    @Test
    void regionsDoNotRenderWithoutEvaluatedReference() {
        when(delegate.render(any(), anyString(), any(), anyBoolean(), any(), any())).thenReturn("skipped");
        assertEquals("skipped", accessor.render(null, "{velocityV2}A{velocityV2}", null, false, null, null));
        verify(wikiService, never()).getWikiRenderingContextMap();
    }

    @Test
    void outputsAreAvailableOnlyDuringRendering() {
        accessor.render(null, "{velocityV2}x{velocityV2}", null, false, null, null);
        assertNull(new Velocity2WikiOutputs().get("anything"));
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
        String html = accessor.render(null, "1.4: $x {velocityV2}#set($d = 1.5)$greeting $d{velocityV2}", null, false, null, null);
        assertEquals("<p>1.4: $x Hello 1.5</p>", html);
    }

    @Test
    void regionsShareOneContext() {
        String html = accessor.render(null, "{velocityV2}#set($a = 'A'){velocityV2}|{velocityV2}$a{velocityV2}", null, false, null, null);
        assertEquals("<p>|A</p>", html);
    }

    @Test
    void documentIsAVariable() {
        IModule document = mock(IModule.class, RETURNS_DEEP_STUBS);
        when(document.getModuleName()).thenReturn("Spec");
        String html = accessor.render(document, "{velocityV2}$document.moduleName{velocityV2}", null, false, null, null);
        assertEquals("<p>Spec</p>", html);
    }

    @Test
    void errorRendersMessageInPlaceOfRegion() {
        String html = accessor.render(null, "ok {velocityV2}#if({velocityV2}", null, false, null, null);
        assertTrue(html.startsWith("<p>ok <div class=\"polarion-errormessage\">"), html);
    }

    @Test
    void delegateResultNullStaysNull() {
        when(delegate.render(any(), anyString(), any(), anyBoolean(), any(), any())).thenReturn(null);
        assertNull(accessor.render(null, "{velocityV2}x{velocityV2}", null, false, null, null));
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
