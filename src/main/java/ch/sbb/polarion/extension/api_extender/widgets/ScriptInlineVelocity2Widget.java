package ch.sbb.polarion.extension.api_extender.widgets;

import com.polarion.alm.shared.api.SharedContext;
import com.polarion.alm.shared.api.model.rp.widget.RichPageWidgetContext;
import org.jetbrains.annotations.NotNull;

/**
 * The same as the Polarion Script - Inline widget, but renders the script with the embedded Velocity 2 engine.
 */
public class ScriptInlineVelocity2Widget extends ScriptBlockVelocity2Widget {

    public static final String ID = "ch.sbb.polarion.extension.api_extender.widgets.scriptInlineVelocity2";
    static final String LABEL = "Script - Inline Velocity V2";

    @Override
    public @NotNull String getLabel(@NotNull SharedContext context) {
        return LABEL;
    }

    @Override
    public @NotNull String getDetailsHtml(@NotNull RichPageWidgetContext widgetContext) {
        return "Renders a Velocity script inline with Velocity Engine 2.4.1. The script gets the same variables as in the Script - Inline widget. Experimental: use this widget at your own risk.";
    }

    @Override
    public boolean isInline() {
        return true;
    }
}
