package ladysnake.dissolution.common.blocks;

import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.registries.SoulStates;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.MaterialLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.SoundCategory;
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

/**
 * A custom fluid class that emulates Archimede's principle as well as dynamic movement incapacitation
 * TODO make the fluid's density use forge's quanta instead of being hardcoded
 * TODO add a {@link net.minecraft.entity.ai.attributes.IAttribute} system to modify mob densities
 */
public class BlockFluidMercury extends BlockFluidClassic {

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

    public BlockFluidMercury(Fluid fluid) {
        super(fluid, MATERIAL_MERCURY);
        this.setDensity(3);
        this.setQuantaPerBlock(16);
        this.setLightOpacity(12);
        this.setLightLevel(5.0f);
    }

    @Override
    public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entityIn) {
        if (state.getBlock() != this) return;

        double immersedVolume = calculateVolumeImmersed(entityIn);
        double archimedesPush = immersedVolume * 0.07;

        try {
            entityIn.onGround = false;
            if (entityIn.fallDistance > 2 * entityIn.getMaxFallHeight())
                entityIn.fall(entityIn.fallDistance, 0.5f);
            entityIn.fallDistance = 0;
            if (entityIn instanceof EntityLivingBase) {
                if ((Boolean) isJumping.invoke(entityIn))
                    handleMercuryJump(entityIn);
                jumpTicks.invoke(entityIn, 2);
                if (entityIn.isSneaking())
                    entityIn.motionY -= 0.2;
            }
            entityIn.motionX *= (1.1 - immersedVolume);
            entityIn.motionY *= (1.1 - immersedVolume);
            entityIn.motionZ *= (1.1 - immersedVolume);
            entityIn.motionY += archimedesPush;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        if (entityIn instanceof EntityPlayer) {

            EntityPlayer player = (EntityPlayer) entityIn;

            if (player.getActivePotionEffects().isEmpty()) {
                player.addPotionEffect(new PotionEffect(Potion.getPotionById(19), 200, 2));
            }

            if (CapabilityIncorporealHandler.getHandler(player).getCorporealityStatus().isIncorporeal()) {

                world.playSound(null, player.getPosition(), SoundEvents.ITEM_TOTEM_USE, SoundCategory.PLAYERS, 4.0F, (float) (0.8F + Math.random() * 0.3F));
                CapabilityIncorporealHandler.getHandler(player).setCorporealityStatus(SoulStates.BODY);

            }
        }
        if (entityIn instanceof EntityLiving && ((EntityLiving) entityIn).getActivePotionEffects().isEmpty()) {
            ((EntityLiving) entityIn).addPotionEffect(new PotionEffect(Potion.getPotionById(19), 200, 2));
        }


        super.onEntityCollidedWithBlock(world, pos, state, entityIn);
    }

    private void handleMercuryJump(Entity entityIn) {
        entityIn.motionY = Math.max(entityIn.motionY, 0.4);
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
