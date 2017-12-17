package ladysnake.dissolution.common.registries;

import ladysnake.dissolution.api.corporeality.ICorporealityStatus;
import ladysnake.dissolution.common.Reference;
import net.minecraft.entity.player.EntityPlayer;

public class SoulCorporealityStatus extends IncorporealStatus {

    public static final ICorporealityStatus SOUL = new SoulCorporealityStatus()
            .setRegistryName(Reference.MOD_ID, "soul");

    public SoulCorporealityStatus() {
        super();
    }

    public void initState(EntityPlayer owner) {
        super.initState(owner);
        owner.setEntityInvulnerable(true);
        owner.eyeHeight = 0.8f;
        owner.capabilities.disableDamage = true;
    }

    public void resetState(EntityPlayer owner) {
        super.resetState(owner);
        owner.setEntityInvulnerable(false);
        owner.eyeHeight = owner.getDefaultEyeHeight();
        owner.capabilities.disableDamage = false;
    }
}
