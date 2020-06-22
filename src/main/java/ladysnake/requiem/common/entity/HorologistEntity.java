package ladysnake.requiem.common.entity;

import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.common.RequiemComponents;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class HorologistEntity extends PassiveEntity implements Npc {
    public HorologistEntity(EntityType<? extends HorologistEntity> type, World world) {
        super(type, world);
    }

    @Override
    public net.minecraft.entity.EntityData initialize(IWorld world, LocalDifficulty difficulty, SpawnType spawnType, @Nullable net.minecraft.entity.EntityData entityData, @Nullable CompoundTag entityTag) {
        if (entityData == null) {
            entityData = new PassiveEntity.EntityData();
            ((PassiveEntity.EntityData)entityData).setBabyAllowed(false);
        }

        return super.initialize(world, difficulty, spawnType, entityData, entityTag);
    }

    @Override
    protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
        return this.isBaby() ? 0.81F : 1.62F;
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false;
    }

    @Nullable
    @Override
    public PassiveEntity createChild(PassiveEntity mate) {
        return null;
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(10, new LookAtEntityGoal(this, PlayerEntity.class, 32.0F));
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        ParticleEffect particleType = null;
        if (source.getAttacker() instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) source.getAttacker();
            this.swapPosition(attacker);
            attacker.damage(source, amount);
            particleType = ParticleTypes.SMOKE;
        } else if (source.isSourceCreativePlayer() || source.isOutOfWorld() || amount > 3F){
            RequiemComponents.HOROLOGIST_MANAGER.get(this.world.getLevelProperties()).freeHorologist();
            this.remove();
            particleType = ParticleTypes.LARGE_SMOKE;
        }
        if (particleType != null) {
            ((ServerWorld) this.world).spawnParticles(
                particleType,
                this.offsetX(0.5D), this.getHeight() * 0.5, this.offsetZ(0.5D),
                15,
                this.getWidth() * 0.5, this.getHeight() * 0.5, this.getWidth() * 0.5,
                0.2
            );
        }
        return false;
    }

    private void swapPosition(LivingEntity attacker) {
        Vec3d pos = attacker.getPos();
        float yaw = attacker.yaw;
        float pitch = attacker.pitch;
        attacker.copyPositionAndRotation(this);
        this.refreshPositionAndAngles(pos.x, pos.y, pos.z, yaw, pitch);
    }

    @Override
    public boolean interactMob(PlayerEntity player, Hand hand) {
        if (world.isClient) {
            RequiemPlayer.from(player).getDialogueTracker().startDialogue(Requiem.id("remnant_choice"));
            return true;
        }
        return super.interactMob(player, hand);
    }
}
