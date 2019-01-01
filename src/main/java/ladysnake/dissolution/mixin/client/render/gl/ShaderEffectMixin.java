package ladysnake.dissolution.mixin.client.render.gl;

import ladysnake.dissolution.lib.client.shader.AccessiblePassesShaderEffect;
import net.minecraft.class_279;
import net.minecraft.client.gl.PostProcessShader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(class_279.class)
public abstract class ShaderEffectMixin implements AccessiblePassesShaderEffect {
    @Shadow @Final private List<PostProcessShader> field_1497;

    @Override
    public List<PostProcessShader> getPasses() {
        return field_1497;
    }
}
