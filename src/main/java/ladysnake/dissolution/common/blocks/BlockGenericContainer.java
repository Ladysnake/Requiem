package ladysnake.dissolution.common.blocks;

import ladysnake.dissolution.api.GenericStackInventory;
import ladysnake.dissolution.api.IGenericInventoryProvider;
import ladysnake.dissolution.common.capabilities.CapabilityGenericInventoryProvider;
import ladysnake.dissolution.common.inventory.InputItemHandler;
import ladysnake.dissolution.common.registries.EnumPowderOres;
import ladysnake.dissolution.common.tileentities.PowderContainer;
import ladysnake.dissolution.common.tileentities.TileEntityCrucible;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public abstract class BlockGenericContainer extends Block {
    public BlockGenericContainer(Material materialIn) {
        super(materialIn);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack heldItem = playerIn.getHeldItem(hand);
        TileEntity tile = worldIn.getTileEntity(pos);
        if (heldItem.isEmpty() && playerIn.isSneaking() && tile instanceof PowderContainer) {
            if(!worldIn.isRemote) {
                playerIn.addItemStackToInventory(getDroppedItem(worldIn, pos));
                ((PowderContainer) tile).dropContent();
                ((PowderContainer) tile).setShouldDrop(false);
                worldIn.setBlockToAir(pos);
            }
            return true;
        } else if(tile instanceof PowderContainer && heldItem.hasCapability(CapabilityGenericInventoryProvider.CAPABILITY_GENERIC, null)) {
            GenericStackInventory<EnumPowderOres> powderInv = CapabilityGenericInventoryProvider.getInventory(heldItem, EnumPowderOres.class);
            if(powderInv != null) {
                ((PowderContainer) tile).pourPowder(powderInv);
            }
        } else {
            InputItemHandler tileItemInventory = (InputItemHandler) tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            if(tileItemInventory != null) {
                if (heldItem.isEmpty()) {
                    playerIn.addItemStackToInventory(tileItemInventory.extractItem(0, TileEntityCrucible.MAX_VOLUME, false));
                    return true;
                } else if(tileItemInventory.isWhitelisted(heldItem)) {
                    playerIn.setHeldItem(hand, ItemHandlerHelper.insertItem(tileItemInventory, heldItem, false));
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void dropBlockAsItemWithChance(World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, float chance, int fortune) {}

    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
        super.onBlockHarvested(worldIn, pos, state, player);
        TileEntity tile = worldIn.getTileEntity(pos);
        if(player.isCreative() && tile instanceof PowderContainer)
            ((PowderContainer) tile).setShouldDrop(false);
    }

    @Override
    public void breakBlock(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if(tileEntity instanceof PowderContainer && ((PowderContainer) tileEntity).shouldDrop()) {
            ((PowderContainer) tileEntity).dropContent();
            spawnAsEntity(worldIn, pos, getDroppedItem(worldIn, pos));
        }
        super.breakBlock(worldIn, pos, state);
    }

    protected ItemStack getDroppedItem(World worldIn, BlockPos pos) {
        TileEntity tile = worldIn.getTileEntity(pos);
        if(tile instanceof PowderContainer) {
            PowderContainer powderContainer = ((PowderContainer)tile);
            ItemStack itemstack = new ItemStack(this);
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            nbttagcompound.setTag("BlockEntityTag", powderContainer.saveToNbt(new NBTTagCompound()));
            itemstack.setTagCompound(nbttagcompound);
            IGenericInventoryProvider inventoryProvider = itemstack.getCapability(CapabilityGenericInventoryProvider.CAPABILITY_GENERIC, null);
            IFluidHandler fluidHandler = itemstack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            IItemHandler itemHandler = itemstack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            if(inventoryProvider != null) {
                inventoryProvider.setInventory(EnumPowderOres.class, new GenericStackInventory<>(powderContainer.getPowderInventory()));
            } else
                LogManager.getLogger().error("The dropped item stack had no generic inventory capability attached");
            if(fluidHandler != null) {
                IFluidHandler tileFluidHandler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
                if(tileFluidHandler != null)
                    fluidHandler.fill(tileFluidHandler.drain(Integer.MAX_VALUE, true), true);
            }
            if(itemHandler != null) {
                IItemHandler tileItemHandler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.DOWN);
                if(tileItemHandler != null)
                    itemHandler.insertItem(0, tileItemHandler.extractItem(0, Integer.MAX_VALUE, false), false);
            }
            return itemstack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        if(advanced.isAdvanced()) {
            GenericStackInventory<EnumPowderOres> inventory = CapabilityGenericInventoryProvider.getInventory(stack, EnumPowderOres.class);
            IFluidHandlerItem fluidHandler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            IItemHandler itemHandler = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            if(inventory != null) {
                if (inventory.isEmpty()) {
                    tooltip.add("No deposit left");
                } else {
                    for (int i = 0; i < inventory.getSlotCount(); i++) {
                        if (inventory.getStackInSlot(i).isEmpty()) break;
                        tooltip.add(inventory.getStackInSlot(i).getType() + ":" + inventory.getStackInSlot(i).getCount());
                    }
                }
            }
            if(fluidHandler != null) {
                FluidStack fluidStack = fluidHandler.getTankProperties()[0].getContents();
                if(fluidStack != null)
                    tooltip.add(I18n.format(fluidStack.getUnlocalizedName()) + ":" + fluidStack.amount);
                else
                    tooltip.add("No liquid left");
            }
            if(itemHandler != null) {
                ItemStack itemStack = itemHandler.getStackInSlot(0);
                if(!itemStack.isEmpty())
                    tooltip.add(I18n.format(itemStack.getUnlocalizedName()) + ":" + itemStack.getCount());
                else
                    tooltip.add("No residue left");
            }
        }
    }

    @Nonnull
    @Override
    public ItemStack getPickBlock(@Nonnull IBlockState state, RayTraceResult target, @Nonnull World world, @Nonnull BlockPos pos, EntityPlayer player) {
        ItemStack itemstack = super.getPickBlock(state, target, world, pos, player);
        TileEntity tileEntity = world.getTileEntity(pos);
        if(tileEntity instanceof PowderContainer) {
            NBTTagCompound nbttagcompound = ((PowderContainer) tileEntity).saveToNbt(new NBTTagCompound());
            if (!nbttagcompound.hasNoTags()) {
                itemstack.setTagInfo("BlockEntityTag", nbttagcompound);
            }
        }
        return itemstack;
    }

    @Override
    public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
        return false;
    }

    @Override
    public boolean isFullBlock(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Nonnull
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public abstract boolean hasTileEntity(IBlockState state);

    @Override
    public abstract TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state);

}
