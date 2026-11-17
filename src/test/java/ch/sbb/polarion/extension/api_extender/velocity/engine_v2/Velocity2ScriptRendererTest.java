package ch.sbb.polarion.extension.api_extender.velocity.engine_v2;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class Velocity2ScriptRendererTest {

    private final Velocity2ScriptRenderer renderer = new Velocity2ScriptRenderer();

    private Map<String, Object> render(String script, Map<String, Object> variables) {
        Supplier<Map<String, Object>> supplier = () -> variables;
        return renderer.run(Map.of(
                Velocity2ScriptRenderer.PARAM_SCRIPT, script,
                Velocity2ScriptRenderer.PARAM_LOG_TAG, "test",
                Velocity2ScriptRenderer.PARAM_VARIABLES, supplier));
    }

    private String html(String script, Map<String, Object> variables) {
        Map<String, Object> result = render(script, variables);
        assertNull(result.get(Velocity2ScriptRenderer.RESULT_ERROR), String.valueOf(result));
        return (String) result.get(Velocity2ScriptRenderer.RESULT_HTML);
    }

    @Test
    void rendersVelocity2Syntax() {
        assertEquals("1.5", html("#set($x = 1.5)$x", Map.of()));
        assertEquals("a, b, c", html("#foreach($i in $items)$i#if($foreach.hasNext), #end#end", Map.of("items", List.of("a", "b", "c"))));
        assertEquals("2", html("#set($m = {'k': 2})$m.k", Map.of()));
    }

    @Test
    void seesGivenVariables() {
        assertEquals("Hello World", html("Hello $name", Map.of("name", "World")));
    }

    @Test
    void doesNotChangeGivenVariables() {
        Map<String, Object> variables = new HashMap<>(Map.of("name", "World"));
        html("#set($name = 'changed')#set($added = 1)", variables);
        assertEquals(Map.of("name", "World"), variables);
    }

    @Test
    void parseErrorGivesError() {
        Map<String, Object> result = render("#if(", Map.of());
        assertNull(result.get(Velocity2ScriptRenderer.RESULT_HTML));
        assertTrue(((String) result.get(Velocity2ScriptRenderer.RESULT_ERROR)).contains("test"), String.valueOf(result));
    }

    @Test
    void blocksReflectionAndSystem() {
        assertEquals("$s.class.forName('java.lang.Runtime')", html("$s.class.forName('java.lang.Runtime')", Map.of("s", "text")));
        assertEquals("$s.getClass().getClassLoader()", html("$s.getClass().getClassLoader()", Map.of("s", "text")));
    }

    @Test
    void cannotParseFiles() {
        Map<String, Object> result = render("#parse('META-INF/MANIFEST.MF')", Map.of());
        assertNotNull(result.get(Velocity2ScriptRenderer.RESULT_ERROR));
    }

    @Test
    void macroIsLocalToScript() {
        assertEquals("hi", html("#macro(greet)hi#end#greet()", Map.of()));
        assertEquals("#greet()", html("#greet()", Map.of()));
    }
}
