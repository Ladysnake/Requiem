package ladysnake.dissolution.core;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

public class DissolutionClassTransformer implements IClassTransformer {
    private final Map<ASMUtil.MethodKey, ASMUtil.MethodInfo> livingBaseMethods = new HashMap<>();

    @Override
    public byte[] transform(String className, String transformedName, byte[] basicClass) {
        switch (transformedName) {
            case "net.minecraft.entity.player.EntityPlayer":
//                try {
//                    Class.forName("net.minecraft.entity.EntityLivingBase");
//                } catch (ClassNotFoundException e) {
//                    e.printStackTrace();
//                }
                try {
                    // get every method from EntityLivingBase
                    ClassReader reader = new ClassReader(Launch.classLoader.getClassBytes("net.minecraft.entity.EntityLivingBase"));
                    ClassNode classNode = new ClassNode();
                    reader.accept(classNode, 0);
                    {
                        for (MethodNode methodNode : classNode.methods) {
                            // do not try to override a final method nor methods that can't be called
                            if ((methodNode.access & (Opcodes.ACC_FINAL | Opcodes.ACC_STATIC | Opcodes.ACC_PRIVATE)) == 0) {
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
                        ASMUtil.MethodInfo info = livingBaseMethods.get(key);
                        // if the method overrides an EntityLivingBase method, inject a hook
                        if (info != null) {
                            livingBaseMethods.remove(key);
                            InsnList preInstructions = new InsnList();
                            preInstructions.add(new VarInsnNode(Opcodes.ALOAD, 0)); // thisPlayer
                            preInstructions.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "ladysnake/dissolution/core/DissolutionHooks",
                                    "getPossessedEntity",
                                    "(Lnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/entity/EntityLivingBase;",
                                    false
                            ));
                            LabelNode lbl = new LabelNode();
                            preInstructions.add(new JumpInsnNode(Opcodes.IFNULL, lbl));
                            preInstructions.add(generateInsnList(methodNode.name, methodNode.desc));
                            preInstructions.add(new InsnNode(ASMUtil.getReturnCode(methodNode.desc)));
                            preInstructions.add(lbl);
                            methodNode.instructions.insert(preInstructions);
                        }
                    }
//                    livingBaseMethods.forEach((methodKey, methodInfo) -> createProxyMethod(classNode, methodKey.name, methodKey.desc, methodInfo));
                });
//            case "net.minecraft.entity.EntityLivingBase":
//                return invokeCthulhu(basicClass, classNode -> {
//                    for (MethodNode methodNode : classNode.methods) {
//                        // do not try to override a final method nor methods that can't be called
//                        if ((methodNode.access & Opcodes.ACC_FINAL) == 0) {
//                            System.out.println(FMLDeobfuscatingRemapper.INSTANCE.unmap(methodNode.name) + " | " + methodNode.desc);
//                            // store the method information for use in EntityPlayer
//                            ASMUtil.MethodInfo info = new ASMUtil.MethodInfo(methodNode);
//                            livingBaseMethods.put(new ASMUtil.MethodKey(methodNode.name, methodNode.desc), info);
//                        }
//                    }
//                });
        }
        return basicClass;
    }

    private void createProxyMethod(ClassNode classNode, String methodName, String methodDesc, ASMUtil.MethodInfo info) {
        MethodNode methodNode = new MethodNode();
        if (info.abstr) throw new RuntimeException("Why do I have to generate an abstract method ?!");
        methodNode.name = methodName;
        methodNode.desc = methodDesc;
        methodNode.parameters = info.params;
        InsnList instructions = generateInsnList(methodName, methodDesc);
        instructions.add(new InsnNode(ASMUtil.getReturnCode(methodDesc)));
        methodNode.instructions.add(instructions);
        classNode.methods.add(methodNode);
    }

    private InsnList generateInsnList(String methodName, String methodDesc) {
        InsnList instructions = new InsnList();
        // get parameter types
        char[] paramTypes = ASMUtil.parseMethodArguments(methodDesc);
        for (int i = 0; i < paramTypes.length; i++) {
            int code;
            char t = paramTypes[i];
            switch (t) {
                case 'Z':
                case 'B':
                case 'C':
                case 'S':
                case 'I':
                    code = Opcodes.ILOAD;
                    break;
                case 'J':
                    code = Opcodes.LLOAD;
                    break;
                case 'F':
                    code = Opcodes.FLOAD;
                    break;
                case 'D':
                    code = Opcodes.DLOAD;
                    break;
                default:
                    code = Opcodes.ALOAD;
            }
            instructions.add(new VarInsnNode(code, i));
        }
        instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "net/minecraft/entity/EntityLivingBase", methodName, methodDesc, false));
        return instructions;
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
