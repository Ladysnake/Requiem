package ladysnake.dissolution.client.models.blocks;

import java.util.ArrayList;
import java.util.List;

import ladysnake.dissolution.client.renders.blocks.DissolutionModelLoader;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.blocks.alchemysystem.BlockEssentiaPipe;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.property.IExtendedBlockState;

public class EssentiaPipeBakedModel implements IBakedModel {
	
	public static final String LOCATION_NAME = "baked_essentia_pipe";
	public static final ModelResourceLocation BAKED_MODEL = new ModelResourceLocation(Reference.MOD_ID + ":" + LOCATION_NAME);
	
	public static final ResourceLocation CENTER = new ResourceLocation(Reference.MOD_ID, "machine/pipe/pipe_center");
	public static final ResourceLocation DOWN = new ResourceLocation(Reference.MOD_ID, "machine/pipe/pipe_down");
	public static final ResourceLocation EAST = new ResourceLocation(Reference.MOD_ID, "machine/pipe/pipe_east");
	public static final ResourceLocation NORTH = new ResourceLocation(Reference.MOD_ID, "machine/pipe/pipe_north");
	public static final ResourceLocation SOUTH = new ResourceLocation(Reference.MOD_ID, "machine/pipe/pipe_south");
	public static final ResourceLocation UP = new ResourceLocation(Reference.MOD_ID, "machine/pipe/pipe_up");
	public static final ResourceLocation WEST = new ResourceLocation(Reference.MOD_ID, "machine/pipe/pipe_west");

	@Override
	public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
    	List<BakedQuad> quads = new ArrayList<>();
    	
    	quads.addAll(DissolutionModelLoader.getModel(CENTER).getQuads(state, side, rand));

    	IExtendedBlockState extendedBlockState = (IExtendedBlockState) state;
        Boolean north = extendedBlockState.getValue(BlockEssentiaPipe.NORTH);
        Boolean south = extendedBlockState.getValue(BlockEssentiaPipe.SOUTH);
        Boolean west = extendedBlockState.getValue(BlockEssentiaPipe.WEST);
        Boolean east = extendedBlockState.getValue(BlockEssentiaPipe.EAST);
        Boolean up = extendedBlockState.getValue(BlockEssentiaPipe.UP);
        Boolean down = extendedBlockState.getValue(BlockEssentiaPipe.DOWN);
        
        if(up)
        	quads.addAll(DissolutionModelLoader.getModel(UP).getQuads(state, side, rand));
        if(down)
        	quads.addAll(DissolutionModelLoader.getModel(DOWN).getQuads(state, side, rand));
        if(north)
        	quads.addAll(DissolutionModelLoader.getModel(NORTH).getQuads(state, side, rand));
        if(south)
        	quads.addAll(DissolutionModelLoader.getModel(SOUTH).getQuads(state, side, rand));
        if(west)
        	quads.addAll(DissolutionModelLoader.getModel(WEST).getQuads(state, side, rand));
        if(east)
        	quads.addAll(DissolutionModelLoader.getModel(EAST).getQuads(state, side, rand));
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

	@Override
	public TextureAtlasSprite getParticleTexture() {
		return DissolutionModelLoader.getModel(CENTER).getParticleTexture();
	}

	@Override
	public ItemOverrideList getOverrides() {
		return null;
	}

}
