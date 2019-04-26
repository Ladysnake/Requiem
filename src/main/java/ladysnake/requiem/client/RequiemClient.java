/*
 * Requiem
 * Copyright (C) 2019 Ladysnake
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
 */
package ladysnake.requiem.client;

import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.annotation.CalledThroughReflection;
import ladysnake.requiem.api.v1.event.minecraft.ItemTooltipCallback;
import ladysnake.requiem.api.v1.event.minecraft.client.HotbarRenderCallback;
import ladysnake.requiem.client.network.ClientMessageHandling;
import ladysnake.requiem.common.tag.RequiemEntityTags;
import ladysnake.satin.api.event.PickEntityShaderCallback;
import ladysnake.satin.api.experimental.ReadableDepthFramebuffer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.BowItem;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Style;
import net.minecraft.text.TextFormat;
import net.minecraft.text.TextFormatter;
import net.minecraft.text.TranslatableTextComponent;
import net.minecraft.util.ActionResult;

@CalledThroughReflection
public class RequiemClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientMessageHandling.init();
        registerCallbacks();
    }

    private void registerCallbacks() {
        ReadableDepthFramebuffer.useFeature();
        RequiemFx.INSTANCE.registerCallbacks();
        ShadowPlayerFx.INSTANCE.registerCallbacks();

        PickEntityShaderCallback.EVENT.register((camera, loadShaderFunc, appliedShaderGetter) -> {
            if (camera instanceof RequiemPlayer) {
                Entity possessed = ((RequiemPlayer)camera).getPossessionComponent().getPossessedEntity();
                if (possessed != null) {
                    MinecraftClient.getInstance().gameRenderer.onCameraEntitySet(possessed);
                }
            }
        });
        // Start possession on right click
        UseEntityCallback.EVENT.register((player, world, hand, target, hitPosition) -> {
            if (player == MinecraftClient.getInstance().cameraEntity && ((RequiemPlayer) player).getRemnantState().isIncorporeal()) {
                if (target instanceof MobEntity && target.world.isClient) {
                    target.world.playSound(player, target.x, target.y, target.z, SoundEvents.ENTITY_VEX_AMBIENT, SoundCategory.PLAYERS, 2, 0.6f);
                    RequiemFx.INSTANCE.beginFishEyeAnimation(target);
                }
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        });
        // Prevents the hotbar from being rendered when the player cannot use items
        HotbarRenderCallback.EVENT.register(tickDelta -> {
            MinecraftClient client = MinecraftClient.getInstance();
            RequiemPlayer player = (RequiemPlayer) client.player;
            if (!client.player.isCreative() && player.getRemnantState().isSoul()) {
                Entity possessed = player.getPossessionComponent().getPossessedEntity();
                if (possessed == null || !RequiemEntityTags.ITEM_USER.contains(possessed.getType())) {
                    return ActionResult.SUCCESS;
                }
            }
            return ActionResult.PASS;
        });
        // Add custom tooltips to items when the player is possessing certain entities
        ItemTooltipCallback.EVENT.register((item, player, context, lines) -> {
            if (player != null) {
                LivingEntity possessed = ((RequiemPlayer)player).getPossessionComponent().getPossessedEntity();
                if (possessed instanceof AbstractSkeletonEntity && item.getItem() instanceof BowItem) {
                    lines.add(TextFormatter.style(
                            new TranslatableTextComponent("requiem:tooltip.skeletal_efficiency"),
                            new Style().setColor(TextFormat.DARK_GRAY)
                    ));
                }
            }
        });
    }
}
