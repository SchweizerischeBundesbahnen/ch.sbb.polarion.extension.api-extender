package ch.sbb.polarion.extension.api_extender.widgets;

import com.polarion.alm.shared.api.SharedContext;
import com.polarion.alm.shared.api.model.rp.widget.RichPageWidgetContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ScriptInlineVelocity2WidgetTest {

    private final ScriptInlineVelocity2Widget widget = new ScriptInlineVelocity2Widget();

    @Test
    void isInlineAndRestricted() {
        assertTrue(widget.isInline());
        assertTrue(widget.isRestricted());
        assertFalse(new ScriptBlockVelocity2Widget().isInline());
    }

    @Test
    void hasOwnLabelAndIcon() {
        assertEquals("Script - Inline Velocity V2", widget.getLabel(mock(SharedContext.class)));
        assertEquals("/polarion/api-extender-app/ui/images/app-icon.svg", widget.getIcon(mock(RichPageWidgetContext.class)));
    }
}
