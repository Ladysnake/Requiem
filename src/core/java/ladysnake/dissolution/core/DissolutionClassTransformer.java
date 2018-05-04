package ladysnake.dissolution.core;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.Printer;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

public class DissolutionClassTransformer implements IClassTransformer {
    private final Map<ASMUtil.MethodKey, ASMUtil.MethodInfo> livingBaseMethods = new HashMap<>();

    @Override
    public byte[] transform(String className, String transformedName, byte[] basicClass) {
        switch (transformedName) {
            case "net.minecraft.entity.player.EntityPlayer":
                try {
                    // get every method from EntityLivingBase
                    ClassReader reader = new ClassReader(Launch.classLoader.getClassBytes("net.minecraft.entity.EntityLivingBase"));
                    ClassNode classNode = new ClassNode();
                    reader.accept(classNode, 0);
                    {
                        for (MethodNode methodNode : classNode.methods) {
                            // do not try to override a final method nor methods that can't be called
                            if ((methodNode.access & (Opcodes.ACC_FINAL | Opcodes.ACC_STATIC | Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED)) == 0) {
                                System.out.println(FMLDeobfuscatingRemapper.INSTANCE.unmap(methodNode.name) + " | " + methodNode.desc);
                                // store the method information for use in EntityPlayer
                                ASMUtil.MethodInfo info = new ASMUtil.MethodInfo(methodNode);
                                livingBaseMethods.put(new ASMUtil.MethodKey(methodNode.name, methodNode.desc), info);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return invokeCthulhu(basicClass, classNode -> {
                    for (MethodNode methodNode : classNode.methods) {
                        ASMUtil.MethodKey key = new ASMUtil.MethodKey(methodNode.name, methodNode.desc);
                        ASMUtil.MethodInfo info = livingBaseMethods.remove(key);
                        // if the method overrides an EntityLivingBase method, inject a hook
                        if (info != null) {
                            InsnList preInstructions = generateDelegationInsnList(methodNode.name, methodNode.desc);
                            System.out.println("==================" + methodNode.name + methodNode.desc + "===============");
                            dump(preInstructions);
                            methodNode.instructions.insert(preInstructions);
                        }
                    }
                    // go through every remaining method and generate a delegating override
//                    livingBaseMethods.forEach((methodKey, methodInfo) -> createDelegationOverride(classNode, methodKey.name, methodKey.desc, methodInfo));
                });
        }
        return basicClass;
    }

    private void createDelegationOverride(ClassNode classNode, String methodName, String methodDesc, ASMUtil.MethodInfo info) {
        MethodNode methodNode = new MethodNode(info.access, methodName, methodDesc, info.signature, info.exceptions);
        if (info.abstr) throw new RuntimeException("Why do I have to generate an abstract method ?!");
        methodNode.parameters = info.params;
        // if the player is possessing something, delegate the call
        InsnList instructions = generateDelegationInsnList(methodName, methodDesc);
        // else call super
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new InsnNode(Opcodes.POP));
        instructions.add(new InsnNode(Opcodes.ACONST_NULL));
//        generateCall(instructions, methodName, methodDesc, Opcodes.INVOKESPECIAL);
        methodNode.instructions.add(instructions);
        classNode.methods.add(methodNode);
    }

    private InsnList generateDelegationInsnList(String methodName, String methodDesc) {
        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0)); // thisPlayer
        // get the possessed entity
        instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "ladysnake/dissolution/core/DissolutionHooks",
                "getPossessedEntity",
                "(Lnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/entity/EntityLivingBase;",
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
        generateCall(instructions, methodName, methodDesc, Opcodes.INVOKEDYNAMIC, storedRet);
//        // and return immediately
//        instructions.add(new InsnNode(Type.getType(methodDesc).getReturnType().getOpcode(Opcodes.IRETURN)));
        instructions.add(lbl);
        return instructions;
    }

    public static void dump(InsnList instructions) {
        Iterator<AbstractInsnNode> iterator = instructions.iterator();
        while (iterator.hasNext()) {
            AbstractInsnNode node = iterator.next();
            String s = node instanceof LabelNode ? "LABEL" : Printer.OPCODES[node.getOpcode()] + " ";
            if (node instanceof VarInsnNode) s += ((VarInsnNode) node).var;
            else if (node instanceof MethodInsnNode) s += ((MethodInsnNode) node).name + ((MethodInsnNode) node).desc;
            System.out.println(s);
        }
    }

    /**
     * Generates the instructions to load method parameters. The callee should already be on the stack.
     *  @param methodName the name of the method to call
     * @param methodDesc the description of the method to call
     * @param invokeCode the {@link Opcodes} that should be used when invoking the method
     */
    private void generateCall(InsnList instructions, String methodName, String methodDesc, int invokeCode, int storedRet) {
        Type[] paramTypes = Type.getArgumentTypes(methodDesc);
        int paramIndex = 1;
        // add each additional parameter in order, starting from 1
        for (Type paramType : paramTypes) {
            int code = paramType.getOpcode(Opcodes.ILOAD);
            for (int i = 0; i < paramType.getSize(); i++)
                instructions.add(new VarInsnNode(code, paramIndex+i));
            paramIndex += paramType.getSize();
        }
//        for (int i = paramTypes.length-1; i >= 0; i--) {
//            Type type = paramTypes[i];

//            instructions.add(new InsnNode(Opcodes.POP));
//            instructions.add(new MethodInsnNode(
//                    Opcodes.INVOKESTATIC,
//                    "ladysnake/dissolution/core/DissolutionHooks",
//                    "print",
//                    "(" +(type.getDescriptor().charAt(0) == 'L' ? "Ljava/lang/Object;" : type.getDescriptor()) + ")V",
//                    false
//            ));
//        }
//        instructions.add(new MethodInsnNode(
//                Opcodes.INVOKESTATIC,
//                "ladysnake/dissolution/core/DissolutionHooks",
//                "print",
//                "(Ljava/lang/Object;)V",
//                false
//        ));
        instructions.add(new MethodInsnNode(invokeCode, "net/minecraft/entity/EntityLivingBase", methodName, methodDesc, false));
        // if it returns something, remove
        if (!Type.getReturnType(methodDesc).getDescriptor().equals("V"))
            instructions.add(new InsnNode(Opcodes.POP));
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

    /**
     * Transforms a class using the given transformer
     * @param basicClass a byte array representing the class being transformed
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

    public static boolean hook(EntityPlayer d) {
        return d.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem() instanceof ItemArmor;
    }
}
