package ch.sbb.polarion.extension.api_extender.velocity.livereport;

/**
 * Has the shape of {@code AbstractScriptRenderer} which {@link Velocity2ScriptWeaver} relies on.
 * The tests weave this class and load the result in a separate class loader.
 */
public class FakeScriptRenderer {

    private final String script;
    private final FakeVelocityContext velocityContext = new FakeVelocityContext();

    public FakeScriptRenderer(String script) {
        this.script = script;
    }

    public FakeVelocityContext velocityContext() {
        return velocityContext;
    }

    public String render() {
        return evaluate(script);
    }

    /**
     * Reads the field outside {@code render()}, so the weaver must leave it alone.
     */
    public String log() {
        return script;
    }

    private static String evaluate(String script) {
        return "<" + script + ">";
    }
}
