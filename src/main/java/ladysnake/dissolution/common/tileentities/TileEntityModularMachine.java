package ladysnake.dissolution.common.tileentities;

import com.google.common.collect.ImmutableSet;
import ladysnake.dissolution.common.DissolutionConfig;
import ladysnake.dissolution.common.blocks.alchemysystem.AbstractPowerConductor;
import ladysnake.dissolution.common.blocks.alchemysystem.BlockCasing;
import ladysnake.dissolution.common.blocks.alchemysystem.IPowerConductor;
import ladysnake.dissolution.common.blocks.alchemysystem.IPowerConductor.IMachine.PowerConsumption;
import ladysnake.dissolution.common.init.ModModularSetups;
import ladysnake.dissolution.common.items.AlchemyModule;
import ladysnake.dissolution.common.items.ItemAlchemyModule;
import ladysnake.dissolution.common.registries.modularsetups.ISetupInstance;
import ladysnake.dissolution.common.registries.modularsetups.SetupPowerGenerator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class TileEntityModularMachine extends TileEntity implements ITickable {
	
	private Set<ItemAlchemyModule> installedModules;
	private ISetupInstance currentSetup = null;
	private PowerConsumption powerConsumption;
	private boolean powered = false;
	private boolean running = false;

	public TileEntityModularMachine() {
		this.installedModules = new HashSet<>();
	}

	@Override
	public void update() {
		if(currentSetup != null)
			currentSetup.onTick();
	}
	
	/**
	 * Adjusts the given facing to take the block's rotation into account
	 * Should be used by setups trying to interact with the world
	 * @param face a facing relative to the machine's rotation
	 * @return the corresponding in-world face using the block's rotation
	 */
	public EnumFacing adjustFaceOut(EnumFacing face) {
		if(face.getAxis() == EnumFacing.Axis.Y)
			return face;
		switch(world.getBlockState(pos).getValue(BlockCasing.FACING)) {
		case WEST: face = face.rotateY();
		case SOUTH: face = face.rotateY();
		case EAST: face = face.rotateY();
		default:
		}
		return face;
	}
	
	/**
	 * Does the opposite operation to {@link #adjustFaceOut}
	 * @param face an absolute facing given by the world
	 * @return the corresponding relative facing
	 */
	public EnumFacing adjustFaceIn(EnumFacing face) {
		if(face.getAxis() == EnumFacing.Axis.Y)
			return face;
		switch(world.getBlockState(pos).getValue(BlockCasing.FACING)) {
		case WEST: face = face.rotateYCCW();
		case SOUTH: face = face.rotateYCCW();
		case EAST: face = face.rotateYCCW();
		default:
		}
		return face;
	}

	/**
	 * Attempts to output an ItemStack into a container
	 * @param stack the stack to output through this machine
	 * @param face the face interacting with the world, not taking into account the casing's rotation
	 * @return what could not be inserted
	 */
	public ItemStack tryOutput(ItemStack stack, EnumFacing face) {
		face = this.adjustFaceOut(face);
		TileEntity te = this.getWorld().getTileEntity(getPos().offset(face));
		if (te != null) {
			IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
					face.getOpposite());
			if (handler != null) {
				for (int i = 0; i < handler.getSlots(); i++) {
					if ((stack = handler.insertItem(i, stack, false)).isEmpty())
						return ItemStack.EMPTY;
				}
			}
		}
		return stack;
	}
	
	public boolean addModule(ItemAlchemyModule module) {
		boolean added = installedModules.stream().allMatch(mod -> (mod.getType().isCompatible(module.getType())))
				&& installedModules.add(module);
		if(added) {
			this.verifySetup();
			this.world.markBlockRangeForRenderUpdate(this.pos, this.pos);
			SetupPowerGenerator.THREADPOOL.submit(() -> AbstractPowerConductor.updatePowerCore(world, pos));
			this.markDirty();
		}
		return added;
	}
	
	public ItemStack removeModule() {
		Iterator<ItemAlchemyModule> iterator = installedModules.iterator();
		ItemStack ret = ItemStack.EMPTY;
		if(iterator.hasNext()) {
			ret = new ItemStack(iterator.next());
			iterator.remove();
			if(this.currentSetup != null)
				this.currentSetup.onRemoval();
			this.verifySetup();
			this.world.markBlockRangeForRenderUpdate(this.pos, this.pos);
			this.markDirty();
		}
		return ret;
	}
	
	public void interact(EntityPlayer playerIn, EnumHand hand, BlockCasing.EnumPartType part, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(this.currentSetup != null)
			this.currentSetup.onInteract(playerIn, hand, part, adjustFaceIn(facing), hitX, hitY, hitZ);
	}

	public boolean isPlugAttached(EnumFacing facing, BlockCasing.EnumPartType part) {
		return ((this.currentSetup != null) && this.currentSetup.isPlugAttached(facing, part));
	}
	
	public Set<ItemAlchemyModule> getInstalledModules() {
		return ImmutableSet.copyOf(this.installedModules);
	}
	
	public ISetupInstance getCurrentSetup() {
		return this.currentSetup;
	}
	
	public void dropContent() {
		if(this.currentSetup != null)
			this.currentSetup.onRemoval();
		if(!this.world.isRemote)
			for (ItemAlchemyModule module : installedModules) {
				world.spawnEntity(new EntityItem(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), new ItemStack(module)));
			}
		this.installedModules.clear();
	}
	
	private void verifySetup() {
		Set<ItemAlchemyModule> modules = getInstalledModules();
		System.out.println(ModModularSetups.REGISTRY.getValues());
		currentSetup = ModModularSetups.REGISTRY.getValues().stream().peek(System.out::println).filter(setup -> setup.isValidSetup(modules)).map(setup -> setup.getInstance(this)).findAny().orElse(null);
	}
	
	public PowerConsumption getPowerConsumption() {
		return powerConsumption;
	}

	public void setPowerConsumption(PowerConsumption powerConsumption) {
		this.powerConsumption = powerConsumption;
	}
	
	public boolean isPowered() {
		return powered;
	}

	public void setPowered(boolean powered) {
		this.powered = powered;
	}

	public boolean isRunning() {
		return running;
	}

	/**
	 * 
	 * @param running Whether the machine and its components should appear activated
	 */
	public void setRunning(boolean running) {
		this.running = running;
	}
	
	@Override
	public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
		return hasCapability(capability, facing, BlockCasing.EnumPartType.BOTTOM);
	}
	
	public boolean hasCapability(Capability<?> capability, EnumFacing facing, BlockCasing.EnumPartType part) {
		return (this.currentSetup != null && this.currentSetup.hasCapability(capability, adjustFaceIn(facing), part)) || super.hasCapability(capability, facing);
	}
	
	@Override
	public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
		return getCapability(capability, facing, BlockCasing.EnumPartType.BOTTOM);
	}
	
	public <T> T getCapability(Capability<T> capability, EnumFacing facing, BlockCasing.EnumPartType part) {
		T setupCap = null;
		if(this.currentSetup != null)
			setupCap = this.currentSetup.getCapability(capability, adjustFaceIn(facing), part);
		return setupCap == null ? (super.getCapability(capability, facing)) : setupCap;
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		NBTTagCompound nbt = new NBTTagCompound();
		this.writeToNBTBasic(nbt);

		return new SPacketUpdateTileEntity(getPos(), 0, nbt);

	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		this.readFromNBTBasic(pkt.getNbtCompound());
	}

	@Override
	public @Nonnull NBTTagCompound getUpdateTag() {
		NBTTagCompound nbt =  super.getUpdateTag();
		writeToNBTBasic(nbt);
		return nbt;
	}

	@Override
	public void handleUpdateTag(@Nonnull NBTTagCompound tag) {
		readFromNBTBasic(tag);
	}

	private void readFromNBTBasic(NBTTagCompound compound) {
		super.readFromNBT(compound);
		NBTTagList modules = compound.getTagList("modules", 10);
		for (NBTBase mod : modules) {
			System.out.println(AlchemyModule.valueOf(((NBTTagCompound)mod).getString("type")) + " " + ((NBTTagCompound)mod).getInteger("tier"));
			this.installedModules.add(ItemAlchemyModule.getFromType(
					AlchemyModule.valueOf(((NBTTagCompound)mod).getString("type")),
					((NBTTagCompound)mod).getInteger("tier")));
		}
		verifySetup();
		this.setPowered(compound.getBoolean("powered"));
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		readFromNBTBasic(compound);
		if(this.currentSetup != null)
			this.currentSetup.readFromNBT((NBTTagCompound) compound.getTag("currentSetup"));
	}

	private	void writeToNBTBasic(NBTTagCompound compound) {
		super.writeToNBT(compound);
		NBTTagList modules = new NBTTagList();
		for (ItemAlchemyModule mod : installedModules) {
			if(mod != null) {
				NBTTagCompound comp = new NBTTagCompound();
				comp.setString("type", mod.getType().name());
				comp.setInteger("tier", mod.getTier());
				modules.appendTag(comp);
			}
		}
		compound.setTag("modules", modules);
		compound.setBoolean("powered", isPowered());
	}
	
	@Override
	public @Nonnull NBTTagCompound writeToNBT(NBTTagCompound compound) {
		writeToNBTBasic(compound);
		if(this.currentSetup != null)
			compound.setTag("currentSetup", currentSetup.writeToNBT(new NBTTagCompound()));
		return compound;
	}
	
	@Override
	public boolean shouldRefresh(World world, BlockPos pos, @Nonnull IBlockState oldState, @Nonnull IBlockState newState) {
		return oldState.getBlock() != newState.getBlock() || oldState.withProperty(IPowerConductor.POWERED, newState.getValue(IPowerConductor.POWERED)) != newState;
	}
	
}
