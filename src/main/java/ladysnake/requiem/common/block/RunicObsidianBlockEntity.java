package ladysnake.requiem.common.block;

import ladysnake.requiem.common.tag.RequiemBlockTags;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class RunicObsidianBlockEntity extends BlockEntity implements Tickable {
    private static final Direction[] possibleDelegations = new Direction[]{Direction.DOWN, Direction.EAST, Direction.NORTH};
    private @Nullable BlockPos delegating = null;
    private boolean enabled = false;
    private int rangeLevel = 0;

    public RunicObsidianBlockEntity() {
        super(RequiemBlockEntities.RUNIC_OBSIDIAN);
    }

    @Override
    public void tick() {
        assert this.world != null;
        if (this.world.getTime() % 80L == 0L) {
            this.refresh();
        }
    }

    private RunicObsidianBlockEntity getObeliskCore() {
        if (this.world != null && this.delegating != null) {
            BlockEntity blockEntity = this.world.getBlockEntity(this.delegating);
            if (blockEntity instanceof RunicObsidianBlockEntity) {
                return (RunicObsidianBlockEntity) blockEntity;
            }
        }
        return this;
    }

    public int getRangeLevel() {
        return this.getObeliskCore().rangeLevel;
    }

    private void refresh() {
        assert this.world != null;
        this.delegating = null;
        this.enabled = false;
        this.rangeLevel = 0;
        BlockEntity be = this.world.getBlockEntity(this.pos.down());
        if (be instanceof RunicObsidianBlockEntity) {
            this.delegating = ((RunicObsidianBlockEntity) be).delegating != null ? ((RunicObsidianBlockEntity) be).delegating : this.pos.down();
        } else if (this.world.getBlockState(this.pos.down()).isIn(RequiemBlockTags.OBELISK_FRAME)) {
            be = this.world.getBlockEntity(this.pos.west());
            if (be instanceof RunicObsidianBlockEntity) {
                this.delegating = ((RunicObsidianBlockEntity) be).delegating != null ? ((RunicObsidianBlockEntity) be).delegating : this.pos.west();
            } else {
                be = this.world.getBlockEntity(this.pos.north());
                if (be instanceof RunicObsidianBlockEntity) {
                    this.delegating = ((RunicObsidianBlockEntity) be).delegating != null ? ((RunicObsidianBlockEntity) be).delegating : this.pos.north();
                } else {
                    // base of the obelisk
                    int level = this.checkBase();
                    if (level > 0) {
                        this.rangeLevel = level;
                    }
                }
            }
        }
    }

    private int checkBase() {
        assert this.world != null;
        // start at north-west corner
        BlockPos origin = this.pos.add(-1, -1, -1);
        BlockPos.Mutable pos = origin.mutableCopy();
        for (int lvl = 0; lvl < 5; lvl++) {
            for (int i=0; i<lvl+1; ++i) {
                if (!this.world.getBlockState(pos).isIn(RequiemBlockTags.OBELISK_FRAME)) {
                    return lvl-1;
                }
                pos.move(Direction.SOUTH);
            }

            for (int i=0; i<lvl; ++i) {
                if (!this.world.getBlockState(pos).isIn(RequiemBlockTags.OBELISK_FRAME)) {
                    return lvl-1;
                }
                pos.move(Direction.WEST);
            }

            pos.set(origin).move(Direction.EAST, lvl);
        }
        return 0;
    }
}
