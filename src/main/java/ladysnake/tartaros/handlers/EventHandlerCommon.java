package ladysnake.tartaros.handlers;

import ladysnake.tartaros.Reference;
import ladysnake.tartaros.capabilities.IIncorporeal;
import ladysnake.tartaros.capabilities.IncorporealProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

public class EventHandlerCommon {
	
	public static final ResourceLocation INCORPOREAL_CAP = new ResourceLocation(Reference.MOD_ID, "incorporeal");	//TODO find the correct name for the provider
	
	@SubscribeEvent
	 public void attachCapability(AttachCapabilitiesEvent<Entity> event) {

		if (!(event.getObject() instanceof EntityPlayer)) return;
		System.out.println("\nYES\n");

	 	event.addCapability(INCORPOREAL_CAP, new IncorporealProvider());
	 	
	 }
	
	@SubscribeEvent
	 public void onPlayerLogsIn(PlayerLoggedInEvent event) {

		EntityPlayer player = event.player;
		IIncorporeal corp = player.getCapability(IncorporealProvider.INCORPOREAL_CAP, null);




	 System.out.println(corp.isIncorporeal() ? "vous etes mort" : "vous etes vivant");

	 }
	
	@SubscribeEvent (priority = EventPriority.LOWEST)
	public void onLivingDeath(LivingDeathEvent e) {
		if(e.getEntityLiving() instanceof EntityPlayer)
			System.out.println("T mor lol");
	}
}
