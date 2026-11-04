package ch.sbb.polarion.extension.api_extender.velocity.wiki;

import ch.sbb.polarion.extension.generic.test_extensions.BundleJarsPrioritizingRunnableMockExtension;
import com.polarion.alm.wiki.IWikiService;
import com.polarion.platform.core.IPlatform;
import com.polarion.platform.core.PlatformContext;
import com.xpn.xwiki.XWikiContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(BundleJarsPrioritizingRunnableMockExtension.class)
class Velocity2WikiPageRenderersTest {

    private final Velocity2WikiPageExtractRenderer extract = new Velocity2WikiPageExtractRenderer();
    private final Velocity2WikiPageRestoreRenderer restore = new Velocity2WikiPageRestoreRenderer();
    private final XWikiContext context = new XWikiContext();
    private MockedStatic<PlatformContext> platformContext;

    @BeforeEach
    void setUp() {
        IWikiService wikiService = mock(IWikiService.class);
        when(wikiService.getWikiRenderingContextMap()).thenReturn(Map.of("greeting", "Hello"));
        IPlatform platform = mock(IPlatform.class);
        when(platform.lookupService(IWikiService.class)).thenReturn(wikiService);
        platformContext = mockStatic(PlatformContext.class);
        platformContext.when(PlatformContext::getPlatform).thenReturn(platform);
    }

    @AfterEach
    void tearDown() {
        platformContext.close();
    }

    @Test
    void contentWithoutMarkerIsUnchanged() {
        assertEquals("#set($x = 1)$x", extract.render("#set($x = 1)$x", null, null, context));
        assertEquals("text", restore.render("text", null, null, context));
    }

    @Test
    void extractAndRestoreRenderRegions() {
        String extracted = extract.render("1.4: $x {velocityV2}#set($d = 1.5)$greeting $d{velocityV2}", null, null, context);
        assertFalse(extracted.contains("velocityV2}"), extracted);
        assertFalse(extracted.contains("$greeting"), extracted);

        // Velocity 1.4 and wiki markup run between the two renderers.
        String rendered = "<p>" + extracted.replace("$x", "X") + "</p>";
        assertEquals("<p>1.4: X Hello 1.5</p>", restore.render(rendered, null, null, context));
    }

    @Test
    void includedPageTokensAreRestoredToo() {
        String outer = extract.render("{velocityV2}outer{velocityV2}", null, null, context);
        String inner = extract.render("{velocityV2}inner{velocityV2}", null, null, context);
        assertEquals("outer|inner", restore.render(outer + "|" + inner, null, null, context));
    }

    @Test
    void scriptErrorRendersMessage() {
        String extracted = extract.render("{velocityV2}#if({velocityV2}", null, null, context);
        assertTrue(restore.render(extracted, null, null, context).startsWith("<div class=\"polarion-errormessage\">"));
    }

    @Test
    void convertMethodsKeepContent() {
        assertEquals("all", extract.convertSingleLine("m", "p", "all", null, context));
        assertEquals("all", restore.convertMultiLine("m", "p", "d", "all", null, context));
    }
}
