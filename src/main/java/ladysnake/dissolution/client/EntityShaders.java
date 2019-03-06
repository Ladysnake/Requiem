package ladysnake.dissolution.client;

import ladysnake.dissolution.api.v1.DissolutionPlayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.WaterCreatureEntity;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class EntityShaders {

    public static void pickShader(Entity camera, Consumer<Identifier> loadShaderFunc, @SuppressWarnings("unused") Supplier<ShaderEffect> appliedShaderGetter) {
        if (camera instanceof DissolutionPlayer) {
            Entity possessed = (Entity) ((DissolutionPlayer)camera).getPossessionComponent().getPossessedEntity();
            if (possessed != null) {
                MinecraftClient.getInstance().gameRenderer.onCameraEntitySet(possessed);
            }
        } else if (camera instanceof WaterCreatureEntity) {
            loadShaderFunc.accept(DissolutionFx.FISH_EYE_SHADER_ID);
        }
    }

}
