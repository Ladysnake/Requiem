package ladysnake.dissolution.common.items;

import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.init.ModFluids;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStackSimple;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class ItemJar extends Item {

    public ItemJar() {
        super();
        this.setMaxStackSize(1);
        this.setHasSubtypes(true);
        this.addPropertyOverride(
                new ResourceLocation(Reference.MOD_ID, "fluid"),
                (stack, worldIn, entityIn) -> {
                    FluidStack fluidStack = FluidUtil.getFluidContained(stack);
                    if (fluidStack == null)
                        return 0;
                    if (FluidRegistry.WATER.equals(fluidStack.getFluid()))
                        return 1;
                    if (ModFluids.MERCURY.fluid().equals(fluidStack.getFluid()))
                        return 2;
                    return 0;
                }
        );

    }

    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, @Nonnull EnumHand handIn) {
        ItemStack jar = playerIn.getHeldItem(handIn);
        FluidStack containedFluid = FluidUtil.getFluidContained(jar);
        RayTraceResult raytraceresult = this.rayTrace(worldIn, playerIn, containedFluid == null);
        if (raytraceresult == null) {
            return new ActionResult<>(EnumActionResult.PASS, jar);
        } else if (raytraceresult.typeOfHit != RayTraceResult.Type.BLOCK) {
            return new ActionResult<>(EnumActionResult.PASS, jar);
        }
        BlockPos pos = raytraceresult.getBlockPos();
        if (FluidUtil.interactWithFluidHandler(playerIn, handIn, worldIn, pos, raytraceresult.sideHit))
            return new ActionResult<>(EnumActionResult.SUCCESS, jar);
        FluidActionResult result = FluidUtil.tryPlaceFluid(playerIn, worldIn, pos.offset(raytraceresult.sideHit), jar, containedFluid);
        if (result.isSuccess())
            return new ActionResult<>(EnumActionResult.SUCCESS, worldIn.isRemote ? jar : result.getResult());
        return super.onItemRightClick(worldIn, playerIn, handIn);
    }

//    @Override
//    public void getSubItems(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> items) {
//        if (tab == Dissolution.CREATIVE_TAB) {
//            items.add(new ItemStack(this));
//            ItemStack stack = new ItemStack(this);
//            IFluidHandlerItem handler = FluidUtil.getFluidHandler(stack);
//            if (handler == null)
//                Dissolution.LOGGER.error("An error occurred while populating the creative tab : water jar item had a null fluid handler");
//            else
//                handler.fill(new FluidStack(FluidRegistry.WATER, Fluid.BUCKET_VOLUME), true);
//            items.add(stack);
//            stack = new ItemStack(this);
//            handler = FluidUtil.getFluidHandler(stack);
//            if (handler == null)
//                Dissolution.LOGGER.error("An error occurred while populating the creative tab : mercury jar item had a null fluid handler");
//            else
//                handler.fill(new FluidStack(ModFluids.MERCURY.fluid(), Fluid.BUCKET_VOLUME), true);
//            items.add(stack);
//        }
//    }

    @Nonnull
    @Override
    public String getUnlocalizedName(ItemStack stack) {
        FluidStack fluid = FluidUtil.getFluidContained(stack);
        if (fluid != null) {
            if (fluid.getFluid().equals(FluidRegistry.WATER))
                return "item.water_jar.name";
            if (fluid.getFluid().equals(ModFluids.MERCURY.fluid()))
                return "item.mercury_jar.name";
        }
        return super.getUnlocalizedName(stack);
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        return new FluidHandlerJar(stack, Fluid.BUCKET_VOLUME);
    }

    static class FluidHandlerJar extends FluidHandlerItemStackSimple {

        static List<Fluid> acceptedFluids = Arrays.asList(FluidRegistry.WATER, ModFluids.MERCURY.fluid());

        /**
         * @param container The container itemStack, data is stored on it directly as NBT.
         * @param capacity  The maximum capacity of this fluid tank.
         */
        public FluidHandlerJar(@Nonnull ItemStack container, int capacity) {
            super(container, capacity);
        }

        @Override
        public boolean canFillFluidType(FluidStack fluid) {
            return acceptedFluids.contains(fluid.getFluid());
        }
    }
}
