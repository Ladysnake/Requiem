package ladysnake.dissolution.common.items;

import ladysnake.dissolution.api.corporeality.IIncorporealHandler;
import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.init.ModItems;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
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

    @Nonnull
    @Override
    public ItemStack onItemUseFinish(ItemStack stack, @Nonnull World worldIn, EntityLivingBase entityLiving) {
        if (entityLiving instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entityLiving;
            IIncorporealHandler handler = CapabilityIncorporealHandler.getHandler(player);
            // no food value when you are a zombie
            if (handler.getPossessed() != null) {
                // Only consume outside of creative
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
                onFoodEaten(stack, worldIn, player);
                player.addStat(StatList.getObjectUseStats(this));
                return stack;
            }
        }
        return super.onItemUseFinish(stack, worldIn, entityLiving);
    }

    @Override
    protected void onFoodEaten(ItemStack stack, World world, @Nonnull EntityPlayer player) {
        super.onFoodEaten(stack, world, player);
        if (!world.isRemote && applyWarp && Dissolution.config.warpyFlesh) {
            addWarp(world, player);
        }
    }

    /**
     * This method is called when thaumcraft is present to add warp to the given player
     */
    protected void addWarp(World world, @Nonnull EntityPlayer player) {
        // regular human flesh has less impact than organs
        if (this == ModItems.HUMAN_FLESH_RAW && world.rand.nextFloat() < 0.05F
                || world.rand.nextFloat() < 0.1f) {
            ThaumcraftApi.internalMethods.addWarpToPlayer(player, 1, IPlayerWarp.EnumWarpType.NORMAL);
        } else if (this != ModItems.HUMAN_FLESH_RAW || world.rand.nextFloat() < 0.75) {
            ThaumcraftApi.internalMethods.addWarpToPlayer(player, 1 + world.rand.nextInt(3), IPlayerWarp.EnumWarpType.TEMPORARY);
        }
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, @Nonnull EnumHand handIn) {
        ItemStack itemstack = playerIn.getHeldItem(handIn);

        // allows players possessing an entity to always eat
        if (playerIn.canEat(false) || CapabilityIncorporealHandler.getHandler(playerIn).getPossessed() != null) {
            playerIn.setActiveHand(handIn);
            return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
        } else {
            return new ActionResult<>(EnumActionResult.FAIL, itemstack);
        }
    }
}
