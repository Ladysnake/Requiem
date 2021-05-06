package ladysnake.requiem.common.block;

import ladysnake.requiem.common.tag.RequiemBlockTags;
import net.minecraft.block.BlockState;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.StairShape;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class RunicObsidianBlockEntity extends BlockEntity implements Tickable {
    private static final Direction[] possibleDelegations = new Direction[]{Direction.DOWN, Direction.EAST, Direction.NORTH};
    private @Nullable BlockPos delegating = null;
    private boolean enabled = false;
    private int obeliskWidth = 0;
    private int obeliskHeight = 0;

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
        return this.getObeliskCore().obeliskWidth;
    }

    public int getPowerLevel() {
        return this.getObeliskCore().obeliskHeight;
    }

    private void refresh() {
        assert this.world != null;
        this.delegating = null;
        this.enabled = false;
        this.obeliskWidth = 0;
        this.obeliskHeight = 0;
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
                    int width = this.checkBase();
                    if (width > 0) {
                        int height = this.checkCore(width);
                        if (height > 0 && this.checkCap(width, height)) {
                            this.obeliskWidth = width;
                            this.obeliskHeight = height;
                        }
                    }
                }
            }
        }
    }

    private int checkCore(int width) {
        assert this.world != null;
        // start at north-west corner
        BlockPos origin = this.pos.add(-1, 0, -1);
        BlockPos.Mutable pos = origin.mutableCopy();
        int height = 0;

        while (true) {
            if (!world.getBlockState(pos.set(origin.getX(), origin.getY() + height, origin.getZ())).isIn(RequiemBlockTags.OBELISK_FRAME)
                || !world.getBlockState(pos.set(origin.getX() + width + 1, origin.getY() + height, origin.getZ())).isIn(RequiemBlockTags.OBELISK_FRAME)
                || !world.getBlockState(pos.set(origin.getX() + width + 1, origin.getY() + height, origin.getZ() + width + 1)).isIn(RequiemBlockTags.OBELISK_FRAME)
                || !world.getBlockState(pos.set(origin.getX(), origin.getY() + height, origin.getZ() + width + 1)).isIn(RequiemBlockTags.OBELISK_FRAME)
            ) {
                return height;
            }
            height++;
        }
    }

    private int checkBase() {
        return checkObeliskExtremity(this.pos.add(-1, -1, -1), state -> state.isIn(RequiemBlockTags.OBELISK_FRAME));
    }

    private boolean checkCap(int width, int height) {
        assert this.world != null;
        if (this.checkObeliskExtremity(this.pos.add(-1, height, -1), state -> {
            if (state.isOf(RequiemBlocks.POLISHED_OBSIDIAN_STAIRS)) {
                StairShape stairShape = state.get(StairsBlock.SHAPE);
                return stairShape == StairShape.OUTER_LEFT || stairShape == StairShape.OUTER_RIGHT;
            }
            return false;
        }) != width) {
            return false;
        }
        for (BlockPos blockPos : BlockPos.iterate(this.pos.up(height + 1), this.pos.add(width-1, height + 1, width-1))) {
            if (!world.getBlockState(blockPos).isOf(RequiemBlocks.POLISHED_OBSIDIAN_SLAB)) {
                return false;
            }
        }
        return true;
    }

    private int checkObeliskExtremity(BlockPos origin, Predicate<BlockState> corner) {
        assert this.world != null;
        // start at bottom north-west corner
        BlockPos.Mutable pos = origin.mutableCopy();
        int lastSideLength = -1;

        for (Direction direction : new Direction[]{Direction.SOUTH, Direction.EAST, Direction.NORTH, Direction.WEST}) {
            int sideLength = 0;
            while (true) {
                BlockState state = world.getBlockState(pos);
                if (corner.test(state)) {
                    // Corner block can be start or end of side
                    if (sideLength > 0) {
                        break;
                    }
                } else if (state.isOf(RequiemBlocks.POLISHED_OBSIDIAN_STAIRS)) {
                    sideLength++;
                    if (sideLength > 5) {
                        // Too large: not a valid base
                        return 0;
                    }
                } else {
                    // Unexpected block: not a valid base
                    return 0;
                }
                pos.move(direction);
            }
            if (lastSideLength < 0) {
                lastSideLength = sideLength;
            } else if (lastSideLength != sideLength) {
                // Not a square base: not a valid base
                return 0;
            }
        }

        return lastSideLength;
    }
}
