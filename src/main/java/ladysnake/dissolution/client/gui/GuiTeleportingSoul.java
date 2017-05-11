package ladysnake.dissolution.client.gui;

import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiScreenEvent.DrawScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiTeleportingSoul extends GuiScreen {
	
	private static int transfert = 0;
	public static final int TELEPORT_TO_NETHER = -1;
	public static final int TELEPORT_TO_OVERWORLD = 1;
	
	public static void setTransfert(int transfert) {
		GuiTeleportingSoul.transfert = transfert;
	}
	
	@SubscribeEvent
	public void onDrawScreen(DrawScreenEvent.Post event) {
		if(event.getGui() instanceof GuiDownloadTerrain && transfert != 0) {
			this.drawLoadingScreen(event.getGui().width, event.getGui().height);
		}
	}
	
	public void drawLoadingScreen(int width, int height) {
		
	}

}
