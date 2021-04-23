package ladysnake.pandemonium.common.entity.effect;

import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import ladysnake.requiem.api.v1.remnant.StickyStatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectType;
import org.jetbrains.annotations.NotNull;

public class PenanceStatusEffect extends StatusEffect implements StickyStatusEffect {
    protected PenanceStatusEffect(StatusEffectType type, int color) {
        super(type, color);
    }

    /**
     * If this method returns {@code true}, this effect cannot be cleared by anything
     * except the /clear command.
     *
     * @param entity the entity to check
     */
    @Override
    public boolean shouldStick(@NotNull LivingEntity entity) {
        return RemnantComponent.isVagrant(entity);
    }
}
