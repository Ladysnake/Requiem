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

import com.mojang.blaze3d.systems.RenderSystem;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.dialogue.DialogueTracker;
import ladysnake.requiem.api.v1.entity.InventoryPart;
import ladysnake.requiem.api.v1.event.minecraft.ItemTooltipCallback;
import ladysnake.requiem.api.v1.event.minecraft.client.CrosshairRenderCallback;
import ladysnake.requiem.api.v1.event.requiem.InventoryLockingChangeCallback;
import ladysnake.requiem.api.v1.event.requiem.PossessionStateChangeCallback;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.api.v1.remnant.DeathSuspender;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import ladysnake.requiem.client.gui.CutsceneDialogueScreen;
import ladysnake.requiem.common.sound.RequiemSoundEvents;
import ladysnake.requiem.common.tag.RequiemEntityTypeTags;
import ladysnake.requiem.common.tag.RequiemItemTags;
import ladysnake.satin.api.event.PickEntityShaderCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.util.math.MatrixStack;
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
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class RequiemClientListener implements
    ClientTickEvents.EndTick,
    PickEntityShaderCallback,
    UseEntityCallback,
    ItemTooltipCallback,
    InventoryLockingChangeCallback {

    private static final Identifier POSSESSION_ICON = Requiem.id("textures/gui/possession_icon.png");

    private final RequiemClient rc;
    private final MinecraftClient mc = MinecraftClient.getInstance();
    private int timeBeforeDialogueGui;

    public RequiemClientListener(RequiemClient requiemClient) {
        this.rc = requiemClient;
    }

    void registerCallbacks() {
        ClientTickEvents.END_CLIENT_TICK.register(this);
        PickEntityShaderCallback.EVENT.register(this);
        // Start possession on right click
        UseEntityCallback.EVENT.register(this);
        // Draw a possession indicator under the crosshair
        CrosshairRenderCallback.EVENT.register(Requiem.id("possession_indicator"), this::drawPossessionIndicator);
        // Make the crosshair purple when able to teleport to the Overworld using an enderman
        CrosshairRenderCallback.EVENT.register(Requiem.id("enderman_color"), this::drawEnderCrosshair);
        // Update the visibility of the crafting book button
        InventoryLockingChangeCallback.EVENT.register(this);
        // Add custom tooltips to items when the player is possessing certain entities
        ItemTooltipCallback.EVENT.register(this);
        // Immovable mobs are a specific kind of boring, so we let players leave them regardless of their condition
        PossessionStateChangeCallback.EVENT.register((possessor, target) -> {
            if (possessor.world.isClient && target != null && RequiemEntityTypeTags.IMMOVABLE.contains(target.getType())) {
                this.mc.inGameHud.setOverlayMessage(new TranslatableText("requiem:shulker.onboard", FractureKeyBinding.etherealFractureKey.getBoundKeyLocalizedText()), false);
            }
        });
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
            MobEntity possessedEntity = PossessionComponent.get(client.player).getPossessedEntity();
            if (possessedEntity != null && possessedEntity.isOnFire()) {
                client.player.setOnFireFor(1);
            }
        }
    }

    @Override
    public void pickEntityShader(@Nullable Entity camera, Consumer<Identifier> loadShaderFunc, Supplier<ShaderEffect> appliedShaderGetter) {
        if (camera != null) {
            Entity possessed = PossessionComponent.getPossessedEntity(camera);
            if (possessed != null) {
                this.mc.gameRenderer.onCameraEntitySet(possessed);
            }
        }
    }

    @Override
    public ActionResult interact(PlayerEntity player, World world, Hand hand, Entity target, @Nullable EntityHitResult hitPosition) {
        if (player == this.mc.getCameraEntity() && RemnantComponent.get(player).isIncorporeal()) {
            if (target instanceof MobEntity && target.world.isClient) {
                target.world.playSound(player, target.getX(), target.getY(), target.getZ(), RequiemSoundEvents.EFFECT_POSSESSION_ATTEMPT, SoundCategory.PLAYERS, 2, 0.6f);
                this.rc.getRequiemFxRenderer().beginFishEyeAnimation(target);
            }
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    public void drawPossessionIndicator(MatrixStack matrices, int scaledWidth, int scaledHeight) {
        MinecraftClient client = this.mc;
        assert client.player != null;
        if (RemnantComponent.get(client.player).isIncorporeal()) {
            if (client.targetedEntity instanceof MobEntity) {
                RequiemTargetHandler.drawCrosshairIcon(client.getTextureManager(), matrices, scaledWidth, scaledHeight, POSSESSION_ICON);
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
    public void onInventoryLockingChange(PlayerEntity player, InventoryPart part, boolean locked) {
        if (part == InventoryPart.CRAFTING && player == this.mc.player) {
            if (this.mc.currentScreen instanceof InventoryScreenAccessor) {
                ((InventoryScreenAccessor) this.mc.currentScreen).requiem_getRecipeBookButton().visible = !locked;
            }
        }
    }

    @Override
    public void onTooltipBuilt(ItemStack item, @Nullable PlayerEntity player, @SuppressWarnings("unused") TooltipContext context, List<Text> lines) {
        if (player != null) {
            LivingEntity possessed = PossessionComponent.get(player).getPossessedEntity();
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
            lines.add(new TranslatableText(key).styled(style -> style.withColor(Formatting.DARK_GRAY)));
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
