package ladysnake.requiem.common.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class InertRunestoneBlockEntity extends BlockEntity {
    private @Nullable Text customName;

    public InertRunestoneBlockEntity(BlockPos pos, BlockState state) {
        super(RequiemBlockEntities.INERT_RUNIC_OBSIDIAN, pos, state);
    }

    public InertRunestoneBlockEntity(BlockEntityType<? extends InertRunestoneBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public Optional<Text> getCustomName() {
        return Optional.ofNullable(this.customName);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        if (nbt.contains("CustomName", 8)) {
            this.customName = Text.Serializer.fromJson(nbt.getString("CustomName"));
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);

        if (this.customName != null) {
            nbt.putString("CustomName", Text.Serializer.toJson(this.customName));
        }
    }
}
