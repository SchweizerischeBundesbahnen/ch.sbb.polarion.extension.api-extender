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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    @AfterEach
    void removeOutputs() {
        Velocity2WikiOutputs.setOutputs(null);
    }

    private static final Pattern REFERENCE = Pattern.compile("\\$\\{velocityV2Output\\.get\\('([a-z0-9]+)'\\)}");

    /**
     * Stands in for Velocity v1.4, which runs between the two renderers and resolves the $velocityV2Output references.
     */
    private static String velocity14(String source) {
        Velocity2WikiOutputs outputs = new Velocity2WikiOutputs();
        return REFERENCE.matcher(source).replaceAll(match -> Matcher.quoteReplacement(String.valueOf(outputs.get(match.group(1)))));
    }

    @Test
    void contentWithoutMarkerIsUnchanged() {
        assertEquals("#set($x = 1)$x", extract.render("#set($x = 1)$x", null, null, context));
        assertEquals("text", restore.render("text", null, null, context));
    }

    @Test
    void regionBecomesVelocityReference() {
        String extracted = extract.render("a {velocityV2}x{velocityV2}.txt", null, null, context);
        assertTrue(extracted.matches("a \\$\\{velocityV2Output\\.get\\('velocitytwo[a-z0-9]+'\\)}\\.txt"), extracted);
    }

    @Test
    void velocity14GetsRegionOutputs() {
        String extracted = extract.render("1.4: $x {velocityV2}#set($d = 1.5)$greeting $d{velocityV2}", null, null, context);
        assertFalse(extracted.contains("$greeting"), extracted);
        assertEquals("1.4: $x Hello 1.5", velocity14(extracted));
        assertEquals("1.4: $x Hello 1.5", restore.render("1.4: $x Hello 1.5", null, null, context));
    }

    @Test
    void restoreRemovesOutputsFromThread() {
        String extracted = extract.render("{velocityV2}x{velocityV2}", null, null, context);
        restore.render(velocity14(extracted), null, null, context);
        assertEquals("null", velocity14(extracted));
    }

    @Test
    void outputsStayWhileOuterPageRenders() {
        String outer = extract.render("{velocityV2}outer{velocityV2}", null, null, context);
        // A page which Velocity v1.4 of the outer page includes, with and without regions.
        String inner = extract.render("{velocityV2}inner{velocityV2}", null, null, context);
        restore.render(velocity14(inner), null, null, context);
        extract.render("plain", null, null, context);
        restore.render("plain", null, null, context);

        assertEquals("outer|inner", velocity14(outer + "|" + inner));
        restore.render("outer|inner", null, null, context);
        assertEquals("null", velocity14(outer));
    }

    @Test
    void scriptErrorRendersMessage() {
        String extracted = extract.render("{velocityV2}#if({velocityV2}", null, null, context);
        assertTrue(velocity14(extracted).startsWith("<div class=\"polarion-errormessage\">"));
    }

    @Test
    void convertMethodsKeepContent() {
        assertEquals("all", extract.convertSingleLine("m", "p", "all", null, context));
        assertEquals("all", restore.convertMultiLine("m", "p", "d", "all", null, context));
    }
}
