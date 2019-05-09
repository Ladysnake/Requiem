package ladysnake.requiem.api.v1.util;

import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.profiler.Profiler;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public interface SubDataManager<T> extends SimpleResourceReloadListener<T> {
    @Override
    default CompletableFuture<Void> apply(T data, ResourceManager manager, Profiler profiler, Executor executor) {
        return CompletableFuture.runAsync(() -> apply(data), executor);
    }

    void toPacket(PacketByteBuf buf);

    /**
     * Asynchronously process and load data from a packet. The code
     * must be thread-safe and not modify game state!
     */
    T loadFromPacket(PacketByteBuf buf);

    /**
     * Synchronously apply loaded data to the game state.
     */
    void apply(T data);
}
