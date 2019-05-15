package ladysnake.requiem.mixin.server.world;

import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.internal.ProtoPossessable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.spongepowered.asm.mixin.injection.At.Shift.AFTER;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {
    @Inject(method = "loadEntityUnchecked", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerChunkManager;loadEntity(Lnet/minecraft/entity/Entity;)V", shift = AFTER))
    private void possessLoadedEntities(Entity entity, CallbackInfo ci) {
        PlayerEntity possessor = ((ProtoPossessable) entity).getPossessor();
        if (possessor != null && entity instanceof MobEntity) {
            ((RequiemPlayer)possessor).getPossessionComponent().startPossessing((MobEntity) entity);
        }
    }
}
