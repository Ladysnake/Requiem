package ladysnake.dissolution.core;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.minecraft.launchwrapper.Launch;
import org.apache.logging.log4j.message.FormattedMessage;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class AddGetterClassAdapter extends ClassVisitor {
    static final List<String> entityClasses = Arrays.asList("net/minecraft/entity/Entity", "net/minecraft/entity/EntityLivingBase", "net/minecraft/entity/player/EntityPlayer");
    private static Map<String, String> publicFields = null;
    private static final LoadingCache<String, Boolean> entityClassCache = CacheBuilder.newBuilder().build(CacheLoader.from(AddGetterClassAdapter::isEntityClass));

    public AddGetterClassAdapter(int api, ClassVisitor cv) {
        super(api, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name,
                                     String desc, String signature, String[] exceptions) {
        MethodVisitor mv;
        mv = cv.visitMethod(access, name, desc, signature, exceptions);
        if (mv != null/* && !name.equals("<init>")*/) {
            mv = new AddGetterAdapter(api, mv);
        }
        return mv;
    }

    private static boolean isPublicField(String fieldName, String fieldDesc) {
        if (publicFields == null) {
            publicFields = new HashMap<>();
            for (String clazz : entityClasses) {
                try {
                    // get every public field from the EntityPlayer class and its parents
                    ClassReader reader = new ClassReader(Launch.classLoader.getClassBytes(clazz));
                    ClassNode classNode = new ClassNode();
                    reader.accept(classNode, 0);
                    for (FieldNode fieldNode : classNode.fields) {
                        if ((fieldNode.access & Opcodes.ACC_PUBLIC) == Opcodes.ACC_PUBLIC) {
                            publicFields.put(fieldNode.name, fieldNode.desc);
                        }
                    }
                } catch (IOException e) {
                    DissolutionLoadingPlugin.LOGGER.error(new FormattedMessage("Error while reading {} methods !", clazz), e);
                }
            }
        }
        return fieldDesc.equals(publicFields.get(fieldName));
    }

    private static boolean isEntityClass(String className) {
        if (className == null)
            return false;
        if (entityClasses.contains(className))
            return true;
        ClassReader reader;
        try {
            reader = new ClassReader(Launch.classLoader.getClassBytes(className.replace('/', '.')));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, 0);
        try {
            return classNode.superName != null && entityClassCache.get(classNode.superName);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public static class AddGetterAdapter extends MethodVisitor {

        public AddGetterAdapter(int api, MethodVisitor mv) {
            super(api, mv);
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            if (opcode == Opcodes.GETFIELD && isPublicField(name, desc)) {
                try {
                    if (entityClassCache.get(owner)) {
                        visitMethodInsn(
                                Opcodes.INVOKEVIRTUAL,
                                owner,
                                "get"+ name.substring(0,1).toUpperCase(Locale.ENGLISH) + name.substring(1) + 0,
                                "()" + desc,
                                false
                        );
                    }
                } catch (ExecutionException e) {
                    DissolutionLoadingPlugin.LOGGER.error(new FormattedMessage("Error while looking up whether {} is an entity subclass", owner), e);
                }
            } else if (opcode == Opcodes.PUTFIELD && isPublicField(name, desc) && entityClasses.contains(owner)) {
                visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL,
                        owner,
                        "set" + name.substring(0, 1).toUpperCase(Locale.ENGLISH) + name.substring(1) + 0,
                        "(" + desc + ")V",
                        false
                );
            } else {
                super.visitFieldInsn(opcode, owner, name, desc);
            }
        }
    }
}
