package ch.sbb.polarion.extension.api_extender.widgets;

import ch.sbb.polarion.extension.api_extender.velocity.engine_v2.Velocity2ScriptRenderer;
import ch.sbb.polarion.extension.api_extender.velocity.livereport.Velocity14Context;
import ch.sbb.polarion.extension.generic.util.BundleJarsPrioritizingRunnable;
import com.polarion.alm.server.api.model.rp.widget.impl.RichPageRenderingContextImpl;
import com.polarion.alm.shared.api.SharedContext;
import com.polarion.alm.shared.api.model.ModelObject;
import com.polarion.alm.shared.api.model.internal.RendererBase;
import com.polarion.alm.shared.api.model.rp.parameter.ParameterFactory;
import com.polarion.alm.shared.api.model.rp.parameter.RichPageParameter;
import com.polarion.alm.shared.api.model.rp.parameter.ScriptParameter;
import com.polarion.alm.shared.api.model.rp.widget.RichPageWidget;
import com.polarion.alm.shared.api.model.rp.widget.RichPageWidgetContext;
import com.polarion.alm.shared.api.model.rp.widget.RichPageWidgetRenderingContext;
import com.polarion.alm.shared.api.utils.collections.ImmutableStrictList;
import com.polarion.alm.shared.api.utils.collections.ReadOnlyStrictMap;
import com.polarion.alm.shared.api.utils.collections.StrictMapImpl;
import com.polarion.alm.shared.api.utils.html.HtmlFragmentBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * The same as the Polarion Script - Block widget, but renders the script with the embedded Velocity 2 engine.
 * <p>
 * The script gets the same variables as in the Polarion widget: Polarion fills a Velocity v1.4 context,
 * and this widget copies its entries into a Velocity 2 context.
 * This class is loaded by the bundle class loader, which links Velocity types against Polarion's Velocity v1.4.
 * So it must not call Velocity methods directly or load {@link Velocity2ScriptRenderer} types.
 */
public class ScriptBlockVelocity2Widget extends RichPageWidget {

    public static final String ID = "ch.sbb.polarion.extension.api_extender.widgets.scriptBlockVelocity2";
    static final String LABEL = "Script - Block Velocity V2";
    static final String PARAMETER_SCRIPT = "script";
    static final String APP_ICON = "/polarion/api-extender-app/ui/images/app-icon.svg";
    private static final String LOG_TAG = "Velocity 2 script widget";

    @Override
    public @NotNull String getIcon(@NotNull RichPageWidgetContext widgetContext) {
        return APP_ICON;
    }

    @Override
    public @NotNull String getLabel(@NotNull SharedContext context) {
        return LABEL;
    }

    @Override
    public @NotNull String getDetailsHtml(@NotNull RichPageWidgetContext widgetContext) {
        return "Renders a Velocity script with Velocity Engine 2.4.1. The script gets the same variables as in the Script - Block widget.";
    }

    @Override
    public boolean isRestricted() {
        return true;
    }

    @Override
    public @NotNull Iterable<String> getTags(@NotNull SharedContext context) {
        return new ImmutableStrictList<>(context.localization().getString("richpages.widget.tag.scripting"), context.localization().getString("richpages.widget.tag.collections"));
    }

    @Override
    public @NotNull ReadOnlyStrictMap<String, RichPageParameter> getParametersDefinition(@NotNull ParameterFactory factory) {
        StrictMapImpl<String, RichPageParameter> parameters = new StrictMapImpl<>();
        parameters.put(PARAMETER_SCRIPT, factory.script(factory.context().localization().getString("richpages.widget.scriptWidget.script")).build());
        return parameters;
    }

    @Override
    public @NotNull String renderHtml(@NotNull RichPageWidgetRenderingContext context) {
        // Only the internal implementation creates the Velocity context of the Polarion widget.
        RichPageRenderingContextImpl renderingContext = (RichPageRenderingContextImpl) context;
        ScriptParameter parameter = context.parameter(PARAMETER_SCRIPT);
        String script = Objects.requireNonNullElse(parameter.value(), "");
        Map<String, Object> variables = toMap(renderingContext.createVelocityContext());
        UnaryOperator<String> warningRenderer = message -> renderingContext.renderWarning(
                renderingContext.sharedContext().localization().getString("richpages.widget.scriptWidget.exception", message));
        return render(renderingContext.createHtmlFragmentBuilder(), script, createLogTag(renderingContext), variables, warningRenderer);
    }

    /**
     * Tests call this method directly: Mockito cannot mock {@link RichPageRenderingContextImpl},
     * because it references Velocity v1.4 types, which are not on the test class path.
     */
    static @NotNull String render(@NotNull HtmlFragmentBuilder builder, @NotNull String script, @NotNull String logTag,
                                  @NotNull Map<String, Object> variables, @NotNull UnaryOperator<String> warningRenderer) {
        // Polarion renderers called from the script, for example render().htmlFor(), write to the implicit builder target.
        RendererBase.executeWithImplicitBuilderTarget(builder.target(), () -> {
            builder.html(evaluate(script, logTag, variables, warningRenderer));
            return null;
        });
        return builder.toString();
    }

    private static @NotNull String evaluate(@NotNull String script, @NotNull String logTag,
                                            @NotNull Map<String, Object> variables, @NotNull UnaryOperator<String> warningRenderer) {
        Supplier<Map<String, Object>> variablesSupplier = () -> variables;
        Map<String, Object> result = BundleJarsPrioritizingRunnable.executeCached(Velocity2ScriptRenderer.class, Map.of(
                Velocity2ScriptRenderer.PARAM_SCRIPT, script,
                Velocity2ScriptRenderer.PARAM_LOG_TAG, logTag,
                Velocity2ScriptRenderer.PARAM_VARIABLES, variablesSupplier));

        if (result.get(Velocity2ScriptRenderer.RESULT_HTML) instanceof String html) {
            return html;
        }
        String message = result.get(Velocity2ScriptRenderer.RESULT_ERROR) instanceof String error
                ? error
                : String.valueOf(result.get(BundleJarsPrioritizingRunnable.ERROR_KEY));
        return warningRenderer.apply(message);
    }

    private static @NotNull String createLogTag(@NotNull RichPageRenderingContextImpl renderingContext) {
        ModelObject object = renderingContext.object();
        return object != null ? LOG_TAG + " in: " + object.getReferenceToCurrent().toPath() : LOG_TAG;
    }

    static @NotNull Map<String, Object> toMap(@NotNull Object velocityContext) {
        return Velocity14Context.toMap(velocityContext);
    }
}
