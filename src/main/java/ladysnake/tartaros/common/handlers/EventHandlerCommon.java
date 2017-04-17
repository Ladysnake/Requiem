package ladysnake.tartaros.common.handlers;

import ladysnake.tartaros.common.Reference;
import ladysnake.tartaros.common.capabilities.IIncorporealHandler;
import ladysnake.tartaros.common.capabilities.IncorporealDataHandler;
import ladysnake.tartaros.common.capabilities.IncorporealDataHandler.Provider;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent; 

public class EventHandlerCommon {
	
	@SubscribeEvent
	 public void attachCapability(AttachCapabilitiesEvent<Entity> event) {

		if (!(event.getObject() instanceof EntityPlayer)) return;
		
		event.addCapability(new ResourceLocation(Reference.MOD_ID, "incorporeal"), new Provider());
		System.out.println("\nYES\n");
	 }

	
	@SubscribeEvent
	public void clonePlayer(PlayerEvent.Clone event) {
		if(event.isWasDeath()){
			System.out.println("T mor lol");
			final IIncorporealHandler clone = IncorporealDataHandler.getHandler(event.getEntity());
			clone.setIncorporeal(true);
		}
	}
	
	@SubscribeEvent (priority = EventPriority.HIGHEST)
	public void onPlayerInteract(PlayerInteractEvent event) {
		IIncorporealHandler playerCorp = IncorporealDataHandler.getHandler(event.getEntityPlayer());
		if(playerCorp.isIncorporeal()){
			System.out.println("MOUHAHAHA");
			if(event.isCancelable())
				event.setCanceled(true);
			playerCorp.setIncorporeal(false);
		}
	}
	
	@SubscribeEvent
	public void onEntityRender(RenderLivingEvent.Pre event) {
	    if(event.getEntity() instanceof EntityPlayer){
	    	System.out.println("coucou");
	    	IIncorporealHandler playerCorp = IncorporealDataHandler.getHandler((EntityPlayer)event.getEntity());
	    	if(playerCorp.isIncorporeal())
	    		GlStateManager.color(1.0F, 1.0F, 1.0F, 0.5F); //0.5F being alpha
	    }
	}
}
