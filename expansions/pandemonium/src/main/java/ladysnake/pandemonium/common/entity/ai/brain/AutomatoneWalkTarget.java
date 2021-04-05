package ladysnake.pandemonium.common.entity.ai.brain;

import baritone.api.pathing.goals.Goal;
import net.minecraft.entity.ai.brain.LookTarget;
import net.minecraft.entity.ai.brain.WalkTarget;

public class AutomatoneWalkTarget extends WalkTarget {
    private final Goal goal;

    public AutomatoneWalkTarget(LookTarget lookTarget, float speed, Goal goal) {
        super(lookTarget, speed, 0);
        this.goal = goal;
    }

    public Goal getGoal() {
        return goal;
    }
}
