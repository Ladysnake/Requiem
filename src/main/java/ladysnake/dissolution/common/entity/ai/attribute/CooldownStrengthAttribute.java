package ladysnake.dissolution.common.entity.ai.attribute;

import ladysnake.dissolution.api.corporeality.IPossessable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;

public class CooldownStrengthAttribute extends DelegatingAttribute {
    private IPossessable owner;

    public <T extends EntityLivingBase & IPossessable> CooldownStrengthAttribute(T entity) {
        super(entity.getAttributeMap(), entity.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE));
        this.owner = entity;
    }

    @Override
    public double getAttributeValue() {
        double strength = super.getAttributeValue();
        if (owner.isBeingPossessed()) {
            EntityPlayer player = owner.getPossessingEntity();
            double attackCharge = player.getCooledAttackStrength(0.5f);
            strength = strength * (0.2F + attackCharge * attackCharge * 0.8F);
        }
        return strength;
    }
}
