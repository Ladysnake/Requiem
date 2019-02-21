package ladysnake.dissolution.mixin.client.gui;

import net.minecraft.client.gui.SplashScreen;
import net.minecraft.client.texture.Texture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SplashScreen.class)
public class SplashScreenMixin {
    private int debug_retardCount = 0;

    // TODO remove when Mojang has stopped being acoustic
    @Redirect(method = "draw", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/texture/TextureManager;registerTexture(Lnet/minecraft/util/Identifier;Lnet/minecraft/client/texture/Texture;)Z"))
    private boolean fixMojangRetardation(TextureManager textureManager, Identifier identifier, Texture texture) {
        debug_retardCount++;
        return false;
    }
}
