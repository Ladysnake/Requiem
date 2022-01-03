package ladysnake.requiem.common.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ladysnake.requiem.core.util.serde.MoreCodecs;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import java.util.Optional;

/**
 * @param pos the position of the structure's origin
 */
public record ObeliskDescriptor(RegistryKey<World> dimension, BlockPos pos, int width, int height, Optional<Text> name) {
    public static final Codec<ObeliskDescriptor> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        World.CODEC.fieldOf("dimension").forGetter(ObeliskDescriptor::dimension),
        BlockPos.CODEC.fieldOf("pos").forGetter(ObeliskDescriptor::pos),
        Codec.INT.optionalFieldOf("width", 1).forGetter(ObeliskDescriptor::width),
        Codec.INT.optionalFieldOf("height", 1).forGetter(ObeliskDescriptor::height),
        MoreCodecs.text(MoreCodecs.STRING_JSON).optionalFieldOf("name").forGetter(ObeliskDescriptor::name)
    ).apply(instance, ObeliskDescriptor::new));

    public ObeliskDescriptor(RegistryKey<World> dimension, BlockPos pos, int width, int height) {
        this(dimension, pos, width, height, Optional.empty());
    }

    public Vec3d center() {
        return new Vec3d(
            (this.pos().getX() * 2 + this.width - 1) * 0.5,
            (this.pos().getY() * 2 + this.height - 1) * 0.5,
            (this.pos().getZ() * 2 + this.width - 1) * 0.5
        );
    }

    public Text resolveName() {
        return this.name().orElseGet(() -> Text.of(this.pos.toShortString()));
    }
}
