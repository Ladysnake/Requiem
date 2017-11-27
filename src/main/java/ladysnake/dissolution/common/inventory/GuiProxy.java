package ladysnake.dissolution.common.inventory;

import ladysnake.dissolution.common.entity.EntityPlayerCorpse;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class GuiProxy implements IGuiHandler {

    public static final int PLAYER_CORPSE = 0;

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        switch (ID) {
            case PLAYER_CORPSE:
                EntityPlayerCorpse pc = world.getEntitiesWithinAABB(EntityPlayerCorpse.class, new AxisAlignedBB(pos)).stream().findAny().orElse(null);
                if (pc != null)
                    return new ContainerChest(player.inventory, pc.getInventory(), player);
            default:
                return null;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        switch (ID) {
            case PLAYER_CORPSE:
                List<EntityPlayerCorpse> pc = world.getEntitiesWithinAABB(EntityPlayerCorpse.class, new AxisAlignedBB(pos));
                if (!pc.isEmpty())
                    return new GuiChest(player.inventory, pc.get(0).getInventory());
        }
        return null;
    }

}
