package ladysnake.dissolution.common.tileentities;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import ladysnake.dissolution.api.ModularMachineSetup;
import ladysnake.dissolution.common.blocks.alchemysystem.AlchemyModule;
import ladysnake.dissolution.common.blocks.alchemysystem.BlockCasing;
import ladysnake.dissolution.common.blocks.alchemysystem.IPowerConductor;
import ladysnake.dissolution.common.blocks.alchemysystem.IPowerConductor.IMachine.PowerConsumption;
import ladysnake.dissolution.common.init.ModModularSetups;
import ladysnake.dissolution.common.items.ItemAlchemyModule;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
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

public class TileEntityModularMachine extends TileEntity implements ITickable {
	
	private Set<ItemAlchemyModule> installedModules;
	private Optional<ModularMachineSetup> currentSetup = Optional.empty();
	private PowerConsumption powerConsumption;
	private boolean powered = false;
	private boolean running = false;
	private boolean keepInventory = false;
	
	public TileEntityModularMachine() {
		this.installedModules = new HashSet<>();
	}

	@Override
	public void update() {
		currentSetup.ifPresent(set -> set.onTick(this));
	}
	
	public boolean addModule(ItemAlchemyModule module) {
		boolean added = installedModules.stream().allMatch(mod -> (mod.getType().isCompatible(module.getType())))
				&& installedModules.add(module);
		if(added) {
			this.verifySetup();
			this.world.markBlockRangeForRenderUpdate(this.pos, this.pos);
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
			this.currentSetup.ifPresent(setup -> setup.onRemoval(this));
			this.verifySetup();
			this.world.markBlockRangeForRenderUpdate(this.pos, this.pos);
			this.markDirty();
		}
		return ret;
	}
	
	public void interact(EntityPlayer playerIn, EnumHand hand, BlockCasing.EnumPartType part, EnumFacing facing, float hitX, float hitY, float hitZ) {
		this.currentSetup.ifPresent(setup -> setup.onInteract(this, playerIn, hand, part, facing, hitX, hitY, hitZ));
	}
	
	public void onScheduledUpdate() {
		this.currentSetup.ifPresent(setup -> setup.onScheduledUpdate(this));
	}
	
	public Set<ItemAlchemyModule> getInstalledModules() {
		return ImmutableSet.copyOf(this.installedModules);
	}
	
	public void dropContent() {
		this.currentSetup.ifPresent(setup -> setup.onRemoval(this));
		if(!this.world.isRemote)
			for (ItemAlchemyModule module : installedModules) {
				world.spawnEntity(new EntityItem(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), new ItemStack(module)));
			}
		this.installedModules.clear();
	}
	
	protected void verifySetup() {
		Set<ItemAlchemyModule> modules = getInstalledModules();
		System.out.println(ModModularSetups.REGISTRY.getValues());
		currentSetup = ModModularSetups.REGISTRY.getValues().stream().peek(System.out::println).filter(setup -> setup.isValidSetup(modules)).findAny();
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
		System.out.println(this.pos + " : powered: " + powered);
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
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return this.currentSetup.isPresent();
		return super.hasCapability(capability, facing);
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		NBTTagCompound nbt = new NBTTagCompound();
		this.writeToNBT(nbt);

		return new SPacketUpdateTileEntity(getPos(), 0, nbt);

	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		this.readFromNBT(pkt.getNbtCompound());
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound nbt =  super.getUpdateTag();
		writeToNBT(nbt);
		return nbt;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		NBTTagList modules = compound.getTagList("modules", 10);
		for (NBTBase mod : modules) {
			System.out.println(AlchemyModule.valueOf(((NBTTagCompound)mod).getString("type")) + " " + ((NBTTagCompound)mod).getInteger("tier"));
			this.installedModules.add(ItemAlchemyModule.getFromType(
					AlchemyModule.valueOf(((NBTTagCompound)mod).getString("type")), 
					((NBTTagCompound)mod).getInteger("tier")));
		}
		verifySetup();
		this.currentSetup.ifPresent(setup -> setup.readFromNBT(this, (NBTTagCompound) compound.getTag("currentSetup")));
		this.setPowered(compound.getBoolean("powered"));
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
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
		this.currentSetup.ifPresent(setup -> compound.setTag("currentSetup", setup.writeToNBT(this, new NBTTagCompound())));
		compound.setBoolean("powered", isPowered());
		return compound;
	}
	
	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
		return oldState.getBlock() != newState.getBlock() || oldState.withProperty(IPowerConductor.POWERED, newState.getValue(IPowerConductor.POWERED)) != newState;
	}
	
}
