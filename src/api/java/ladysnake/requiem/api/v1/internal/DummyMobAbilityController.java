package ladysnake.requiem.api.v1.internal;

import ladysnake.requiem.api.v1.entity.ability.AbilityType;
import ladysnake.requiem.api.v1.entity.ability.MobAbilityController;
import net.minecraft.entity.Entity;

public class DummyMobAbilityController implements MobAbilityController {

    @Override
    public boolean useDirect(AbilityType type, Entity target) {
        return false;
    }

    @Override
    public boolean useIndirect(AbilityType type) {
        return false;
    }

    @Override
    public void updateAbilities() {
        // NO-OP
    }
}
