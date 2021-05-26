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
package ladysnake.requiem.client;

import com.mojang.blaze3d.systems.RenderSystem;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.dialogue.DialogueTracker;
import ladysnake.requiem.api.v1.event.minecraft.ItemTooltipCallback;
import ladysnake.requiem.api.v1.event.minecraft.client.ApplyCameraTransformsCallback;
import ladysnake.requiem.api.v1.event.minecraft.client.CrosshairRenderCallback;
import ladysnake.requiem.api.v1.event.requiem.PossessionStateChangeCallback;
import ladysnake.requiem.api.v1.event.requiem.client.RenderSelfPossessedEntityCallback;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.api.v1.remnant.DeathSuspender;
import ladysnake.requiem.client.gui.CutsceneDialogueScreen;
import ladysnake.requiem.client.particle.GhostParticle;
import ladysnake.requiem.common.impl.possession.item.PossessionItemOverrideWrapper;
import ladysnake.requiem.common.tag.RequiemEntityTypeTags;
import ladysnake.requiem.common.util.ItemUtil;
import ladysnake.satin.api.event.ShaderEffectRenderCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.CrossbowUser;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.DrownedEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.GuardianEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MilkBucketItem;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.item.TridentItem;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public final class RequiemClientListener implements
    ClientTickEvents.EndTick,
    ItemTooltipCallback {

    public static boolean skipNextGuardian =  false;

    private final RequiemClient rc;
    private final MinecraftClient mc = MinecraftClient.getInstance();
    private int timeBeforeDialogueGui;

    public RequiemClientListener(RequiemClient requiemClient) {
        this.rc = requiemClient;
    }

    void registerCallbacks() {
        ClientTickEvents.END_CLIENT_TICK.register(this);
        // Make the crosshair purple when able to teleport to the Overworld using an enderman
        CrosshairRenderCallback.EVENT.register(Requiem.id("enderman_color"), this::drawEnderCrosshair);
        // Add custom tooltips to items when the player is possessing certain entities
        ItemTooltipCallback.EVENT.register(this);
        PossessionStateChangeCallback.EVENT.register((possessor, target) -> {
            if (possessor.world.isClient && target != null) {
                if (RequiemEntityTypeTags.IMMOVABLE.contains(target.getType())) {
                    this.mc.inGameHud.setOverlayMessage(new TranslatableText("requiem:shulker.onboard", mc.options.keySneak.getBoundKeyLocalizedText(), FractureKeyBinding.etherealFractureKey.getBoundKeyLocalizedText()), false);
                } else if (RequiemEntityTypeTags.FRICTIONLESS_HOSTS.contains(target.getType())) {
                    this.mc.inGameHud.setOverlayMessage(new TranslatableText("requiem:dissociate_hint", FractureKeyBinding.etherealFractureKey.getBoundKeyLocalizedText()), false);
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
    }

    @Override
    public void onEndTick(MinecraftClient client) {
        if (client.player != null && client.currentScreen == null) {
            if (DeathSuspender.get(client.player).isLifeTransient()) {
                if (--timeBeforeDialogueGui == 0) {
                    DialogueTracker dialogueTracker = DialogueTracker.get(client.player);
                    dialogueTracker.startDialogue(Requiem.id("remnant_choice"));
                    client.openScreen(new CutsceneDialogueScreen(new TranslatableText("requiem:dialogue_screen"), dialogueTracker.getCurrentDialogue(), this.rc.getWorldFreezeFxRenderer()));
                } else if (timeBeforeDialogueGui < 0) {
                    timeBeforeDialogueGui = 20;
                }
            }
        }
    }

    public void drawEnderCrosshair(MatrixStack matrices, int scaledWidth, int scaledHeight) {
        MinecraftClient client = this.mc;
        assert client.player != null;
        if (client.targetedEntity instanceof EndermanEntity && client.player.world.getRegistryKey() == World.END) {
            // TODO probably replace with a proper texture
            RenderSystem.color3f(0.4f, 0.0f, 1.0f);
        }
    }

    @Override
    public void onTooltipBuilt(ItemStack item, @Nullable PlayerEntity player, @SuppressWarnings("unused") TooltipContext context, List<Text> lines) {
        if (player != null) {
            MobEntity possessed = PossessionComponent.get(player).getPossessedEntity();
            if (possessed == null) {
                return;
            }

            lines.addAll(PossessionItemOverrideWrapper.buildTooltip(player.world, player, possessed, item));

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
            lines.add(new TranslatableText(key).styled(style -> style.withColor(Formatting.DARK_GRAY)));
        }
    }

}
