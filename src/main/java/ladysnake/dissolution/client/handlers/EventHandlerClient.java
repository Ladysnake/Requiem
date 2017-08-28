package ladysnake.dissolution.client.handlers;

import java.lang.reflect.Field;

import ladysnake.dissolution.client.particles.AdditiveParticle;
import ladysnake.dissolution.client.renders.DissolutionParticleManager;
import ladysnake.dissolution.client.renders.blocks.RenderSoulAnchor;
import ladysnake.dissolution.common.DissolutionConfigManager;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.DissolutionConfigManager.FlightModes;
import ladysnake.dissolution.common.blocks.ISoulInteractable;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.capabilities.IIncorporealHandler;
import ladysnake.dissolution.common.networking.PacketHandler;
import ladysnake.dissolution.common.networking.PingMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.ReflectionHelper.UnableToFindFieldException;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(value=Side.CLIENT, modid=Reference.MOD_ID)
public class EventHandlerClient {

	public static int cameraAnimation = 0;

	private static final float SOUL_VERTICAL_SPEED = 0.1f;
	private static RenderSoulAnchor renderAnch = new RenderSoulAnchor();
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
		System.out.println("stiching textures");
		event.getMap().registerSprite(AdditiveParticle.STAR_PARTICLE_TEXTURE);
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
	public static void onRenderGameOverlay(RenderGameOverlayEvent.Pre event) {
		EntityPlayer player = Minecraft.getMinecraft().player;
		if(event.getType() == RenderGameOverlayEvent.ElementType.ALL && CapabilityIncorporealHandler.getHandler(player).isIncorporeal()) {

			// Disables most gui renders
			GuiIngameForge.renderFood = false;
			GuiIngameForge.renderHotbar = player.isCreative();
			GuiIngameForge.renderHealthMount = false;
			GuiIngameForge.renderArmor = false;
			GuiIngameForge.renderHealth = player.isRiding();
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

		if(playerCorp.isIncorporeal() && !player.isCreative()) {

			if(DissolutionConfigManager.isFlightEnabled(FlightModes.CUSTOM_FLIGHT)) {
				player.capabilities.setFlySpeed(player.experienceLevel > 0 ? 0.025f : 0.01f);
				// Makes the player glide and stuff
				if(playerSP.movementInput.jump && player.experienceLevel > 0 && player.getRidingEntity() == null) {
					player.motionY = SOUL_VERTICAL_SPEED;
					player.velocityChanged = true;
				} else if(player.motionY <= 0 && player.getRidingEntity() == null) {
					if(player.world.getBlockState(player.getPosition()).getMaterial().isLiquid() ||
							player.world.getBlockState(player.getPosition().down()).getMaterial().isLiquid()) {
						if(event.player.experienceLevel <= 0 &&
								!(player.world.getBlockState(player.getPosition()).getMaterial().isLiquid()))
							player.motionY = 0;
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

	@SubscribeEvent
	public static void onEntityRender(RenderLivingEvent.Pre event) {
	    if(event.getEntity() instanceof EntityPlayer){
	    	final IIncorporealHandler playerCorp = CapabilityIncorporealHandler.getHandler((EntityPlayer)event.getEntity());
	    	if(playerCorp.isIncorporeal()){
	    		GlStateManager.color(0.9F, 0.9F, 1.0F, 0.5F); // Tints the player blue and halves the transparency
	    	}
	    }
	}
	
	@SubscribeEvent
	public static void onRenderWorldLast(RenderWorldLastEvent event) {
		DissolutionParticleManager.INSTANCE.renderParticles(event.getPartialTicks());
	}

	@SubscribeEvent
	public static void onRenderSpecificHand(RenderSpecificHandEvent event) {
   		event.setCanceled(CapabilityIncorporealHandler.getHandler(Minecraft.getMinecraft().player).isIncorporeal());
	}

	@SubscribeEvent
	public static void onDrawBlockHighlight (DrawBlockHighlightEvent event) {
		try {
			event.setCanceled(CapabilityIncorporealHandler.getHandler(event.getPlayer()).isIncorporeal() &&
					!(event.getPlayer().world.getBlockState(event.getTarget().getBlockPos()).getBlock() instanceof ISoulInteractable));
		} catch (NullPointerException e) {}
	}
}
