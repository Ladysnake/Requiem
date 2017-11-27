package ladysnake.dissolution.common.inventory;

import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.init.ModItems;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class DissolutionTab extends CreativeTabs {

    public DissolutionTab() {
        super(Reference.MOD_ID);
    }

    @Nonnull
    @Override
    public ItemStack getTabIconItem() {
        return new ItemStack(ModItems.LOGO);
    }


}
