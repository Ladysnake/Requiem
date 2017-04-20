package ladysnake.tartaros.common.crafting;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;

import ladysnake.tartaros.common.init.ModItems;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;

public class CrystallizerRecipes {

	private static final CrystallizerRecipes CRYSTAL_BASE = new CrystallizerRecipes();
	private final Map<ItemStack, ItemStack> crystallizingList = Maps.<ItemStack, ItemStack>newHashMap();
	public final Map<Item, Integer> crystalTimeList = new HashMap<Item, Integer>();
	
	public static CrystallizerRecipes instance()
    {
        return CRYSTAL_BASE;
    }
	
	private CrystallizerRecipes() {
		addCrystalRecipe(new ItemStack(Blocks.NETHERRACK), new ItemStack(Blocks.NETHER_BRICK));
		addCrystalRecipe(new ItemStack(Blocks.SAND), new ItemStack(Blocks.GLASS), 20);
		addCrystalRecipe(new ItemStack(ModItems.ectoplasm), new ItemStack(ModItems.ectoplasma), 400);
	}
	
	/**
	 * adds a recipe to the crystallizer
	 * @param input the item that gets consumed
	 * @param output the item that gets created
	 */
	public void addCrystalRecipe(ItemStack input, ItemStack output){
		addCrystalRecipe(input, output, 200);
	}
	
	/**
	 * adds a recipe to the crystallizer
	 * @param input the item that gets consumed
	 * @param output the item that gets created
	 * @param time the time, in ticks, that the process will take. (a blaze powder fuels for 800 ticks)
	 */
	public void addCrystalRecipe(ItemStack input, ItemStack output, int time){
		if (getCrystalResult(input) != ItemStack.EMPTY) { net.minecraftforge.fml.common.FMLLog.info("Ignored crystallizing recipe with conflicting input: " + input + " = " + output); return; }
        this.crystallizingList.put(input, output);
        this.crystalTimeList.put(input.getItem(), time);
	}
	
	public ItemStack getCrystalResult(ItemStack input) {
		for (Entry<ItemStack, ItemStack> entry : this.crystallizingList.entrySet())
        {
            if (this.compareItemStacks(input, (ItemStack)entry.getKey()))
            {
                return (ItemStack)entry.getValue();
            }
        }

        return ItemStack.EMPTY;
	}
	
	private boolean compareItemStacks(ItemStack stack1, ItemStack stack2)
    {
        return stack2.getItem() == stack1.getItem() && (stack2.getMetadata() == 32767 || stack2.getMetadata() == stack1.getMetadata());
    }
	
	public Map<ItemStack, ItemStack> getCrystallizingList()
    {
        return this.crystallizingList;
    }

}
