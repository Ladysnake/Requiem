package ladysnake.dissolution.common.crafting;

import net.minecraft.item.ItemStack;

public class CrystallizerRecipe {
	private ItemStack input, output;
	private int processTime;
	
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
		this.input = input;
		this.output = output;
		this.processTime = time;
	}

	public ItemStack getInput() {
		return input;
	}

	public ItemStack getOutput() {
		return output;
	}

	public int getProcessTime() {
		return processTime;
	}
	
}
