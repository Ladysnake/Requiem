package ladysnake.requiem.common.entity.ai.attribute;

import ladysnake.requiem.api.v1.possession.Possessable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;

public class CooldownStrengthAttribute extends DelegatingAttribute {
    private final Possessable owner;

    public <T extends LivingEntity & Possessable> CooldownStrengthAttribute(T entity) {
        super(entity.getAttributeContainer(), entity.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE));
        this.owner = entity;
    }

    @Override
    public double getValue() {
        final double strength = super.getValue();
        PlayerEntity possessor = this.owner.getPossessor();
        if (possessor != null) {
            double attackCharge = possessor.method_7261(0.5f);
            return strength * (0.2F + attackCharge * attackCharge * 0.8F);
        }
        return strength;
    }
}
