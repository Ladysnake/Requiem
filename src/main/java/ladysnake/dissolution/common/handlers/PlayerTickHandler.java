package ladysnake.dissolution.common.handlers;

import java.lang.reflect.Field;
import java.util.Random;

import ladysnake.dissolution.api.IIncorporealHandler;
import ladysnake.dissolution.common.DissolutionConfig;
import ladysnake.dissolution.common.DissolutionConfigManager;
import ladysnake.dissolution.common.DissolutionConfigManager.FlightModes;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.FoodStats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.ReflectionHelper.UnableToFindFieldException;

public class PlayerTickHandler {

	protected static final Random rand = new Random();
	public static final int SPAWN_RADIUS_FROM_ORIGIN = 10;
	private static Field foodTimer, foodExhaustionLevel;

	protected int ticksSpentNearSpawn = 0;
	
	static {
		try {
			foodTimer = ReflectionHelper.findField(FoodStats.class, "foodTimer", "field_75123_d");
			foodExhaustionLevel = ReflectionHelper.findField(FoodStats.class, "foodExhaustionLevel", "field_75126_c");
		} catch (UnableToFindFieldException e) {
			e.printStackTrace();
		}
	}

	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent event) {
		final IIncorporealHandler playerCorp = CapabilityIncorporealHandler.getHandler(event.player);
		
		if (playerCorp.isIncorporeal()) {
			
			if(!event.player.isCreative())
				handleSoulFlight(event.player);
			
			handlePossessingTick(event.player);
			
			try {
				foodTimer.setInt(event.player.getFoodStats(), 20);
				foodExhaustionLevel.setFloat(event.player.getFoodStats(), 0f);
			} catch (IllegalArgumentException | IllegalAccessException | NullPointerException e) {
				e.printStackTrace();
			}

			if (event.side.isClient())
				return;
			
			// Makes the player tangible if he is near 0,0
			if (event.player.getDistance(0, event.player.posY, 0) < SPAWN_RADIUS_FROM_ORIGIN
					&& ++ticksSpentNearSpawn >= 100) {
				respawnPlayerOrigin(event.player);
			}
			
			// Randomly removes experience from the player
			if (event.player.experience > 0 && rand.nextBoolean())
				event.player.experience--;
			else if (rand.nextInt() % 300 == 0 && event.player.experienceLevel > 0)
				event.player.addExperienceLevel(-1);

			// Teleports the player to the nether if needed
			if (!playerCorp.isSynced() && !event.player.world.isRemote
					&& DissolutionConfig.respawn.respawnInNether) {
				CustomDissolutionTeleporter.transferPlayerToDimension((EntityPlayerMP) event.player, DissolutionConfig.respawn.respawnDimension);
			}
		}
		if(event.side.isServer())
			playerCorp.setSynced(true);
	}
	
	/**
	 * Sets the player's motion and capabilities according to its soul status and the current configuration
	 * @param player
	 */
	private void handleSoulFlight(EntityPlayer player) {
		if(player.getRidingEntity() != null) return;
		
		if (DissolutionConfigManager.isFlightEnabled(FlightModes.SPECTATOR_FLIGHT) || DissolutionConfigManager.isFlightEnabled(FlightModes.CUSTOM_FLIGHT))
			player.capabilities.isFlying = player.experienceLevel > 0;
		if(DissolutionConfigManager.isFlightEnabled(FlightModes.CUSTOM_FLIGHT) && player.experienceLevel > 0) {
			player.onGround = false;
		}
		else if (DissolutionConfigManager.isFlightEnabled(FlightModes.CREATIVE_FLIGHT) && player.experienceLevel <= 0)
			player.capabilities.isFlying = false;
		if (DissolutionConfigManager.isFlightEnabled(FlightModes.SPECTATOR_FLIGHT)
				|| DissolutionConfigManager.isFlightEnabled(FlightModes.CREATIVE_FLIGHT))
			player.capabilities.allowFlying = player.experienceLevel > 0;
	}
	
	/**
	 * Handles movement for the entity this player is possessing
	 * @param player
	 */
	private void handlePossessingTick(EntityPlayer player) {
		if(player.isRiding()) {
			Entity possessed = player.getRidingEntity();
			if(!player.world.isRemote) {
				possessed.rotationYaw = player.rotationYaw;
				possessed.prevRotationYaw = possessed.rotationYaw;
				possessed.setRotationYawHead(player.getRotationYawHead());
			}
			possessed.rotationPitch = player.rotationPitch;
			possessed.prevRotationPitch = possessed.rotationPitch;
			if(possessed instanceof EntityLiving) {
				((EntityLiving)possessed).cameraPitch = player.cameraPitch;
				((EntityLiving) possessed).randomYawVelocity = 0;
				((EntityLiving)possessed).moveRelative(player.moveStrafing, 0.5f, player.moveForward, 0.02f);
				float f1 = MathHelper.sin(player.rotationYaw * 0.017453292F);
	            float f2 = MathHelper.cos(player.rotationYaw * 0.017453292F);
	            BlockPos target = rayTrace(player,100, 1.0f).getBlockPos();
				((EntityLiving)possessed).getNavigator().tryMoveToXYZ(target.getX(), target.getY(), target.getZ(), 1);
			}
			//System.out.println(event.side + " " + possessed);
		}
	}
	
	private RayTraceResult rayTrace(EntityPlayer player, double blockReachDistance, float partialTicks)
    {
        Vec3d vec3d = player.getPositionEyes(partialTicks);
        Vec3d vec3d1 = player.getLook(partialTicks);
        Vec3d vec3d2 = vec3d.addVector(vec3d1.x * blockReachDistance, vec3d1.y * blockReachDistance, vec3d1.z * blockReachDistance);
        return player.world.rayTraceBlocks(vec3d, vec3d2, false, false, true);
    }
	
	/**
	 * Makes the player tangible and runs some logic specific to Origin respawn
	 * @param player
	 */
	private void respawnPlayerOrigin(EntityPlayer player) {
		if(player.world.isRemote)
			return;
		
		CapabilityIncorporealHandler.getHandler(player).setIncorporeal(false);

		for (int i = 0; i < 50; i++) {
			double motionX = rand.nextGaussian() * 0.02D;
			double motionY = rand.nextGaussian() * 0.02D + 1;
			double motionZ = rand.nextGaussian() * 0.02D;
			((WorldServer) player.world).spawnParticle(EnumParticleTypes.CLOUD, false,
					player.posX + 0.5D, player.posY + 1.0D, player.posZ + 0.5D, 1, 0.3D, 0.3D,
					0.3D, 0.0D, new int[0]);
		}
		
		if (player.dimension == -1 && DissolutionConfig.respawn.respawnInNether) {
			BlockPos spawnPos = player.getBedLocation(player.getSpawnDimension());
			if(spawnPos == null)
				spawnPos = player.world.getMinecraftServer().getWorld(0).getSpawnPoint();
			player.setPosition(spawnPos.getX() / 8, spawnPos.getY() / 8, spawnPos.getZ() / 8);
			CustomDissolutionTeleporter.transferPlayerToDimension((EntityPlayerMP) player,
					player.getSpawnDimension());
		}
		ticksSpentNearSpawn = 0;
	}
}
