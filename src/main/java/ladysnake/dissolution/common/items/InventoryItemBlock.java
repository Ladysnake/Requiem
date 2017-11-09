package ladysnake.dissolution.common.items;

import ladysnake.dissolution.api.GenericStackInventory;
import ladysnake.dissolution.common.capabilities.CapabilityGenericInventoryProvider;
import ladysnake.dissolution.common.registries.EnumPowderOres;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InventoryItemBlock extends ItemBlock {
    private boolean generic, item, fluid;

    public InventoryItemBlock(Block block, boolean generic, boolean item, boolean fluid) {
        super(block);
        this.setMaxStackSize(1);
        this.generic = generic;
        this.item = item;
        this.fluid = fluid;
    }

    @Nonnull
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, @Nonnull BlockPos pos, @Nonnull EnumHand hand, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ) {
        if(player.isSneaking() && player.getHeldItem(hand).hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
            IItemHandler handler = player.getHeldItem(hand).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            if(handler != null && handler.getSlots() > 0 && !handler.getStackInSlot(0).isEmpty()) {
                worldIn.spawnEntity(new EntityItem(worldIn, pos.getX() + hitX, pos.getY() + hitY, pos.getZ() + hitZ, handler.extractItem(0, Integer.MAX_VALUE, false)));
                return EnumActionResult.SUCCESS;
            }
        }
        if(player.isSneaking() && player.getHeldItem(hand).hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
            IFluidHandlerItem handler = player.getHeldItem(hand).getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            if(handler != null) {
                if(FluidUtil.interactWithFluidHandler(player, hand, worldIn, pos, facing))
                    return EnumActionResult.SUCCESS;
                ItemStack heldItem = player.getHeldItem(hand);
                FluidStack heldFluid = FluidUtil.getFluidContained(heldItem);
                FluidActionResult result = FluidUtil.tryPlaceFluid(player, worldIn, pos.offset(facing), heldItem, heldFluid);
                if(!result.isSuccess())
                    result = FluidUtil.tryPickUpFluid(heldItem, player, worldIn, pos.offset(facing), facing);
                if(result.isSuccess()) {
                    player.setHeldItem(hand, result.getResult());
                    return EnumActionResult.SUCCESS;
                }
                if(worldIn.getBlockState(pos.offset(facing)).getMaterial().isLiquid())
                    return EnumActionResult.FAIL;
            }
        }
        return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public boolean placeBlockAt(@Nonnull ItemStack stack, @Nonnull EntityPlayer player, World world, @Nonnull BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
        boolean ret = super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState);
        if(ret && !world.isRemote) {
            TileEntity tileEntity = world.getTileEntity(pos);
            if(tileEntity != null) {
                GenericStackInventory<EnumPowderOres> tileInventory = CapabilityGenericInventoryProvider.getInventory(tileEntity, EnumPowderOres.class);
                if(tileInventory != null) {
                    GenericStackInventory<EnumPowderOres> inventory = CapabilityGenericInventoryProvider.getInventory(stack, EnumPowderOres.class);
                    if(inventory != null)
                        while(inventory.getTotalAmount() > 0 && tileInventory.canInsert())
                            tileInventory.insert(inventory.extract(Integer.MAX_VALUE, null));
                }
                IFluidHandler tank = tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
                IFluidHandler stackFluidHandler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
                if(tank != null && stackFluidHandler != null)
                    tank.fill(stackFluidHandler.drain(Integer.MAX_VALUE, !player.isCreative()), true);
                IItemHandler itemHandler = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.DOWN);
                IItemHandler stackItemHandler = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
                if(itemHandler != null && stackItemHandler != null)
                    itemHandler.insertItem(0, stackItemHandler.extractItem(0, Integer.MAX_VALUE, player.isCreative()), false);
                tileEntity.markDirty();
            }
        }
        return ret;
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        return new CapabilityProvider(stack);
    }

    protected class CapabilityProvider implements ICapabilitySerializable<NBTTagCompound> {
        CapabilityGenericInventoryProvider.Provider genericProvider;
        ICapabilityProvider fluidProvider;
        IItemHandler itemHandler;

        public CapabilityProvider(ItemStack stack) {
            super();
            if(generic)
                genericProvider = new CapabilityGenericInventoryProvider.Provider();
            if(fluid)
                fluidProvider = new FluidHandlerItemStack(stack, Fluid.BUCKET_VOLUME);
            if(item)
                itemHandler = new ItemStackHandler();
        }

        @Override
        public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
            if (capability == CapabilityGenericInventoryProvider.CAPABILITY_GENERIC)
                return generic;
            if (capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY)
                return fluid;
            return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && item;
        }

        @Nullable
        @Override
        public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
            if(capability == CapabilityGenericInventoryProvider.CAPABILITY_GENERIC && generic)
                return genericProvider.getCapability(capability, facing);
            if(capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY && fluid)
                return fluidProvider.getCapability(capability, facing);
            if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && item)
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemHandler);
            return null;
        }

        @Override
        public NBTTagCompound serializeNBT() {
            NBTTagCompound compound = new NBTTagCompound();
            if(generic)
                compound.setTag("generic", genericProvider.serializeNBT());
            if(item) {
                NBTBase itemCompound = CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.getStorage().writeNBT(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, itemHandler, null);
                if (itemCompound != null)
                    compound.setTag("item", itemCompound);
            }
            return compound;
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            if(generic)
                genericProvider.deserializeNBT(nbt.getTag("generic"));
            if(item)
                CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.getStorage().readNBT(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, itemHandler, null, nbt.getTag("item"));
        }
    }
}
