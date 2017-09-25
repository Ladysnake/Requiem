package ladysnake.dissolution.common.tileentities;

import com.google.common.collect.ImmutableSet;
import ladysnake.dissolution.common.blocks.alchemysystem.AbstractPowerConductor;
import ladysnake.dissolution.common.blocks.alchemysystem.BlockCasing;
import ladysnake.dissolution.common.blocks.alchemysystem.IPowerConductor;
import ladysnake.dissolution.common.blocks.alchemysystem.IPowerConductor.IMachine.PowerConsumption;
import ladysnake.dissolution.common.init.ModItems;
import ladysnake.dissolution.common.init.ModModularSetups;
import ladysnake.dissolution.common.items.AlchemyModuleTypes;
import ladysnake.dissolution.common.items.ItemAlchemyModule;
import ladysnake.dissolution.common.items.ItemPlug;
import ladysnake.dissolution.common.registries.modularsetups.ISetupInstance;
import ladysnake.dissolution.common.registries.modularsetups.SetupPowerGenerator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
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
import java.util.*;
import java.util.function.BiPredicate;

public class TileEntityModularMachine extends TileEntity implements ITickable {
	
	private Set<ItemAlchemyModule.AlchemyModule> installedModules;
	private Map<BlockCasing.EnumPartType, Map<EnumFacing, Boolean>> plugs;
	private ISetupInstance currentSetup = null;
	private PowerConsumption powerConsumption;
	private boolean powered = false;
	private boolean running = false;

	public TileEntityModularMachine() {
		this.installedModules = new HashSet<>();
		this.plugs = new HashMap<>();
		for(BlockCasing.EnumPartType part : BlockCasing.EnumPartType.values()) {
			Map<EnumFacing, Boolean> facings = new HashMap<>();
			for(EnumFacing facing : EnumFacing.values()) {
				facings.put(facing, false);
			}
			this.plugs.put(part, facings);
		}
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
	 * Attempts to output an ItemStack into one or more containers
	 * @param stack the stack to output through this machine
	 * @return what could not be inserted
	 */
	public ItemStack tryOutput(ItemStack stack, BlockCasing.EnumPartType part) {
		for(EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
			stack = tryOutput(stack, part, facing);
			if(stack.isEmpty())
				return ItemStack.EMPTY;
		}
		return stack;
	}

	/**
	 * Attempts to output an ItemStack into a container
	 * @param stack the stack to output through this machine
	 * @param face the face interacting with the world, not taking into account the casing's rotation
	 * @return what could not be inserted
	 */
	public ItemStack tryOutput(ItemStack stack, BlockCasing.EnumPartType part, EnumFacing face) {
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

	public Map<EnumFacing, TileEntity> getAdjacentTEs(BlockCasing.EnumPartType part, BiPredicate<EnumFacing, TileEntity> condition) {
		Map<EnumFacing, TileEntity> ret = new HashMap<>();
		BlockPos pos = part == BlockCasing.EnumPartType.BOTTOM ? this.pos : this.pos.up();
		for(EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
			TileEntity te = world.getTileEntity(pos.offset(facing));
			if(te != null && condition.test(facing, te))
				ret.put(facing, te);
		}
		return ret;
	}
	
	private boolean addModule(ItemAlchemyModule.AlchemyModule module) {
		System.out.println("module added : " + module);
		boolean added = installedModules.stream().allMatch(mod -> (mod.getType().isCompatible(module.getType())))
				&& installedModules.add(module);
		if(added) {
			this.verifySetup();
			SetupPowerGenerator.THREADPOOL.submit(() -> AbstractPowerConductor.updatePowerCore(world, pos));
			this.world.markBlockRangeForRenderUpdate(this.pos, this.pos);
			this.markDirty();
		}
		return added;
	}
	
	private ItemStack removeModule() {
		Iterator<ItemAlchemyModule.AlchemyModule> iterator = installedModules.iterator();
		ItemStack ret = ItemStack.EMPTY;
		if(iterator.hasNext()) {
			ret = new ItemStack(iterator.next().toItem());
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
		ItemStack stack = playerIn.getHeldItem(hand);
		if(stack.getItem() instanceof ItemAlchemyModule) {
			if(addModule(((ItemAlchemyModule) stack.getItem()).toModule(part)) && !playerIn.isCreative()) {
				stack.shrink(1);
			}
		} else if(stack.isEmpty() && playerIn.isSneaking()) {
			playerIn.addItemStackToInventory(removeModule());
		} else if(stack.getItem() instanceof ItemPlug) {
			Boolean b = this.plugs.get(part).put(facing, true);
			if(b == null || !b) {
				this.world.markBlockRangeForRenderUpdate(this.pos, this.pos);
				this.markDirty();
				if(!playerIn.isCreative())
					stack.shrink(1);
			}
		}else if(this.currentSetup != null)
			this.currentSetup.onInteract(playerIn, hand, part, adjustFaceIn(facing), hitX, hitY, hitZ);
	}

	public boolean isPlugAttached(EnumFacing facing, BlockCasing.EnumPartType part) {
		return this.plugs.get(part).get(facing);
		// return ((this.currentSetup != null) && this.currentSetup.isPlugAttached(facing, part));
	}
	
	public Set<ItemAlchemyModule.AlchemyModule> getInstalledModules() {
		return ImmutableSet.copyOf(this.installedModules);
	}
	
	public ISetupInstance getCurrentSetup() {
		return this.currentSetup;
	}
	
	public void dropContent() {
		if(this.currentSetup != null)
			this.currentSetup.onRemoval();
		if(!this.world.isRemote) {
			for (ItemAlchemyModule.AlchemyModule module : installedModules) {
				world.spawnEntity(new EntityItem(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(),
						new ItemStack(module.toItem())));
			}
			world.spawnEntity(new EntityItem(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(),
					new ItemStack(ModItems.PLUG, (int) this.plugs.values().stream().flatMap(e -> e.values().stream())
							.filter(e -> e).count())));
		}
		this.installedModules.clear();
	}
	
	private void verifySetup() {
		Set<ItemAlchemyModule.AlchemyModule> modules = getInstalledModules();
		currentSetup = ModModularSetups.REGISTRY.getValues().stream().filter(setup -> setup.isValidSetup(modules)).map(setup -> setup.getInstance(this)).findAny().orElse(null);
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
			System.out.println(((NBTTagCompound)mod).getString("type"));
			AlchemyModuleTypes type = AlchemyModuleTypes.valueOf(((NBTTagCompound)mod).getString("type"));
			if(type != null)
				this.installedModules.add(new ItemAlchemyModule.AlchemyModule(
						type,
						((NBTTagCompound)mod).getInteger("tier")));
		}
		NBTTagCompound plugsNBT = compound.getCompoundTag("plugs");
		this.plugs.forEach((key, value) -> value.replaceAll((f, b) -> plugsNBT.getCompoundTag(key.name()).getBoolean(f.name())));
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
		for (ItemAlchemyModule.AlchemyModule mod : installedModules) {
			if(mod != null) {
				NBTTagCompound comp = new NBTTagCompound();
				comp.setString("type", mod.getType().name());
				comp.setInteger("tier", mod.getTier());
				modules.appendTag(comp);
			}
		}
		compound.setTag("modules", modules);
		NBTTagCompound plugs = new NBTTagCompound();
		for(Map.Entry<BlockCasing.EnumPartType, Map<EnumFacing, Boolean>> part : this.plugs.entrySet()) {
			NBTTagCompound facings = new NBTTagCompound();
			for(Map.Entry<EnumFacing, Boolean> side : part.getValue().entrySet()) {
				facings.setBoolean(side.getKey().name(), side.getValue());
			}
			plugs.setTag(part.getKey().name(), facings);
		}
		compound.setTag("plugs", plugs);
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
