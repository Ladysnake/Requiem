package ladysnake.dissolution.common.entity.ai;

import net.minecraft.entity.ai.EntityAIBase;

public class EntityAIInert extends EntityAIBase {

    private boolean shouldExecute;

    public EntityAIInert(boolean shouldExecute) {
        super();
        this.shouldExecute = shouldExecute;
        this.setMutexBits(0xFF);
    }

    public void setShouldExecute(boolean shouldExecute) {
        this.shouldExecute = shouldExecute;
    }

    @Override
    public boolean shouldExecute() {
        return shouldExecute;
    }

    @Override
    public boolean isInterruptible() {
        return !shouldExecute;
    }

}
