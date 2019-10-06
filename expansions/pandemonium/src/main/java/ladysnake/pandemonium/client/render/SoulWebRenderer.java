package ladysnake.pandemonium.client.render;

import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import ladysnake.pandemonium.Pandemonium;
import ladysnake.satin.api.util.ShaderLoader;
import ladysnake.satin.api.util.ShaderPrograms;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.debug.PathfindingDebugRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.SystemUtil;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.io.IOException;
import java.util.List;

public final class SoulWebRenderer extends PathfindingDebugRenderer implements SimpleSynchronousResourceReloadListener {
    public static final Identifier RESOURCE_ID = Pandemonium.id("soul_web_renderer");

    private final Int2ObjectOpenHashMap<Path> paths = new Int2ObjectOpenHashMap<>();
    private final Int2LongOpenHashMap pathTimes = new Int2LongOpenHashMap();
    private final MinecraftClient client;
    private int shader;
    private boolean rendering;

    public SoulWebRenderer(MinecraftClient mc) {
        super(mc);
        client = mc;
    }

    @Override
    public void addPath(int entityId, Path path, float size) {
        super.addPath(entityId, path, size);
        this.paths.put(entityId, path);
        this.pathTimes.put(entityId, SystemUtil.getMeasuringTimeMs());
    }

    public void render(float tickDelta, long time) {
//        this.render(time);
        rendering = true;
        if (shader > 0) {
            ShaderPrograms.useShader(shader);
        }
        long systemTime = SystemUtil.getMeasuringTimeMs();
        for (ObjectIterator<Int2ObjectMap.Entry<Path>> iterator = this.paths.int2ObjectEntrySet().fastIterator(); iterator.hasNext(); ) {
            Int2ObjectMap.Entry<Path> pathEntry = iterator.next();
            int entityId = pathEntry.getIntKey();
            if (systemTime - pathTimes.get(entityId) > 20000L) {
                iterator.remove();
                continue;
            }
            Entity tracked = this.client.world.getEntityById(entityId);
            if (tracked != null) {
                double x = tracked.x;
                double y = tracked.y;
                double z = tracked.z;
                double prevX = tracked.prevRenderX;
                double prevY = tracked.prevRenderY;
                double prevZ = tracked.prevRenderZ;
                Path path = pathEntry.getValue();
                List<PathNode> nodes = path.getNodes();
                int currentNodeIndex = path.getCurrentNodeIndex();

                int max = Math.min(currentNodeIndex + 4, nodes.size());
                Vec3d lastNodePos = path.getNodePosition(tracked, path.getLength() - 1);
                Vec3d firstNodePos = path.getNodePosition(tracked, 0);
                double progress = path.isFinished() ? 1 : Math.max(0, (1 - tracked.squaredDistanceTo(lastNodePos) / lastNodePos.squaredDistanceTo(firstNodePos)));
                for (int i = currentNodeIndex; i < max; i++) {
                    Vec3d node = path.getNodePosition(tracked, i);
                    tracked.x = tracked.prevRenderX = MathHelper.lerp(progress, tracked.x, node.x);
                    tracked.y = tracked.prevRenderY = MathHelper.lerp(progress, tracked.y, node.y);
                    tracked.z = tracked.prevRenderZ = MathHelper.lerp(progress, tracked.z, node.z);
                    client.getEntityRenderManager().render(tracked, tickDelta, false);
                }
                tracked.x = x;
                tracked.y = y;
                tracked.z = z;
                tracked.prevRenderX = prevX;
                tracked.prevRenderY = prevY;
                tracked.prevRenderZ = prevZ;
            }
        }
        ShaderPrograms.useShader(0);
        rendering = false;
    }

    public boolean isRendering() {
        return rendering;
    }

    @Override
    public Identifier getFabricId() {
        return RESOURCE_ID;
    }

    @Override
    public void apply(ResourceManager var1) {
        try {
            shader = ShaderLoader.getInstance().loadShader(client.getResourceManager(), Pandemonium.id("shaders/vertex_base.vsh"), Pandemonium.id("shaders/king_crimson.fsh"));
        } catch (IOException e) {
            Pandemonium.LOGGER.error("Failed to load shader", e);
            shader = -1;
        }
    }
}
