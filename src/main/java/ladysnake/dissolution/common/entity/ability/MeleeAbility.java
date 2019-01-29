package ladysnake.dissolution.common.entity.ability;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;

public class MeleeAbility extends DirectAbilityBase<MobEntity> {
    public MeleeAbility(MobEntity owner) {
        super(owner);
    }

    @Override
    public boolean trigger(PlayerEntity player, Entity target) {
        // We actually need to check if the entity has an attack damage attribute, because mojang doesn't.
        boolean success = owner.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE) != null && owner.method_6121(target);
        if (success && target instanceof LivingEntity) {
            player.getMainHandStack().onEntityDamaged((LivingEntity) target, player);
        }
        return success;
    }
}
