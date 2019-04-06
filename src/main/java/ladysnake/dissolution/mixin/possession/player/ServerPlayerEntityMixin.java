package ladysnake.dissolution.mixin.possession.player;

import com.mojang.authlib.GameProfile;
import ladysnake.dissolution.Dissolution;
import ladysnake.dissolution.api.v1.DissolutionPlayer;
import ladysnake.dissolution.api.v1.possession.Possessable;
import ladysnake.dissolution.api.v1.possession.PossessionComponent;
import ladysnake.dissolution.mixin.entity.EntityAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.packet.CustomPayloadS2CPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.function.Function;

import static ladysnake.dissolution.common.network.DissolutionNetworking.createCorporealityMessage;
import static ladysnake.dissolution.common.network.DissolutionNetworking.sendToAllTrackingIncluding;
import static ladysnake.dissolution.mixin.server.PlayerTagKeys.*;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    @Nullable
    private CompoundTag dissolution$possessedEntityTag;

    public ServerPlayerEntityMixin(World world_1, GameProfile gameProfile_1) {
        super(world_1, gameProfile_1);
    }

    @Inject(method = "changeDimension", at = @At("HEAD"))
    private void changePossessedDimension(DimensionType dim, CallbackInfoReturnable<Entity> info) {
        PossessionComponent possessionComponent = ((DissolutionPlayer) this).getPossessionComponent();
        if (possessionComponent.isPossessing()) {
            Entity current = (Entity) possessionComponent.getPossessedEntity();
            if (current != null && !current.removed) {
                this.dissolution$possessedEntityTag = new CompoundTag();
                current.saveSelfToTag(this.dissolution$possessedEntityTag);
                current.remove();
            }
        }
    }

    @Inject(method = "copyFrom", at = @At("RETURN"))
    private void clonePlayer(ServerPlayerEntity original, boolean fromEnd, CallbackInfo ci) {
        this.dissolution$possessedEntityTag = ((ServerPlayerEntityMixin) (Object) original).dissolution$possessedEntityTag;
    }

    @Inject(method = "onTeleportationDone", at = @At("HEAD"))
    private void onTeleportDone(CallbackInfo info) {
        CustomPayloadS2CPacket message = createCorporealityMessage(this);
        sendToAllTrackingIncluding(this, message);
        if (this.dissolution$possessedEntityTag != null) {
            Entity formerPossessed = EntityType.loadEntityWithPassengers(
                    this.dissolution$possessedEntityTag,
                    world,
                    Function.identity()
            );
            if (formerPossessed instanceof MobEntity) {
                formerPossessed.setPositionAndAngles(this);
                if (world.spawnEntity(formerPossessed)) {
                    ((DissolutionPlayer) this).getPossessionComponent().startPossessing((MobEntity) formerPossessed);
                } else {
                    Dissolution.LOGGER.error("Failed to spawn possessed entity {}", formerPossessed);
                }
            } else {
                Dissolution.LOGGER.error("Could not recreate possessed entity {}", dissolution$possessedEntityTag);
            }
            this.dissolution$possessedEntityTag = null;
        }
    }

    @Inject(method = "fall", at = @At("HEAD"), cancellable = true)
    private void onFall(double fallY, boolean onGround, BlockState floorBlock, BlockPos floorPos, CallbackInfo info) {
        Possessable possessed = ((DissolutionPlayer) this).getPossessionComponent().getPossessedEntity();
        if (possessed != null) {
            ((Entity) possessed).fallDistance = this.fallDistance;
            ((EntityAccessor) possessed).onFall(fallY, onGround, floorBlock, floorPos);
        }
    }

    @Inject(method = "writeCustomDataToTag", at = @At("RETURN"))
    private void writePossessedMobToTag(CompoundTag tag, CallbackInfo info) {
        Entity possessedEntity = (Entity) ((DissolutionPlayer) this).getPossessionComponent().getPossessedEntity();
        if (possessedEntity != null) {
            Entity possessedEntityVehicle = possessedEntity.getTopmostRiddenEntity();
            CompoundTag possessedRoot = new CompoundTag();
            CompoundTag serializedPossessed = new CompoundTag();
            possessedEntityVehicle.saveToTag(serializedPossessed);
            possessedRoot.put(POSSESSED_ENTITY_TAG, serializedPossessed);
            possessedRoot.putUuid(POSSESSED_UUID_TAG, possessedEntity.getUuid());
            tag.put(POSSESSED_ROOT_TAG, possessedRoot);
        } else if (this.dissolution$possessedEntityTag != null) {
            CompoundTag possessedRoot = new CompoundTag();
            possessedRoot.put(POSSESSED_ENTITY_TAG, this.dissolution$possessedEntityTag);
            possessedRoot.putUuid(POSSESSED_UUID_TAG, this.dissolution$possessedEntityTag.getUuid("UUID"));
            tag.put(POSSESSED_ROOT_TAG, possessedRoot);
        }
    }
}
