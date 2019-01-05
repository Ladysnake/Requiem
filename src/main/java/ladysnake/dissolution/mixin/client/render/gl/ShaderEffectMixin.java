package ladysnake.dissolution.mixin.client.render.gl;

import ladysnake.dissolution.lib.client.shader.AccessiblePassesShaderEffect;
import net.minecraft.client.gl.PostProcessShader;
import net.minecraft.client.gl.ShaderEffect;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

/**
 * The ShaderEffect class does not have a getter for its passes, so this mixin does the job until Access Transformers
 * are a thing.
 */
@Mixin(ShaderEffect.class)
public abstract class ShaderEffectMixin implements AccessiblePassesShaderEffect {
    @Shadow @Final private List<PostProcessShader> passes;

    @Override
    public List<PostProcessShader> getPasses() {
        return this.passes;
    }
}
