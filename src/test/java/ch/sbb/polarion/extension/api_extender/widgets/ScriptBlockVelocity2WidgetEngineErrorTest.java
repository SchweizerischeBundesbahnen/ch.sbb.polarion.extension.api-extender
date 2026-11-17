package ch.sbb.polarion.extension.api_extender.widgets;

import ch.sbb.polarion.extension.generic.util.BundleJarsPrioritizingRunnable;
import com.polarion.alm.shared.api.model.internal.RendererBase;
import com.polarion.alm.shared.api.utils.RunnableWithResult;
import com.polarion.alm.shared.api.utils.html.HtmlFragmentBuilder;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Separate from {@link ScriptBlockVelocity2WidgetTest}, whose extension already mocks {@link BundleJarsPrioritizingRunnable}.
 */
class ScriptBlockVelocity2WidgetEngineErrorTest {

    @Test
    void engineFailureRendersWarning() {
        StringBuilder output = new StringBuilder();
        HtmlFragmentBuilder builder = mock(HtmlFragmentBuilder.class, RETURNS_DEEP_STUBS);
        doAnswer(invocation -> {
            output.append((String) invocation.getArgument(0));
            return invocation.getMock();
        }).when(builder).html(anyString());

        try (MockedStatic<RendererBase> rendererBase = mockStatic(RendererBase.class);
             MockedStatic<BundleJarsPrioritizingRunnable> runnable = mockStatic(BundleJarsPrioritizingRunnable.class)) {
            rendererBase.when(() -> RendererBase.executeWithImplicitBuilderTarget(any(), any()))
                    .thenAnswer(invocation -> ((RunnableWithResult<?>) invocation.getArgument(1)).run());
            runnable.when(() -> BundleJarsPrioritizingRunnable.executeCached(any(), anyMap()))
                    .thenReturn(Map.of(BundleJarsPrioritizingRunnable.ERROR_KEY, "engine not loaded"));

            ScriptBlockVelocity2Widget.render(builder, "x", "test", Map.of(), message -> "WARNING: " + message);
        }
        assertEquals("WARNING: engine not loaded", output.toString());
    }
}
