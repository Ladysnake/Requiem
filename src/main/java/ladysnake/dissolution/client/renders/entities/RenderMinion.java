package ladysnake.dissolution.client.renders.entities;

import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.entity.minion.AbstractMinion;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.util.function.BiFunction;
import java.util.function.Function;

public class RenderMinion<V extends AbstractMinion> extends RenderBiped<V> {

    public static final ResourceLocation SKELETON_MINION_TEXTURES = new ResourceLocation(Reference.MOD_ID, "textures/entity/minions/minion_skeleton.png");
    public static final ResourceLocation SKELETON_TEXTURE = new ResourceLocation("textures/entity/skeleton/skeleton.png");

    public static final ResourceLocation STRAY_MINION_TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/entity/minions/minion_stray.png");
    public static final ResourceLocation STRAY_TEXTURE = new ResourceLocation("textures/entity/skeleton/stray.png");

    public static final ResourceLocation ZOMBIE_PIGMAN_TEXTURE = new ResourceLocation("textures/entity/zombie_pigman.png");
    public static final ResourceLocation ZOMBIE_PIGMAN_MINION_TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/entity/minions/minion_zombie_pigman.png");

    private final ResourceLocation texture, textureInert;

    @SafeVarargs
    public RenderMinion(RenderManager renderManagerIn, BiFunction<Float, Boolean, ModelBiped> modelBipedIn, ResourceLocation texture,
                        ResourceLocation textureInert, Function<RenderMinion, LayerRenderer<V>>... layers) {
        super(renderManagerIn, modelBipedIn.apply(0f, false), 0.5f);
        this.texture = texture;
        this.textureInert = textureInert;
        for (Function<RenderMinion, LayerRenderer<V>> layer : layers)
            this.addLayer(layer.apply(this));
        this.addLayer(new LayerBipedArmor(this) {
            protected void initArmor() {
                this.modelLeggings = modelBipedIn.apply(0.5F, true);
                this.modelArmor = modelBipedIn.apply(1.0F, true);
            }
        });
    }

    @Override
    @Nonnull
    protected ResourceLocation getEntityTexture(@Nonnull V entity) {
        if (entity.isInert())
            return textureInert;
        return texture;
    }

    @Override
    public void doRenderShadowAndFire(@Nonnull Entity entityIn, double x, double y, double z, float yaw, float partialTicks) {
        super.doRenderShadowAndFire(entityIn, x, y, z, yaw, partialTicks);
    }

    @Override
    public void doRender(@Nonnull V minionIn, double x, double y, double z, float entityYaw, float partialTicks) {
        GL11.glPushMatrix();
        float colorRatio = (minionIn.getHealth()) / (minionIn.getMaxHealth());
        GlStateManager.color(colorRatio, colorRatio, colorRatio);
        super.doRender(minionIn, x, y, z, entityYaw, partialTicks);
        GL11.glPopMatrix();
    }

}
