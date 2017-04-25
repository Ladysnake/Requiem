package ladysnake.tartaros.client.handlers;

import java.util.List;

import ladysnake.tartaros.client.gui.IncorporealOverlay;
import ladysnake.tartaros.client.renders.blocks.RenderSoulAnchor;
import ladysnake.tartaros.common.capabilities.IIncorporealHandler;
import ladysnake.tartaros.common.capabilities.IncorporealDataHandler;
import ladysnake.tartaros.common.tileentities.TileEntitySoulAnchor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EventHandlerClient {
	
	private static RenderSoulAnchor renderAnch = new RenderSoulAnchor();

	@SubscribeEvent
	public void onEntityRender(RenderLivingEvent.Pre event) {
	    if(event.getEntity() instanceof EntityPlayer){
	    	//System.out.println("coucou");
	    	final IIncorporealHandler playerCorp = IncorporealDataHandler.getHandler((EntityPlayer)event.getEntity());
	    	//System.out.println(playerCorp);
	    	if(playerCorp.isIncorporeal()){
	    		GlStateManager.color(0.9F, 0.9F, 1.0F, 0.5F); //0.5F being alpha
	    	}
	    }
	}
	
	@SubscribeEvent
	public void onRenderGameOverlay(RenderGameOverlayEvent event) {
		if(event.getType() == ElementType.EXPERIENCE) {
		    final IIncorporealHandler playerCorp = IncorporealDataHandler.getHandler(Minecraft.getMinecraft().player);
		    if(playerCorp.isIncorporeal()){
		    	IncorporealOverlay.renderIncorporealOverlay(event.getResolution());
		    }
		}
	}
}
