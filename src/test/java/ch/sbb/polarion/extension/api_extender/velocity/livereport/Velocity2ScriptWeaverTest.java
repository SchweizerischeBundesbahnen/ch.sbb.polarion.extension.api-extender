package ch.sbb.polarion.extension.api_extender.velocity.livereport;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.*;

class Velocity2ScriptWeaverTest {

    @AfterEach
    void tearDown() {
        System.getProperties().remove(Velocity2ScriptHook.PROPERTY);
    }

    @Test
    void wovenRenderPassesRendererAndScriptThroughTheHook() throws Exception {
        List<Object> renderers = new ArrayList<>();
        System.getProperties().put(Velocity2ScriptHook.PROPERTY, (BiFunction<Object, String, String>) (renderer, script) -> {
            renderers.add(renderer);
            return script.toUpperCase();
        });
        Object renderer = newWovenFakeRenderer("abc");

        assertEquals("<ABC>", renderer.getClass().getMethod("render").invoke(renderer));
        assertEquals(List.of(renderer), renderers);
    }

    @Test
    void fieldReadOutsideRenderStaysUnchanged() throws Exception {
        System.getProperties().put(Velocity2ScriptHook.PROPERTY, (BiFunction<Object, String, String>) (renderer, script) -> "changed");
        Object renderer = newWovenFakeRenderer("abc");

        assertEquals("abc", renderer.getClass().getMethod("log").invoke(renderer));
    }

    @Test
    void installedHookLeavesScriptWithoutMarkerUnchanged() throws Exception {
        Velocity2ScriptHook.install();
        Object renderer = newWovenFakeRenderer("$x");

        assertEquals("<$x>", renderer.getClass().getMethod("render").invoke(renderer));
    }

    @Test
    void uninstalledHookPassesScriptThrough() throws Exception {
        Velocity2ScriptHook.install();
        Velocity2ScriptHook.uninstall();
        Object renderer = newWovenFakeRenderer("{velocityV2}x{velocityV2}");

        assertEquals("<{velocityV2}x{velocityV2}>", renderer.getClass().getMethod("render").invoke(renderer));
    }

    @Test
    void classWithoutScriptReadInRenderIsNotWoven() throws IOException {
        assertNull(new Velocity2ScriptWeaver(FakeVelocityContext.class.getName()).weave(classBytes(FakeVelocityContext.class.getName())));
    }

    @Test
    void polarionScriptRendererIsWoven() throws IOException {
        byte[] woven = new Velocity2ScriptWeaver(Velocity2ScriptWeaver.TARGET_CLASS).weave(classBytes(Velocity2ScriptWeaver.TARGET_CLASS));

        assertNotNull(woven);
        assertEquals(1, countHookCalls(woven, Velocity2ScriptWeaver.RENDER_METHOD));
        assertEquals(0, countHookCalls(woven, "handleException"));
    }

    private static Object newWovenFakeRenderer(String script) throws IOException, ReflectiveOperationException {
        String name = FakeScriptRenderer.class.getName();
        byte[] woven = new Velocity2ScriptWeaver(name).weave(classBytes(name));
        assertNotNull(woven);
        Class<?> wovenClass = new ClassLoader(Velocity2ScriptWeaverTest.class.getClassLoader()) {
            Class<?> define() {
                return defineClass(name, woven, 0, woven.length);
            }
        }.define();
        try {
            return wovenClass.getConstructor(String.class).newInstance(script);
        } catch (InvocationTargetException e) {
            throw new AssertionError(e.getCause());
        }
    }

    private static byte[] classBytes(String className) throws IOException {
        try (InputStream stream = Objects.requireNonNull(Velocity2ScriptWeaverTest.class.getClassLoader()
                .getResourceAsStream(className.replace('.', '/') + ".class"), "class file of " + className)) {
            return stream.readAllBytes();
        }
    }

    private static int countHookCalls(byte[] classBytes, String methodName) {
        int[] count = {0};
        new ClassReader(classBytes).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                if (!methodName.equals(name)) {
                    return null;
                }
                return new MethodVisitor(Opcodes.ASM9) {
                    @Override
                    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                        if ("java/util/function/BiFunction".equals(owner) && "apply".equals(name)) {
                            count[0]++;
                        }
                    }
                };
            }
        }, 0);
        return count[0];
    }
}
