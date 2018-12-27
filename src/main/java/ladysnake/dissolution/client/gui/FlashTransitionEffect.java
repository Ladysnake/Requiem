package ladysnake.dissolution.client.gui;

import ladylib.compat.EnhancedBusSubscriber;
import ladysnake.dissolution.common.Ref;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import static net.minecraft.client.renderer.GlStateManager.*;
import static net.minecraft.client.renderer.OpenGlHelper.defaultTexUnit;
import static org.lwjgl.opengl.GL11.*;

@EnhancedBusSubscriber(value = Ref.MOD_ID, side = Side.CLIENT)
public class FlashTransitionEffect {
    public static final FlashTransitionEffect INSTANCE = new FlashTransitionEffect();
    private static final ResourceLocation TRANSITION_TEXTURE = new ResourceLocation(Ref.MOD_ID, "textures/gui/world_transition.png");

    private int transitionTime;
    private int ticks;

    /**
     *
     * @param transitionTime the time in ticks for this transition
     */
    public void fade(int transitionTime) {
        this.ticks = transitionTime;
        this.transitionTime = transitionTime;
    }

    @SubscribeEvent
    public void onTickClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            if (ticks > 0) {
                ticks --;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onRenderWorldLast(RenderGameOverlayEvent.Pre event) {
        if (ticks <= 0 || event.getType() != RenderGameOverlayEvent.ElementType.ALL) {
            // no transition currently occurring, or wrong event type
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        Entity camera = mc.getRenderViewEntity();
        if (camera == null) return;

        float partialTicks = event.getPartialTicks();
        ScaledResolution scaledRes = event.getResolution();

        setActiveTexture(defaultTexUnit);
        mc.getTextureManager().bindTexture(TRANSITION_TEXTURE);
        color(0.0F, 0.0F, 0.0F, (ticks + partialTicks) / (float) transitionTime);
        enableBlend();
        disableAlpha();     // enable transparency

        // Draw quad over the screen
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(0.0D, (double)scaledRes.getScaledHeight(), -90.0D).tex(0.0D, 1.0D).endVertex();
        bufferbuilder.pos((double)scaledRes.getScaledWidth(), (double)scaledRes.getScaledHeight(), -90.0D).tex(1.0D, 1.0D).endVertex();
        bufferbuilder.pos((double)scaledRes.getScaledWidth(), 0.0D, -90.0D).tex(1.0D, 0.0D).endVertex();
        bufferbuilder.pos(0.0D, 0.0D, -90.0D).tex(0.0D, 0.0D).endVertex();
        tessellator.draw();

        // restore old values
        color(1, 1, 1, 1f);
        disableBlend();
        enableAlpha();
        blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA); // restore blending
    }
}
