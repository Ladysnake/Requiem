package ladysnake.dissolution.common.blocks;

import ladysnake.dissolution.common.OreDictHelper;
import ladysnake.dissolution.common.tileentities.TileEntityMortar;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockMortar extends Block {
    public BlockMortar() {
        super(Material.ROCK);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileEntity tile = worldIn.getTileEntity(pos);
        if(tile instanceof TileEntityMortar) {
            if(OreDictHelper.doesItemMatch(playerIn.getHeldItem(hand), OreDictHelper.PESTLE, OreDictHelper.PESTLE_AND_MORTAR))
                ((TileEntityMortar) tile).crush();
            else if (playerIn.getHeldItem(hand).isEmpty() && playerIn.isSneaking())
                worldIn.setBlockToAir(pos);
            else
                ((TileEntityMortar) tile).putItem(playerIn.getHeldItem(hand));
        }
        return true;
    }

    @Override
    public void dropBlockAsItemWithChance(World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, float chance, int fortune) {}



    @Override
    public void breakBlock(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        TileEntity tile = worldIn.getTileEntity(pos);
        if(tile instanceof TileEntityMortar) {
            TileEntityMortar tileMortar = ((TileEntityMortar)tile);
            ItemStack itemstack = new ItemStack(Item.getItemFromBlock(this));
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            nbttagcompound.setTag("BlockEntityTag", tileMortar.saveToNbt(new NBTTagCompound()));
            itemstack.setTagCompound(nbttagcompound);
            spawnAsEntity(worldIn, pos, itemstack);
        }
        super.breakBlock(worldIn, pos, state);
    }

    @Nonnull
    @Override
    public ItemStack getPickBlock(@Nonnull IBlockState state, RayTraceResult target, @Nonnull World world, @Nonnull BlockPos pos, EntityPlayer player) {
        ItemStack itemstack = super.getItem(world, pos, state);
        TileEntity tileEntity = world.getTileEntity(pos);
        if(tileEntity instanceof TileEntityMortar) {
            NBTTagCompound nbttagcompound = ((TileEntityMortar) tileEntity).saveToNbt(new NBTTagCompound());
            if (!nbttagcompound.hasNoTags()) {
                itemstack.setTagInfo("BlockEntityTag", nbttagcompound);
            }
        }

        return itemstack;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return new TileEntityMortar();
    }
}
