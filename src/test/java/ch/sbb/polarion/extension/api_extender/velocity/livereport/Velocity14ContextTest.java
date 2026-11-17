package ch.sbb.polarion.extension.api_extender.velocity.livereport;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class Velocity14ContextTest {

    @Test
    void reflectionFailuresBecomeIllegalStateException() {
        Object notAContext = new Object();
        assertThrows(IllegalStateException.class, () -> Velocity14Context.toMap(notAContext));
        assertThrows(IllegalStateException.class, () -> Velocity14Context.get(notAContext, "key"));
        assertThrows(IllegalStateException.class, () -> Velocity14Context.put(notAContext, "key", "value"));
    }
}
