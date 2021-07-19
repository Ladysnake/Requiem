package ladysnake.requiem.common.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

public class CreativeSoulVesselItem extends EmptySoulVesselItem {
    public CreativeSoulVesselItem(Settings settings) {
        super(settings);
    }

    @Override
    protected boolean wins(PlayerEntity user, int playerSoulStrength, LivingEntity entity, int targetSoulStrength) {
        return true;
    }
}
