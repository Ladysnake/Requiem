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
    private static final Random RANDOM = new Random();

    public WitherSkullAbility(WitherEntity owner, int cooldownTime) {
        super(owner, cooldownTime);
    }

    private static double getHeadX(WitherEntity owner, int headIndex) {
        if (headIndex <= 0) {
            return owner.getX();
        } else {
            float f = (owner.bodyYaw + (float)(180 * (headIndex - 1))) * 0.017453292F;
            float g = MathHelper.cos(f);
            return owner.getX() + (double)g * 1.3D;
        }
    }

    private static double getHeadY(WitherEntity owner, int headIndex) {
        return headIndex <= 0 ? owner.getY() + 3.0D : owner.getY() + 2.2D;
    }

    private static double getHeadZ(WitherEntity owner, int headIndex) {
        if (headIndex <= 0) {
            return owner.getZ();
        } else {
            float f = (owner.bodyYaw + (float)(180 * (headIndex - 1))) * 0.017453292F;
            float g = MathHelper.sin(f);
            return owner.getZ() + (double)g * 1.3D;
        }
    }

    private static WitherSkullEntity summonSkullWithTarget(WitherEntity owner, double j, double k, double l) {
        int headIndex = RANDOM.nextInt(3);
        double x = getHeadX(owner, headIndex);
        double y = getHeadY(owner, headIndex);
        double z = getHeadZ(owner, headIndex);
        return summonSkullWithTarget(owner, x, y, z, j, k, l);
    }

    private static WitherSkullEntity summonSkullWithTarget(WitherEntity owner, double x, double y, double z, double j, double k, double l) {
        if (!owner.isSilent()) {
            owner.world.syncWorldEvent(null, 1024, owner.getBlockPos(), 0);
        }
        WitherSkullEntity witherSkullEntity = new WitherSkullEntity(
            owner.world,
            owner,
            j, k, l
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
            summonSkullWithTarget(owner, rot.x + this.owner.getRandom().nextGaussian(), rot.y, rot.z + this.owner.getRandom().nextGaussian())
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
            double g = getHeadX(owner, headIndex);
            double h = getHeadY(owner, headIndex);
            double i = getHeadZ(owner, headIndex);
            double j = target.getX() - g;
            double k = target.getY() + (double)target.getStandingEyeHeight() * 0.5D - h;
            double l = target.getZ() - i;
            summonSkullWithTarget(owner, g, h, i, j, k, l);
            return true;
        }
    }
}
