/*
 * Requiem
 * Copyright (C) 2017-2021 Ladysnake
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
package ladysnake.pandemonium.common.entity.fakeplayer;

import ladysnake.pandemonium.common.entity.PandemoniumEntities;
import ladysnake.requiem.common.entity.attribute.PossessionDelegatingModifier;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FakePlayerGuide extends PathAwareEntity {
    protected final LivingEntity owner;

    public FakePlayerGuide(LivingEntity owner) {
        this(PandemoniumEntities.FAKE_PLAYER_AI, owner);
    }

    public FakePlayerGuide(EntityType<? extends PathAwareEntity> type, LivingEntity owner) {
        super(type, owner.world);
        this.owner = owner;
        PossessionDelegatingModifier.replaceAttributes(this, () -> this.owner);
    }

    public void addGoal(int priority, Goal goal) {
        this.goalSelector.add(priority, goal);
    }

    public void addTargetGoal(int priority, Goal goal) {
        this.targetSelector.add(priority, goal);
    }

    public void tickAi() {
        copyState(this.owner, this);
        this.despawnCounter = 0;
        this.setBoundingBox(this.owner.getBoundingBox());
        this.tickNewAi();
        this.owner.setSprinting(this.getTarget() != null);
        copyState(this, this.owner);
    }

    public LivingEntity getOwner() {
        return owner;
    }

    private static void copyState(LivingEntity from, LivingEntity to) {
        to.setPos(from.getX(), from.getY(), from.getZ());
        to.setVelocity(from.getVelocity());
        to.prevX = from.prevX;
        to.prevY = from.prevY;
        to.prevZ = from.prevZ;
        to.setOnGround(from.isOnGround());
        // TODO consider merging the following code with RequiemFx#setupRenderDelegate
        to.bodyYaw = from.bodyYaw;
        to.prevBodyYaw = from.prevBodyYaw;
        to.yaw = from.yaw;
        to.prevYaw = from.prevYaw;
        to.pitch = from.pitch;
        to.prevPitch = from.prevPitch;
        to.headYaw = from.headYaw;
        to.prevHeadYaw = from.prevHeadYaw;
    }

    @Override
    public boolean tryAttack(Entity target) {
        return this.owner.tryAttack(target);
    }

    @Override
    public float getPathfindingFavor(BlockPos pos, WorldView world) {
        return world.getBlockState(pos.down()).isOf(Blocks.GRASS_BLOCK) ? 10.0F : world.getBrightness(pos) - 0.5F;
    }

    @Override
    protected float turnHead(float bodyRotation, float headRotation) {
        return super.turnHead(bodyRotation, headRotation);
    }

    @Override
    public boolean isOnFire() {
        return this.owner.isOnFire();
    }

    @Override
    public Iterable<ItemStack> getItemsHand() {
        return this.owner.getItemsHand();
    }

    @Override
    public Iterable<ItemStack> getArmorItems() {
        return this.owner.getArmorItems();
    }

    @Override
    public ItemStack getEquippedStack(EquipmentSlot slot) {
        return this.owner.getEquippedStack(slot);
    }

    @Override
    public void setJumping(boolean jumping) {
        this.owner.setJumping(jumping);
    }

    @Override
    public List<Entity> getPassengerList() {
        return this.owner.getPassengerList();
    }

    @Override
    public void setMovementSpeed(float movementSpeed) {
        this.owner.setMovementSpeed(movementSpeed);
        this.setForwardSpeed(movementSpeed);
    }

    @Override
    public boolean canTarget(EntityType<?> type) {
        return this.owner.canTarget(type);
    }

    @Override
    public boolean canTarget(LivingEntity target) {
        return super.canTarget(target) && this.owner.canTarget(target);
    }

    @Override
    public int getLastAttackedTime() {
        return this.owner.getLastAttackedTime();
    }

    @Nullable
    @Override
    public LivingEntity getAttacker() {
        return this.owner.getAttacker();
    }

    @Override
    public float getMovementSpeed() {
        return this.owner.getMovementSpeed();
    }

    @Override
    public void setForwardSpeed(float forwardSpeed) {
        this.owner.forwardSpeed = forwardSpeed * 6;
    }

    @Override
    public void setUpwardSpeed(float upwardSpeed) {
        this.owner.upwardSpeed = upwardSpeed;
    }

    @Override
    public void setSidewaysSpeed(float sidewaysMovement) {
        this.owner.sidewaysSpeed = sidewaysMovement;
    }
}
