package ladysnake.dissolution.common.items;

import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.capabilities.IPlayerWarp;

import javax.annotation.Nonnull;

public class ItemHumanFlesh extends ItemFood {

    private boolean applyWarp;

    public ItemHumanFlesh(int amount, float saturation, boolean isWolfFood) {
        super(amount, saturation, isWolfFood);
        this.applyWarp = Loader.isModLoaded("thaumcraft");
    }

    @Override
    protected void onFoodEaten(ItemStack stack, World world, @Nonnull EntityPlayer player) {
        super.onFoodEaten(stack, world, player);
        if (!world.isRemote && applyWarp && Dissolution.config.warpyFlesh) {
            if (world.rand.nextFloat() < 0.05F) {
                ThaumcraftApi.internalMethods.addWarpToPlayer(player, 1, IPlayerWarp.EnumWarpType.NORMAL);
            } else if (world.rand.nextFloat() < 0.75) {
                ThaumcraftApi.internalMethods.addWarpToPlayer(player, 1 + world.rand.nextInt(3), IPlayerWarp.EnumWarpType.TEMPORARY);
            }
        }
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, @Nonnull EnumHand handIn) {
        ItemStack itemstack = playerIn.getHeldItem(handIn);

        if (playerIn.canEat(false) || CapabilityIncorporealHandler.getHandler(playerIn).getPossessed() != null) {
            playerIn.setActiveHand(handIn);
            return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
        } else {
            return new ActionResult<>(EnumActionResult.FAIL, itemstack);
        }
    }
}
