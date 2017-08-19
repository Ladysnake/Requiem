package ladysnake.dissolution.common.handlers;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;

import ladysnake.dissolution.common.capabilities.IIncorporealHandler;
import ladysnake.dissolution.common.DissolutionConfig;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.entity.EntityPlayerCorpse;
import ladysnake.dissolution.common.entity.item.EntityItemWaystone;
import ladysnake.dissolution.common.entity.minion.AbstractMinion;
import ladysnake.dissolution.common.entity.minion.EntityMinionPigZombie;
import ladysnake.dissolution.common.entity.minion.EntityMinionSkeleton;
import ladysnake.dissolution.common.entity.minion.EntityMinionStray;
import ladysnake.dissolution.common.entity.minion.EntityMinionWitherSkeleton;
import ladysnake.dissolution.common.entity.minion.EntityMinionZombie;
import ladysnake.dissolution.common.init.ModBlocks;
import ladysnake.dissolution.common.init.ModItems;
import ladysnake.dissolution.common.inventory.InventoryPlayerCorpse;
import ladysnake.dissolution.common.inventory.InventorySearchHelper;
import ladysnake.dissolution.common.items.ItemScythe;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityHusk;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityStray;
import net.minecraft.entity.monster.EntityWitherSkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.scoreboard.IScoreCriteria;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class LivingDeathHandler {
	
	private static Method destroyVanishingCursedItems;
	
	static {
		destroyVanishingCursedItems = ReflectionHelper.findMethod(EntityPlayer.class, "destroyVanishingCursedItems", "func_190776_cN");
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onLivingDeath(LivingDeathEvent event) {
		if (event.getEntity() instanceof EntityPlayer)
			this.handlePlayerDeath(event);

		if (event.getSource().getTrueSource() instanceof EntityPlayer)
			this.handlePlayerKill(event);
	}
	
	protected void handlePlayerDeath(LivingDeathEvent event) {
		
		final EntityPlayer p = (EntityPlayer) event.getEntity();
		final IIncorporealHandler corp = CapabilityIncorporealHandler.getHandler(p);
		corp.setLastDeathMessage(
				p.getDisplayNameString() + event.getSource().getDeathMessage(p).getUnformattedComponentText());

		final ItemStack merc = new ItemStack(ModBlocks.MERCURIUS_WAYSTONE);
		if (p.inventory.hasItemStack(merc)) {
			p.inventory.removeStackFromSlot(p.inventory.getSlotFor(merc));
			p.world.spawnEntity(new EntityItemWaystone(p.world, p.posX + 0.5, p.posY + 1.0, p.posZ + 0.5));
		}
		
		if(!p.world.isRemote) {
			final EntityPlayerCorpse body = new EntityPlayerCorpse(p.world);
			body.setPosition(p.posX, p.posY, p.posZ);
			body.setCustomNameTag(p.getName());

			boolean flag = false;
			if(event.getSource().getTrueSource() instanceof EntityPlayer) {
				EntityPlayer killer = (EntityPlayer) event.getSource().getTrueSource();
				if(!InventorySearchHelper.findItem(killer, ModItems.EYE_OF_THE_UNDEAD).isEmpty())
					flag = true;
			}
			
			ItemStack lifeProtectionRing = InventorySearchHelper.findItem(p, ModItems.SCARAB_OF_ETERNITY);
			if(!lifeProtectionRing.isEmpty()) {
				p.inventory.setInventorySlotContents(InventorySearchHelper.getSlotFor(p.inventory, lifeProtectionRing), ItemStack.EMPTY);
				flag = true;
			}

			body.setDecaying(!flag);
			
			if(DissolutionConfig.respawn.bodiesHoldInventory) {
				
				if((flag || DissolutionConfig.respawn.bodiesHoldInventory) && !p.isSpectator() && !p.world.getGameRules().getBoolean("keepInventory")) {
					transferEquipment(p, body);
					body.setInventory(new InventoryPlayerCorpse(p.inventory.mainInventory, body));
					p.inventory.clear();
				}
			}
			
			body.onUpdate();
			
			p.world.spawnEntity(body);
			body.setPlayer(p.getUniqueID());
		}
		
		if(DissolutionConfig.respawn.skipDeathScreen) {
			if(!p.world.isRemote)
				fakePlayerDeath((EntityPlayerMP)p, event.getSource());
			corp.setIncorporeal(true);
			p.setHealth(20f);
			if(!DissolutionConfig.respawn.respawnInNether && DissolutionConfig.respawn.wowLikeRespawn) {
				BlockPos respawnLoc = p.getBedLocation() != null ? p.getBedLocation() : p.world.getSpawnPoint();
				p.setPosition(respawnLoc.getX(), respawnLoc.getY(), respawnLoc.getZ());
			}
			if(DissolutionConfig.respawn.respawnInNether && !p.world.isRemote)
				CustomDissolutionTeleporter.transferPlayerToDimension((EntityPlayerMP) p, DissolutionConfig.respawn.respawnDimension);
			event.setCanceled(true);
		}
	}
	
	public static void fakePlayerDeath(EntityPlayerMP player, DamageSource cause) {
        boolean flag = player.world.getGameRules().getBoolean("showDeathMessages");

        if (flag)
        {
            Team team = player.getTeam();

            if (team != null && team.getDeathMessageVisibility() != Team.EnumVisible.ALWAYS)
            {
                if (team.getDeathMessageVisibility() == Team.EnumVisible.HIDE_FOR_OTHER_TEAMS)
                {
                    player.mcServer.getPlayerList().sendMessageToAllTeamMembers(player, player.getCombatTracker().getDeathMessage());
                }
                else if (team.getDeathMessageVisibility() == Team.EnumVisible.HIDE_FOR_OWN_TEAM)
                {
                    player.mcServer.getPlayerList().sendMessageToTeamOrAllPlayers(player, player.getCombatTracker().getDeathMessage());
                }
            }
            else
            {
                player.mcServer.getPlayerList().sendMessage(player.getCombatTracker().getDeathMessage());
            }
        }

        if (!player.world.getGameRules().getBoolean("keepInventory") && !player.isSpectator())
        {
        	player.captureDrops = true;
        	player.capturedDrops.clear();
            try {
				destroyVanishingCursedItems.invoke(player);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
            player.inventory.dropAllItems();

            net.minecraftforge.event.entity.player.PlayerDropsEvent event = new net.minecraftforge.event.entity.player.PlayerDropsEvent(player, cause, player.capturedDrops, false);
            if (!net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event))
            {
                for (net.minecraft.entity.item.EntityItem item : player.capturedDrops)
                {
                    player.world.spawnEntity(item);
                }
            }
        }

        for (ScoreObjective scoreobjective : player.world.getScoreboard().getObjectivesFromCriteria(IScoreCriteria.DEATH_COUNT))
        {
            Score score = player.getWorldScoreboard().getOrCreateScore(player.getName(), scoreobjective);
            score.incrementScore();
        }

        EntityLivingBase entitylivingbase = player.getAttackingEntity();

        if (entitylivingbase != null)
        {
            EntityList.EntityEggInfo entitylist$entityegginfo = EntityList.ENTITY_EGGS.get(EntityList.getKey(entitylivingbase));

            if (entitylist$entityegginfo != null)
            {
                player.addStat(entitylist$entityegginfo.entityKilledByStat);
            }

            //entitylivingbase.func_191956_a(player, player.scoreValue, cause);
        }

        player.clearActivePotions();
        player.addStat(StatList.DEATHS);
        player.takeStat(StatList.TIME_SINCE_DEATH);
        player.extinguish();
        //player.setFlag(0, false);
        player.getCombatTracker().reset();
	}
	
	protected void handlePlayerKill(LivingDeathEvent event) {
		EntityPlayer killer = (EntityPlayer) event.getSource().getTrueSource();
		EntityLivingBase victim = event.getEntityLiving();
		
		if (killer.getHeldItemMainhand().getItem() instanceof ItemScythe) {
			((ItemScythe) killer.getHeldItemMainhand().getItem()).harvestSoul(killer, victim);
		}

		ItemStack eye = InventorySearchHelper.findItem(killer, ModItems.EYE_OF_THE_UNDEAD);
		if (killer.world.rand.nextInt(1) == 0 && !eye.isEmpty() && !killer.world.isRemote) {

			AbstractMinion corpse = null;
			
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
				transferEquipment(victim, corpse);
				corpse.onUpdate();
				victim.world.spawnEntity(corpse);
				victim.posY = -500;
			}
		}
	}
	
	public static void transferEquipment(EntityLivingBase source, EntityLivingBase dest) {
		for (ItemStack stuff : source.getEquipmentAndArmor()) {
			EntityEquipmentSlot slot = null;
			if(stuff.getItem().isValidArmor(stuff, EntityEquipmentSlot.HEAD, source))
				slot = EntityEquipmentSlot.HEAD;
			else if(stuff.getItem().isValidArmor(stuff, EntityEquipmentSlot.CHEST, source))
				slot = EntityEquipmentSlot.CHEST;
			else if(stuff.getItem().isValidArmor(stuff, EntityEquipmentSlot.LEGS, source))
				slot = EntityEquipmentSlot.LEGS;
			else if(stuff.getItem().isValidArmor(stuff, EntityEquipmentSlot.FEET, source))
				slot = EntityEquipmentSlot.FEET;
			else if(stuff.getItem().isValidArmor(stuff, EntityEquipmentSlot.MAINHAND, source) && !stuff.isEmpty())
				slot = EntityEquipmentSlot.MAINHAND;
			else if(stuff.getItem().isValidArmor(stuff, EntityEquipmentSlot.OFFHAND, source) && !stuff.isEmpty())
				slot = EntityEquipmentSlot.OFFHAND;
			if(slot != null) {
				if(dest.getItemStackFromSlot(slot) != ItemStack.EMPTY)
					dest.entityDropItem(stuff, 0.5f);
				else
					dest.setItemStackToSlot(slot, stuff);
				source.setItemStackToSlot(slot, ItemStack.EMPTY);
			}
		}
	}
}
