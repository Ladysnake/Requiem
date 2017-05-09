package ladysnake.dissolution.common.compat;

import java.util.ArrayList;
import java.util.List;

import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.crafting.CrystallizerRecipe;
import ladysnake.dissolution.common.init.ModBlocks;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.IRecipeRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.BlankRecipeWrapper;
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
				List<ItemStack> inputList = new ArrayList<ItemStack>();
				inputList.add(cr.getInput());
				inputList.add(cr.getFuel());
				ingredients.setInputs(ItemStack.class, inputList);
				ingredients.setOutput(ItemStack.class, cr.getOutput());
			}
			
		}, Reference.MOD_ID + ".crystallizer");
		
		registry.addRecipes(CrystallizerRecipe.crystallizingRecipes, Reference.MOD_ID + ".crystallizer");
		registry.addRecipeCategoryCraftingItem(new ItemStack(ModBlocks.CRYSTALLIZER), Reference.MOD_ID + ".crystallizer");
	}
	
	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
		recipeRegistry = jeiRuntime.getRecipeRegistry();
	}

}
