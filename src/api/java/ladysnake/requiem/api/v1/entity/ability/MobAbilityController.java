package ladysnake.requiem.api.v1.entity.ability;

import ladysnake.requiem.api.v1.internal.DummyMobAbilityController;
import net.minecraft.entity.Entity;

/**
 * A {@link MobAbilityController} is interacted with by a player to use special {@link MobAbility mob abilities}
 */
public interface MobAbilityController {
    MobAbilityController DUMMY = new DummyMobAbilityController();

    boolean useDirect(AbilityType type, Entity target);

    boolean useIndirect(AbilityType type);

    void updateAbilities();
}
