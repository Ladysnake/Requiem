package ladysnake.dissolution.common.entity.ai;

import ladysnake.dissolution.common.entity.minion.AbstractMinion;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBow;
import net.minecraft.util.EnumHand;

public class EntityAIMinionAttack extends EntityAIAttackMelee {

    protected AbstractMinion minion;
    private int attackTime = -1;
    private int seeTime;
    private int attackCooldown;

    public EntityAIMinionAttack(AbstractMinion minion, double speedIn, boolean useLongMemory) {
        super(minion, speedIn, useLongMemory);
        float maxAttackDistance = 16;
        this.attackCooldown = 30;
        this.minion = minion;
    }

    @Override
    public void updateTask() {
        super.updateTask();

        if (minion.getHeldItemMainhand().getItem() != Items.BOW) return;

        EntityLivingBase entitylivingbase = this.minion.getAttackTarget();

        boolean flag = this.minion.getEntitySenses().canSee(entitylivingbase);
        boolean flag1 = this.seeTime > 0;
        if (this.minion.isHandActive()) {
            if (!flag && this.seeTime < -60) {
                this.minion.resetActiveHand();
            } else if (flag) {
                int i = this.minion.getItemInUseMaxCount();
                //System.out.println(i);

                if (i >= 20) {
                    this.minion.resetActiveHand();
                    this.minion.attackEntityWithRangedAttack(entitylivingbase, ItemBow.getArrowVelocity(i));
                    this.attackTime = this.attackCooldown;
                }
            }
        } else if (--this.attackTime <= 0 && this.seeTime >= -60) {
            this.minion.setActiveHand(EnumHand.MAIN_HAND);
            //System.out.println(this.entity.getHeldItemMainhand());
        }
    }

}
