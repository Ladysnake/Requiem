package ladysnake.dissolution.common.handlers;

import ladysnake.dissolution.api.IIncorporealHandler;
import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.config.DissolutionConfig;
import ladysnake.dissolution.common.config.DissolutionConfigManager;
import ladysnake.dissolution.common.config.DissolutionConfigManager.FlightModes;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.capabilities.EctoplasmStats;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.FoodStats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.ReflectionHelper.UnableToFindFieldException;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class PlayerTickHandler {

	static Set<EntityPlayer> sneakingPossessingPlayers = new HashSet<>();

	protected static final Random rand = new Random();
	private static final int SPAWN_RADIUS_FROM_ORIGIN = 10;
	private static MethodHandle foodTimer, foodExhaustionLevel, flyToggleTimer;

	private int ticksSpentNearSpawn = 0;
	
	static {
		try {
			Field field = ReflectionHelper.findField(FoodStats.class, "foodTimer", "field_75123_d");
			foodTimer = MethodHandles.lookup().unreflectSetter(field);
			field = ReflectionHelper.findField(FoodStats.class, "foodExhaustionLevel", "field_75126_c");
			foodExhaustionLevel = MethodHandles.lookup().unreflectSetter(field);
			field = ReflectionHelper.findField(EntityPlayer.class, "flyToggleTimer", "field_71101_bC");
			flyToggleTimer = MethodHandles.lookup().unreflectSetter(field);
		} catch (UnableToFindFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent event) {
		if(sneakingPossessingPlayers.remove(event.player))
			event.player.setSneaking(true);
		if(event.phase != TickEvent.Phase.END) return;
		final IIncorporealHandler playerCorp = CapabilityIncorporealHandler.getHandler(event.player);
		if(playerCorp.isStrongSoul()) {
			if (playerCorp.getCorporealityStatus().isIncorporeal()) {
				if (!event.player.isCreative() &&
						(playerCorp.getCorporealityStatus() == IIncorporealHandler.CorporealityStatus.SOUL
								|| playerCorp.getEctoplasmStats().getActiveSpells().contains(EctoplasmStats.SoulSpells.FLIGHT)))
					handleSoulFlight(event.player);

				handlePossessingTick(event.player);

				try {
					foodTimer.invokeExact(event.player.getFoodStats(), 0);
					foodExhaustionLevel.invokeExact(event.player.getFoodStats(), 0f);
				} catch (Throwable throwable) {
					throwable.printStackTrace();
				}

				if (event.side.isClient())
					return;

				// Makes the player tangible if he is near 0,0
				if (event.player.getDistance(0, event.player.posY, 0) < SPAWN_RADIUS_FROM_ORIGIN
						&& ++ticksSpentNearSpawn >= 100) {
					respawnPlayerOrigin(event.player);
				}

				// Randomly removes experience from the player
				if (playerCorp.getCorporealityStatus() == IIncorporealHandler.CorporealityStatus.SOUL) {
					if (event.player.experience > 0 && rand.nextBoolean())
						event.player.experience--;
					else if (rand.nextInt() % 300 == 0 && event.player.experienceLevel > 0)
						event.player.addExperienceLevel(-1);
				}
			}
			// Teleports the player to wherever they should be if needed
			if (playerCorp.getDeathStats().wasDead() && !event.player.world.isRemote) {
				event.player.world.profiler.startSection("placing_respawned_player");
				// changes the player's dimension if required by the config or if they died there
				if (DissolutionConfig.respawn.respawnInNether)
					CustomDissolutionTeleporter.transferPlayerToDimension((EntityPlayerMP) event.player, DissolutionConfig.respawn.respawnDimension);
				else if (!DissolutionConfig.respawn.wowLikeRespawn && event.player.dimension != playerCorp.getDeathStats().getDeathDimension())
					CustomDissolutionTeleporter.transferPlayerToDimension((EntityPlayerMP) event.player, playerCorp.getDeathStats().getDeathDimension());

				// changes the player's position to where they died
				if(!DissolutionConfig.respawn.wowLikeRespawn) {
					Vec3d deathLoc = playerCorp.getDeathStats().getDeathLocation();
					BlockPos deathPos = new BlockPos(deathLoc);
					if (!event.player.world.isOutsideBuildHeight(deathPos) && event.player.world.isAirBlock(deathPos))
						((EntityPlayerMP)event.player).connection.setPlayerLocation(deathLoc.x, deathLoc.y, deathLoc.z, event.player.rotationYaw, event.player.rotationPitch);
					else if (!event.player.world.isOutsideBuildHeight(deathPos)) {
						deathPos = getSafeSpawnLocation(event.player.world, deathPos, 0);
						if(deathPos != null)
							((EntityPlayerMP)event.player).connection.setPlayerLocation(deathPos.getX(), deathPos.getY(), deathPos.getZ(), event.player.rotationYaw, event.player.rotationPitch);
					}
				}
				event.player.world.profiler.endSection();
				playerCorp.getDeathStats().setDead(false);
			}
		}
		if(event.side.isServer())
			playerCorp.setSynced(true);
	}

	@Nullable
	private BlockPos getSafeSpawnLocation(World worldIn, BlockPos pos, int tries) {
		int i = pos.getX();
		int j = pos.getY();
		int k = pos.getZ();

		for (int l = 0; l <= 1; ++l) {
			int i1 = i - 1;
			int j1 = k - 1;
			int k1 = i1 + 2;
			int l1 = j1 + 2;

			for (int i2 = i1; i2 <= k1; ++i2) {
				for (int j2 = j1; j2 <= l1; ++j2) {
					BlockPos blockpos = new BlockPos(i2, j, j2);

					if (hasRoomForPlayer(worldIn, blockpos)) {
						if (tries <= 0) {
							return blockpos;
						}
						--tries;
					}
				}
			}
		}
		return null;
	}

	protected boolean hasRoomForPlayer(World worldIn, BlockPos pos) {
		return !worldIn.getBlockState(pos).getMaterial().isSolid() && !worldIn.getBlockState(pos.up()).getMaterial().isSolid();
	}
	
	/**
	 * Sets the player's motion and capabilities according to its soul status and the current configuration
	 */
	private void handleSoulFlight(EntityPlayer player) {
		if(player.getRidingEntity() != null) return;

		if (DissolutionConfigManager.isFlightSetTo(FlightModes.SPECTATOR_FLIGHT)
				|| DissolutionConfigManager.isFlightSetTo(FlightModes.CUSTOM_FLIGHT))
			player.capabilities.isFlying = true;
		if(DissolutionConfigManager.isFlightSetTo(FlightModes.CUSTOM_FLIGHT)) {
			player.onGround = false;
		}
//		if (DissolutionConfigManager.isFlightEnabled(FlightModes.SPECTATOR_FLIGHT)
//				|| DissolutionConfigManager.isFlightEnabled(FlightModes.CREATIVE_FLIGHT))
		if(!DissolutionConfigManager.isFlightSetTo(FlightModes.NO_FLIGHT))
			player.capabilities.allowFlying = true;
		if(DissolutionConfigManager.isFlightSetTo(FlightModes.CUSTOM_FLIGHT)) {
			try {
				flyToggleTimer.invokeExact(player, 0);
			} catch (Throwable throwable) {
				Dissolution.LOGGER.error("an error occurred while handling soul flight", throwable);
			}
		}
	}

	/**
	 * Handles movement for the entity this player is possessing
	 */
	private void handlePossessingTick(EntityPlayer player) {
		Entity possessed = player.getRidingEntity();
		if(possessed != null) {
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
//				possessed.moveRelative(player.moveStrafing, 0.5f, player.moveForward, 0.02f);
//				float f1 = MathHelper.sin(player.rotationYaw * 0.017453292F);
//	            float f2 = MathHelper.cos(player.rotationYaw * 0.017453292F);
//	            BlockPos target = rayTrace(player).getBlockPos();
//	            if(target != null)
//					((EntityLiving)possessed).getNavigator().tryMoveToXYZ(target.getX(), target.getY(), target.getZ(), 1);
			}
		}
	}
	
	private RayTraceResult rayTrace(EntityPlayer player)
    {
        Vec3d vec3d = player.getPositionEyes(1.0f);
        Vec3d vec3d1 = player.getLook(1.0f);
        Vec3d vec3d2 = vec3d.addVector(vec3d1.x * 100, vec3d1.y * 100, vec3d1.z * 100);
        return player.world.rayTraceBlocks(vec3d, vec3d2, false, false, true);
    }
	
	/**
	 * Makes the player tangible and runs some logic specific to Origin respawn
	 */
	private void respawnPlayerOrigin(EntityPlayer player) {
		if(player.world.isRemote || !DissolutionConfig.respawn.wowLikeRespawn)
			return;
		
		CapabilityIncorporealHandler.getHandler(player).setCorporealityStatus(IIncorporealHandler.CorporealityStatus.BODY);

		((WorldServer) player.world).spawnParticle(EnumParticleTypes.CLOUD, false,
				player.posX + 0.5D, player.posY + 1.0D, player.posZ + 0.5D, 50, 0.3D, 0.3D,
				0.3D, 0.01D);

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
