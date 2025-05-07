/*
 * Requiem
 * Copyright (C) 2017-2024 Ladysnake
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

import baritone.api.fakeplayer.FakeClientPlayerEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.event.minecraft.ItemTooltipCallback;
import ladysnake.requiem.api.v1.event.minecraft.client.ApplyCameraTransformsCallback;
import ladysnake.requiem.api.v1.event.minecraft.client.CrosshairRenderCallback;
import ladysnake.requiem.api.v1.event.requiem.PossessionStateChangeCallback;
import ladysnake.requiem.api.v1.event.requiem.client.RenderSelfPossessedEntityCallback;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import ladysnake.requiem.client.particle.GhostParticle;
import ladysnake.requiem.client.screen.RiftScreen;
import ladysnake.requiem.common.entity.RequiemEntities;
import ladysnake.requiem.common.possession.item.PossessionItemOverrideWrapper;
import ladysnake.requiem.common.tag.RequiemEntityTypeTags;
import ladysnake.requiem.core.tag.RequiemCoreEntityTags;
import ladysnake.requiem.core.util.ItemUtil;
import ladysnake.satin.api.event.ShaderEffectRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.CrossbowUser;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.DrownedEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.GuardianEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.entity.mob.warden.WardenEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MilkBucketItem;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.item.TridentItem;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientTickEvents;

import javax.annotation.Nullable;
import java.util.List;

public final class RequiemClientListener implements
    ClientTickEvents.End,
    ItemTooltipCallback {

    private static boolean skipNextGuardian = false;

    public static boolean shouldSkipNextGuardian() {
        if (skipNextGuardian) {
            skipNextGuardian = false;
            return true;
        }
        return false;
    }

    private final RequiemClient rc;
    private final MinecraftClient mc = MinecraftClient.getInstance();

    public RequiemClientListener(RequiemClient requiemClient) {
        this.rc = requiemClient;
    }

    void registerCallbacks() {
        ClientTickEvents.END.register(this);
        // Make the crosshair purple when able to teleport to the Overworld using an enderman
        CrosshairRenderCallback.EVENT.register(Requiem.id("enderman_color"), this::drawEnderCrosshair);
        // Add custom tooltips to items when the player is possessing certain entities
        ItemTooltipCallback.EVENT.register(this);
        PossessionStateChangeCallback.EVENT.register((possessor, target) -> {
            if (possessor.getWorld().isClient && target != null) {
                if (target.getType().isIn(RequiemCoreEntityTags.IMMOVABLE)) {
                    this.mc.inGameHud.setOverlayMessage(Text.translatable("requiem:shulker.onboard", mc.options.sneakKey.getKeyName(), FractureKeyBinding.etherealFractureKey.getKeyName()), false);
                } else if (target.getType().isIn(RequiemCoreEntityTags.FRICTIONLESS_HOSTS)) {
                    this.mc.inGameHud.setOverlayMessage(Text.translatable("requiem:dissociate_hint", FractureKeyBinding.etherealFractureKey.getKeyName()), false);
                }
            }
        });
        ApplyCameraTransformsCallback.EVENT.register(new HeadDownTransformHandler());
        RenderSelfPossessedEntityCallback.EVENT.register(possessed -> {
            if (possessed instanceof ShulkerEntity) return true;
            if (possessed instanceof GuardianEntity) {
                skipNextGuardian = true;
                return true;
            }
            return false;
        });
        ShaderEffectRenderCallback.EVENT.register(GhostParticle::draw);
        MutableBoolean wasLookingAtShell = new MutableBoolean();
        ClientTickEvents.START.register(client -> {
            if (client.player != null && client.targetedEntity instanceof FakeClientPlayerEntity fp
                && fp.getType() == RequiemEntities.PLAYER_SHELL) {
                if (PossessionComponent.getHost(client.player) != null
                    && client.player.getUuid().equals(fp.getOwnerUuid())) {
                    client.inGameHud.setOverlayMessage(Text.translatable(
                        "requiem:merge_hint",
                        FractureKeyBinding.etherealFractureKey.getKeyName()
                    ), false);
                    wasLookingAtShell.setTrue();
                }
            } else if (wasLookingAtShell.booleanValue()) {
                client.inGameHud.setOverlayMessage(Text.empty(), false);
                wasLookingAtShell.setFalse();
            }
        });
        WorldRenderEvents.BEFORE_DEBUG_RENDER.register(context -> {
            if (mc.currentScreen instanceof RiftScreen riftScreen) {
                riftScreen.updateMatrices(context.matrixStack(), context.projectionMatrix());
            }
        });
    }

    @Override
    public void endClientTick(MinecraftClient client) {
        if (client.player != null) {
            MobEntity possessedEntity = PossessionComponent.get(client.player).getHost();

            if (possessedEntity != null && possessedEntity.getHealth() != client.player.getHealth()) {
                client.player.updateHealth(possessedEntity.getHealth());
                if (client.player.getHealth() <= 0) client.player.setHealth(1);
            }
        }
    }

    public void drawEnderCrosshair(GuiGraphics graphics, int scaledWidth, int scaledHeight) {
        MinecraftClient client = this.mc;
        assert client.player != null;
        if (RemnantComponent.isVagrant(client.player) && client.targetedEntity instanceof EndermanEntity && client.player.getWorld().getRegistryKey() == World.END) {
            // TODO probably replace with a proper texture
            RenderSystem.setShaderColor(0.4f, 0.0f, 1.0f, 1.0f);
        }
    }

    @Override
    public void onTooltipBuilt(ItemStack item, @Nullable PlayerEntity player, @SuppressWarnings("unused") TooltipContext context, List<Text> lines) {
        if (player != null) {
            MobEntity possessed = PossessionComponent.get(player).getHost();
            if (possessed == null) {
                return;
            }

            lines.addAll(PossessionItemOverrideWrapper.buildTooltip(player.getWorld(), player, possessed, item));

            String key;
            if (possessed.getType().isIn(RequiemEntityTypeTags.ARROW_GENERATORS) && item.getItem() instanceof RangedWeaponItem) {
                key = "requiem:tooltip.ammo_generator";
            } else if (possessed instanceof CrossbowUser && item.getItem() instanceof CrossbowItem) {
                key = "requiem:tooltip.bolt_hoarder";
            } else if (possessed instanceof AbstractSkeletonEntity && item.getItem() instanceof BowItem) {
                key = "requiem:tooltip.skeletal_efficiency";
            } else if (possessed instanceof AbstractSkeletonEntity && item.getItem() instanceof MilkBucketItem) {
                key = "requiem:tooltip.calcium_bucket";
            } else if (possessed instanceof DrownedEntity && item.getItem() instanceof TridentItem) {
                key = "requiem:tooltip.drowned_grip";
            } else if (possessed instanceof WitchEntity && ItemUtil.isWaterBottle(item)) {
                key = "requiem:tooltip.witch_brew_base";
            } else {
                return;
            }
            lines.add(Text.translatable(key).styled(style -> style.withColor(Formatting.DARK_GRAY)));
        }
    }

}
