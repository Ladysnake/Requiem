package ladysnake.dissolution.common.registries.modularsetups;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableSet;

import ladysnake.dissolution.api.EssentiaStack;
import ladysnake.dissolution.api.EssentiaTypes;
import ladysnake.dissolution.api.IEssentiaHandler;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.blocks.alchemysystem.BlockCasing;
import ladysnake.dissolution.common.blocks.alchemysystem.BlockCasing.EnumPartType;
import ladysnake.dissolution.common.blocks.alchemysystem.IPowerConductor;
import ladysnake.dissolution.common.capabilities.CapabilityEssentiaHandler;
import ladysnake.dissolution.common.init.ModItems;
import ladysnake.dissolution.common.items.AlchemyModule;
import ladysnake.dissolution.common.items.ItemAlchemyModule;
import ladysnake.dissolution.common.tileentities.TileEntityModularMachine;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;

public class SetupCrystallizer extends ModularMachineSetup {

	private static final ImmutableSet<ItemAlchemyModule> setup = ImmutableSet.of(
			ItemAlchemyModule.getFromType(AlchemyModule.ALCHEMY_INTERFACE, 1),
			ItemAlchemyModule.getFromType(AlchemyModule.CRYSTALLIZER, 1),
			ItemAlchemyModule.getFromType(AlchemyModule.CONTAINER, 1));
	private final Map<EssentiaStack, Item> conversions;

	public SetupCrystallizer() {
		this.setRegistryName(new ResourceLocation(Reference.MOD_ID, "crystallizer"));
		this.conversions = new HashMap<>();
		addConversion(EssentiaTypes.CINNABARIS, ModItems.CINNABAR);
		addConversion(EssentiaTypes.SULPURIS, ModItems.SULFUR);
		addConversion(EssentiaTypes.SALIS, ModItems.HALITE);
	}
	
	private void addConversion(EssentiaTypes essentia, Item out) {
		conversions.put(new EssentiaStack(essentia, 9), out);
	}

	@Override
	public ImmutableSet<ItemAlchemyModule> getSetup() {
		return setup;
	}

	@Override
	public ISetupInstance getInstance(TileEntityModularMachine te) {
		return new Instance(te);
	}

	public class Instance implements ISetupInstance {

		private TileEntityModularMachine tile;
		private IEssentiaHandler essentiaInput;
		private OutputItemHandler oreOutput;
		private int progressTicks;

		public Instance(TileEntityModularMachine tile) {
			super();
			this.tile = tile;
			if(tile.hasWorld())
				tile.getWorld().markBlockRangeForRenderUpdate(tile.getPos().add(1, 0, 1), tile.getPos().add(-1, 0, -1));
			this.essentiaInput = new CapabilityEssentiaHandler.DefaultEssentiaHandler(99);
			this.essentiaInput.setSuction(10, EssentiaTypes.UNTYPED);
			this.oreOutput = new OutputItemHandler();
		}

		@Override
		public void onTick() {
			if (!tile.getWorld().isRemote && this.progressTicks++ % 20 == 0) {
				if (this.oreOutput.getStackInSlot(0).getCount() < this.oreOutput.getSlotLimit(0)) {
					if(this.essentiaInput.readContent().getCount() >= 9) {
						EssentiaStack in = this.essentiaInput.extract(9);
						this.oreOutput.insertItemInternal(0, new ItemStack(conversions.get(in)), false);
					}
				}
				this.oreOutput.insertItemInternal(0,
						tile.tryOutput(this.oreOutput.extractItem(0, 10, false), EnumFacing.WEST),
						false);
			}
		}

		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing, EnumPartType part) {
			if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
				return part == BlockCasing.EnumPartType.BOTTOM && facing != EnumFacing.EAST;
			}
			return capability == CapabilityEssentiaHandler.CAPABILITY_ESSENTIA && part == EnumPartType.TOP;
		}

		@Override
		public <T> T getCapability(Capability<T> capability, EnumFacing facing, EnumPartType part) {
			if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
				if (part == BlockCasing.EnumPartType.BOTTOM && facing != EnumFacing.EAST)
					return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this.oreOutput);
			}
			if (capability == CapabilityEssentiaHandler.CAPABILITY_ESSENTIA) {
				if (part == BlockCasing.EnumPartType.TOP)
					return CapabilityEssentiaHandler.CAPABILITY_ESSENTIA.cast(this.essentiaInput);
			}
			return null;
		}
		
		@Override
		public void readFromNBT(NBTTagCompound compound) {
			CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.getStorage().readNBT(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, this.oreOutput, EnumFacing.WEST, compound.getTag("output"));
			CapabilityEssentiaHandler.CAPABILITY_ESSENTIA.getStorage().readNBT(CapabilityEssentiaHandler.CAPABILITY_ESSENTIA, this.essentiaInput, EnumFacing.NORTH, compound.getTag("essentiaInput"));
		}
		
		@SuppressWarnings("ConstantConditions")
		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound compound) {
			compound.setTag("output", CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.getStorage().writeNBT(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, this.oreOutput, EnumFacing.WEST));
			compound.setTag("essentiaInput", CapabilityEssentiaHandler.CAPABILITY_ESSENTIA.getStorage().writeNBT(CapabilityEssentiaHandler.CAPABILITY_ESSENTIA, essentiaInput, EnumFacing.NORTH));
			return compound;
		}

		@Override
		public boolean isPlugAttached(EnumFacing facing, BlockCasing.EnumPartType part) {
			TileEntity neighbour = tile.getWorld().getTileEntity((part == EnumPartType.BOTTOM ? tile.getPos() : tile.getPos().up()).offset(facing));
			return part == EnumPartType.TOP &&
					(neighbour != null && neighbour.hasCapability(CapabilityEssentiaHandler.CAPABILITY_ESSENTIA, facing.getOpposite())
					|| tile.getWorld().getBlockState(tile.getPos().up().offset(facing)).getBlock() instanceof IPowerConductor)
					|| part == EnumPartType.BOTTOM && tile.adjustFaceIn(facing) != EnumFacing.EAST && neighbour != null && neighbour.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite());
		}
	}

}
