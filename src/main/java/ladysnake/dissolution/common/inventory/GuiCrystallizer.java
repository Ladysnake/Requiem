package ladysnake.dissolution.common.inventory;

import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.tileentities.TileEntityCrystallizer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.ResourceLocation;

public class GuiCrystallizer extends GuiContainer {
	public static final int WIDTH = 176;
    public static final int HEIGHT = 166;
    
    private static final ResourceLocation background = new ResourceLocation(Reference.MOD_ID, "textures/gui/container/crystallizer.png");
	
    private TileEntityCrystallizer tileCrystallizer;
    
	public GuiCrystallizer(TileEntityCrystallizer te, ContainerCrystallizer container) {
		super(container);
		
		xSize = WIDTH;
        ySize = HEIGHT;
        
        tileCrystallizer = te;
	}

	@Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        mc.getTextureManager().bindTexture(background);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        
        if (TileEntityFurnace.isBurning(this.tileCrystallizer))
        {
            int k = this.getBurnLeftScaled(13);
            this.drawTexturedModalRect(guiLeft + 56, guiTop + 36 + 13 - k, 176, 13 - k, 14, k + 1);
        }
        
        int l = this.getCookProgressScaled(24);
        this.drawTexturedModalRect(guiLeft + 79, guiTop + 34, 176, 14, l + 1, 16);
    }
	
	private int getCookProgressScaled(int pixels)
    {
        int i = this.tileCrystallizer.getField(2);
        int j = this.tileCrystallizer.getField(3);
        return j != 0 && i != 0 ? i * pixels / j : 0;
    }

    private int getBurnLeftScaled(int pixels)
    {
        int i = this.tileCrystallizer.getField(1);

        if (i == 0)
        {
            i = 200;
        }

        return this.tileCrystallizer.getField(0) * pixels / i;
    }

}
