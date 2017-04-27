package ladysnake.tartaros.client.handlers;

import ladysnake.tartaros.client.gui.GuiIncorporealOverlay;
import ladysnake.tartaros.client.renders.blocks.RenderSoulAnchor;
import ladysnake.tartaros.common.capabilities.IIncorporealHandler;
import ladysnake.tartaros.common.capabilities.IncorporealDataHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EventHandlerClient {
	
	private static RenderSoulAnchor renderAnch = new RenderSoulAnchor();

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
	public void onRenderGameOverlay(RenderGameOverlayEvent event) {
		if(event.getType() == ElementType.EXPERIENCE) {
			final IIncorporealHandler myCorp = IncorporealDataHandler.getHandler(Minecraft.getMinecraft().player);
		    if(myCorp.isIncorporeal()){
		    	GuiIncorporealOverlay.INSTANCE.drawIncorporealOverlay(event.getResolution());
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
