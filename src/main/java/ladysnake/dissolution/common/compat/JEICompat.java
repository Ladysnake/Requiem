package ladysnake.dissolution.common.compat;

import java.util.ArrayList;
import java.util.List;

import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.crafting.CrystallizerRecipe;
import ladysnake.dissolution.common.init.ModBlocks;
import ladysnake.dissolution.common.init.ModItems;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.IRecipeRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IIngredientBlacklist;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.api.recipe.IStackHelper;
import net.minecraft.item.ItemStack;

@JEIPlugin
public class JEICompat implements IModPlugin {

    public static IRecipeRegistry recipeRegistry;
	
	@Override
	public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {}

	@Override
	public void registerIngredients(IModIngredientRegistration registry) {}
	
	@Override
	public void registerCategories(IRecipeCategoryRegistration registry) {
		registry.addRecipeCategories(new CrystallizerRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
	}

	@Override
	public void register(IModRegistry registry) {
		IJeiHelpers jeiHelpers = registry.getJeiHelpers();
        blacklistStuff(jeiHelpers.getIngredientBlacklist());
        
		registry.handleRecipes(CrystallizerRecipe.class, crystallizerRecipe -> (ingredients -> {
				List<ItemStack> inputList = new ArrayList<ItemStack>();
				inputList.add(crystallizerRecipe.getInput());
				inputList.add(crystallizerRecipe.getFuel());
				ingredients.setInputs(ItemStack.class, inputList);
				ingredients.setOutput(ItemStack.class, crystallizerRecipe.getOutput());
			}), Reference.MOD_ID + ".crystallizer");
		
		registry.addRecipes(CrystallizerRecipe.crystallizingRecipes, Reference.MOD_ID + ".crystallizer");
		registry.addRecipeCatalyst(new ItemStack(ModBlocks.CRYSTALLIZER), Reference.MOD_ID + ".crystallizer");
	}
	
	public void blacklistStuff(IIngredientBlacklist blacklist) {
		blacklist.addIngredientToBlacklist(new ItemStack(ModItems.DEBUG_ITEM));
	}
	
	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
		recipeRegistry = jeiRuntime.getRecipeRegistry();
	}

}
