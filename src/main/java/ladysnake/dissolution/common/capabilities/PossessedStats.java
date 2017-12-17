package ladysnake.dissolution.common.capabilities;

import ladysnake.dissolution.api.corporeality.IPossessedStats;
import ladysnake.dissolution.common.registries.CorporealityStatus;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;

public class PossessedStats implements IPossessedStats {

    private int health;
    private CapabilityIncorporealHandler.DefaultIncorporealHandler mainHandler;

    public PossessedStats(CapabilityIncorporealHandler.DefaultIncorporealHandler mainHandler) {
        this.mainHandler = mainHandler;
    }

    @Override
    public int getPurifiedHealth() {
        return health;
    }

    @Override
    public void purifyHealth(int purified) {
        this.setPurifiedHealth(health+1);
    }

    @Override
    public void setPurifiedHealth(int health) {
        if(!(mainHandler.getPossessed() instanceof EntityLivingBase)) return;
        EntityLivingBase possessed = ((EntityLivingBase)mainHandler.getPossessed());
        if(health >= possessed.getHealth() && !possessed.world.isRemote) {
            mainHandler.setCorporealityStatus(CorporealityStatus.BODY);
            this.health = 0;
            possessed.world.removeEntity(possessed);
        } else this.health = health;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("purifiedHealth", health);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        this.health = nbt.getInteger("purifiedHealth");
    }
}
