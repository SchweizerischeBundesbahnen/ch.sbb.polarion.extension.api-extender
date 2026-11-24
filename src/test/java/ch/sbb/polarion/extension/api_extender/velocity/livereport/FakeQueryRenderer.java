package ch.sbb.polarion.extension.api_extender.velocity.livereport;

/**
 * Has the shape of {@code VelocityQueryRenderer} which {@link Velocity2QueryWeaver} and {@link Velocity2QueryHook} rely on.
 * The tests weave this class and load the result in a separate class loader.
 */
public class FakeQueryRenderer {

    private final RenderingContext renderingContext = new RenderingContext();

    public FakeVelocityContext velocityContext() {
        return renderingContext.velocityContext;
    }

    public String renderLucene(String query) {
        return renderImpl(query);
    }

    /**
     * Has the parameter types of {@code renderImpl}, but another name, so the weaver must leave it alone.
     */
    public String renderOther(String query) {
        return "[" + query + "]";
    }

    private String renderImpl(String query) {
        if (query.isEmpty()) {
            return "";
        }
        return "<" + query + ">";
    }

    /**
     * The part of {@code RichPageRenderingContextImpl} which {@link Velocity2QueryHook} uses by reflection.
     */
    public static class RenderingContext {
        private final FakeVelocityContext velocityContext = new FakeVelocityContext();

        public FakeVelocityContext createVelocityContext() {
            return velocityContext;
        }
    }
}
