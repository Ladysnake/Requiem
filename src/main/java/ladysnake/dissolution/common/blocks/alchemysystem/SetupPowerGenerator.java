package ladysnake.dissolution.common.blocks.alchemysystem;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import ladysnake.dissolution.api.ModularMachineSetup;
import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.blocks.alchemysystem.BlockCasing.EnumPartType;
import ladysnake.dissolution.common.blocks.alchemysystem.IPowerConductor.IMachine.PowerConsumption;
import ladysnake.dissolution.common.items.ItemAlchemyModule;
import ladysnake.dissolution.common.tileentities.TileEntityModularMachine;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SetupPowerGenerator extends ModularMachineSetup {
	
	public static final ImmutableSet<ItemAlchemyModule> setup = ImmutableSet.of(
			ItemAlchemyModule.getFromType(AlchemyModule.GENERATOR, 1),
			ItemAlchemyModule.getFromType(AlchemyModule.INTERFACE, 1));
	
	public SetupPowerGenerator() {
		this.setRegistryName(new ResourceLocation(Reference.MOD_ID, "power_generator"));
	}
	
	@Override
	public void init(TileEntityModularMachine te) {
		nodes.put(te, new HashSet<>());
		detectNetwork(te);
	}

	@Override
	public void onTick(TileEntityModularMachine te) {
		te.setPowerConsumption(PowerConsumption.GENERATOR);
	}
	
	@Override
	public void onInteract(TileEntityModularMachine te, EntityPlayer playerIn, EnumHand hand, EnumPartType part,
			EnumFacing facing, float hitX, float hitY, float hitZ) {
		super.onInteract(te, playerIn, hand, part, facing, hitX, hitY, hitZ);
		this.setEnabled(te, !isEnabled(te));
		updateNetwork(te);
	}
	
	@Override
	public void onRemoval(TileEntityModularMachine te) {
		super.onRemoval(te);
		updateNetwork(te);
	}
	
	@Override
	public void onScheduledUpdate(TileEntityModularMachine te) {
		updateNetwork(te);
	}

	@Override
	public ImmutableSet<ItemAlchemyModule> getSetup() {
		return setup;
	}	
	
	private Map<TileEntityModularMachine, Set<BlockPos>> nodes = new HashMap<>();
	
	public void setEnabled(TileEntityModularMachine te, boolean b) {
		te.setRunning(b);
	}
	
	public boolean isEnabled(TileEntityModularMachine te) {
		return te.isRunning();
	}
	
	public void updateNetwork(TileEntityModularMachine te) {
		Set<BlockPos> old = new HashSet<>(nodes.get(te));
		detectNetwork(te);
		System.out.println(old);
		System.out.println(nodes.get(te));
		old.stream().filter(pos -> !nodes.get(te).contains(pos) && te.getWorld().getBlockState(pos).getBlock() instanceof IPowerConductor)
		.forEach(pos -> ((IPowerConductor)te.getWorld().getBlockState(pos).getBlock()).setPowered(te.getWorld(), pos, false));
	}
	
	public void detectNetwork(TileEntityModularMachine te) {
		nodes.get(te).clear();
		detectNetwork(te, te.getWorld(), te.getPos(), new LinkedList<>(), 0);
	}
	
	private void detectNetwork(TileEntityModularMachine te, World world, BlockPos pos, List<BlockPos> searchedBlocks, int i) {
		if(searchedBlocks.contains(pos))
			return;
		
		searchedBlocks.add(pos);
		if(++i > 100 || !(world.getBlockState(pos).getBlock() instanceof IPowerConductor)) 
			return;
		
		Block block = world.getBlockState(pos).getBlock();
		
		if(!((IPowerConductor)block).isConductive(world, pos))
			return;

		((IPowerConductor)block).setPowered(world, pos, isEnabled(te));
		nodes.get(te).add(pos);
		
		for(EnumFacing face : EnumFacing.values())
			detectNetwork(te, world, pos.offset(face), searchedBlocks, i);
	}

}
