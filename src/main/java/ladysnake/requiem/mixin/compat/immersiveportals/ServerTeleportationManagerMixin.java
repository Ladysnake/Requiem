package ladysnake.requiem.mixin.compat.immersiveportals;

import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.api.v1.remnant.MobResurrectable;
import ladysnake.requiem.common.network.RequiemNetworking;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.qouteall.immersive_portals.teleportation.ServerTeleportationManager")
public class ServerTeleportationManagerMixin {

    @Inject(method = "changePlayerDimension", at = @At(value = "HEAD", shift = At.Shift.AFTER))   // Let cancelling mixins do their job
    private void changePossessedDimension(ServerPlayerEntity player, ServerWorld fromWorld, ServerWorld toWorld, Vec3d destination, CallbackInfo ci) {
        PossessionComponent possessionComponent = RequiemPlayer.from(player).asPossessor();
        if (possessionComponent.isPossessing()) {
            MobEntity current = possessionComponent.getPossessedEntity();
            if (current != null && !current.removed) {
                ((MobResurrectable)player).setResurrectionEntity(current);
                current.remove();
            }
        }
    }

    @Inject(method = "changePlayerDimension", at = @At("RETURN"))
    private void changePlayerDimension(
        ServerPlayerEntity player,
        ServerWorld fromWorld,
        ServerWorld toWorld,
        Vec3d destination,
        CallbackInfo ci
    ) {
        RequiemNetworking.sendToAllTrackingIncluding(player, RequiemNetworking.createCorporealityMessage(player));
        ((MobResurrectable) player).spawnResurrectionEntity();
    }
}
