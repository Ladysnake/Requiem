package ladysnake.dissolution.common.blocks;

import ladysnake.dissolution.common.inventory.InputItemHandler;
import ladysnake.dissolution.common.tileentities.TileEntityCrucible;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;

public class BlockCrucible extends BlockGenericContainer {
    public BlockCrucible() {
        super(Material.ROCK);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if(super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ))
            return true;
        TileEntity tile = worldIn.getTileEntity(pos);
        if(tile instanceof TileEntityCrucible && !worldIn.isRemote) {
            IFluidHandler fluidTank = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
            /*if(playerIn.getHeldItem(hand).getItem().equals(Items.WATER_BUCKET) && fluidTank != null) {
                fluidTank.fill(new FluidStack(FluidRegistry.WATER, Fluid.BUCKET_VOLUME), true);
                playerIn.setHeldItem(hand, new ItemStack(Items.BUCKET));
            } else*/ if (playerIn.getHeldItem(hand).hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
                IFluidHandlerItem fluidHandlerItem = playerIn.getHeldItem(hand).getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
                if(fluidHandlerItem != null && fluidTank != null) {
                    FluidStack fluidStack = fluidHandlerItem.drain(Fluid.BUCKET_VOLUME, !playerIn.isCreative());
                    if(fluidStack != null) {
                        worldIn.playSound(playerIn, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1, 1);
                        fluidStack.amount -= fluidTank.fill(fluidStack, true);
                        fluidHandlerItem.fill(fluidStack, !playerIn.isCreative());
                    } else {
                        FluidStack fluidStack1 = fluidTank.drain(Fluid.BUCKET_VOLUME, !playerIn.isCreative());
                        if(fluidStack1 != null) {
                            worldIn.playSound(playerIn, pos, SoundEvents.ITEM_BUCKET_FILL, SoundCategory.BLOCKS, 1, 1);
                            fluidStack1.amount -= fluidHandlerItem.fill(fluidStack1, true);
                            fluidTank.fill(fluidStack1, !playerIn.isCreative());
                        }
                    }
                }
            }
        }
        return true;
    }

    @Nonnull
    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return new AxisAlignedBB(0.2,0,0.2,0.8,0.7,0.8);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return new TileEntityCrucible();
    }
}
