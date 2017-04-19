package ladysnake.tartaros.common.blocks;

import ladysnake.tartaros.common.Reference;
import ladysnake.tartaros.common.capabilities.IIncorporealHandler;
import ladysnake.tartaros.common.capabilities.IncorporealDataHandler;
import ladysnake.tartaros.common.networkingtest.PacketHandler;
import ladysnake.tartaros.common.networkingtest.SimpleMessage;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;

public class BlockMercuriusWaystone extends Block implements IRespawnLocation {

	public BlockMercuriusWaystone() {
		super(Material.ROCK);
		this.setUnlocalizedName(Reference.Blocks.MERCURIUS_WAYSTONE.getUnlocalizedName());
		this.setRegistryName(Reference.Blocks.MERCURIUS_WAYSTONE.getRegistryName());
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		IIncorporealHandler playerCorp = IncorporealDataHandler.getHandler(playerIn);
		playerCorp.setIncorporeal(false, playerIn);
		IMessage msg = new SimpleMessage(playerIn.getUniqueID().getMostSignificantBits(), playerIn.getUniqueID().getLeastSignificantBits(), false);
		PacketHandler.net.sendToAll(msg);
		return true;
	}
}
