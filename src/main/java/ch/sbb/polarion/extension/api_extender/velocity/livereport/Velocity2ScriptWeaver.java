package ch.sbb.polarion.extension.api_extender.velocity.livereport;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Rewrites {@code AbstractScriptRenderer.render()} so that the script passes through {@link Velocity2ScriptHook}
 * before Velocity v1.4 parses it. In the Polarion class, {@code render()} reads the {@code script} field once,
 * as the argument of {@code VelocityEngine.evaluate()}.
 * <p>
 * After each read of the field in {@code render()}, the woven code does the equivalent of
 * {@code script = ((BiFunction) System.getProperties().get(PROPERTY)).apply(this, script)}.
 * It refers to JDK types only, so the woven class has no wire to this bundle, and a hot redeploy does not need
 * a refresh of {@code com.polarion.alm.ui}. It adds no branch, so the existing stack map frames stay valid.
 */
public final class Velocity2ScriptWeaver {

    public static final String TARGET_CLASS = "com.polarion.alm.server.api.model.rp.widget.AbstractScriptRenderer";
    static final String RENDER_METHOD = "render";
    static final String SCRIPT_FIELD = "script";
    private static final String STRING_DESCRIPTOR = "Ljava/lang/String;";

    private final String owner;

    /**
     * @param className the class to weave, with the shape of {@code AbstractScriptRenderer}: a {@code String script} field,
     *                  a {@code velocityContext()} method and a {@code render()} method without parameters
     */
    public Velocity2ScriptWeaver(@NotNull String className) {
        this.owner = className.replace('.', '/');
    }

    /**
     * @return the woven class, or {@code null} when {@code render()} does not read the {@code script} field
     */
    public byte @Nullable [] weave(byte @NotNull [] classBytes) {
        ClassReader reader = new ClassReader(classBytes);
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
        RenderVisitor visitor = new RenderVisitor(writer);
        reader.accept(visitor, 0);
        return visitor.woven ? writer.toByteArray() : null;
    }

    private final class RenderVisitor extends ClassVisitor {
        private boolean woven;

        private RenderVisitor(@NotNull ClassVisitor next) {
            super(Opcodes.ASM9, next);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            MethodVisitor next = super.visitMethod(access, name, descriptor, signature, exceptions);
            if (RENDER_METHOD.equals(name) && descriptor.startsWith("()")) {
                return new ScriptReadVisitor(next);
            }
            return next;
        }

        private final class ScriptReadVisitor extends MethodVisitor {
            private ScriptReadVisitor(@NotNull MethodVisitor next) {
                super(Opcodes.ASM9, next);
            }

            @Override
            public void visitFieldInsn(int opcode, String fieldOwner, String name, String fieldDescriptor) {
                super.visitFieldInsn(opcode, fieldOwner, name, fieldDescriptor);
                if (opcode == Opcodes.GETFIELD && owner.equals(fieldOwner) && SCRIPT_FIELD.equals(name) && STRING_DESCRIPTOR.equals(fieldDescriptor)) {
                    applyHook();
                    woven = true;
                }
            }

            // Stack before: [..., script]. Stack after: [..., hook.apply(this, script)].
            private void applyHook() {
                super.visitVarInsn(Opcodes.ALOAD, 0);
                super.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "getProperties", "()Ljava/util/Properties;", false);
                super.visitLdcInsn(Velocity2ScriptHook.PROPERTY);
                super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/Properties", "get", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
                super.visitTypeInsn(Opcodes.CHECKCAST, "java/util/function/BiFunction");
                // [script, this, hook] to [hook, script, this] to [hook, this, script]
                super.visitInsn(Opcodes.DUP_X2);
                super.visitInsn(Opcodes.POP);
                super.visitInsn(Opcodes.SWAP);
                super.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/function/BiFunction", "apply", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", true);
                super.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/String");
            }
        }
    }
}
