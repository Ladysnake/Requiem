package ladysnake.dissolution.common.crafting;

import java.util.ArrayList;
import java.util.List;

import ladysnake.dissolution.common.init.ModBlocks;
import ladysnake.dissolution.common.inventory.InventorySearchHelper;
import ladysnake.dissolution.common.items.ItemBaseResource;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class CrystallizerRecipe {
	private ItemStack input, output, fuel;

	private int processTime;
	public static final List<CrystallizerRecipe> crystallizingRecipes = new ArrayList<CrystallizerRecipe>();
	
	static {
		new CrystallizerRecipe(ItemBaseResource.resourceFromName("ectoplasm"), ItemBaseResource.resourceFromName("ectoplasma"), 400);
		new CrystallizerRecipe(new ItemStack(ModBlocks.ECTOPLASM), new ItemStack(ModBlocks.ECTOPLASMA), 400*9);
		new CrystallizerRecipe(new ItemStack(Blocks.SAND), new ItemStack(Blocks.GLASS), 20);
		new CrystallizerRecipe(new ItemStack(Blocks.NETHERRACK), new ItemStack(Blocks.NETHER_BRICK));
	}

	/**
	 * creates a recipe for the crystallizer
	 * @param input the item that gets consumed
	 * @param output the item that gets created
	 */
	public CrystallizerRecipe(ItemStack input, ItemStack output) {
		this(input, output, 200);
	}

	/**
	 * creates a recipe for the crystallizer
	 * @param input the item that gets consumed
	 * @param output the item that gets created
	 * @param time the time, in ticks, that the process will take. (a blaze powder fuels for 800 ticks)
	 */
	public CrystallizerRecipe(ItemStack input, ItemStack output, int time) {
		if (getCrystalRecipe(input) != null) { 
			net.minecraftforge.fml.common.FMLLog.info("Ignored crystallizing recipe with conflicting input: " + input + " => " + output); 
			return;
		}
		this.input = input;
		this.output = output;
		this.processTime = time;
		this.fuel = new ItemStack(Items.BLAZE_POWDER);
		crystallizingRecipes.add(this);
	}

	/**
	 * @return the input stack for this recipe
	 */
	public ItemStack getInput() {
		return input;
	}

	/**
	 * @return the output stack of this recipe
	 */
	public ItemStack getOutput() {
		return output;
	}

	/**
	 * @return the time that this recipe takes
	 */
	public int getProcessTime() {
		return processTime;
	}

	/**
	 * @return the fuel consumed by the transformation
	 */
	public ItemStack getFuel() {
		return fuel;
	}

	/**
	 * Searches in the recipe list the corresponding recipe for a given input
	 * @param input The itemstack inserted in the crystallizer
	 * @return The corresponding recipe
	 */
	public static CrystallizerRecipe getCrystalRecipe(ItemStack input) {
		for (CrystallizerRecipe cr : CrystallizerRecipe.crystallizingRecipes)
	    {
	        if (InventorySearchHelper.compareItemStacks(input, cr.getInput()))
	        {
	            return cr;
	        }
	    }
	
	    return null;
	}
	
}
