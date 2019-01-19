package ladysnake.dissolution.common.impl.possession.asm;

import org.apiguardian.api.API;
import org.objectweb.asm.*;

import static org.apiguardian.api.API.Status.INTERNAL;

/**
 * A class visitor that can change a class' supertype
 */
@API(status = INTERNAL)
public class ChangeParentClassAdapter extends ClassVisitor {
    private final String templateName;
    private final Type templateType;

    private final String name;
    private String oldSuperName;
    private final String newSuperName;

    public ChangeParentClassAdapter(int api, ClassVisitor cv, String name, String newSuperName, Class<?> templateClass) {
        super(api, cv);
        this.name = name;
        this.newSuperName = newSuperName;
        this.templateName = templateClass.getName().replace('.', '/');
        this.templateType = Type.getType("L" + templateName + ";");
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

    /**
     * A method visitor that replaces all references to the old superclass
     */
    private class ChangeParentMethodAdapter extends MethodVisitor {

        public ChangeParentMethodAdapter(int api, MethodVisitor mv) {
            super(api, mv);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            desc = replaceDesc(desc);
            if (opcode == Opcodes.INVOKESPECIAL && owner.equals(oldSuperName)) {
                // change super calls
                visitMethodInsn(
                        Opcodes.INVOKESPECIAL,
                        newSuperName,
                        name,
                        desc,
                        false
                );
            } else if (owner.equals(templateName)) {
                // change calls to self
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
                if (arg instanceof Handle && templateName.equals(((Handle) arg).getOwner())) {
                    bsmArgs[i] = new Handle(((Handle) arg).getTag(), ChangeParentClassAdapter.this.name, ((Handle) arg).getName(), ((Handle) arg).getDesc(), ((Handle) arg).isInterface());
                }
            }
            super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
        }

        private String replaceDesc(String desc) {
            if (desc.contains(templateName)) {
                Type methodType = Type.getMethodType(desc);
                Type[] arguments = methodType.getArgumentTypes();
                StringBuilder buf = new StringBuilder();
                buf.append('(');
                for (Type argument : arguments) {
                    if (argument.getSort() == Type.OBJECT && templateName.equals(argument.getInternalName())) {
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
            if (cst.equals(templateType)) {
                cst = Type.getType("L" + ChangeParentClassAdapter.this.name + ";");
            }
            super.visitLdcInsn(cst);
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            if (owner.equals(templateName)) {
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
            if (type.getSort() == Type.OBJECT && type.getInternalName().equals(templateName)) {
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
                if (templateName.equals(local[i])) {
                    local[i] = ChangeParentClassAdapter.this.name;
                }
            }
            for (int i = 0; i < stack.length; i++) {
                if (templateName.equals(stack[i])) {
                    stack[i] = ChangeParentClassAdapter.this.name;
                }
            }
            super.visitFrame(type, nLocal, local, nStack, stack);
        }
    }
}
