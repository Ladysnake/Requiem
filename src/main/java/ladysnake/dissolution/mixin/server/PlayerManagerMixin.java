package ladysnake.dissolution.mixin.server;

import com.mojang.authlib.GameProfile;
import ladysnake.dissolution.Dissolution;
import ladysnake.dissolution.api.v1.DissolutionPlayer;
import ladysnake.dissolution.api.v1.DissolutionWorld;
import ladysnake.dissolution.api.v1.event.PlayerCloneCallback;
import ladysnake.dissolution.api.v1.event.PlayerRespawnCallback;
import ladysnake.dissolution.api.v1.possession.Possessable;
import ladysnake.dissolution.api.v1.possession.PossessionComponent;
import ladysnake.dissolution.api.v1.remnant.FractureAnchor;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.UserCache;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import javax.annotation.Nullable;
import java.util.UUID;

import static ladysnake.dissolution.common.network.DissolutionNetworking.*;
import static ladysnake.dissolution.mixin.server.PlayerTagKeys.*;
import static org.spongepowered.asm.mixin.injection.At.Shift.AFTER;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
    @Shadow @Final private MinecraftServer server;

    @Inject(method = "onPlayerConnect", at = @At("RETURN"))
    private void onPlayerConnect(ClientConnection connection, ServerPlayerEntity createdPlayer, CallbackInfo info) {
        sendTo(createdPlayer, createCorporealityMessage(createdPlayer));
    }

    @Inject(
            method = "onPlayerConnect",
            at = @At(
                    value = "CONSTANT",
                    args = "stringValue=RootVehicle",
                    ordinal = 0
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void logInPossessedEntity(
            ClientConnection connection,
            ServerPlayerEntity player,
            CallbackInfo info,
            // Local variables
            GameProfile gameProfile_1,
            UserCache userCache_1,
            String string_1,
            @Nullable CompoundTag serializedPlayer
    ) {
        if (serializedPlayer != null && serializedPlayer.containsKey(POSSESSED_ROOT_TAG, NbtType.COMPOUND)) {
            sendTo(player, createCorporealityMessage(player));
            ServerWorld world = this.server.getWorld(player.dimension);
            CompoundTag serializedPossessedInfo = serializedPlayer.getCompound(POSSESSED_ROOT_TAG);
            Entity possessedEntityMount = EntityType.loadEntityWithPassengers(
                    serializedPossessedInfo.getCompound(POSSESSED_ENTITY_TAG),
                    world,
                    (entity_1x) -> !world.method_18768(entity_1x) ? null : entity_1x
            );
            if (possessedEntityMount != null) {
                UUID possessedEntityUuid = serializedPossessedInfo.getUuid(POSSESSED_UUID_TAG);
                resumePossession(((DissolutionPlayer) player).getPossessionComponent(), world, possessedEntityMount, possessedEntityUuid);
            }
        }
    }

    private void resumePossession(PossessionComponent player, ServerWorld world, Entity possessedEntityMount, UUID possessedEntityUuid) {
        if (possessedEntityMount instanceof MobEntity && possessedEntityMount.getUuid().equals(possessedEntityUuid)) {
            player.startPossessing((MobEntity) possessedEntityMount);
        } else {
            for (Entity entity : possessedEntityMount.getPassengersDeep()) {
                if (entity instanceof MobEntity && entity.getUuid().equals(possessedEntityUuid)) {
                    player.startPossessing((MobEntity) entity);
                    break;
                }
            }
        }

        if (!player.isPossessing()) {
            Dissolution.LOGGER.warn("Couldn't reattach possessed entity to player");
            world.method_18774(possessedEntityMount);

            for (Entity entity : possessedEntityMount.getPassengersDeep()) {
                world.method_18774(entity);
            }
        }
    }

    @Inject(
            method = "method_14611",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/PlayerManager;savePlayerData(Lnet/minecraft/server/network/ServerPlayerEntity;)V",
                    shift = AFTER,
                    ordinal = 0
            )
    )
    private void logOutPossessedEntity(ServerPlayerEntity player, CallbackInfo info) {
        Possessable possessedEntity = ((DissolutionPlayer) player).getPossessionComponent().getPossessedEntity();
        if (possessedEntity != null) {
            ServerWorld serverWorld_1 = player.getServerWorld();
            serverWorld_1.method_18774((Entity) possessedEntity);
            for (Entity ridden : ((Entity) possessedEntity).getPassengersDeep()) {
                serverWorld_1.method_18774(ridden);
            }
            serverWorld_1.method_8497(player.chunkX, player.chunkZ).markDirty();
        }
    }

    @Inject(method = "method_14606", at = @At("RETURN"))
    private void sendWorldJoinMessages(ServerPlayerEntity player, ServerWorld world, CallbackInfo ci) {
        for (FractureAnchor anchor : ((DissolutionWorld)world).getAnchorManager().getAnchors()) {
            sendTo(player, createAnchorUpdateMessage(anchor));
        }
    }

    @Inject(
            method = "method_14556",
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;method_17892(Lnet/minecraft/entity/Entity;)Z")),
            at = @At(
                    value = "FIELD",
                    opcode = Opcodes.GETFIELD,
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;networkHandler:Lnet/minecraft/server/network/ServerPlayNetworkHandler;",
                    ordinal = 0
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void firePlayerCloneEvent(
            ServerPlayerEntity original,
            DimensionType destination,
            boolean returnFromEnd,
            CallbackInfoReturnable<ServerPlayerEntity> cir,
            BlockPos spawnPos,
            boolean forcedSpawn,
            ServerPlayerInteractionManager manager,
            ServerPlayerEntity clone
    ) {
        PlayerCloneCallback.EVENT.invoker().onPlayerClone(original, clone, returnFromEnd);
    }

    @Inject(
            method = "method_14556",
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;method_17892(Lnet/minecraft/entity/Entity;)Z")),
            at = @At("RETURN"),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void firePlayerRespawnEvent(
            ServerPlayerEntity original,
            DimensionType destination,
            boolean returnFromEnd,
            CallbackInfoReturnable<ServerPlayerEntity> cir,
            BlockPos spawnPos,
            boolean forcedSpawn,
            ServerPlayerInteractionManager manager,
            ServerPlayerEntity clone
    ) {
        PlayerRespawnCallback.EVENT.invoker().onPlayerRespawn(clone, returnFromEnd);
    }
}
