package ladysnake.dissolution.common.registries.modularsetups;

import com.google.common.collect.ImmutableSet;

import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.blocks.alchemysystem.BlockCasing;
import ladysnake.dissolution.common.blocks.alchemysystem.BlockCasing.EnumPartType;
import ladysnake.dissolution.common.init.ModItems;
import ladysnake.dissolution.common.items.AlchemyModule;
import ladysnake.dissolution.common.items.ItemAlchemyModule;
import ladysnake.dissolution.common.tileentities.TileEntityModularMachine;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class SetupOreSieve extends ModularMachineSetup {
	
	private static final ImmutableSet<ItemAlchemyModule> setup = ImmutableSet.of(
			ItemAlchemyModule.getFromType(AlchemyModule.CONTAINER, 1), 
			ItemAlchemyModule.getFromType(AlchemyModule.INTERFACE, 1), 
			ItemAlchemyModule.getFromType(AlchemyModule.FILTER, 1));
	
	public SetupOreSieve() {
		this.setRegistryName(new ResourceLocation(Reference.MOD_ID, "ore_sieve"));
	}
	
	@Override
	public ISetupInstance getInstance(TileEntityModularMachine te) {
		return new Instance(te);
	}
	
	@Override
	public ImmutableSet<ItemAlchemyModule> getSetup() {
		return setup;
	}
	
	public static class Instance implements ISetupInstance {
		
		TileEntityModularMachine te;
		private IItemHandler input, oreOutput, depletedOutput;
		private int advancement;
		private int processingTime;
		
		public Instance(TileEntityModularMachine te) {
			super();
			this.te = te;
			this.input = new ItemStackHandler();
			this.oreOutput = new ItemStackHandler();
			this.depletedOutput = new ItemStackHandler();
			this.processingTime = AlchemyModule.FILTER.maxTier / this.te.getInstalledModules().stream().filter(mod -> mod.getType() == AlchemyModule.FILTER).findAny().get().getTier();
			this.input.insertItem(0, new ItemStack(Blocks.BEDROCK, 64), false);
		}

		@Override
		public void onTick() {
			if(te.isPowered() && !te.getWorld().isRemote && !input.getStackInSlot(0).isEmpty()) {
				if(advancement++ % (processingTime * 20) == 0) {
					input.extractItem(0, 1, false);
					depletedOutput.insertItem(0, new ItemStack(Blocks.SAND), false);
					depletedOutput.insertItem(0, tryOutput(depletedOutput.extractItem(0, 10, false), te.getPos(), EnumFacing.WEST), false);
						
					oreOutput.insertItem(0, new ItemStack(ModItems.CINNABAR), false);
					oreOutput.insertItem(0, tryOutput(oreOutput.extractItem(0, 10, false), te.getPos(), EnumFacing.NORTH), false);
				}
			}
		}
		
		private ItemStack tryOutput (ItemStack stack, BlockPos pos, EnumFacing face) {
			TileEntity te = this.te.getWorld().getTileEntity(pos.offset(face));
			if(te != null) {
				IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, face.getOpposite());
				if(handler != null) {
					for (int i = 0; i < handler.getSlots(); i++) {
						if((stack = handler.insertItem(i, stack, false)).isEmpty())
							return ItemStack.EMPTY;
					}
				}
			}
			return stack;
		}
	
		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing, EnumPartType part) {
			if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
				return (part == BlockCasing.EnumPartType.TOP && facing == EnumFacing.EAST) || 
						(part == BlockCasing.EnumPartType.BOTTOM && (facing == EnumFacing.WEST || facing == EnumFacing.NORTH));
			return false;
		}

		@Override
		public <T> T getCapability(Capability<T> capability, EnumFacing facing, EnumPartType part) {
			if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
				if(part == BlockCasing.EnumPartType.TOP && facing == EnumFacing.EAST)
					return (T) input;
				else if(part == BlockCasing.EnumPartType.BOTTOM && facing == EnumFacing.WEST)
					return (T) depletedOutput;
				else if(part == BlockCasing.EnumPartType.BOTTOM && facing == EnumFacing.NORTH)
					return (T) oreOutput;
			}
			return null;
		}
	}

}
