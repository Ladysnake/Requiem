package ladysnake.requiem.mixin.server.network;

import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.remnant.RemnantState;
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
    private void keepSoulAbilities(GameMode newMode, CallbackInfo info) {
        if (RemnantState.getIfRemnant(this.player).filter(RemnantState::isSoul).isPresent()) {
            this.player.abilities.invulnerable = true;
            ((RequiemPlayer)this.player).getMovementAlterer().applyConfig();
        }
    }
}
