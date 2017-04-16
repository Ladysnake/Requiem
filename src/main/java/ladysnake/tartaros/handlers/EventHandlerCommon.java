package ladysnake.tartaros.handlers;

import ladysnake.tartaros.Reference;
import ladysnake.tartaros.capabilities.IIncorporealHandler;
import ladysnake.tartaros.capabilities.IncorporealDataHandler;
import ladysnake.tartaros.capabilities.IncorporealDataHandler.Provider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
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

	
	@SubscribeEvent (priority = EventPriority.LOWEST)
	public void clonePlayer(PlayerEvent.Clone event) {
		if(event.isWasDeath()){
			System.out.println("T mor lol");
			final IIncorporealHandler clone = IncorporealDataHandler.getHandler(event.getEntity());
			clone.setIncorporeal(true);
		}
	}
}
