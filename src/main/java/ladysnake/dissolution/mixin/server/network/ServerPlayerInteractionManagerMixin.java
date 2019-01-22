package ladysnake.dissolution.mixin.server.network;

import ladysnake.dissolution.api.v1.DissolutionPlayer;
import ladysnake.dissolution.api.v1.remnant.RemnantHandler;
import ladysnake.dissolution.common.tag.DissolutionEntityTags;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.spongepowered.asm.mixin.injection.At.Shift.AFTER;

@Mixin(ServerPlayerInteractionManager.class)
public abstract class ServerPlayerInteractionManagerMixin {

    @Shadow public ServerPlayerEntity player;

    @Inject(
            method = "setGameMode",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/GameMode;setAbilitites(Lnet/minecraft/entity/player/PlayerAbilities;)V",
                    shift = AFTER
            ))
    public void keepSoulAbilities(GameMode newMode, CallbackInfo info) {
        if (RemnantHandler.get(this.player).filter(RemnantHandler::isSoul).isPresent()) {
            Entity possessed = (Entity) ((DissolutionPlayer)this.player).getPossessionManager().getPossessedEntity();
            this.player.abilities.invulnerable = true;
            this.player.abilities.allowFlying = possessed == null || this.player.isCreative() || DissolutionEntityTags.FLIGHT.contains(possessed.getType());
        }
    }
}
