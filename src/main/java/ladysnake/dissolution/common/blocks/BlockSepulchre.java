package ladysnake.dissolution.common.blocks;

import ladysnake.dissolution.api.corporeality.IIncorporealHandler;
import ladysnake.dissolution.api.corporeality.ISoulInteractable;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.init.ModBlocks;
import ladysnake.dissolution.common.items.ItemBurial;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;
import java.util.Random;
import java.util.function.Supplier;

public class BlockSepulchre extends BlockHorizontal implements ISoulInteractable {

    public static final PropertyEnum<BlockSepulchre.EnumPartType> PART = PropertyEnum.create("part",
            BlockSepulchre.EnumPartType.class);
    private static final float TEXEL = 1/16f;
    protected static final AxisAlignedBB AABB_X = new AxisAlignedBB(-12* TEXEL, 0, -2* TEXEL, 1.0D + 12* TEXEL, 12* TEXEL, 18* TEXEL);
    protected static final AxisAlignedBB AABB_Z = new AxisAlignedBB(-2* TEXEL, 0.0D, -12* TEXEL, 18* TEXEL, 12* TEXEL, 1.0D + 12* TEXEL);

    private final Supplier<ItemBurial> itemSupplier;

    public BlockSepulchre(Material material, Supplier<ItemBurial> itemSupplier) {
        super(material);
        this.itemSupplier = itemSupplier;
        this.setDefaultState(this.blockState.getBaseState().withProperty(PART, BlockSepulchre.EnumPartType.CENTER));
        this.setHardness(1f);
        this.setHarvestLevel("pickaxe", 0);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        IIncorporealHandler handler = CapabilityIncorporealHandler.getHandler(playerIn);
        if (handler.getPossessed() instanceof EntityLivingBase) {
            EntityLivingBase possessed = (EntityLivingBase) handler.getPossessed();
            // TODO make something proper because I'm way too lazy right now
            if (worldIn.isDaytime() && worldIn.getPlayers(EntityPlayerMP.class, p -> true).size() == 1)
                worldIn.setWorldTime(14000);
        }
        return true;
    }

    public enum EnumPartType implements IStringSerializable {
        SIDE, CENTER;

        public String toString() {
            return this.name().toLowerCase(Locale.ENGLISH);
        }

        @Nonnull
        public String getName() {
            return this.toString();
        }
    }

    @Override
    @Deprecated
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    @Deprecated
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (state.getValue(PART) == BlockSepulchre.EnumPartType.SIDE) {
            BlockPos center = getCenter(worldIn, pos);
            if(center == null) worldIn.setBlockToAir(pos);
        }
    }

    private BlockPos getCenter(World world, BlockPos pos) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                BlockPos pos1 = pos.add(i, 0, j);
                if (pos.equals(pos1)) continue;
                IBlockState state = world.getBlockState(pos1);
                if (state.getBlock() == this && state.getValue(PART) == EnumPartType.CENTER)
                    return pos1;
            }
        }
        return null;
    }

    @Nonnull
    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return state.getValue(PART) == BlockSepulchre.EnumPartType.SIDE ? Items.AIR : itemSupplier.get();
    }

    @Nonnull
    @Override
    @Deprecated
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        if (state.getValue(PART) == EnumPartType.CENTER) {
                return (state.getValue(FACING).getAxis() == EnumFacing.Axis.X) ? AABB_X : AABB_Z;
        }
        return new AxisAlignedBB(0, 0, 0, 0, 0, 0);
    }

    @Nullable
    @Override
    @Deprecated
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, @Nonnull IBlockAccess worldIn, @Nonnull BlockPos pos) {
        return /*blockState.getValue(PART) == EnumPartType.CENTER ? */super.getCollisionBoundingBox(blockState, worldIn, pos)/* : null*/;
    }

    @Override
    @Deprecated
    public boolean isFullBlock(IBlockState state) {
        return false;
    }

    @Override
    @Deprecated
    public boolean isNormalCube(IBlockState state) {
        return false;
    }

    @Override
    @Deprecated
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public void dropBlockAsItemWithChance(World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, float chance, int fortune) {
        if (state.getValue(PART) == BlockSepulchre.EnumPartType.CENTER) {
            super.dropBlockAsItemWithChance(worldIn, pos, state, chance, 0);
        }
    }

    @Nonnull
    @Override
    @Deprecated
    public EnumPushReaction getMobilityFlag(IBlockState state) {
        return EnumPushReaction.DESTROY;
    }

    @Nonnull
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Nonnull
    @Override
    @Deprecated
    public ItemStack getItem(World worldIn, BlockPos pos, @Nonnull IBlockState state) {
        return new ItemStack(itemSupplier.get());
    }

    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
        if (player.capabilities.isCreativeMode && state.getValue(PART) == BlockSepulchre.EnumPartType.SIDE) {
            BlockPos blockpos = pos.offset(state.getValue(FACING).getOpposite());

            if (worldIn.getBlockState(blockpos).getBlock() == this) {
                worldIn.setBlockToAir(blockpos);
            }
        }
    }

    @Nonnull
    @Deprecated
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing enumfacing = EnumFacing.getHorizontal(meta);
        return (meta & 8) > 0
                ? this.getDefaultState().withProperty(PART, BlockSepulchre.EnumPartType.SIDE).withProperty(FACING,
                enumfacing)
                : this.getDefaultState().withProperty(PART, BlockSepulchre.EnumPartType.CENTER).withProperty(FACING,
                enumfacing);
    }

    @Nonnull
    @Deprecated
    public IBlockState withRotation(@Nonnull IBlockState state, Rotation rot) {
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Nonnull
    @Deprecated
    public IBlockState withMirror(@Nonnull IBlockState state, Mirror mirrorIn) {
        return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
    }

    public int getMetaFromState(IBlockState state) {
        int i;
        i = state.getValue(FACING).getHorizontalIndex();

        if (state.getValue(PART) == BlockSepulchre.EnumPartType.SIDE) {
            i |= 8;
        }

        return i;
    }

    @Nonnull
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, PART);
    }

    @Nonnull
    @Override
    @Deprecated
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return state.getValue(PART) == EnumPartType.CENTER ? EnumBlockRenderType.MODEL : EnumBlockRenderType.INVISIBLE;
    }
}
