package ladysnake.dissolution.common.entity.ai.attribute;

import ladysnake.dissolution.api.v1.possession.Possessable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;

public class CooldownStrengthAttribute extends DelegatingAttribute {
    private Possessable owner;

    public <T extends LivingEntity & Possessable> CooldownStrengthAttribute(T entity) {
        super(entity.getAttributeContainer(), entity.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE));
        this.owner = entity;
    }

    @Override
    public double getValue() {
        final double strength = super.getValue();
        return owner.getPossessor().map(player -> {
            double attackCharge = player.method_7261(0.5f);
            return strength * (0.2F + attackCharge * attackCharge * 0.8F);
        }).orElse(strength);
    }
}
