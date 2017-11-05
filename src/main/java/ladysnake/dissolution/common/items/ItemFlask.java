package ladysnake.dissolution.common.items;

import ladysnake.dissolution.common.Dissolution;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ItemFlask extends Item implements ICustomLocation{

    private final List<String> variants = Arrays.asList("glass_flask", "conservation_potion", "transcendence_potion", "water_flask");

    @Nonnull
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        //TODO implement dem potion effects
        return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public ModelResourceLocation getModelLocation() {
        assert this.getRegistryName() != null;
        return new ModelResourceLocation(this.getRegistryName().getResourceDomain() + ":glassware/" + this.getRegistryName().getResourcePath());
    }

    @Override
    public void registerRender() {
        assert this.getRegistryName() != null;
        for(int i = 0; i < variants.size(); i++)
            ModelLoader.setCustomModelResourceLocation(this, i,
                    new ModelResourceLocation(this.getRegistryName().getResourceDomain() + ":glassware/" + variants.get(i)));
    }

    @Override
    public boolean getHasSubtypes() {
        return true;
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
        if(tab == Dissolution.CREATIVE_TAB)
            for (int i = 0; i < variants.size(); ++i) {
                items.add(new ItemStack(this, 1, i));
            }
    }
}
