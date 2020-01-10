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
 */
package ladysnake.requiem.client;

import com.mojang.blaze3d.systems.RenderSystem;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.annotation.CalledThroughReflection;
import ladysnake.requiem.api.v1.dialogue.DialogueTracker;
import ladysnake.requiem.api.v1.event.minecraft.ItemTooltipCallback;
import ladysnake.requiem.api.v1.event.minecraft.client.CrosshairRenderCallback;
import ladysnake.requiem.api.v1.event.minecraft.client.HotbarRenderCallback;
import ladysnake.requiem.client.gui.CutsceneDialogueScreen;
import ladysnake.requiem.client.network.ClientMessageHandling;
import ladysnake.requiem.common.sound.RequiemSoundEvents;
import ladysnake.requiem.common.tag.RequiemEntityTypeTags;
import ladysnake.requiem.common.tag.RequiemItemTags;
import ladysnake.satin.api.event.PickEntityShaderCallback;
import ladysnake.satin.api.experimental.ReadableDepthFramebuffer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.potion.PotionUtil;
import net.minecraft.sound.SoundCategory;
import net.minecraft.tag.ItemTags;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.dimension.DimensionType;

import javax.annotation.Nullable;
import java.util.List;

import static net.minecraft.client.gui.DrawableHelper.GUI_ICONS_LOCATION;

@CalledThroughReflection
public class RequiemClient implements ClientModInitializer {

    private static final Identifier POSSESSION_ICON = Requiem.id("textures/gui/possession_icon.png");

    @Override
    public void onInitializeClient() {
        ClientMessageHandling.init();
        registerCallbacks();
    }

    private void registerCallbacks() {
        ReadableDepthFramebuffer.useFeature();
        RequiemFx.INSTANCE.registerCallbacks();
        ShadowPlayerFx.INSTANCE.registerCallbacks();
        ZaWorldFx.INSTANCE.registerCallbacks();

        ClientTickCallback.EVENT.register(client -> {
            if (client.player != null && client.currentScreen == null) {
                if (((RequiemPlayer)client.player).getDeathSuspender().isLifeTransient()) {
                    DialogueTracker dialogueTracker = ((RequiemPlayer) client.player).getDialogueTracker();
                    dialogueTracker.startDialogue(Requiem.id("remnant_choice"));
                    client.openScreen(new CutsceneDialogueScreen(new TranslatableText("requiem:dialogue_screen"), dialogueTracker.getCurrentDialogue()));
                }
                MobEntity possessedEntity = ((RequiemPlayer) client.player).asPossessor().getPossessedEntity();
                if (possessedEntity != null && possessedEntity.isOnFire()) {
                    client.player.setOnFireFor(1);
                }
            }
        });
        PickEntityShaderCallback.EVENT.register((camera, loadShaderFunc, appliedShaderGetter) -> {
            if (camera instanceof RequiemPlayer) {
                Entity possessed = ((RequiemPlayer)camera).asPossessor().getPossessedEntity();
                if (possessed != null) {
                    MinecraftClient.getInstance().gameRenderer.onCameraEntitySet(possessed);
                }
            }
        });
        // Start possession on right click
        UseEntityCallback.EVENT.register((player, world, hand, target, hitPosition) -> {
            if (player == MinecraftClient.getInstance().cameraEntity && ((RequiemPlayer) player).asRemnant().isIncorporeal()) {
                if (target instanceof MobEntity && target.world.isClient) {
                    target.world.playSound(player, target.getX(), target.getY(), target.getZ(), RequiemSoundEvents.EFFECT_POSSESSION_ATTEMPT, SoundCategory.PLAYERS, 2, 0.6f);
                    RequiemFx.INSTANCE.beginFishEyeAnimation(target);
                }
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        });
        CrosshairRenderCallback.EVENT.register(Requiem.id("possession_indicator"), (scaledWidth, scaledHeight) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            assert client.player != null;
            if (((RequiemPlayer) client.player).asRemnant().isIncorporeal()) {
                if (client.targetedEntity instanceof MobEntity) {
                    int x = (scaledWidth - 32) / 2 + 8;
                    int y = (scaledHeight - 16) / 2 + 16;
                    RenderSystem.color3f(1.0f, 1.0f, 1.0f);
                    client.getTextureManager().bindTexture(POSSESSION_ICON);
                    DrawableHelper.blit(x, y, 16, 16, 0, 0, 16, 16, 16, 16);
                    client.getTextureManager().bindTexture(GUI_ICONS_LOCATION);
                }
            }
        });
        CrosshairRenderCallback.EVENT.register(Requiem.id("enderman_color"), (scaledWidth, scaledHeight) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            assert client.player != null;
            if (client.targetedEntity instanceof EndermanEntity && client.player.dimension == DimensionType.THE_END) {
                RenderSystem.color3f(0.4f, 0.0f, 1.0f);
            }
        });
        // Prevents the hotbar from being rendered when the player cannot use items
        HotbarRenderCallback.EVENT.register(tickDelta -> {
            MinecraftClient client = MinecraftClient.getInstance();
            assert client.player != null;
            RequiemPlayer player = (RequiemPlayer) client.player;
            if (!client.player.isCreative() && player.asRemnant().isSoul()) {
                Entity possessed = player.asPossessor().getPossessedEntity();
                if (possessed == null || !RequiemEntityTypeTags.ITEM_USER.contains(possessed.getType())) {
                    return ActionResult.SUCCESS;
                }
            }
            return ActionResult.PASS;
        });
        // Add custom tooltips to items when the player is possessing certain entities
        ItemTooltipCallback.EVENT.register(RequiemClient::addPossessionTooltip);
        // Register special icons for different levels of attrition
        ClientSpriteRegistryCallback.event(new Identifier("textures/atlas/mob_effects.png")).register((atlasTexture, registry) -> {
            for (int i = 1; i <= 4; i++) {
                registry.register(Requiem.id("mob_effect/attrition_" + i));
            }
        });
    }

    private static void addPossessionTooltip(ItemStack item, @Nullable PlayerEntity player, @SuppressWarnings("unused") TooltipContext context, List<Text> lines) {
        if (player != null) {
            LivingEntity possessed = ((RequiemPlayer) player).asPossessor().getPossessedEntity();
            if (possessed == null) {
                return;
            }
            String key;
            if (possessed instanceof AbstractSkeletonEntity && item.getItem() instanceof BowItem) {
                key = "requiem:tooltip.skeletal_efficiency";
            } else if (possessed instanceof AbstractSkeletonEntity && RequiemItemTags.BONES.contains(item.getItem())) {
                key = "requiem:tooltip.bony_prosthesis";
            } else if (possessed instanceof AbstractSkeletonEntity && item.getItem() instanceof MilkBucketItem) {
                key = "requiem:tooltip.calcium_bucket";
            } else if (possessed instanceof DrownedEntity && item.getItem() instanceof TridentItem) {
                key = "requiem:tooltip.drowned_grip";
            } else if (possessed instanceof ZombieEntity && RequiemItemTags.RAW_MEATS.contains(item.getItem())) {
                key = "requiem:tooltip.zombie_food";
            } else if (possessed instanceof DrownedEntity && ItemTags.FISHES.contains(item.getItem())) {
                key = "requiem:tooltip.drowned_food";
            } else if (possessed.isUndead() && item.getItem() == Items.GOLDEN_APPLE) {
                key = "requiem:tooltip.cure_reagent";
            } else if (possessed.isUndead() && item.getItem() == Items.POTION && isWeaknessPotion(item)) {
                key = "requiem:tooltip.cure_catalyst";
            } else {
                return;
            }
            lines.add(Texts.setStyleIfAbsent(
                    new TranslatableText(key),
                    new Style().setColor(Formatting.DARK_GRAY)
            ));
        }
    }

    private static boolean isWeaknessPotion(ItemStack stack) {
        for (StatusEffectInstance effect : PotionUtil.getPotionEffects(stack)) {
            if (effect.getEffectType() == StatusEffects.WEAKNESS) {
                return true;
            }
        }
        return false;
    }
}
