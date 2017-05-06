package ladysnake.dissolution.common.compat;

import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.inventory.GuiCrystallizer;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.gui.IDrawableAnimated.StartDirection;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class CrystallizerRecipeCategory extends BlankRecipeCategory {

	public static final ResourceLocation background_texture = new ResourceLocation(Reference.MOD_ID, "textures/gui/container/crystallizer_jei.png");
	private final IDrawable background;
	private final IDrawableAnimated progressBar, fuelBar;
    private final String name;
    private IGuiHelper helper = null;
    
	
	public CrystallizerRecipeCategory(IGuiHelper helper) {
		super();
		this.background = helper.createDrawable(background_texture, 0, 0, GuiCrystallizer.WIDTH, GuiCrystallizer.HEIGHT);
		this.progressBar = helper.createAnimatedDrawable(helper.createDrawable(background_texture, 176, 14, 25, 13), 200, StartDirection.LEFT, false);
		this.fuelBar = helper.createAnimatedDrawable(helper.createDrawable(background_texture, 176, 13, 14, 1), 600, StartDirection.BOTTOM, true);
		this.name = I18n.format("dissolution.jei.recipe.crystallizer");
	}
	
	@Override
	public void drawExtras(Minecraft minecraft) {
//		progressBar.draw(minecraft, 24, 50);
//		fuelBar.draw(minecraft, 2, 0);
	}

	@Override
	public String getUid() {
		return Reference.MOD_ID + ".crystallizer";
	}

	@Override
	public String getTitle() {
		return name;
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, IRecipeWrapper recipeWrapper, IIngredients ingredients) {
		IGuiItemStackGroup stacks = recipeLayout.getItemStacks();
		
		stacks.init(0, true, 56, 17);
		stacks.init(1, true, 56, 53);
		stacks.init(2, false, 116, 35);
		
		stacks.set(0, ingredients.getInputs(ItemStack.class).get(0));
		stacks.set(1, ingredients.getInputs(ItemStack.class).get(1));
		stacks.set(2, ingredients.getOutputs(ItemStack.class).get(0));
	}

}
