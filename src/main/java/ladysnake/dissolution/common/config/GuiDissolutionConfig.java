package ladysnake.dissolution.common.config;

import ladysnake.dissolution.common.Reference;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

import java.util.List;

public class GuiDissolutionConfig extends GuiConfig {
    public GuiDissolutionConfig(GuiScreen parentScreen, List<IConfigElement> configElements, String title) {
        super(parentScreen, configElements, Reference.MOD_ID, false, false, title);
    }
}
