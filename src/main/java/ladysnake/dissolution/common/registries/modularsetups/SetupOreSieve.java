package ladysnake.dissolution.common.registries.modularsetups;

import com.google.common.collect.ImmutableSet;
import ladysnake.dissolution.api.EssentiaStack;
import ladysnake.dissolution.api.EssentiaTypes;
import ladysnake.dissolution.api.IEssentiaHandler;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.blocks.alchemysystem.BlockCasing;
import ladysnake.dissolution.common.blocks.alchemysystem.BlockCasing.EnumPartType;
import ladysnake.dissolution.common.blocks.alchemysystem.IPowerConductor;
import ladysnake.dissolution.common.capabilities.CapabilityEssentiaHandler;
import ladysnake.dissolution.common.init.ModBlocks;
import ladysnake.dissolution.common.items.AlchemyModuleTypes;
import ladysnake.dissolution.common.items.ItemAlchemyModule;
import ladysnake.dissolution.common.tileentities.TileEntityModularMachine;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;

import java.util.HashMap;
import java.util.Map;

public class SetupOreSieve extends ModularMachineSetup {

	private final Map<Item, Item> conversions;
	private final Map<Item, EssentiaStack> essentiaConversions;
	private static final ImmutableSet<ItemAlchemyModule> setup = ImmutableSet.of(
			ItemAlchemyModule.getFromType(AlchemyModuleTypes.CONTAINER, 1),
			ItemAlchemyModule.getFromType(AlchemyModuleTypes.ALCHEMY_INTERFACE_BOTTOM, 1),
			ItemAlchemyModule.getFromType(AlchemyModuleTypes.MINERAL_FILTER, 1));

	public SetupOreSieve() {
		this.setRegistryName(new ResourceLocation(Reference.MOD_ID, "ore_sieve"));
		this.conversions = new HashMap<>();
		this.essentiaConversions = new HashMap<>();
		addConversion(Blocks.CLAY, ModBlocks.DEPLETED_CLAY, new EssentiaStack(EssentiaTypes.SALIS, 1));
		addConversion(Blocks.COAL_BLOCK, ModBlocks.DEPLETED_COAL, new EssentiaStack(EssentiaTypes.SULPURIS, 1));
		addConversion(Blocks.MAGMA, ModBlocks.DEPLETED_MAGMA, new EssentiaStack(EssentiaTypes.CINNABARIS, 1));
	}

	private void addConversion(Block input, Block output, EssentiaStack essentiaOutput) {
		this.conversions.put(Item.getItemFromBlock(input), Item.getItemFromBlock(output));
		this.essentiaConversions.put(Item.getItemFromBlock(input), essentiaOutput);
	}

	@Override
	public ISetupInstance getInstance(TileEntityModularMachine te) {
		return new Instance(te);
	}

	@Override
	public ImmutableSet<ItemAlchemyModule> getSetup() {
		return setup;
	}

	@SuppressWarnings("ConstantConditions")
	public class Instance implements ISetupInstance {

		TileEntityModularMachine tile;
		private InputItemHandler input;
		private OutputItemHandler depletedOutput;
		private IEssentiaHandler essentiaOutput;
		private int progressTicks;
		private int transferCooldown;
		private int processingTime;

		Instance(TileEntityModularMachine te) {
			super();
			this.tile = te;
			this.input = new InputItemHandler(Blocks.CLAY, Blocks.COAL_BLOCK, Blocks.MAGMA);
			this.essentiaOutput = new CapabilityEssentiaHandler.DefaultEssentiaHandler(100);
			this.depletedOutput = new OutputItemHandler();
			this.processingTime = AlchemyModuleTypes.MINERAL_FILTER.maxTier / this.tile.getInstalledModules().stream()
					.filter(mod -> mod.getType() == AlchemyModuleTypes.MINERAL_FILTER).findAny().get().getTier();
		}

		@Override
		public void onTick() {
			if (tile.isPowered() && !tile.getWorld().isRemote) {
				if (!input.getStackInSlot(0).isEmpty() && progressTicks++ % (processingTime * 20) == 0) {
					
					// processes a material
					if(depletedOutput.getStackInSlot(0).getCount() < depletedOutput.getSlotLimit(0)
						&& !essentiaOutput.isFull()) {
						ItemStack inputStack = input.extractItem(0, 1, false);
						
						depletedOutput.insertItemInternal(0, new ItemStack(conversions.get(inputStack.getItem())), false);
						depletedOutput.insertItemInternal(0,
								tile.tryOutput(depletedOutput.extractItem(0, 10, false), EnumPartType.BOTTOM), false);
						
						EssentiaStack result = essentiaConversions.get(inputStack.getItem());
						this.essentiaOutput.insert(result);
					}
				}
				
				if(transferCooldown++ % 10 == 0) {
					// attempts to auto-output essentia
					for(Map.Entry<EnumFacing, TileEntity> side : tile.getAdjacentTEs(EnumPartType.BOTTOM,
							(face, te) -> te.hasCapability(CapabilityEssentiaHandler.CAPABILITY_ESSENTIA, face.getOpposite())).entrySet()) {
						this.essentiaOutput.flow(side.getValue().getCapability(CapabilityEssentiaHandler.CAPABILITY_ESSENTIA,
								side.getKey().getOpposite()));
					}
				}
			}
		}

		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing, EnumPartType part) {
			if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
				return true;
			else if (capability == CapabilityEssentiaHandler.CAPABILITY_ESSENTIA)
				return (part == BlockCasing.EnumPartType.BOTTOM);
			return false;
		}

		@Override
		public <T> T getCapability(Capability<T> capability, EnumFacing facing, EnumPartType part) {
			if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
				if (part == BlockCasing.EnumPartType.TOP)
					return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(input);
				else if (part == BlockCasing.EnumPartType.BOTTOM)
					return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(depletedOutput);
			} else if (capability == CapabilityEssentiaHandler.CAPABILITY_ESSENTIA) {
				if (part == BlockCasing.EnumPartType.BOTTOM)
					return CapabilityEssentiaHandler.CAPABILITY_ESSENTIA.cast(essentiaOutput);
			}
			return null;
		}

		@Override
		public boolean isPlugAttached(EnumFacing facing, EnumPartType part) {
			TileEntity neighbour = tile.getWorld().getTileEntity((part == EnumPartType.BOTTOM ? tile.getPos() : tile.getPos().up()).offset(facing));
			return (part == EnumPartType.TOP && (tile.getWorld().getBlockState(tile.getPos().up().offset(facing)).getBlock() instanceof IPowerConductor
					|| (neighbour != null && neighbour.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite()))))
				|| (part == EnumPartType.BOTTOM && (neighbour != null && (neighbour.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite())
					|| neighbour.hasCapability(CapabilityEssentiaHandler.CAPABILITY_ESSENTIA, facing.getOpposite()))));
		}

		@Override
		public void readFromNBT(NBTTagCompound compound) {
			CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.getStorage().readNBT(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, input, EnumFacing.EAST, compound.getTag("input"));
			CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.getStorage().readNBT(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, depletedOutput, EnumFacing.EAST, compound.getTag("output"));
			CapabilityEssentiaHandler.CAPABILITY_ESSENTIA.getStorage().readNBT(CapabilityEssentiaHandler.CAPABILITY_ESSENTIA, essentiaOutput, EnumFacing.NORTH, compound.getTag("essentiaOutput"));
		}
		
		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound compound) {
			compound.setTag("input", CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.getStorage().writeNBT(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, this.input, EnumFacing.EAST));
			compound.setTag("output", CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.getStorage().writeNBT(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, this.depletedOutput, EnumFacing.WEST));
			compound.setTag("essentiaOutput", CapabilityEssentiaHandler.CAPABILITY_ESSENTIA.getStorage().writeNBT(CapabilityEssentiaHandler.CAPABILITY_ESSENTIA, essentiaOutput, EnumFacing.NORTH));
			return compound;
		}
	}

}
