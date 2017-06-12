package ladysnake.dissolution.common.blocks;

import java.util.Random;

import ladysnake.dissolution.common.Reference;
import net.minecraft.block.Block;
import net.minecraft.block.BlockIce;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class BlockDriedLava extends BlockIce {
	
    public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 3);

    public BlockDriedLava()
    {
        this.setDefaultState(this.blockState.getBaseState().withProperty(AGE, Integer.valueOf(0)));
        this.setUnlocalizedName(Reference.Blocks.DRIED_LAVA.getUnlocalizedName());
    	this.setRegistryName(Reference.Blocks.DRIED_LAVA.getRegistryName());
    }

    public int getMetaFromState(IBlockState state)
    {
        return ((Integer)state.getValue(AGE)).intValue();
    }


    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(AGE, Integer.valueOf(MathHelper.clamp(meta, 0, 3)));
    }

    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        if ((rand.nextInt(3) == 0 || this.countNeighbors(worldIn, pos) < 4) && worldIn.getLightFromNeighbors(pos) > 11 - ((Integer)state.getValue(AGE)).intValue() - state.getLightOpacity())
        {
            this.slightlyMelt(worldIn, pos, state, rand, true);
        }
        else
        {
            worldIn.scheduleUpdate(pos, this, MathHelper.getInt(rand, 20, 40));
        }
    }

    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
    {
        if (blockIn == this)
        {
            int i = this.countNeighbors(worldIn, pos);

            if (i < 2)
            {
                if (worldIn.provider.doesWaterVaporize())
                {
                    worldIn.setBlockToAir(pos);
                }
                else
                {
                    this.dropBlockAsItem(worldIn, pos, worldIn.getBlockState(pos), 0);
                    worldIn.setBlockState(pos, Blocks.LAVA.getDefaultState());
                    worldIn.neighborChanged(pos, Blocks.LAVA, pos);
                }
            }
        }
    }

    private int countNeighbors(World p_185680_1_, BlockPos p_185680_2_)
    {
        int i = 0;

        for (EnumFacing enumfacing : EnumFacing.values())
        {
            if (p_185680_1_.getBlockState(p_185680_2_.offset(enumfacing)).getBlock() == this)
            {
                ++i;

                if (i >= 4)
                {
                    return i;
                }
            }
        }

        return i;
    }

    protected void slightlyMelt(World worldIn, BlockPos pos, IBlockState p_185681_3_, Random p_185681_4_, boolean p_185681_5_)
    {
        int i = ((Integer)p_185681_3_.getValue(AGE)).intValue();

        if (i < 3)
        {
            worldIn.setBlockState(pos, p_185681_3_.withProperty(AGE, Integer.valueOf(i + 1)), 2);
            worldIn.scheduleUpdate(pos, this, MathHelper.getInt(p_185681_4_, 20, 40));
        }
        else
        {
            if (worldIn.provider.doesWaterVaporize())
            {
                worldIn.setBlockToAir(pos);
            }
            else
            {
                this.dropBlockAsItem(worldIn, pos, worldIn.getBlockState(pos), 0);
                worldIn.setBlockState(pos, Blocks.LAVA.getDefaultState());
                worldIn.neighborChanged(pos, Blocks.LAVA, pos);
            }

            if (p_185681_5_)
            {
                for (EnumFacing enumfacing : EnumFacing.values())
                {
                    BlockPos blockpos = pos.offset(enumfacing);
                    IBlockState iblockstate = worldIn.getBlockState(blockpos);

                    if (iblockstate.getBlock() == this)
                    {
                        this.slightlyMelt(worldIn, blockpos, iblockstate, p_185681_4_, false);
                    }
                }
            }
        }
    }

    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] {AGE});
    }

    public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state)
    {
        return ItemStack.EMPTY;
    }
}