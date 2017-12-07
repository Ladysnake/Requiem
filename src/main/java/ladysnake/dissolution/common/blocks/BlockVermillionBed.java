package ladysnake.dissolution.common.blocks;

import ladysnake.dissolution.common.init.ModItems;
import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Random;

public class BlockVermillionBed extends BlockBed {

    public BlockVermillionBed() {
        super();
        this.hasTileEntity = false;
    }

    @Nonnull
    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return state.getValue(PART) == BlockBed.EnumPartType.FOOT ? Items.AIR : ModItems.VERMILLION_BED;
    }

    @Nonnull
    @Override
    public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
        return new ItemStack(ModItems.VERMILLION_BED);
    }

    @Override
    public void dropBlockAsItemWithChance(@Nonnull World worldIn, @Nonnull BlockPos pos, IBlockState state, float chance, int fortune) {
        if (state.getValue(PART) == BlockBed.EnumPartType.HEAD) {
            spawnAsEntity(worldIn, pos, new ItemStack(ModItems.VERMILLION_BED));
        }
    }

    @Nonnull
    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return false;
    }
}
