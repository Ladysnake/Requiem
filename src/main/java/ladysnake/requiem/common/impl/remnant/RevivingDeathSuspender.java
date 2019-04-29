package ladysnake.requiem.common.impl.remnant;

import ladysnake.requiem.api.v1.player.DeathSuspender;
import ladysnake.requiem.common.util.DamageSourceSerialization;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;

import javax.annotation.Nullable;

public class RevivingDeathSuspender implements DeathSuspender {
    private boolean lifeTransient;
    private PlayerEntity player;
    @Nullable
    private DamageSource deathCause;

    public RevivingDeathSuspender(PlayerEntity player) {
        this.player = player;
    }

    @Override
    public void suspendDeath(DamageSource deathCause) {
        this.player.setHealth(1f);
        this.player.setInvulnerable(true);
        this.player.abilities.invulnerable = true;
        this.deathCause = deathCause;
        this.lifeTransient = true;
    }

    @Override
    public boolean isLifeTransient() {
        return this.lifeTransient;
    }

    @Override
    public void setLifeTransient(boolean lifeTransient) {
        this.lifeTransient = true;
    }

    @Override
    public void resumeDeath() {
        this.player.setInvulnerable(false);
        this.player.abilities.invulnerable = false;
        this.player.setHealth(0f);
        this.player.onDeath(this.deathCause);
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag.putBoolean("lifeTransient", this.lifeTransient);
        if (this.deathCause != null) {
            tag.put("deathCause", DamageSourceSerialization.toTag(this.deathCause));
        }
        return tag;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        this.lifeTransient = tag.getBoolean("lifeTransient");
        if (tag.containsKey("deathCause") && this.player.world.isClient) {
            this.deathCause = DamageSourceSerialization.fromTag(tag.getCompound("deathCause"), (ServerWorld)this.player.world);
        }
    }
}
