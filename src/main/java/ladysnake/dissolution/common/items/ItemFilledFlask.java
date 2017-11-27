package ladysnake.dissolution.common.items;

import ladysnake.dissolution.api.IIncorporealHandler;
import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.init.ModItems;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

public class ItemFilledFlask extends ItemFlask {

    private static final List<String> variants = Arrays.asList("water_flask", "transcendence_potion", "conservation_potion");

    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        playerIn.setActiveHand(handIn);
        return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
    }

    @Nonnull
    @Override
    public ItemStack onItemUseFinish(@Nonnull ItemStack stack, World worldIn, EntityLivingBase entityLiving) {
        if (entityLiving instanceof EntityPlayer) {
            EntityPlayer entityplayer = (EntityPlayer) entityLiving;
            //noinspection ConstantConditions
            entityplayer.addStat(StatList.getObjectUseStats(this));

            if (stack.getMetadata() == variants.indexOf("transcendence_potion")) {
                stack.shrink(1);
                if (stack.isEmpty())
                    entityplayer.setHeldItem(entityplayer.getActiveHand(), new ItemStack(ModItems.GLASS_FLASK));
                else
                    entityplayer.addItemStackToInventory(new ItemStack(ModItems.GLASS_FLASK));
                ItemAcerbacaFruit.split(entityplayer, IIncorporealHandler.CorporealityStatus.ECTOPLASM);
                return ItemStack.EMPTY;
            }
            if (entityplayer instanceof EntityPlayerMP) {
                CriteriaTriggers.CONSUME_ITEM.trigger((EntityPlayerMP) entityplayer, stack);
            }
            if (!entityplayer.capabilities.isCreativeMode) {
                stack.shrink(1);
                if (stack.isEmpty()) {
                    return new ItemStack(ModItems.GLASS_FLASK);
                }
                entityplayer.inventory.addItemStackToInventory(new ItemStack(ModItems.GLASS_FLASK));
            }
        }
        stack.shrink(1);
        return stack;
    }

    public static int getMetaForVariant(String variant) {
        return variants.indexOf(variant);
    }

    @Override
    public void registerRender() {
        assert this.getRegistryName() != null;
        for (int i = 0; i < variants.size(); i++)
            ModelLoader.setCustomModelResourceLocation(this, i,
                    new ModelResourceLocation(this.getRegistryName().getResourceDomain() + ":glassware/" + variants.get(i)));
    }

    @Override
    public boolean getHasSubtypes() {
        return true;
    }

    public int getMaxItemUseDuration(ItemStack stack) {
        return 32;
    }

    @Nonnull
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.DRINK;
    }

    @Nonnull
    @Override
    public String getUnlocalizedName(ItemStack stack) {
        if (stack.getMetadata() < variants.size())
            return "item." + variants.get(stack.getMetadata());
        return super.getUnlocalizedName(stack);
    }

    @Override
    public void getSubItems(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> items) {
        if (tab == Dissolution.CREATIVE_TAB)
            for (int i = 0; i < variants.size(); ++i) {
                items.add(new ItemStack(this, 1, i));
            }
    }
}
