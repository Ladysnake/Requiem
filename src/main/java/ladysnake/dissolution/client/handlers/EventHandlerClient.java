package ladysnake.dissolution.client.handlers;

import ladysnake.dissolution.api.PlayerIncorporealEvent;
import ladysnake.dissolution.api.corporeality.IIncorporealHandler;
import ladysnake.dissolution.api.corporeality.IPossessable;
import ladysnake.dissolution.api.corporeality.ISoulInteractable;
import ladysnake.dissolution.client.particles.DissolutionParticleManager;
import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.config.DissolutionConfigManager;
import ladysnake.dissolution.common.networking.PacketHandler;
import ladysnake.dissolution.common.networking.PingMessage;
import ladysnake.dissolution.common.registries.SoulStates;
import ladysnake.dissolution.unused.common.blocks.BlockFluidMercury;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.ReflectionHelper.UnableToFindFieldException;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(value = Side.CLIENT, modid = Reference.MOD_ID)
public class EventHandlerClient {

    /**True if this client is connected to a server without the mod*/
    private static boolean noServerInstall;
    private static int cameraAnimation = 0;

    private static final float SOUL_VERTICAL_SPEED = 0.1f;
    private static MethodHandle highlightingItemStack;
    private static int refreshTimer = 0;

    static {
        try {
            Field f = ReflectionHelper.findField(GuiIngame.class, "highlightingItemStack", "field_92016_l");
            highlightingItemStack = MethodHandles.lookup().unreflectSetter(f);
        } catch (UnableToFindFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
//		event.getMap().registerSprite(AdditiveParticle.STAR_PARTICLE_TEXTURE);
    }

    @SubscribeEvent
    public static void onGameTick(TickEvent.ClientTickEvent event) {
        final EntityPlayer player = Minecraft.getMinecraft().player;
        if (player == null || noServerInstall) {
            return;
        }

        DissolutionParticleManager.INSTANCE.updateParticles();

        final IIncorporealHandler playerCorp = CapabilityIncorporealHandler.getHandler(player);

        // Sends a request to the server
        if (!playerCorp.isSynced() && refreshTimer++ % 100 == 0) {
            IMessage msg = new PingMessage(player.getUniqueID().getMostSignificantBits(),
                    player.getUniqueID().getLeastSignificantBits());
            PacketHandler.NET.sendToServer(msg);
        } else if (playerCorp.isSynced()) {
            refreshTimer = 0;
        }
    }

    @SubscribeEvent
    public static void onClientConnectedToServer(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        refreshTimer = 0;
        // if it's a local connection, the mod is installed
        noServerInstall = Dissolution.noServerInstall && !event.isLocal();
    }

    @SubscribeEvent
    public static void onPlayerIncorporeal(PlayerIncorporealEvent event) {
        EntityPlayer player = event.getEntityPlayer();
        if (player == Minecraft.getMinecraft().player) {
            if (DissolutionConfigManager.isFlightSetTo(DissolutionConfigManager.FlightModes.CUSTOM_FLIGHT)) {
                player.capabilities.setFlySpeed(event.getNewStatus().isIncorporeal() ? 0.025f : 0.05f);
            }
            if (!event.getNewStatus().isIncorporeal()) {
                GuiIngameForge.renderHotbar = true;
                GuiIngameForge.renderFood = true;
                GuiIngameForge.renderArmor = true;
                GuiIngameForge.renderAir = true;
            }
        }
    }

    @SubscribeEvent
    public static void onRenderGameOverlay(RenderGameOverlayEvent.Pre event) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        IIncorporealHandler handler = CapabilityIncorporealHandler.getHandler(player);
        boolean show = !handler.getCorporealityStatus().isIncorporeal();
        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {

            boolean possessing = handler.getPossessed() != null;
            // Disables most gui renders
            GuiIngameForge.renderFood = show;
            GuiIngameForge.renderHotbar = show || player.isCreative() || possessing;
            GuiIngameForge.renderHealthMount = show;
            GuiIngameForge.renderArmor = show || possessing;
            GuiIngameForge.renderAir = show;

            // Prevents the display of the name of the selected ItemStack
            if (!show && !player.isCreative() && !possessing) {
                try {
                    highlightingItemStack.invoke(Minecraft.getMinecraft().ingameGUI, ItemStack.EMPTY);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        }
    }

    @SubscribeEvent
    public static void onInventoryRender(GuiScreenEvent.DrawScreenEvent.Pre event) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        GuiScreen gui = event.getGui();
        if (player != null && gui instanceof GuiInventory) {
            GuiInventory inv = (GuiInventory) gui;
            int guiLeft = inv.getGuiLeft();
            int guiTop = inv.getGuiTop();
            EntityLivingBase possessed = CapabilityIncorporealHandler.getHandler(player).getPossessed();
            if (possessed != null) {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GuiInventory.drawEntityOnScreen(guiLeft + 51, guiTop + 75, 30, guiLeft + 51 - inv.oldMouseX, guiTop + 75 - 50 - inv.oldMouseY, possessed);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent event) {
        if (noServerInstall) {
            return;
        }

        final EntityPlayer player = event.player;
        final EntityPlayerSP playerSP = Minecraft.getMinecraft().player;
        if (player != playerSP) {
            return;
        }

        final IIncorporealHandler playerCorp = CapabilityIncorporealHandler.getHandler(player);

        if (cameraAnimation-- > 0 && event.player.eyeHeight < 1.8f) {
            player.eyeHeight += player.getDefaultEyeHeight() / 20f;
        }

        if (player.world.isMaterialInBB(player.getEntityBoundingBox()
                .grow(-0.1D, -0.4D, -0.1D), BlockFluidMercury.MATERIAL_MERCURY)) {
            playerSP.motionX *= 0.4f;
            playerSP.motionZ *= 0.4f;
        }

        if (!event.player.isCreative() &&
                playerCorp.getCorporealityStatus() == SoulStates.SOUL && event.phase == TickEvent.Phase.START) {

            if (DissolutionConfigManager.isFlightSetTo(DissolutionConfigManager.FlightModes.CUSTOM_FLIGHT)) {
                player.capabilities.setFlySpeed(0.025f);
                // Makes the player glide and stuff
                if (playerSP.movementInput.jump && player.getRidingEntity() == null) {
                    player.motionY = SOUL_VERTICAL_SPEED;
                    player.velocityChanged = true;
                } else if (player.motionY <= 0 && player.getRidingEntity() == null) {
                    if (player.world.getBlockState(player.getPosition()).getMaterial().isLiquid() ||
                            player.world.getBlockState(player.getPosition().down()).getMaterial().isLiquid()) {
                        player.velocityChanged = true;
                    } else {
                        player.motionY = -0.8f * SOUL_VERTICAL_SPEED;
                        player.fallDistance = 0;
                        player.velocityChanged = true;
                    }
                }
            }
        }
        IPossessable possessed = playerCorp.getPossessed();
        if (possessed != null) {
            possessed.possessTickClient();
        }
    }

    @SubscribeEvent
    public static void onEntityViewRenderCameraSetup(EntityViewRenderEvent.CameraSetup event) {
        EntityPlayerSP playerSP = Minecraft.getMinecraft().player;
        if (CapabilityIncorporealHandler.getHandler(playerSP).getPossessed() != null) {
            float yaw = (float) (playerSP.prevRotationYaw + (playerSP.rotationYaw - playerSP.prevRotationYaw) * event.getRenderPartialTicks() + 180.0F);
            float pitch = (float) (playerSP.prevRotationPitch + (playerSP.rotationPitch - playerSP.prevRotationPitch) * event.getRenderPartialTicks());
            event.setYaw(yaw);
            event.setPitch(pitch);
        }
    }

    @SubscribeEvent
    public static void onPlayerRender(RenderPlayerEvent.Pre event) {
        if (noServerInstall) {
            return;
        }

        final IIncorporealHandler playerCorp = CapabilityIncorporealHandler.getHandler(event.getEntityPlayer());
        if (playerCorp.getCorporealityStatus().isIncorporeal()) {
            float alpha = CapabilityIncorporealHandler.getHandler(Minecraft.getMinecraft().player).isStrongSoul() ? 0.8F : 0.05F;
            GlStateManager.color(0.9F, 0.9F, 1.0F, alpha); // Tints the player blue and reduces the transparency
        }
        event.getRenderer().shadowOpaque = playerCorp.getCorporealityStatus().isIncorporeal() ? 0F : 1F;
    }

    @SubscribeEvent
    public static void onPlayerRender(RenderPlayerEvent.Post event) {
        if (!noServerInstall && CapabilityIncorporealHandler.getHandler(event.getEntityPlayer()).getCorporealityStatus().isIncorporeal()) {
            GlStateManager.color(0, 0, 0, 0);
        }
    }

    @SubscribeEvent
    public static void onRenderWorldLast(RenderWorldLastEvent event) {
        DissolutionParticleManager.INSTANCE.renderParticles(event.getPartialTicks());
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public static void onRenderSpecificHand(RenderSpecificHandEvent event) {
        if (noServerInstall) {
            return;
        }
        EntityPlayerSP playerSP = Minecraft.getMinecraft().player;
        IIncorporealHandler handler = CapabilityIncorporealHandler.getHandler(playerSP);
        if (handler.getCorporealityStatus().isIncorporeal()) {
            EntityLivingBase possessed = handler.getPossessed();
            if (possessed == null && !playerSP.isCreative()) {
                event.setCanceled(true);
            } else if (possessed != null) {
                if(event.getHand() == EnumHand.MAIN_HAND) {
                    // adjust matrices to avoid a completely broken render
                    float f1 = playerSP.prevRotationPitch + (playerSP.rotationPitch - playerSP.prevRotationPitch) * event.getPartialTicks();
                    float f2 = playerSP.prevRotationYaw + (playerSP.rotationYaw - playerSP.prevRotationYaw) * event.getPartialTicks();
                    float f3 = possessed.prevRotationPitch + (possessed.rotationPitch - possessed.prevRotationPitch) * event.getPartialTicks();
                    float f4 = possessed.prevRotationYaw + (possessed.rotationYaw - possessed.prevRotationYaw) * event.getPartialTicks();
                    rotateArmReverse(playerSP, possessed, f3, f4, event.getPartialTicks());
                    rotateAroundXAndYReverse(f1, f2, f3, f4);
                }
                // render hand if possible
                if (event.getItemStack().isEmpty()) {
                    Render render = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(possessed);
                    if (render instanceof RenderLivingBase) {
                        RenderLivingBase renderLivingBase = (RenderLivingBase) render;
                        ModelBase model = renderLivingBase.getMainModel();
                        if (model instanceof ModelBiped && renderLivingBase.bindEntityTexture(possessed)) {
                            ModelBiped modelBiped = (ModelBiped) model;
                            if (event.getHand() == EnumHand.MAIN_HAND) {
                                EnumHandSide handSide = playerSP.getPrimaryHand();
                                GlStateManager.pushMatrix();
                                renderArmFirstPerson(possessed, modelBiped, event.getEquipProgress(), 0, handSide);
                                GlStateManager.popMatrix();
                                event.setCanceled(true);
                            }
                        }
                    }
                }
            }
        }
    }

    private static void renderArmFirstPerson(EntityLivingBase entity, ModelBiped model, float equippedProgress, float partialTicks, EnumHandSide handSide) {
        boolean rightHand = handSide != EnumHandSide.LEFT;
        float translation = rightHand ? 1.0F : -1.0F;
        float partialTicksSqrt = MathHelper.sqrt(partialTicks);
        float f2 = -0.3F * MathHelper.sin(partialTicksSqrt * (float) Math.PI);
        float f3 = 0.4F * MathHelper.sin(partialTicksSqrt * ((float) Math.PI * 2F));
        float f4 = -0.4F * MathHelper.sin(partialTicks * (float) Math.PI);
        GlStateManager.translate(translation * (f2 + 0.64000005F), f3 + -0.6F + equippedProgress * -0.6F, f4 + -0.71999997F);
        GlStateManager.rotate(translation * 45.0F, 0.0F, 1.0F, 0.0F);
        float f5 = MathHelper.sin(partialTicks * partialTicks * (float) Math.PI);
        float f6 = MathHelper.sin(partialTicksSqrt * (float) Math.PI);
        GlStateManager.rotate(translation * f6 * 70.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(translation * f5 * -20.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.translate(translation * -1.0F, 3.6F, 3.5F);
        GlStateManager.rotate(translation * 120.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(200.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(translation * -135.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(translation * 5.6F, 0.0F, 0.0F);
        GlStateManager.disableCull();

        renderArm(entity, model, rightHand);

        GlStateManager.enableCull();
    }

    private static void renderArm(EntityLivingBase entity, ModelBiped model, boolean right) {
        GlStateManager.color(1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        model.swingProgress = 0.0F;
        model.isSneak = false;
        model.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, entity);
        ModelRenderer arm = right ? model.bipedRightArm : model.bipedLeftArm;
        arm.rotateAngleX = 0.0F;
        arm.render(0.0625F);
        GlStateManager.disableBlend();
    }

    private static void rotateArmReverse(EntityPlayerSP entityplayersp, EntityLivingBase possessed, float f2, float f3, float partialTicks) {
        float f = entityplayersp.prevRenderArmPitch + (entityplayersp.renderArmPitch - entityplayersp.prevRenderArmPitch) * partialTicks;
        float f1 = entityplayersp.prevRenderArmYaw + (entityplayersp.renderArmYaw - entityplayersp.prevRenderArmYaw) * partialTicks;
        GlStateManager.rotate(((possessed.rotationPitch - f2) - (entityplayersp.rotationPitch - f)) * 0.1F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(((possessed.rotationYaw - f3) - (entityplayersp.rotationYaw - f1)) * 0.1F, 0.0F, 1.0F, 0.0F);
    }

    private static void rotateAroundXAndYReverse(float angle, float angleY, float angle1, float angleY1) {
        GlStateManager.pushMatrix();
        GlStateManager.rotate(angle1 - angle, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(angleY1 - angleY, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.popMatrix();
    }

    @SubscribeEvent
    public static void onDrawBlockHighlight(DrawBlockHighlightEvent event) {
        if (!noServerInstall && event.getTarget().typeOfHit == RayTraceResult.Type.BLOCK) {
            event.setCanceled(CapabilityIncorporealHandler.getHandler(event.getPlayer()).getCorporealityStatus().isIncorporeal() &&
                    !(event.getPlayer().world.getBlockState(event.getTarget().getBlockPos()).getBlock() instanceof ISoulInteractable));
        }
    }
}
