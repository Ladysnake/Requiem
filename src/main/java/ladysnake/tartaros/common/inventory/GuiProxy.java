package ladysnake.tartaros.common.inventory;

import ladysnake.tartaros.common.tileentities.TileEntityCrystallizer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiProxy implements IGuiHandler {

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		BlockPos pos = new BlockPos(x, y, z);
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityCrystallizer) {
            return new ContainerCrystallizer(player.inventory, (TileEntityCrystallizer) te);
        }
        return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		BlockPos pos = new BlockPos(x, y, z);
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityCrystallizer) {
        	TileEntityCrystallizer containerTileEntity = (TileEntityCrystallizer) te;
            return new GuiCrystallizer(containerTileEntity, new ContainerCrystallizer(player.inventory, containerTileEntity));
        }
        return null;
	}

}
