package ladysnake.dissolution.common.entity.ai.brain;

import ladysnake.reflectivefabric.reflection.UncheckedReflectionException;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Schedule;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static ladysnake.reflectivefabric.reflection.ReflectionHelper.pick;

public class ScheduleBuilderWrapper {
    private static final MethodHandle SCHEDULE_BUILDER$NEW;
    private static final MethodHandle SCHEDULE_BUILDER$WITH_ACTIVITY;
    private static final MethodHandle SCHEDULE_BUILDER$BUILD;

    private final Object delegate;

    public static ScheduleBuilderWrapper create() {
        try {
            return new ScheduleBuilderWrapper(SCHEDULE_BUILDER$NEW.invokeExact(new Schedule()));
        } catch (Throwable t) {
            throw new UncheckedReflectionException(t);
        }
    }

    private ScheduleBuilderWrapper(Object delegate) {
        this.delegate = delegate;
    }

    public ScheduleBuilderWrapper withActivity(int startTime, Activity activity) {
        try {
            SCHEDULE_BUILDER$WITH_ACTIVITY.invokeExact(delegate, startTime, activity);
        } catch (Throwable t) {
            throw new UncheckedReflectionException(t);
        }
        return this;
    }

    public Schedule build() {
        try {
            return (Schedule) SCHEDULE_BUILDER$BUILD.invokeExact(delegate);
        } catch (Throwable t) {
            throw new UncheckedReflectionException(t);
        }
    }

    static {
        try {
            Class<?> scheduleBuilderClass = Class.forName(pick("net.minecraft.class_4171", "net.minecraft.entity.ai.brain.ScheduleBuilder"));
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            Constructor<?> builderCnst = scheduleBuilderClass.getConstructor(Schedule.class);
            builderCnst.setAccessible(true);
            Method withActivity = scheduleBuilderClass.getDeclaredMethod(pick("method_19221", "withActivity"), int.class, Activity.class);
            withActivity.setAccessible(true);
            Method build = scheduleBuilderClass.getDeclaredMethod(pick("method_19220", "build"));
            build.setAccessible(true);
            SCHEDULE_BUILDER$NEW = lookup.unreflectConstructor(builderCnst);
            SCHEDULE_BUILDER$WITH_ACTIVITY = lookup.unreflect(withActivity);
            SCHEDULE_BUILDER$BUILD = lookup.unreflect(build);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
            throw new UncheckedReflectionException("Failed to reflect into ScheduleBuilder", e);
        }
    }
}
