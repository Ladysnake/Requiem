package ladysnake.dissolution.common.blocks;

import java.util.Random;

import ladysnake.dissolution.api.IIncorporealHandler;
import ladysnake.dissolution.api.ISoulInteractable;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.init.ModItems;
import ladysnake.dissolution.common.tileentities.TileEntitySepulture;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public class BlockSepulture extends BlockHorizontal implements ISoulInteractable {

	public static final PropertyEnum<BlockSepulture.EnumPartType> PART = PropertyEnum.create("part",
			BlockSepulture.EnumPartType.class);
	protected static final AxisAlignedBB SEPULTURE_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.5625D, 1.0D);

	public BlockSepulture() {
		super(Material.ROCK);
		this.setDefaultState(this.blockState.getBaseState().withProperty(PART, BlockSepulture.EnumPartType.FOOT));
		this.setHardness(1f);
		this.setHarvestLevel("pickaxe", 0);
	}

	public enum EnumPartType implements IStringSerializable {
		HEAD("head"), FOOT("foot");

		private final String name;

		EnumPartType(String name) {
			this.name = name;
		}

		public String toString() {
			return this.name;
		}

		@Nonnull
		public String getName() {
			return this.name;
		}
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		IIncorporealHandler playerCorp = CapabilityIncorporealHandler.getHandler(playerIn);
		TileEntitySepulture tile = this.getTE(worldIn, pos);
		if(tile == null) return false;

		if (playerCorp.getCorporealityStatus().isIncorporeal()) {
			tile.setDeathMessage(playerCorp.getDeathStats().getLastDeathMessage());
		} else {
				if (tile.getDeathMessage() != null
						&& !tile.getDeathMessage().trim().isEmpty())
					playerIn.sendStatusMessage(new TextComponentString(tile.getDeathMessage()),
							false);
		}
		return true;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
		EnumFacing enumfacing = state.getValue(FACING);

		if (state.getValue(PART) == BlockSepulture.EnumPartType.HEAD) {
			if (worldIn.getBlockState(pos.offset(enumfacing.getOpposite())).getBlock() != this) {
				worldIn.setBlockToAir(pos);
			}
		} else if (worldIn.getBlockState(pos.offset(enumfacing)).getBlock() != this) {
			worldIn.setBlockToAir(pos);

			if (!worldIn.isRemote) {
				this.dropBlockAsItem(worldIn, pos, state, 0);
			}
		}
	}

	@Nonnull
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return state.getValue(PART) == BlockSepulture.EnumPartType.HEAD ? Items.AIR : ModItems.SEPULTURE;
	}

	@Nonnull
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return SEPULTURE_AABB;
	}

	@Override
	public void dropBlockAsItemWithChance(World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, float chance, int fortune) {
		if (state.getValue(PART) == BlockSepulture.EnumPartType.FOOT) {
			super.dropBlockAsItemWithChance(worldIn, pos, state, chance, 0);
		}
	}

	@Nonnull
	@Override
	public EnumPushReaction getMobilityFlag(IBlockState state) {
		return EnumPushReaction.DESTROY;
	}

	@Nonnull
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	@Nonnull
	@Override
	public ItemStack getItem(World worldIn, BlockPos pos, @Nonnull IBlockState state) {
		return new ItemStack(ModItems.SEPULTURE);
	}

	@Override
	public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
		if (player.capabilities.isCreativeMode && state.getValue(PART) == BlockSepulture.EnumPartType.HEAD) {
			BlockPos blockpos = pos.offset(state.getValue(FACING).getOpposite());

			if (worldIn.getBlockState(blockpos).getBlock() == this) {
				worldIn.setBlockToAir(blockpos);
			}
		}
	}

	@Nonnull
	public IBlockState getStateFromMeta(int meta) {
		EnumFacing enumfacing = EnumFacing.getHorizontal(meta);
		return (meta & 8) > 0
				? this.getDefaultState().withProperty(PART, BlockSepulture.EnumPartType.HEAD).withProperty(FACING,
						enumfacing)
				: this.getDefaultState().withProperty(PART, BlockSepulture.EnumPartType.FOOT).withProperty(FACING,
						enumfacing);
	}

	@Nonnull
	public IBlockState getActualState(@Nonnull IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		if (state.getValue(PART) == BlockSepulture.EnumPartType.FOOT) {
			IBlockState iblockstate = worldIn.getBlockState(pos.offset(state.getValue(FACING)));
		}

		return state;
	}

	@Nonnull
	public IBlockState withRotation(@Nonnull IBlockState state, Rotation rot) {
		return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Nonnull
	public IBlockState withMirror(@Nonnull IBlockState state, Mirror mirrorIn) {
		return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
	}

	public int getMetaFromState(IBlockState state) {
		int i;
		i = state.getValue(FACING).getHorizontalIndex();

		if (state.getValue(PART) == BlockSepulture.EnumPartType.HEAD) {
			i |= 8;
		}

		return i;
	}

	@Nonnull
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING, PART);
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return state.getValue(PART) == EnumPartType.HEAD;
	}

	@Override
	public TileEntity createTileEntity(@Nonnull World worldIn, @Nonnull IBlockState state) {
		return new TileEntitySepulture();
	}

	private TileEntitySepulture getTE(IBlockAccess world, BlockPos pos) {
		if (world.getBlockState(pos).getValue(PART) == EnumPartType.HEAD)
			return (TileEntitySepulture) world.getTileEntity(pos);
		if (world.getTileEntity(
				pos.offset(world.getBlockState(pos).getValue(FACING))) instanceof TileEntitySepulture)
			return (TileEntitySepulture) world
					.getTileEntity(pos.offset(world.getBlockState(pos).getValue(FACING)));
		return null;
	}

}
