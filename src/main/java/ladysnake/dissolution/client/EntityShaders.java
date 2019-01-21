package ladysnake.dissolution.client;

import net.minecraft.entity.Entity;
import net.minecraft.entity.WaterCreatureEntity;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;

public final class EntityShaders {

    @Nullable
    public static Identifier getShader(Entity camera) {
        if (camera instanceof WaterCreatureEntity) {
            return ShaderHandler.FISH_EYE_SHADER_ID;
        }
        return null;
    }

}
