package ch.sbb.polarion.extension.api_extender.velocity.livereport;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The part of the Velocity 1.4 {@code VelocityContext} API which {@link Velocity14Context} uses by reflection.
 */
public class FakeVelocityContext {

    private final Map<String, Object> values = new LinkedHashMap<>();

    public Object[] getKeys() {
        return values.keySet().toArray();
    }

    public Object get(String key) {
        return values.get(key);
    }

    public Object put(String key, Object value) {
        return values.put(key, value);
    }
}
