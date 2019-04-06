package ladysnake.dissolution.common.entity.ai.brain;

import ladysnake.dissolution.Dissolution;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Schedule;
import net.minecraft.util.registry.Registry;

public final class DissolutionSchedules {
    private DissolutionSchedules() { throw new AssertionError(); }

    public static Schedule AWAKENED_PLAYER_SHELL;

    public static void init() {
        AWAKENED_PLAYER_SHELL = Registry.register(Registry.SCHEDULE, Dissolution.id("test"), ScheduleBuilderWrapper.create()
                .withActivity(0, Activity.CORE)
                .build());
    }

}
