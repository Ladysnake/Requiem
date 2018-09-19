package ladysnake.dissolution.common.registries;

import net.minecraft.entity.player.EntityPlayer;

public class SoulCorporealityStatus extends IncorporealStatus {

    public SoulCorporealityStatus() {
        super();
    }

    public void initState(EntityPlayer owner) {
        super.initState(owner);
        owner.setEntityInvulnerable(true);
        owner.capabilities.disableDamage = true;
    }

    public void resetState(EntityPlayer owner) {
        super.resetState(owner);
        owner.setEntityInvulnerable(false);
        owner.capabilities.disableDamage = owner.isCreative();
    }
}
