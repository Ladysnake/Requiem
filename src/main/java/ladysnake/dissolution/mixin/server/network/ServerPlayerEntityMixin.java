package ladysnake.dissolution.mixin.server.network;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static ladysnake.dissolution.common.network.DissolutionNetworking.createCorporealityPacket;
import static ladysnake.dissolution.common.network.DissolutionNetworking.sendTo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {

    @Inject(at = @At("HEAD"), method = "onStartedTracking")
    public void onStartedTracking(Entity tracked, CallbackInfo info) {
        if (tracked instanceof PlayerEntity) {
            sendTo((ServerPlayerEntity)(Object)this, createCorporealityPacket((PlayerEntity) tracked));
        }
    }
}
