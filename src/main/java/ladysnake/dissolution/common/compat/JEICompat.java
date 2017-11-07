package ladysnake.dissolution.common.compat;

import ladysnake.dissolution.common.init.ModBlocks;
import ladysnake.dissolution.common.init.ModItems;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.IRecipeRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IIngredientBlacklist;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@JEIPlugin
public class JEICompat implements IModPlugin {

    private IModRegistry registry;
	
	@Override
	public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {}

	@Override
	public void registerIngredients(IModIngredientRegistration registry) {}
	
	@Override
	public void registerCategories(IRecipeCategoryRegistration registry) {
		// registry.addRecipeCategories(new CrystallizerRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void register(IModRegistry registry) {
        this.registry = registry;
        blacklistStuff();
		addInformationTabs();
	}

	@SideOnly(Side.CLIENT)
	private void addInformationTabs() {
		addInformationTab(ModBlocks.LAMENT_STONE);
		addInformationTab(ModBlocks.CRUCIBLE);
		addInformationTab(ModBlocks.MAGNET);
		addInformationTab(ModBlocks.MORTAR);
		addInformationTab(ModItems.IGNEOUS_ROCK);
	}

	@SideOnly(Side.CLIENT)
	private void addInformationTab(Block block) {
		this.addInformationTab(Item.getItemFromBlock(block));
	}

	@SideOnly(Side.CLIENT)
	private void addInformationTab(Item item) {
		registry.addIngredientInfo(new ItemStack(item), ItemStack.class, I18n.format("jei.description.dissolution.%s", item.getUnlocalizedName()));
	}
	
	private void blacklistStuff() {
		IIngredientBlacklist blacklist = registry.getJeiHelpers().getIngredientBlacklist();
		blacklist.addIngredientToBlacklist(new ItemStack(ModItems.DEBUG_ITEM));
	}
	
	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        IRecipeRegistry recipeRegistry = jeiRuntime.getRecipeRegistry();
	}

}
