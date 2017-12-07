package ladysnake.dissolution.common.entity;

import ladysnake.dissolution.common.entity.minion.AbstractMinion;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;

public class EntityRunicCircle extends Entity {
    private static final DataParameter<Integer> CIRCLES = EntityDataManager
            .createKey(EntityRunicCircle.class, DataSerializers.VARINT);


    public EntityRunicCircle(World worldIn) {
        super(worldIn);
    }

    public void getMotifs() {
        int circles = getDataManager().get(CIRCLES);
        for (int i = 0; i < 32; i++) {
            if ((circles & (1 << i)) > 0) {
                
            }
        }
    }

    @Override
    protected void entityInit() {
        this.getDataManager().register(CIRCLES, 0);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {

    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {

    }
}
