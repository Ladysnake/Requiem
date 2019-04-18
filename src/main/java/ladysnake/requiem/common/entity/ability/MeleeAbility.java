package ladysnake.requiem.common.entity.ability;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;

public class MeleeAbility extends DirectAbilityBase<MobEntity> {
    private final boolean ignoreDamageAttribute;

    public MeleeAbility(MobEntity owner) {
        this(owner, false);
    }

    public MeleeAbility(MobEntity owner, boolean ignoreDamageAttribute) {
        super(owner);
        this.ignoreDamageAttribute = ignoreDamageAttribute;
    }

    @Override
    public boolean trigger(PlayerEntity player, Entity target) {
        // We actually need to check if the entity has an attack damage attribute, because mojang doesn't.
        boolean success = (ignoreDamageAttribute || owner.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE) != null) && owner.attack(target);
        if (success && target instanceof LivingEntity) {
            player.getMainHandStack().onEntityDamaged((LivingEntity) target, player);
        }
        return success;
    }
}
