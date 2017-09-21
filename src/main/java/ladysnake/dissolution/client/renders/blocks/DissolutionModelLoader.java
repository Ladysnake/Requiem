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
import java.util.logging.Logger;

import com.google.common.collect.Sets;
import com.google.gson.JsonParseException;

import ladysnake.dissolution.common.Reference;
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
import net.minecraftforge.common.model.ITransformation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
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

	/** Stores locations of models to load */
	private final Map<ResourceLocation, Set<ModelRotation>> modelsLocation = new HashMap<>();
	/** Stores locations of textures to load */
	private Set<ResourceLocation> spritesLocation;
	/** Associates a texture location with a sprite */
	private final Map<ResourceLocation, TextureAtlasSprite> sprites = new HashMap<>();
	/** Associates a model location with an unbaked model */
	private final Map<ResourceLocation, ModelBlock> blockModelsLocation = new HashMap<>();
	/** Associates a model location and a rotation with a baked model */
	private final Map<ResourceLocation, Map<ModelRotation, IBakedModel>> models = new HashMap<>();
	private final FaceBakery faceBakery = new FaceBakery();

	/**
	 * Adds a model to be loaded. The X0_Y0 rotation is always loaded by default.
	 * Should be called on ModelRegistryEvent
	 * 
	 * @param modelLocation
	 *            the location of the model
	 */
	public static void addModel(ResourceLocation modelLocation, ModelRotation... rotations) {
		INSTANCE.modelsLocation.computeIfAbsent(modelLocation, rl -> new HashSet<>()).add(ModelRotation.X0_Y0);
		for (ModelRotation rot : rotations)
			INSTANCE.modelsLocation.get(modelLocation).add(rot);
	}

	public static void addAllModels(ResourceLocation... locations) {
		for (ResourceLocation loc : locations)
			addModel(loc);
	}

	/**
	 * Gets a loaded model in baked form
	 * 
	 * @param model
	 *            the resource location used to load the model
	 * @return the baked model previously loaded from the file
	 */
	public static IBakedModel getModel(ResourceLocation model) {
		return getModel(model, ModelRotation.X0_Y0);
	}

	/**
	 * Gets a baked model with a rotation. All rotations variants need to be
	 * registered during initialization.
	 * 
	 * @param modelLocation
	 * @param rotation
	 * @return the baked model previously loaded from the file
	 */
	public static IBakedModel getModel(ResourceLocation modelLocation, ModelRotation rotation) {
		return INSTANCE.models.get(modelLocation).get(rotation);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void loadSpecialModels(ModelRegistryEvent event) {
		for (ResourceLocation rl : INSTANCE.modelsLocation.keySet()) {
			try {
				INSTANCE.blockModelsLocation.put(rl, INSTANCE.loadModel(rl));
			} catch (IOException e) {
				Logger.getGlobal().warning(rl + ": an issue prevented the file from being read");
				e.printStackTrace();
			} catch (JsonParseException e) {
				Logger.getGlobal().warning(rl + ": this json file isn't valid");
				e.printStackTrace();
			}
		}
	}

	@SubscribeEvent
	public static void loadSpecialSprites(TextureStitchEvent.Pre event) {
		INSTANCE.spritesLocation = INSTANCE.getItemsTextureLocations();
		INSTANCE.spritesLocation.forEach(rl -> INSTANCE.sprites.put(rl, event.getMap().registerSprite(rl)));
	}

	@SubscribeEvent
	public static void bakeSpecialModels(TextureStitchEvent.Post event) {
		INSTANCE.modelsLocation.forEach(
				(rl, rotations) -> rotations.forEach(rot -> INSTANCE.models.computeIfAbsent(rl, res -> new HashMap<>())
						.put(rot, INSTANCE.bakeModel(INSTANCE.blockModelsLocation.get(rl), rot, false))));
		// INSTANCE.blockModelsLocation.forEach((rl, mb) ->
		// INSTANCE.models.computeIfAbsent(rl, res -> new
		// HashMap<>()).put(ModelRotation.X0_Y0, INSTANCE.bakeModel(mb)));
	}

	private Set<ResourceLocation> getItemsTextureLocations() {
		Set<ResourceLocation> set = Sets.<ResourceLocation>newHashSet();

		for (ResourceLocation resourcelocation : this.modelsLocation.keySet()) {
			ModelBlock modelblock = this.blockModelsLocation.get(resourcelocation);

			if (modelblock != null) {
				set.add(new ResourceLocation(modelblock.resolveTextureName("particle")));

				{
					for (BlockPart blockpart : modelblock.getElements()) {
						for (BlockPartFace blockpartface : blockpart.mapFaces.values()) {
							ResourceLocation resourcelocation1 = new ResourceLocation(
									modelblock.resolveTextureName(blockpartface.texture));
							set.add(resourcelocation1);
						}
					}
				}
			}
		}

		return set;
	}

	private ModelBlock loadModel(ResourceLocation location) throws IOException {
		try (IResource iresource = Minecraft.getMinecraft().getResourceManager()
				.getResource(this.getModelLocation(location));
				Reader reader = new InputStreamReader(iresource.getInputStream(), StandardCharsets.UTF_8)) {
			String s = location.getResourcePath();

			ModelBlock lvt_5_2_ = ModelBlock.deserialize(reader);
			lvt_5_2_.name = location.toString();
			ModelBlock modelblock1 = lvt_5_2_;
			return modelblock1;
		}
	}

	private ResourceLocation getModelLocation(ResourceLocation location) {
		return new ResourceLocation(location.getResourceDomain(), "models/" + location.getResourcePath() + ".json");
	}

	private IBakedModel bakeModel(ModelBlock modelBlockIn) {
		return bakeModel(modelBlockIn, ModelRotation.X0_Y0, false);
	}

	private IBakedModel bakeModel(ModelBlock modelBlockIn, ModelRotation modelRotationIn, boolean uvLocked) {
		TextureAtlasSprite textureatlassprite = this.sprites
				.get(new ResourceLocation(modelBlockIn.resolveTextureName("particle")));
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

	private BakedQuad makeBakedQuad(BlockPart partIn, BlockPartFace faceIn, TextureAtlasSprite sprite,
			EnumFacing facing, ITransformation rotation, boolean uvLocked) {
		return this.faceBakery.makeBakedQuad(partIn.positionFrom, partIn.positionTo, faceIn, sprite, facing, rotation,
				partIn.partRotation, uvLocked, partIn.shade);
	}

}
