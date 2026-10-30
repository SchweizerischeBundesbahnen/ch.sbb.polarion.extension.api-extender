package ch.sbb.polarion.extension.api_extender.velocity.tools_v3;

import ch.sbb.polarion.extension.generic.test_extensions.BundleJarsPrioritizingRunnableMockExtension;
import ch.sbb.polarion.extension.generic.util.BundleJarsPrioritizingRunnable;
import org.apache.velocity.tools.generic.CollectionTool;
import org.apache.velocity.tools.generic.EscapeTool;
import org.apache.velocity.tools.generic.JsonTool;
import org.apache.velocity.tools.generic.LinkTool;
import org.apache.velocity.tools.generic.LoopTool;
import org.apache.velocity.tools.generic.MathTool;
import org.apache.velocity.tools.generic.XmlTool;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(BundleJarsPrioritizingRunnableMockExtension.class)
class VelocityToolsV3Test {

    private final VelocityToolsV3 velocityToolsV3 = new VelocityToolsV3();

    @Test
    void factoryAndFacadeKnowTheSameTools() {
        assertEquals(VelocityToolsV3.TOOL_NAMES, VelocityToolsV3Factory.getToolNames());
    }

    @Test
    void everyToolIsCreated() {
        for (String name : VelocityToolsV3.TOOL_NAMES) {
            assertNotNull(velocityToolsV3.get(name), name);
        }
    }

    @Test
    void unknownToolReturnsNull() {
        assertNull(velocityToolsV3.get("class"));
    }

    @Test
    void factoryRejectsUnknownTool() {
        VelocityToolsV3Factory factory = new VelocityToolsV3Factory();
        Map<String, Object> params = Map.of(VelocityToolsV3Factory.PARAM_TOOL, "context");
        assertThrows(IllegalArgumentException.class, () -> factory.run(params));
    }

    @Test
    void sharedToolIsReused() {
        assertSame(velocityToolsV3.get("math"), velocityToolsV3.get("math"));
    }

    @Test
    void requestScopedToolIsNew() {
        assertNotSame(velocityToolsV3.get("json"), velocityToolsV3.get("json"));
    }

    @Test
    void toolsWork() {
        assertEquals(5, ((MathTool) velocityToolsV3.get("math")).add(2, 3));
        assertEquals("a &amp; b", ((EscapeTool) velocityToolsV3.get("esc")).html("a & b"));
        assertArrayEquals(new String[]{"a", "b", "c"}, ((CollectionTool) velocityToolsV3.get("collection")).split("a,b,c"));
        JsonTool json = ((JsonTool) velocityToolsV3.get("json")).parse("{\"name\": \"value\"}");
        assertEquals("value", json.get("name"));
    }

    @Test
    void jsonToolCannotLoadExternalContent() {
        JsonTool json = (JsonTool) velocityToolsV3.get("json");
        assertNull(json.fetch("http://localhost/secret"));
        assertNull(json.read("META-INF/MANIFEST.MF"));
    }

    @Test
    void loopToolIsNewAndKnowsFirstAndLast() {
        assertNotSame(velocityToolsV3.get("loop"), velocityToolsV3.get("loop"));
        LoopTool loop = (LoopTool) velocityToolsV3.get("loop");
        List<String> marks = new ArrayList<>();
        Iterator<?> iterator = loop.watch(List.of("a", "b", "c"));
        while (iterator.hasNext()) {
            Object item = iterator.next();
            marks.add(item + (loop.isFirst() ? "<first>" : "") + (loop.isLast() ? "<last>" : ""));
        }
        assertEquals(List.of("a<first>", "b", "c<last>"), marks);
    }

    @Test
    void renderToolEvaluatesWithVelocity2() {
        SandboxedRenderTool render = (SandboxedRenderTool) velocityToolsV3.get("render");
        assertSame(render, velocityToolsV3.get("render"));
        assertEquals("1.5", render.eval("#set($x = 1.5)$x"));
        assertEquals("Hello Bob", render.eval("Hello $name", "name", "Bob"));
        assertEquals("3", render.eval("$a.size()", Map.of("a", List.of(1, 2, 3))));
        assertEquals("a,b", render.eval("#foreach($i in $items)$i#if($foreach.hasNext),#end#end", "items", List.of("a", "b")));
        assertEquals("12345", render.eval("$a$b$c$d$e", "a", 1, "b", 2, "c", 3, "d", 4, "e", 5));
    }

    @Test
    void namesAreTheToolNames() {
        assertEquals(VelocityToolsV3.TOOL_NAMES, velocityToolsV3.getNames());
    }

    @Test
    void failedToolCreationIsReportedWithItsCause() {
        Exception cause = new IllegalArgumentException("creation failed");
        when(BundleJarsPrioritizingRunnable.executeCached(VelocityToolsV3Factory.class, Map.of(VelocityToolsV3Factory.PARAM_TOOL, "math"), false))
                .thenReturn(Map.of(BundleJarsPrioritizingRunnable.ERROR_KEY, cause));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> velocityToolsV3.get("math"));

        assertSame(cause, exception.getCause());
    }

    @Test
    void jsonToolBlocksEveryLoadingMethod() {
        RestrictedJsonTool json = (RestrictedJsonTool) velocityToolsV3.get("json");
        assertNull(json.acquireString("http://localhost/secret"));
        assertNull(json.acquireReader("http://localhost/secret"));
        assertNull(json.getResourceReader("META-INF/MANIFEST.MF"));
    }

    @Test
    void renderToolAcceptsEveryNumberOfPairs() {
        SandboxedRenderTool render = (SandboxedRenderTool) velocityToolsV3.get("render");
        assertEquals("12", render.eval("$a$b", "a", 1, "b", 2));
        assertEquals("123", render.eval("$a$b$c", "a", 1, "b", 2, "c", 3));
        assertEquals("1234", render.eval("$a$b$c$d", "a", 1, "b", 2, "c", 3, "d", 4));
    }

    @Test
    void renderToolAcceptsNullMap() {
        SandboxedRenderTool render = (SandboxedRenderTool) velocityToolsV3.get("render");
        assertEquals("plain", render.eval("plain", (Map<?, ?>) null));
    }

    @Test
    void renderToolKeepsMacrosInsideOneCall() {
        SandboxedRenderTool render = (SandboxedRenderTool) velocityToolsV3.get("render");
        assertEquals("first", render.eval("#macro(leakCheck)first#end#leakCheck()"));
        assertEquals("#leakCheck()", render.eval("#leakCheck()"));
        assertEquals("second", render.eval("#macro(leakCheck)second#end#leakCheck()"));
    }

    @Test
    void renderToolRejectsWrongInput() {
        SandboxedRenderTool render = (SandboxedRenderTool) velocityToolsV3.get("render");
        assertNull(render.eval(null));
        assertNull(render.eval("#if("));
    }

    @Test
    void xmlToolParsesAndFinds() {
        assertNotSame(velocityToolsV3.get("xml"), velocityToolsV3.get("xml"));
        XmlTool xml = ((XmlTool) velocityToolsV3.get("xml")).parse("<r><item id='1'>a</item><item id='2'>b</item></r>");
        assertEquals("b", xml.find("/r/item[@id='2']").getText());
        assertEquals(2, xml.children().size());
    }

    @Test
    void xmlToolCannotLoadExternalContent() {
        XmlTool xml = (XmlTool) velocityToolsV3.get("xml");
        assertNull(xml.fetch("http://localhost/secret"));
        assertNull(xml.read("META-INF/MANIFEST.MF"));
    }

    @Test
    void xmlToolDoesNotExpandExternalEntities(@TempDir Path tempDir) throws IOException {
        Path secret = Files.writeString(tempDir.resolve("secret.txt"), "TOP-SECRET");
        String payload = "<?xml version=\"1.0\"?><!DOCTYPE r [<!ENTITY x SYSTEM \"" + secret.toUri() + "\">]><r>&x;</r>";
        XmlTool xml = ((XmlTool) velocityToolsV3.get("xml")).parse(payload);
        assertFalse(String.valueOf(xml).contains("TOP-SECRET"));
    }

    @Test
    void linkToolBuildsEncodedLinks() {
        // set* methods change the instance, so each script must get its own LinkTool.
        assertNotSame(velocityToolsV3.get("link"), velocityToolsV3.get("link"));
        LinkTool link = (LinkTool) velocityToolsV3.get("link");
        assertEquals("/polarion/?query=type%3Arequirement+AND+status%3Aopen", link.path("/polarion/").param("query", "type:requirement AND status:open").toString());
        assertEquals("https://host/p?a=1&amp;c=x+y%26z", link.uri("https://host/p?a=1").param("c", "x y&z").toString());
        assertEquals("a+b%26c", link.encode("a b&c"));
        assertNull(link.toString());
    }

    @Test
    void logToolWritesToPolarionLog() {
        Object log = velocityToolsV3.get("log");
        assertInstanceOf(PolarionLogTool.class, log);
        assertSame(log, velocityToolsV3.get("log"));
        PolarionLogTool logTool = (PolarionLogTool) log;
        assertDoesNotThrow(() -> {
            logTool.error("error");
            logTool.warn("warn");
            logTool.info("info");
            logTool.debug("debug");
            logTool.trace("trace");
        });
    }

    @Test
    void renderToolIsSandboxed() {
        SandboxedRenderTool render = (SandboxedRenderTool) velocityToolsV3.get("render");
        assertNull(render.eval("#parse('/etc/passwd')"));
        assertNull(render.eval("#include('pom.xml')"));
        assertEquals("$s.class.classLoader", render.eval("$s.class.classLoader", "s", "text"));
        assertEquals("$s.class.forName('java.lang.System')", render.eval("$s.class.forName('java.lang.System')", "s", "text"));
    }
}
