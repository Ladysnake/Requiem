package ladysnake.requiem.mixin.client.compat.healthoverlay;

import ladysnake.requiem.api.v1.RequiemPlayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Pseudo
@Mixin(targets = "terrails.healthoverlay.HealthRenderer")
public class HealthRendererMixin {
    @Shadow
    private MinecraftClient client;

    @ModifyVariable(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Util;getMeasuringTimeMs()J"), ordinal = 1)
    private int substituteHealth(int health) {
        assert client.player != null;
        LivingEntity entity = ((RequiemPlayer)client.player).asPossessor().getPossessedEntity();
        if (entity != null) {
            return MathHelper.ceil(entity.getHealth());
        }
        return health;
    }
}
