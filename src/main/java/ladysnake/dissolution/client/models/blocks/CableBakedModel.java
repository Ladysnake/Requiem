package ladysnake.dissolution.client.models.blocks;

import ladysnake.dissolution.client.models.DissolutionModelLoader;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.blocks.alchemysystem.BlockPowerCable;
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
public class CableBakedModel implements IBakedModel {

    static final String LOCATION_NAME = "bakedpipe";
    public static final ModelResourceLocation BAKED_MODEL = new ModelResourceLocation(Reference.MOD_ID + ":" + LOCATION_NAME);

    public static final ResourceLocation INTERSECTION = new ResourceLocation(Reference.MOD_ID, "machine/pipe/pipe_intersection");
    public static final ResourceLocation SECTION = new ResourceLocation(Reference.MOD_ID, "machine/pipe/resonant_pipe_section");
    public static final ResourceLocation START = new ResourceLocation(Reference.MOD_ID, "machine/pipe/resonant_pipe_start");

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        List<BakedQuad> quads = new ArrayList<>();

        IExtendedBlockState extendedBlockState = (IExtendedBlockState) state;
        Boolean north = extendedBlockState.getValue(BlockPowerCable.NORTH);
        Boolean south = extendedBlockState.getValue(BlockPowerCable.SOUTH);
        Boolean west = extendedBlockState.getValue(BlockPowerCable.WEST);
        Boolean east = extendedBlockState.getValue(BlockPowerCable.EAST);
        Boolean up = extendedBlockState.getValue(BlockPowerCable.UP);
        Boolean down = extendedBlockState.getValue(BlockPowerCable.DOWN);

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
