package ladysnake.dissolution.common.inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.Dissolution;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

public class TartarosGuiFactory implements IModGuiFactory {

	@Override
	public void initialize(Minecraft minecraftInstance) {

	}

	@Override
	public boolean hasConfigGui() {
		return true;
	}

	@Override
	public GuiScreen createConfigGui(GuiScreen parentScreen) {
		return null;
	}

	@Override
	public Class<? extends GuiScreen> mainConfigGuiClass() {
		return GuiTartarosConfig.class;
	}

	@Override
	public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
		return null;
	}

	@Override
	public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
		return null;
	}
	
	public static class GuiTartarosConfig extends GuiConfig {
		public GuiTartarosConfig(GuiScreen parentScreen) {
			super(parentScreen, new ConfigElement(Dissolution.config.getCategory(Configuration.CATEGORY_GENERAL)).getChildElements(), Reference.MOD_ID, false, false, GuiConfig.getAbridgedConfigPath(Dissolution.config.toString()));
		}
	}

}
