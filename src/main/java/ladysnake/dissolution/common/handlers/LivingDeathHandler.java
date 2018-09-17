package ladysnake.dissolution.common.handlers;

import ladylib.LadyLib;
import ladylib.misc.ReflectionUtil;
import ladysnake.dissolution.api.corporeality.IIncorporealHandler;
import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.networking.FlashTransitionMessage;
import ladysnake.dissolution.common.networking.PacketHandler;
import ladysnake.dissolution.common.networking.RemnantRespawnPacket;
import ladysnake.dissolution.common.registries.SoulStates;
import net.minecraft.entity.Entity;
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
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.invoke.MethodHandle;
import java.util.List;

public class LivingDeathHandler {

    private static MethodHandle destroyVanishingCursedItems = ReflectionUtil.findMethodHandleFromObfName(EntityPlayer.class, "func_190776_cN", void.class);

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof EntityPlayer) {
            this.handlePlayerDeath(event);
        }

//        if (event.getSource().getTrueSource() instanceof EntityPlayer)
//            this.handlePlayerKill((EntityPlayer) event.getSource().getTrueSource(), event.getEntityLiving());
    }

    protected void handlePlayerDeath(LivingDeathEvent event) {
        final EntityPlayer p = (EntityPlayer) event.getEntity();
        final IIncorporealHandler corp = CapabilityIncorporealHandler.getHandler(p);
        if (corp.getCorporealityStatus().isIncorporeal() && corp.getPossessed() != null) {
            corp.getPossessed().attackEntityFrom(event.getSource(), Float.MAX_VALUE);
            corp.setPossessed(null);
        }
        if (!corp.isStrongSoul() || corp.getCorporealityStatus() == SoulStates.SOUL) {
            return;
        }
        if (corp.getCorporealityStatus() == SoulStates.ECTOPLASM) {
            corp.setCorporealityStatus(SoulStates.SOUL);
            p.setHealth(20f);
            event.setCanceled(true);
            return;
        }

        corp.getDeathStats().setDeathDimension(p.dimension);

        if (p.world instanceof WorldServer) {
            Entity killer = event.getSource().getTrueSource();
            LootTable table = p.world.getLootTableManager().getLootTableFromLocation(new ResourceLocation("dissolution:inject/human"));
            LootContext.Builder ctx = new LootContext.Builder((WorldServer) p.world)
                    .withLootedEntity(p) // set looted entity
                    .withDamageSource(event.getSource()); // pass killing blow and non-player killer
            if (killer instanceof EntityPlayer) {
                ctx = ctx
                        .withLuck(((EntityPlayer) killer).getLuck()) // adjust luck, commonly EntityPlayer.getLuck()
                        .withPlayer((EntityPlayer) killer); // set player as killer
            }
            List<ItemStack> stacks = table.generateLootForPools(p.world.rand, ctx.build());
            for (ItemStack stack : stacks) {
                p.dropItem(stack, true, true);
            }
        }

//        if (!p.world.isRemote && Dissolution.config.respawn.spawnCorpses) {
//            final EntityPlayerCorpse body = new EntityPlayerCorpse(p.world);
//            body.setPosition(p.posX, p.posY, p.posZ);
//            body.setCustomNameTag(p.getName());
//            body.setDecompositionCountdown(EntityPlayerCorpse.MAX_DECAY_TIME);
//
//            if (Dissolution.config.respawn.bodiesHoldInventory) {
//                if (Dissolution.config.respawn.bodiesHoldInventory && !p.isSpectator() && !p.world.getGameRules().getBoolean("keepInventory")) {
//                    DissolutionInventoryHelper.transferEquipment(p, body);
//                    body.setInventory(new InventoryPlayerCorpse(p.inventory.mainInventory, body));
//                    p.inventory.clear();
//                }
//            }
//
//            body.onUpdate();
//
//            p.world.spawnEntity(body);
//            body.setPlayer(p.getUniqueID());
//        }

        if (Dissolution.config.respawn.skipDeathScreen) {
            if (!p.world.isRemote) {
                EntityPlayerMP playerMP = (EntityPlayerMP) p;
                fakePlayerDeath(playerMP, event.getSource());
                PacketHandler.NET.sendTo(new FlashTransitionMessage(), playerMP);
                RemnantRespawnPacket.fakeRespawn(playerMP);
            }
            event.setCanceled(true);
        }
    }

    public static void fakePlayerDeath(EntityPlayerMP player, DamageSource cause) {
        boolean flag = player.world.getGameRules().getBoolean("showDeathMessages");

        if (flag) {
            Team team = player.getTeam();

            if (team != null && team.getDeathMessageVisibility() != Team.EnumVisible.ALWAYS) {
                if (team.getDeathMessageVisibility() == Team.EnumVisible.HIDE_FOR_OTHER_TEAMS) {
                    player.server.getPlayerList().sendMessageToAllTeamMembers(player, player.getCombatTracker().getDeathMessage());
                } else if (team.getDeathMessageVisibility() == Team.EnumVisible.HIDE_FOR_OWN_TEAM) {
                    player.server.getPlayerList().sendMessageToTeamOrAllPlayers(player, player.getCombatTracker().getDeathMessage());
                }
            } else {
                player.server.getPlayerList().sendMessage(player.getCombatTracker().getDeathMessage());
            }
        }

        if (!player.world.getGameRules().getBoolean("keepInventory") && !player.isSpectator()) {
            player.captureDrops = true;
            player.capturedDrops.clear();
            try {
                destroyVanishingCursedItems.invoke(player);
            } catch (Throwable t) {
                LadyLib.LOGGER.error("Failed to destroy vanishing items", t);
            }
            player.inventory.dropAllItems();

            net.minecraftforge.event.entity.player.PlayerDropsEvent event = new net.minecraftforge.event.entity.player.PlayerDropsEvent(player, cause, player.capturedDrops, false);
            if (!net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event)) {
                for (net.minecraft.entity.item.EntityItem item : player.capturedDrops) {
                    player.world.spawnEntity(item);
                }
            }
        }

        for (ScoreObjective scoreobjective : player.world.getScoreboard().getObjectivesFromCriteria(IScoreCriteria.DEATH_COUNT)) {
            Score score = player.getWorldScoreboard().getOrCreateScore(player.getName(), scoreobjective);
            score.incrementScore();
        }

        EntityLivingBase entitylivingbase = player.getAttackingEntity();

        if (entitylivingbase != null) {
            EntityList.EntityEggInfo entitylist$entityegginfo = EntityList.ENTITY_EGGS.get(EntityList.getKey(entitylivingbase));

            if (entitylist$entityegginfo != null) {
                player.addStat(entitylist$entityegginfo.entityKilledByStat);
            }

        }

        player.clearActivePotions();
        player.addStat(StatList.DEATHS);
        player.takeStat(StatList.TIME_SINCE_DEATH);
        player.extinguish();
        //player.setFlag(0, false);
        player.getCombatTracker().reset();
    }

}
