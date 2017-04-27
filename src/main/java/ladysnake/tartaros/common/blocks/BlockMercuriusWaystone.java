package ladysnake.tartaros.common.blocks;

import java.util.List;
import java.util.Random;

import ladysnake.tartaros.common.Reference;
import ladysnake.tartaros.common.capabilities.IIncorporealHandler;
import ladysnake.tartaros.common.capabilities.IncorporealDataHandler;
import ladysnake.tartaros.common.init.ModBlocks;
import ladysnake.tartaros.common.networking.PacketHandler;
import ladysnake.tartaros.common.networking.IncorporealMessage;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;

public class BlockMercuriusWaystone extends Block implements IRespawnLocation {
	
	private static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(0.0625 * 4, 0, 0.0625 * 4, 0.0625 * 12, 0.0625 * 16, 0.0625 * 12);
	private static final AxisAlignedBB COLLISION_BOX = new AxisAlignedBB(0.0625 * 4, 0, 0.0625 * 4, 0.0625 * 12, 0.0625 * 16, 0.0625 * 12);

	public BlockMercuriusWaystone() {
		super(Material.ROCK);
		this.setUnlocalizedName(Reference.Blocks.MERCURIUS_WAYSTONE.getUnlocalizedName());
		this.setRegistryName(Reference.Blocks.MERCURIUS_WAYSTONE.getRegistryName());
		this.setHardness(1.0f);
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		IIncorporealHandler playerCorp = IncorporealDataHandler.getHandler(playerIn);
		if(playerCorp.isIncorporeal()){
			playerCorp.setIncorporeal(false, playerIn);
			IMessage msg = new IncorporealMessage(playerIn.getUniqueID().getMostSignificantBits(), playerIn.getUniqueID().getLeastSignificantBits(), false);
			PacketHandler.net.sendToAll(msg);
			worldIn.setBlockToAir(pos);


			if (!worldIn.isRemote)
			{
				WorldServer worldserver = (WorldServer)worldIn;
				//System.out.println("particles !");
				Random rand = new Random();
				for(int i = 0; i < 50; i++) {
				    double motionX = rand.nextGaussian() * 0.02D;
				    double motionY = rand.nextGaussian() * 0.02D;
				    double motionZ = rand.nextGaussian() * 0.02D;
				    worldserver.spawnParticle(EnumParticleTypes.CLOUD, false, pos.getX() + 0.5D, pos.getY()+ 1.0D, pos.getZ()+ 0.5D, 1, 0.3D, 0.3D, 0.3D, 0.0D, new int[0]); 
				}
			}


		}
		return true;
	}
	
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer,
			ItemStack stack) {
		if(!worldIn.isRemote) {
			if (worldIn.provider.getDimensionType().getId() != -1) {
				WorldServer worldserver = worldIn.getMinecraftServer().worldServerForDimension(-1);
			    BlockPos bp = new BlockPos(pos.getX()/8, 120, pos.getZ()/8);
			    while((worldserver.getBlockState(bp.down()) == Blocks.AIR.getDefaultState() || worldserver.getBlockState(bp) != Blocks.AIR.getDefaultState()) && bp.getY() > 0)
			    	bp = bp.down();
			    if(bp.getY() > 0)
			    	worldserver.setBlockState(bp, ModBlocks.soul_anchor.getDefaultState(), 0b11);
			    else {
			    	if(placer instanceof EntityPlayer)
			    		((EntityPlayer)placer).sendStatusMessage(new TextComponentTranslation("tile.waystone.cannotplace", new Object[0]), true);
			    	return;
			    }
			    System.out.println(bp);
			}
	    	super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
		}
		
	}
	
	@Override
	public int quantityDropped(Random random) {
		return 1;
	}
	
	@Override
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return BOUNDING_BOX;
	}
	
	@Override
	public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox,
			List<AxisAlignedBB> collidingBoxes, Entity entityIn, boolean p_185477_7_) {
		super.addCollisionBoxToList(pos, entityBox, collidingBoxes, COLLISION_BOX);
	}
	
	
}
