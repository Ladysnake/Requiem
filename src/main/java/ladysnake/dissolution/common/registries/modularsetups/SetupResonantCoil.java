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
import ladysnake.dissolution.common.init.ModSounds;
import ladysnake.dissolution.common.inventory.InputItemHandler;
import ladysnake.dissolution.common.items.AlchemyModuleTypes;
import ladysnake.dissolution.common.items.ItemAlchemyModule;
import ladysnake.dissolution.common.items.ItemSoulInAJar;
import ladysnake.dissolution.common.tileentities.TileEntityModularMachine;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class SetupResonantCoil extends ModularMachineSetup {
	
	private static final ImmutableSet<ItemAlchemyModule.AlchemyModule> setup = ImmutableSet.of(
			new ItemAlchemyModule.AlchemyModule(AlchemyModuleTypes.RESONANT_GENERATOR, 1),
			new ItemAlchemyModule.AlchemyModule(AlchemyModuleTypes.ALCHEMICAL_INTERFACE_BOTTOM, 1));
	
	public static final ExecutorService THREADPOOL = Executors.newCachedThreadPool();
	
	public SetupResonantCoil() {
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
		private TileEntityModularMachine tile;
		private InputItemHandler itemInput;
		private int time;
		
		Instance(TileEntityModularMachine tile) {
			nodes = new HashSet<>();
			this.tile = tile;
			updateScheduled = true;
			itemInput = new InputItemHandler(ModItems.SOUL_IN_A_FLASK);
		}

		@Override
		public void init() {
			tile.setPowerConsumption(PowerConsumption.GENERATOR);
		}

		@Override
		public void onTick() {
			if(time++ % 20 == 0 && this.itemInput.getStackInSlot(0).isEmpty()) {
				TileEntity te = tile.getWorld().getTileEntity(tile.getPos().up(2));
				if(te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.DOWN)) {
					IItemHandler inv = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.DOWN);
					if(inv != null)
						for(int i = 0; i < inv.getSlots(); i++)
							if (inv.getStackInSlot(i).getItem() instanceof ItemSoulInAJar)
								this.itemInput.insertItem(0, inv.extractItem(i, 1, false), false);
				}
			}
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
							((IPowerConductor) tile.getWorld().getBlockState(pos).getBlock()).setPowered(tile.getWorld(), pos, false);
						} catch (ClassCastException ignored) {}
					});
					nodes = newNodes;
					for(BlockPos pos : nodes) {
						((IPowerConductor) tile.getWorld().getBlockState(pos).getBlock()).setPowered(tile.getWorld(), pos, isRunning());
					}
					scheduledTask = null;
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			}
			if(this.isRunning() && !tile.getWorld().isRemote) {
				if(time % 276 == 0)
					tile.getWorld().playSound(null, tile.getPos().getX() + 0.5D, tile.getPos().getY() + 1, tile.getPos().getZ() + 0.5D,
							ModSounds.resonant_coil.sound(), SoundCategory.BLOCKS, 1.0F, 1.0F);
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
				if (stack.getItem() == ModItems.SOUL_IN_A_FLASK) {
					stack.setCount(this.itemInput.insertItem(0, stack.copy(), false).getCount());
				} else if (stack.isEmpty()) {
					playerIn.addItemStackToInventory(this.itemInput.extractItem(0, 64, false));
				}
			}
		}
		
		@Override
		public void onRemoval() {
			tile.getWorld().spawnEntity(new EntityItem(tile.getWorld(), tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ(), this.itemInput.extractItem(0, 64, false)));
			tile.setPowerConsumption(PowerConsumption.NONE);
			for(BlockPos pos : nodes) {
				try {
					((IPowerConductor) tile.getWorld().getBlockState(pos).getBlock()).setPowered(tile.getWorld(), pos, false);
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
			return detectNetwork(tile.getWorld(), tile.getPos(), new LinkedList<>(), 0, new HashSet<>());
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
		public ResourceLocation getPlugModel(EnumFacing facing, EnumPartType part, ResourceLocation defaultModel) {
			return part == EnumPartType.TOP ? null : defaultModel;
		}

		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing, EnumPartType part) {
			return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && part == EnumPartType.TOP && facing == EnumFacing.UP;
		}

		@Override
		public <T> T getCapability(Capability<T> capability, EnumFacing facing, EnumPartType part) {
			if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && part == EnumPartType.TOP && facing == EnumFacing.UP)
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
