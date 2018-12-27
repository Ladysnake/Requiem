package ladysnake.dissolution.common.handlers;

import ladysnake.dissolution.api.corporeality.IIncorporealHandler;
import ladysnake.dissolution.api.corporeality.IPossessable;
import ladysnake.dissolution.api.corporeality.ISoulInteractable;
import ladysnake.dissolution.api.possession.PossessionEvent;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.entity.PossessableEntityFactory;
import ladysnake.dissolution.common.inventory.DissolutionInventoryHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.GetCollisionBoxesEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class InteractEventsHandler {

    public static final double MAX_THICCNESS = 0.9;     // you are welcome

    /**
     * Make wisps and soul players go through thin walls
     */
    @SubscribeEvent
    public void onGetCollisionBoxes(GetCollisionBoxesEvent event) {
        if (event.getEntity() instanceof EntityPlayer) {
            IIncorporealHandler handler = CapabilityIncorporealHandler.getHandler((EntityPlayer) event.getEntity());
            if (handler.isIncorporeal()) {
                event.getCollisionBoxesList().removeIf(axisAlignedBB -> axisAlignedBB.getAverageEdgeLength() < MAX_THICCNESS);
            }
        }
    }

    /**
     * Allows possession start
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        EntityPlayer player = event.getEntityPlayer();
        Entity target = event.getTarget();
        IIncorporealHandler handler = CapabilityIncorporealHandler.getHandler(player);
        if (!(handler.getCorporealityStatus().isIncorporeal() && handler.getPossessed() == null
                && !player.isCreative())) {
            return;
        }
        event.setCanceled(true);
        if (handler.getCorporealityStatus().isIncorporeal()) {
            if (target instanceof ISoulInteractable) {
                event.setCancellationResult(((ISoulInteractable) target).applySoulInteraction(player, event.getLocalPos(), event.getHand()));
            } else if (event.getSide().isServer()
                    && target instanceof EntityLivingBase && !target.getIsInvulnerable()) {
                PossessionEvent.Setup setupEvent = new PossessionEvent.Setup(
                        player,
                        (EntityLivingBase) target,
                        PossessableEntityFactory.createPossessableEntityFrom((EntityLivingBase) target)
                );
                if (MinecraftForge.EVENT_BUS.post(setupEvent)) {
                    return;
                }
                EntityLivingBase host = setupEvent.getPossessed();
                if (host != null && ((IPossessable)host).canBePossessedBy(player)) {
                    if (((EntityLivingBase) target).getHeldItemMainhand().getItem() instanceof ItemBow) {
                        player.addItemStackToInventory(new ItemStack(Items.ARROW, host.world.rand.nextInt(10) + 2));
                    }
                    DissolutionInventoryHelper.transferEquipment((EntityLivingBase) target, player);
                    if (host != target) {
                        target.setPosition(0, -100, 0);
                        target.world.spawnEntity(host);
                        target.world.removeEntity(target);
                    }
                    handler.setPossessed((EntityMob & IPossessable) host);
                    event.setCancellationResult(EnumActionResult.SUCCESS);
                }
            }
        }
    }

    /**
     * Make projectiles go through ghost players
     */
    @SubscribeEvent
    public void onProjectileImpact(ProjectileImpactEvent event) {
        Entity entityHit = event.getRayTraceResult().entityHit;
        if (event.getRayTraceResult().typeOfHit == RayTraceResult.Type.ENTITY && entityHit instanceof EntityPlayer) {
            IIncorporealHandler handler = CapabilityIncorporealHandler.getHandler((EntityPlayer) entityHit);
            if (handler.getCorporealityStatus().isIncorporeal()) {
                if (handler.isPossessionActive()) {
                    Entity possessed = handler.getPossessed();
                    Entity projectile = event.getEntity();
                    Entity shooter = null;
                    if (projectile instanceof EntityArrow) {
                        shooter = ((EntityArrow) projectile).shootingEntity;
                    } else if (projectile instanceof EntityThrowable) {
                        shooter = ((EntityThrowable) projectile).getThrower();
                    }
                    if (shooter != possessed && shooter != entityHit) {
                        // Make the projectile impact the possessed entity
                        event.getRayTraceResult().entityHit = handler.getPossessed();
                        return;
                    }
                }
                // Make the projectile not hit the spirit / the shooter
                event.setCanceled(true);
            }
        }
    }

    /**
     * Prevent possessed mobs from shooting themselves with standard projectiles
     */
    @SubscribeEvent
    public void onThrowableImpact(ProjectileImpactEvent.Throwable event) {
        if (event.getRayTraceResult().typeOfHit == RayTraceResult.Type.ENTITY &&
                CapabilityIncorporealHandler.getHandler(event.getThrowable().getThrower())
                        .map(IIncorporealHandler::getPossessed)
                        .filter(event.getRayTraceResult().entityHit::equals)
                        .isPresent()) {
            event.setCanceled(true);
        }
    }

    /**
     * Prevent possessed mobs from shooting themselves with arrows
     */
    @SubscribeEvent
    public void onArrowImpact(ProjectileImpactEvent.Arrow event) {
        if (event.getRayTraceResult().typeOfHit == RayTraceResult.Type.ENTITY &&
                        CapabilityIncorporealHandler.getHandler(event.getArrow().shootingEntity)
                                .map(IIncorporealHandler::getPossessed)
                                .filter(event.getRayTraceResult().entityHit::equals)
                                .isPresent()) {
            event.setCanceled(true);
        }
    }
}
