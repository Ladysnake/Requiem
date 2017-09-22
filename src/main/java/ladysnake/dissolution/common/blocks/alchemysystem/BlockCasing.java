package ladysnake.dissolution.common.blocks.alchemysystem;

import java.util.Random;

import ladysnake.dissolution.client.models.blocks.PropertyBoolean;
import ladysnake.dissolution.client.models.blocks.UnlistedPropertyModulePresent;
import ladysnake.dissolution.common.DissolutionConfig;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.blocks.alchemysystem.IPowerConductor.IMachine;
import ladysnake.dissolution.common.init.ModItems;
import ladysnake.dissolution.common.items.ItemAlchemyModule;
import ladysnake.dissolution.common.registries.modularsetups.ISetupInstance;
import ladysnake.dissolution.common.registries.modularsetups.SetupPowerGenerator;
import ladysnake.dissolution.common.tileentities.TileEntityModularMachine;
import ladysnake.dissolution.common.tileentities.TileEntityProxy;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public class BlockCasing extends AbstractPowerConductor implements IMachine {
	
	public static final UnlistedPropertyModulePresent MODULES_PRESENT = new UnlistedPropertyModulePresent();
	public static final PropertyBoolean PLUG_NORTH = new PropertyBoolean("plug_north");
	public static final PropertyBoolean PLUG_EAST = new PropertyBoolean("plug_east");
	public static final PropertyBoolean PLUG_SOUTH = new PropertyBoolean("plug_south");
	public static final PropertyBoolean PLUG_WEST = new PropertyBoolean("plug_west");
	public static final PropertyBoolean RUNNING = new PropertyBoolean("running");
	public static final PropertyEnum<EnumPartType> PART = PropertyEnum.create("part", EnumPartType.class);
	public static final PropertyDirection FACING = BlockHorizontal.FACING;
	public static final ResourceLocation CASING_BOTTOM = new ResourceLocation(Reference.MOD_ID, "machine/wooden_machine_casing_bottom");
	public static final ResourceLocation CASING_TOP = new ResourceLocation(Reference.MOD_ID, "machine/wooden_machine_casing_top");
	public static final ResourceLocation PLUG = new ResourceLocation(Reference.MOD_ID, "machine/pipe/plug");
	
	public BlockCasing() {
		super();
		this.setDefaultState(this.blockState.getBaseState().withProperty(PART, EnumPartType.BOTTOM));
		this.setHardness(1f);
        this.setSoundType(SoundType.WOOD);
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack stack = playerIn.getHeldItem(hand);
		EnumPartType part = state.getValue(PART);
		if(part == EnumPartType.TOP)
			pos = pos.down();
		TileEntity te = worldIn.getTileEntity(pos);
		if(te instanceof TileEntityModularMachine) {
			if(stack.getItem() instanceof ItemAlchemyModule) {
				if(((TileEntityModularMachine)te).addModule((ItemAlchemyModule)stack.getItem()) && !playerIn.isCreative()) {
					stack.shrink(1);
				}
			} else if(playerIn.getHeldItemMainhand().isEmpty() && playerIn.isSneaking()) {
				playerIn.addItemStackToInventory(((TileEntityModularMachine)te).removeModule());
			} else {
				((TileEntityModularMachine)te).interact(playerIn, hand, part, facing, hitX, hitY, hitZ);
			}
		}
		return true;
	}
	
	@Override
	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
		if(state.getValue(PART) == EnumPartType.TOP)
			pos = pos.down();
		TileEntity te = worldIn.getTileEntity(pos);
		if(te instanceof TileEntityModularMachine) {
			ISetupInstance setup = ((TileEntityModularMachine)te).getCurrentSetup();
			if(setup instanceof SetupPowerGenerator.Instance)
				((SetupPowerGenerator.Instance)setup).scheduleUpdate();
		}
	}
	
	@Override
	@Deprecated
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Nonnull
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}
	
	@Override
	public boolean rotateBlock(World world, @Nonnull BlockPos pos, @Nonnull EnumFacing axis) {
		BlockPos pos2;
		if(world.getBlockState(pos).getValue(PART) == EnumPartType.TOP)
			pos2 = pos.down();
		else
			pos2 = pos.up();
		return super.rotateBlock(world, pos, axis) && super.rotateBlock(world, pos2, axis);
	}

	@Nonnull
	protected BlockStateContainer createBlockState() {
		return new ExtendedBlockState(this, new IProperty[] {FACING, PART, POWERED}, new IUnlistedProperty[] {MODULES_PRESENT, RUNNING, PLUG_EAST, PLUG_NORTH, PLUG_SOUTH, PLUG_WEST});
	}
	
	@Nonnull
	@Override
	public IBlockState getExtendedState(@Nonnull IBlockState state, IBlockAccess world, BlockPos pos) {
		TileEntity te = world.getTileEntity(state.getValue(PART) == EnumPartType.BOTTOM ? pos : pos.down());
		if(te instanceof TileEntityModularMachine) {
			EnumPartType part = state.getValue(PART);
			if (part == EnumPartType.BOTTOM)
				state = ((IExtendedBlockState) state)
						.withProperty(MODULES_PRESENT, ((TileEntityModularMachine) te).getInstalledModules())
						.withProperty(RUNNING, ((TileEntityModularMachine) te).isRunning());
			boolean flag = DissolutionConfig.client.plugsEverywhere;
			state = ((IExtendedBlockState) state)
					.withProperty(PLUG_EAST, flag || ((TileEntityModularMachine) te).isPlugAttached(EnumFacing.EAST, part))
					.withProperty(PLUG_NORTH, flag || ((TileEntityModularMachine) te).isPlugAttached(EnumFacing.NORTH, part))
					.withProperty(PLUG_WEST, flag || ((TileEntityModularMachine) te).isPlugAttached(EnumFacing.WEST, part))
					.withProperty(PLUG_SOUTH, flag || ((TileEntityModularMachine) te).isPlugAttached(EnumFacing.SOUTH, part));
		}
		return state;
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(@Nonnull World worldIn, @Nonnull IBlockState state) {
		return state.getValue(PART) == EnumPartType.BOTTOM ? new TileEntityModularMachine() : new TileEntityProxy();
	}

	@Override
	public PowerConsumption getPowerConsumption(IBlockAccess worldIn, BlockPos pos) {
		if(worldIn.getBlockState(pos).getValue(PART) == EnumPartType.TOP)
			pos = pos.down();
		TileEntity te = worldIn.getTileEntity(pos);
		if(te instanceof TileEntityModularMachine)
			return ((TileEntityModularMachine)te).getPowerConsumption();
		return PowerConsumption.NONE;
	}


	
	@Override
	public boolean shouldPowerConnect(IBlockAccess worldIn, BlockPos pos, EnumFacing facing) {
		EnumPartType part = worldIn.getBlockState(pos).getValue(PART);
		TileEntity te = worldIn.getTileEntity(part == EnumPartType.BOTTOM ? pos : pos.down());
		return te instanceof TileEntityModularMachine && ((TileEntityModularMachine) te).isPlugAttached(facing, part);
	}

	@Nonnull
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return state.getValue(PART) == EnumPartType.TOP ? Items.AIR : ModItems.WOODEN_CASING;
	}

	@Nonnull
	@Override
	@Deprecated
	public ItemStack getItem(World worldIn, BlockPos pos, @Nonnull IBlockState state) {
		return new ItemStack(ModItems.WOODEN_CASING);
	}
	
	@Override
	public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
		if(state.getValue(PART) == EnumPartType.TOP)
			pos = pos.down();

		if (player.capabilities.isCreativeMode) {

			if (worldIn.getBlockState(pos) == state.withProperty(PART, EnumPartType.BOTTOM)) {
				worldIn.setBlockToAir(pos);
			}
		}
		
		TileEntity te = worldIn.getTileEntity(pos);
		if(te instanceof TileEntityModularMachine)
			((TileEntityModularMachine)te).dropContent();
	}
	
	@Override
	public void setPowered(IBlockAccess worldIn, BlockPos pos, boolean b) {
		if(worldIn.getBlockState(pos).getValue(PART) == EnumPartType.TOP)
			pos = pos.down();
		TileEntity te = worldIn.getTileEntity(pos);
		if (te instanceof TileEntityModularMachine)
			((TileEntityModularMachine)te).setPowered(b);
	}
	
	@Override
	public boolean isPowered(IBlockAccess worldIn, BlockPos pos) {
		if(worldIn.getBlockState(pos).getValue(PART) == EnumPartType.TOP)
			pos = pos.down();
		TileEntity te = worldIn.getTileEntity(pos);
		return (te instanceof TileEntityModularMachine) && ((TileEntityModularMachine)te).isPowered();
		
	}

	@Nonnull
	@Deprecated
	public IBlockState withRotation(@Nonnull IBlockState state, Rotation rot) {
		return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Nonnull
	@Deprecated
	public IBlockState withMirror(@Nonnull IBlockState state, Mirror mirrorIn) {
		return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
	}

	@Nonnull
	public IBlockState getStateFromMeta(int meta) {
		EnumFacing enumfacing = EnumFacing.getHorizontal(meta);
		return (meta & 8) > 0
				? this.getDefaultState().withProperty(PART, EnumPartType.TOP).withProperty(FACING,
						enumfacing)
				: this.getDefaultState().withProperty(PART, EnumPartType.BOTTOM).withProperty(FACING,
						enumfacing);
	}

	public int getMetaFromState(IBlockState state) {
		int i = state.getValue(FACING).getHorizontalIndex();

		if (state.getValue(PART) == EnumPartType.TOP) {
			i |= 8;
		}

		return i;
	}

	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
		if (state.getValue(PART) == EnumPartType.TOP) {
			if (worldIn.getBlockState(pos.down()).getBlock() != this) {
				worldIn.setBlockToAir(pos);
			}
		} else if (worldIn.getBlockState(pos.up()).getBlock() != this) {
			worldIn.setBlockToAir(pos);

			if (!worldIn.isRemote) {
				this.dropBlockAsItem(worldIn, pos, state, 0);
			}
		}
	}
	
	public enum EnumPartType implements IStringSerializable
    {
        TOP,
        BOTTOM;

        public String getName()
        {
            return this.toString().toLowerCase();
        }
    }

}
