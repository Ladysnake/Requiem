package ladysnake.dissolution.client.handlers;

import ladysnake.dissolution.api.EctoplasmStats;
import ladysnake.dissolution.api.IIncorporealHandler;
import ladysnake.dissolution.api.ISoulInteractable;
import ladysnake.dissolution.client.particles.AdditiveParticle;
import ladysnake.dissolution.client.particles.DissolutionParticleManager;
import ladysnake.dissolution.client.renders.entities.RenderWillOWisp;
import ladysnake.dissolution.common.DissolutionConfigManager;
import ladysnake.dissolution.common.DissolutionConfigManager.FlightModes;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.capabilities.PlayerIncorporealEvent;
import ladysnake.dissolution.common.networking.PacketHandler;
import ladysnake.dissolution.common.networking.PingMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.EntityLiving;
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
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.ReflectionHelper.UnableToFindFieldException;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Field;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(value=Side.CLIENT, modid=Reference.MOD_ID)
public class EventHandlerClient {

	public static int cameraAnimation = 0;

	private static final float SOUL_VERTICAL_SPEED = 0.1f;
	private static Field highlightingItemStack;
	private static int refreshTimer = 0;

	private static float prevHealth = 20;
	private static double prevMaxHealth = 20;
	private static boolean wasRidingLastTick = false;

	static {
		try {
			highlightingItemStack = ReflectionHelper.findField(GuiIngame.class, "highlightingItemStack", "field_92016_l");
		} catch (UnableToFindFieldException e) {
			e.printStackTrace();
		}
	}
	
	@SubscribeEvent
	public static void onTextureStitch(TextureStitchEvent.Pre event){
		event.getMap().registerSprite(AdditiveParticle.STAR_PARTICLE_TEXTURE);
		event.getMap().registerSprite(RenderWillOWisp.WILL_O_WISP_TEXTURE);
	}

	@SubscribeEvent
	public static void onGameTick(TickEvent event) {
		final EntityPlayer player = Minecraft.getMinecraft().player;
		if (player == null || event.side.isServer()) return;
		
		DissolutionParticleManager.INSTANCE.updateParticles();

		final IIncorporealHandler playerCorp = CapabilityIncorporealHandler.getHandler(player);

		// Sends a request to the server
		if(!playerCorp.isSynced() && refreshTimer++%100 == 0 && refreshTimer <= 1000)
		{
			IMessage msg = new PingMessage(player.getUniqueID().getMostSignificantBits(),
					player.getUniqueID().getLeastSignificantBits());
			PacketHandler.net.sendToServer(msg);
		} else if(playerCorp.isSynced())
			refreshTimer = 0;

		// Convoluted way of displaying the health of the possessed entity
		if(player.isRiding() && player.getRidingEntity() instanceof EntityLiving) {
			if(!wasRidingLastTick) {
				prevHealth = player.getHealth();
				IAttributeInstance maxHealth = player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
				prevMaxHealth = maxHealth.getAttributeValue();
				maxHealth.setBaseValue(
						((EntityLiving)player.getRidingEntity()).getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getAttributeValue());
				wasRidingLastTick = true;
			}
			player.setHealth(((EntityLiving)player.getRidingEntity()).getHealth());
		} else if(wasRidingLastTick) {
			player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(prevMaxHealth);
			player.setHealth(prevHealth);
			wasRidingLastTick = false;
		}
	}

	@SubscribeEvent
	public static void onPlayerIncorporeal(PlayerIncorporealEvent event) {
		EntityPlayer player = event.getPlayer();
		if(player == Minecraft.getMinecraft().player) {
			if (DissolutionConfigManager.isFlightEnabled(FlightModes.CUSTOM_FLIGHT)) {
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
		if(event.getType() == RenderGameOverlayEvent.ElementType.ALL &&
				CapabilityIncorporealHandler.getHandler(player).getCorporealityStatus().isIncorporeal()) {

			// Disables most gui renders
			GuiIngameForge.renderFood = false;
			GuiIngameForge.renderHotbar = player.isCreative();
			GuiIngameForge.renderHealthMount = false;
			GuiIngameForge.renderArmor = false;
			GuiIngameForge.renderAir = false;

			// Prevents the display of the name of the selected ItemStack
			if(!player.isCreative()) {
				try {
					highlightingItemStack.set(Minecraft.getMinecraft().ingameGUI, ItemStack.EMPTY);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent event) {
		if(event.side.isServer())
			return;

		final EntityPlayer player = event.player;
		final EntityPlayerSP playerSP = Minecraft.getMinecraft().player;
		final IIncorporealHandler playerCorp = CapabilityIncorporealHandler.getHandler(player);

		if(cameraAnimation-- > 0 && event.player.eyeHeight < 1.8f)
			player.eyeHeight += player.getDefaultEyeHeight() / 20f;

		if(!event.player.isCreative() &&
				(playerCorp.getCorporealityStatus() == IIncorporealHandler.CorporealityStatus.SOUL
						|| playerCorp.getEctoplasmStats().getActiveSpells().contains(EctoplasmStats.SoulSpells.FLIGHT))) {

			if(DissolutionConfigManager.isFlightEnabled(FlightModes.CUSTOM_FLIGHT)) {
				player.capabilities.setFlySpeed(0.025f);
				// Makes the player glide and stuff
				if(playerSP.movementInput.jump && player.getRidingEntity() == null) {
					player.motionY = SOUL_VERTICAL_SPEED;
					player.velocityChanged = true;
				} else if(player.motionY <= 0 && player.getRidingEntity() == null) {
					if(player.world.getBlockState(player.getPosition()).getMaterial().isLiquid() ||
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
	}

	private static RenderWillOWisp<EntityPlayer> renderSoul;

	@SubscribeEvent
	public static void onPlayerRender(RenderPlayerEvent.Pre event) {
    	final IIncorporealHandler playerCorp = CapabilityIncorporealHandler.getHandler(event.getEntityPlayer());
    	if(playerCorp.getCorporealityStatus() == IIncorporealHandler.CorporealityStatus.ECTOPLASM){
    		GlStateManager.color(0.9F, 0.9F, 1.0F, 0.5F); // Tints the player blue and halves the transparency
		} else if(playerCorp.getCorporealityStatus() == IIncorporealHandler.CorporealityStatus.SOUL) {
			if(renderSoul == null)
				renderSoul = new RenderWillOWisp<>(Minecraft.getMinecraft().getRenderManager());
			renderSoul.doRender(event.getEntityPlayer(), event.getX(), event.getY(), event.getZ(),
					event.getEntityPlayer().getRotationYawHead(), event.getPartialRenderTick());
    		event.setCanceled(true);
		}
		event.getRenderer().shadowOpaque = playerCorp.getCorporealityStatus().isIncorporeal() ? 0F : 1F;
	}

	@SubscribeEvent
	public static void onPlayerRender(RenderPlayerEvent.Post event) {
		if(CapabilityIncorporealHandler.getHandler(event.getEntityPlayer()).getCorporealityStatus().isIncorporeal())
			GlStateManager.color(0,0,0,0);
	}
	
	@SubscribeEvent
	public static void onRenderWorldLast(RenderWorldLastEvent event) {
		DissolutionParticleManager.INSTANCE.renderParticles(event.getPartialTicks());
	}

	@SubscribeEvent
	public static void onRenderSpecificHand(RenderSpecificHandEvent event) {
   		event.setCanceled(CapabilityIncorporealHandler.getHandler(Minecraft.getMinecraft().player).getCorporealityStatus().isIncorporeal());
	}

	@SubscribeEvent
	public static void onDrawBlockHighlight (DrawBlockHighlightEvent event) {
		if(event.getTarget().getBlockPos() != null)
			event.setCanceled(CapabilityIncorporealHandler.getHandler(event.getPlayer()).getCorporealityStatus().isIncorporeal() &&
					!(event.getPlayer().world.getBlockState(event.getTarget().getBlockPos()).getBlock() instanceof ISoulInteractable));
	}
}
