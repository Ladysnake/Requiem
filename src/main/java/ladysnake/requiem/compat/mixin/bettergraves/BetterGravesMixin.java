package ladysnake.requiem.compat.mixin.bettergraves;

import bettergraves.BetterGraves;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BetterGraves.class)
public abstract class BetterGravesMixin {
    @Inject(method = "placeGrave", at = @At("HEAD"), cancellable = true, remap = false)
    private static void preventGravePlacement(BlockPos deathLocation, ServerPlayerEntity player, ServerWorld world, DamageSource deathBlow, CallbackInfo ci) {
        if (player.isAlive() && RemnantComponent.isVagrant(player)) {
            player.inventory.dropAll();
            ci.cancel();
        }
    }
}
