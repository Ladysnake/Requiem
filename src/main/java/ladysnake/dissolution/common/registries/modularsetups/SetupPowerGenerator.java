package ladysnake.dissolution.common.registries.modularsetups;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

import com.google.common.collect.ImmutableSet;

import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.blocks.alchemysystem.BlockCasing.EnumPartType;
import ladysnake.dissolution.common.blocks.alchemysystem.IPowerConductor;
import ladysnake.dissolution.common.blocks.alchemysystem.IPowerConductor.IMachine.PowerConsumption;
import ladysnake.dissolution.common.init.ModItems;
import ladysnake.dissolution.common.inventory.InputItemHandler;
import ladysnake.dissolution.common.items.AlchemyModuleTypes;
import ladysnake.dissolution.common.items.ItemAlchemyModule;
import ladysnake.dissolution.common.tileentities.TileEntityModularMachine;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;

public class SetupPowerGenerator extends ModularMachineSetup {
	
	private static final ImmutableSet<ItemAlchemyModule.AlchemyModule> setup = ImmutableSet.of(
			new ItemAlchemyModule.AlchemyModule(AlchemyModuleTypes.RESONANT_GENERATOR, 1),
			new ItemAlchemyModule.AlchemyModule(AlchemyModuleTypes.ALCHEMICAL_INTERFACE_TOP, 1));
	
	public static final ExecutorService THREADPOOL = Executors.newCachedThreadPool();
	
	public SetupPowerGenerator() {
		this.setRegistryName(new ResourceLocation(Reference.MOD_ID, "power_generator"));
	}
	
	@Override
	public ISetupInstance getInstance(TileEntityModularMachine te) {
		return new Instance(te);
	}

	@Override
	public ImmutableSet<ItemAlchemyModule.AlchemyModule> getSetup() {
		return setup;
	}	
	
	public static class Instance implements ISetupInstance {
		
		private Set<BlockPos> nodes;
		private boolean updateScheduled;
		private boolean running;
		private Future<Set<BlockPos>> scheduledTask;
		private TileEntityModularMachine te;
		private InputItemHandler itemInput;
		
		Instance(TileEntityModularMachine te) {
			nodes = new HashSet<>();
			this.te = te;
			te.setPowerConsumption(PowerConsumption.GENERATOR);
			updateScheduled = true;
			itemInput = new InputItemHandler(ModItems.SOUL_IN_A_BOTTLE);
		}
		
		@Override
		public void onTick() {
			if(updateScheduled || (this.isRunning() ^ !this.itemInput.getStackInSlot(0).isEmpty())) {
				this.setRunning(!itemInput.getStackInSlot(0).isEmpty());
				scheduledTask = THREADPOOL.submit((Callable<Set<BlockPos>>) this::detectNetwork);
				updateScheduled = false;
			}
			if(scheduledTask != null && scheduledTask.isDone()) {
				try {
					Set<BlockPos> newNodes = scheduledTask.get();
					nodes.stream().filter(pos -> !newNodes.contains(pos)).forEach(pos -> {
						try {
							((IPowerConductor)te.getWorld().getBlockState(pos).getBlock()).setPowered(te.getWorld(), pos, false);
						} catch (ClassCastException ignored) {}
					});
					nodes = newNodes;
					for(BlockPos pos : nodes) {
						((IPowerConductor)te.getWorld().getBlockState(pos).getBlock()).setPowered(te.getWorld(), pos, isRunning());
					}
					scheduledTask = null;
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			}
		}
		
		public synchronized void scheduleUpdate() {
			this.updateScheduled = true;
		}
		
		@Override
		public void onInteract(EntityPlayer playerIn, EnumHand hand, EnumPartType part,
				EnumFacing facing, float hitX, float hitY, float hitZ) {
			if(!playerIn.world.isRemote) {
				ItemStack stack = playerIn.getHeldItem(hand);
				if (stack.getItem() == ModItems.SOUL_IN_A_BOTTLE) {
					stack.setCount(this.itemInput.insertItem(0, stack.copy(), false).getCount());
				} else if (stack.isEmpty()) {
					playerIn.addItemStackToInventory(this.itemInput.extractItem(0, 64, false));
				}
			}
		}
		
		@Override
		public void onRemoval() {
			te.getWorld().spawnEntity(new EntityItem(te.getWorld(), te.getPos().getX(), te.getPos().getY(), te.getPos().getZ(), this.itemInput.extractItem(0, 64, false)));
			te.setPowerConsumption(PowerConsumption.NONE);
			for(BlockPos pos : nodes) {
				try {
					((IPowerConductor)te.getWorld().getBlockState(pos).getBlock()).setPowered(te.getWorld(), pos, false);
				} catch (ClassCastException ignored) {}
			}
		}
		
		void setRunning(boolean b) {
			running = b;
		}
		
		boolean isRunning() {
			return running;
		}
		
		Set<BlockPos> detectNetwork() {
			return detectNetwork(te.getWorld(), te.getPos(), new LinkedList<>(), 0, new HashSet<>());
		}
		
		private Set<BlockPos> detectNetwork(World world, BlockPos pos, List<BlockPos> searchedBlocks, int i, Set<BlockPos> nodes) {
			if(searchedBlocks.contains(pos))
				return nodes;
			
			searchedBlocks.add(pos);
			if(++i > 100 || !(world.getBlockState(pos).getBlock() instanceof IPowerConductor)) 
				return nodes;
			
			Block block = world.getBlockState(pos).getBlock();
			
			if(!((IPowerConductor)block).isConductive(world, pos))
				return nodes;
	
			nodes.add(pos);
			
			for(EnumFacing face : EnumFacing.values())
				detectNetwork(world, pos.offset(face), searchedBlocks, i, nodes);
			
			return nodes;
		}

        @Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing, EnumPartType part) {
			return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && part == EnumPartType.TOP;
		}

		@Override
		public <T> T getCapability(Capability<T> capability, EnumFacing facing, EnumPartType part) {
			if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && part == EnumPartType.TOP)
				return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemInput);
			return null;
		}
		
		@Override
		public void readFromNBT(NBTTagCompound compound) {
			this.itemInput.deserializeNBT(compound.getCompoundTag("itemInput"));
		}
		
		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound compound) {
			compound.setTag("itemInput", this.itemInput.serializeNBT());
			return compound;
		}
	}

}
