package ladysnake.requiem.api.v1.remnant;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.nbt.CompoundTag;

public interface DeathSuspender {
    void suspendDeath(DamageSource deathCause);

    boolean isLifeTransient();

    void setLifeTransient(boolean lifeTransient);

    void resumeDeath();

    CompoundTag toTag(CompoundTag tag);

    void fromTag(CompoundTag tag);
}
