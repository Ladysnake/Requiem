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
package ladysnake.requiem.client;

import com.mojang.blaze3d.systems.RenderSystem;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.block.VagrantTargetableBlock;
import ladysnake.requiem.api.v1.entity.ability.AbilityType;
import ladysnake.requiem.api.v1.event.minecraft.client.CrosshairRenderCallback;
import ladysnake.requiem.api.v1.event.minecraft.client.UpdateTargetedEntityCallback;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import ladysnake.requiem.core.ability.PlayerAbilityController;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class RequiemTargetHandler implements UpdateTargetedEntityCallback, CrosshairRenderCallback {
    private final MinecraftClient client = MinecraftClient.getInstance();

    void registerCallbacks() {
        CrosshairRenderCallback.EVENT.register(Requiem.id("target_handler"), this);
        UpdateTargetedEntityCallback.EVENT.register(this);
    }

    @Override
    public void updateTargetedEntity(float tickDelta) {
        if (this.client.player == null) return;
        PlayerAbilityController abilityController = PlayerAbilityController.get(this.client.player);

        Entity entity = this.client.getCameraEntity();
        assert entity != null;

        AbilityType[] abilityTypes = abilityController.getSortedAbilities();
        double maxRange = abilityController.getRange(abilityTypes[abilityTypes.length - 1]);

        HitResult blockResult = entity.raycast(maxRange, tickDelta, false);
        Vec3d startPoint = entity.getCameraPosVec(tickDelta);
        double distanceToBlockSq = blockResult != null ? blockResult.getPos().squaredDistanceTo(startPoint) : Double.POSITIVE_INFINITY;
        Vec3d rotationVec = entity.getRotationVec(1.0F);

        abilityController.clearTargets();

        for (int i = 0; i < abilityTypes.length; i++) {
            AbilityType k = abilityTypes[i];

            double range = abilityController.getRange(k);
            double effectiveRangeSq = Math.min(range * range, distanceToBlockSq);

            Vec3d endPoint = startPoint.add(rotationVec.x * range, rotationVec.y * range, rotationVec.z * range);
            Box box = entity.getBoundingBox().stretch(rotationVec.multiply(range)).expand(1.0D, 1.0D, 1.0D);
            EntityHitResult entityHitResult = ProjectileUtil.raycast(
                entity,
                startPoint,
                endPoint,
                box,
                ((GameRendererAccessor) client.gameRenderer)::requiem$isEligibleForTargeting,
                effectiveRangeSq
            );

            if (entityHitResult != null) {
                Entity hitEntity = entityHitResult.getEntity();
                Vec3d hitPos = entityHitResult.getPos();
                double distanceToHitSq = startPoint.squaredDistanceTo(hitPos);

                if (distanceToHitSq < effectiveRangeSq || blockResult == null) {
                    // Every target after this one must have a higher range, so they will target the same entity
                    for (; i < abilityTypes.length; i++) {
                        AbilityType eligibleAbility = abilityTypes[i];
                        abilityController.tryTarget(eligibleAbility, hitEntity);
                    }
                }
            }
        }
    }

    @Override
    public void onCrosshairRender(MatrixStack matrices, int scaledWidth, int scaledHeight) {
        if (this.client.player != null && RemnantComponent.isVagrant(this.client.player) && this.client.currentScreen == null) {
            PlayerAbilityController abilityController = PlayerAbilityController.get(this.client.player);
            AbilityType renderedType = AbilityType.ATTACK;

            float f = abilityController.getCooldownProgress(renderedType);

            if (abilityController.getTargetedEntity(renderedType) == null
                && this.client.crosshairTarget instanceof BlockHitResult bhr) {
                VagrantTargetableBlock targetable = VagrantTargetableBlock.LOOKUP.find(this.client.world, bhr.getBlockPos(), null);
                if (targetable != null) {
                    drawCrosshairIcon(matrices, scaledWidth, scaledHeight, targetable.getTargetedIcon(), targetable.canBeUsedByVagrant(client.player) ? 1 : 0);
                    return;
                }
            }

            if (f < 1 || abilityController.getTargetedEntity(renderedType) != null) {
                drawCrosshairIcon(matrices, scaledWidth, scaledHeight, abilityController.getIconTexture(renderedType), f);
            }
        }
    }

    static void drawCrosshairIcon(MatrixStack matrices, int scaledWidth, int scaledHeight, Identifier abilityIcon, float progress) {
        int x = (scaledWidth - 32) / 2 + 8;
        int y = (scaledHeight - 16) / 2 + 16;
        RenderSystem.setShaderTexture(0, abilityIcon);
        int height = (int)(progress * 8.0F);
        DrawableHelper.drawTexture(matrices, x, y, 16, 8, 0, 0, 16, 8, 16, 16);
        DrawableHelper.drawTexture(matrices, x, y + 8 - height, 16, height, 0, 16 - height, 16, height, 16, 16);
        RenderSystem.setShaderTexture(0, DrawableHelper.GUI_ICONS_TEXTURE);
    }
}
