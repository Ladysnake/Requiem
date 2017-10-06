package ladysnake.dissolution.common.entity.souls;

import ladysnake.dissolution.api.Soul;
import ladysnake.dissolution.api.SoulTypes;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EntitySoulSpawner extends EntityLiving {

    public EntitySoulSpawner(World worldIn) {
        super(worldIn);
        this.setInvisible(true);
    }

    @Override
    public boolean getCanSpawnHere() {
        if(super.getCanSpawnHere() && this.world.canSeeSky(new BlockPos(this))) {
            if (this.world.getEntitiesWithinAABB(AbstractSoul.class, new AxisAlignedBB(this.getPosition()).grow(100)).size() < 10) {
                AbstractSoul soul = new EntityFleetingSoul(this.world, this.posX, this.posY, this.posZ, new Soul(SoulTypes.BENIGN));
                this.world.spawnEntity(soul);
            }
        }
        return false;
    }
}
