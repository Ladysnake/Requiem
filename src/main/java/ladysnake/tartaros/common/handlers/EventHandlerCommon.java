package ladysnake.tartaros.common.handlers;

import java.util.Random;

import ladysnake.tartaros.client.gui.IncorporealOverlay;
import ladysnake.tartaros.common.Reference;
import ladysnake.tartaros.common.blocks.IRespawnLocation;
import ladysnake.tartaros.common.capabilities.IIncorporealHandler;
import ladysnake.tartaros.common.capabilities.IncorporealDataHandler;
import ladysnake.tartaros.common.capabilities.IncorporealDataHandler.Provider;
import ladysnake.tartaros.common.entity.EntityItemWaystone;
import ladysnake.tartaros.common.init.ModBlocks;
import ladysnake.tartaros.common.networkingtest.PacketHandler;
import ladysnake.tartaros.common.networkingtest.PingMessage;
import ladysnake.tartaros.common.networkingtest.SimpleMessage;
import ladysnake.tartaros.common.networkingtest.UpdateMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly; 

public class EventHandlerCommon {

	private static final Random rand = new Random();
	private static int ticksSinceLastSync = 0;
	
	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent event) {
		if(event.side == Side.CLIENT) return;
		
		final IIncorporealHandler playerCorp = IncorporealDataHandler.getHandler(event.player);
		if(playerCorp.isIncorporeal() && !event.player.isCreative()) {
			if(++ticksSinceLastSync >= 100) {
				if(Math.floor(event.player.posX) == 0 && Math.floor(event.player.posZ) == 0) {
					playerCorp.setIncorporeal(false, event.player);
					for(int i = 0; i < 50; i++) {
					    double motionX = rand.nextGaussian() * 0.02D;
					    double motionY = rand.nextGaussian() * 0.02D + 1;
					    double motionZ = rand.nextGaussian() * 0.02D;
					    ((WorldServer)event.player.world).spawnParticle(EnumParticleTypes.CLOUD, false, event.player.posX + 0.5D, event.player.posY+ 1.0D, event.player.posZ+ 0.5D, 1, 0.3D, 0.3D, 0.3D, 0.0D, new int[0]); 
					}
				}
				IMessage msg = new SimpleMessage(event.player.getUniqueID().getMostSignificantBits(), event.player.getUniqueID().getLeastSignificantBits(), playerCorp.isIncorporeal());
				PacketHandler.net.sendToAll(msg);
				ticksSinceLastSync = 0;
			}
			if(event.player.experience > 0 && rand.nextInt()%3 == 0)
				event.player.experience --;
				if(rand.nextInt()%300 == 0)
					if(event.player.experienceLevel > 0) {
						event.player.removeExperienceLevel(1);
					} else {
						event.player.capabilities.allowFlying = false;
					}
		}
	}
	
	@SubscribeEvent
	 public void attachCapability(AttachCapabilitiesEvent<Entity> event) {

		if (!(event.getObject() instanceof EntityPlayer)) return;
		
		event.addCapability(new ResourceLocation(Reference.MOD_ID, "incorporeal"), new Provider());
	 }
	
	@SubscribeEvent
	public void onLivingDeath(LivingDeathEvent event){
		if(event.getEntity() instanceof EntityPlayer){
			EntityPlayer p = (EntityPlayer)event.getEntity();
			final ItemStack merc = new ItemStack(ModBlocks.mercurius_waystone);
			if(p.inventory.hasItemStack(merc)) {
				p.inventory.removeStackFromSlot(p.inventory.getSlotFor(merc));
				p.world.spawnEntity(new EntityItemWaystone(p.world, p.posX + 0.5, p.posY + 1.0, p.posZ + 0.5));
			}
		}
	}

	
	@SubscribeEvent
	public void clonePlayer(PlayerEvent.Clone event) {
		if(event.isWasDeath() && !event.getEntityPlayer().isCreative()){
			event.getEntityPlayer().experienceLevel = event.getOriginal().experienceLevel;
			final IIncorporealHandler clone = IncorporealDataHandler.getHandler(event.getEntityPlayer());
			clone.setIncorporeal(true, event.getEntityPlayer());
			IMessage msg = new SimpleMessage(event.getEntityPlayer().getUniqueID().getMostSignificantBits(), event.getEntityPlayer().getUniqueID().getLeastSignificantBits(), true);
			PacketHandler.net.sendToAll(msg);
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onVisibilityPlayer(PlayerEvent.Visibility event) {
		final IIncorporealHandler playerCorp = IncorporealDataHandler.getHandler(event.getEntityPlayer());
		if(playerCorp.isIncorporeal())
			event.modifyVisibility(0D);
	}
	
	@SubscribeEvent (priority = EventPriority.HIGHEST)
	public void onPlayerInteract(PlayerInteractEvent event) {
		final IIncorporealHandler playerCorp = IncorporealDataHandler.getHandler(event.getEntityPlayer());
		if(playerCorp.isIncorporeal() && !event.getEntityPlayer().isCreative()){
			if(event.isCancelable() && !(event.getWorld().getBlockState(event.getPos()).getBlock() instanceof IRespawnLocation))
				event.setCanceled(true);
		}
	}
	
	@SubscribeEvent (priority = EventPriority.HIGHEST)
	public void onPlayerAttackEntity(AttackEntityEvent event) {
		final IIncorporealHandler playerCorp = IncorporealDataHandler.getHandler(event.getEntityPlayer());
		if(playerCorp.isIncorporeal() && !event.getEntityPlayer().isCreative()){
			if(event.isCancelable())
				event.setCanceled(true);
			return;
		}
		if(event.getTarget() instanceof EntityPlayer) {
			final IIncorporealHandler targetCorp = IncorporealDataHandler.getHandler(event.getTarget());
			if(targetCorp.isIncorporeal() && !event.getEntityPlayer().isCreative())
				if(event.isCancelable())
					event.setCanceled(true);
		}
	}
	
	@SubscribeEvent (priority = EventPriority.HIGHEST)
	public void onEntityItemPickup(EntityItemPickupEvent event) {
		final IIncorporealHandler playerCorp = IncorporealDataHandler.getHandler(event.getEntityPlayer());
		if(playerCorp.isIncorporeal() && !event.getEntityPlayer().isCreative()){
			if(event.isCancelable())
				event.setCanceled(true);
		}
	}
	
	@SideOnly(Side.CLIENT)
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
	
	@SideOnly(Side.CLIENT)
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
