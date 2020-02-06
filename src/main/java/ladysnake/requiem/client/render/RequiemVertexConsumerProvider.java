package ladysnake.requiem.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.*;

import javax.annotation.Nullable;

public class RequiemVertexConsumerProvider implements VertexConsumerProvider {
    private final Immediate parent;
    private final Immediate plainDrawer = VertexConsumerProvider.immediate(new BufferBuilder(256));
    private int red = 255;
    private int green = 255;
    private int blue = 255;
    private int alpha = 255;
    @Nullable
    private RenderLayer secondaryLayer;

    public RequiemVertexConsumerProvider(Immediate immediate) {
        this.parent = immediate;
    }

    @Override
    public VertexConsumer getBuffer(RenderLayer renderLayer) {
        VertexConsumer parentBuffer = this.parent.getBuffer(renderLayer);
        if (secondaryLayer != null) {
            VertexConsumer effectBuffer = this.plainDrawer.getBuffer(this.secondaryLayer);
            FixedVertexConsumer fixedEffectBuffer = new FixedVertexConsumer(effectBuffer, this.red, this.green, this.blue, this.alpha);
            return VertexConsumers.dual(fixedEffectBuffer, parentBuffer);
        }
        return parentBuffer;
    }

    public void setColor(int red, int green, int blue, int alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    public void draw() {
        this.plainDrawer.draw();
    }

    public void setLayer(RenderLayer layer) {
        this.secondaryLayer = layer;
    }

    @Environment(EnvType.CLIENT)
    static class FixedVertexConsumer extends FixedColorVertexConsumer {
        private final VertexConsumer delegate;
        private double x;
        private double y;
        private double z;
        private float u;
        private float v;

        private FixedVertexConsumer(VertexConsumer delegate, int red, int green, int blue, int alpha) {
            this.delegate = delegate;
            super.fixedColor(red, green, blue, alpha);
        }

        @Override
        public void fixedColor(int red, int green, int blue, int alpha) {
        }

        @Override
        public VertexConsumer vertex(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
            return this;
        }

        @Override
        public VertexConsumer color(int red, int green, int blue, int alpha) {
            return this;
        }

        @Override
        public VertexConsumer texture(float u, float v) {
            this.u = u;
            this.v = v;
            return this;
        }

        @Override
        public VertexConsumer overlay(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer light(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            return this;
        }

        @Override
        public void vertex(float x, float y, float z, float red, float green, float blue, float alpha, float u, float v, int overlay, int light, float normalX, float normalY, float normalZ) {
            this.delegate.vertex(x, y, z).color(this.fixedRed, this.fixedGreen, this.fixedBlue, this.fixedAlpha).texture(u, v).next();
        }

        @Override
        public void next() {
            this.delegate.vertex(this.x, this.y, this.z).color(this.fixedRed, this.fixedGreen, this.fixedBlue, this.fixedAlpha).texture(this.u, this.v).next();
        }
    }
}
