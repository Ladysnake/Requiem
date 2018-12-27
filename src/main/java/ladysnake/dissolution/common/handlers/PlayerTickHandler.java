package ladysnake.dissolution.common.handlers;

import ladylib.reflection.TypedReflection;
import ladylib.reflection.typed.TypedSetter;
import ladysnake.dissolution.api.corporeality.IIncorporealHandler;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.config.DissolutionConfigManager;
import ladysnake.dissolution.common.registries.SoulStates;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.FoodStats;
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

                // Randomly removes experience from the player
                if (playerCorp.getCorporealityStatus() == SoulStates.SOUL && playerCorp.getPossessed() == null) {
                    if (event.player.experience > 0 && rand.nextBoolean()) {
                        event.player.experience--;
                    } else if (rand.nextInt() % 300 == 0 && event.player.experienceLevel > 0) {
                        event.player.addExperienceLevel(-1);
                    }
                }
            }
        }
        if (event.side.isServer()) {
            playerCorp.setSynced(true);
        }
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
