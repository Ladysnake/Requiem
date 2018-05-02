package ladysnake.dissolution.core;

import ladysnake.dissolution.api.corporeality.IIncorporealHandler;
import ladysnake.dissolution.api.corporeality.IPossessable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class DissolutionHooks {
    @CapabilityInject(IIncorporealHandler.class)
    public static Capability<IIncorporealHandler> cap;

    public static EntityLivingBase getPossessedEntity(EntityPlayer player) {
        if (player.hasCapability(cap, null)) {
            IPossessable possessable = player.getCapability(cap, null).getPossessed();
            if (possessable instanceof EntityLivingBase)
                return (EntityLivingBase) possessable;
        }
        return null;
    }
}
