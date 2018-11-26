package ladysnake.dissolution.client.config;

import ladysnake.dissolution.common.Ref;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

import java.util.List;

public class GuiDissolutionConfig extends GuiConfig {
    public GuiDissolutionConfig(GuiScreen parentScreen, List<IConfigElement> configElements, String title) {
        super(parentScreen, configElements, Ref.MOD_ID, false, false, title);
    }
}
