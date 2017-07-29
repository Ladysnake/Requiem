package ladysnake.dissolution.common.handlers;

import java.util.List;

import ladysnake.dissolution.common.blocks.ISoulInteractable;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.capabilities.IIncorporealHandler;
import ladysnake.dissolution.common.capabilities.SoulTypes;
import ladysnake.dissolution.common.config.DissolutionConfig;
import ladysnake.dissolution.common.entity.EntityPlayerCorpse;
import ladysnake.dissolution.common.entity.soul.EntitySoulCamera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketCamera;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

public class InteractEventsHandler {
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		if (isGhost(event) && !(event.getWorld().getBlockState(event.getPos()).getBlock() instanceof ISoulInteractable)) {
			event.setCanceled(true);
		}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPlayerLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
		String debugPrefix = null; // "[InteractEventsHandler.onPlayerLeftClickBlock] ";
		if(debugPrefix != null)
			System.out.println("");
		if(isGhost(event)) {
			event.setCanceled(true);
			
			if(!DissolutionConfig.wip.enableSoulDash) return;
			
			final IIncorporealHandler ghostHandler = CapabilityIncorporealHandler.getHandler(event.getEntityPlayer());
			ghostHandler.setIntangible(true);
			
			if(event.getSide() == Side.SERVER && false) {
				if(debugPrefix != null)
					System.out.println(debugPrefix + "player position : " + event.getEntityPlayer().getPosition());
				EntitySoulCamera mount = new EntitySoulCamera((EntityPlayerMP)event.getEntityPlayer());
				event.getWorld().spawnEntity(mount);
				Vec3i vec = event.getFace().getOpposite().getDirectionVec();
				BlockPos start = event.getEntityPlayer().getPosition();
				BlockPos dest = event.getPos();
				// first we search for a body belonging to the player in the same Y coordinates (if the player clicked in this direction)
				List<EntityPlayerCorpse> validCorpses = null;
				if(event.getFace().getAxis() == EnumFacing.Axis.Y)
					validCorpses = event.getWorld().getEntitiesWithinAABB(EntityPlayerCorpse.class, 
							new AxisAlignedBB(start.up(255), start.down(255)), 
							corpse -> corpse.getPlayer().equals(event.getEntityPlayer().getUniqueID()));
				System.out.println(debugPrefix + validCorpses);
				if(validCorpses != null && !validCorpses.isEmpty())
					dest = validCorpses.get(0).getPosition();
				else {	// if none are found, we search for an airspace in an acceptable range
					System.out.println(debugPrefix + "entering for loop " + (dest.distanceSq(start) <= 25) + " " + !event.getWorld().isAirBlock(dest) + " " + !event.getWorld().isAirBlock(dest.down()));
					for(; dest.distanceSq(start) <= 25 &&
							!event.getWorld().isAirBlock(dest) && 
							!event.getWorld().isAirBlock(dest.up()); 
							dest = dest.add(vec)) {
						System.out.println(debugPrefix + "(for loop) " + dest);
					}
				}
				System.out.println(debugPrefix + "destination: " + dest);
				if(dest.distanceSq(start) < 25) {
					mount.setDest(dest);
					/*
					mount.getNavigator().setPath(new Path(new PathPoint[] {
							new PathPoint(start.getX(), start.getY(), start.getZ()),
							new PathPoint(dest.getX(), dest.getY(), dest.getZ())}), 1.0);*/
				}
				
			}
		}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPlayerRightClickItem(PlayerInteractEvent.RightClickItem event) {
		if(isGhost(event) && !(event.getItemStack().getItem() instanceof ISoulInteractable))
			event.setCanceled(true);
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPlayerEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
		EntityPlayer player = event.getEntityPlayer();
		if(isGhost(event) && !(event.getTarget() instanceof ISoulInteractable)) {
			if(event.getTarget() instanceof EntityLiving && 
					SoulTypes.getSoulFor((EntityLiving) event.getTarget()) == SoulTypes.BENIGN && 
					event.getTarget() != event.getEntityPlayer().getRidingEntity()) {
				player.startRiding(event.getTarget(), true);
				player.eyeHeight = event.getTarget().getEyeHeight();
				player.setInvisible(true);
				if(!player.world.isRemote) {
					fakeSpectator((EntityPlayerMP)player, event.getTarget());
				}
			}
			event.setCanceled(true);
		}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPlayerEntityInteract(PlayerInteractEvent.EntityInteract event) {
		if(isGhost(event) && !(event.getTarget() instanceof ISoulInteractable))
			event.setCanceled(true);
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onEntityMount(EntityMountEvent event) {
		if(event.isMounting() && isGhost(event)) {
			
		} else if(isGhost(event)) {
			final EntityPlayer player = (EntityPlayer)event.getEntity();
			((EntityPlayer)event.getEntity()).eyeHeight = ((EntityPlayer)event.getEntity()).getDefaultEyeHeight();
			((EntityPlayer)event.getEntity()).setInvisible(DissolutionConfig.ghost.invisibleGhosts);
			if(!player.world.isRemote) {
				fakeSpectator((EntityPlayerMP)player, player);
			}
			if(event.getEntityBeingMounted() instanceof EntityLiving)
				((EntityLiving)event.getEntityBeingMounted()).setNoAI(false);
		}
	}
	
	private void fakeSpectator(EntityPlayerMP player, Entity entityToSpectate) {
        Entity spectatingEntity = (Entity)(entityToSpectate == null ? this : entityToSpectate);

		//((EntityPlayerMP)player).setSpectatingEntity(entityToSpectate);
        //player.connection.sendPacket(new SPacketCamera(spectatingEntity));
	}
	
	/**
	 * Checks if the player from the event is intangible
	 * @param event
	 * @return true if the event's entity is a non-creative player and a ghost
	 */
	private static boolean isGhost(EntityEvent event) {
		return event.getEntity() instanceof EntityPlayer && 
				CapabilityIncorporealHandler.getHandler((EntityPlayer) event.getEntity()).isIncorporeal() && 
				!((EntityPlayer)event.getEntity()).isCreative();
	}
	
	/**
	 * Same as {@link #isGhost(EntityEvent)} except optimized for player events.
	 */
	private static boolean isGhost(PlayerEvent event) {
		return CapabilityIncorporealHandler.getHandler(event.getEntityPlayer()).isIncorporeal() && !event.getEntityPlayer().isCreative();
	}

}
