/*
 * Requiem
 * Copyright (C) 2017-2022 Ladysnake
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses>.
 *
 * Linking this mod statically or dynamically with other
 * modules is making a combined work based on this mod.
 * Thus, the terms and conditions of the GNU General Public License cover the whole combination.
 *
 * In addition, as a special exception, the copyright holders of
 * this mod give you permission to combine this mod
 * with free software programs or libraries that are released under the GNU LGPL
 * and with code included in the standard release of Minecraft under All Rights Reserved (or
 * modified versions of such code, with unchanged license).
 * You may copy and distribute such a system following the terms of the GNU GPL for this mod
 * and the licenses of the other code concerned.
 *
 * Note that people who make modified versions of this mod are not obligated to grant
 * this special exception for their modified versions; it is their choice whether to do so.
 * The GNU General Public License gives permission to release a modified version without this exception;
 * this exception also makes it possible to release a modified version which carries forward this exception.
 */
package ladysnake.requiem.common.entity;

import ladysnake.requiem.common.block.InertRunestoneBlock;
import ladysnake.requiem.common.particle.WispTrailParticleEffect;
import ladysnake.requiem.common.sound.RequiemSoundEvents;
import ladysnake.requiem.core.movement.PlayerMovementAlterer;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ObeliskSoulEntity extends SoulEntity {
    public static final TrackedData<Boolean> PHASING = DataTracker.registerData(ObeliskSoulEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    private @Nullable BlockPos targetPos;
    private int ticksAgainstWall = 0;

    public ObeliskSoulEntity(EntityType<? extends ObeliskSoulEntity> type, World world) {
        this(type, world, null);
    }

    public ObeliskSoulEntity(EntityType<? extends ObeliskSoulEntity> type, World world, @Nullable BlockPos targetPos) {
        super(type, world);
        this.targetPos = targetPos;
        this.targetChangeCooldown = 20;
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.getDataTracker().startTracking(PHASING, false);
    }

    public boolean isPhasing() {
        return this.getDataTracker().get(PHASING);
    }

    public void setPhasing(boolean phasing) {
        this.getDataTracker().set(PHASING, phasing);
    }

    @Override
    public void tick() {
        this.noClip = this.isPhasing() || this.age >= this.maxAge;

        if (!world.isClient) {
            if (this.targetPos != null && this.isTargetStale(targetPos)) {
                this.setMaxAge(60);
                this.targetPos = null;
            }
            if (isActivatedRunestone(this.getBlockStateAtPos())) {
                this.world.sendEntityStatus(this, SOUL_EXPIRED_STATUS);
                this.discard();
            }
        }
        super.tick();
        if (!this.world.isClient()) {
            if (this.prevX == this.getX() && this.prevY == this.getY() && this.prevZ == this.getZ()) {
                this.ticksAgainstWall++;
                if (this.ticksAgainstWall > PlayerMovementAlterer.TICKS_BEFORE_PHASING) {
                    this.setPhasing(true);
                }
            } else if (this.world.isSpaceEmpty(this)) {
                this.setPhasing(false);
                this.ticksAgainstWall = 0;
            }
        }
    }

    @Override
    protected void tickTimeInSolid() {
        if (this.targetPos == null) {
            super.tickTimeInSolid();
        }
    }

    @Override
    protected void spawnTrailParticle() {
        float redEvolution = -0.05f * (1f - this.getConversionProgress());
        float greenEvolution = -0.06f * this.getConversionProgress();
        float blueEvolution = 0.0f;
        this.world.addParticle(new WispTrailParticleEffect(1.0f, 1.0f, 1.0f, redEvolution, greenEvolution, blueEvolution), this.getX() + random.nextGaussian() / 15, this.getY() + random.nextGaussian() / 15, this.getZ() + random.nextGaussian() / 15, 0, 0.2d, 0);
    }

    public float getConversionProgress() {
        float conversionTime = 40f;
        return Math.min(1f, this.age / conversionTime);
    }

    @Override
    protected SoundEvent getDisintegrationSound() {
        return RequiemSoundEvents.ENTITY_OBELISK_SOUL_DISINTEGRATES;
    }

    @Override
    protected float getSpeedModifier() {
        return (this.isPhasing() ? 0.4f : 1) * super.getSpeedModifier();
    }

    @Override
    protected float getMaxSpeedModifier() {
        return 0.6f;
    }

    @Override
    protected Vec3d selectNextTarget() {
        if (this.targetPos != null) {
            return this.selectPosAround(Vec3d.ofCenter(targetPos));
        } else {
            return super.selectNextTarget();
        }
    }

    @Override
    protected void expire() {
        // obelisk souls head straight to the core instead of disappearing instantly
        if (this.targetPos == null) {
            super.expire();
        }
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        if (nbt.contains("target_pos", NbtElement.COMPOUND_TYPE)) {
            this.targetPos = NbtHelper.toBlockPos(nbt.getCompound("target_pos"));
        }
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        if (this.targetPos != null) {
            nbt.put("target_pos", NbtHelper.fromBlockPos(this.targetPos));
        }
    }

    private boolean isTargetStale(BlockPos targetPos) {
        if (world.isChunkLoaded(targetPos)) {
            return !isActivatedRunestone(world.getBlockState(targetPos));
        }
        return true;
    }

    private static boolean isActivatedRunestone(BlockState blockState) {
        return blockState.getBlock() instanceof InertRunestoneBlock && blockState.get(InertRunestoneBlock.ACTIVATED);
    }

    protected Vec3d selectPosAround(Vec3d targetPos) {
        if (this.age >= this.maxAge) return targetPos;

        float convergenceFactor = Math.max(1, MathHelper.sqrt(this.targetChanges / 3f));
        float wanderingRange = 15f;
        double fuzzyFactor = wanderingRange / convergenceFactor;
        return targetPos.add(
            random.nextGaussian() * fuzzyFactor,
            random.nextGaussian() * fuzzyFactor,
            random.nextGaussian() * fuzzyFactor
        );
    }
}
