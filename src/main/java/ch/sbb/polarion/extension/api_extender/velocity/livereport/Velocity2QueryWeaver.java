package ch.sbb.polarion.extension.api_extender.velocity.livereport;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Rewrites {@code VelocityQueryRenderer.renderImpl(String)} so that the query of a "Lucene + Velocity" or "SQL + Velocity"
 * widget parameter passes through {@link Velocity2QueryHook} before Velocity v1.4 parses it.
 * In the Polarion class, all Velocity queries of widget Data Set parameters go through this private method.
 * <p>
 * At the method entry, the woven code does the equivalent of
 * {@code query = ((BiFunction) System.getProperties().get(PROPERTY)).apply(this, query)}.
 * It refers to JDK types only, so the woven class has no wire to this bundle, and a hot redeploy does not need
 * a refresh of {@code com.polarion.alm.ui}. It adds no branch and keeps the type of the local variable,
 * so the existing stack map frames stay valid.
 */
public final class Velocity2QueryWeaver {

    public static final String TARGET_CLASS = "com.polarion.alm.shared.api.model.rp.parameter.impl.dataset.VelocityQueryRenderer";
    static final String RENDER_METHOD = "renderImpl";
    static final String RENDER_DESCRIPTOR = "(Ljava/lang/String;)Ljava/lang/String;";
    private static final int QUERY_VARIABLE = 1;

    /**
     * @return the woven class, or {@code null} when the class has no instance method {@code String renderImpl(String)}
     */
    public byte @Nullable [] weave(byte @NotNull [] classBytes) {
        ClassReader reader = new ClassReader(classBytes);
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
        RenderVisitor visitor = new RenderVisitor(writer);
        reader.accept(visitor, 0);
        return visitor.woven ? writer.toByteArray() : null;
    }

    private static final class RenderVisitor extends ClassVisitor {
        private boolean woven;

        private RenderVisitor(@NotNull ClassVisitor next) {
            super(Opcodes.ASM9, next);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            MethodVisitor next = super.visitMethod(access, name, descriptor, signature, exceptions);
            if (RENDER_METHOD.equals(name) && RENDER_DESCRIPTOR.equals(descriptor) && (access & Opcodes.ACC_STATIC) == 0) {
                woven = true;
                return new MethodEntryVisitor(next);
            }
            return next;
        }
    }

    private static final class MethodEntryVisitor extends MethodVisitor {
        private MethodEntryVisitor(@NotNull MethodVisitor next) {
            super(Opcodes.ASM9, next);
        }

        @Override
        public void visitCode() {
            super.visitCode();
            super.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "getProperties", "()Ljava/util/Properties;", false);
            super.visitLdcInsn(Velocity2QueryHook.PROPERTY);
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/Properties", "get", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
            super.visitTypeInsn(Opcodes.CHECKCAST, "java/util/function/BiFunction");
            super.visitVarInsn(Opcodes.ALOAD, 0);
            super.visitVarInsn(Opcodes.ALOAD, QUERY_VARIABLE);
            super.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/function/BiFunction", "apply", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", true);
            super.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/String");
            super.visitVarInsn(Opcodes.ASTORE, QUERY_VARIABLE);
        }
    }
}
