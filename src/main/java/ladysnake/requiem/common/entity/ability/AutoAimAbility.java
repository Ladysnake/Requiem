package ladysnake.requiem.common.entity.ability;

import ladysnake.requiem.api.v1.entity.ability.AbilityType;
import ladysnake.requiem.api.v1.entity.ability.MobAbilityController;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.util.math.Box;

public class AutoAimAbility<E extends LivingEntity> extends IndirectAbilityBase<E> {
    private final double searchRangeX;
    private final double searchRangeY;
    private final AbilityType type;

    public AutoAimAbility(E owner, AbilityType type, double horizontalSearchRange, double verticalSearchRange) {
        super(owner, 0);
        this.searchRangeX = horizontalSearchRange;
        this.searchRangeY = verticalSearchRange;
        this.type = type;
    }

    @Override
    public Result trigger() {
        // method_21727 = getClosestEntity
        LivingEntity target = this.owner.world.getClosestEntityIncludingUngeneratedChunks(
            LivingEntity.class,
            new TargetPredicate(),
            this.owner,
            this.owner.getX(),
            this.owner.getY() + (double) this.owner.getStandingEyeHeight(),
            this.owner.getZ(),
            this.getSearchBox());
        if (target != null) {
            return Result.of(MobAbilityController.get(this.owner).useDirect(type, target));
        }
        return Result.FAIL;
    }

    private Box getSearchBox() {
        double range = this.searchRangeX;
        return this.owner.getBoundingBox().expand(range, searchRangeY, range);
    }
}
