package ladysnake.dissolution.common.handlers;

import java.util.Random;

import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.TartarosConfig;
import ladysnake.dissolution.common.blocks.IRespawnLocation;
import ladysnake.dissolution.common.capabilities.IIncorporealHandler;
import ladysnake.dissolution.common.capabilities.IncorporealDataHandler;
import ladysnake.dissolution.common.capabilities.IncorporealDataHandler.Provider;
import ladysnake.dissolution.common.entity.EntityItemWaystone;
import ladysnake.dissolution.common.entity.EntityMinion;
import ladysnake.dissolution.common.entity.EntityMinionSkeleton;
import ladysnake.dissolution.common.entity.EntityMinionStray;
import ladysnake.dissolution.common.entity.EntityMinionZombie;
import ladysnake.dissolution.common.init.ModBlocks;
import ladysnake.dissolution.common.init.ModItems;
import ladysnake.dissolution.common.inventory.Helper;
import ladysnake.dissolution.common.items.ItemScythe;
import ladysnake.dissolution.common.networking.IncorporealMessage;
import ladysnake.dissolution.common.networking.PacketHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityHusk;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityStray;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
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

public class EventHandlerCommon {

	@SubscribeEvent
	public void attachCapability(AttachCapabilitiesEvent<Entity> event) {

		if (!(event.getObject() instanceof EntityPlayer))
			return;

		event.addCapability(new ResourceLocation(Reference.MOD_ID, "incorporeal"), new Provider());
	}

	@SubscribeEvent
	public void clonePlayer(PlayerEvent.Clone event) {
		if (event.isWasDeath() && !event.getEntityPlayer().isCreative()) {
			event.getEntityPlayer().experienceLevel = event.getOriginal().experienceLevel;
			final IIncorporealHandler corpse = IncorporealDataHandler.getHandler(event.getOriginal());
			final IIncorporealHandler clone = IncorporealDataHandler.getHandler(event.getEntityPlayer());
			clone.setIncorporeal(true, event.getEntityPlayer());
			clone.setLastDeathMessage(corpse.getLastDeathMessage());
			clone.setSynced(false);
			IMessage msg = new IncorporealMessage(event.getEntityPlayer().getUniqueID().getMostSignificantBits(),
					event.getEntityPlayer().getUniqueID().getLeastSignificantBits(), true);
			PacketHandler.net.sendToAll(msg);
			
			if(TartarosConfig.respawnInNether)
				event.getEntityPlayer().setPosition(event.getOriginal().posX, event.getOriginal().posY, event.getOriginal().posZ);
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onVisibilityPlayer(PlayerEvent.Visibility event) {
		final IIncorporealHandler playerCorp = IncorporealDataHandler.getHandler(event.getEntityPlayer());
		if (playerCorp.isIncorporeal())
			event.modifyVisibility(0D);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPlayerInteract(PlayerInteractEvent event) {
		final IIncorporealHandler playerCorp = IncorporealDataHandler.getHandler(event.getEntityPlayer());
		if (playerCorp.isIncorporeal() && !event.getEntityPlayer().isCreative()) {
			if (event.isCancelable()
					&& !(event.getWorld().getBlockState(event.getPos()).getBlock() instanceof IRespawnLocation)
					&& !(IncorporealDataHandler.soulInteractableBlocks
							.contains(event.getWorld().getBlockState(event.getPos()).getBlock())))
				event.setCanceled(true);
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPlayerAttackEntity(AttackEntityEvent event) {
		final IIncorporealHandler playerCorp = IncorporealDataHandler.getHandler(event.getEntityPlayer());
		if (playerCorp.isIncorporeal() && !event.getEntityPlayer().isCreative()) {
			if (event.isCancelable())
				event.setCanceled(true);
			return;
		}
		if (event.getTarget() instanceof EntityPlayer) {
			final IIncorporealHandler targetCorp = IncorporealDataHandler.getHandler(event.getTarget());
			if (playerCorp.isIncorporeal() && !event.getEntityPlayer().isCreative())
				if (event.isCancelable())
					event.setCanceled(true);
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onEntityItemPickup(EntityItemPickupEvent event) {
		final IIncorporealHandler playerCorp = IncorporealDataHandler.getHandler(event.getEntityPlayer());
		if (playerCorp.isIncorporeal() && !event.getEntityPlayer().isCreative()) {
			if (event.isCancelable())
				event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onEntityStruckByLightning(EntityStruckByLightningEvent event) {
		if (event.getEntity() instanceof EntityPlayer) {
			final IIncorporealHandler playerCorp = IncorporealDataHandler.getHandler((EntityPlayer) event.getEntity());
			if (playerCorp.isIncorporeal()) {
				playerCorp.setIncorporeal(false, (EntityPlayer) event.getEntity());
				IMessage msg = new IncorporealMessage(event.getEntity().getUniqueID().getMostSignificantBits(),
						event.getEntity().getUniqueID().getLeastSignificantBits(), playerCorp.isIncorporeal());
				PacketHandler.net.sendToAll(msg);
			}
		}
	}

}
