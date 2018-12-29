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
import net.minecraft.init.MobEffects;
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

            if (this.mc.playerController.shouldDrawHUD()) {
                EntityLivingBase possessed = pl.getPossessed();
                eventParent.set(this, event);
                if (possessed != null && possessed.getHealth() > 0) {
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
                    // We need to set the render view entity back to a player as renderAir and renderHotbar require it
                    mc.setRenderViewEntity(this.mc.player);
                    this.mc.player.setAir(possessed.getAir());
                    this.renderAir(res.getScaledWidth(), res.getScaledHeight());
                    this.renderHotbar(res, event.getPartialTicks());
                    mc.setRenderViewEntity(possessed);
                }
            } else if (Minecraft.getMinecraft().player.isCreative() && pl.getPossessed() != null) {
                this.renderHotbar(res, event.getPartialTicks());
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
        float absorb = MathHelper.ceil(player.getAbsorptionAmount());

        int healthRows = MathHelper.ceil((healthMax + absorb) / 2.0F / 10.0F);
        int rowHeight = Math.max(10 - (healthRows - 2), 3);

        this.rand.setSeed((long) (updateCounter * 312871));

        int left = width / 2 - 91;
        int top = height - GuiIngameForge.left_height;
        GuiIngameForge.left_height += (healthRows * rowHeight);
        if (rowHeight != 10) {
            GuiIngameForge.left_height += 10 - rowHeight;
        }

        int regen = -1;
        if (player.isPotionActive(MobEffects.REGENERATION)) {
            regen = updateCounter % 25;
        }

        int MARGIN = 0;
        final int BACKGROUND = (highlight ? MARGIN + 9 : MARGIN);
        final int TOP = textureRow * 9;
        if (player.isPotionActive(MobEffects.POISON)) {
            MARGIN += 36;
            this.mc.getTextureManager().bindTexture(ICONS);
        } else if (player.isPotionActive(MobEffects.WITHER)) {
            MARGIN += 72;
            this.mc.getTextureManager().bindTexture(ICONS);
        } else {
            this.mc.getTextureManager().bindTexture(ECTOPLASM_ICONS);
        }

        float absorbRemaining = absorb;

        for (int i = MathHelper.ceil((healthMax + absorb) / 2.0F) - 1; i >= 0; --i) {
            //int b0 = (highlight ? 1 : 0);
            int row = MathHelper.ceil((float) (i + 1) / 10.0F) - 1;
            int x = left + i % 10 * 8;
            int y = top - row * rowHeight;

            if (health <= 4) {
                y += rand.nextInt(2);
            }
            if (i == regen) {
                y -= 2;
            }

            drawTexturedModalRect(x, y, BACKGROUND, TOP, 9, 9);

            if (highlight) {
                if (i * 2 + 1 < healthLast) {
                    drawTexturedModalRect(x, y, MARGIN + 54, TOP, 9, 9); //6
                } else if (i * 2 + 1 == healthLast) {
                    drawTexturedModalRect(x, y, MARGIN + 63, TOP, 9, 9); //7
                }
            }

            if (absorbRemaining > 0.0F) {
                if (absorbRemaining == absorb && absorb % 2.0F == 1.0F) {
                    drawTexturedModalRect(x, y, MARGIN + 153, TOP, 9, 9); //17
                    absorbRemaining -= 1.0F;
                } else {
                    drawTexturedModalRect(x, y, MARGIN + 144, TOP, 9, 9); //16
                    absorbRemaining -= 2.0F;
                }
            } else {
                if (i * 2 + 1 < health) {
                    drawTexturedModalRect(x, y, MARGIN + 36, TOP, 9, 9); //4
                } else if (i * 2 + 1 == health) {
                    drawTexturedModalRect(x, y, MARGIN + 45, TOP, 9, 9); //5
                }
            }
        }
    }

}
