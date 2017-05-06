package ladysnake.dissolution.common.handlers;

import java.util.List;
import java.util.Random;

import ladysnake.dissolution.common.entity.EntityMinionSquelette;
import ladysnake.dissolution.common.entity.EntityMinionStray;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.TartarosConfig;
import ladysnake.dissolution.common.blocks.IRespawnLocation;
import ladysnake.dissolution.common.capabilities.IIncorporealHandler;
import ladysnake.dissolution.common.capabilities.IncorporealDataHandler;
import ladysnake.dissolution.common.capabilities.IncorporealDataHandler.Provider;
import ladysnake.dissolution.common.entity.EntityItemWaystone;
import ladysnake.dissolution.common.entity.EntityMinion;
import ladysnake.dissolution.common.entity.EntityMinionHusk;
import ladysnake.dissolution.common.entity.EntityMinionZombie;
import ladysnake.dissolution.common.init.ModBlocks;
import ladysnake.dissolution.common.init.ModItems;
import ladysnake.dissolution.common.inventory.Helper;
import ladysnake.dissolution.common.items.ItemEyeDead;
import ladysnake.dissolution.common.items.ItemScythe;
import ladysnake.dissolution.common.networking.IncorporealMessage;
import ladysnake.dissolution.common.networking.PacketHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.AbstractSkeleton;
import net.minecraft.entity.monster.EntityHusk;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityStray;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
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

	private static final Random rand = new Random();
	private static int ticksSinceLastSync = 0;

	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent event) {

		final IIncorporealHandler playerCorp = IncorporealDataHandler.getHandler(event.player);

		playerCorp.tick(event);
		

		if (playerCorp.isIncorporeal() && !event.player.isCreative()) {

			event.player.capabilities.isFlying = event.player.experienceLevel > 0;
			event.player.capabilities.allowFlying = event.player.experienceLevel > 0;
			if (event.side == Side.CLIENT)
				return;

			// Makes the player tangible if he is near 0,0
			if (Math.pow(Math.pow(Math.floor(event.player.posX), 2) + Math.pow(Math.floor(event.player.posZ), 2),
					0.5f) < 10 && ++ticksSinceLastSync >= 100) {
				playerCorp.setIncorporeal(false, event.player);
				IMessage msg = new IncorporealMessage(event.player.getUniqueID().getMostSignificantBits(),
						event.player.getUniqueID().getLeastSignificantBits(), playerCorp.isIncorporeal());
				PacketHandler.net.sendToAll(msg);
				for (int i = 0; i < 50; i++) {
					double motionX = rand.nextGaussian() * 0.02D;
					double motionY = rand.nextGaussian() * 0.02D + 1;
					double motionZ = rand.nextGaussian() * 0.02D;
					((WorldServer) event.player.world).spawnParticle(EnumParticleTypes.CLOUD, false,
							event.player.posX + 0.5D, event.player.posY + 1.0D, event.player.posZ + 0.5D, 1, 0.3D, 0.3D,
							0.3D, 0.0D, new int[0]);
				}
				if(event.player.dimension == -1 && TartarosConfig.respawnInNether) {
					CustomTartarosTeleporter.transferPlayerToDimension((EntityPlayerMP) event.player, event.player.getSpawnDimension());
//					event.player.setPosition(event.player.spawn, y, z);
				}
				ticksSinceLastSync = 0;
			}
			if (event.player.experience > 0 && rand.nextBoolean())
				event.player.experience--;
			else if (rand.nextInt() % 300 == 0 && event.player.experienceLevel > 0)
				event.player.removeExperienceLevel(1);
		}

		if (playerCorp.isIncorporeal() && !playerCorp.isSynced() && !event.player.world.isRemote
				&& TartarosConfig.respawnInNether) {
			CustomTartarosTeleporter.transferPlayerToDimension((EntityPlayerMP) event.player, -1);
			playerCorp.setSynced(true);
		}
	}

	@SubscribeEvent
	public void attachCapability(AttachCapabilitiesEvent<Entity> event) {

		if (!(event.getObject() instanceof EntityPlayer))
			return;

		event.addCapability(new ResourceLocation(Reference.MOD_ID, "incorporeal"), new Provider());
	}

	@SubscribeEvent
	public void onLivingDeath(LivingDeathEvent event) {

		if (event.getEntity() instanceof EntityPlayer) {
			EntityPlayer p = (EntityPlayer) event.getEntity();
			final IIncorporealHandler corp = IncorporealDataHandler.getHandler(p);
			corp.setLastDeathMessage(
					p.getDisplayNameString() + event.getSource().getDeathMessage(p).getUnformattedComponentText());

			final ItemStack merc = new ItemStack(ModBlocks.MERCURIUS_WAYSTONE);
			if (p.inventory.hasItemStack(merc)) {
				p.inventory.removeStackFromSlot(p.inventory.getSlotFor(merc));
				p.world.spawnEntity(new EntityItemWaystone(p.world, p.posX + 0.5, p.posY + 1.0, p.posZ + 0.5));
			}
		}

		if (event.getSource().getEntity() instanceof EntityPlayer) {
			EntityPlayer killer = (EntityPlayer) event.getSource().getEntity();
			EntityLivingBase victim = event.getEntityLiving();
			if (killer.getHeldItemMainhand().getItem() instanceof ItemScythe) {
				((ItemScythe) killer.getHeldItemMainhand().getItem()).fillBottle(killer);
			}

			if(killer.world.isRemote) return;
			ItemStack eye = Helper.findItem(killer, ModItems.EYE_OF_THE_UNDEAD);
			if (killer.world.rand.nextInt(1) == 0 && !eye.isEmpty() && !killer.world.isRemote) {

				if (victim instanceof EntityZombie) {
					EntityMinionZombie skullZ = (victim instanceof EntityHusk) ?
							new EntityMinionHusk(victim.world) :
							new EntityMinionZombie(victim.world);
					skullZ.setPosition(victim.posX, victim.posY, victim.posZ);
					skullZ.onUpdate();
					victim.world.spawnEntity(skullZ);
				} else if (victim instanceof EntitySkeleton) {
						System.out.println("ske");
						EntityMinionSquelette skullS;
						skullS = new EntityMinionSquelette(victim.world);
						skullS.onUpdate();
						skullS.setPosition(victim.posX, victim.posY, victim.posZ);
						victim.world.spawnEntity(skullS);

				} else if(victim instanceof EntityStray){
					EntityMinionStray skullSt;
					skullSt = new EntityMinionStray(victim.world);
					skullSt.onUpdate();
					skullSt.setPosition(victim.posX, victim.posY, victim.posZ);
					victim.world.spawnEntity(skullSt);
					
				}
			}
		}
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
			if (targetCorp.isIncorporeal() && !event.getEntityPlayer().isCreative())
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
