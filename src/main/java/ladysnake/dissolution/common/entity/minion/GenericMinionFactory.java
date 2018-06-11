package ladysnake.dissolution.common.entity.minion;

import ladysnake.dissolution.api.corporeality.IPossessable;
import ladysnake.dissolution.common.entity.EntityPossessableImpl;
import ladysnake.dissolution.core.SafeClassWriter;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.*;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GenericMinionFactory {
    private static final ASMClassLoader LOADER = new ASMClassLoader();
    private static final Map<Class<? extends EntityMob>, Class<? extends EntityMob>> POSSESSABLES = new HashMap<>();

    /**
     * Creates a new class of possessable entity derived from the given base
     */
    @SuppressWarnings("unchecked")
    public static <T extends EntityMob, P extends EntityMob & IPossessable> Class<P> defineGenericMinion(Class<T> baseEntityClass) {
        return (Class<P>) POSSESSABLES.computeIfAbsent(baseEntityClass, base -> {
            try {
                final byte[] possessableImplBytes = Launch.classLoader.getClassBytes(EntityPossessableImpl.class.getName().replace('.', '/'));
                final ClassReader reader = new ClassReader(possessableImplBytes);
                final String name = getName(base);
                ClassWriter writer = new SafeClassWriter(0);
//                ClassVisitor traceVisitor = new TraceClassVisitor(writer, new PrintWriter(System.out));
                ClassVisitor adapter = new ChangeParentClassAdapter(Opcodes.ASM5, writer, name.replace('.', '/'), base.getName().replace('.', '/'));
                reader.accept(adapter, 0);

                Class ret = LOADER.define(name, writer.toByteArray());
                POSSESSABLES.put(baseEntityClass, ret);
                return ret;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T extends EntityMob, P extends EntityMob & IPossessable> Class<P> getPossessable(Class<T> base) {
        return (Class<P>) POSSESSABLES.get(base);
    }

    private static String getName(Class baseClass) {
        return String.format("%s_%s", EntityPossessableImpl.class.getName(), baseClass.getSimpleName());
    }

    private static class ASMClassLoader extends ClassLoader {
        private ASMClassLoader() {
            super(ASMClassLoader.class.getClassLoader());
        }

        public Class<?> define(String name, byte[] data) {
            return defineClass(name, data, 0, data.length);
        }
    }

    private static class ChangeParentClassAdapter extends ClassVisitor {
        private static final String TEMPLATE_NAME = EntityPossessableImpl.class.getName().replace('.', '/');
        private static final Type TEMPLATE_TYPE = Type.getType("L" + TEMPLATE_NAME + ";");

        private final String name;
        private String oldSuperName;
        private final String newSuperName;

        public ChangeParentClassAdapter(int api, ClassVisitor cv, String name, String newSuperName) {
            super(api, cv);
            this.name = name;
            this.newSuperName = newSuperName;
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            oldSuperName = superName;
            super.visit(version, access, this.name, signature, this.newSuperName, interfaces);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
            mv = new ChangeParentMethodAdapter(api, mv);
            return mv;
        }

        private class ChangeParentMethodAdapter extends MethodVisitor {

            public ChangeParentMethodAdapter(int api, MethodVisitor mv) {
                super(api, mv);
            }

            @Override
            public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
                desc = replaceDesc(desc);
                if (opcode == Opcodes.INVOKESPECIAL && owner.equals(oldSuperName)) {
                    visitMethodInsn(
                            Opcodes.INVOKESPECIAL,
                            newSuperName,
                            name,
                            desc,
                            false
                    );
                } else if (owner.equals(TEMPLATE_NAME)) {
                    visitMethodInsn(
                            opcode,
                            ChangeParentClassAdapter.this.name,
                            name,
                            desc,
                            false
                    );
                } else {
                    super.visitMethodInsn(opcode, owner, name, desc, itf);
                }
            }

            @Override
            public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
                desc = replaceDesc(desc);
                for (int i = 0; i < bsmArgs.length; i++) {
                    Object arg = bsmArgs[i];
                    if (arg instanceof Handle && TEMPLATE_NAME.equals(((Handle) arg).getOwner())) {
                        bsmArgs[i] = new Handle(((Handle) arg).getTag(), ChangeParentClassAdapter.this.name, ((Handle) arg).getName(), ((Handle) arg).getDesc(), ((Handle) arg).isInterface());
                    }
                }
                super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
            }

            private String replaceDesc(String desc) {
                if (desc.contains(TEMPLATE_NAME)) {
                    Type methodType = Type.getMethodType(desc);
                    Type[] arguments = methodType.getArgumentTypes();
                    StringBuilder buf = new StringBuilder();
                    buf.append('(');
                    for (Type argument : arguments) {
                        if (argument.getSort() == Type.OBJECT && TEMPLATE_NAME.equals(argument.getInternalName())) {
                            buf.append('L').append(ChangeParentClassAdapter.this.name).append(';');
                        } else {
                            buf.append(argument.getDescriptor());
                        }
                    }
                    buf.append(')');
                    buf.append(methodType.getReturnType().getDescriptor());
                    desc = buf.toString();
                }
                return desc;
            }

            @Override
            public void visitLdcInsn(Object cst) {
                if (cst.equals(TEMPLATE_TYPE)) {
                    cst = Type.getType("L" + ChangeParentClassAdapter.this.name + ";");
                }
                super.visitLdcInsn(cst);
            }

            @Override
            public void visitFieldInsn(int opcode, String owner, String name, String desc) {
                if (owner.equals(TEMPLATE_NAME)) {
                    visitFieldInsn(
                            opcode,
                            ChangeParentClassAdapter.this.name,
                            name,
                            desc
                    );
                } else {
                    super.visitFieldInsn(opcode, owner, name, desc);
                }
            }

            @Override
            public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
                Type type = Type.getType(desc);
                if (type.getSort() == Type.OBJECT && type.getInternalName().equals(TEMPLATE_NAME)) {
                    visitLocalVariable(
                            name,
                            "L" + ChangeParentClassAdapter.this.name + ";",
                            signature,
                            start,
                            end,
                            index
                    );
                } else {
                    super.visitLocalVariable(name, desc, signature, start, end, index);
                }
            }

            @Override
            public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
                for (int i = 0; i < local.length; i++) {
                    if (TEMPLATE_NAME.equals(local[i])) {
                        local[i] = ChangeParentClassAdapter.this.name;
                    }
                }
                super.visitFrame(type, nLocal, local, nStack, stack);
            }
        }
    }

}
