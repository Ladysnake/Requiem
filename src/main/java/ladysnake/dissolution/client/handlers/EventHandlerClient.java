package ladysnake.dissolution.client.handlers;

import ladysnake.dissolution.api.corporeality.IIncorporealHandler;
import ladysnake.dissolution.api.corporeality.ISoulInteractable;
import ladysnake.dissolution.client.particles.DissolutionParticleManager;
import ladysnake.dissolution.client.renders.entities.RenderWillOWisp;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.blocks.BlockFluidMercury;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.capabilities.EctoplasmStats;
import ladysnake.dissolution.api.PlayerIncorporealEvent;
import ladysnake.dissolution.common.config.DissolutionConfigManager;
import ladysnake.dissolution.common.networking.PacketHandler;
import ladysnake.dissolution.common.networking.PingMessage;
import ladysnake.dissolution.common.registries.EctoplasmCorporealityStatus;
import ladysnake.dissolution.common.registries.SoulCorporealityStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
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

    private static int cameraAnimation = 0;

    private static final float SOUL_VERTICAL_SPEED = 0.1f;
    private static MethodHandle highlightingItemStack;
    private static int refreshTimer = 0;

    private static float prevHealth = 20;
    private static double prevMaxHealth = 20;
    private static boolean wasRidingLastTick = false;

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
        if (player == null) return;

        DissolutionParticleManager.INSTANCE.updateParticles();

        final IIncorporealHandler playerCorp = CapabilityIncorporealHandler.getHandler(player);

        // Sends a request to the server
        if (!playerCorp.isSynced() && refreshTimer++ % 100 == 0 && refreshTimer <= 1000) {
            IMessage msg = new PingMessage(player.getUniqueID().getMostSignificantBits(),
                    player.getUniqueID().getLeastSignificantBits());
            PacketHandler.NET.sendToServer(msg);
        } else if (playerCorp.isSynced())
            refreshTimer = 0;

        // Convoluted way of displaying the health of the possessed entity
        if (player.isRiding() && player.getRidingEntity() instanceof EntityLiving && ((EntityLiving) player.getRidingEntity()).getHealth() > 0) {
            if (!wasRidingLastTick) {
                prevHealth = player.getHealth();
                IAttributeInstance maxHealth = player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
                prevMaxHealth = maxHealth.getAttributeValue();
                maxHealth.setBaseValue(
                        ((EntityLiving) player.getRidingEntity()).getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getAttributeValue());
                wasRidingLastTick = true;
            }
            if (player.getHealth() != ((EntityLiving) player.getRidingEntity()).getHealth())
                player.setHealth(((EntityLiving) player.getRidingEntity()).getHealth());
        } else if (wasRidingLastTick) {
            player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(prevMaxHealth);
            player.setHealth(prevHealth);
            wasRidingLastTick = false;
        }
    }

    @SubscribeEvent
    public static void onClientConnectedToServer(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        refreshTimer = 0;
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
        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL && handler.getCorporealityStatus().isIncorporeal()) {

            // Disables most gui renders
            GuiIngameForge.renderFood = false;
            GuiIngameForge.renderHotbar = player.isCreative() || handler.getPossessed() != null;
            GuiIngameForge.renderHealthMount = false;
            GuiIngameForge.renderArmor = handler.getPossessed() != null;
            GuiIngameForge.renderAir = false;

            // Prevents the display of the name of the selected ItemStack
            if (!player.isCreative() && handler.getPossessed() == null) {
                try {
                    highlightingItemStack.invoke(Minecraft.getMinecraft().ingameGUI, ItemStack.EMPTY);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent event) {
        if (event.side.isServer())
            return;

        final EntityPlayer player = event.player;
        final EntityPlayerSP playerSP = Minecraft.getMinecraft().player;
        if (player != playerSP) return;

        final IIncorporealHandler playerCorp = CapabilityIncorporealHandler.getHandler(player);

        if (cameraAnimation-- > 0 && event.player.eyeHeight < 1.8f)
            player.eyeHeight += player.getDefaultEyeHeight() / 20f;

        if (player.world.isMaterialInBB(player.getEntityBoundingBox()
                .grow(-0.1D, -0.4D, -0.1D), BlockFluidMercury.MATERIAL_MERCURY)) {
            try {
//				playerSP.motionY *= 0.2f;
//				if(playerSP.movementInput.jump && playerSP.motionY < 0.6f)
//					playerSP.motionY += 0.4f;
//				else
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            playerSP.motionX *= 0.4f;
            playerSP.motionZ *= 0.4f;
        }

        if (!event.player.isCreative() &&
                (playerCorp.getCorporealityStatus() == SoulCorporealityStatus.SOUL
                        || playerCorp.getEctoplasmStats().getActiveSpells().contains(EctoplasmStats.SoulSpells.FLIGHT)) && event.phase == TickEvent.Phase.START) {

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
        if (playerCorp.getPossessed() != null) {
            playerCorp.getPossessed().possessTickClient();
        }
    }

    private static RenderWillOWisp<EntityPlayer> renderSoul;

    @SubscribeEvent
    public static void onPlayerRender(RenderPlayerEvent.Pre event) {
        final IIncorporealHandler playerCorp = CapabilityIncorporealHandler.getHandler(event.getEntityPlayer());
        if (playerCorp.getCorporealityStatus() == EctoplasmCorporealityStatus.ECTOPLASM) {
            GlStateManager.color(0.9F, 0.9F, 1.0F, 0.5F); // Tints the player blue and halves the transparency
        } else if (playerCorp.getCorporealityStatus() == SoulCorporealityStatus.SOUL) {
            if (playerCorp.getPossessed() == null) {
                if (renderSoul == null)
                    renderSoul = new RenderWillOWisp<>(Minecraft.getMinecraft().getRenderManager());
                renderSoul.doRender(event.getEntityPlayer(), event.getX(), event.getY(), event.getZ(),
                        event.getEntityPlayer().getRotationYawHead(), event.getPartialRenderTick());
            }
            event.setCanceled(true);
        }
        event.getRenderer().shadowOpaque = playerCorp.getCorporealityStatus().isIncorporeal() ? 0F : 1F;
    }

    @SubscribeEvent
    public static void onPlayerRender(RenderPlayerEvent.Post event) {
        if (CapabilityIncorporealHandler.getHandler(event.getEntityPlayer()).getCorporealityStatus().isIncorporeal())
            GlStateManager.color(0, 0, 0, 0);
    }

    @SubscribeEvent
    public static void onRenderWorldLast(RenderWorldLastEvent event) {
        DissolutionParticleManager.INSTANCE.renderParticles(event.getPartialTicks());
    }

    @SubscribeEvent
    public static void onRenderSpecificHand(RenderSpecificHandEvent event) {
        IIncorporealHandler handler = CapabilityIncorporealHandler.getHandler(Minecraft.getMinecraft().player);
        if (handler.getCorporealityStatus().isIncorporeal()) {
            if (handler.getPossessed() == null || event.getItemStack().isEmpty())
                event.setCanceled(true);
            else if (handler.getPossessed() instanceof EntityLivingBase) {
                EntityLivingBase possessed = (EntityLivingBase) CapabilityIncorporealHandler.getHandler(Minecraft.getMinecraft().player).getPossessed();
                EntityPlayerSP playerSP = Minecraft.getMinecraft().player;
                float f1 = playerSP.prevRotationPitch + (playerSP.rotationPitch - playerSP.prevRotationPitch) * event.getPartialTicks();
                float f2 = playerSP.prevRotationYaw + (playerSP.rotationYaw - playerSP.prevRotationYaw) * event.getPartialTicks();
                float f3 = possessed.prevRotationPitch + (possessed.rotationPitch - possessed.prevRotationPitch) * event.getPartialTicks();
                float f4 = possessed.prevRotationYaw + (possessed.rotationYaw - possessed.prevRotationYaw) * event.getPartialTicks();
                rotateArmReverse(playerSP, possessed, f3, f4, event.getPartialTicks());
                rotateAroundXAndYReverse(f1, f2, f3, f4);
            }
        }
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
        if (event.getTarget().getBlockPos() != null)
            event.setCanceled(CapabilityIncorporealHandler.getHandler(event.getPlayer()).getCorporealityStatus().isIncorporeal() &&
                    !(event.getPlayer().world.getBlockState(event.getTarget().getBlockPos()).getBlock() instanceof ISoulInteractable));
    }
}
