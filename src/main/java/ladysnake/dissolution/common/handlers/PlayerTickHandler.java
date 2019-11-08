package ladysnake.dissolution.common.handlers;

import ladylib.reflection.TypedReflection;
import ladylib.reflection.typed.TypedSetter;
import ladysnake.dissolution.api.corporeality.IIncorporealHandler;
import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.config.DissolutionConfigManager;
import ladysnake.dissolution.common.registries.SoulStates;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketSetExperience;
import net.minecraft.util.FoodStats;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class PlayerTickHandler {

    static Set<EntityPlayer> sneakingPossessingPlayers = new HashSet<>();

    protected static final Random rand = new Random();
    private static TypedSetter<FoodStats, Integer> foodTimer = TypedReflection.findSetter(FoodStats.class, "field_75123_d", int.class);
    private static TypedSetter<FoodStats, Float> foodExhaustionLevel = TypedReflection.findSetter(FoodStats.class, "field_75126_c", float.class);
    private static TypedSetter<EntityPlayer, Integer> flyToggleTimer = TypedReflection.findSetter(EntityPlayer.class, "field_71101_bC", int.class);

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent event) {
        if (sneakingPossessingPlayers.remove(event.player)) {
            event.player.setSneaking(true);
        }
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        final IIncorporealHandler playerCorp = CapabilityIncorporealHandler.getHandler(event.player);
        if (playerCorp.isStrongSoul()) {
            if (playerCorp.getCorporealityStatus().isIncorporeal()) {
                if (!event.player.isCreative() && playerCorp.getCorporealityStatus() == SoulStates.SOUL &&
                        !playerCorp.isPossessionActive()) {
                    handleSoulFlight(event.player);
                }

                try {
                    foodTimer.set(event.player.getFoodStats(), 0);
                    foodExhaustionLevel.set(event.player.getFoodStats(), 0f);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }

                if (event.side.isClient()) {
                    return;
                }

                // Randomly remove experience from the player
                if (playerCorp.getCorporealityStatus() == SoulStates.SOUL && !playerCorp.isPossessionActive()) {
                    if (rand.nextInt(10) == 0) {
                        int removedXp = removeXp(event.player, 1);
                        ((EntityPlayerMP)event.player).connection.sendPacket(new SPacketSetExperience(event.player.experience, event.player.experienceTotal, event.player.experienceLevel));
                        if (removedXp <= 0 && (event.player.world.getMinecraftServer().isHardcore() || Dissolution.config.ghost.dieFromExperience)) {
                            event.player.setHealth(0f);
                            event.player.sendStatusMessage(new TextComponentTranslation("dissolution.message.out_of_xp_death"), false);
                            playerCorp.setStrongSoul(false);
                            event.player.attackEntityFrom(Dissolution.OUT_OF_XP, Float.POSITIVE_INFINITY);
                        }
                    }
                }
            }
        }
        if (event.side.isServer()) {
            playerCorp.setSynced(true);
        }
    }

    /**
     * Removes XP from a player.
     * Taken from The Betweenlands' <a href="https://github.com/Angry-Pixel/The-Betweenlands/blob/1.12-dev/src/main/java/thebetweenlands/common/item/equipment/ItemRing.java">source code</a>
     *
     * @author TheCyberbrick
     * @param player the player whose xp will be removed
     * @param amount the amount of experience points to remove
     * @return the amount of xp that was actually removed
     */
    public static int removeXp(EntityPlayer player, int amount) {
        int startAmount = amount;
        while(amount > 0) {
            int barCap = player.xpBarCap();
            int barXp = (int) (barCap * player.experience);
            int removeXp = Math.min(barXp, amount);
            int newBarXp = barXp - removeXp;
            amount -= removeXp;
            player.experienceTotal -= removeXp;
            if(player.experienceTotal < 0) {
                player.experienceTotal = 0;
            }
            if(newBarXp == 0 && amount > 0) {
                player.experienceLevel--;
                if(player.experienceLevel < 0) {
                    player.experienceLevel = 0;
                    player.experienceTotal = 0;
                    player.experience = 0;
                    break;
                } else {
                    player.experience = 1.0F;
                }
            } else {
                player.experience = newBarXp / (float) barCap;
            }
        }
        return startAmount - amount;
    }

    /**
     * Sets the player's motion and capabilities according to its soul status and the current configuration
     */
    private void handleSoulFlight(EntityPlayer player) {
        if (player.getRidingEntity() != null) {
            return;
        }

        if (DissolutionConfigManager.isFlightSetTo(DissolutionConfigManager.FlightModes.SPECTATOR_FLIGHT)
                || DissolutionConfigManager.isFlightSetTo(DissolutionConfigManager.FlightModes.CUSTOM_FLIGHT)) {
            player.capabilities.isFlying = true;
        }
        if (DissolutionConfigManager.isFlightSetTo(DissolutionConfigManager.FlightModes.CUSTOM_FLIGHT)) {
            player.onGround = false;
        }
//		if (DissolutionConfigManager.isFlightEnabled(FlightModes.SPECTATOR_FLIGHT)
//				|| DissolutionConfigManager.isFlightEnabled(FlightModes.CREATIVE_FLIGHT))
        if (!DissolutionConfigManager.isFlightSetTo(DissolutionConfigManager.FlightModes.NO_FLIGHT)) {
            player.capabilities.allowFlying = true;
        }
        if (DissolutionConfigManager.isFlightSetTo(DissolutionConfigManager.FlightModes.CUSTOM_FLIGHT)) {
            flyToggleTimer.set(player, 0);
        }
    }

}
