package ladysnake.tartaros.common.entity.ai;

import ladysnake.tartaros.common.entity.EntityMinion;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIAttackMelee;

public class EntityAIMinionAttack extends EntityAIAttackMelee {

	private EntityMinion minion;
	
	public EntityAIMinionAttack(EntityMinion minion, double speedIn, boolean useLongMemory) {
		super(minion, speedIn, useLongMemory);
		this.minion = minion;
	}

	//TODO custom behavior
}
