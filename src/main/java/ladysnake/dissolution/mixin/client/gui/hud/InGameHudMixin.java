package ladysnake.dissolution.mixin.client.gui.hud;

import com.mojang.blaze3d.platform.GlStateManager;
import ladysnake.dissolution.Dissolution;
import ladysnake.dissolution.api.DissolutionPlayer;
import ladysnake.dissolution.api.possession.Possessor;
import ladysnake.dissolution.api.remnant.RemnantHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(InGameHud.class)
public class InGameHudMixin extends Drawable {

    private static final Identifier DISSOLUTION_ECTOPLASM_ICONS = Dissolution.id("textures/gui/icons.png");
    @Shadow @Final private MinecraftClient client;

    @Shadow private long field_2032;

    @Shadow private int ticks;

    @Shadow @Final private Random random;

    @Shadow private int field_2033;

    @Shadow private int scaledHeight;

    @Inject(
            method = "method_1760",
            at = @At(value = "CONSTANT", args = "stringValue=health")
    )
    public void drawPossessionHud(CallbackInfo info) {
        RemnantHandler handler = ((DissolutionPlayer)client.player).getRemnantHandler();
        if (handler != null && handler.isSoul()) {
            int scaledWidth = this.client.window.getScaledWidth();
            int scaledHeight = this.client.window.getScaledHeight();

            LivingEntity possessed = (LivingEntity) ((Possessor) this.client.player).getPossessedEntity();
            if (possessed != null) {
                if (this.client.interactionManager.hasStatusBars()) {
                    if (possessed.getHealth() > 0) {
                        int textureRow = 0;
                        if (possessed instanceof PigZombieEntity) {
                            textureRow = 1;
                        } else if (possessed instanceof HuskEntity) {
                            textureRow = 2;
                        } else if (possessed instanceof WitherSkeletonEntity) {
                            textureRow = 4;
                        } else if (possessed instanceof StrayEntity) {
                            textureRow = 5;
                        } else if (possessed instanceof SkeletonEntity) {
                            textureRow = 3;
                        }
                        this.client.getTextureManager().bindTexture(DISSOLUTION_ECTOPLASM_ICONS);
                        this.drawPossessionHealthBar(possessed, scaledWidth, scaledHeight, textureRow);
                        this.client.getTextureManager().bindTexture(ICONS);
                    }
                }
                this.client.player.setBreath(possessed.getBreath());
            }
            GlStateManager.color4f(1, 1, 1, 0);
        }
    }

    @Inject(method = "method_1759", at = @At("HEAD"), cancellable = true)
    public void cancelHotBarRender(float tickDelta, CallbackInfo info) {
        RemnantHandler handler = ((DissolutionPlayer)client.player).getRemnantHandler();
        if (handler != null && handler.isSoul()) {
            info.cancel();
        }
    }

    @Inject(
            method = "method_1760",
            at = @At(value = "CONSTANT", args = "stringValue=air")
    )
    public void resumeDrawing(CallbackInfo info) {
        RemnantHandler handler = ((DissolutionPlayer)client.player).getRemnantHandler();
        if (handler != null && handler.isSoul()) {
            GlStateManager.color4f(1, 1, 1, 1);
        }
    }

    private void drawPossessionHealthBar(LivingEntity player, int width, int height, int textureRow) {
        int health = MathHelper.ceil(player.getHealth());
        boolean highlight = this.field_2032 > (long) this.ticks && (this.field_2032 - (long) this.ticks) / 3L % 2L == 1L;

        int healthLast = this.field_2033;

        EntityAttributeInstance attrMaxHealth = player.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        float healthMax = (float) attrMaxHealth.getValue();
        int absorb = MathHelper.ceil(client.player.getAbsorptionAmount());

        if (healthMax > 100) {
            drawShortenedPossessionHealthBar(health, absorb, width, height, textureRow);
            return;
        }

        int healthRows = MathHelper.ceil((healthMax + absorb) / 2.0F / 10.0F);
        int rowHeight = Math.max(10 - (healthRows - 2), 3);

        this.random.setSeed((long) (ticks * 312871));

        int left = width / 2 - 91;
        int top = scaledHeight - 39;

        int textureMargin = 0;
        int textureTop;
        if (absorb > 0) {
            textureMargin += 16;
            textureTop = 0;
            this.client.getTextureManager().bindTexture(ICONS);
        } else {
            textureTop = textureRow * 9;
            this.client.getTextureManager().bindTexture(DISSOLUTION_ECTOPLASM_ICONS);
        }
        int textureBackground = (highlight ? textureMargin + 9 : textureMargin);

        float absorbRemaining = absorb;

        for (int i = MathHelper.ceil((healthMax + absorb) / 2.0F) - 1; i >= 0; --i) {
            //int b0 = (highlight ? 1 : 0);
            int row = MathHelper.ceil((float) (i + 1) / 10.0F) - 1;
            int x = left + i % 10 * 8;
            int y = top - row * rowHeight;

            if (health <= 4) {
                y += random.nextInt(2);
            }

            drawTexturedRect(x, y, textureBackground, textureTop, 9, 9);

            if (highlight) {
                if (i * 2 + 1 < healthLast) {
                    drawTexturedRect(x, y, textureMargin + 54, textureTop, 9, 9); //6
                } else if (i * 2 + 1 == healthLast) {
                    drawTexturedRect(x, y, textureMargin + 63, textureTop, 9, 9); //7
                }
            }

            if (absorbRemaining > 0.0F) {
                if (absorbRemaining == absorb && absorb % 2.0F == 1.0F) {
                    drawTexturedRect(x, y, textureMargin + 153, textureTop, 9, 9); //17
                    absorbRemaining -= 1.0F;
                } else {
                    drawTexturedRect(x, y, textureMargin + 144, textureTop, 9, 9); //16
                    absorbRemaining -= 2.0F;
                }
                if (absorbRemaining <= 0.0F) {
                    this.client.getTextureManager().bindTexture(DISSOLUTION_ECTOPLASM_ICONS);
                    textureMargin -= 16;
                    textureTop = textureRow * 9;
                    textureBackground = (highlight ? textureMargin + 9 : textureMargin);
                }
            } else {
                if (i * 2 + 1 < health) {
                    drawTexturedRect(x, y, textureMargin + 36, textureTop, 9, 9); //4
                } else if (i * 2 + 1 == health) {
                    drawTexturedRect(x, y, textureMargin + 45, textureTop, 9, 9); //5
                }
            }
        }
    }

    private void drawShortenedPossessionHealthBar(int health, int absorb, int width, int height, int textureRow) {
        int left = width / 2 - 91;
        int top = scaledHeight - 39;
        client.getTextureManager().bindTexture(DISSOLUTION_ECTOPLASM_ICONS);
        drawTexturedRect(left, top, 0, textureRow * 9, 9, 9);
        drawTexturedRect(left, top, 36, textureRow * 9, 9, 9);
        left = client.fontRenderer.drawWithShadow("x" + health / 2, left + 9, top, 0xFFFFFF);
        if (absorb > 0) {
            client.getTextureManager().bindTexture(ICONS);
            drawTexturedRect(left + 11, top, 16, 0, 9, 9);
            drawTexturedRect(left + 11, top, 16 + 144, 0, 9, 9);
            client.fontRenderer.drawWithShadow("x" + absorb / 2, left + 11 + 9, top, 0xFFFFFF);
        }

    }

}
