package ladysnake.dissolution.unused.common.items;

import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.items.ItemSoulInAJar;
import ladysnake.dissolution.unused.common.entity.souls.EntityFleetingSoul;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStackSimple;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
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
                    if (fluidStack == null) {
                        return 0;
                    }
                    if (FluidRegistry.WATER.equals(fluidStack.getFluid())) {
                        return 1;
                    }
                    return 0;
                }
        );

    }

    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, @Nonnull EnumHand handIn) {
        ItemStack jar = playerIn.getHeldItem(handIn);
        FluidStack containedFluid = FluidUtil.getFluidContained(jar);
        RayTraceResult raytraceresult = this.rayTrace(worldIn, playerIn, containedFluid == null);
        if (raytraceresult == null || raytraceresult.typeOfHit != RayTraceResult.Type.ENTITY) {
            return new ActionResult<>(EnumActionResult.PASS, jar);
        }
        Entity target = raytraceresult.entityHit;
        if (target instanceof EntityFleetingSoul) {
            EntityFleetingSoul soul = (EntityFleetingSoul) target;
            if (soul.canBePickupBy(playerIn)) {
                jar.shrink(1);
                // TODO use ladylib's turnItem method
                playerIn.addItemStackToInventory(ItemSoulInAJar.newTypedSoulBottle(soul.getSoulType()));
                soul.setDead();
                return new ActionResult<>(EnumActionResult.SUCCESS, jar);
            }
        }
        return super.onItemRightClick(worldIn, playerIn, handIn);
    }

    @Nonnull
    @Override
    public String getTranslationKey(ItemStack stack) {
        FluidStack fluid = FluidUtil.getFluidContained(stack);
        if (fluid != null) {
            if (fluid.getFluid().equals(FluidRegistry.WATER)) {
                return "item.water_jar.name";
            }
        }
        return super.getTranslationKey(stack);
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        return new FluidHandlerJar(stack, Fluid.BUCKET_VOLUME);
    }

    static class FluidHandlerJar extends FluidHandlerItemStackSimple {

        static List<Fluid> acceptedFluids = Collections.singletonList(FluidRegistry.WATER);

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
