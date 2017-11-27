package ladysnake.dissolution.common.blocks;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.MaterialLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

public class BlockFluidLol extends BlockFluidClassic {

    public static final MaterialLiquid MATERIAL_MERCURY = new MaterialLiquid(MapColor.IRON);
    private static MethodHandle jumpTicks, isJumping;

    static {
        try {
            Field field = ReflectionHelper.findField(EntityLivingBase.class, "jumpTicks", "field_70773_bE");
            jumpTicks = MethodHandles.lookup().unreflectSetter(field);
            field = ReflectionHelper.findField(EntityLivingBase.class, "isJumping", "field_70703_bu");
            isJumping = MethodHandles.lookup().unreflectGetter(field);
        } catch (ReflectionHelper.UnableToFindFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public BlockFluidLol(Fluid fluid) {
        super(fluid, MATERIAL_MERCURY);
        this.setDensity(3);
        this.setQuantaPerBlock(16);
        this.setLightOpacity(12);
        this.setLightLevel(5.0f);
    }

    @Override
    public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entityIn) {
        if (state.getBlock() != this) return;

        double archimedesPush = calculateVolumeImmersed(entityIn) * 0.08;

        try {
            entityIn.onGround = false;
            entityIn.fallDistance = 0;
            entityIn.motionY += archimedesPush;
            if (entityIn instanceof EntityLivingBase) {
                if ((Boolean) isJumping.invoke(entityIn))
                    handleMercuryJump(entityIn);
                jumpTicks.invoke(entityIn, 2);
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        super.onEntityCollidedWithBlock(world, pos, state, entityIn);
    }

    private void handleMercuryJump(Entity entityIn) {
        entityIn.motionY += 0.4;
    }

    /**
     * Calculates the ratio between the entity's total volume and the volume of mercury it displaces
     */
    private double calculateVolumeImmersed(Entity entity) {
        AxisAlignedBB bb = entity.getEntityBoundingBox();
        int minX = MathHelper.floor(bb.minX);
        int maxX = MathHelper.ceil(bb.maxX);
        int minY = MathHelper.floor(bb.minY);
        int maxY = MathHelper.ceil(bb.maxY);
        int minZ = MathHelper.floor(bb.minZ);
        int maxZ = MathHelper.ceil(bb.maxZ);

        BlockPos.PooledMutableBlockPos blockPos$pooledMutableBlockPos = BlockPos.PooledMutableBlockPos.retain();
        double volume = 0;

        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                for (int z = minZ; z <= maxZ; ++z) {
                    if (entity.world.getBlockState(blockPos$pooledMutableBlockPos.setPos(x, y, z)).getMaterial() == MATERIAL_MERCURY) {
                        double immX = 1, immY = 1, immZ = 1;
                        if (x < bb.minX)
                            immX -= bb.minX - x;
                        if (y < bb.minY)
                            immY -= bb.minY - y;
                        if (z < bb.minZ)
                            immZ -= bb.minZ - z;
                        if (x + 1 > bb.maxX)
                            immX -= x + 1 - bb.maxX;
                        if (y + 1 > bb.maxY)
                            immY -= y + 1 - bb.maxY;
                        if (z + 1 > bb.maxZ)
                            immZ -= z + 1 - bb.maxZ;
                        volume += immX * immY * immZ;
                    }
                }
            }
        }

        blockPos$pooledMutableBlockPos.release();
        return volume / (Math.abs(bb.maxX - bb.minX) * Math.abs(bb.maxY - bb.minY) * Math.abs(bb.maxZ - bb.minZ));
    }
}
