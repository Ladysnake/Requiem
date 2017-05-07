package ladysnake.dissolution.common.tileentities;

import java.util.List;
import java.util.Random;

import ladysnake.dissolution.common.capabilities.IncorporealDataHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntitySulfurCandle extends TileEntity implements ITickable {

	@Override
	public void update() {
		if (this.world.getTotalWorldTime() % 40L == 0L)
        {
            this.updateCandle();
        }		
	}
	
	public void updateCandle() {
		int x = this.pos.getX();
		int y = this.pos.getY();
		int z = this.pos.getZ();
		AxisAlignedBB affectedArea = new AxisAlignedBB(x, y, z, x +1, y + 1, z+1).expandXyz(20);
		List<EntityPlayer> players = this.world.getEntitiesWithinAABB(EntityPlayer.class, affectedArea);
		for(EntityPlayer p : players) {
			IncorporealDataHandler.getHandler(p).setSoulCandleNearby(true, 2);
		}

	}
	
}
