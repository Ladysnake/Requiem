package ladysnake.dissolution.mixin.entity.player;

import ladysnake.dissolution.Dissolution;
import ladysnake.dissolution.api.DissolutionPlayer;
import ladysnake.dissolution.api.possession.Possessable;
import ladysnake.dissolution.api.remnant.RemnantHandler;
import ladysnake.dissolution.common.impl.possession.Possession;
import ladysnake.dissolution.common.impl.remnant.DefaultRemnantHandler;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.CheckForNull;
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
        PlayerEntity self = (PlayerEntity)(Object)this;
        return self.world.isClient || (!self.isSpectator() && this.isRemnant() && this.getRemnantHandler().isIncorporeal());
    }

    @Override
    public boolean startPossessing(final MobEntity mob) {
        // 1- check that the player can initiate possession
        if (!canStartPossessing(mob)) {
            return false;
        }
        @Nullable Possessable possessable;
        final PlayerEntity player = (PlayerEntity) (Object) this;
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
        this.setSize(pMob.width, pMob.height);
        possessable.setPossessingEntity(((PlayerEntity)(Object)this).getUuid());
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
        if (!this.world.isClient) {
            sendTo((ServerPlayerEntity)(Object)this, createPossessionPacket(getUuid(), this.possessedNetworkId));
        }
    }

    @CheckForNull
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

    @Inject(method = "getEyeHeight", at = @At("RETURN"), cancellable = true)
    public void adjustEyeHeight(CallbackInfoReturnable<Float> info) {
        if (this.isPossessing()) {
            Entity possessedEntity = (Entity) this.getPossessedEntity();
            if (possessedEntity != null) {
                info.setReturnValue(possessedEntity.getEyeHeight());
            }
        }
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
