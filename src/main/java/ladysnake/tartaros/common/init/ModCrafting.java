package ladysnake.tartaros.common.init;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModCrafting {

	public static void register() {
		GameRegistry.addShapedRecipe(new ItemStack(ModBlocks.soul_extractor), "SSD", "GIG", "QRS", 'S', Blocks.COBBLESTONE, 'D', Items.DIAMOND, 'G', Blocks.GLASS, 'I', Items.IRON_INGOT, 'Q', Items.QUARTZ, 'R', Items.REDSTONE);
		GameRegistry.addShapedRecipe(new ItemStack(ModBlocks.crystallizer), "SGI", "BIB", "QRS", 'S', Blocks.COBBLESTONE, 'G', Blocks.GLASS, 'I', Items.IRON_INGOT, 'B', Blocks.IRON_BLOCK, 'Q', Items.QUARTZ, 'R', Items.REDSTONE);
		GameRegistry.addShapedRecipe(new ItemStack(ModItems.sepulture), "CCC", "QGS", 'C', new ItemStack(Blocks.STONE_SLAB, 1, 1), 'Q', Items.QUARTZ, 'G', ModItems.soul_gem, 'S', new ItemStack(Items.SKULL, 1, 1));
		GameRegistry.addShapedRecipe(new ItemStack(ModBlocks.mercurius_waystone), "W", "G", "C", 'W', Blocks.COBBLESTONE_WALL, 'G', ModItems.soul_gem, 'C', Blocks.COBBLESTONE);
		GameRegistry.addShapedRecipe(new ItemStack(ModBlocks.ectoplasma), "EEE", "EEE", "EEE", 'E', ModItems.ectoplasma);
		GameRegistry.addShapedRecipe(new ItemStack(ModBlocks.ectoplasm), "EEE", "EEE", "EEE", 'E', ModItems.ectoplasm);
		GameRegistry.addShapelessRecipe(new ItemStack(ModItems.soul_gem), ModItems.ectoplasma, ModItems.soul_in_a_bottle);
		
	}
}
