package ladysnake.dissolution.common.blocks;

import ladysnake.dissolution.api.GenericStackInventory;
import ladysnake.dissolution.api.IGenericInventoryProvider;
import ladysnake.dissolution.common.capabilities.CapabilityGenericInventoryProvider;
import ladysnake.dissolution.common.registries.EnumPowderOres;
import ladysnake.dissolution.common.tileentities.IPowderContainer;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public abstract class BlockPrimitiveContainer extends Block {
    public BlockPrimitiveContainer(Material materialIn) {
        super(materialIn);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack heldItem = playerIn.getHeldItem(hand);
        TileEntity tile = worldIn.getTileEntity(pos);
        if (heldItem.isEmpty() && playerIn.isSneaking()) {
            if(!worldIn.isRemote)
                worldIn.setBlockToAir(pos);
            return true;
        } else if(tile instanceof IPowderContainer && heldItem.hasCapability(CapabilityGenericInventoryProvider.CAPABILITY_GENERIC, null)) {
            GenericStackInventory<EnumPowderOres> powderInv = CapabilityGenericInventoryProvider.getInventory(heldItem, EnumPowderOres.class);
            if(powderInv != null) {
                ((IPowderContainer) tile).pourPowder(powderInv);
            }
        }
        return false;
    }

    @Override
    public void dropBlockAsItemWithChance(World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, float chance, int fortune) {}

    @Override
    public void breakBlock(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        TileEntity tile = worldIn.getTileEntity(pos);
        if(tile instanceof IPowderContainer) {
            IPowderContainer powderContainer = ((IPowderContainer)tile);
            ItemStack itemstack = new ItemStack(Item.getItemFromBlock(this));
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            nbttagcompound.setTag("BlockEntityTag", powderContainer.saveToNbt(new NBTTagCompound()));
            itemstack.setTagCompound(nbttagcompound);
            IGenericInventoryProvider inventoryProvider = itemstack.getCapability(CapabilityGenericInventoryProvider.CAPABILITY_GENERIC, null);
            if(inventoryProvider != null)
                inventoryProvider.setInventory(EnumPowderOres.class, powderContainer.getPowderInventory());
            spawnAsEntity(worldIn, pos, itemstack);
        }
        super.breakBlock(worldIn, pos, state);
    }

    @Nonnull
    @Override
    public ItemStack getPickBlock(@Nonnull IBlockState state, RayTraceResult target, @Nonnull World world, @Nonnull BlockPos pos, EntityPlayer player) {
        ItemStack itemstack = super.getPickBlock(state, target, world, pos, player);
        TileEntity tileEntity = world.getTileEntity(pos);
        if(tileEntity instanceof IPowderContainer) {
            NBTTagCompound nbttagcompound = ((IPowderContainer) tileEntity).saveToNbt(new NBTTagCompound());
            if (!nbttagcompound.hasNoTags()) {
                itemstack.setTagInfo("BlockEntityTag", nbttagcompound);
            }
        }
        return itemstack;
    }

    @Override
    public boolean isFullBlock(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Nonnull
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public abstract boolean hasTileEntity(IBlockState state);

    public abstract TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state);

}
