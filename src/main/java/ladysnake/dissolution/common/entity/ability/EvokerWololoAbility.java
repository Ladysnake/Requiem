package ladysnake.dissolution.common.entity.ability;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.EvokerEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerEntity;

public class EvokerWololoAbility extends DirectAbilityBase<EvokerEntity> {
    private EvokerEntity.WololoGoal wololoGoal;
    private boolean started;

    public EvokerWololoAbility(EvokerEntity owner) {
        super(owner);
        wololoGoal = owner.new WololoGoal();
    }

    @Override
    public boolean trigger(PlayerEntity player, Entity entity) {
        boolean success = false;
        if (entity instanceof SheepEntity) {
            if (wololoGoal.canStart()) {
                wololoGoal.start();
                started = true;
                success = true;
            }
        }
        return success;
    }

    @Override
    public void update() {
        if (started && wololoGoal.shouldContinue()) {
            wololoGoal.tick();
        } else {
            started = false;
        }
    }

}
