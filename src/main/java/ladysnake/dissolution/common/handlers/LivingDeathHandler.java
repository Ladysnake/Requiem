package ladysnake.dissolution.common.handlers;

import ladysnake.dissolution.common.capabilities.IIncorporealHandler;
import ladysnake.dissolution.common.capabilities.IncorporealDataHandler;
import ladysnake.dissolution.common.entity.EntityItemWaystone;
import ladysnake.dissolution.common.entity.EntityMinion;
import ladysnake.dissolution.common.entity.EntityMinionPigZombie;
import ladysnake.dissolution.common.entity.EntityMinionSkeleton;
import ladysnake.dissolution.common.entity.EntityMinionStray;
import ladysnake.dissolution.common.entity.EntityMinionWitherSkeleton;
import ladysnake.dissolution.common.entity.EntityMinionZombie;
import ladysnake.dissolution.common.init.ModBlocks;
import ladysnake.dissolution.common.init.ModItems;
import ladysnake.dissolution.common.inventory.Helper;
import ladysnake.dissolution.common.items.ItemScythe;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityHusk;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityStray;
import net.minecraft.entity.monster.EntityWitherSkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class LivingDeathHandler {

	@SubscribeEvent
	public void onLivingDeath(LivingDeathEvent event) {
		if (event.getEntity() instanceof EntityPlayer)
			this.handlePlayerDeath(event);

		if (event.getSource().getEntity() instanceof EntityPlayer)
			this.handlePlayerKill(event);
	}
	
	public void handlePlayerDeath(LivingDeathEvent event) {
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
	
	public void handlePlayerKill(LivingDeathEvent event) {
		EntityPlayer killer = (EntityPlayer) event.getSource().getEntity();
		EntityLivingBase victim = event.getEntityLiving();
		
		if (killer.getHeldItemMainhand().getItem() instanceof ItemScythe) {
			((ItemScythe) killer.getHeldItemMainhand().getItem()).harvestSoul(killer, victim);
		}

		ItemStack eye = Helper.findItem(killer, ModItems.EYE_OF_THE_UNDEAD);
		if (killer.world.rand.nextInt(1) == 0 && !eye.isEmpty() && !killer.world.isRemote) {

			EntityMinion corpse = null;
			
			if(victim instanceof EntityPigZombie) {
				corpse = new EntityMinionPigZombie(victim.world, ((EntityZombie)victim).isChild());
			} else if (victim instanceof EntityZombie) {
				corpse = new EntityMinionZombie(victim.world, victim instanceof EntityHusk, ((EntityZombie)victim).isChild());
			} else if (victim instanceof EntitySkeleton) {
				corpse = new EntityMinionSkeleton(victim.world);
			} else if(victim instanceof EntityStray){
				corpse = new EntityMinionStray(victim.world);
			} else if(victim instanceof EntityWitherSkeleton){
				corpse = new EntityMinionWitherSkeleton(victim.world);
			}

			if (corpse != null) {
				corpse.setPosition(victim.posX, victim.posY, victim.posZ);
				for (ItemStack stuff : victim.getEquipmentAndArmor()) {
					if(stuff.getItem().isValidArmor(stuff, EntityEquipmentSlot.HEAD, victim))
						corpse.setItemStackToSlot(EntityEquipmentSlot.HEAD, stuff);
					else if(stuff.getItem().isValidArmor(stuff, EntityEquipmentSlot.CHEST, victim))
						corpse.setItemStackToSlot(EntityEquipmentSlot.CHEST, stuff);
					else if(stuff.getItem().isValidArmor(stuff, EntityEquipmentSlot.LEGS, victim))
						corpse.setItemStackToSlot(EntityEquipmentSlot.LEGS, stuff);
					else if(stuff.getItem().isValidArmor(stuff, EntityEquipmentSlot.FEET, victim))
						corpse.setItemStackToSlot(EntityEquipmentSlot.FEET, stuff);
					
					else if(stuff.getItem().isValidArmor(stuff, EntityEquipmentSlot.MAINHAND, victim) && !stuff.isEmpty())
						corpse.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, stuff);
					else if(stuff.getItem().isValidArmor(stuff, EntityEquipmentSlot.OFFHAND, victim) && !stuff.isEmpty())
						corpse.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, stuff);
				}
				corpse.onUpdate();
				victim.world.spawnEntity(corpse);
				victim.posY = -500;
			}
		}
	}
}
