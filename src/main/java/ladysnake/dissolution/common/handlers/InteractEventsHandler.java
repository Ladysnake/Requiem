package ladysnake.dissolution.common.handlers;

import com.google.common.collect.ImmutableSet;
import ladysnake.dissolution.api.IIncorporealHandler;
import ladysnake.dissolution.api.IPossessable;
import ladysnake.dissolution.api.ISoulInteractable;
import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.config.DissolutionConfigManager;
import ladysnake.dissolution.common.entity.EntityRunicCircle;
import ladysnake.dissolution.common.entity.minion.AbstractMinion;
import ladysnake.dissolution.common.entity.souls.AbstractSoul;
import ladysnake.dissolution.common.inventory.DissolutionInventoryHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.BlockGravel;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.GetCollisionBoxesEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Iterator;
import java.util.Set;

public class InteractEventsHandler {
    private static final Set<Block> SOIL_BLOCKS = ImmutableSet.of(Blocks.DIRT, Blocks.GRASS, Blocks.SAND, Blocks.GRAVEL);

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        IIncorporealHandler status = CapabilityIncorporealHandler.getHandler(event.getEntityPlayer());
        if (!event.getEntityPlayer().isCreative()
                && status.getPossessed() == null
                && (status.getCorporealityStatus() == IIncorporealHandler.CorporealityStatus.SOUL
                || (status.getCorporealityStatus() == IIncorporealHandler.CorporealityStatus.ECTOPLASM
                && !DissolutionConfigManager.canEctoplasmInteractWith(event.getWorld().getBlockState(event.getPos()).getBlock())))) {
            event.setCanceled(true);
        } else if(event.getEntityPlayer().getHeldItem(event.getHand()).getItem() == Items.STICK && event.getFace() == EnumFacing.UP) {
            IBlockState state = event.getWorld().getBlockState(event.getPos());
            if(SOIL_BLOCKS.contains(state.getBlock())) {
                EntityRunicCircle runicCircle = new EntityRunicCircle(event.getWorld());
                event.getWorld().spawnEntity(runicCircle);
                //TODO open the gui for rune drawing
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (isGhost(event)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onGetCollisionBoxes(GetCollisionBoxesEvent event) {
        if (event.getEntity() instanceof AbstractSoul
                || event.getEntity() instanceof EntityPlayer
                && CapabilityIncorporealHandler.getHandler((EntityPlayer) event.getEntity())
                .getCorporealityStatus() == IIncorporealHandler.CorporealityStatus.SOUL) {
            final Iterator<AxisAlignedBB> iterator = event.getCollisionBoxesList().iterator();
            while (iterator.hasNext())
                if (iterator.next().getAverageEdgeLength() < Dissolution.config.ghost.maxThickness)
                    iterator.remove();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (isGhost(event)
                && !DissolutionConfigManager.canEctoplasmInteractWith(event.getItemStack().getItem()))
            event.setCanceled(true);
    }

    /**
     * Allows possession start
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if (!isGhost(event)) return;
        event.setCanceled(true);
        if (event.getSide().isServer() && event.getTarget() instanceof EntityLivingBase && !event.getTarget().getIsInvulnerable()) {
            IIncorporealHandler handler = CapabilityIncorporealHandler.getHandler(event.getEntityPlayer());
            IPossessable host = AbstractMinion.createMinion((EntityLivingBase) event.getTarget());
            if (host instanceof EntityLivingBase && host.canBePossessedBy(event.getEntityPlayer())) {
                EntityLivingBase eHost = (EntityLivingBase) host;
                if (((EntityLivingBase) event.getTarget()).getHeldItemMainhand().getItem() instanceof ItemBow)
                    event.getEntityPlayer().addItemStackToInventory(new ItemStack(Items.ARROW, eHost.world.rand.nextInt(10) + 2));
                DissolutionInventoryHelper.transferEquipment((EntityLivingBase) event.getTarget(), event.getEntityPlayer());
                if (host != event.getTarget()) {
                    event.getTarget().setPosition(0, -100, 0);
                    event.getTarget().world.spawnEntity(eHost);
                    event.getTarget().world.removeEntity(event.getTarget());
                }
                handler.setPossessed(host);
                event.setCancellationResult(EnumActionResult.SUCCESS);
            }
        }
    }

    @SubscribeEvent
    public void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        IIncorporealHandler handler = CapabilityIncorporealHandler.getHandler(event.getEntityPlayer());
        if (handler.getCorporealityStatus().isIncorporeal() && handler.getPossessed() != null)
            event.setNewSpeed(event.getOriginalSpeed() * 5);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (isGhost(event) && !(event.getTarget() instanceof ISoulInteractable)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onItemUseStart(LivingEntityUseItemEvent.Start event) {
        final IIncorporealHandler handler = CapabilityIncorporealHandler.getHandler(event.getEntity());
        if (handler != null && handler.getPossessed() instanceof EntityLivingBase) {        // synchronizes item use between player and possessed entity
            ((EntityLivingBase) handler.getPossessed()).setActiveHand(event.getEntityLiving().getHeldItemMainhand().equals(event.getItem()) ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND);
        }
    }

    @SubscribeEvent
    public void onItemUseTick(LivingEntityUseItemEvent.Tick event) {
        if (event.getDuration() <= 1) {            // prevent the player from finishing the item use if possessing an entity
            final IIncorporealHandler handler = CapabilityIncorporealHandler.getHandler(event.getEntity());
            if (handler != null && handler.getPossessed() instanceof EntityLivingBase) {
                event.getEntityLiving().resetActiveHand();
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onItemUseStop(LivingEntityUseItemEvent.Stop event) {
        final IIncorporealHandler handler = CapabilityIncorporealHandler.getHandler(event.getEntity());
        if (handler != null && handler.getPossessed() instanceof EntityLivingBase) {
            ((EntityLivingBase) handler.getPossessed()).stopActiveHand();
            event.setCanceled(true);            // prevent the player from duplicating the action
        }
    }

    /**
     * Prevents a player from ending possession prematurely
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntityMount(EntityMountEvent event) {
        IIncorporealHandler handler = CapabilityIncorporealHandler.getHandler(event.getEntity());
        if (!event.isMounting() && handler != null && handler.getCorporealityStatus().isIncorporeal()) {
            if ((event.getEntity() instanceof EntityPlayer && ((EntityPlayer) event.getEntity()).isCreative()))
                handler.setPossessed(null);
            else {
                if (event.getEntityBeingMounted() == handler.getPossessed() && !(handler.setPossessed(null))) {
                    if (event.getEntity().isSneaking() && event.getEntity() instanceof EntityPlayer)
                        PlayerTickHandler.sneakingPossessingPlayers.add((EntityPlayer) event.getEntity());
                    event.setCanceled(true);
                }
            }
        }
    }

    /**
     * Checks if the player from the event is intangible
     *
     * @return true if the event's entity is a non-creative player and a ghost
     */
    private static boolean isGhost(PlayerEvent event) {
        IIncorporealHandler handler = CapabilityIncorporealHandler.getHandler(event.getEntityPlayer());
        return handler.getCorporealityStatus().isIncorporeal() && handler.getPossessed() == null
                && !event.getEntityPlayer().isCreative();
    }
}
