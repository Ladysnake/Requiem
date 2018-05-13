package ladysnake.dissolution.core;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import org.apache.logging.log4j.message.FormattedMessage;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

public class DissolutionClassTransformer implements IClassTransformer {
    private final Map<ASMUtil.MethodKey, ASMUtil.MethodInfo> livingBaseMethods = new HashMap<>();

    @Override
    public byte[] transform(String className, String transformedName, byte[] basicClass) {
        // replace every direct access to entity fields by getters and setters
        // TODO make it work
        basicClass = kotlinify(basicClass);

        // create getters and setters for every public field
        if (AddGetterClassAdapter.entityClasses.contains(className.replace('.', '/'))) {
            basicClass = invokeCthulhu(basicClass, classNode -> {
                for (FieldNode fieldNode : classNode.fields) {
                    if ((fieldNode.access & Opcodes.ACC_PUBLIC) == Opcodes.ACC_PUBLIC) {
                        MethodNode methodNode = new MethodNode(
                                Opcodes.ACC_PUBLIC,
                                "get" + fieldNode.name.substring(0,1).toUpperCase(Locale.ENGLISH) + fieldNode.name.substring(1) + 0,
                                "()" + fieldNode.desc,
                                fieldNode.signature,
                                null
                        );

                        methodNode.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        methodNode.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, classNode.name, fieldNode.name, fieldNode.desc));
                        methodNode.instructions.add(new InsnNode(Type.getType(fieldNode.desc).getOpcode(Opcodes.IRETURN)));
                        classNode.methods.add(methodNode);
                        methodNode = new MethodNode(
                                Opcodes.ACC_PUBLIC,
                                "set" + fieldNode.name.substring(0,1).toUpperCase(Locale.ENGLISH) + fieldNode.name.substring(1) + 0,
                                "(" + fieldNode.desc + ")V",
                                fieldNode.signature,
                                null
                        );
                        methodNode.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        methodNode.instructions.add(new VarInsnNode(Type.getType(fieldNode.desc).getOpcode(Opcodes.ILOAD), 1));
                        methodNode.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, classNode.name, fieldNode.name, fieldNode.desc));
                        methodNode.instructions.add(new InsnNode(Opcodes.RETURN));
                        System.out.println("Created getters and setters for " + fieldNode.name + fieldNode.desc + " in class " + transformedName);
                        classNode.methods.add(methodNode);
                    }
                }
            });
        }
        switch (transformedName) {
            case "net.minecraft.entity.player.EntityPlayer":
                for (String clazz : new String[] {"net.minecraft.entity.Entity", "net.minecraft.entity.EntityLivingBase"}) {
                    try {
                        // get every method from the parent class
                        ClassReader reader = new ClassReader(Launch.classLoader.getClassBytes(clazz));
                        ClassNode classNode = new ClassNode();
                        reader.accept(classNode, 0);
                        for (MethodNode methodNode : classNode.methods) {
                            // do not try to override a final method, methods that can't be called nor the constructor
                            if ((methodNode.access & (Opcodes.ACC_FINAL | Opcodes.ACC_STATIC | Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED)) == 0 &&
                                    !methodNode.name.equals("<init>")) {
                                // store the method information for use in EntityPlayer
                                ASMUtil.MethodInfo info = new ASMUtil.MethodInfo(methodNode);
                                livingBaseMethods.put(new ASMUtil.MethodKey(methodNode.name, methodNode.desc), info);
                            }
                        }
                    } catch (IOException e) {
                        DissolutionLoadingPlugin.LOGGER.error(new FormattedMessage("Error while reading {} methods !", clazz), e);
                    }
                }
                return invokeCthulhu(basicClass, classNode -> {
                    List<MethodNode> methods = classNode.methods;
                    for (MethodNode methodNode : methods) {
                        ASMUtil.MethodKey key = new ASMUtil.MethodKey(methodNode.name, methodNode.desc);
                        ASMUtil.MethodInfo info = livingBaseMethods.remove(key);
                        // if the method overrides an EntityLivingBase method, inject a hook
                        if (info != null) {
                            methodNode.instructions.insert(generateDelegationInsnList(methodNode.name, methodNode.desc));
                        }

                        // special case capability methods to avoid recursive calls
                        if (methodNode.name.equals("getCapability") || methodNode.name.equals("hasCapability")) {
                            insertCapabilityHook(methodNode);
                        }
                    }
                    // go through every remaining method and generate a delegating override
                    livingBaseMethods.forEach((methodKey, methodInfo) -> createDelegationOverride(classNode, methodKey.name, methodKey.desc, methodInfo));
                });
        }
        return basicClass;
    }

    /**
     * Inserts a set of instructions in a (has/get)Capability method to special case the possession capability
     *
     * @param methodNode the method in which the instructions should be inserted
     */
    private void insertCapabilityHook(MethodNode methodNode) {
        InsnList preInstructions = new InsnList();
        preInstructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
        preInstructions.add(new FieldInsnNode(
                Opcodes.GETSTATIC,
                "ladysnake/dissolution/core/DissolutionHooks",
                "cap",
                "Lnet/minecraftforge/common/capabilities/Capability;"
        ));
        LabelNode lbl = new LabelNode();
        preInstructions.add(new JumpInsnNode(Opcodes.IF_ACMPNE, lbl));
        preInstructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        preInstructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
        preInstructions.add(new VarInsnNode(Opcodes.ALOAD, 2));
        preInstructions.add(new MethodInsnNode(
                Opcodes.INVOKESPECIAL,
                "net/minecraft/entity/EntityLivingBase",
                methodNode.name,
                methodNode.desc,
                false
        ));
        preInstructions.add(new InsnNode(Type.getReturnType(methodNode.desc).getOpcode(Opcodes.IRETURN)));
        preInstructions.add(lbl);
        methodNode.instructions.insert(preInstructions);
    }

    /**
     * Creates a method override from scratch, delegating the call if possible and calling super if not
     *
     * @param classNode  the class in which to add the method
     * @param methodName the name of the method to override
     * @param methodDesc the description of the method to override
     * @param info       the additional information defining the method
     */
    private void createDelegationOverride(ClassNode classNode, String methodName, String methodDesc, ASMUtil.MethodInfo info) {
        MethodNode methodNode = new MethodNode(info.access, methodName, methodDesc, info.signature, info.exceptions);
        if (info.abstr) throw new RuntimeException("Why do I have to generate an abstract method ?!");
        methodNode.parameters = info.params;
        // if the player is possessing something, delegate the call
        InsnList instructions = generateDelegationInsnList(methodName, methodDesc);
        // else call super
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        generateCall(instructions, methodName, methodDesc, Opcodes.INVOKESPECIAL);
        instructions.add(new InsnNode(Type.getType(methodDesc).getReturnType().getOpcode(Opcodes.IRETURN)));
        methodNode.instructions.add(instructions);
        classNode.methods.add(methodNode);
    }

    /**
     * Generates the bytecode instructions to delegate a call to the possessed entity, if any
     *
     * @param methodName the name of the method for which to generate a delegating call
     * @param methodDesc the description of the method for which to generate a delegating call
     * @return an instruction list containing the generated instructions
     */
    private InsnList generateDelegationInsnList(String methodName, String methodDesc) {
        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0)); // thisPlayer
        // get the possessed entity
        instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "ladysnake/dissolution/core/DissolutionHooks",
                "getPossessedEntity",
                "(Lnet/minecraft/entity/Entity;)Lnet/minecraft/entity/EntityLivingBase;",
                false
        ));
        // store the result in a local variable
        int storedRet = getFirstFreeSlot(methodDesc);
        instructions.add(new VarInsnNode(Opcodes.ASTORE, storedRet));
        // If the result is null, execute the rest of the method normally
        LabelNode lbl = new LabelNode();
        instructions.add(new VarInsnNode(Opcodes.ALOAD, storedRet));
        instructions.add(new JumpInsnNode(Opcodes.IFNULL, lbl));
        // else, delegate the call to the possessed entity
        instructions.add(new VarInsnNode(Opcodes.ALOAD, storedRet));
        generateCall(instructions, methodName, methodDesc, Opcodes.INVOKEVIRTUAL);
        // and return immediately
        instructions.add(new InsnNode(Type.getType(methodDesc).getReturnType().getOpcode(Opcodes.IRETURN)));
        instructions.add(lbl);
        return instructions;
    }

    /**
     * Generates the instructions to load method parameters. The callee should already be on the stack.
     *
     * @param methodName the name of the method to call
     * @param methodDesc the description of the method to call
     * @param invokeCode the {@link Opcodes} that should be used when invoking the method
     */
    private void generateCall(InsnList instructions, String methodName, String methodDesc, int invokeCode) {
        Type[] paramTypes = Type.getArgumentTypes(methodDesc);
        int paramIndex = 1;
        // add each additional parameter in order, starting from 1
        for (Type paramType : paramTypes) {
            int code = paramType.getOpcode(Opcodes.ILOAD);
            instructions.add(new VarInsnNode(code, paramIndex));
            paramIndex += paramType.getSize();
        }
        instructions.add(new MethodInsnNode(invokeCode, "net/minecraft/entity/EntityLivingBase", methodName, methodDesc, false));
    }

    /**
     * @param methodDesc the description of the method being called
     * @return the relative address of the first free variable after method args
     */
    private int getFirstFreeSlot(String methodDesc) {
        Type methodType = Type.getType(methodDesc);
        Type[] paramTypes = methodType.getArgumentTypes();
        int paramIndex = 1;
        for (Type paramType : paramTypes) {
            paramIndex += paramType.getSize();
        }
        return paramIndex;
    }

    private byte[] kotlinify(byte[] basicClass) {
        ClassReader reader = new ClassReader(basicClass);
        ClassVisitor writer = new SafeClassWriter(0);
        ClassVisitor kotlinifier = new AddGetterClassAdapter(Opcodes.ASM5, writer);
        reader.accept(kotlinifier, 0);
        return ((SafeClassWriter) writer).toByteArray();
    }

    /**
     * Transforms a class using the given transformer
     *
     * @param basicClass  a byte array representing the class being transformed
     * @param transformer a consumer taking a {@link ClassNode}
     * @return the class' bytecode after transformation
     */
    private byte[] invokeCthulhu(byte[] basicClass, Consumer<ClassNode> transformer) {
        ClassReader reader = new ClassReader(basicClass);
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, 0);

        transformer.accept(classNode);

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(writer);

        return writer.toByteArray();
    }
}
