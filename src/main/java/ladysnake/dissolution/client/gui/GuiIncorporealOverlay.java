package ladysnake.dissolution.client.gui;

import ladylib.reflection.TypedReflection;
import ladylib.reflection.typed.TypedSetter;
import ladysnake.dissolution.api.corporeality.IIncorporealHandler;
import ladysnake.dissolution.common.Ref;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

@SideOnly(Side.CLIENT)
public class GuiIncorporealOverlay extends GuiIngameForge {

    private static final ResourceLocation ECTOPLASM_ICONS = new ResourceLocation(Ref.MOD_ID, "textures/gui/icons.png");
    private static final TypedSetter<GuiIngameForge, RenderGameOverlayEvent> eventParent = TypedReflection.findSetter(GuiIngameForge.class, "eventParent", RenderGameOverlayEvent.class);
    private final Random rand = new Random();

    public GuiIncorporealOverlay(Minecraft mc) {
        super(mc);
    }

    @SubscribeEvent
    public void onRenderExperienceBar(RenderGameOverlayEvent.Post event) {
        final IIncorporealHandler pl = CapabilityIncorporealHandler.getHandler(this.mc.player);
        if (event.getType() == ElementType.ALL) {
            OverlaysRenderer.INSTANCE.renderOverlays(event.getPartialTicks());

            /* Draw Incorporeal Ingame Gui */

            ScaledResolution res = event.getResolution();

            EntityLivingBase possessed = pl.getPossessed();
            if (possessed != null) {
                eventParent.set(this, event);
                if (this.mc.playerController.shouldDrawHUD()) {
                    if (possessed.getHealth() > 0) {
                        int textureRow = 0;
                        if (possessed instanceof EntityPigZombie) {
                            textureRow = 1;
                        } else if (possessed instanceof EntityHusk) {
                            textureRow = 2;
                        } else if (possessed instanceof EntityWitherSkeleton) {
                            textureRow = 4;
                        } else if (possessed instanceof EntityStray) {
                            textureRow = 5;
                        } else if (possessed instanceof EntitySkeleton) {
                            textureRow = 3;
                        }
                        this.mc.getTextureManager().bindTexture(ECTOPLASM_ICONS);
                        this.drawCustomHealthBar(possessed, res, textureRow);
                        this.mc.getTextureManager().bindTexture(GuiIngameForge.ICONS);
                        this.renderArmor(res.getScaledWidth(), res.getScaledHeight());
                    }
                }
                // We need to set the render view entity back to a player as renderAir and renderHotbar require it
                mc.setRenderViewEntity(this.mc.player);
                this.mc.player.setAir(possessed.getAir());
                this.renderAir(res.getScaledWidth(), res.getScaledHeight());
                this.renderHotbar(res, event.getPartialTicks());
                mc.setRenderViewEntity(possessed);
            }
        }
    }

    @SubscribeEvent
    public void onRenderHealth(RenderGameOverlayEvent.Pre event) {
        if (event.getType() == ElementType.HEALTH) {
            final IIncorporealHandler pl = CapabilityIncorporealHandler.getHandler(this.mc.player);
            event.setCanceled(pl.getCorporealityStatus().isIncorporeal() && pl.getPossessed() == null);
        }
    }

    private void drawCustomHealthBar(EntityLivingBase player, ScaledResolution scaledResolution, int textureRow) {
        int width = scaledResolution.getScaledWidth();
        int height = scaledResolution.getScaledHeight();

        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        int health = MathHelper.ceil(player.getHealth());
        boolean highlight = healthUpdateCounter > (long) updateCounter && (healthUpdateCounter - (long) updateCounter) / 3L % 2L == 1L;

        if (health < this.playerHealth && player.hurtResistantTime > 0) {
            this.lastSystemTime = Minecraft.getSystemTime();
            this.healthUpdateCounter = (long) (this.updateCounter + 20);
        } else if (health > this.playerHealth && player.hurtResistantTime > 0) {
            this.lastSystemTime = Minecraft.getSystemTime();
            this.healthUpdateCounter = (long) (this.updateCounter + 10);
        }

        if (Minecraft.getSystemTime() - this.lastSystemTime > 1000L) {
            this.playerHealth = health;
            this.lastPlayerHealth = health;
            this.lastSystemTime = Minecraft.getSystemTime();
        }

        this.playerHealth = health;
        int healthLast = this.lastPlayerHealth;

        IAttributeInstance attrMaxHealth = player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
        float healthMax = (float) attrMaxHealth.getAttributeValue();
        int absorb = MathHelper.ceil(mc.player.getAbsorptionAmount());

        if (healthMax > 100) {
            drawShortenedCustomHealthBar(health, absorb, width, height, textureRow);
            return;
        }

        int healthRows = MathHelper.ceil((healthMax + absorb) / 2.0F / 10.0F);
        int rowHeight = Math.max(10 - (healthRows - 2), 3);

        this.rand.setSeed((long) (updateCounter * 312871));

        int left = width / 2 - 91;
        int top = height - GuiIngameForge.left_height;
        GuiIngameForge.left_height += (healthRows * rowHeight);
        if (rowHeight != 10) {
            GuiIngameForge.left_height += 10 - rowHeight;
        }

        int textureMargin = 0;
        int textureTop;
        if (absorb > 0) {
            textureMargin += 16;
            textureTop = 0;
            this.mc.getTextureManager().bindTexture(ICONS);
        } else {
            textureTop = textureRow * 9;
            this.mc.getTextureManager().bindTexture(ECTOPLASM_ICONS);
        }
        int textureBackground = (highlight ? textureMargin + 9 : textureMargin);

        float absorbRemaining = absorb;

        for (int i = MathHelper.ceil((healthMax + absorb) / 2.0F) - 1; i >= 0; --i) {
            //int b0 = (highlight ? 1 : 0);
            int row = MathHelper.ceil((float) (i + 1) / 10.0F) - 1;
            int x = left + i % 10 * 8;
            int y = top - row * rowHeight;

            if (health <= 4) {
                y += rand.nextInt(2);
            }

            drawTexturedModalRect(x, y, textureBackground, textureTop, 9, 9);

            if (highlight) {
                if (i * 2 + 1 < healthLast) {
                    drawTexturedModalRect(x, y, textureMargin + 54, textureTop, 9, 9); //6
                } else if (i * 2 + 1 == healthLast) {
                    drawTexturedModalRect(x, y, textureMargin + 63, textureTop, 9, 9); //7
                }
            }

            if (absorbRemaining > 0.0F) {
                if (absorbRemaining == absorb && absorb % 2.0F == 1.0F) {
                    drawTexturedModalRect(x, y, textureMargin + 153, textureTop, 9, 9); //17
                    absorbRemaining -= 1.0F;
                } else {
                    drawTexturedModalRect(x, y, textureMargin + 144, textureTop, 9, 9); //16
                    absorbRemaining -= 2.0F;
                }
                if (absorbRemaining <= 0.0F) {
                    this.mc.getTextureManager().bindTexture(ECTOPLASM_ICONS);
                    textureMargin -= 16;
                    textureTop = textureRow * 9;
                    textureBackground = (highlight ? textureMargin + 9 : textureMargin);
                }
            } else {
                if (i * 2 + 1 < health) {
                    drawTexturedModalRect(x, y, textureMargin + 36, textureTop, 9, 9); //4
                } else if (i * 2 + 1 == health) {
                    drawTexturedModalRect(x, y, textureMargin + 45, textureTop, 9, 9); //5
                }
            }
        }
    }

    private void drawShortenedCustomHealthBar(int health, int absorb, int width, int height, int textureRow) {
        int left = width / 2 - 91;
        int top = height - GuiIngameForge.left_height;
        GuiIngameForge.left_height += 11;
        mc.getTextureManager().bindTexture(ECTOPLASM_ICONS);
        drawTexturedModalRect(left, top, 0, textureRow * 9, 9, 9);
        drawTexturedModalRect(left, top, 36, textureRow * 9, 9, 9);
        left = mc.fontRenderer.drawString("x" + health / 2, left + 9, top, 0xFFFFFF, true);
        if (absorb > 0) {
            mc.getTextureManager().bindTexture(ICONS);
            drawTexturedModalRect(left + 11, top, 16, 0, 9, 9);
            drawTexturedModalRect(left + 11, top, 16 + 144, 0, 9, 9);
            mc.fontRenderer.drawString("x" + absorb / 2, left + 11 + 9, top, 0xFFFFFF, true);
        }

    }

}
