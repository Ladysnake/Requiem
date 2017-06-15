package ladysnake.dissolution.common.entity.ai.boss;

import java.util.LinkedList;

import ladysnake.dissolution.common.entity.BossMawOfTheVoid;
import ladysnake.dissolution.common.entity.EntityBrimstoneFire;
import net.minecraft.entity.ai.EntityAIBase;

public class EntityAiAttackBrimstoneFires extends EntityAIBase {
	
	protected BossMawOfTheVoid attacker;
	/**The amount of ticks this attack has been active*/
	protected int attackTicks;
	/**The max duration for this attack*/
	protected int maxAttackDuration;
	
	public EntityAiAttackBrimstoneFires(LinkedList<EntityBrimstoneFire> allFires) {
		this.setMutexBits(0b0111);
	}

	@Override
	public boolean shouldExecute() {
		return false;
	}
	
	@Override
	public void startExecuting() {
		// TODO Auto-generated method stub
	}
	
	@Override
	public boolean continueExecuting() {
		// TODO Auto-generated method stub
		return this.shouldExecute();
	}
	
	@Override
	public void updateTask() {
		// TODO Auto-generated method stub
	}

}
