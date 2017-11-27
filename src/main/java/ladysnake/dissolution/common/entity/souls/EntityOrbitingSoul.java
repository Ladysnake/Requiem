package ladysnake.dissolution.common.entity.souls;

import ladysnake.dissolution.common.Dissolution;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityOrbitingSoul extends AbstractSoul {
    /**
     * Spherical coordinates of this entity
     */
    protected double theta, phi, r;
    /**
     * Motion in spherical coordinates
     */
    protected double motionTheta, motionPhi, motionR;
    private Entity orbited;

    public EntityOrbitingSoul(World worldIn) {
        super(worldIn);
        this.noClip = true;
    }

    public EntityOrbitingSoul(World worldIn, double x, double y, double z) {
        this(worldIn);
        this.setPosition(x, y, z);
    }

    public void onUpdate() {

        super.onUpdate();

        if (!this.hasNoGravity()) {
            this.motionY -= 0.03D;
        }

        if ((xTarget == 0 && yTarget == 0 && zTarget == 0) || this.soulAge % 100 == 0) {
            if (this.orbited == null || this.getDistanceSq(orbited) > 1024.0) {
                if ((this.orbited = this.world.getClosestPlayerToEntity(this, 1024.0)) != null) {
                    this.xTarget = this.orbited.posX;
                    this.yTarget = this.orbited.posY;
                    this.zTarget = this.orbited.posZ;
                    this.r = Math.sqrt((this.posX - this.xTarget) * (this.posX - this.xTarget) +
                            (this.posY - this.yTarget) * (this.posY - this.yTarget) +
                            (this.posZ - this.zTarget) * (this.posZ - this.zTarget));
                    this.theta = Math.acos((zTarget - this.posZ) / r);
                    this.phi = Math.atan((yTarget - this.posY) / (xTarget - this.posX));
                }
            }
        }

        if (this.orbited instanceof EntityPlayer && ((EntityPlayer) this.orbited).isSpectator()) {
            this.orbited = null;
        }

        if (this.orbited != null) {
            this.motionR = 0;
            this.motionPhi = (Math.PI / 180.0) * 2.0;
            this.motionTheta = (Math.PI / 180.0) * 2.0;
        } else {
            this.motionR = (this.motionTheta = (this.motionPhi = 0));
        }

        this.r += motionR;
        this.phi += motionPhi;
        this.theta += motionTheta;
        double newPosX = this.r * Math.sin(this.theta) * Math.cos(this.phi);
        double newPosY = this.r * Math.sin(this.theta) * Math.sin(this.phi);
        double newPosZ = this.r * Math.cos(this.theta);
        this.motionX = (newPosX + this.xTarget) - this.posX;
        this.motionY = (newPosY + this.yTarget) - this.posY;
        this.motionZ = (newPosZ + this.zTarget) - this.posZ;

        this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
    }

    @SideOnly(Side.CLIENT)
    protected void spawnParticles() {
        for (double i = 0; i < 9; i++) {
            double coeff = i / 9.0;
            Dissolution.proxy.spawnParticle(getEntityWorld(),
                    (float) (prevPosX + (posX - prevPosX) * coeff), (float) (prevPosY + (posY - prevPosY) * coeff), (float) (prevPosZ + (posZ - prevPosZ) * coeff),    //position
                    0.0125f * (rand.nextFloat() - 0.5f), 0.0125f * (rand.nextFloat() - 0.5f), 0.0125f * (rand.nextFloat() - 0.5f),    //motion
                    255, 64, 16, 255,    //color
                    2.0f, 24);
        }
    }

}
