package ladysnake.dissolution.common.registries;

import ladysnake.dissolution.api.corporeality.IIncorporealHandler;
import ladysnake.dissolution.api.corporeality.ISoulInteractable;
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
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

public class IncorporealStatus extends CorporealityStatus {
    private static final MethodHandle isImmuneToFireMH;

    static {
        MethodHandle temp;
        try {
            Field field = ReflectionHelper.findField(Entity.class, "field_70178_ae", "isImmuneToFire");
            temp = MethodHandles.lookup().unreflectSetter(field);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            temp = null;
        }
        isImmuneToFireMH = temp;
    }

    public IncorporealStatus() {
        super(false, true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerAttackEntity(AttackEntityEvent event) {
        if (subscribedPlayers.contains(event.getEntityPlayer()))
            event.setCanceled(!DissolutionConfigManager.canEctoplasmBeAttackedBy(event.getTarget()));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (subscribedPlayers.contains(event.getEntityPlayer())) {
            IIncorporealHandler handler = CapabilityIncorporealHandler.getHandler(event.getEntityPlayer());
            if (handler.getPossessed() == null
                    && !event.getEntityPlayer().isCreative()) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (subscribedPlayers.contains(event.getEntityPlayer())) {
            IIncorporealHandler handler = CapabilityIncorporealHandler.getHandler(event.getEntityPlayer());
            if (handler.getPossessed() != null)
                event.setNewSpeed(event.getOriginalSpeed() * 5);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getEntityPlayer().isCreative() && this.subscribedPlayers.contains(event.getEntityPlayer())) {
            IIncorporealHandler status = CapabilityIncorporealHandler.getHandler(event.getEntityPlayer());
            if (status.getPossessed() == null
                    && this.preventsInteraction(event.getWorld().getBlockState(event.getPos()))) {
                event.setCanceled(true);
            }
        }
    }

    protected boolean preventsInteraction(IBlockState blockState) {
        return true;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if(this.subscribedPlayers.contains(event.getEntityPlayer()) && !event.getEntityPlayer().isCreative()) {
            IIncorporealHandler handler = CapabilityIncorporealHandler.getHandler(event.getEntityPlayer());
            if (this.preventsInteraction(event.getTarget()) && handler.getPossessed() == null
                    && !event.getEntityPlayer().isCreative() && !(event.getTarget() instanceof ISoulInteractable)) {
                event.setCanceled(true);
            }
        }
    }

    protected boolean preventsInteraction(Entity entity) {
        return preventsEntityInteract;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if(this.subscribedPlayers.contains(event.getEntityPlayer())) {
            IIncorporealHandler handler = CapabilityIncorporealHandler.getHandler(event.getEntityPlayer());
            if(this.preventsInteraction(event.getItemStack()) && handler.getPossessed() == null
                    && !event.getEntityPlayer().isCreative())
                event.setCanceled(true);
        }
    }

    protected boolean preventsInteraction(ItemStack item) {
        return !DissolutionConfigManager.canEctoplasmInteractWith(item.getItem());
    }

    @Override
    public void initState(EntityPlayer owner) {
        // if it's the first player in this state, we need to subscribe this as an event handler
        if (this.subscribedPlayers.isEmpty())
            MinecraftForge.EVENT_BUS.register(this);
        super.initState(owner);
        changeState(owner, true);
        owner.setInvisible(Dissolution.config.ghost.invisibleGhosts);
    }

    @Override
    public void resetState(EntityPlayer owner) {
        super.resetState(owner);
        // to avoid unnecessary event handling, we unregister this when no one is in this status
        if (this.subscribedPlayers.isEmpty())
            MinecraftForge.EVENT_BUS.register(this);

        changeState(owner, false);
        if (Dissolution.config.ghost.invisibleGhosts)
            owner.setInvisible(false);
    }

    protected void changeState(EntityPlayer owner, boolean init) {
        try {
            if (isImmuneToFireMH != null)
                isImmuneToFireMH.invoke(owner, init);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        if (!owner.isCreative()) {
            boolean enableFlight = (!DissolutionConfigManager.isFlightSetTo(DissolutionConfigManager.FlightModes.NO_FLIGHT)) && (!DissolutionConfigManager.isFlightSetTo(DissolutionConfigManager.FlightModes.CUSTOM_FLIGHT));
            owner.capabilities.allowFlying = (init && (owner.experienceLevel > 0) && enableFlight);
            owner.capabilities.isFlying = (init && owner.capabilities.isFlying && owner.experienceLevel > 0 && enableFlight);
        }
    }
}
