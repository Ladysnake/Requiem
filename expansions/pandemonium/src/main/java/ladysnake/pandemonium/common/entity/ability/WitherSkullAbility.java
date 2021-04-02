package ladysnake.pandemonium.common.entity.ability;

import ladysnake.requiem.api.v1.entity.ability.DirectAbility;
import ladysnake.requiem.api.v1.entity.ability.IndirectAbility;
import ladysnake.requiem.common.entity.ability.AbilityBase;
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

public class WitherSkullAbility extends AbilityBase<WitherEntity> {
    protected static final Random RANDOM = new Random();

    public WitherSkullAbility(WitherEntity owner, int cooldownTime) {
        super(owner, cooldownTime);
    }

    protected double getHeadX(int headIndex) {
        if (headIndex <= 0) {
            return owner.getX();
        } else {
            float f = (owner.bodyYaw + (float)(180 * (headIndex - 1))) * 0.017453292F;
            float g = MathHelper.cos(f);
            return owner.getX() + (double)g * 1.3D;
        }
    }

    protected double getHeadY(int headIndex) {
        return headIndex <= 0 ? owner.getY() + 3.0D : owner.getY() + 2.2D;
    }

    protected double getHeadZ(int headIndex) {
        if (headIndex <= 0) {
            return owner.getZ();
        } else {
            float f = (owner.bodyYaw + (float)(180 * (headIndex - 1))) * 0.017453292F;
            float g = MathHelper.sin(f);
            return owner.getZ() + (double)g * 1.3D;
        }
    }

    protected WitherSkullEntity summonSkullWithTarget(double dirX, double dirY, double dirZ) {
        int headIndex = RANDOM.nextInt(3);
        double x = getHeadX(headIndex);
        double y = getHeadY(headIndex);
        double z = getHeadZ(headIndex);
        return summonSkullWithTarget(x, y, z, dirX, dirY, dirZ);
    }

    protected WitherSkullEntity summonSkullWithTarget(double x, double y, double z, double dirX, double dirY, double dirZ) {
        if (!owner.isSilent()) {
            owner.world.syncWorldEvent(null, 1024, owner.getBlockPos(), 0);
        }
        WitherSkullEntity witherSkullEntity = new WitherSkullEntity(
            this.owner.world,
            this.owner,
            dirX, dirY, dirZ
        );
        witherSkullEntity.setOwner(owner);

        witherSkullEntity.setPos(x, y, z);
        owner.world.spawnEntity(witherSkullEntity);
        return witherSkullEntity;
    }

    public static class BlueWitherSkullAbility extends WitherSkullAbility implements IndirectAbility<WitherEntity> {

        public BlueWitherSkullAbility(WitherEntity owner, int cooldown) {
            super(owner, cooldown);
        }

        public BlueWitherSkullAbility(WitherEntity owner) {
            this(owner,40);
        }

        /**
         * Triggers an indirect ability.
         *
         * @return <code>true</code> if the ability has been successfully used
         */
        @Override
        public boolean trigger() {
            Vec3d rot = this.owner.getRotationVec(1.0f).multiply(10);
            this.summonSkullWithTarget(rot.x + this.owner.getRandom().nextGaussian(), rot.y, rot.z + this.owner.getRandom().nextGaussian())
                .setCharged(true);
            return true;
        }
    }

    public static class BlackWitherSkullAbility extends WitherSkullAbility implements DirectAbility<WitherEntity, LivingEntity> {
        public BlackWitherSkullAbility(WitherEntity owner, int cooldown) {
            super(owner, cooldown);
        }

        public BlackWitherSkullAbility(WitherEntity owner) {
            this(owner,40);
        }

        /**
         * If the range is 0, the vanilla targeting system is used
         */
        @Override
        public double getRange() {
            return 20;
        }

        @Override
        public Class<LivingEntity> getTargetType() {
            return LivingEntity.class;
        }

        @Override
        public boolean canTarget(LivingEntity target) {
            return true;
        }

        @Override
        public boolean trigger(LivingEntity target) {
            int headIndex = RANDOM.nextInt(3);
            double g = getHeadX(headIndex);
            double h = getHeadY(headIndex);
            double i = getHeadZ(headIndex);
            double dirX = target.getX() - g;
            double dirY = target.getY() + (double)target.getStandingEyeHeight() * 0.5D - h;
            double dirZ = target.getZ() - i;
            this.summonSkullWithTarget(g, h, i, dirX, dirY, dirZ);
            return true;
        }
    }
}
