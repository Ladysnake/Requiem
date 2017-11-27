package ladysnake.dissolution.common.entity.boss;

import ladysnake.dissolution.common.entity.ai.boss.BossAiAttackBrimstoneFires;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.world.World;

import java.util.LinkedList;
import java.util.List;

public class EntityMawOfTheVoid extends EntityCreature {

    public int lastAttack;

    public EntityMawOfTheVoid(World worldIn) {
        super(worldIn);
        lastAttack = -1;
    }

    @Override
    public void initEntityAI() {
        List<EntityBrimstoneFire> brimstoneFires = new LinkedList<>();
        tasks.taskEntries.clear();
        targetTasks.taskEntries.clear();
        tasks.addTask(0, new BossAiAttackBrimstoneFires(this, brimstoneFires));
        tasks.addTask(1, new EntityMawOfTheVoid.AIDoNothing());
        tasks.addTask(2, new EntityMawOfTheVoid.AITest());
    }

    class AIDoNothing extends EntityAIBase {
        public AIDoNothing() {
            this.setMutexBits(7);
        }

        @Override
        public boolean shouldExecute() {
            return false;
        }
    }

    class AITest extends EntityAIBase {
        private int i;

        public AITest() {
            this.setMutexBits(1);
            i = 0;
        }

        @Override
        public boolean shouldExecute() {
            i = 0;
            return rand.nextBoolean();
        }

        @Override
        public boolean shouldContinueExecuting() {
//			System.out.println("continueExecuting");
            return ++i < 2000 || true;
        }

        @Override
        public void startExecuting() {
            System.out.println("startExecuting");
            i = 0;
        }

        @Override
        public void updateTask() {
            if (i > 1998)
                lastAttack = 0;
        }
    }

}
