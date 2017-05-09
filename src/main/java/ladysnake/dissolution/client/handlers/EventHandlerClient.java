package ladysnake.dissolution.client.handlers;

import ladysnake.dissolution.client.gui.GuiIncorporealOverlay;
import ladysnake.dissolution.client.renders.blocks.RenderSoulAnchor;
import ladysnake.dissolution.common.capabilities.IIncorporealHandler;
import ladysnake.dissolution.common.capabilities.IncorporealDataHandler;
import ladysnake.dissolution.common.networking.IncorporealMessage;
import ladysnake.dissolution.common.networking.PacketHandler;
import ladysnake.dissolution.common.networking.PingMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EventHandlerClient {
	
	private static RenderSoulAnchor renderAnch = new RenderSoulAnchor();
	private static int refresh = 0;

	@SubscribeEvent
	public void onGameTick(TickEvent event) {
		if (Minecraft.getMinecraft().player == null || !Minecraft.getMinecraft().player.world.isRemote) return;
		final IIncorporealHandler playerCorp = IncorporealDataHandler.getHandler(Minecraft.getMinecraft().player);
		//System.out.println(playerCorp.isSynced());
		if(!playerCorp.isSynced() && refresh++%100 == 0){
			IMessage msg = new PingMessage(Minecraft.getMinecraft().player.getUniqueID().getMostSignificantBits(), Minecraft.getMinecraft().player.getUniqueID().getLeastSignificantBits());
			PacketHandler.net.sendToServer(msg);
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
	public void onRenderSpecificHand(RenderSpecificHandEvent event) {		//TODO make this work
		final IIncorporealHandler myCorp = IncorporealDataHandler.getHandler(Minecraft.getMinecraft().player);
		
		if(myCorp.isIncorporeal()){
	    		event.setCanceled(true);
	    	}
	}
}
