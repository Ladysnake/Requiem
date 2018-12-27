package ladysnake.dissolution.common.util;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import ladylib.compat.EnhancedBusSubscriber;
import ladysnake.dissolution.common.Ref;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apiguardian.api.API;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

public class DelayedTaskRunner {
    @EnhancedBusSubscriber(Ref.MOD_ID)
    public static final DelayedTaskRunner INSTANCE = new DelayedTaskRunner();

    private final Int2ObjectMap<Queue<DelayedTask>> dimensionsDelayedTasks = new Int2ObjectOpenHashMap<>();

    @SubscribeEvent
    public void onTickWorldTick(TickEvent.WorldTickEvent event) {
        if (!event.world.isRemote) {
            int dimension = event.world.provider.getDimension();
            if (dimensionsDelayedTasks.containsKey(dimension)) {
                dimensionsDelayedTasks.get(dimension).removeIf(DelayedTask::tick);
            }
        }
    }

    @API(status = EXPERIMENTAL, since = "0.2.0")
    public void addDelayedTask(int dimension, int delay, Runnable action) {
        Queue<DelayedTask> tasksForDimension = dimensionsDelayedTasks.get(dimension);
        if (tasksForDimension == null) {
            // Using a concurrent queue as tasks could be added during iteration
            dimensionsDelayedTasks.put(dimension, tasksForDimension = new ConcurrentLinkedQueue<>());
        }
        tasksForDimension.add(new DelayedTask(action, delay));
    }
}
