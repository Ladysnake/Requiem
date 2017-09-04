package ladysnake.dissolution.common.tileentities;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import ladysnake.dissolution.common.blocks.alchemysystem.AlchemyModules;
import ladysnake.dissolution.common.items.ItemAlchemyModule;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

public class TileEntityModularMachine extends TileEntity {
	
	private Set<ItemAlchemyModule> installedModules;
	
	public TileEntityModularMachine() {
		this.installedModules = new HashSet<>();
	}
	
	public boolean addModule(ItemAlchemyModule module) {
		System.out.println(this.world.isRemote + " " + module.getTier());
		this.world.markBlockRangeForRenderUpdate(this.pos, this.pos);
		this.markDirty();
		return installedModules.add(module);
	}
	
	public Set<ItemAlchemyModule> getInstalledModules() {
		return ImmutableSet.copyOf(this.installedModules);
	}
	
	public void dropContent() {
		for (ItemAlchemyModule module : installedModules) {
			world.spawnEntity(new EntityItem(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), new ItemStack(module)));
		}
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

	//calls readFromNbt by default. no need to add anything in here
	@Override
	public void handleUpdateTag(NBTTagCompound tag) {
		super.handleUpdateTag(tag);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		System.out.println(compound);
		NBTTagList modules = compound.getTagList("modules", 10);
		for (NBTBase mod : modules) {
			System.out.println(AlchemyModules.valueOf(((NBTTagCompound)mod).getString("type")) + " " + ((NBTTagCompound)mod).getInteger("tier"));
			this.installedModules.add(ItemAlchemyModule.getFromType(
					AlchemyModules.valueOf(((NBTTagCompound)mod).getString("type")), 
					((NBTTagCompound)mod).getInteger("tier")));
		}
		System.out.println(this.installedModules);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		NBTTagList modules = new NBTTagList();
		for (ItemAlchemyModule mod : installedModules) {
			System.out.println(mod);
			if(mod != null) {
				NBTTagCompound comp = new NBTTagCompound();
				comp.setString("type", mod.getType().name());
				comp.setInteger("tier", mod.getTier());
				modules.appendTag(comp);
			}
		}
		compound.setTag("modules", modules);
		return compound;
	}
	
}
