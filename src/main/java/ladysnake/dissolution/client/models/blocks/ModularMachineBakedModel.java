package ladysnake.dissolution.client.models.blocks;

import ladysnake.dissolution.client.models.DissolutionModelLoader;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.blocks.alchemysystem.BlockCasing;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
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
import java.util.Objects;
import java.util.stream.Collectors;

@SideOnly(Side.CLIENT)
public class ModularMachineBakedModel implements IBakedModel {

    static final String LOCATION_NAME = "bakedmodularmachine";
    public static final ModelResourceLocation BAKED_MODEL = new ModelResourceLocation(Reference.MOD_ID + ":" + LOCATION_NAME);
    private static final ResourceLocation CASING_TEXTURE = new ResourceLocation(Reference.MOD_ID, "machines/wooden_machine_casing");

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {

        List<IBakedModel> parts = new ArrayList<>();
        ModelRotation rotation = getRotationFromFacing((IExtendedBlockState) state);

        if (state.getValue(BlockCasing.PART) == BlockCasing.EnumPartType.TOP)
            parts.add(DissolutionModelLoader.getModel(BlockCasing.CASING_TOP));
        else {
            parts.add(DissolutionModelLoader.getModel(BlockCasing.CASING_BOTTOM));
            for (Object module : ((IExtendedBlockState) state).getValue(BlockCasing.MODULES_PRESENT)) {
                if (module instanceof ResourceLocation) {
                    parts.add(DissolutionModelLoader.getModel((ResourceLocation) module, rotation));
                }
            }
        }
        ResourceLocation plugModel = null;
        if ((plugModel = ((IExtendedBlockState) state).getValue(BlockCasing.PLUG_NORTH)) != null)
            parts.add(DissolutionModelLoader.getModel(plugModel, ModelRotation.X0_Y270));
        if ((plugModel = ((IExtendedBlockState) state).getValue(BlockCasing.PLUG_SOUTH)) != null)
            parts.add(DissolutionModelLoader.getModel(plugModel, ModelRotation.X0_Y90));
        if ((plugModel = ((IExtendedBlockState) state).getValue(BlockCasing.PLUG_WEST)) != null)
            parts.add(DissolutionModelLoader.getModel(plugModel, ModelRotation.X0_Y180));
        if ((plugModel = ((IExtendedBlockState) state).getValue(BlockCasing.PLUG_EAST)) != null)
            parts.add(DissolutionModelLoader.getModel(plugModel, ModelRotation.X0_Y0));

        return parts.stream().filter(Objects::nonNull).map(m -> m.getQuads(state, side, rand)).flatMap(List::stream).collect(Collectors.toList());
    }

    private ModelRotation getRotationFromFacing(IExtendedBlockState state) {
        ModelRotation rotation;
        switch (state.getValue(BlockCasing.FACING)) {
            case EAST:
                rotation = ModelRotation.X0_Y90;
                break;
            case SOUTH:
                rotation = ModelRotation.X0_Y180;
                break;
            case WEST:
                rotation = ModelRotation.X0_Y270;
                break;
            default:
                rotation = ModelRotation.X0_Y0;
                break;
        }
        return rotation;
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
        return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(CASING_TEXTURE.toString());
    }

    @Nonnull
    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.NONE;
    }

}
