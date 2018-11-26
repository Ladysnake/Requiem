package ladysnake.dissolution.common.items;

import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.Ref;
import ladysnake.dissolution.common.capabilities.CapabilitySoulHandler;
import ladysnake.dissolution.common.entity.SoulType;
import ladysnake.dissolution.common.tileentities.TileEntityWispInAJar;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemSoulInAJar extends ItemBlock {

    public ItemSoulInAJar(Block block) {
        super(block);
        this.setMaxStackSize(1);
        this.setHasSubtypes(true);
        this.addPropertyOverride(
                new ResourceLocation(Ref.MOD_ID, "soul"),
                (stack, worldIn, entityIn) -> getSoul(stack).ordinal()
        );
    }

    @Nonnull
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, @Nonnull BlockPos pos, @Nonnull EnumHand hand, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (player.isSneaking()) {
            SoulType soul = getSoul(player.getHeldItem(hand));
            if (!worldIn.getBlockState(pos).getBlock().isReplaceable(worldIn, pos)) {
                pos = pos.offset(facing);
            }
            EnumActionResult ret = super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
            TileEntity jar = worldIn.getTileEntity(pos);
            if (jar instanceof TileEntityWispInAJar) {
                TileEntityWispInAJar wispInAJar = (TileEntityWispInAJar) jar;
                wispInAJar.setContainedSoul(soul);
            }
            return ret;
        }
        return EnumActionResult.PASS;
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, @Nonnull EnumHand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        SoulType soul = getSoul(stack);
        RayTraceResult raytraceresult = this.rayTrace(worldIn, playerIn, true);
        if (raytraceresult == null || raytraceresult.typeOfHit == RayTraceResult.Type.MISS) {
            return new ActionResult<>(EnumActionResult.PASS, stack);
        }
        BlockPos pos = raytraceresult.getBlockPos().offset(raytraceresult.sideHit);
        if (!worldIn.isRemote) {
            soul.instantiate(worldIn, pos.getX(), pos.getY(), pos.getZ(), playerIn).ifPresent(worldIn::spawnEntity);
        }
        stack.shrink(1);
        ItemStack emptyJar = new ItemStack(Items.GLASS_BOTTLE/*ModItems.GLASS_JAR*/);
        if (stack.isEmpty()) {
            stack = emptyJar;
        } else {
            playerIn.addItemStackToInventory(emptyJar);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    private SoulType getSoul(ItemStack stack) {
        try {
            if (stack.getTagCompound() != null && stack.getTagCompound().hasKey("soultype")) {
                return SoulType.valueOf(stack.getTagCompound().getString("soultype"));
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return SoulType.NONE;
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, @Nonnull ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add(getSoul(stack).toString());
    }

    public static ItemStack newTypedSoulBottle(SoulType soulType) {
        ItemStack stack = new ItemStack(Items.GLASS_BOTTLE);//ModItems.WISP_IN_A_JAR);
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("soultype", soulType.name());
        stack.setTagCompound(nbt);
        return stack;
    }

    @Override
    public void getSubItems(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> items) {
        if (tab == Dissolution.CREATIVE_TAB) {
            items.add(newTypedSoulBottle(SoulType.WILL_O_WISP));
            items.add(newTypedSoulBottle(SoulType.FAERIE));
            items.add(newTypedSoulBottle(SoulType.TIRED_FAERIE));
        }
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        return new CapabilitySoulHandler.Provider();
    }

}
