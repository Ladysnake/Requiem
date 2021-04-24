package ladysnake.pandemonium.common.entity.effect;

import ladysnake.pandemonium.Pandemonium;
import ladysnake.pandemonium.common.PlayerSplitter;
import ladysnake.pandemonium.common.entity.PlayerShellEntity;
import ladysnake.requiem.api.v1.event.requiem.PossessionStartCallback;
import ladysnake.requiem.api.v1.possession.Possessable;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import ladysnake.requiem.api.v1.remnant.RemnantType;
import ladysnake.requiem.api.v1.remnant.StickyStatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public class PenanceStatusEffect extends StatusEffect {
    protected PenanceStatusEffect(StatusEffectType type, int color) {
        super(type, color);
    }

    @Override
    public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        super.onApplied(entity, attributes, amplifier);
        if (amplifier >= 1) {
            PossessionComponent c = PossessionComponent.KEY.getNullable(entity);
            if (c != null) { // It's 1 because amplifiers are 0 based for some fucking reason
                c.stopPossessing();
            }
            if (entity instanceof ServerPlayerEntity && !(RemnantComponent.get((PlayerEntity) entity)).isVagrant()) {
                if (RemnantComponent.get((PlayerEntity) entity).getRemnantType().isDemon()) {
                    PlayerSplitter.split((ServerPlayerEntity) entity);
                } else {
                    entity.damage(DamageSource.MAGIC, amplifier*4);
                }
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    public static PossessionStartCallback.@NotNull Result canPossess(MobEntity target, PlayerEntity possessor, boolean simulate) {
        if (possessor.hasStatusEffect(PandemoniumStatusEffects.PENANCE) && possessor.getStatusEffect(PandemoniumStatusEffects.PENANCE).getAmplifier() >= 2 && target instanceof PlayerShellEntity) {
            return PossessionStartCallback.Result.DENY;
        }
        return PossessionStartCallback.Result.PASS;
    }
}
