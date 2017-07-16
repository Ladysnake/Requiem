package ladysnake.dissolution.common.entity.soul;

import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.base.Optional;

import ladysnake.dissolution.common.blocks.ISoulInteractable;
import ladysnake.dissolution.common.entity.minion.AbstractMinion;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;

public class AbstractSoul extends EntityFlying implements IEntityOwnable, ISoulInteractable {
	
	private static final DataParameter<Optional<UUID>> OWNER_UNIQUE_ID = EntityDataManager.<Optional<UUID>>createKey(AbstractMinion.class, DataSerializers.OPTIONAL_UNIQUE_ID);

	public AbstractSoul(World worldIn) {
		super(worldIn);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
        this.dataManager.register(OWNER_UNIQUE_ID, Optional.absent());
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		this.setOwnerId(compound.getUniqueId("OwnerUUID"));
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		if (this.getOwnerId() != null)
        {
            compound.setUniqueId("OwnerUUID", getOwnerId());
        }
	}

	@Nullable
	@Override
    public UUID getOwnerId()
    {
        return (UUID)((Optional)this.dataManager.get(OWNER_UNIQUE_ID)).orNull();
    }

    public void setOwnerId(@Nullable UUID uuid)
    {
        this.dataManager.set(OWNER_UNIQUE_ID, Optional.fromNullable(uuid));
    }

    @Nullable
    @Override
    public EntityLivingBase getOwner()
    {
        try
        {
            UUID uuid = this.getOwnerId();
            return uuid == null ? null : this.world.getPlayerEntityByUUID(uuid);
        }
        catch (IllegalArgumentException e)
        {
            return null;
        }
    }

}
