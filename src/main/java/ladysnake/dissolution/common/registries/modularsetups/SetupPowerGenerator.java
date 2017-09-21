package ladysnake.dissolution.common.registries.modularsetups;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.google.common.collect.ImmutableSet;

import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.blocks.alchemysystem.BlockCasing.EnumPartType;
import ladysnake.dissolution.common.blocks.alchemysystem.IPowerConductor;
import ladysnake.dissolution.common.blocks.alchemysystem.IPowerConductor.IMachine.PowerConsumption;
import ladysnake.dissolution.common.init.ModItems;
import ladysnake.dissolution.common.items.AlchemyModule;
import ladysnake.dissolution.common.items.ItemAlchemyModule;
import ladysnake.dissolution.common.tileentities.TileEntityModularMachine;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

public class SetupPowerGenerator extends ModularMachineSetup {
	
	public static final ImmutableSet<ItemAlchemyModule> setup = ImmutableSet.of(
			ItemAlchemyModule.getFromType(AlchemyModule.GENERATOR, 1),
			ItemAlchemyModule.getFromType(AlchemyModule.MATERIAL_INTERFACE, 1));
	
	public static final ExecutorService THREADPOOL = Executors.newCachedThreadPool();
	
	public SetupPowerGenerator() {
		this.setRegistryName(new ResourceLocation(Reference.MOD_ID, "power_generator"));
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
		
		private Set<BlockPos> nodes;
		private boolean updateScheduled;
		private Future<Set<BlockPos>> scheduledTask;
		private TileEntityModularMachine te;
		private InputItemHandler itemInput;
		
		public Instance(TileEntityModularMachine te) {
			nodes = new HashSet<>();
			this.te = te;
			te.setPowerConsumption(PowerConsumption.GENERATOR);
			updateScheduled = true;
			itemInput = new InputItemHandler(ModItems.SOUL_IN_A_BOTTLE);
		}
		
		@Override
		public void onTick() {
			if(updateScheduled) {
				scheduledTask = THREADPOOL.submit(() -> detectNetwork());
				updateScheduled = false;
			}
			if(scheduledTask != null && scheduledTask.isDone()) {
				try {
					Set<BlockPos> newNodes = scheduledTask.get();
					nodes.stream().filter(pos -> !newNodes.contains(pos)).forEach(pos -> {
						try {
							((IPowerConductor)te.getWorld().getBlockState(pos).getBlock()).setPowered(te.getWorld(), pos, false);
						} catch (ClassCastException e) {}
					});
					nodes = newNodes;
					for(BlockPos pos : nodes)
						((IPowerConductor)te.getWorld().getBlockState(pos).getBlock()).setPowered(te.getWorld(), pos, isEnabled());
					scheduledTask = null;
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			}
		}
		
		public synchronized void scheduleUpdate() {
			System.out.println("update scheduled");
			this.updateScheduled = true;
		}
		
		@Override
		public void onInteract(EntityPlayer playerIn, EnumHand hand, EnumPartType part,
				EnumFacing facing, float hitX, float hitY, float hitZ) {
			this.setEnabled(!isEnabled());
			this.scheduleUpdate();
		}
		
		@Override
		public void onRemoval() {
			te.setPowerConsumption(PowerConsumption.NONE);
			for(BlockPos pos : nodes) {
				try {
					((IPowerConductor)te.getWorld().getBlockState(pos).getBlock()).setPowered(te.getWorld(), pos, false);
				} catch (ClassCastException e) {}
			}
		}
		
		public void setEnabled(boolean b) {
			te.setRunning(b);
		}
		
		public boolean isEnabled() {
			return te.isRunning();
		}
		
		public Set<BlockPos> detectNetwork() {
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
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public <T> T getCapability(Capability<T> capability, EnumFacing facing, EnumPartType part) {
			// TODO Auto-generated method stub
			return null;
		}
	}

}
