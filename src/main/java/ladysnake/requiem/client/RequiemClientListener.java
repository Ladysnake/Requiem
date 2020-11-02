package ladysnake.requiem.client;

import com.mojang.blaze3d.systems.RenderSystem;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.dialogue.DialogueTracker;
import ladysnake.requiem.api.v1.event.minecraft.ItemTooltipCallback;
import ladysnake.requiem.api.v1.event.minecraft.client.CrosshairRenderCallback;
import ladysnake.requiem.api.v1.event.minecraft.client.HotbarRenderCallback;
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
import net.minecraft.client.gui.DrawableHelper;
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
    HotbarRenderCallback,
    ItemTooltipCallback {

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
        // Prevents the hotbar from being rendered when the player cannot use items
        HotbarRenderCallback.EVENT.register(this);
        // Add custom tooltips to items when the player is possessing certain entities
        ItemTooltipCallback.EVENT.register(this);
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
    public ActionResult interact(PlayerEntity player, World world, Hand hand, Entity target, EntityHitResult hitPosition) {
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
                int x = (scaledWidth - 32) / 2 + 8;
                int y = (scaledHeight - 16) / 2 + 16;
                client.getTextureManager().bindTexture(POSSESSION_ICON);
                DrawableHelper.drawTexture(matrices, x, y, 16, 16, 0, 0, 16, 16, 16, 16);
                client.getTextureManager().bindTexture(DrawableHelper.GUI_ICONS_TEXTURE);
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
    public ActionResult onHotbarRender(MatrixStack matrices, float tickDelta) {
        MinecraftClient client = this.mc;
        assert client.player != null;
        if (!client.player.isCreative() && RemnantComponent.get(client.player).isSoul()) {
            Entity possessed = PossessionComponent.get(client.player).getPossessedEntity();
            if (possessed == null || !RequiemEntityTypeTags.ITEM_USER.contains(possessed.getType())) {
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.PASS;
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
