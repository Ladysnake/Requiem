package ladysnake.tartaros.common.compat;

import ladysnake.tartaros.common.Reference;
import ladysnake.tartaros.common.crafting.CrystallizerRecipe;
import ladysnake.tartaros.common.crafting.CrystallizerRecipes;
import ladysnake.tartaros.common.init.ModBlocks;
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
import mezz.jei.api.recipe.BlankRecipeWrapper;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.IRecipeWrapperFactory;
import mezz.jei.api.recipe.IStackHelper;
import net.minecraft.item.ItemStack;

@JEIPlugin
public class JEICompat implements IModPlugin {

	public static IStackHelper stackHelper;
    public static IJeiHelpers jeiHelpers;
    public static IRecipeRegistry recipeRegistry;
	
	@Override
	public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {}

	@Override
	public void registerIngredients(IModIngredientRegistration registry) {}

	@Override
	public void register(IModRegistry registry) {
		jeiHelpers = registry.getJeiHelpers();
        stackHelper = jeiHelpers.getStackHelper();
        IGuiHelper guiHelper = registry.getJeiHelpers().getGuiHelper();
        
		registry.addRecipeCategories(new CrystallizerRecipeCategory(guiHelper));
		registry.handleRecipes(CrystallizerRecipe.class, (CrystallizerRecipe cr) -> new BlankRecipeWrapper() {

			@Override
			public void getIngredients(IIngredients ingredients) {
				ingredients.setInput(ItemStack.class, cr.getInput());
				ingredients.setOutput(ItemStack.class, cr.getOutput());
			}
			
		}, Reference.MOD_ID + ".crystallizer");
		
		registry.addRecipes(CrystallizerRecipes.crystallizingRecipes, Reference.MOD_ID + ".crystallizer");
		registry.addRecipeCategoryCraftingItem(new ItemStack(ModBlocks.CRYSTALLIZER), Reference.MOD_ID + ".crystallizer");
	}
	
	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
		recipeRegistry = jeiRuntime.getRecipeRegistry();
	}

}
