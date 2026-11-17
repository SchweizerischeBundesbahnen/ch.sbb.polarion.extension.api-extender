package ch.sbb.polarion.extension.api_extender.velocity.wiki;

import ch.sbb.polarion.extension.api_extender.velocity.engine_v2.Velocity2ScriptRenderer;
import ch.sbb.polarion.extension.generic.util.BundleJarsPrioritizingRunnable;
import com.polarion.alm.server.api.transaction.WriteTransactionImpl;
import com.polarion.alm.wiki.IWikiService;
import com.polarion.platform.core.IPlatform;
import com.polarion.platform.core.PlatformContext;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

class Velocity2WikiScriptsTest {

    @Test
    void engineFailureRendersErrorForEachScript() {
        try (MockedStatic<BundleJarsPrioritizingRunnable> runnable = mockStatic(BundleJarsPrioritizingRunnable.class)) {
            runnable.when(() -> BundleJarsPrioritizingRunnable.executeCached(any(), anyMap()))
                    .thenReturn(Map.of(BundleJarsPrioritizingRunnable.ERROR_KEY, "engine not loaded"));

            List<String> htmls = Velocity2WikiScripts.render(List.of("a", "b"), Map.of(), "test");
            assertEquals(List.of(Velocity2WikiScripts.renderError("engine not loaded"), Velocity2WikiScripts.renderError("engine not loaded")), htmls);
        }
    }

    @Test
    void incompleteEngineResultRendersErrors() {
        try (MockedStatic<BundleJarsPrioritizingRunnable> runnable = mockStatic(BundleJarsPrioritizingRunnable.class)) {
            runnable.when(() -> BundleJarsPrioritizingRunnable.executeCached(any(), anyMap()))
                    .thenReturn(Map.of(Velocity2ScriptRenderer.RESULT_HTMLS, List.of("x")));

            assertTrue(Velocity2WikiScripts.render(List.of("a"), Map.of(), "test").get(0).contains("polarion-errormessage"));
        }
    }

    @Test
    void noTransactionWhenNoneCanBeCreated() {
        IWikiService wikiService = mock(IWikiService.class);
        when(wikiService.getWikiRenderingContextMap()).thenReturn(Map.of());
        IPlatform platform = mock(IPlatform.class);
        when(platform.lookupService(IWikiService.class)).thenReturn(wikiService);
        try (MockedStatic<PlatformContext> platformContext = mockStatic(PlatformContext.class);
             MockedConstruction<WriteTransactionImpl> ignored = mockConstruction(WriteTransactionImpl.class, (mock, context) -> {
                 throw new IllegalStateException("no platform");
             })) {
            platformContext.when(PlatformContext::getPlatform).thenReturn(platform);

            assertFalse(Velocity2WikiScripts.createVariables().containsKey(Velocity2WikiScripts.VARIABLE_TRANSACTION));
        }
    }
}
