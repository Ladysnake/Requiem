package ladysnake.satin.client.shader;

import net.minecraft.util.Identifier;

import static ladysnake.satin.Satin.MOD_ID;
import static ladysnake.satin.client.shader.ShaderHelper.SHADER_LOCATION_PREFIX;

public final class BaseShaders {
    private BaseShaders() { }

    public static final Identifier BASE_VERTEX = new Identifier(MOD_ID, SHADER_LOCATION_PREFIX + "vertex_base.vsh");
    public static final Identifier BASE_FRAGMENT = new Identifier(MOD_ID, SHADER_LOCATION_PREFIX + "fragment_base.fsh");

    /**Changes the saturation of the texture based on the <tt>saturation</tt> uniform*/
    public static final Identifier SATURATION = new Identifier(MOD_ID, "saturation");

    public static void onShaderRegistry(ShaderRegistryEvent event) {
        event.registerFragmentShader(SATURATION);
    }

}