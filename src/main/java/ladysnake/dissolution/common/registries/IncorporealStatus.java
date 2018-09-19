package ladysnake.dissolution.common.registries;

import ladylib.misc.ReflectionUtil;
import ladysnake.dissolution.api.corporeality.IIncorporealHandler;
import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.config.DissolutionConfigManager;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.invoke.MethodHandle;

public class IncorporealStatus extends CorporealityStatus {
    private static final MethodHandle isImmuneToFireMH = ReflectionUtil.findSetterFromObfName(Entity.class, "field_70178_ae", boolean.class);

    public IncorporealStatus() {
        super(false, true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerAttackEntity(AttackEntityEvent event) {
        if (isAffected(CapabilityIncorporealHandler.getHandler(event.getEntityPlayer())) && !event.getEntityPlayer().isCreative()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        IIncorporealHandler handler = CapabilityIncorporealHandler.getHandler(event.getEntityPlayer());
        if (isAffected(handler)) {
            if (handler.getPossessed() == null && !event.getEntityPlayer().isCreative()) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        // Technically redundant with the other events but redundancy is good
        IIncorporealHandler handler = CapabilityIncorporealHandler.getHandler(event.getPlayer());
        if (isAffected(handler) && handler.getPossessedUUID() == null) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        IIncorporealHandler handler = CapabilityIncorporealHandler.getHandler(event.getEntityPlayer());
        if (isAffected(handler)) {
            Entity possessed = handler.getPossessed();
            // restores the mining speed as minecraft inflicts a penalty each time you mount something
            if (possessed != null && possessed.onGround && !event.getEntityPlayer().onGround) {
                event.setNewSpeed(event.getOriginalSpeed() * 5);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        IIncorporealHandler status = CapabilityIncorporealHandler.getHandler(event.getEntityPlayer());
        if (!event.getEntityPlayer().isCreative() && isAffected(status)) {
            if (status.getPossessed() == null
                    && this.preventsInteraction(event.getWorld().getBlockState(event.getPos()))) {
                event.setCanceled(true);
            }
        }
    }

    protected boolean preventsInteraction(IBlockState blockState) {
        return !DissolutionConfigManager.canEctoplasmInteractWith(blockState.getBlock());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerEntityInteract(PlayerInteractEvent.EntityInteract event) {
        IIncorporealHandler handler = CapabilityIncorporealHandler.getHandler(event.getEntityPlayer());
        if(isAffected(handler) && !event.getEntityPlayer().isCreative()) {
            if (this.preventsInteraction() && handler.getPossessed() == null
                    && !event.getEntityPlayer().isCreative()) {
                event.setCanceled(true);
            }
        }
    }

    protected boolean preventsInteraction() {
        return preventsEntityInteract;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerRightClickItem(PlayerInteractEvent.RightClickItem event) {
        IIncorporealHandler handler = CapabilityIncorporealHandler.getHandler(event.getEntityPlayer());
        if(isAffected(handler)) {
            if(this.preventsInteraction(event.getItemStack()) && handler.getPossessed() == null
                    && !event.getEntityPlayer().isCreative()) {
                event.setCanceled(true);
            }
        }
    }

    protected boolean preventsInteraction(ItemStack item) {
        return !DissolutionConfigManager.canEctoplasmInteractWith(item.getItem());
    }

    @Override
    public void initState(EntityPlayer owner) {
        MinecraftForge.EVENT_BUS.register(this);
        super.initState(owner);
        changeState(owner, true);
        owner.setInvisible(Dissolution.config.ghost.invisibleGhosts);
    }

    @Override
    public void resetState(EntityPlayer owner) {
        super.resetState(owner);

        changeState(owner, false);
        if (Dissolution.config.ghost.invisibleGhosts) {
            owner.setInvisible(false);
        }
    }

    protected void changeState(EntityPlayer owner, boolean init) {
        try {
            isImmuneToFireMH.invoke(owner, init);
        } catch (Throwable throwable) {
            Dissolution.LOGGER.error("Could not set player immunity to fire", throwable);
        }
    }

    protected boolean isAffected(IIncorporealHandler player) {
        return player.getCorporealityStatus() instanceof IncorporealStatus;
    }
}
