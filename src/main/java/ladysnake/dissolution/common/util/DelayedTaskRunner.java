package ladysnake.dissolution.common.util;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import ladylib.compat.EnhancedBusSubscriber;
import ladysnake.dissolution.common.Ref;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apiguardian.api.API;

import java.util.ArrayList;
import java.util.List;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

public class DelayedTaskRunner {
    @EnhancedBusSubscriber(Ref.MOD_ID)
    public static final DelayedTaskRunner INSTANCE = new DelayedTaskRunner();

    private final Int2ObjectMap<List<DelayedTask>> dimensionsDelayedTasks = new Int2ObjectOpenHashMap<>();

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
        List<DelayedTask> tasksForDimension = dimensionsDelayedTasks.get(dimension);
        if (tasksForDimension == null) {
            dimensionsDelayedTasks.put(dimension, tasksForDimension = new ArrayList<>());
        }
        tasksForDimension.add(new DelayedTask(action, delay));
    }
}
