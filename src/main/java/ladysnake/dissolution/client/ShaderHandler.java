package ladysnake.dissolution.client;

import it.unimi.dsi.fastutil.floats.FloatConsumer;
import ladysnake.dissolution.Dissolution;
import ladysnake.dissolution.api.remnant.RemnantHandler;
import ladysnake.dissolution.lib.client.shader.ManagedShaderEffect;
import ladysnake.dissolution.lib.client.shader.ShaderEffectManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

public final class ShaderHandler implements FloatConsumer {
    public static final Identifier SPECTRE_SHADER_ID = Dissolution.id("shaders/post/spectre.json");

    public static final ShaderHandler INSTANCE = new ShaderHandler();

    private final MinecraftClient mc = MinecraftClient.getInstance();
    private final ManagedShaderEffect spectreShader = ShaderEffectManager.manage(SPECTRE_SHADER_ID);

    @Override
    public void accept(float tickDelta) {
        if (RemnantHandler.get(mc.player).filter(RemnantHandler::isIncorporeal).isPresent()) {
            spectreShader.render(tickDelta);
        }
    }
}
