package ladysnake.dissolution.client.models.blocks;

import java.util.LinkedList;
import java.util.List;

import ladysnake.dissolution.client.renders.blocks.DissolutionModelLoader;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.blocks.alchemysystem.BlockCasing;
import ladysnake.dissolution.common.items.ItemAlchemyModule;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.property.IExtendedBlockState;

public class ModularMachineBakedModel implements IBakedModel {
	
	public static final String LOCATION_NAME = "bakedmodularmachine";
	public static final ModelResourceLocation BAKED_MODEL = new ModelResourceLocation(Reference.MOD_ID + ":" + LOCATION_NAME);
	private static final ResourceLocation CASING_TEXTURE = new ResourceLocation(Reference.MOD_ID, "blocks/machine_parts/wooden_alchemical_machine_structure");

	@Override
	public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
		if(state.getValue(BlockCasing.PART) == BlockCasing.EnumPartType.TOP)
			return DissolutionModelLoader.getModel(BlockCasing.CASING_TOP).getQuads(state, side, rand);

		List<BakedQuad> quads = new LinkedList<>();
		ModelRotation rotation;
		switch (state.getValue(BlockCasing.FACING)) {
		case EAST: rotation = ModelRotation.X0_Y90;
			break;
		case SOUTH: rotation = ModelRotation.X0_Y180;
			break;
		case WEST: rotation = ModelRotation.X0_Y270;
			break;
		default: rotation = ModelRotation.X0_Y0;
			break;
		}
		
        quads.addAll(DissolutionModelLoader.getModel(BlockCasing.CASING_BOTTOM).getQuads(state, side, rand));
        try {
	        for(Object module : ((IExtendedBlockState)state).getValue(BlockCasing.MODULES_PRESENT)) {
	        	if(module instanceof ItemAlchemyModule) {
	        		// System.out.println(rotation + ":" + ((ItemAlchemyModule) module).getModel(((IExtendedBlockState)state).getValue(BlockCasing.RUNNING)));
	        		quads.addAll(DissolutionModelLoader.getModel(((ItemAlchemyModule) module)
	        				.getModel(((IExtendedBlockState)state).getValue(BlockCasing.RUNNING)), rotation)
	        				.getQuads(state, side, rand));
	        	}
	        }
        } catch (NullPointerException e) {}
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
		return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(CASING_TEXTURE.toString());
	}

	@Override
	public ItemOverrideList getOverrides() {
		return null;
	}

}
