package ladysnake.dissolution.common.registries.modularsetups;

import com.google.common.collect.ImmutableSet;
import ladysnake.dissolution.api.DistillateStack;
import ladysnake.dissolution.api.DistillateTypes;
import ladysnake.dissolution.api.IDistillateHandler;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.blocks.alchemysystem.BlockCasing;
import ladysnake.dissolution.common.blocks.alchemysystem.BlockCasing.EnumPartType;
import ladysnake.dissolution.common.blocks.alchemysystem.IPowerConductor;
import ladysnake.dissolution.common.capabilities.CapabilityDistillateHandler;
import ladysnake.dissolution.common.init.ModBlocks;
import ladysnake.dissolution.common.inventory.InputItemHandler;
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
	private final Map<Item, DistillateStack> essentiaConversions;
	private static final ImmutableSet<ItemAlchemyModule.AlchemyModule> setup = ImmutableSet.of(
			new ItemAlchemyModule.AlchemyModule(AlchemyModuleTypes.ALCHEMICAL_INTERFACE_TOP, 1),
			new ItemAlchemyModule.AlchemyModule(AlchemyModuleTypes.MINERAL_FILTER, 1),
			new ItemAlchemyModule.AlchemyModule(AlchemyModuleTypes.ALCHEMICAL_INTERFACE_BOTTOM, 1));

	public SetupOreSieve() {
		this.setRegistryName(new ResourceLocation(Reference.MOD_ID, "ore_sieve"));
		this.conversions = new HashMap<>();
		this.essentiaConversions = new HashMap<>();
		addConversion(Blocks.CLAY, ModBlocks.DEPLETED_CLAY, new DistillateStack(DistillateTypes.SALIS, 1));
		addConversion(Blocks.COAL_BLOCK, ModBlocks.DEPLETED_COAL, new DistillateStack(DistillateTypes.SULPURIS, 1));
		addConversion(Blocks.MAGMA, ModBlocks.DEPLETED_MAGMA, new DistillateStack(DistillateTypes.CINNABARIS, 1));
	}

	private void addConversion(Block input, Block output, DistillateStack essentiaOutput) {
		this.conversions.put(Item.getItemFromBlock(input), Item.getItemFromBlock(output));
		this.essentiaConversions.put(Item.getItemFromBlock(input), essentiaOutput);
	}

	@Override
	public ISetupInstance getInstance(TileEntityModularMachine te) {
		return new Instance(te);
	}

	@Override
	public ImmutableSet<ItemAlchemyModule.AlchemyModule> getSetup() {
		return setup;
	}

	public class Instance implements ISetupInstance {

		TileEntityModularMachine tile;
		private InputItemHandler input;
		private OutputItemHandler depletedOutput;
		private IDistillateHandler essentiaOutput;
		private int progressTicks;
		private int transferCooldown;
		private int processingTime;

		Instance(TileEntityModularMachine te) {
			super();
			this.tile = te;
			this.input = new InputItemHandler(Blocks.CLAY, Blocks.COAL_BLOCK, Blocks.MAGMA);
			this.essentiaOutput = new CapabilityDistillateHandler.DefaultDistillateHandler(100);
			this.depletedOutput = new OutputItemHandler();
			this.processingTime = AlchemyModuleTypes.MINERAL_FILTER.maxTier / this.tile.getInstalledModules().stream()
					.filter(mod -> mod.getType() == AlchemyModuleTypes.MINERAL_FILTER).findAny()
					.map(ItemAlchemyModule.AlchemyModule::getTier).orElse(1);
		}

		@Override
		public void init() {
			tile.setPowerConsumption(IPowerConductor.IMachine.PowerConsumption.CONSUMER);
		}

		@Override
		public void onRemoval() {
			tile.setPowerConsumption(IPowerConductor.IMachine.PowerConsumption.NONE);
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
						
						DistillateStack result = essentiaConversions.get(inputStack.getItem());
						this.essentiaOutput.insert(result);
					}
				}
				
				if(transferCooldown++ % 10 == 0) {
					// attempts to auto-output essentia
					for(Map.Entry<EnumFacing, TileEntity> side : tile.getAdjacentTEs(EnumPartType.BOTTOM,
							(face, te) -> te.hasCapability(CapabilityDistillateHandler.CAPABILITY_ESSENTIA, face.getOpposite())).entrySet()) {
						this.essentiaOutput.flow(side.getValue().getCapability(CapabilityDistillateHandler.CAPABILITY_ESSENTIA,
								side.getKey().getOpposite()));
					}
				}
			}
		}

		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing, EnumPartType part) {
			if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
				return true;
			else if (capability == CapabilityDistillateHandler.CAPABILITY_ESSENTIA)
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
			} else if (capability == CapabilityDistillateHandler.CAPABILITY_ESSENTIA) {
				if (part == BlockCasing.EnumPartType.BOTTOM)
					return CapabilityDistillateHandler.CAPABILITY_ESSENTIA.cast(essentiaOutput);
			}
			return null;
		}

        @Override
		public void readFromNBT(NBTTagCompound compound) {
			CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.getStorage().readNBT(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, input, EnumFacing.EAST, compound.getTag("input"));
			CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.getStorage().readNBT(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, depletedOutput, EnumFacing.EAST, compound.getTag("output"));
			CapabilityDistillateHandler.CAPABILITY_ESSENTIA.getStorage().readNBT(CapabilityDistillateHandler.CAPABILITY_ESSENTIA, essentiaOutput, EnumFacing.NORTH, compound.getTag("essentiaOutput"));
		}
		
		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound compound) {
			compound.setTag("input", CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.getStorage().writeNBT(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, this.input, EnumFacing.EAST));
			compound.setTag("output", CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.getStorage().writeNBT(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, this.depletedOutput, EnumFacing.WEST));
			compound.setTag("essentiaOutput", CapabilityDistillateHandler.CAPABILITY_ESSENTIA.getStorage().writeNBT(CapabilityDistillateHandler.CAPABILITY_ESSENTIA, essentiaOutput, EnumFacing.NORTH));
			return compound;
		}
	}

}
