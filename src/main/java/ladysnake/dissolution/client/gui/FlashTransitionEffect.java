package ladysnake.dissolution.client.gui;

import ladylib.compat.EnhancedBusSubscriber;
import ladysnake.dissolution.common.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import static net.minecraft.client.renderer.GlStateManager.*;
import static net.minecraft.client.renderer.OpenGlHelper.defaultTexUnit;
import static org.lwjgl.opengl.GL11.*;

@EnhancedBusSubscriber(side = Side.CLIENT)
public class FlashTransitionEffect {
    public static final FlashTransitionEffect INSTANCE = new FlashTransitionEffect();
    private static final ResourceLocation TRANSITION_TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/gui/world_transition.png");

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
    void onRenderWorldLast(RenderWorldLastEvent event) {
        if (ticks <= 0) return;

        Minecraft mc = Minecraft.getMinecraft();
        Entity camera = mc.getRenderViewEntity();
        if (camera == null) return;

        float partialTicks = event.getPartialTicks();

        setActiveTexture(defaultTexUnit);
        mc.getTextureManager().bindTexture(TRANSITION_TEXTURE);
        // Ignore depth when rendering lights
        disableDepth();

        pushMatrix();
        color(0.0F, 0.0F, 0.0F, (ticks + partialTicks) / (float) transitionTime);
        scale(1F, 1F, 1F);
        enableBlend();
        disableAlpha();     // enable transparency

        // Setup overlay rendering
        ScaledResolution scaledRes = new ScaledResolution(mc);
        matrixMode(GL_PROJECTION);
        loadIdentity();
        ortho(0.0D, scaledRes.getScaledWidth_double(), scaledRes.getScaledHeight_double(), 0.0D, 1000.0D, 3000.0D);
        matrixMode(GL_MODELVIEW);
        loadIdentity();
        translate(0.0F, 0.0F, -2000.0F);

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
        popMatrix();
        disableBlend();
        enableAlpha();
        blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA); // restore blending
        enableDepth();
    }
}
