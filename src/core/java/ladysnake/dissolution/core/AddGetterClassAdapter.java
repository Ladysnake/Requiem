package ladysnake.dissolution.core;

import net.minecraft.launchwrapper.Launch;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.message.FormattedMessage;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.io.IOException;
import java.util.*;

public class AddGetterClassAdapter extends ClassVisitor {

    public AddGetterClassAdapter(int api, ClassVisitor cv) {
        super(api, cv);
    }

    @Override
    public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name,
                                     final String desc, final String signature, final String[] exceptions) {
        MethodVisitor mv;
        mv = cv.visitMethod(access, name, desc, signature, exceptions);
        final ASMUtil.MethodKey key = AddGetterAdapter.mutableKey;
        key.name = name;
        key.desc = desc;
        if (mv != null && !AddGetterAdapter.gettersSettersNames.containsKey(key)) {
            mv = new AddGetterAdapter(api, mv);
        }
        return mv;
    }

    public static class AddGetterAdapter extends MethodVisitor {
        static final List<String> entityClasses = new ArrayList<>();
        static Map<ASMUtil.MethodKey, Pair<String, String>> gettersSettersNames = null;
        private static final ASMUtil.MethodKey mutableKey = new ASMUtil.MethodKey(null, null);

        public AddGetterAdapter(final int api, final MethodVisitor mv) {
            super(api, mv);
        }

        static void init() {
            if (gettersSettersNames != null) return;

            gettersSettersNames = new HashMap<>();
            String clazz = "net/minecraft/entity/player/EntityPlayer";
            try {
                do {
                    // get every public field from the EntityPlayer class and its parents
                    final ClassReader reader = new ClassReader(Launch.classLoader.getClassBytes(clazz));
                    final ClassNode classNode = new ClassNode();
                    reader.accept(classNode, 0);
                    for (FieldNode fieldNode : classNode.fields) {
                        // if it is public and not static
                        if ((fieldNode.access & (Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC)) == Opcodes.ACC_PUBLIC) {
                            // create getter / setter names that do not collide with existing methods
                            final StringBuilder setterBuilder = new StringBuilder("get").append(fieldNode.name.substring(0,1).toUpperCase(Locale.ENGLISH)).append(fieldNode.name.substring(1));
                            final StringBuilder getterBuilder = new StringBuilder("set").append(fieldNode.name.substring(0,1).toUpperCase(Locale.ENGLISH)).append(fieldNode.name.substring(1));
                            do {
                                setterBuilder.insert(0, '_');
                                getterBuilder.insert(0, '_');
                            } while (classNode.methods.stream().anyMatch(mn -> mn.name.contentEquals(getterBuilder) || mn.name.contentEquals(setterBuilder)));
                            final String setterName = setterBuilder.toString();
                            final String getterName = getterBuilder.toString();
                            generateGetterSetter(classNode, fieldNode, setterName, getterName);


                            // add the field name and description to the list of accesses to replace
                            gettersSettersNames.put(new ASMUtil.MethodKey(fieldNode.name, fieldNode.desc), Pair.of(getterName, setterName));
                        }
                    }
                    entityClasses.add(clazz);
                    clazz = classNode.superName;
                } while (clazz != null && !clazz.equals("java/lang/Object"));
            } catch (IOException e) {
                DissolutionLoadingPlugin.LOGGER.error(new FormattedMessage("Error while reading {} methods !", clazz), e);
            }
        }

        @Override
        public void visitFieldInsn(final int opcode, final String owner, final String name, final String desc) {
            final ASMUtil.MethodKey key = mutableKey;
            key.name = name;
            key.desc = desc;
            if (opcode == Opcodes.GETFIELD && gettersSettersNames.containsKey(key) && entityClasses.contains(owner)) {
                visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL,
                        owner,
                        gettersSettersNames.get(new ASMUtil.MethodKey(name, desc)).getLeft(),
                        "()" + desc,
                        false
                );
            } else if (opcode == Opcodes.PUTFIELD && gettersSettersNames.containsKey(key) && entityClasses.contains(owner)) {
                visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL,
                        owner,
                        gettersSettersNames.get(new ASMUtil.MethodKey(name, desc)).getRight(),
                        "(" + desc + ")V",
                        false
                );
            } else {
                super.visitFieldInsn(opcode, owner, name, desc);
            }
        }
    }

    static void generateGetterSetter(ClassNode classNode, FieldNode fieldNode, String getterName, String setterName) {
        // generate a getter for the field
        MethodNode methodNode = new MethodNode(
                Opcodes.ACC_PUBLIC,
                getterName,
                "()" + fieldNode.desc,
                fieldNode.signature,
                null
        );

        methodNode.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        methodNode.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, classNode.name, fieldNode.name, fieldNode.desc));
        methodNode.instructions.add(new InsnNode(Type.getType(fieldNode.desc).getOpcode(Opcodes.IRETURN)));
        classNode.methods.add(methodNode);

        // generate a setter for the field
        if ((fieldNode.access & Opcodes.ACC_FINAL) == 0) {
            methodNode = new MethodNode(
                    Opcodes.ACC_PUBLIC,
                    setterName,
                    "(" + fieldNode.desc + ")V",
                    fieldNode.signature,
                    null
            );
            methodNode.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            methodNode.instructions.add(new VarInsnNode(Type.getType(fieldNode.desc).getOpcode(Opcodes.ILOAD), 1));
            methodNode.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, classNode.name, fieldNode.name, fieldNode.desc));
            methodNode.instructions.add(new InsnNode(Opcodes.RETURN));
            classNode.methods.add(methodNode);
        }

    }
}
