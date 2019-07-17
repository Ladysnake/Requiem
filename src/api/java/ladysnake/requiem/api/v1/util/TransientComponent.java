package ladysnake.requiem.api.v1.util;

import nerdhub.cardinal.components.api.component.Component;
import net.minecraft.nbt.CompoundTag;

/**
 * A component that has no data to save, or has its data saved through external systems
 */
public interface TransientComponent extends Component {
    @Override
    default void fromTag(CompoundTag tag) {
        // NO-OP
    }

    @Override
    default CompoundTag toTag(CompoundTag tag) {
        // NO-OP
        return tag;
    }
}
