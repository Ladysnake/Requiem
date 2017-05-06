package ladysnake.dissolution.common.init;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModCrafting {

	public static void register() {
		GameRegistry.addShapedRecipe(new ItemStack(ModBlocks.CRYSTALLIZER), "SGI", "BIB", "QRS", 'S', Blocks.COBBLESTONE, 'G', Blocks.GLASS, 'I', Items.IRON_INGOT, 'B', Blocks.IRON_BLOCK, 'Q', Items.QUARTZ, 'R', Items.REDSTONE);
		GameRegistry.addShapedRecipe(new ItemStack(ModBlocks.ECTOPLASM), "EEE", "EEE", "EEE", 'E', new ItemStack(ModItems.BASE_RESOURCE, 1, 0));
		GameRegistry.addShapedRecipe(new ItemStack(ModBlocks.ECTOPLASMA), "EEE", "EEE", "EEE", 'E', new ItemStack(ModItems.BASE_RESOURCE, 1, 1));
		GameRegistry.addShapedRecipe(new ItemStack(ModItems.EYE_OF_THE_UNDEAD), "IGI", "IEI", "ISI", 'I', Items.IRON_INGOT, 'G', Blocks.GLASS, 'E', Items.ENDER_EYE, 'S', new ItemStack(ModItems.BASE_RESOURCE, 1, 1));
		GameRegistry.addShapedRecipe(new ItemStack(ModItems.GRAND_FAUX), "IBB", "PSS", "SPP", 'I', Items.IRON_INGOT, 'B', Blocks.IRON_BLOCK, 'P', Items.PAPER, 'S', Items.STICK);
		GameRegistry.addShapedRecipe(new ItemStack(ModItems.SCYTHE_IRON), "III", " SS", "S  ", 'I', Items.IRON_INGOT, 'S', Items.STICK);
		GameRegistry.addShapedRecipe(new ItemStack(ModBlocks.MERCURIUS_WAYSTONE), "W", "G", "C", 'W', Blocks.COBBLESTONE_WALL, 'G', ModItems.SOUL_GEM, 'C', Blocks.COBBLESTONE);
		GameRegistry.addShapedRecipe(new ItemStack(ModItems.SEPULTURE), "CCC", "QGS", 'C', new ItemStack(Blocks.STONE_SLAB, 1, 0), 'Q', Items.QUARTZ, 'G', ModItems.SOUL_GEM, 'S', new ItemStack(Items.SKULL, 1, 1));
		GameRegistry.addShapedRecipe(new ItemStack(ModBlocks.SOUL_EXTRACTOR), "SSD", "GIG", "QRS", 'S', Blocks.COBBLESTONE, 'D', Items.DIAMOND, 'G', Blocks.GLASS, 'I', Items.IRON_INGOT, 'Q', Items.QUARTZ, 'R', Items.REDSTONE);
		GameRegistry.addShapedRecipe(new ItemStack(Blocks.SOUL_SAND, 8), "SSS", "SBS", "SSS", 'S', Blocks.SAND, 'B', ModItems.SOUL_IN_A_BOTTLE);
		GameRegistry.addShapelessRecipe(new ItemStack(ModItems.SOUL_GEM), new ItemStack(ModItems.BASE_RESOURCE, 1, 1), ModItems.SOUL_IN_A_BOTTLE);
	}
}
