package ladysnake.tartaros.common.handlers;

import ladysnake.tartaros.common.Reference;
import ladysnake.tartaros.common.blocks.IRespawnLocation;
import ladysnake.tartaros.common.capabilities.IIncorporealHandler;
import ladysnake.tartaros.common.capabilities.IncorporealDataHandler;
import ladysnake.tartaros.common.capabilities.IncorporealDataHandler.Provider;
import ladysnake.tartaros.common.capabilities.IncorporealMessage;
import ladysnake.tartaros.common.init.ModBlocks;
import ladysnake.tartaros.common.networkingtest.PacketHandler;
import ladysnake.tartaros.common.networkingtest.PingMessage;
import ladysnake.tartaros.common.networkingtest.SimpleMessage;
import ladysnake.tartaros.common.networkingtest.UpdateMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly; 

public class EventHandlerCommon {
	
	private static boolean hasAskedServerForRenderInfo = false;
	
	@SubscribeEvent
	 public void attachCapability(AttachCapabilitiesEvent<Entity> event) {

		if (!(event.getObject() instanceof EntityPlayer)) return;
		
		event.addCapability(new ResourceLocation(Reference.MOD_ID, "incorporeal"), new Provider());
	 }
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onLivingDeath(LivingDeathEvent event) {
		if(event.getEntity() instanceof EntityPlayer && ((EntityPlayer)event.getEntity()).getUniqueID() == Minecraft.getMinecraft().player.getUniqueID())
		hasAskedServerForRenderInfo = false;
	}

	
	@SubscribeEvent
	public void clonePlayer(PlayerEvent.Clone event) {
		if(event.isWasDeath()){
			event.getEntityPlayer().experienceLevel = event.getOriginal().experienceLevel;
			final IIncorporealHandler clone = IncorporealDataHandler.getHandler(event.getEntityPlayer());
			clone.setIncorporeal(true);
			TartarosPacketHandler.INSTANCE.sendToAll(new IncorporealMessage(true, event.getEntityPlayer().getUniqueID().getMostSignificantBits(), event.getEntityPlayer().getUniqueID().getLeastSignificantBits()));
			IMessage msg = new SimpleMessage(event.getEntityPlayer().getUniqueID().getMostSignificantBits(), event.getEntityPlayer().getUniqueID().getLeastSignificantBits(), true);
			PacketHandler.net.sendToAll(msg);
		}
	}
	
	@SubscribeEvent (priority = EventPriority.HIGHEST)
	public void onPlayerInteract(PlayerInteractEvent event) {
		final IIncorporealHandler playerCorp = IncorporealDataHandler.getHandler(event.getEntityPlayer());
		if(playerCorp.isIncorporeal()){
			if(event.isCancelable() && !(event.getWorld().getBlockState(event.getPos()).getBlock() instanceof IRespawnLocation))
				event.setCanceled(true);
		}
	}
	
	@SubscribeEvent (priority = EventPriority.HIGHEST)
	public void onPlayerAttackEntity(AttackEntityEvent event) {
		final IIncorporealHandler playerCorp = IncorporealDataHandler.getHandler(event.getEntityPlayer());
		System.out.println(playerCorp + "\n" + playerCorp.getIncorporeal());
		if(playerCorp.isIncorporeal()){
			if(event.isCancelable())
				event.setCanceled(true);
		}
	}
	
	@SubscribeEvent (priority = EventPriority.HIGHEST)
	public void onEntityItemPickup(EntityItemPickupEvent event) {
		final IIncorporealHandler playerCorp = IncorporealDataHandler.getHandler(event.getEntityPlayer());
		System.out.println(playerCorp);
		if(playerCorp.isIncorporeal()){
			if(event.isCancelable())
				event.setCanceled(true);
		}
	}
	
	@SubscribeEvent (priority = EventPriority.HIGH)
	public void onEntityRender(RenderLivingEvent.Pre event) {
	    if(event.getEntity() instanceof EntityPlayer){
	    	if(!hasAskedServerForRenderInfo) {
	    		System.out.println("asking for render info");
	    		IMessage msg = new PingMessage();
				PacketHandler.net.sendToServer((msg));
	    	}
	    	//System.out.println("coucou");
	    	final IIncorporealHandler playerCorp = IncorporealDataHandler.getHandler((EntityPlayer)event.getEntity());
	    	//System.out.println(playerCorp);
	    	if(playerCorp.isIncorporeal()){
	    		GlStateManager.color(1.0F, 1.0F, 1.0F, 0.5F); //0.5F being alpha
	    	}
	    }
	}
}
