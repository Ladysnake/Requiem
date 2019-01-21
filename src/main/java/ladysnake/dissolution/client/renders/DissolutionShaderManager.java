package ladysnake.dissolution.client.renders;

import ladylib.client.shader.PostProcessShader;
import ladylib.compat.EnhancedBusSubscriber;
import ladysnake.dissolution.api.corporeality.IIncorporealHandler;
import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.Ref;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import static net.minecraftforge.fml.relauncher.Side.CLIENT;

@EnhancedBusSubscriber(value = Ref.MOD_ID, side = CLIENT)
public class DissolutionShaderManager {
    /**
     * The shader used to render the blue overlay when incorporeal
     */
    public static final ResourceLocation SPECTRE_SHADER = new ResourceLocation(Ref.MOD_ID, "shaders/post/spectre.json");
    private final PostProcessShader shader = PostProcessShader.loadShader(SPECTRE_SHADER);

    // PostProcessShader#render clears the depth mask. As such, we need to make it run after most other effects have been rendered.
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        IIncorporealHandler handler = CapabilityIncorporealHandler.getHandler(Minecraft.getMinecraft().player);
        if (handler.isIncorporeal() && Dissolution.config.client.useShaders) {
            shader.render(event.getPartialTicks());
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        }
    }
}
