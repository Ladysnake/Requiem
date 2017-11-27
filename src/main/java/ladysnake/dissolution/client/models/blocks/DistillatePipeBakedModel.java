package ladysnake.dissolution.client.models.blocks;

import ladysnake.dissolution.client.models.DissolutionModelLoader;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.blocks.alchemysystem.BlockDistillatePipe;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class DistillatePipeBakedModel implements IBakedModel {

    public static final String LOCATION_NAME = "baked_essentia_pipe";
    public static final ModelResourceLocation BAKED_MODEL = new ModelResourceLocation(Reference.MOD_ID + ":" + LOCATION_NAME);

    public static final ResourceLocation INTERSECTION = new ResourceLocation(Reference.MOD_ID, "machine/pipe/pipe_intersection");
    public static final ResourceLocation SECTION = new ResourceLocation(Reference.MOD_ID, "machine/pipe/essential_pipe_section");
    public static final ResourceLocation START = new ResourceLocation(Reference.MOD_ID, "machine/pipe/essential_pipe_start");

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        List<BakedQuad> quads = new ArrayList<>();

        IExtendedBlockState extendedBlockState = (IExtendedBlockState) state;
        Boolean north = extendedBlockState.getValue(BlockDistillatePipe.NORTH);
        Boolean south = extendedBlockState.getValue(BlockDistillatePipe.SOUTH);
        Boolean west = extendedBlockState.getValue(BlockDistillatePipe.WEST);
        Boolean east = extendedBlockState.getValue(BlockDistillatePipe.EAST);
        Boolean up = extendedBlockState.getValue(BlockDistillatePipe.UP);
        Boolean down = extendedBlockState.getValue(BlockDistillatePipe.DOWN);

        if (north && south && !(west || east || up || down)) {
            quads.addAll(DissolutionModelLoader.getModel(SECTION, ModelRotation.X0_Y0).getQuads(state, side, rand));
            return quads;
        } else if (west && east && !(north || south || up || down)) {
            quads.addAll(DissolutionModelLoader.getModel(SECTION, ModelRotation.X0_Y90).getQuads(state, side, rand));
            return quads;
        } else if (up && down && !(north || south || west || east)) {
            quads.addAll(DissolutionModelLoader.getModel(SECTION, ModelRotation.X90_Y0).getQuads(state, side, rand));
            return quads;
        }

        quads.addAll(DissolutionModelLoader.getModel(INTERSECTION).getQuads(state, side, rand));

        if (up)
            quads.addAll(DissolutionModelLoader.getModel(START, ModelRotation.X270_Y0).getQuads(state, side, rand));
        if (down)
            quads.addAll(DissolutionModelLoader.getModel(START, ModelRotation.X90_Y0).getQuads(state, side, rand));
        if (north)
            quads.addAll(DissolutionModelLoader.getModel(START, ModelRotation.X0_Y0).getQuads(state, side, rand));
        if (south)
            quads.addAll(DissolutionModelLoader.getModel(START, ModelRotation.X0_Y180).getQuads(state, side, rand));
        if (west)
            quads.addAll(DissolutionModelLoader.getModel(START, ModelRotation.X0_Y270).getQuads(state, side, rand));
        if (east)
            quads.addAll(DissolutionModelLoader.getModel(START, ModelRotation.X0_Y90).getQuads(state, side, rand));
        return quads;
    }

    @Override
    public boolean isAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Nonnull
    @Override
    public TextureAtlasSprite getParticleTexture() {
        return DissolutionModelLoader.getModel(START).getParticleTexture();
    }

    @Nonnull
    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.NONE;
    }

}
