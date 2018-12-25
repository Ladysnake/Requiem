package ladysnake.dissolution.client.renders;

import ladylib.client.shader.PostProcessShader;
import ladylib.compat.EnhancedBusSubscriber;
import ladysnake.dissolution.api.corporeality.IIncorporealHandler;
import ladysnake.dissolution.common.Ref;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@EnhancedBusSubscriber(Ref.MOD_ID)
public class DissolutionShaderManager {
    /**
     * The shader used to render the blue overlay when incorporeal
     */
    public static final ResourceLocation SPECTRE_SHADER = new ResourceLocation(Ref.MOD_ID, "shaders/post/spectre.json");
    private final PostProcessShader shader = PostProcessShader.loadShader(SPECTRE_SHADER);

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        IIncorporealHandler handler = CapabilityIncorporealHandler.getHandler(Minecraft.getMinecraft().player);
        if (handler.isIncorporeal()) {
            shader.render(event.getPartialTicks());
        }
    }
}
