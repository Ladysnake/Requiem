package ladysnake.dissolution.client.handlers;

import ladysnake.dissolution.client.renders.blocks.RenderSoulAnchor;
import ladysnake.dissolution.common.DissolutionConfig;
import ladysnake.dissolution.common.blocks.ISoulInteractable;
import ladysnake.dissolution.common.capabilities.IIncorporealHandler;
import ladysnake.dissolution.common.capabilities.IncorporealDataHandler;
import ladysnake.dissolution.common.networking.PacketHandler;
import ladysnake.dissolution.common.networking.PingMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EventHandlerClient {
	
	private static final float SOUL_VERTICAL_SPEED = 0.1f;
	private static RenderSoulAnchor renderAnch = new RenderSoulAnchor();
	private static int refresh = 0;

	@SubscribeEvent
	public void onGameTick(TickEvent event) {
		if (Minecraft.getMinecraft().player == null || event.side.isServer()) return;
		final IIncorporealHandler playerCorp = IncorporealDataHandler.getHandler(Minecraft.getMinecraft().player);
		//System.out.println(refresh);
		if(!playerCorp.isSynced() && refresh++%100 == 0)
		{
			System.out.println("REFRESH");
			IMessage msg = new PingMessage(Minecraft.getMinecraft().player.getUniqueID().getMostSignificantBits(), 
					Minecraft.getMinecraft().player.getUniqueID().getLeastSignificantBits());
			PacketHandler.net.sendToServer(msg);
		}
	}
	
	@SubscribeEvent
	public void onRenderGameOverlay(RenderGameOverlayEvent.Pre event) {
		if(event.getType() == RenderGameOverlayEvent.ElementType.ALL && IncorporealDataHandler.getHandler(Minecraft.getMinecraft().player).isIncorporeal()) {
			GuiIngameForge.renderFood = false;
			GuiIngameForge.renderHotbar = Minecraft.getMinecraft().player.isCreative();
		}
	}
	
	@SubscribeEvent
	public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
		if(IncorporealDataHandler.getHandler(Minecraft.getMinecraft().player).isIncorporeal() && 
				(event.getType() == RenderGameOverlayEvent.ElementType.HEALTH || event.getType() == RenderGameOverlayEvent.ElementType.FOOD
				|| event.getType() == RenderGameOverlayEvent.ElementType.HOTBAR)) {
			GlStateManager.color(1.0F,  1.0F, 1.0F, 1.0F);
		}
	}
	/*
	@SubscribeEvent
	public void onRenderGameOverlay(RenderGameOverlayEvent event) {
		if(IncorporealDataHandler.getHandler(Minecraft.getMinecraft().player).isIncorporeal() && event.getType() == RenderGameOverlayEvent.ElementType.HEALTH)
			event.setCanceled(false);
	}*/
	
	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent event) {
		if(event.side.isServer()) 
			return;
		if(DissolutionConfig.flightMode != DissolutionConfig.CUSTOM_FLIGHT) 
			return;
		if(IncorporealDataHandler.getHandler(event.player).isIncorporeal() && !event.player.isCreative()) {
		
			if(Minecraft.getMinecraft().gameSettings.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindJump) && event.player.experienceLevel > 0) {
				event.player.motionY = SOUL_VERTICAL_SPEED;
				event.player.velocityChanged = true;
			} else if(event.player.motionY <= 0) {
				if(event.player.world.getBlockState(event.player.getPosition()).getMaterial().isLiquid() ||
						event.player.world.getBlockState(event.player.getPosition().down()).getMaterial().isLiquid()) {
					if(event.player.experienceLevel <= 0 && 
							!(event.player.world.getBlockState(event.player.getPosition()).getMaterial().isLiquid()))
						event.player.motionY = 0;
					event.player.velocityChanged = true;
				} else {
					event.player.motionY = -0.8f * SOUL_VERTICAL_SPEED;
					event.player.fallDistance = 0;
					event.player.velocityChanged = true;
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onEntityRender(RenderLivingEvent.Pre event) {
	    if(event.getEntity() instanceof EntityPlayer){
	    	final IIncorporealHandler playerCorp = IncorporealDataHandler.getHandler((EntityPlayer)event.getEntity());
	    	if(playerCorp.isIncorporeal()){
	    		GlStateManager.color(0.9F, 0.9F, 1.0F, 0.5F); //0.5F being alpha
	    	}
	    }
	}
	
	@SubscribeEvent
	public void onRenderSpecificHand(RenderSpecificHandEvent event) {
   		event.setCanceled(IncorporealDataHandler.getHandler(Minecraft.getMinecraft().player).isIncorporeal());
	}
	
	@SubscribeEvent
	public void onDrawBlockHighlight (DrawBlockHighlightEvent event) {
		try {
			event.setCanceled(IncorporealDataHandler.getHandler(Minecraft.getMinecraft().player).isIncorporeal() && 
					!(event.getPlayer().world.getBlockState(event.getTarget().getBlockPos()).getBlock() instanceof ISoulInteractable));
		} catch (NullPointerException e) {	}
	}
}
