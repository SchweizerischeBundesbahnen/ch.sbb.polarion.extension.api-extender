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

class Velocity2QueryWeaverTest {

    @AfterEach
    void tearDown() {
        System.getProperties().remove(Velocity2QueryHook.PROPERTY);
    }

    @Test
    void wovenRenderImplPassesRendererAndQueryThroughTheHook() throws Exception {
        List<Object> renderers = new ArrayList<>();
        System.getProperties().put(Velocity2QueryHook.PROPERTY, (BiFunction<Object, String, String>) (renderer, query) -> {
            renderers.add(renderer);
            return query.toUpperCase();
        });
        Object renderer = newWovenFakeRenderer();

        assertEquals("<ABC>", invoke(renderer, "renderLucene", "abc"));
        assertEquals("", invoke(renderer, "renderLucene", ""));
        assertEquals(List.of(renderer, renderer), renderers);
    }

    @Test
    void otherMethodStaysUnchanged() throws Exception {
        System.getProperties().put(Velocity2QueryHook.PROPERTY, (BiFunction<Object, String, String>) (renderer, query) -> "changed");
        Object renderer = newWovenFakeRenderer();

        assertEquals("[abc]", invoke(renderer, "renderOther", "abc"));
    }

    @Test
    void installedHookLeavesQueryWithoutMarkerUnchanged() throws Exception {
        Velocity2QueryHook.install();
        Object renderer = newWovenFakeRenderer();

        assertEquals("<status:$x>", invoke(renderer, "renderLucene", "status:$x"));
    }

    @Test
    void uninstalledHookPassesQueryThrough() throws Exception {
        Velocity2QueryHook.install();
        Velocity2QueryHook.uninstall();
        Object renderer = newWovenFakeRenderer();

        assertEquals("<{velocityV2}x{velocityV2}>", invoke(renderer, "renderLucene", "{velocityV2}x{velocityV2}"));
    }

    @Test
    void classWithoutRenderImplIsNotWoven() throws IOException {
        assertNull(new Velocity2QueryWeaver().weave(classBytes(FakeScriptRenderer.class.getName())));
    }

    @Test
    void renderImplWithOtherShapeIsNotWoven() throws IOException {
        Velocity2QueryWeaver weaver = new Velocity2QueryWeaver();

        assertNull(weaver.weave(classBytes(StaticRenderImpl.class.getName())));
        assertNull(weaver.weave(classBytes(OtherParameterRenderImpl.class.getName())));
    }

    @Test
    void polarionQueryRendererIsWoven() throws IOException {
        byte[] woven = new Velocity2QueryWeaver().weave(classBytes(Velocity2QueryWeaver.TARGET_CLASS));

        assertNotNull(woven);
        assertEquals(1, countHookCalls(woven, Velocity2QueryWeaver.RENDER_METHOD));
        assertEquals(0, countHookCalls(woven, "renderLucene"));
        assertEquals(0, countHookCalls(woven, "renderSql"));
    }

    private static Object invoke(Object renderer, String method, String query) throws ReflectiveOperationException {
        return renderer.getClass().getMethod(method, String.class).invoke(renderer, query);
    }

    private static Object newWovenFakeRenderer() throws IOException, ReflectiveOperationException {
        String name = FakeQueryRenderer.class.getName();
        byte[] woven = new Velocity2QueryWeaver().weave(classBytes(name));
        assertNotNull(woven);
        Class<?> wovenClass = new ClassLoader(Velocity2QueryWeaverTest.class.getClassLoader()) {
            Class<?> define() {
                return defineClass(name, woven, 0, woven.length);
            }
        }.define();
        try {
            return wovenClass.getConstructor().newInstance();
        } catch (InvocationTargetException e) {
            throw new AssertionError(e.getCause());
        }
    }

    private static byte[] classBytes(String className) throws IOException {
        try (InputStream stream = Objects.requireNonNull(Velocity2QueryWeaverTest.class.getClassLoader()
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

    @SuppressWarnings("unused")
    private static final class StaticRenderImpl {
        private static String renderImpl(String query) {
            return query;
        }
    }

    @SuppressWarnings("unused")
    private static final class OtherParameterRenderImpl {
        private String renderImpl(Object query) {
            return String.valueOf(query);
        }
    }
}
