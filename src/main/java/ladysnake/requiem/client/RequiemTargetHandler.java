/*
 * Requiem
 * Copyright (C) 2017-2020 Ladysnake
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

import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.entity.ability.AbilityType;
import ladysnake.requiem.api.v1.entity.ability.MobAbilityController;
import ladysnake.requiem.api.v1.event.minecraft.client.CrosshairRenderCallback;
import ladysnake.requiem.api.v1.event.minecraft.client.UpdateTargetedEntityCallback;
import ladysnake.requiem.api.v1.event.requiem.PossessionStateChangeCallback;
import ladysnake.requiem.common.network.RequiemNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;

public final class RequiemTargetHandler implements UpdateTargetedEntityCallback, PossessionStateChangeCallback, CrosshairRenderCallback {
    private static final Identifier ABILITY_ICON = Requiem.id("textures/gui/possession_icon.png");

    private final MinecraftClient client = MinecraftClient.getInstance();

    private final EnumMap<AbilityType, WeakReference<Entity>> targets = new EnumMap<>(AbilityType.class);
    private AbilityType[] sortedAbilities = AbilityType.values();
    private @Nullable MobAbilityController abilityController;

    void registerCallbacks() {
        CrosshairRenderCallback.EVENT.register(Requiem.id("target_handler"), this);
        UpdateTargetedEntityCallback.EVENT.register(this);
        PossessionStateChangeCallback.EVENT.register(this);
    }

    public boolean useDirectAbility(AbilityType type) {
        Entity targetedEntity = this.getTargetedEntity(type);
        if (targetedEntity != null) {
            assert this.abilityController != null : "A target was found but no ability should be active";

            if (abilityController.useDirect(type, targetedEntity)) {
                RequiemNetworking.sendAbilityUseMessage(type, targetedEntity);
                return true;
            }
        }
        return false;
    }

    public @Nullable Entity getTargetedEntity(AbilityType type) {
        WeakReference<Entity> ref = targets.get(type);
        return ref == null ? null : ref.get();
    }

    @Override
    public void onPossessionStateChange(PlayerEntity player, @Nullable MobEntity possessed) {
        if (player == client.player) {
            setAbilities(MobAbilityController.KEY.maybeGet(possessed).orElse(null));
        }
    }

    public void setAbilities(@Nullable MobAbilityController c) {
        this.targets.clear();

        if (c != null) {
            AbilityType[] a = this.sortedAbilities.clone();
            Arrays.sort(a, Comparator.comparingDouble(c::getRange));
            this.sortedAbilities = a;
        }

        this.abilityController = c;
    }

    @Override
    public void updateTargetedEntity(float tickDelta) {
        if (this.abilityController == null) return;
        MobAbilityController abilityController = this.abilityController;

        Entity entity = this.client.getCameraEntity();
        assert entity != null;

        AbilityType[] abilityTypes = this.sortedAbilities;
        double maxRange = abilityController.getRange(abilityTypes[abilityTypes.length - 1]);

        HitResult blockResult = entity.raycast(maxRange, tickDelta, false);
        Vec3d startPoint = entity.getCameraPosVec(tickDelta);
        double distanceToBlockSq = blockResult != null ? blockResult.getPos().squaredDistanceTo(startPoint) : Double.POSITIVE_INFINITY;
        Vec3d rotationVec = entity.getRotationVec(1.0F);

        this.targets.clear();

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
                ((GameRendererAccessor) client.gameRenderer)::requiem_isEligibleForTargeting,
                effectiveRangeSq
            );

            if (entityHitResult != null) {
                Entity hitEntity = entityHitResult.getEntity();
                Vec3d hitPos = entityHitResult.getPos();
                double distanceToHitSq = startPoint.squaredDistanceTo(hitPos);

                if (distanceToHitSq < effectiveRangeSq || blockResult == null) {
                    WeakReference<Entity> ref = new WeakReference<>(hitEntity);
                    // Every target after this one must have a higher range, so they will target the same entity
                    for (; i < abilityTypes.length; i++) {
                        AbilityType eligibleAbility = abilityTypes[i];

                        if (abilityController.canTarget(eligibleAbility, hitEntity)) {
                            targets.put(eligibleAbility, ref);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onCrosshairRender(MatrixStack matrices, int scaledWidth, int scaledHeight) {
        if (this.getTargetedEntity(AbilityType.ATTACK) instanceof MobEntity) {
            drawCrosshairIcon(client.getTextureManager(), matrices, scaledWidth, scaledHeight, ABILITY_ICON);
        }
    }

    static void drawCrosshairIcon(TextureManager textureManager, MatrixStack matrices, int scaledWidth, int scaledHeight, Identifier abilityIcon) {
        int x = (scaledWidth - 32) / 2 + 8;
        int y = (scaledHeight - 16) / 2 + 16;
        textureManager.bindTexture(abilityIcon);
        DrawableHelper.drawTexture(matrices, x, y, 16, 16, 0, 0, 16, 16, 16, 16);
        textureManager.bindTexture(DrawableHelper.GUI_ICONS_TEXTURE);
    }
}
