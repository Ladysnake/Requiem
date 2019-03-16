package ladysnake.dissolution.common.impl.ability;

import ladysnake.dissolution.api.v1.entity.ability.AbilityType;
import ladysnake.dissolution.api.v1.entity.ability.MobAbilityController;
import net.minecraft.entity.Entity;

public class DummyMobAbilityController implements MobAbilityController {
    public static final DummyMobAbilityController DUMMY = new DummyMobAbilityController();

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
