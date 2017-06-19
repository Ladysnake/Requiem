package ladysnake.dissolution.common.handlers;

import java.lang.reflect.InvocationTargetException;

import com.mojang.authlib.GameProfile;

import ladysnake.dissolution.common.DissolutionConfig;
import ladysnake.dissolution.common.capabilities.IIncorporealHandler;
import ladysnake.dissolution.common.capabilities.IncorporealDataHandler;
import ladysnake.dissolution.common.entity.EntityItemWaystone;
import ladysnake.dissolution.common.entity.EntityMinion;
import ladysnake.dissolution.common.entity.EntityMinionPigZombie;
import ladysnake.dissolution.common.entity.EntityMinionSkeleton;
import ladysnake.dissolution.common.entity.EntityMinionStray;
import ladysnake.dissolution.common.entity.EntityMinionWitherSkeleton;
import ladysnake.dissolution.common.entity.EntityMinionZombie;
import ladysnake.dissolution.common.entity.EntityPlayerCorpse;
import ladysnake.dissolution.common.init.ModBlocks;
import ladysnake.dissolution.common.init.ModItems;
import ladysnake.dissolution.common.inventory.Helper;
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
import net.minecraft.scoreboard.IScoreCriteria;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class LivingDeathHandler {

	@SubscribeEvent(priority = EventPriority.LOWEST)
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
		
		if(!p.world.isRemote) {
			EntityPlayerCorpse body = new EntityPlayerCorpse(p.world);
			body.setPosition(p.posX, p.posY, p.posZ);
			p.world.spawnEntity(body);
		}
		
		if(DissolutionConfig.skipDeathScreen || true) {
			if(!p.world.isRemote)
				fakePlayerDeath((EntityPlayerMP)p, event.getSource());
			corp.setIncorporeal(true, p);
			p.setHealth(1);
			if(DissolutionConfig.respawnInNether && !p.world.isRemote)
				CustomTartarosTeleporter.transferPlayerToDimension((EntityPlayerMP) p, -1);
			event.setCanceled(true);
		}
	}
	
	public void fakePlayerDeath(EntityPlayerMP player, DamageSource cause) {
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
				ReflectionHelper.findMethod(EntityPlayer.class, "destroyVanishingCursedItems", "func_190776_cN").invoke(player);
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

        player.addStat(StatList.DEATHS);
        player.takeStat(StatList.TIME_SINCE_DEATH);
        player.extinguish();
        //player.setFlag(0, false);
        player.getCombatTracker().reset();
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
