package ladysnake.dissolution.mixin.entity.player;

import ladysnake.dissolution.Dissolution;
import ladysnake.dissolution.api.DissolutionPlayer;
import ladysnake.dissolution.api.possession.Possessable;
import ladysnake.dissolution.api.remnant.RemnantHandler;
import ladysnake.dissolution.common.impl.possession.Possession;
import ladysnake.dissolution.common.impl.remnant.DefaultRemnantHandler;
import ladysnake.dissolution.common.network.DissolutionNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.UUID;

import static ladysnake.dissolution.common.network.DissolutionNetworking.*;

@Mixin(value = PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements DissolutionPlayer {
    private static final String TAG_REMNANT_DATA = "dissolution:remnant_data";

    private @Nullable RemnantHandler remnantHandler;
    private @Nullable UUID possessedUuid;
    private int possessedNetworkId;

    protected PlayerEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Override
    public @Nullable RemnantHandler getRemnantHandler() {
        return this.remnantHandler;
    }

    @Override
    public void setRemnantHandler(@Nullable RemnantHandler handler) {
        this.remnantHandler = handler;
        if (!this.world.isClient) {
            ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
            sendTo(player, createCorporealityPacket(player));
            sendToAllTracking(player, createCorporealityPacket(player));
        }
    }

    @Override
    public boolean canStartPossessing(MobEntity mob) {
        return true;
    }

    @Override
    public boolean startPossessing(final MobEntity mob) {
        // 1- check that the player can initiate possession
        if (!canStartPossessing(mob)) {
            return false;
        }
        Possessable possessable = null;
        final PlayerEntity player = (PlayerEntity) (Object) this;
        if (mob instanceof Possessable) {
            possessable = (Possessable) mob;
        } else if (Possession.getConversionRegistry().canBePossessed(mob)) {
            possessable = Possession.getConversionRegistry().convert(mob, player);
        }
        // 2- check that the mob can be possessed
        if (possessable == null || !possessable.canBePossessedBy(player)) {
            return false;
        }
        // 3- Actually set the possessed entity
        this.setPossessed(possessable);
        possessable.setPossessingEntity(((PlayerEntity)(Object)this).getUuid());
        return true;
    }

    private void setPossessed(@Nullable Possessable possessable) {
        if (possessable != null) {
            this.possessedUuid = ((MobEntity)possessable).getUuid();
            this.possessedNetworkId = ((MobEntity)possessable).getEntityId();
        } else {
            this.possessedUuid = null;
            this.possessedNetworkId = 0;
        }
        if (!this.world.isClient) {
            ServerPlayerEntity player = (ServerPlayerEntity)(Object) this;
            DissolutionNetworking.sendTo(player, DissolutionNetworking.createPossessionPacket(player.getUuid(), this.possessedNetworkId));
        }
    }

    @Override
    public void stopPossessing() {
        Possessable possessedEntity = this.getPossessedEntity();
        if (possessedEntity != null) {
            this.setPossessed(null);
            possessedEntity.setPossessingEntity(null);
        }
    }

    @Nullable
    @Override
    public Possessable getPossessedEntity() {
        if (!isPossessing()) {
            return null;
        }
        // First attempt: use the network id (client & server)
        Entity host = this.world.getEntityById(this.possessedNetworkId);
        if (host == null) {
            if (this.world instanceof ServerWorld) {
                // Second attempt: use the UUID (server)
                host = this.world.getEntityByUuid(this.getPossessedEntityUuid());
            }
            if (host == null) {
                Dissolution.LOGGER.warn("{}: this player's possessed entity is nowhere to be found", this);
            } else if (host instanceof MobEntity && host instanceof Possessable) {
                this.setPossessed((Possessable) host);
            }
        }
        return (Possessable) host;
    }

    @Nullable
    @Override
    public UUID getPossessedEntityUuid() {
        return this.possessedUuid;
    }

    @Inject(at = @At("TAIL"), method = "writeCustomDataToTag")
    public void writeCustomDataToTag(CompoundTag tag, CallbackInfo info) {
        if (this.remnantHandler != null) {
            tag.put(TAG_REMNANT_DATA, this.remnantHandler.writeToTag());
        }
    }

    @Inject(at = @At("TAIL"), method = "readCustomDataFromTag")
    public void readCustomDataFromTag(CompoundTag tag, CallbackInfo info) {
        if (tag.containsKey(TAG_REMNANT_DATA)) {
            if (this.remnantHandler == null) {
                this.setRemnantHandler(new DefaultRemnantHandler((PlayerEntity)(Object)this));
            }
            this.remnantHandler.readFromTag(tag.getCompound(TAG_REMNANT_DATA));
        }
    }
}
