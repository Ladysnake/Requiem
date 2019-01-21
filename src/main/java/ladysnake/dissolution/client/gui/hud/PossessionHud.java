package ladysnake.dissolution.client.gui.hud;

import ladysnake.dissolution.Dissolution;
import ladysnake.dissolution.api.v1.DissolutionPlayer;
import ladysnake.dissolution.api.v1.remnant.RemnantHandler;
import ladysnake.dissolution.common.tag.DissolutionEntityTags;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.*;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.Random;

public class PossessionHud extends Drawable {
    public static final Identifier DISSOLUTION_ECTOPLASM_ICONS = Dissolution.id("textures/gui/icons.png");
    public static final PossessionHud INSTANCE = new PossessionHud(MinecraftClient.getInstance());

    private MinecraftClient client;
    private Random random = new Random();

    public PossessionHud(MinecraftClient client) {
        this.client = client;
    }

    public ActionResult onRenderHotbar(@SuppressWarnings("unused") float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        DissolutionPlayer player = (DissolutionPlayer) client.player;
        RemnantHandler handler = player.getRemnantHandler();
        if (!client.player.isCreative() && handler != null && handler.isSoul()) {
            Entity possessed = (Entity) player.getPossessionManager().getPossessedEntity();
            if (possessed == null || !DissolutionEntityTags.ITEM_USER.contains(possessed.getType())) {
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.PASS;
    }

    public void draw(int healthLast, int ticks, boolean highlight) {
        int scaledWidth = this.client.window.getScaledWidth();
        int scaledHeight = this.client.window.getScaledHeight();

        LivingEntity possessed = (LivingEntity) ((DissolutionPlayer) this.client.player).getPossessionManager().getPossessedEntity();
        if (possessed != null) {
            if (this.client.interactionManager.hasStatusBars()) {
                if (possessed.getHealth() > 0) {
                    int textureRow = 0;
                    // TODO use entity tags instead of mojangswitch
                    if (possessed instanceof PigZombieEntity) {
                        textureRow = 2;
                    } else if (possessed instanceof HuskEntity) {
                        textureRow = 3;
                    } else if (possessed instanceof ZombieEntity) {
                        textureRow = 1;
                    } else if (possessed instanceof WitherSkeletonEntity) {
                        textureRow = 5;
                    } else if (possessed instanceof StrayEntity) {
                        textureRow = 6;
                    } else if (possessed instanceof SkeletonEntity) {
                        textureRow = 4;
                    }
                    this.client.getTextureManager().bindTexture(DISSOLUTION_ECTOPLASM_ICONS);
                    this.drawPossessionHealthBar(possessed, scaledWidth, scaledHeight, textureRow, healthLast, highlight, ticks);
                    this.client.getTextureManager().bindTexture(ICONS);
                }
            }
            this.client.player.setBreath(possessed.getBreath());
        }
    }


    private void drawPossessionHealthBar(LivingEntity player, int width, int height, int textureRow, int healthLast, boolean highlight, int ticks) {
        int health = MathHelper.ceil(player.getHealth());

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
        int top = height - 39;

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
        int top = height - 39;
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
