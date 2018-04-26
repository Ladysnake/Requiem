package ladysnake.dissolution.common.entity.souls;

import ladysnake.dissolution.common.Dissolution;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;

@Optional.Interface(iface = "elucent.albedo.lighting.ILightProvider", modid = "albedo", striprefs = true)
public class EntitySplinterSoul extends EntityFleetingSoul implements IEntityOwnable {
    private UUID ownerUUID;

    public EntitySplinterSoul(World world) {
        super(world);
    }

    @Override
    public boolean canBePickupBy(EntityLivingBase entity) {
        return Objects.equals(entity.getUniqueID(), this.getOwnerId());
    }

    @Override
    protected Entity selectTarget() {
        return getOwner();
    }

    @Nullable
    @Override
    public UUID getOwnerId() {
        return ownerUUID;
    }

    public void setOwnerId(UUID id) {
        this.ownerUUID = id;
    }

    @Nullable
    @Override
    public Entity getOwner() {
        try {
            UUID uuid = this.getOwnerId();
            return uuid == null ? null : this.world.getPlayerEntityByUUID(uuid);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);

        if (this.getOwnerId() != null) {
            compound.setString("OwnerUUID", this.getOwnerId().toString());
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);

        if (compound.hasKey("OwnerUUID", 8)) {
            try {
                this.setOwnerId(UUID.fromString(compound.getString("OwnerUUID")));
            }
            catch (IllegalArgumentException e) {
                Dissolution.LOGGER.error("Could not read owner of will o' splinter", e);
            }
        }
    }
}

