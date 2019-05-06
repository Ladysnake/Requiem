package ladysnake.dissolution.common.entity;

import ladysnake.dissolution.api.corporeality.IPossessable;
import ladysnake.dissolution.api.possession.DissolutionPossessionApi;
import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.config.DissolutionConfigManager;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import org.objectweb.asm.*;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class PossessableEntityFactory {
    private static final ASMClassLoader LOADER = new ASMClassLoader();
    // A cache containing every entity type created by this factory
    private static final Map<Class<? extends EntityLivingBase>, Class<? extends EntityLivingBase>> GENERATED_POSSESSABLES = new HashMap<>();

    /**
     * Creates and registers a new class of {@link IPossessable possessable} entity derived from the given base.
     * <p>
     * The new class will have the given base class as its superclass and implement {@link IPossessable}, using
     * {@link EntityPossessableImpl} as a template.
     * @see DissolutionPossessionApi#registerPossessedVersion(Class, Class)
     */
    @SuppressWarnings("unchecked")
    public static <T extends EntityLivingBase> Class<? extends T> defineGenericPossessable(Class<T> baseEntityClass) {
        return (Class<? extends T>) GENERATED_POSSESSABLES.computeIfAbsent(baseEntityClass, base -> {
            try {
                final byte[] possessableImplBytes = Launch.classLoader.getClassBytes(EntityPossessableImpl.class.getName().replace('.', '/'));
                final ClassReader reader = new ClassReader(possessableImplBytes);
                final String name = getName(base);
                ClassWriter writer = new ClassWriter(0);
                ClassVisitor adapter = new ChangeParentClassAdapter(Opcodes.ASM5, writer, name.replace('.', '/'), base.getName().replace('.', '/'));
                reader.accept(adapter, 0);

                Class possessedEntityClass = LOADER.define(name, writer.toByteArray());
                DissolutionPossessionApi.registerPossessedVersion(base, possessedEntityClass);
                return possessedEntityClass;
            } catch (IOException e) {
                Dissolution.LOGGER.warn("Could not define a possessed version of an entity class", e);
            }
            return null;
        });
    }

    public static Stream<Map.Entry<Class<? extends EntityLivingBase>, Class<? extends EntityLivingBase>>> getAllGeneratedPossessables() {
        return GENERATED_POSSESSABLES.entrySet().stream();
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <T extends EntityLivingBase & IPossessable> T createPossessableEntityFrom(EntityLivingBase deadGuy) {
        if (DissolutionConfigManager.isEntityBlacklisted(deadGuy)) {
            return null;
        }
        if (deadGuy instanceof IPossessable) {
            return (T) deadGuy;
        }

        EntityLivingBase corpse = null;

        Class<? extends EntityLivingBase> clazz = deadGuy.getClass();
        Class<? extends EntityLivingBase> possessableClass = DissolutionPossessionApi.getPossessable(clazz);
        if (possessableClass != null) {
            corpse = (EntityLivingBase) EntityList.newEntity(possessableClass, deadGuy.world);
        }

        if (corpse != null) {
            for (IAttributeInstance attribute : deadGuy.getAttributeMap().getAllAttributes()) {
                IAttributeInstance corpseAttribute = corpse.getAttributeMap().getAttributeInstance(attribute.getAttribute());
                //noinspection ConstantConditions
                if (corpseAttribute == null) {
                    corpseAttribute = corpse.getAttributeMap().registerAttribute(attribute.getAttribute());
                }
                corpseAttribute.setBaseValue(attribute.getBaseValue());
                for (AttributeModifier modifier : attribute.getModifiers()) {
                    // Special case Epic Siege Mod as its modifiers will get reapplied afterwards.
                    if (!modifier.getName().startsWith("ESM_TWEAK")) {
                        corpseAttribute.removeModifier(modifier.getID());
                        corpseAttribute.applyModifier(modifier);
                    }
                }
            }
            for (PotionEffect potionEffect : deadGuy.getActivePotionEffects()) {
                corpse.addPotionEffect(new PotionEffect(potionEffect));
            }
            List<EntityDataManager.DataEntry<?>> dataEntries = deadGuy.getDataManager().getAll();
            if (dataEntries != null) {
                for (EntityDataManager.DataEntry entry : dataEntries) {
                    corpse.getDataManager().set(entry.getKey(), entry.getValue());
                }
            }
            corpse.setPositionAndRotation(deadGuy.posX, deadGuy.posY, deadGuy.posZ, deadGuy.rotationYaw, deadGuy.rotationPitch);
            corpse.onUpdate();
        }

        return (T) corpse;
    }

    private static String getName(Class baseClass) {
        return String.format("%s_%s_%d", EntityPossessableImpl.class.getName(), baseClass.getSimpleName(), baseClass.getName().hashCode());
    }

    /**
     * A class loader allowing the creation of any class from its bytecode, as well as its injection into the classpath
     */
    private static class ASMClassLoader extends ClassLoader {
        private ASMClassLoader() {
            super(ASMClassLoader.class.getClassLoader());
        }

        public Class<?> define(String name, byte[] data) {
            return defineClass(name, data, 0, data.length);
        }
    }

    /**
     * A class visitor that can change a class' supertype
     */
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
                } else if (owner.equals(TEMPLATE_NAME)) {
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
