package ladysnake.dissolution.common.blocks;

import ladysnake.dissolution.api.IDialogueStats;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import javax.annotation.Nonnull;

public class BlockShrine extends Block {

    public static final PropertyBool WATER = PropertyBool.create("water");
    public static final PropertyDirection FACING = BlockHorizontal.FACING;

    public BlockShrine() {
        super(Material.ROCK);
        this.setHardness(50);
        this.setResistance(200);
        this.setDefaultState(super.getDefaultState().withProperty(WATER, false));
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!state.getValue(WATER) && playerIn.getHeldItem(hand).hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
            FluidStack water = playerIn.getHeldItem(hand).getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)
                    .drain(new FluidStack(FluidRegistry.WATER, Fluid.BUCKET_VOLUME), true);
            if (water != null) {
                worldIn.setBlockState(pos, state.withProperty(WATER, true));
                worldIn.playSound(playerIn, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1, 1);
            }
            return true;
        } else if (state.getValue(WATER)) {
            IDialogueStats stats = CapabilityIncorporealHandler.getHandler(playerIn).getDialogueStats();
            stats.resetProgress();
            return true;
        }
        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }

    @Nonnull
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, WATER, FACING);
    }

    @Nonnull
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return super.getStateFromMeta(meta)
                .withProperty(WATER, (meta & 0b1000) > 0)
                .withProperty(FACING, EnumFacing.getHorizontal(meta & 0b0111));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex() | (state.getValue(WATER) ? 0b1000 : 0);
    }
}
