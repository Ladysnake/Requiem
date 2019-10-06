package ladysnake.pandemonium.common.entity.effect;

import ladysnake.pandemonium.common.entity.PlayerShellEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectType;

public class BodyRecallEffect extends StatusEffect {
    public BodyRecallEffect(StatusEffectType type, int color) {
        super(type, color);
    }

    @Override
    public boolean canApplyUpdateEffect(int time, int potency) {
        return time == 1;
    }

    @Override
    public void applyUpdateEffect(LivingEntity target, int potency) {
        if (target instanceof PlayerShellEntity) {
            PlayerShellEntity shell = (PlayerShellEntity) target;
            shell.getPlayerUuid().map(target.world::getPlayerByUuid).ifPresent(player -> {
                target.copyPositionAndRotation(player);
                shell.onSoulInteract(player);
            });
        }
    }
}
