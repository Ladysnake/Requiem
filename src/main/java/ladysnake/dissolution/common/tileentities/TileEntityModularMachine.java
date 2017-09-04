package ladysnake.dissolution.common.tileentities;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import ladysnake.dissolution.common.blocks.alchemysystem.AlchemyModules;
import ladysnake.dissolution.common.items.ItemAlchemyModule;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;

public class TileEntityModularMachine extends TileEntity {
	
	private Set<ItemAlchemyModule> installedModules;
	
	public TileEntityModularMachine() {
		this.installedModules = new HashSet<>();
	}
	
	public boolean addModule(ItemAlchemyModule module) {
		System.out.println(this.world.isRemote);
		this.world.markBlockRangeForRenderUpdate(this.pos, this.pos);
		this.markDirty();
		return installedModules.add(module);
	}
	
	public Set<ItemAlchemyModule> getInstalledModules() {
		return ImmutableSet.copyOf(this.installedModules);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		System.out.println(compound);
		NBTTagList modules = compound.getTagList("modules", 10);
		modules.forEach(mod ->
			this.installedModules.add(ItemAlchemyModule.getFromType(
					AlchemyModules.valueOf(((NBTTagCompound)mod).getString("type")), 
					((NBTTagCompound)mod).getInteger("tier"))));
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		NBTTagList modules = new NBTTagList();
		for (ItemAlchemyModule mod : installedModules) {
			System.out.println(mod);
			NBTTagCompound comp = new NBTTagCompound();
			comp.setString("type", mod.getType().name());
			comp.setInteger("tier", mod.getTier());
			modules.appendTag(comp);
		}
		compound.setTag("modules", modules);
		return compound;
	}
	
}
