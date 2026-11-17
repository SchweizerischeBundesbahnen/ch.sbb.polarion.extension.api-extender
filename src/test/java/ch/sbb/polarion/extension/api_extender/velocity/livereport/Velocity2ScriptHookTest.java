package ch.sbb.polarion.extension.api_extender.velocity.livereport;

import ch.sbb.polarion.extension.generic.test_extensions.BundleJarsPrioritizingRunnableMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(BundleJarsPrioritizingRunnableMockExtension.class)
class Velocity2ScriptHookTest {

    private static final Pattern REFERENCE = Pattern.compile("\\$\\{velocityV2Output\\.get\\('(velocitytwo[a-z0-9]+)'\\)}");

    private final Velocity2ScriptHook hook = new Velocity2ScriptHook();
    private final FakeScriptRenderer renderer = new FakeScriptRenderer("");

    @Test
    void scriptWithoutMarkerIsUnchanged() {
        String script = "#set($x = 1)$x";
        assertSame(script, hook.apply(renderer, script));
        assertNull(hook.apply(renderer, null));
        assertNull(renderer.velocityContext().get("velocityV2Output"));
    }

    @Test
    void regionBecomesReferenceAndOutputGoesToContext() {
        renderer.velocityContext().put("greeting", "Hello");

        String result = hook.apply(renderer, "1.4: $x {velocityV2}#set($d = 1.5)$greeting $d{velocityV2}.txt");

        Matcher matcher = REFERENCE.matcher(result);
        assertTrue(matcher.find(), result);
        assertEquals("1.4: $x " + matcher.group() + ".txt", result);
        Velocity2ScriptOutputs outputs = assertInstanceOf(Velocity2ScriptOutputs.class, renderer.velocityContext().get("velocityV2Output"));
        assertEquals("Hello 1.5", outputs.get(matcher.group(1)));
        assertNull(outputs.get("other"));
    }

    @Test
    void regionsShareOneContext() {
        String result = hook.apply(renderer, "{velocityV2}#set($a = 2){velocityV2}-{velocityV2}$a{velocityV2}");

        Matcher matcher = REFERENCE.matcher(result);
        assertTrue(matcher.find() && matcher.find(), result);
        Velocity2ScriptOutputs outputs = (Velocity2ScriptOutputs) renderer.velocityContext().get("velocityV2Output");
        assertEquals("2", outputs.get(matcher.group(1)));
    }

    @Test
    void failingRegionRendersErrorMessage() {
        String result = hook.apply(renderer, "{velocityV2}#if({velocityV2}");

        Matcher matcher = REFERENCE.matcher(result);
        assertTrue(matcher.find(), result);
        Velocity2ScriptOutputs outputs = (Velocity2ScriptOutputs) renderer.velocityContext().get("velocityV2Output");
        assertTrue(outputs.get(matcher.group(1)).contains("Velocity 2 error"), outputs.get(matcher.group(1)));
    }

    @Test
    void rendererWithoutContextLeavesScriptUnchanged() {
        String script = "{velocityV2}x{velocityV2}";
        assertSame(script, hook.apply(new Object(), script));
    }
}
