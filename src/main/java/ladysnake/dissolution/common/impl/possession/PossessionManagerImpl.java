package ladysnake.dissolution.common.impl.possession;

import ladysnake.dissolution.Dissolution;
import ladysnake.dissolution.api.DissolutionPlayer;
import ladysnake.dissolution.api.possession.Possessable;
import ladysnake.dissolution.api.possession.PossessionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import java.util.UUID;

import static ladysnake.dissolution.common.network.DissolutionNetworking.createPossessionPacket;
import static ladysnake.dissolution.common.network.DissolutionNetworking.sendTo;

public class PossessionManagerImpl implements PossessionManager {
    private PlayerEntity player;
    private @Nullable UUID possessedUuid;
    private int possessedNetworkId;

    public PossessionManagerImpl(PlayerEntity player) {
        this.player = player;
    }

    @Override
    public boolean canStartPossessing(MobEntity mob) {
        DissolutionPlayer self = (DissolutionPlayer) player;
        return player.world.isClient || (!player.isSpectator() && self.isRemnant() && self.getRemnantHandler().isIncorporeal());
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
            possessable = Possession.getConversionRegistry().convert(mob, player);
        }
        // 2- check that the mob can be possessed
        if (possessable == null || !possessable.canBePossessedBy(player)) {
            return false;
        }
        // 3- Actually set the possessed entity
        MobEntity pMob = (MobEntity) possessable;
        this.possessedUuid = pMob.getUuid();
        this.possessedNetworkId = pMob.getEntityId();
        // These size changes will be actually applied when the player ticks
        this.player.width = pMob.width;
        this.player.height = pMob.height;
        possessable.setPossessingEntity(this.player.getUuid());
        syncPossessed();
        return true;
    }

    @Override
    public void stopPossessing() {
        Possessable possessedEntity = this.getPossessedEntity();
        if (possessedEntity != null) {
            this.possessedUuid = null;
            this.possessedNetworkId = 0;
            possessedEntity.setPossessingEntity(null);
            syncPossessed();
        }
    }

    private void syncPossessed() {
        if (!this.player.world.isClient) {
            sendTo((ServerPlayerEntity)this.player, createPossessionPacket(this.player.getUuid(), this.possessedNetworkId));
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
                host = this.player.world.getEntityByUuid(this.getPossessedEntityUuid());
            }
            if (host instanceof MobEntity && host instanceof Possessable) {
                this.possessedUuid = host.getUuid();
                this.possessedNetworkId = host.getEntityId();
                syncPossessed();
            } else {
                Dissolution.LOGGER.warn("{}: this player's possessed entity is nowhere to be found", this);
                host = null;
            }
        }
        return (Possessable) host;
    }

    @Override
    public boolean isPossessing() {
        return this.possessedUuid != null;
    }

    @CheckForNull
    @Override
    public UUID getPossessedEntityUuid() {
        return this.possessedUuid;
    }

}
