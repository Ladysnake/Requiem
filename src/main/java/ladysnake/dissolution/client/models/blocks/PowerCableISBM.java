package ladysnake.dissolution.client.models.blocks;

import com.google.common.collect.ImmutableSet;
import ladysnake.dissolution.common.Reference;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.IModelState;

import java.util.Collection;
import java.util.function.Function;

public class PowerCableISBM implements IModel {

    public static final ResourceLocation PIPE_TEXTURE = new ResourceLocation(Reference.MOD_ID, "blocks/power_cable");
    public static final ResourceLocation PIPE_TEXTURE_UNPOWERED = new ResourceLocation(Reference.MOD_ID, "blocks/power_cable_unpowered");

    @Override
    public IBakedModel bake(IModelState state, VertexFormat format,
                            Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        return new CableBakedModel();
    }

    @Override
    public Collection<ResourceLocation> getTextures() {
        return ImmutableSet.of(PIPE_TEXTURE, PIPE_TEXTURE_UNPOWERED);
    }

}
