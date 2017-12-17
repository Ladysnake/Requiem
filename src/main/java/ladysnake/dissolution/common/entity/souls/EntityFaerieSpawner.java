package ladysnake.dissolution.common.entity.souls;

import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EntityFaerieSpawner extends EntityLiving {
    public EntityFaerieSpawner(World worldIn) {
        super(worldIn);
        setInvisible(true);
    }

    @Override
    public boolean getCanSpawnHere() {
        // faerie spawn in full light
        if (!EntitySoulSpawner.isValidLightLevel(this.world, new BlockPos(this)) && this.world.canSeeSky(new BlockPos(this))) {
            if (this.world.getEntitiesWithinAABB(EntityFaerie.class, new AxisAlignedBB(this.getPosition()).grow(100)).size() < 10) {
                AbstractSoul faerie = new EntityFaerie(this.world);
                faerie.setLocationAndAngles(posX, posY, posZ, rotationYaw, rotationPitch);
                this.world.spawnEntity(faerie);
            }
        }
        return false;

    }
}
