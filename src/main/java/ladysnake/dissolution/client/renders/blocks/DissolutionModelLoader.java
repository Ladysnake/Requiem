package ladysnake.dissolution.client.renders.blocks;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;

import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.blocks.alchemysystem.BlockCasing;
import ladysnake.dissolution.common.items.ItemAlchemyModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockPart;
import net.minecraft.client.renderer.block.model.BlockPartFace;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.block.model.SimpleBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

/**
 * This class is a stripped version of the game's model loading code, allowing
 * to load arbitrary models
 *
 */
@Mod.EventBusSubscriber(value = Side.CLIENT, modid = Reference.MOD_ID)
public class DissolutionModelLoader {
	
	private static final DissolutionModelLoader INSTANCE = new DissolutionModelLoader();

	/**Stores locations of models to load*/
	private final List<ResourceLocation> modelsLocation = new ArrayList<>();
	/**Stores locations of textures to load*/
	private Set<ResourceLocation> spritesLocation;
	/**Associates a texture location with a sprite*/
	private final Map<ResourceLocation, TextureAtlasSprite> sprites = new HashMap<>();
	/**Associates a model location with an unbaked model*/
	private final Map<ResourceLocation, ModelBlock> blockModelsLocation = new HashMap<>();
	/**Associates a model location with a baked model*/
	private final Map<ResourceLocation, IBakedModel> models = new HashMap<>();
	private final FaceBakery faceBakery = new FaceBakery();
	
	public static void addModel(ResourceLocation modelLocation) {
		INSTANCE.modelsLocation.add(modelLocation);
	}
	
	public static IBakedModel getModel(ResourceLocation model) {
		return INSTANCE.models.get(model);
	}

	@SubscribeEvent
	public static void loadSpecialModels(ModelRegistryEvent event) {
		addModel(BlockCasing.CASING_BOTTOM);
		addModel(BlockCasing.CASING_TOP);
		ItemAlchemyModule.getModulesModels().values().forEach(DissolutionModelLoader::addModel);
		INSTANCE.modelsLocation.forEach(rl -> INSTANCE.blockModelsLocation.put(rl, INSTANCE.loadModel(rl)));
	}

	@SubscribeEvent
	public static void loadSpecialSprites(TextureStitchEvent.Pre event) {
		INSTANCE.spritesLocation = INSTANCE.getItemsTextureLocations();
		System.out.println(INSTANCE.spritesLocation);
		INSTANCE.spritesLocation.forEach(rl -> INSTANCE.sprites.put(rl, event.getMap().registerSprite(rl)));
	}
	
	@SubscribeEvent
	public static void bakeSpecialModels(TextureStitchEvent.Post event) {
		INSTANCE.blockModelsLocation.forEach((rl, mb) -> INSTANCE.models.put(rl, INSTANCE.bakeModel(mb)));
		System.out.println(INSTANCE.models);
	}
	
	private Set<ResourceLocation> getItemsTextureLocations()
    {
        Set<ResourceLocation> set = Sets.<ResourceLocation>newHashSet();

        for (ResourceLocation resourcelocation : this.modelsLocation)
        {
            ModelBlock modelblock = this.blockModelsLocation.get(resourcelocation);

            if (modelblock != null)
            {
                set.add(new ResourceLocation(modelblock.resolveTextureName("particle")));

                {
                    for (BlockPart blockpart : modelblock.getElements())
                    {
                        for (BlockPartFace blockpartface : blockpart.mapFaces.values())
                        {
                            ResourceLocation resourcelocation1 = new ResourceLocation(modelblock.resolveTextureName(blockpartface.texture));
                            set.add(resourcelocation1);
                        }
                    }
                }
            }
        }

        return set;
    }

	protected ModelBlock loadModel(ResourceLocation location) {
		try (IResource iresource = Minecraft.getMinecraft().getResourceManager()
				.getResource(this.getModelLocation(location));
				Reader reader = new InputStreamReader(iresource.getInputStream(), StandardCharsets.UTF_8)) {
			String s = location.getResourcePath();

			ModelBlock lvt_5_2_ = ModelBlock.deserialize(reader);
			lvt_5_2_.name = location.toString();
			ModelBlock modelblock1 = lvt_5_2_;
			return modelblock1;
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("The resource could not be retrieved", e);
		}
	}

	protected ResourceLocation getModelLocation(ResourceLocation location) {
		return new ResourceLocation(location.getResourceDomain(), "models/" + location.getResourcePath() + ".json");
	}
	
	private IBakedModel bakeModel(ModelBlock modelBlockIn) {
		return bakeModel(modelBlockIn, ModelRotation.X0_Y0, false);
	}

	private IBakedModel bakeModel(ModelBlock modelBlockIn, ModelRotation modelRotationIn, boolean uvLocked) {
		TextureAtlasSprite textureatlassprite = this.sprites.get(new ResourceLocation(modelBlockIn.resolveTextureName("particle")));
		SimpleBakedModel.Builder simplebakedmodel$builder = (new SimpleBakedModel.Builder(modelBlockIn,
				modelBlockIn.createOverrides())).setTexture(textureatlassprite);

		if (modelBlockIn.getElements().isEmpty()) {
			return null;
		} else {
			for (BlockPart blockpart : modelBlockIn.getElements()) {
				for (EnumFacing enumfacing : blockpart.mapFaces.keySet()) {
					BlockPartFace blockpartface = blockpart.mapFaces.get(enumfacing);
					TextureAtlasSprite textureatlassprite1 = this.sprites
							.get(new ResourceLocation(modelBlockIn.resolveTextureName(blockpartface.texture)));

					if (blockpartface.cullFace == null || !net.minecraftforge.common.model.TRSRTransformation
							.isInteger(modelRotationIn.getMatrix())) {
						simplebakedmodel$builder.addGeneralQuad(this.makeBakedQuad(blockpart, blockpartface,
								textureatlassprite1, enumfacing, modelRotationIn, uvLocked));
					} else {
						simplebakedmodel$builder.addFaceQuad(modelRotationIn.rotate(blockpartface.cullFace),
								this.makeBakedQuad(blockpart, blockpartface, textureatlassprite1, enumfacing,
										modelRotationIn, uvLocked));
					}
				}
			}

			return simplebakedmodel$builder.makeBakedModel();
		}
	}

	private BakedQuad makeBakedQuad(BlockPart p_177589_1_, BlockPartFace p_177589_2_, TextureAtlasSprite p_177589_3_,
			EnumFacing p_177589_4_, ModelRotation p_177589_5_, boolean p_177589_6_) {
		return makeBakedQuad(p_177589_1_, p_177589_2_, p_177589_3_, p_177589_4_,
				(net.minecraftforge.common.model.ITransformation) p_177589_5_, p_177589_6_);
	}

	protected BakedQuad makeBakedQuad(BlockPart p_177589_1_, BlockPartFace p_177589_2_, TextureAtlasSprite p_177589_3_,
			EnumFacing p_177589_4_, net.minecraftforge.common.model.ITransformation p_177589_5_, boolean p_177589_6_) {
		return this.faceBakery.makeBakedQuad(p_177589_1_.positionFrom, p_177589_1_.positionTo, p_177589_2_, p_177589_3_,
				p_177589_4_, p_177589_5_, p_177589_1_.partRotation, p_177589_6_, p_177589_1_.shade);
	}

}
