package ladysnake.dissolution.common.handlers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import ladysnake.dissolution.api.IIncorporealHandler;
import ladysnake.dissolution.api.IIncorporealHandler.CorporealityStatus;
import ladysnake.dissolution.common.DissolutionConfig;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.entity.EntityPlayerCorpse;
import ladysnake.dissolution.common.entity.minion.AbstractMinion;
import ladysnake.dissolution.common.init.ModItems;
import ladysnake.dissolution.common.inventory.DissolutionInventoryHelper;
import ladysnake.dissolution.common.inventory.InventoryPlayerCorpse;
import ladysnake.dissolution.common.items.ItemScythe;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
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

import javax.annotation.Nonnull;

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
			this.handlePlayerKill((EntityPlayer)event.getSource().getTrueSource(), event.getEntityLiving());
	}
	
	protected void handlePlayerDeath(LivingDeathEvent event) {
		
		final EntityPlayer p = (EntityPlayer) event.getEntity();
		final IIncorporealHandler corp = CapabilityIncorporealHandler.getHandler(p);
		if(corp.getCorporealityStatus().isIncorporeal() && corp.getPossessed() instanceof EntityLivingBase) {
			((EntityLivingBase) corp.getPossessed()).attackEntityFrom(event.getSource(), Float.MAX_VALUE);
			corp.setPossessed(null);
		}
		if(!corp.isStrongSoul() || corp.getCorporealityStatus() == CorporealityStatus.SOUL)
			return;
		if(corp.getCorporealityStatus() == CorporealityStatus.ECTOPLASM) {
			corp.setCorporealityStatus(CorporealityStatus.SOUL);
			p.setHealth(20f);
			event.setCanceled(true);
			return;
		}
		
		corp.setLastDeathMessage(
				p.getDisplayNameString() + event.getSource().getDeathMessage(p).getUnformattedComponentText());

		if(!p.world.isRemote) {
			final EntityPlayerCorpse body = new EntityPlayerCorpse(p.world);
			body.setPosition(p.posX, p.posY, p.posZ);
			body.setCustomNameTag(p.getName());
			body.setDecompositionCountdown(EntityPlayerCorpse.MAX_DECAY_TIME);

			boolean flag = false;
			if(event.getSource().getTrueSource() instanceof EntityPlayer) {
				EntityPlayer killer = (EntityPlayer) event.getSource().getTrueSource();
				if(!DissolutionInventoryHelper.findItem(killer, ModItems.EYE_OF_THE_UNDEAD).isEmpty())
					flag = true;
			}
			
			if(DissolutionConfig.respawn.bodiesHoldInventory) {
				
				if((flag || DissolutionConfig.respawn.bodiesHoldInventory) && !p.isSpectator() && !p.world.getGameRules().getBoolean("keepInventory")) {
					DissolutionInventoryHelper.transferEquipment(p, body);
					body.setInventory(new InventoryPlayerCorpse(p.inventory.mainInventory, body));
					p.inventory.clear();
				}
			}
			
			body.onUpdate();
			
			p.world.spawnEntity(body);
			body.setPlayer(p.getUniqueID());
		}

		if(!DissolutionConfig.respawn.wowLikeRespawn) {
			p.setSpawnPoint(new BlockPos(p), true);
		}
		
		if(DissolutionConfig.respawn.skipDeathScreen) {
			if(!p.world.isRemote)
				fakePlayerDeath((EntityPlayerMP)p, event.getSource());
			corp.setCorporealityStatus(IIncorporealHandler.CorporealityStatus.SOUL);
			p.setHealth(20f);
			if(p.getBedLocation() != null && !DissolutionConfig.respawn.respawnInNether && DissolutionConfig.respawn.wowLikeRespawn) {
				BlockPos respawnLoc = p.getBedLocation();
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
	
	private void handlePlayerKill(@Nonnull EntityPlayer killer, EntityLivingBase victim) {
		
		if (killer.getHeldItemMainhand().getItem() instanceof ItemScythe) {
			((ItemScythe) killer.getHeldItemMainhand().getItem()).harvestSoul(killer, victim);
		}

/*
		ItemStack eye = DissolutionInventoryHelper.findItem(killer, ModItems.EYE_OF_THE_UNDEAD);
		if (killer.world.rand.nextInt(1) == 0 && !eye.isEmpty() && !killer.world.isRemote) {
			AbstractMinion corpse = AbstractMinion.createMinion(victim);
			if(corpse != null) {
				DissolutionInventoryHelper.transferEquipment(victim, corpse);
				victim.world.spawnEntity(corpse);
				victim.world.removeEntity(victim);
			}
		}
*/
	}
}
