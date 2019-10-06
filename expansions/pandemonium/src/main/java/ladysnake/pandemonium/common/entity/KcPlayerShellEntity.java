package ladysnake.pandemonium.common.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.world.World;

public class KcPlayerShellEntity extends PlayerShellEntity {
    protected KcPlayerShellEntity(EntityType<? extends PlayerShellEntity> type, World world) {
        super(type, world);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (this.age >= 100) {
            this.getPlayerUuid().map(world::getPlayerByUuid).ifPresent(player -> {
                this.copyPositionAndRotation(player);
                this.onSoulInteract(player);
            });
        }
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource_1) {
        return true;
    }
}
