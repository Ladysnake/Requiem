package ladysnake.requiem.client;

import ladysnake.requiem.api.v1.RequiemPlayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.WaterCreatureEntity;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class EntityShaders {

    public static void pickShader(Entity camera, Consumer<Identifier> loadShaderFunc, @SuppressWarnings("unused") Supplier<ShaderEffect> appliedShaderGetter) {
        if (camera instanceof RequiemPlayer) {
            Entity possessed = (Entity) ((RequiemPlayer)camera).getPossessionComponent().getPossessedEntity();
            if (possessed != null) {
                MinecraftClient.getInstance().gameRenderer.onCameraEntitySet(possessed);
            }
        } else if (camera instanceof WaterCreatureEntity) {
            loadShaderFunc.accept(RequiemFx.FISH_EYE_SHADER_ID);
        }
    }

}
