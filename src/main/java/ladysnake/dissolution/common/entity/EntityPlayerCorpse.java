package ladysnake.dissolution.common.entity;

import java.util.UUID;

import com.google.common.base.Optional;

import ladysnake.dissolution.client.handlers.EventHandlerClient;
import ladysnake.dissolution.common.Dissolution;
import ladysnake.dissolution.common.DissolutionConfig;
import ladysnake.dissolution.common.blocks.ISoulInteractable;
import ladysnake.dissolution.common.capabilities.IIncorporealHandler;
import ladysnake.dissolution.common.capabilities.ISoulHandler;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.capabilities.CapabilitySoulHandler;
import ladysnake.dissolution.common.entity.ai.EntityAIMinionAttack;
import ladysnake.dissolution.common.entity.minion.AbstractMinion;
import ladysnake.dissolution.common.handlers.LivingDeathHandler;
import ladysnake.dissolution.common.init.ModItems;
import ladysnake.dissolution.common.inventory.GuiProxy;
import ladysnake.dissolution.common.inventory.InventoryPlayerCorpse;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

public class EntityPlayerCorpse extends AbstractMinion implements ISoulInteractable {
	
	private static DataParameter<Optional<UUID>> PLAYER = EntityDataManager.<Optional<UUID>>createKey(EntityPlayerCorpse.class, DataSerializers.OPTIONAL_UNIQUE_ID);
	private static DataParameter<Boolean> DECAY = EntityDataManager.<Boolean>createKey(EntityPlayerCorpse.class, DataSerializers.BOOLEAN);
	protected InventoryPlayerCorpse inventory;
	protected boolean decaying;
	
	public EntityPlayerCorpse(World worldIn) {
		super(worldIn);
		inventory = new InventoryPlayerCorpse(this);
		decaying = !DissolutionConfig.bodiesDespawn;
	}
	
	@Override
	protected boolean processInteract(EntityPlayer player, EnumHand hand) {
		final IIncorporealHandler handler = CapabilityIncorporealHandler.getHandler(player);

		if(handler.isIncorporeal() && (!this.isDecaying() || DissolutionConfig.wowRespawn)) {
			LivingDeathHandler.transferEquipment(this, player);
			this.onDeath(DamageSource.GENERIC);
			this.setDead();
			player.setPositionAndRotation(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
			player.cameraPitch = 90;
			player.prevCameraPitch = 90;
			if(player.world.isRemote) {
				player.eyeHeight = 0.5f;
				EventHandlerClient.cameraAnimation = 20;
			}
			player.setHealth(4f);
			handler.setIncorporeal(false);
		} else if (!player.world.isRemote && player.getHeldItem(hand).getItem() != ModItems.EYE_OF_THE_UNDEAD) {
			player.openGui(Dissolution.instance, GuiProxy.PLAYER_CORPSE, world, this.getPosition().getX(), this.getPosition().getY(), this.getPosition().getZ());
		}
		
		return true;
	}
	
	@Override
	public void setCorpse(boolean isCorpse) {
		super.setCorpse(isCorpse);
		this.setDecaying(isCorpse);
	}
	
	public InventoryPlayerCorpse getInventory() {
		return inventory;
	}

	public void setInventory(InventoryPlayerCorpse inventory) {
		this.inventory = inventory;
	}
	
	public boolean isDecaying() {
		return this.getDataManager().get(DECAY);
	}
	
	public void setDecaying(boolean decaying) {
		this.getDataManager().set(DECAY, decaying);
	}
	
	@Override
	public void setCustomNameTag(String name) {
		super.setCustomNameTag(name);
	}
	
	@Override
	public void setSwingingArms(boolean swingingArms) {}

	@Override
	protected void initEntityAI() {
		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(2, new EntityAIMinionAttack(this, 1.0D, false));
		this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 1.0D));
		this.tasks.addTask(7, new EntityAIWanderAvoidWater(this, 1.0D));
		this.applyEntityAI();
	}
	
	@Override
	protected void updateMinion() {
		if(this.isDecaying())
			super.updateMinion();
	}
	
	@Override
	public int getMaxTimeRemaining() {
		return this.isDecaying() ? (DissolutionConfig.wowRespawn ? 6000 : 50) : -1;
	}
	
	@Override
	protected void handleSunExposition() {}
	
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
		this.getDataManager().register(DECAY, true);
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
        this.soulInventoryProvider.deserializeNBT((NBTTagCompound) compound.getTag("SoulInventory"));
        setPlayer(compound.getUniqueId("player"));
        setDecaying(compound.getBoolean("decaying"));
	}
	
	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setTag("SoulInventory", this.soulInventoryProvider.serializeNBT());
        compound.setTag("Inventory", this.inventory.writeToNBT(new NBTTagList()));
        compound.setUniqueId("player", getPlayer());
        compound.setBoolean("decaying", isDecaying());
	}

	@Override
	public String toString() {
		return "EntityPlayerCorpse [inventory=" + inventory + ",\n player=" + this.getPlayer() + ",\n soulInventory=" + this.getSoulHandlerCapability() + "]";
	}
	
	private CapabilitySoulHandler.Provider soulInventoryProvider = new CapabilitySoulHandler.Provider();
	
	public ISoulHandler getSoulHandlerCapability() {
		return getCapability(CapabilitySoulHandler.CAPABILITY_SOUL_INVENTORY, EnumFacing.DOWN);
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		if(capability == CapabilitySoulHandler.CAPABILITY_SOUL_INVENTORY)
			return soulInventoryProvider.hasCapability(capability, facing);
		return super.hasCapability(capability, facing);
	}
	
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if(capability == CapabilitySoulHandler.CAPABILITY_SOUL_INVENTORY)
			return soulInventoryProvider.getCapability(capability, facing);
		return super.getCapability(capability, facing);
	}
	
}
