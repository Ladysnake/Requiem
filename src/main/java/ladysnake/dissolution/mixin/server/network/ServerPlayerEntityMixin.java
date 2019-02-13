package ladysnake.dissolution.mixin.server.network;

import com.mojang.authlib.GameProfile;
import ladysnake.dissolution.Dissolution;
import ladysnake.dissolution.api.v1.DissolutionPlayer;
import ladysnake.dissolution.api.v1.possession.Possessable;
import ladysnake.dissolution.api.v1.possession.PossessionComponent;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.packet.MobSpawnClientPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.function.Function;

import static ladysnake.dissolution.common.network.DissolutionNetworking.*;
import static ladysnake.dissolution.mixin.server.PlayerTagKeys.*;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    @Nullable
    private CompoundTag dissolution_possessedEntityTag;

    @Shadow public abstract SleepResult trySleep(BlockPos blockPos_1);

    @Shadow public ServerPlayNetworkHandler networkHandler;

    public ServerPlayerEntityMixin(World world_1, GameProfile gameProfile_1) {
        super(world_1, gameProfile_1);
    }

    @Inject(method = "onStartedTracking", at = @At("HEAD"))
    private void onStartedTracking(Entity tracked, CallbackInfo info) {
        if (tracked instanceof PlayerEntity) {
            // Synchronize soul players with other players
            sendTo((ServerPlayerEntity)(Object)this, createCorporealityPacket((PlayerEntity) tracked));
        } else if (tracked instanceof Possessable) {
            // Synchronize possessed entities with their possessor / other players
            ((Possessable) tracked).getPossessorUuid()
                    .ifPresent(uuid -> sendTo((ServerPlayerEntity)(Object)this, createPossessionPacket(uuid, tracked.getEntityId())));
        }
    }

    @Inject(method = "changeDimension", at = @At("HEAD"))
    private void changePossessedDimension(DimensionType dim, CallbackInfoReturnable<Entity> info) {
        PossessionComponent possessionComponent = ((DissolutionPlayer) this).getPossessionComponent();
        if (possessionComponent.isPossessing()) {
            Entity current = (Entity) possessionComponent.getPossessedEntity();
            if (current != null) {
                possessionComponent.stopPossessing();
                this.dissolution_possessedEntityTag = new CompoundTag();
                current.saveSelfToTag(this.dissolution_possessedEntityTag);
                ((ServerWorld)this.world).method_18216(current);
            }
        }
    }

    @Inject(method = "onTeleportationDone", at = @At("HEAD"))
    private void onTeleportDone(CallbackInfo info) {
        sendTo((ServerPlayerEntity)(Object)this, createCorporealityPacket(this));
        if (this.dissolution_possessedEntityTag != null) {
            Entity formerPossessed = EntityType.loadEntityWithPassengers(
                    this.dissolution_possessedEntityTag,
                    world,
                    Function.identity()
            );
            if (formerPossessed instanceof MobEntity) {
                formerPossessed.setPositionAndAngles(this);
                if (world.spawnEntity(formerPossessed)) {
                    this.networkHandler.sendPacket(new MobSpawnClientPacket((LivingEntity) formerPossessed));
                    ((DissolutionPlayer)this).getPossessionComponent().startPossessing((MobEntity) formerPossessed);
                } else {
                    Dissolution.LOGGER.error("Failed to spawn possessed entity {}", formerPossessed);
                }
            } else {
                Dissolution.LOGGER.error("Could not recreate possessed entity {}", dissolution_possessedEntityTag);
            }
            this.dissolution_possessedEntityTag = null;
        }
    }

    @Inject(method = "method_5623", at=@At("HEAD"), cancellable = true)
    private void method_5623(double double_1, boolean boolean_1, BlockState blockState_1, BlockPos blockPos_1, CallbackInfo info) {
        Possessable possessed = ((DissolutionPlayer)this).getPossessionComponent().getPossessedEntity();
        if (possessed != null) {
            possessed.onPossessorFalls(this.fallDistance, double_1, boolean_1, blockState_1, blockPos_1);
        }
    }

    @Inject(method = "writeCustomDataToTag", at = @At("RETURN"))
    private void writePossessedMobToTag(CompoundTag tag, CallbackInfo info) {
        Entity possessedEntity = (Entity) ((DissolutionPlayer)this).getPossessionComponent().getPossessedEntity();
        if (possessedEntity != null) {
            Entity possessedEntityVehicle = possessedEntity.getTopmostRiddenEntity();
            CompoundTag possessedRoot = new CompoundTag();
            CompoundTag serializedPossessed = new CompoundTag();
            possessedEntityVehicle.saveToTag(serializedPossessed);
            possessedRoot.put(POSSESSED_ENTITY_TAG, serializedPossessed);
            possessedRoot.putUuid(POSSESSED_UUID_TAG, possessedEntity.getUuid());
            tag.put(POSSESSED_ROOT_TAG, possessedRoot);
        } else if (this.dissolution_possessedEntityTag != null) {
            CompoundTag possessedRoot = new CompoundTag();
            possessedRoot.put(POSSESSED_ENTITY_TAG, this.dissolution_possessedEntityTag);
            possessedRoot.putUuid(POSSESSED_UUID_TAG, this.dissolution_possessedEntityTag.getUuid("UUID"));
            tag.put(POSSESSED_ROOT_TAG, possessedRoot);
        }
    }
}
