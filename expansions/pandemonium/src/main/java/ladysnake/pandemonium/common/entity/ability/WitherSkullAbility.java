package ladysnake.pandemonium.common.entity.ability;

import ladysnake.requiem.common.entity.ability.DirectAbilityBase;
import ladysnake.requiem.common.entity.ability.IndirectAbilityBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

public class WitherSkullAbility {
    public static class BlueWitherSkullAbility extends IndirectAbilityBase<WitherEntity> {
        private static final Random RANDOM = new Random();

        public BlueWitherSkullAbility(WitherEntity owner, int cooldown) {
            super(owner, cooldown);
        }

        public BlueWitherSkullAbility(WitherEntity owner) {
            this(owner,40);
        }

        @Override
        protected boolean run() {
            if (!owner.isSilent()) {
                owner.world.syncWorldEvent(null, 1024, owner.getBlockPos(), 0);
            }
            Vec3d rot = this.owner.getRotationVec(1.0f).multiply(10);
            WitherSkullEntity witherSkullEntity = new WitherSkullEntity(
                this.owner.world,
                this.owner,
                rot.x + this.owner.getRandom().nextGaussian(),
                rot.y,
                rot.z + this.owner.getRandom().nextGaussian()
            );
            witherSkullEntity.setOwner(owner);
            witherSkullEntity.setCharged(true);

            int headIndex = RANDOM.nextInt(3);
            double g = getHeadX(headIndex);
            double h = getHeadY(headIndex);
            double i = getHeadZ(headIndex);
            witherSkullEntity.setPos(g, h, i);
            owner.world.spawnEntity(witherSkullEntity);
            return true;
        }

        private double getHeadX(int headIndex) {
            if (headIndex <= 0) {
                return owner.getX();
            } else {
                float f = (owner.bodyYaw + (float)(180 * (headIndex - 1))) * 0.017453292F;
                float g = MathHelper.cos(f);
                return owner.getX() + (double)g * 1.3D;
            }
        }

        private double getHeadY(int headIndex) {
            return headIndex <= 0 ? owner.getY() + 3.0D : owner.getY() + 2.2D;
        }

        private double getHeadZ(int headIndex) {
            if (headIndex <= 0) {
                return owner.getZ();
            } else {
                float f = (owner.bodyYaw + (float)(180 * (headIndex - 1))) * 0.017453292F;
                float g = MathHelper.sin(f);
                return owner.getZ() + (double)g * 1.3D;
            }
        }
    }

    public static class BlackWitherSkullAbility extends DirectAbilityBase<WitherEntity, LivingEntity> {
        private static final Random RANDOM = new Random();

        public BlackWitherSkullAbility(WitherEntity owner, int cooldown) {
            super(owner, cooldown, 20, LivingEntity.class);
        }

        public BlackWitherSkullAbility(WitherEntity owner) {
            this(owner,40);
        }

        @Override
        protected boolean run(LivingEntity target) {
            if (!owner.isSilent()) {
                owner.world.syncWorldEvent(null, 1024, owner.getBlockPos(), 0);
            }

            int headIndex = RANDOM.nextInt(3);
            double g = getHeadX(headIndex);
            double h = getHeadY(headIndex);
            double i = getHeadZ(headIndex);
            double j = target.getX() - g;
            double k = target.getY() + (double)target.getStandingEyeHeight() * 0.5D - h;
            double l = target.getZ() - i;
            WitherSkullEntity witherSkullEntity = new WitherSkullEntity(owner.world, owner, j, k, l);
            witherSkullEntity.setOwner(owner);

            witherSkullEntity.setPos(g, h, i);
            owner.world.spawnEntity(witherSkullEntity);
            return true;
        }

        private double getHeadX(int headIndex) {
            if (headIndex <= 0) {
                return owner.getX();
            } else {
                float f = (owner.bodyYaw + (float)(180 * (headIndex - 1))) * 0.017453292F;
                float g = MathHelper.cos(f);
                return owner.getX() + (double)g * 1.3D;
            }
        }

        private double getHeadY(int headIndex) {
            return headIndex <= 0 ? owner.getY() + 3.0D : owner.getY() + 2.2D;
        }

        private double getHeadZ(int headIndex) {
            if (headIndex <= 0) {
                return owner.getZ();
            } else {
                float f = (owner.bodyYaw + (float)(180 * (headIndex - 1))) * 0.017453292F;
                float g = MathHelper.sin(f);
                return owner.getZ() + (double)g * 1.3D;
            }
        }
    }
}
