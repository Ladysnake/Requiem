package ladysnake.dissolution.common.inventory;

import java.util.List;
import java.util.Optional;

import ladysnake.dissolution.common.entity.EntityPlayerCorpse;
import ladysnake.dissolution.common.tileentities.TileEntityCrystallizer;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiProxy implements IGuiHandler {
	
	public static final int CRYSTALLIZER = 0;
	public static final int PLAYER_CORPSE = 1;
	
	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		BlockPos pos = new BlockPos(x, y, z);
        TileEntity te = world.getTileEntity(pos);
        switch(ID) {
        case CRYSTALLIZER:
        	if (te instanceof TileEntityCrystallizer)
        		return new ContainerCrystallizer(player.inventory, (TileEntityCrystallizer) te);
        case PLAYER_CORPSE:
        	EntityPlayerCorpse pc = world.getEntitiesWithinAABB(EntityPlayerCorpse.class, new AxisAlignedBB(pos)).stream().findAny().orElse(null);
        	if(pc != null)
        		return new ContainerChest(player.inventory, pc.getInventory(), player);
        default: return null;
        }
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		BlockPos pos = new BlockPos(x, y, z);
        TileEntity te = world.getTileEntity(pos);
        switch(ID) {
        case CRYSTALLIZER:
            if (te instanceof TileEntityCrystallizer)
                return new GuiCrystallizer((TileEntityCrystallizer) te, new ContainerCrystallizer(player.inventory, (TileEntityCrystallizer) te));
        case PLAYER_CORPSE:
        	List<EntityPlayerCorpse> pc = world.getEntitiesWithinAABB(EntityPlayerCorpse.class, new AxisAlignedBB(pos));
        	if(!pc.isEmpty())
        		return new GuiChest(player.inventory, pc.get(0).getInventory());
        }
        return null;
	}

}
