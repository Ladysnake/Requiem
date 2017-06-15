package ladysnake.dissolution.common.entity;

import java.util.LinkedList;

import ladysnake.dissolution.common.entity.ai.boss.EntityAiAttackBrimstoneFires;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BossMawOfTheVoid extends EntityCreature {
	
	public int lastAttack;
	private LinkedList<EntityBrimstoneFire> brimstoneFires;

	public BossMawOfTheVoid(World worldIn) {
		super(worldIn);
		lastAttack = -1;
		brimstoneFires = new LinkedList<>();
	}
	
	@Override
	public void initEntityAI() {
		tasks.taskEntries.clear();
    	targetTasks.taskEntries.clear();
    	tasks.addTask(0, new EntityAiAttackBrimstoneFires(brimstoneFires));
    	tasks.addTask(1, new BossMawOfTheVoid.AIDoNothing());
    	tasks.addTask(2, new BossMawOfTheVoid.AITest());
	}
	
	class AIDoNothing extends EntityAIBase
    {
        public AIDoNothing()
        {
            this.setMutexBits(7);
        }

        @Override
        public boolean shouldExecute()
        {
            return true;
        }
    }
	
	class AITest extends EntityAIBase
	{
		private int i;
		
		public AITest()
		{
			this.setMutexBits(1);
			i = 0;
		}
		
		@Override
		public boolean shouldExecute()
		{
//			System.out.println("shouldExecute");
			return rand.nextBoolean();
		}
		
		@Override
		public boolean continueExecuting() {
//			System.out.println("continueExecuting");
			return i < 200;
		}
		
		@Override
		public void startExecuting() {
			System.out.println("startExecuting");
			i = 0;
		}
		
		@Override
		public void updateTask() 
		{
//			System.out.println(i++);
		}
	}

}
