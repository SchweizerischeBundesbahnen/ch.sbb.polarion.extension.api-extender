package ch.sbb.polarion.extension.api_extender.widgets;

import ch.sbb.polarion.extension.generic.test_extensions.BundleJarsPrioritizingRunnableMockExtension;
import com.polarion.alm.shared.api.model.internal.RendererBase;
import com.polarion.alm.shared.api.utils.RunnableWithResult;
import com.polarion.alm.shared.api.utils.html.HtmlFragmentBuilder;
import org.apache.velocity.VelocityContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(BundleJarsPrioritizingRunnableMockExtension.class)
class ScriptBlockVelocity2WidgetTest {

    private final ScriptBlockVelocity2Widget widget = new ScriptBlockVelocity2Widget();
    private final StringBuilder output = new StringBuilder();
    private MockedStatic<RendererBase> rendererBase;

    @BeforeEach
    void setUp() {
        rendererBase = mockStatic(RendererBase.class);
        rendererBase.when(() -> RendererBase.executeWithImplicitBuilderTarget(any(), any()))
                .thenAnswer(invocation -> ((RunnableWithResult<?>) invocation.getArgument(1)).run());
    }

    @AfterEach
    void tearDown() {
        rendererBase.close();
    }

    private String render(String script, Map<String, Object> variables) {
        HtmlFragmentBuilder builder = mock(HtmlFragmentBuilder.class, RETURNS_DEEP_STUBS);
        doAnswer(invocation -> {
            output.append((String) invocation.getArgument(0));
            return invocation.getMock();
        }).when(builder).html(anyString());
        when(builder.toString()).thenAnswer(invocation -> output.toString());

        return ScriptBlockVelocity2Widget.render(builder, script, "test", variables, message -> "WARNING: " + message);
    }

    @Test
    void rendersScriptWithPageVariables() {
        assertEquals("Hello World 1.5", render("#set($x = 1.5)Hello $name $x", Map.of("name", "World")));
    }

    @Test
    void rendersInsideImplicitBuilderTarget() {
        render("x", Map.of());
        rendererBase.verify(() -> RendererBase.executeWithImplicitBuilderTarget(any(), any()));
    }

    @Test
    void scriptErrorRendersWarning() {
        String html = render("#if(", Map.of());
        assertTrue(html.startsWith("WARNING: "), html);
    }

    @Test
    void toMapCopiesContextEntries() {
        assertEquals(Map.of("a", 1, "b", "two"), ScriptBlockVelocity2Widget.toMap(new VelocityContext(Map.of("a", 1, "b", "two"))));
    }

    @Test
    void isRestricted() {
        assertTrue(widget.isRestricted());
    }
}
