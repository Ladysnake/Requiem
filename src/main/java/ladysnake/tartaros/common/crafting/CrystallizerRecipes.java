package ladysnake.tartaros.common.crafting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ladysnake.tartaros.common.init.ModItems;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

public class CrystallizerRecipes {

	private static final CrystallizerRecipes CRYSTAL_BASE = new CrystallizerRecipes();
	public static final List<CrystallizerRecipe> crystallizingRecipes = new ArrayList<CrystallizerRecipe>();
	
	public static CrystallizerRecipes instance()
    {
        return CRYSTAL_BASE;
    }
	
	static {
		addCrystalRecipe(new ItemStack(Blocks.NETHERRACK), new ItemStack(Blocks.NETHER_BRICK));
		addCrystalRecipe(new ItemStack(Blocks.SAND), new ItemStack(Blocks.GLASS), 20);
		addCrystalRecipe(new ItemStack(ModItems.ECTOPLASM), new ItemStack(ModItems.ECTOPLASMA), 400);
	}
	
	/**
	 * adds a recipe to the crystallizer
	 * @param input the item that gets consumed
	 * @param output the item that gets created
	 */
	public static void addCrystalRecipe(ItemStack input, ItemStack output){
		addCrystalRecipe(input, output, 200);
	}
	
	/**
	 * adds a recipe to the crystallizer
	 * @param input the item that gets consumed
	 * @param output the item that gets created
	 * @param time the time, in ticks, that the process will take. (a blaze powder fuels for 800 ticks)
	 */
	public static void addCrystalRecipe(ItemStack input, ItemStack output, int time){
		if (getCrystalResult(input) != ItemStack.EMPTY) { net.minecraftforge.fml.common.FMLLog.info("Ignored crystallizing recipe with conflicting input: " + input + " = " + output); return; }
        	crystallizingRecipes.add(new CrystallizerRecipe(input, output, time));
	}
	
	public static ItemStack getCrystalResult(ItemStack input) {
		for (CrystallizerRecipe cr : crystallizingRecipes)
        {
            if (compareItemStacks(input, cr.getInput()))
            {
                return cr.getOutput();
            }
        }

        return ItemStack.EMPTY;
	}
	
	private static boolean compareItemStacks(ItemStack stack1, ItemStack stack2)
    {
        return stack2.getItem() == stack1.getItem() && (stack2.getMetadata() == 32767 || stack2.getMetadata() == stack1.getMetadata());
    }
	
}
