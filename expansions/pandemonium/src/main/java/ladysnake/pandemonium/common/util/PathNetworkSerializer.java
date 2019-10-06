package ladysnake.pandemonium.common.util;

import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public final class PathNetworkSerializer {

    public static PacketByteBuf serializePath(Path path, int pathId, PacketByteBuf buf) {
        buf.writeInt(pathId);
        buf.writeFloat(0.5f); // visual path size
        buf.writeBoolean(false); // ?
        buf.writeInt(path.getCurrentNodeIndex());
        buf.writeInt(0); // length of some weird set that is never used
        BlockPos endPos = path.method_48();
        buf.writeInt(endPos.getX());
        buf.writeInt(endPos.getY());
        buf.writeInt(endPos.getZ());
        List<PathNode> nodes = path.getNodes();
        buf.writeInt(nodes.size());
        for (PathNode node : nodes) {
            serializePathNode(node, buf);
        }
        buf.writeInt(0); // length of some weird node array that exists only for the DebugRenderer
        buf.writeInt(0); // length of some other weird node array that exists only for the DebugRenderer
        return buf;
    }

    private static void serializePathNode(PathNode node, PacketByteBuf buf) {
        buf.writeInt(node.x);
        buf.writeInt(node.y);
        buf.writeInt(node.z);
        buf.writeFloat(node.field_46);
        buf.writeFloat(node.field_43);
        buf.writeBoolean(node.field_42);
        buf.writeInt(node.type.ordinal());
        buf.writeFloat(node.heapWeight);
    }
}
