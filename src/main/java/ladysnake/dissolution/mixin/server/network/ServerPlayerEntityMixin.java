package ladysnake.dissolution.mixin.server.network;

import ladysnake.dissolution.api.DissolutionPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {

    @Inject(at = @At("HEAD"), method = "onStartedTracking")
    public void onStartedTracking(Entity tracked, CallbackInfo info) {
        if (tracked instanceof DissolutionPlayer) {
            // TODO send a packet
        }
    }
}
