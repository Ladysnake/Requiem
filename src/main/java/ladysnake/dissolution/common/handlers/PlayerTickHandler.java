package ladysnake.dissolution.common.handlers;

import java.util.Random;

import ibxm.Player;
import ladysnake.dissolution.common.DissolutionConfig;
import ladysnake.dissolution.common.capabilities.IIncorporealHandler;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.networking.IncorporealMessage;
import ladysnake.dissolution.common.networking.PacketHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;

public class PlayerTickHandler {

	protected static final Random rand = new Random();
	public static final int SPAWN_RADIUS_FROM_ORIGIN = 10;
	protected int ticksSpentNearSpawn = 0;
	private static float accelerationX = 0;
	private static float accelerationY = 0;
	private static float accelerationZ = 0;

	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent event) {

		
		final IIncorporealHandler playerCorp = CapabilityIncorporealHandler.getHandler(event.player);
		
		
		playerCorp.tick(event);
				
		if (playerCorp.isIncorporeal()) {
			if(!event.player.isCreative()) {
				if (DissolutionConfig.flightMode == DissolutionConfig.SPECTATOR_FLIGHT || DissolutionConfig.flightMode == DissolutionConfig.CUSTOM_FLIGHT)
					event.player.capabilities.isFlying = event.player.experienceLevel > 0;
				if(DissolutionConfig.flightMode == DissolutionConfig.CUSTOM_FLIGHT && event.player.experienceLevel > 0) {
					event.player.onGround = false;
					event.player.capabilities.setFlySpeed(event.player.experienceLevel > 0 ? 0.025f : 0.01f);
				}
				else if (DissolutionConfig.flightMode == DissolutionConfig.CREATIVE_FLIGHT && event.player.experienceLevel <= 0)
					event.player.capabilities.isFlying = false;
				if (DissolutionConfig.flightMode == DissolutionConfig.SPECTATOR_FLIGHT
						|| DissolutionConfig.flightMode == DissolutionConfig.CREATIVE_FLIGHT)
					event.player.capabilities.allowFlying = event.player.experienceLevel > 0;
			}

			if (event.side.isClient())
				return;

			// Makes the player tangible if he is near 0,0
			if (event.player.getDistance(0, event.player.posY, 0) < SPAWN_RADIUS_FROM_ORIGIN
					&& ++ticksSpentNearSpawn >= 100) {
				playerCorp.setIncorporeal(false, event.player);

				for (int i = 0; i < 50; i++) {
					double motionX = rand.nextGaussian() * 0.02D;
					double motionY = rand.nextGaussian() * 0.02D + 1;
					double motionZ = rand.nextGaussian() * 0.02D;
					((WorldServer) event.player.world).spawnParticle(EnumParticleTypes.CLOUD, false,
							event.player.posX + 0.5D, event.player.posY + 1.0D, event.player.posZ + 0.5D, 1, 0.3D, 0.3D,
							0.3D, 0.0D, new int[0]);
				}
				
				if (event.player.dimension == -1 && DissolutionConfig.respawnInNether) {
					BlockPos spawnPos = event.player.getBedLocation(event.player.getSpawnDimension());
					if(spawnPos == null)
						spawnPos = event.player.world.getMinecraftServer().getWorld(0).getSpawnPoint();
					event.player.setPosition(spawnPos.getX() / 8, spawnPos.getY() / 8, spawnPos.getZ() / 8);
					CustomTartarosTeleporter.transferPlayerToDimension((EntityPlayerMP) event.player,
							event.player.getSpawnDimension());
				}
				ticksSpentNearSpawn = 0;
			}
			if (event.player.experience > 0 && rand.nextBoolean())
				event.player.experience--;
			else if (rand.nextInt() % 300 == 0 && event.player.experienceLevel > 0)
				event.player.addExperienceLevel(-1);


				if (!playerCorp.isSynced() && !event.player.world.isRemote
						&& DissolutionConfig.respawnInNether) {
					CustomTartarosTeleporter.transferPlayerToDimension((EntityPlayerMP) event.player, -1);
				}
		}
		if(event.side.isServer())
			playerCorp.setSynced(true);
	}
}
