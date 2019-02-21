package ladysnake.dissolution.common.impl.possession;

import com.google.common.collect.MapMaker;
import ladysnake.dissolution.Dissolution;
import ladysnake.dissolution.api.v1.DissolutionPlayer;
import ladysnake.dissolution.api.v1.possession.Possessable;
import ladysnake.dissolution.api.v1.possession.PossessionComponent;
import ladysnake.dissolution.common.DissolutionRegistries;
import ladysnake.dissolution.common.entity.ai.attribute.AttributeHelper;
import ladysnake.dissolution.common.entity.ai.attribute.PossessionDelegatingAttribute;
import ladysnake.dissolution.common.impl.movement.SerializableMovementConfig;
import ladysnake.dissolution.common.tag.DissolutionEntityTags;
import ladysnake.dissolution.common.util.InventoryHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AbstractEntityAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import static ladysnake.dissolution.common.network.DissolutionNetworking.*;

public class PossessionComponentImpl implements PossessionComponent {
    private Set<PlayerEntity> attributeUpdated = Collections.newSetFromMap(new MapMaker().weakKeys().makeMap());

    private PlayerEntity player;
    @Nullable private UUID possessedUuid;
    private int possessedNetworkId;

    public PossessionComponentImpl(PlayerEntity player) {
        this.player = player;
        this.possessedNetworkId = -1;
    }

    @Override
    public boolean canStartPossessing(final MobEntity mob) {
        DissolutionPlayer dp = (DissolutionPlayer) player;
        return player.world.isClient || (!player.isSpectator() && dp.isRemnant() && dp.getRemnantState().isIncorporeal());
    }

    @Override
    public boolean startPossessing(final MobEntity mob) {
        // 1- check that the player can initiate possession
        if (!canStartPossessing(mob)) {
            return false;
        }
        @Nullable Possessable possessable;
        if (mob instanceof Possessable) {
            possessable = (Possessable) mob;
        } else {
            possessable = DissolutionRegistries.CONVERSION.convert(mob, player);
        }
        // 2- check that the mob can be possessed
        if (possessable == null || !possessable.canBePossessedBy(player)) {
            return false;
        }
        MobEntity host = (MobEntity) possessable;
        // 3- transfer inventory
        // TODO data driven item giving
        if (host.getMainHandStack().getItem() instanceof BowItem) {
            player.method_7270(new ItemStack(Items.ARROW, host.world.random.nextInt(10) + 2));
        }
        if (DissolutionEntityTags.ITEM_USER.contains(host.getType())) {
            InventoryHelper.transferEquipment(host, player);
        }
        // 4- Actually set the possessed entity
        this.possessedUuid = host.getUuid();
        this.possessedNetworkId = host.getEntityId();
        possessable.setPossessor(this.player);
        syncPossessed();
        // 5- Update some attributes
        this.player.setPositionAndAngles(host);
        ((DissolutionPlayer)this.player).getMovementAlterer().setConfig(Dissolution.getMovementAltererManager().getEntityMovementConfig(host.getType()));
        if (!attributeUpdated.contains(this.player)) {
            swapAttributes(this.player);
            attributeUpdated.add(this.player);
        }

        // 6- Make the mob react a bit
        host.playAmbientSound();
        return true;
    }

    private void swapAttributes(PlayerEntity player) {
        AbstractEntityAttributeContainer attributeMap = player.getAttributeContainer();
        // Replace every registered attribute
        for (EntityAttributeInstance current: attributeMap.values()) {
            EntityAttributeInstance replacement = new PossessionDelegatingAttribute(attributeMap, current, this);
            AttributeHelper.substituteAttributeInstance(attributeMap, replacement);
        }
    }

    @Override
    public void stopPossessing() {
        Possessable possessed = this.getPossessedEntity();
        if (possessed != null) {
            this.possessedUuid = null;
            resetState();
            possessed.setPossessor(null);
            if (player instanceof ServerPlayerEntity) {
                Entity possessedEntity = (Entity) possessed;
                if (DissolutionEntityTags.ITEM_USER.contains(possessedEntity.getType())) {
                    InventoryHelper.transferEquipment(player, (LivingEntity) possessed);
                }
            }
        }
    }

    private void syncPossessed() {
        if (!this.player.world.isClient) {
            sendTo((ServerPlayerEntity)this.player, createPossessionMessage(this.player.getUuid(), this.possessedNetworkId));
            sendToAllTracking(this.player, createPossessionMessage(this.player.getUuid(), this.possessedNetworkId));
        }
    }

    @CheckForNull
    @Override
    public Possessable getPossessedEntity() {
        if (!isPossessing()) {
            return null;
        }
        // First attempt: use the network id (client & server)
        Entity host = this.player.world.getEntityById(this.possessedNetworkId);
        if (host == null) {
            if (this.player.world instanceof ServerWorld) {
                // Second attempt: use the UUID (server)
                // method_14190 == getEntityByUuid
                host = ((ServerWorld)this.player.world).getEntity(this.getPossessedEntityUuid());
            }
            // Set the possessed uuid to null to avoid infinite recursion
            this.possessedUuid = null;
            if (host instanceof MobEntity && host instanceof Possessable) {
                this.startPossessing((MobEntity) host);
            } else {
                if (host != null) {
                    Dissolution.LOGGER.warn("{}: this player's supposedly possessed entity ({}) cannot be possessed!", this.player, host);
                }
                Dissolution.LOGGER.debug("{}: this player's possessed entity is nowhere to be found", this);
                this.resetState();
                host = null;
            }
        }
        return (Possessable) host;
    }

    private void resetState() {
        this.possessedNetworkId = -1;
        ((DissolutionPlayer) this.player).getMovementAlterer().setConfig(SerializableMovementConfig.SOUL);
        syncPossessed();
    }

    @Override
    public boolean isPossessing() {
        return this.possessedUuid != null;
    }

    @CheckForNull
    public UUID getPossessedEntityUuid() {
        return this.possessedUuid;
    }

}
