package ladysnake.requiem.common.impl.anchor;

import ladysnake.requiem.api.v1.remnant.FractureAnchorManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;

import javax.annotation.Nullable;
import java.util.UUID;

public class EntityFractureAnchor extends TrackedFractureAnchor {
    private final UUID entityUuid;

    public EntityFractureAnchor(UUID entityUuid, FractureAnchorManager manager, UUID uuid, int id) {
        super(manager, uuid, id);
        this.entityUuid = entityUuid;
    }

    protected EntityFractureAnchor(FractureAnchorManager manager, CompoundTag tag, int id) {
        super(manager, tag, id);
        this.entityUuid = tag.getUuid("AnchorEntity");
    }

    @Override
    public void update() {
        super.update();
        Entity entity = this.getEntity();
        if (entity != null) {
            if (entity instanceof LivingEntity && ((LivingEntity)entity).getHealth() <= 0.0F) {
                this.invalidate();
            } else if (entity.x != this.x || entity.y != this.y || entity.z != this.z) {
                this.setPosition(entity.x, entity.y, entity.z);
            }
        } else if (this.manager.getWorld().isChunkLoaded(((int)this.x) >> 4, ((int)this.z) >> 4)) {
            // chunk is loaded but entity not found -- assume dead
            this.invalidate();
        }
    }

    @Nullable
    public Entity getEntity() {
        return ((ServerWorld)this.manager.getWorld()).getEntity(this.entityUuid);
    }

    @Override
    public CompoundTag toTag(CompoundTag anchorTag) {
        super.toTag(anchorTag);
        anchorTag.putString("AnchorType", "requiem:entity");
        anchorTag.putUuid("AnchorEntity", this.entityUuid);
        return anchorTag;
    }
}
