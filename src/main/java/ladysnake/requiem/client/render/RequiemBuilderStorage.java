package ladysnake.requiem.client.render;

import ladysnake.satin.api.event.BufferBuildersInitCallback;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.RenderLayer;

import javax.annotation.Nullable;
import java.util.SortedMap;

public final class RequiemBuilderStorage implements BufferBuildersInitCallback {
    public static final RequiemBuilderStorage INSTANCE = new RequiemBuilderStorage();
    @Nullable
    private RequiemVertexConsumerProvider vertexConsumers;

    public RequiemVertexConsumerProvider getRequiemVertexConsumers() {
        assert this.vertexConsumers != null;
        return this.vertexConsumers;
    }

    @Override
    public void initBufferBuilders(BufferBuilderStorage bufferBuilderStorage, SortedMap<RenderLayer, BufferBuilder> sortedMap) {
        this.vertexConsumers = new RequiemVertexConsumerProvider(bufferBuilderStorage.getEntityVertexConsumers());
    }
}
