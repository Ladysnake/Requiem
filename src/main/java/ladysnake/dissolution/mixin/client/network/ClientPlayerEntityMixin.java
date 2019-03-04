package ladysnake.dissolution.mixin.client.network;

import com.mojang.authlib.GameProfile;
import ladysnake.dissolution.api.v1.DissolutionPlayer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends PlayerEntity implements DissolutionPlayer {
    public ClientPlayerEntityMixin(World world_1, GameProfile gameProfile_1) {
        super(world_1, gameProfile_1);
    }

    @Inject(method = "method_3150", at = @At(value = "RETURN", ordinal = 1), cancellable = true)
    private void stopPushingOutOfBlocks(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            Entity possessed = (Entity) this.getPossessionComponent().getPossessedEntity();
            if (possessed != null && possessed.getHeight() < 1F) {
                cir.setReturnValue(false);
            }
        }
    }

}
