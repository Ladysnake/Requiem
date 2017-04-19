package ladysnake.tartaros.common.capabilities;

import net.minecraft.entity.player.EntityPlayer;

public interface IIncorporealHandler {
	
	public void setIncorporeal(boolean ghostMode, EntityPlayer p);

	public void setIncorporeal(int ghostMode);
	
	public boolean isIncorporeal();
	
	public int getIncorporeal();

}
