package ladysnake.dissolution.common.entity;

import java.util.UUID;

import com.google.common.base.Optional;

import ladysnake.dissolution.common.blocks.ISoulInteractable;
import ladysnake.dissolution.common.capabilities.IIncorporealHandler;
import ladysnake.dissolution.common.capabilities.IncorporealDataHandler;
import ladysnake.dissolution.common.inventory.InventoryPlayerCorpse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class EntityPlayerCorpse extends EntityMinion implements ISoulInteractable {
	
	private static DataParameter<Optional<UUID>> PLAYER = EntityDataManager.<Optional<UUID>>createKey(EntityPlayerCorpse.class, DataSerializers.OPTIONAL_UNIQUE_ID);
	protected InventoryPlayerCorpse inventory;
	
	public EntityPlayerCorpse(World worldIn) {
		super(worldIn);
		inventory = new InventoryPlayerCorpse("Corpse");
	}
	
	@Override
	protected boolean processInteract(EntityPlayer player, EnumHand hand) {
		// TODO Auto-generated method stub
		final IIncorporealHandler handler = IncorporealDataHandler.getHandler(player);
		if(!handler.isIncorporeal() || player.world.isRemote)
			return false;
		this.onDeath(DamageSource.GENERIC);
		this.setDead();
		handler.setIncorporeal(false, player);
		return true;
	}
	
	public InventoryPlayerCorpse getInventory() {
		return inventory;
	}

	public void setInventory(InventoryPlayerCorpse inventory) {
		this.inventory = inventory;
	}
	
	@Override
	public void setCustomNameTag(String name) {
		super.setCustomNameTag(name);
		this.inventory.setName(name);
	}
	
	@Override
	public void setSwingingArms(boolean swingingArms) {
		
	}

	@Override
	protected void initEntityAI() {
		
	}
	
	@Override
	protected void update() {
		// TODO something I guess ?
	}
	
	public UUID getPlayer() {
		return this.getDataManager().get(PLAYER).orNull();
	}
	
	public void setPlayer(UUID id) {
		this.getDataManager().set(PLAYER, Optional.<UUID>of(id));
	}
	
	@Override
	protected void entityInit() {
		super.entityInit();
		this.getDataManager().register(PLAYER, Optional.absent());
	}
	
	@Override
	protected void dropLoot(boolean wasRecentlyHit, int lootingModifier, DamageSource cause) {
		super.dropLoot(wasRecentlyHit, lootingModifier, cause);
		if(this.inventory != null)
			this.inventory.dropAllItems(this);
	}
	
	@Override
	protected void dropEquipment(boolean wasRecentlyHit, int lootingModifier) {
		this.getEquipmentAndArmor().forEach(stack -> this.entityDropItem(stack, 0.5f));
    }
	
	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
        this.inventory.readFromNBT(compound.getTagList("Inventory", 10));
        this.inventory.setName(this.getName());
        setPlayer(compound.getUniqueId("player"));
	}
	
	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
        compound.setTag("Inventory", this.inventory.writeToNBT(new NBTTagList()));
        compound.setUniqueId("player", getPlayer());
	}

	@Override
	public String toString() {
		return "EntityPlayerCorpse [inventory=" + inventory + ", player=" + this.getPlayer() + "]";
	}
	
	
}
