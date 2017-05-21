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

public class BlockDriedLava extends Block {
	
    public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 3);

    public BlockDriedLava()
    {
    	super(Material.LAVA);
        this.setDefaultState(this.blockState.getBaseState().withProperty(AGE, Integer.valueOf(0)));
        
        this.setUnlocalizedName(Reference.Blocks.DRIED_LAVA.getUnlocalizedName());
    	this.setRegistryName(Reference.Blocks.DRIED_LAVA.getRegistryName());
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return ((Integer)state.getValue(AGE)).intValue();
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(AGE, Integer.valueOf(MathHelper.clamp(meta, 0, 3)));
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        if ((rand.nextInt(3) == 0 || this.countNeighbors(worldIn, pos) < 4) && worldIn.getLightFromNeighbors(pos) > 11 - ((Integer)state.getValue(AGE)).intValue() - 2)
        {
            this.slightlyMelt(worldIn, pos, state, rand, true);
        }
        else
        {
            worldIn.scheduleUpdate(pos, this, MathHelper.getInt(rand, 20, 40));
        }
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
    {
        if (blockIn == this)
        {
            int i = this.countNeighbors(worldIn, pos);

            if (i < 2)
            {
                this.turnIntoLava(worldIn, pos);
            }
        }
    }
    
    protected void turnIntoLava(World worldIn, BlockPos pos)
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

    private int countNeighbors(World world, BlockPos pos)
    {
        int i = 0;

        for (EnumFacing enumfacing : EnumFacing.values())
        {
            if (world.getBlockState(pos.offset(enumfacing)).getBlock() == this)
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

    protected void slightlyMelt(World world, BlockPos pos, IBlockState block, Random r, boolean b)
    {
        int i = ((Integer)block.getValue(AGE)).intValue();

        if (i < 3)
        {
        	world.setBlockState(pos, block.withProperty(AGE, Integer.valueOf(i + 1)), 2);
        	world.scheduleUpdate(pos, this, MathHelper.getInt(r, 20, 40));
        }
        else
        {
            this.turnIntoLava(world, pos);

            if (b)
            {
                for (EnumFacing enumfacing : EnumFacing.values())
                {
                    BlockPos blockpos = pos.offset(enumfacing);
                    IBlockState iblockstate = world.getBlockState(blockpos);

                    if (iblockstate.getBlock() == this)
                    {
                        this.slightlyMelt(world, blockpos, iblockstate, r, false);
                    }
                }
            }
        }
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] {AGE});
    }
    
    @Override
    public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state)
    {
        return ItemStack.EMPTY;
    }
}