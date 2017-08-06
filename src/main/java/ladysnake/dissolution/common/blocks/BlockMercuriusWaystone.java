package ladysnake.dissolution.common.blocks;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.capabilities.IIncorporealHandler;
import ladysnake.dissolution.common.config.DissolutionConfig;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.init.ModBlocks;
import ladysnake.dissolution.common.networking.IncorporealMessage;
import ladysnake.dissolution.common.networking.PacketHandler;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
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

public class BlockMercuriusWaystone extends Block implements ISoulInteractable {
	
	private static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(0.0625 * 4, 0, 0.0625 * 4, 0.0625 * 12, 0.0625 * 16, 0.0625 * 12);
	private static final AxisAlignedBB COLLISION_BOX = new AxisAlignedBB(0.300D, 0.0D, 0.300D, 0.700D, 1.0D, 0.700D);
	protected static boolean checkBreaking = true;

	public BlockMercuriusWaystone() {
		super(Material.ROCK);
		this.setHardness(1.0f);
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		final IIncorporealHandler playerCorp = CapabilityIncorporealHandler.getHandler(playerIn);
		if(playerCorp.isIncorporeal()){
			playerCorp.setIncorporeal(false);
			
			if(DissolutionConfig.blocks.oneUseWaystone)
				worldIn.setBlockToAir(pos);


			if (!worldIn.isRemote)
			{
				WorldServer worldserver = (WorldServer)worldIn;
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
			placeSoulAnchor(worldIn, pos, placer);
	    	super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
		}
		
	}
	
	public void placeSoulAnchor(World worldIn, BlockPos pos, Entity placer) {
		if (worldIn.provider.getDimensionType().getId() != -1) {
			WorldServer worldservernether = worldIn.getMinecraftServer().getWorld(-1);
		    BlockPos bp = getAnchorBaseSpawnPos(worldservernether, pos);
		    if(bp.getY() > 0)  {
		    	BlockSoulAnchor.scheduledBP.add(pos);
		    	BlockSoulAnchor.scheduledBP.add(pos);
		    	BlockSoulAnchor.scheduledDim.add(placer.dimension);
		    	BlockSoulAnchor.scheduledDim.add(placer.dimension);
		    	worldservernether.setBlockState(bp, ModBlocks.SOUL_ANCHOR.getDefaultState(), 3);
		    }
		    else {
		    	if(placer instanceof EntityPlayer) {
		    		((EntityPlayer)placer).sendStatusMessage(new TextComponentTranslation(this.getUnlocalizedName() + ".cannotplace", new Object[0]), true);
		    		((EntityPlayer)placer).inventory.addItemStackToInventory(new ItemStack(this));
		    	}
		    	WorldServer worldserveroverworld = (WorldServer)worldIn;
				Random rand = new Random();
				for(int i = 0; i < 50; i++) {
				    double motionX = rand.nextGaussian() * 0.02D;
				    double motionY = rand.nextGaussian() * 0.02D;
				    double motionZ = rand.nextGaussian() * 0.02D;
				    worldserveroverworld.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, false, pos.getX() + 0.5D, pos.getY()+ 1.0D, pos.getZ()+ 0.5D, 1, 0.3D, 0.3D, 0.3D, 0.0D, new int[0]); 
				}
		    	checkBreaking = false;
		    	worldIn.setBlockToAir(pos);
		    	checkBreaking = true;
		    	return;
		    }
		    //System.out.println(bp);
		}
	}
	
	@Override
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
		/*
		if(!worldIn.isRemote) {
			WorldServer worldserver = worldIn.getMinecraftServer().worldServerForDimension(-1);
			return super.canPlaceBlockAt(worldIn, pos) && getAnchorSpawnPos(worldserver, pos).getY() > 0;
		}*/
		return super.canPlaceBlockAt(worldIn, pos);
	}
	
	protected BlockPos getAnchorBaseSpawnPos (WorldServer worldserver, BlockPos pos) {
		BlockPos bp = new BlockPos(pos.getX()/8, 120, pos.getZ()/8);
		
		//while there is no ground || in a wall || no place upward
	    while((worldserver.getBlockState(bp.down()) == Blocks.AIR.getDefaultState() || 
	    		worldserver.getBlockState(bp) != Blocks.AIR.getDefaultState() ||
	    		worldserver.getBlockState(bp.up()) != Blocks.AIR.getDefaultState()) && bp.getY() > 0) {
	    	if(worldserver.getBlockState(bp.down(2)).getBlock() instanceof BlockSoulAnchor) {
	    		bp = bp.down(120);
	    	}
	    	bp = bp.down();
	    }
	    return bp;
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
	public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean p_185477_7_)
    {
		addCollisionBoxToList(pos, entityBox, collidingBoxes, COLLISION_BOX);
	}
	
	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isFullBlock(IBlockState state) {
		return false;
	}
	
	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		super.breakBlock(worldIn, pos, state);
		if(!checkBreaking)
			return;
		
		BlockPos bp = new BlockPos(pos.getX()/8, 120, pos.getZ()/8);
		WorldServer worldserver = worldIn.getMinecraftServer().getWorld(-1);
		while((worldserver.getBlockState(bp) != ModBlocks.SOUL_ANCHOR.getDefaultState()) && bp.getY() > 0)
	    	bp = bp.down();
		if(bp.getY() > 0)
			worldserver.setBlockToAir(bp);
	}
	
}
