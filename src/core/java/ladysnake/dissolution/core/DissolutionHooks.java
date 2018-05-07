package ladysnake.dissolution.core;

import ladysnake.dissolution.api.corporeality.IIncorporealHandler;
import ladysnake.dissolution.api.corporeality.IPossessable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class DissolutionHooks {
    @CapabilityInject(IIncorporealHandler.class)
    public static Capability<IIncorporealHandler> cap;

    @SuppressWarnings("unused") // called by ASM voodoo magic
    public static EntityLivingBase getPossessedEntity(Entity player) {
        if (player.hasCapability(cap, null)) {
            IPossessable possessable = player.getCapability(cap, null).getPossessed();
            if (possessable instanceof EntityLivingBase)
                return (EntityLivingBase) possessable;
        }
        return null;
    }

}
