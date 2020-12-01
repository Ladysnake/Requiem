package ladysnake.requiem.client;

import ladysnake.requiem.Requiem;
import ladysnake.requiem.api.v1.possession.PossessionComponent;
import ladysnake.requiem.common.tag.RequiemEntityTypeTags;
import ladysnake.satin.api.event.PickEntityShaderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.passive.DolphinEntity;
import net.minecraft.entity.passive.MooshroomEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class RequiemEntityShaderPicker implements PickEntityShaderCallback {
    public static final Identifier DICHROMATIC_SHADER_ID = shader("dichromatic");
    public static final Identifier TETRACHROMATIC_SHADER_ID = shader("tetrachromatic");

    public static final Identifier BEE_SHADER_ID = shader("bee");
    public static final Identifier DOLPHIN_SHADER_ID = shader("dolphin");
    public static final Identifier FISH_EYE_SHADER_ID = shader("fish_eye");

    public static final Identifier MOOSHROOM_SHADER_ID = shader("mooshroom");

    public void registerCallbacks() {
        PickEntityShaderCallback.EVENT.register(this);
    }

    @Override
    public void pickEntityShader(@Nullable Entity camera, Consumer<Identifier> loadShaderFunc, Supplier<ShaderEffect> appliedShaderGetter) {
        if (camera == null) return;
        // make players use their possessed entity's shader
        Entity possessed = PossessionComponent.getPossessedEntity(camera);
        if (possessed != null) {
            MinecraftClient.getInstance().gameRenderer.onCameraEntitySet(possessed);
        } else if (appliedShaderGetter.get() == null) {
            if (RequiemEntityTypeTags.DICHROMATS.contains(camera.getType())) {
                loadShaderFunc.accept(DICHROMATIC_SHADER_ID);
            } else if (RequiemEntityTypeTags.TETRACHROMATS.contains(camera.getType())) {
                loadShaderFunc.accept(TETRACHROMATIC_SHADER_ID);
            } else if (camera instanceof BeeEntity) {
                loadShaderFunc.accept(BEE_SHADER_ID);
            }else if (camera instanceof MooshroomEntity) {
                loadShaderFunc.accept(MOOSHROOM_SHADER_ID);
            } else if (camera instanceof DolphinEntity) {
                loadShaderFunc.accept(DOLPHIN_SHADER_ID);
            } else if (camera instanceof WaterCreatureEntity) {
                loadShaderFunc.accept(FISH_EYE_SHADER_ID);
            }
        }
    }

    private static Identifier shader(String id) {
        return Requiem.id("shaders/post/" + id + ".json");
    }
}
