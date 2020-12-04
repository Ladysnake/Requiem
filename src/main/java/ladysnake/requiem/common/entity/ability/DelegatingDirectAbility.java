package ladysnake.requiem.common.entity.ability;

import ladysnake.requiem.api.v1.entity.ability.AbilityType;
import ladysnake.requiem.api.v1.entity.ability.DirectAbility;
import ladysnake.requiem.api.v1.entity.ability.MobAbilityController;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class DelegatingDirectAbility<E extends LivingEntity, T extends Entity> implements DirectAbility<E, T> {
    private final LivingEntity owner;
    private final Class<T> targetType;
    private final AbilityType delegatedType;

    public DelegatingDirectAbility(LivingEntity owner, Class<T> targetType, AbilityType delegatedType) {
        this.owner = owner;
        this.targetType = targetType;
        this.delegatedType = delegatedType;
    }

    private MobAbilityController getDelegate() {
        return MobAbilityController.get(this.owner);
    }

    @Override
    public double getRange() {
        return this.getDelegate().getRange(this.delegatedType);
    }

    @Override
    public Class<T> getTargetType() {
        return this.targetType;
    }

    @Override
    public boolean canTarget(T target) {
        return this.getDelegate().canTarget(this.delegatedType, target);
    }

    @Override
    public boolean trigger(T target) {
        return this.getDelegate().useDirect(this.delegatedType, target);
    }

    @Override
    public float getCooldownProgress() {
        return this.getDelegate().getCooldownProgress(this.delegatedType);
    }

    @Override
    public Identifier getIconTexture() {
        return this.getDelegate().getIconTexture(this.delegatedType);
    }

    @Override
    public void update() {
        // NO-OP the delegate is already updated externally
    }

    @Override
    public void writeToPacket(PacketByteBuf buf) {
        // NO-OP the delegate is already synchronized externally
    }

    @Override
    public void readFromPacket(PacketByteBuf buf) {
        // NO-OP the delegate is already synchronized externally
    }
}
