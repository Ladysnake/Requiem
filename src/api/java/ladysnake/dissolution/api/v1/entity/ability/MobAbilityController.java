package ladysnake.dissolution.api.v1.entity.ability;

import net.minecraft.entity.Entity;

/**
 * A {@link MobAbilityController} is interacted with by a player to use special {@link MobAbility mob abilities}
 */
public interface MobAbilityController {
    boolean useDirect(AbilityType type, Entity target);

    boolean useIndirect(AbilityType type);

    void updateAbilities();
}
