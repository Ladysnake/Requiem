package ladysnake.dissolution.client;

import it.unimi.dsi.fastutil.floats.FloatConsumer;
import ladysnake.dissolution.Dissolution;
import ladysnake.dissolution.api.remnant.RemnantHandler;
import ladysnake.satin.client.shader.ManagedShaderEffect;
import ladysnake.satin.client.shader.ShaderEffectManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

public final class ShaderHandler implements FloatConsumer {
    public static final Identifier SPECTRE_SHADER_ID = Dissolution.id("shaders/post/spectre.json");
    public static final Identifier FISH_EYE_SHADER_ID = Dissolution.id("shaders/post/fish_eye.json");

    public static final ShaderHandler INSTANCE = new ShaderHandler();

    private final MinecraftClient mc = MinecraftClient.getInstance();
    private final ManagedShaderEffect spectreShader = ShaderEffectManager.manage(SPECTRE_SHADER_ID);
    private final ManagedShaderEffect fishEyeShader = ShaderEffectManager.manage(FISH_EYE_SHADER_ID);
    private boolean renderFishEye = false;

    @Override
    public void accept(float tickDelta) {
        if (RemnantHandler.get(mc.player).filter(RemnantHandler::isIncorporeal).isPresent()) {
            spectreShader.render(tickDelta);
        }
        if (renderFishEye) {
            fishEyeShader.render(tickDelta);
        }
    }
}
