package ladysnake.dissolution.common.compat;

import java.util.ArrayList;
import java.util.List;

import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.crafting.CrystallizerRecipe;
import ladysnake.dissolution.common.init.ModBlocks;
import ladysnake.dissolution.common.init.ModItems;
import mezz.jei.api.IJeiHelpers;
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

@JEIPlugin
public class JEICompat implements IModPlugin {

    private IRecipeRegistry recipeRegistry;
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
	public void register(IModRegistry registry) {
        this.registry = registry;
        blacklistStuff();
		addInformationTabs();
	}
	
	private void addInformationTabs() {
		addInformationTab(ModItems.SEPULTURE);
	}
	
	private void addInformationTab(Block block) {
		this.addInformationTab(Item.getItemFromBlock(block));
	}
	
	private void addInformationTab(Item item) {
		registry.addIngredientInfo(new ItemStack(item), ItemStack.class, I18n.format("jei.description.dissolution.%s", item.getUnlocalizedName()));
	}
	
	private void blacklistStuff() {
		IIngredientBlacklist blacklist = registry.getJeiHelpers().getIngredientBlacklist();
		blacklist.addIngredientToBlacklist(new ItemStack(ModItems.DEBUG_ITEM));
	}
	
	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
		recipeRegistry = jeiRuntime.getRecipeRegistry();
	}

}
