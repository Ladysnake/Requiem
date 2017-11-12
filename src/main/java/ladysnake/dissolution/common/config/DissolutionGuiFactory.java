package ladysnake.dissolution.common.config;

import com.google.common.collect.Lists;
import ladysnake.dissolution.common.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.DummyConfigElement;
import net.minecraftforge.fml.client.config.IConfigElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DissolutionGuiFactory implements IModGuiFactory {

    @Override
    public void initialize(Minecraft minecraftInstance) {
    }

    @Override
    public boolean hasConfigGui() {
        return true;
    }

    @Override
    public GuiScreen createConfigGui(GuiScreen parentScreen) {
        List<IConfigElement> elements = new ArrayList<>();
        Set<String> catNames = DissolutionConfigManager.getCategoryNames();
        for (String catName : catNames) {
            if (catName.isEmpty())
                continue;
            ConfigCategory category = DissolutionConfigManager.config.getCategory(catName);
            DummyConfigElement.DummyCategoryElement element = new DummyConfigElement.DummyCategoryElement(category.getName(), category.getLanguagekey(), new ConfigElement(category).getChildElements());
            element.setRequiresMcRestart(category.requiresMcRestart());
            element.setRequiresWorldRestart(category.requiresWorldRestart());
            elements.add(element);
        }
        elements.addAll(new ConfigElement(DissolutionConfigManager.config.getCategory(Configuration.CATEGORY_GENERAL)).getChildElements());
        return new GuiDissolutionConfig(parentScreen, elements, Reference.MOD_NAME);
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }
}
