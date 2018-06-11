package ladysnake.dissolution.core;

import ladysnake.dissolution.api.corporeality.IIncorporealHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.UUID;

public class DissolutionHooks {
    @CapabilityInject(IIncorporealHandler.class)
    public static Capability<IIncorporealHandler> cap;

    // TODO access transformer
    private static final MethodHandle entity$uniqueId;

    static {
        MethodHandle temp = null;
        try {
            Field field = Entity.class.getDeclaredField("entityUniqueID");
            field.setAccessible(true);
            temp = MethodHandles.lookup().unreflectGetter(field);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            DissolutionLoadingPlugin.LOGGER.error("Could not access an entity field", e);
        }
        entity$uniqueId = temp;
    }

    @SuppressWarnings("unused") // called by ASM voodoo magic
    public static EntityLivingBase getPossessedEntity(Entity player) {
        if (player.hasCapability(cap, null)) {
            EntityLivingBase possessable = (EntityLivingBase) player.getCapability(cap, null).getPossessed();
            if (possessable != null) {
                return possessable;
            }
        }
        return null;
    }

    public static World getWorldDirect(EntityPlayer player) {
        return player.world;
    }

    public static UUID getUUIDDirect(EntityPlayer player) {
        try {
            return (UUID) entity$uniqueId.invoke(player);
        } catch (Throwable throwable) {
            DissolutionLoadingPlugin.LOGGER.error("could not access the entity's uuid");
            throw new RuntimeException(throwable);
        }
    }

}
