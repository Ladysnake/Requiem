package ladysnake.dissolution.common.handlers;

import ladysnake.dissolution.api.corporeality.ICorporealityStatus;
import ladysnake.dissolution.api.corporeality.IIncorporealHandler;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.config.DissolutionConfigManager;
import ladysnake.dissolution.common.networking.IncorporealMessage;
import ladysnake.dissolution.common.networking.PacketHandler;
import ladysnake.dissolution.common.networking.PossessionMessage;
import ladysnake.dissolution.common.registries.SoulStates;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;


/**
 * This class handles basic events-related logic
 *
 * @author Pyrofab
 */
public class EventHandlerCommon {

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerLoggedInEvent event) {
        event.player.inventoryContainer.addListener(new PlayerInventoryListener((EntityPlayerMP) event.player));
    }

    @SubscribeEvent
    public void onPlayerStartTracking(PlayerEvent.StartTracking event) {
        if (event.getEntityPlayer() instanceof EntityPlayerMP && event.getTarget() instanceof EntityPlayer) {
            EntityPlayerMP thePlayer = (EntityPlayerMP) event.getEntityPlayer();
            EntityPlayer target = (EntityPlayer) event.getTarget();
            IIncorporealHandler handler = CapabilityIncorporealHandler.getHandler(target);
            PacketHandler.NET.sendTo(new IncorporealMessage(target.getEntityId(), handler.isStrongSoul(), handler.getCorporealityStatus()), thePlayer);
            if (handler.isPossessionActive()) {
                PacketHandler.NET.sendTo(new PossessionMessage(target.getUniqueID(), handler.getPossessed().getEntityId()), thePlayer);
            }
        }
    }

    @SubscribeEvent
    public void clonePlayer(PlayerEvent.Clone event) {
        final IIncorporealHandler corpse = CapabilityIncorporealHandler.getHandler(event.getOriginal());
        final IIncorporealHandler clone = CapabilityIncorporealHandler.getHandler(event.getEntityPlayer());
        clone.setStrongSoul(corpse.isStrongSoul());
        clone.setCorporealityStatus(corpse.getCorporealityStatus());
        clone.getDialogueStats().deserializeNBT(corpse.getDialogueStats().serializeNBT());
        clone.setSynced(false);

        if (event.isWasDeath() && !event.getEntityPlayer().isCreative()) {
            if (clone.isStrongSoul()) {
                event.getEntityPlayer().experienceLevel = event.getOriginal().experienceLevel;
                clone.getDeathStats().setDeathDimension(corpse.getDeathStats().getDeathDimension());
                clone.getDeathStats().setDeathLocation(new BlockPos(event.getOriginal().posX, event.getOriginal().posY, event.getOriginal().posZ));
            }
        } else if (!event.isWasDeath()) {
            // Bring the body along if coming from an end portal
            clone.setSerializedPossessedEntity(corpse.getSerializedPossessedEntity());
        }
    }

    /**
     * Makes the player practically invisible to mobs
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onVisibilityPlayer(PlayerEvent.Visibility event) {
        final ICorporealityStatus playerCorp = CapabilityIncorporealHandler.getHandler(event.getEntityPlayer()).getCorporealityStatus();
        if (playerCorp.isIncorporeal()) {
            event.modifyVisibility(0D);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerAttackEntity(AttackEntityEvent event) {
        final IIncorporealHandler playerCorp = CapabilityIncorporealHandler.getHandler(event.getEntityPlayer());
        if (playerCorp.getCorporealityStatus().isIncorporeal() && !event.getEntityPlayer().isCreative()) {
            final EntityLivingBase possessed = playerCorp.getPossessed();
            if (possessed != null && !possessed.isDead) {
                if (event.getTarget() instanceof EntityLivingBase) {
                    event.getEntityPlayer().getHeldItemMainhand().hitEntity((EntityLivingBase) event.getTarget(), event.getEntityPlayer());
                }
                possessed.attackEntityAsMob(event.getTarget());
                return;
            }
        }
        if (event.getTarget() instanceof EntityPlayer) {
            final IIncorporealHandler targetCorp = CapabilityIncorporealHandler.getHandler((EntityPlayer) event.getTarget());
            if (targetCorp.getCorporealityStatus().isIncorporeal() && !event.getEntityPlayer().isCreative()) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onLivingSetAttackTarget(LivingSetAttackTargetEvent event) {
        CapabilityIncorporealHandler.getHandler(event.getTarget()).ifPresent(handler -> {
            if (event.getEntity() instanceof EntityLiving && handler.getCorporealityStatus().isIncorporeal() && DissolutionConfigManager.isEctoplasmImmuneTo(event.getEntity())) {
                ((EntityLiving) event.getEntity()).setAttackTarget(null);
            }
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntityItemPickup(EntityItemPickupEvent event) {
        final IIncorporealHandler playerCorp = CapabilityIncorporealHandler.getHandler(event.getEntityPlayer());
        if (playerCorp.getCorporealityStatus().isIncorporeal() && playerCorp.getPossessed() == null && !event.getEntityPlayer().isCreative()) {
            event.setCanceled(true);
        }
    }

    /**
     * Makes the players tangible again when stroke by lightning. Just because we can.
     */
    @SubscribeEvent
    public void onEntityStruckByLightning(EntityStruckByLightningEvent event) {
        if (event.getEntity() instanceof EntityPlayer) {
            final IIncorporealHandler playerCorp = CapabilityIncorporealHandler.getHandler((EntityPlayer) event.getEntity());
            if (playerCorp.getCorporealityStatus().isIncorporeal()) {
                playerCorp.setCorporealityStatus(SoulStates.BODY);
            }
        }
    }
}
