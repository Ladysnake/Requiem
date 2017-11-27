package ladysnake.dissolution.client.models;

import com.google.common.collect.Sets;
import com.google.gson.JsonParseException;
import ladysnake.dissolution.common.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.model.ITransformation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class is a stripped version of the game's model loading code, allowing
 * to load arbitrary models
 */
@Mod.EventBusSubscriber(value = Side.CLIENT, modid = Reference.MOD_ID)
@SideOnly(Side.CLIENT)
public class DissolutionModelLoader {

    private static final DissolutionModelLoader INSTANCE = new DissolutionModelLoader();
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger("Dissolution Model Loader");

    /**
     * Stores locations of models to load
     */
    private final Map<ResourceLocation, Set<ModelRotation>> modelsLocation = new HashMap<>();
    /**
     * Stores locations of textures to load
     */
    private Set<ResourceLocation> spritesLocation;
    /**
     * Associates a texture location with a sprite
     */
    private final Map<ResourceLocation, TextureAtlasSprite> sprites = new HashMap<>();
    /**
     * Associates a model location with an unbaked model
     */
    private final Map<ResourceLocation, ModelBlock> blockModelsLocation = new HashMap<>();
    /**
     * Associates a model location and a rotation with a baked model
     */
    private final Map<ResourceLocation, Map<ModelRotation, IBakedModel>> models = new HashMap<>();
    private final FaceBakery faceBakery = new FaceBakery();

    /**
     * Adds a model to be loaded. The X0_Y0 rotation is always loaded by default.
     * Should be called on ModelRegistryEvent
     *
     * @param modelLocation the location of the model
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
     * @param model the resource location used to load the model
     * @return the baked model previously loaded from the file
     */
    public static IBakedModel getModel(ResourceLocation model) {
        return getModel(model, ModelRotation.X0_Y0);
    }

    /**
     * Gets a baked model with a rotation. All rotations variants need to be
     * registered during initialization.
     *
     * @param modelLocation the resource location used to load the model
     * @param rotation      a previously registered rotation for this model
     * @return the baked model previously loaded from the file. If the model was not registered, attempts to search in Minecraft's main model registry
     */
    public static IBakedModel getModel(ResourceLocation modelLocation, ModelRotation rotation) {
        return INSTANCE.models.containsKey(modelLocation)
                ? INSTANCE.models.get(modelLocation).get(rotation)
                : Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getModelManager()
                .getModel(new ModelResourceLocation(modelLocation.toString()));
    }

    @SubscribeEvent
    public static void loadSpecialModels(TextureStitchEvent.Pre event) {
        LOGGER.info("Loading special models");
        for (ResourceLocation rl : INSTANCE.modelsLocation.keySet()) {
            try {
                INSTANCE.blockModelsLocation.put(rl, INSTANCE.loadModel(rl));
            } catch (IOException e) {
                LOGGER.warn(rl + ": an issue prevented the file from being read", e);
            } catch (JsonParseException e) {
                LOGGER.warn(rl + ": this json file isn't valid", e);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void loadSpecialSprites(TextureStitchEvent.Pre event) {
        INSTANCE.spritesLocation = INSTANCE.getItemsTextureLocations();
        INSTANCE.spritesLocation.forEach(rl -> INSTANCE.sprites.put(rl, event.getMap().registerSprite(rl)));
    }

    @SubscribeEvent
    public static void bakeSpecialModels(ModelBakeEvent event) {
        INSTANCE.modelsLocation.forEach(
                (rl, rotations) -> rotations.forEach(rot -> INSTANCE.models.computeIfAbsent(rl, res -> new HashMap<>())
                        .put(rot, INSTANCE.bakeModel(INSTANCE.blockModelsLocation.get(rl), rot))));
    }

    private Set<ResourceLocation> getItemsTextureLocations() {
        Set<ResourceLocation> set = Sets.newHashSet();

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

            ModelBlock lvt_5_2_ = ModelBlock.deserialize(reader);
            lvt_5_2_.name = location.toString();
            return lvt_5_2_;
        }
    }

    private ResourceLocation getModelLocation(ResourceLocation location) {
        return new ResourceLocation(location.getResourceDomain(), "models/" + location.getResourcePath() + ".json");
    }

    private IBakedModel bakeModel(ModelBlock modelBlockIn, ModelRotation modelRotationIn) {
        if (modelBlockIn == null)
            return null;
        TextureAtlasSprite textureatlassprite = this.sprites
                .get(new ResourceLocation(modelBlockIn.resolveTextureName("particle")));
        SimpleBakedModel.Builder simpleBakedModel$builder = (new SimpleBakedModel.Builder(modelBlockIn,
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
                        simpleBakedModel$builder.addGeneralQuad(this.makeBakedQuad(blockpart, blockpartface,
                                textureatlassprite1, enumfacing, modelRotationIn));
                    } else {
                        simpleBakedModel$builder.addFaceQuad(modelRotationIn.rotate(blockpartface.cullFace),
                                this.makeBakedQuad(blockpart, blockpartface, textureatlassprite1, enumfacing,
                                        modelRotationIn));
                    }
                }
            }

            return simpleBakedModel$builder.makeBakedModel();
        }
    }

    private BakedQuad makeBakedQuad(BlockPart partIn, BlockPartFace faceIn, TextureAtlasSprite sprite,
                                    EnumFacing facing, ITransformation rotation) {
        return this.faceBakery.makeBakedQuad(partIn.positionFrom, partIn.positionTo, faceIn, sprite, facing, rotation,
                partIn.partRotation, false, partIn.shade);
    }

}
