package ladysnake.dissolution.common.items;

import ladysnake.dissolution.common.init.ModItems;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class ItemFlask extends Item implements ICustomLocation {

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        RayTraceResult raytraceresult = this.rayTrace(worldIn, playerIn, true);
        ItemStack itemstack = playerIn.getHeldItem(handIn);
        //noinspection ConstantConditions
        if (raytraceresult == null) {
            return new ActionResult<>(EnumActionResult.PASS, itemstack);
        } else {
            if (raytraceresult.typeOfHit == RayTraceResult.Type.BLOCK) {
                BlockPos blockpos = raytraceresult.getBlockPos();

                if (!worldIn.isBlockModifiable(playerIn, blockpos) || !playerIn.canPlayerEdit(blockpos.offset(raytraceresult.sideHit), raytraceresult.sideHit, itemstack)) {
                    return new ActionResult<>(EnumActionResult.PASS, itemstack);
                }

                if (worldIn.getBlockState(blockpos).getMaterial() == Material.WATER) {
                    worldIn.playSound(playerIn, playerIn.posX, playerIn.posY, playerIn.posZ, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.NEUTRAL, 1.0F, 1.0F);
                    return new ActionResult<>(EnumActionResult.SUCCESS, this.turnBottleIntoItem(itemstack, playerIn, new ItemStack(ModItems.FILLED_FLASK, 1, ItemFilledFlask.getMetaForVariant("water_flask"))));
                }
            }

            return new ActionResult<>(EnumActionResult.PASS, itemstack);
        }
    }

    protected ItemStack turnBottleIntoItem(ItemStack flask, EntityPlayer player, ItemStack waterFlask) {
        flask.shrink(1);
        //noinspection ConstantConditions
        player.addStat(StatList.getObjectUseStats(this));

        if (flask.isEmpty()) {
            return waterFlask;
        } else {
            if (!player.inventory.addItemStackToInventory(waterFlask)) {
                player.dropItem(waterFlask, false);
            }
            return flask;
        }
    }

    @Override
    public ModelResourceLocation getModelLocation() {
        assert this.getRegistryName() != null;
        return new ModelResourceLocation(this.getRegistryName().getResourceDomain() + ":glassware/" + this.getRegistryName().getResourcePath());
    }
}
