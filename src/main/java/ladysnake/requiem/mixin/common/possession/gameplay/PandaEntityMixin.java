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
package ladysnake.requiem.mixin.common.possession.gameplay;

import ladysnake.requiem.api.v1.possession.Possessable;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PandaEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PandaEntity.class)
public abstract class PandaEntityMixin extends AnimalEntity implements Possessable {
    protected PandaEntityMixin(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    @Shadow
    protected abstract boolean canEat(ItemStack stack);

    @Shadow
    public abstract void setSitting(boolean scared);

    @Shadow
    public abstract boolean isEating();

    @Shadow
    public abstract boolean isSitting();

    @Shadow
    private float sittingAnimationProgress;

    @Shadow
    private float lastSittingAnimationProgress;

    @Shadow
    public abstract void setLyingOnBack(boolean lyingOnBack);

    @ModifyArg(method = "updateEatingAnimation", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/PandaEntity;setEating(Z)V", ordinal = 0))
    private boolean stopEatingConcrete(boolean eat) {
        if (!this.canEat(this.getEquippedStack(EquipmentSlot.MAINHAND))) {
            return false;
        }
        return eat;
    }

    @Override
    public void onPossessorSet(@Nullable PlayerEntity possessor) {
        this.setLyingOnBack(false);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/PandaEntity;updateSittingAnimation()V"))
    private void scareIfSneaking(CallbackInfo ci) {
        PlayerEntity possessor = this.getPossessor();
        if (possessor != null) {
            this.setSitting(possessor.isSneaking());
        }
    }

    @Inject(method = "updateSittingAnimation", at = @At("RETURN"))
    private void recalculateDimensions(CallbackInfo ci) {
        if (this.lastSittingAnimationProgress != this.sittingAnimationProgress) {
            this.calculateDimensions();
        }
    }

    @Intrinsic  // If someone else redefines this method, just let them do whatever
    @Override
    public EntityDimensions getDimensions(EntityPose pose) {
        return super.getDimensions(pose).scaled(1, 1 + this.sittingAnimationProgress * 0.85f);
    }
}
