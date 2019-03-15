package ladysnake.dissolution.mixin.server.network;

import com.mojang.authlib.GameProfile;
import ladysnake.dissolution.api.v1.possession.Possessable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static ladysnake.dissolution.common.network.DissolutionNetworking.*;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {

    public ServerPlayerEntityMixin(World world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "onStartedTracking", at = @At("HEAD"))
    private void onStartedTracking(Entity tracked, CallbackInfo info) {
        if (tracked instanceof PlayerEntity) {
            // Synchronize soul players with other players
            sendTo((ServerPlayerEntity)(Object)this, createCorporealityMessage((PlayerEntity) tracked));
        } else if (tracked instanceof Possessable) {
            // Synchronize possessed entities with their possessor / other players
            ((Possessable) tracked).getPossessorUuid()
                    .ifPresent(uuid -> sendTo((ServerPlayerEntity)(Object)this, createPossessionMessage(uuid, tracked.getEntityId())));
        }
    }
}
