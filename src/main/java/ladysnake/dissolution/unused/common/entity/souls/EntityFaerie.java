package ladysnake.dissolution.unused.common.entity.souls;

import elucent.albedo.lighting.Light;
import ladysnake.dissolution.common.capabilities.CapabilityIncorporealHandler;
import ladysnake.dissolution.common.entity.SoulType;
import ladysnake.dissolution.common.init.ModPotions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;

@Optional.Interface(iface = "elucent.albedo.lighting.ILightProvider", modid = "albedo", striprefs = true)
public class EntityFaerie extends EntityFleetingSoul {

    private static final DataParameter<Boolean> TIRED = EntityDataManager.createKey(EntityFaerie.class, DataSerializers.BOOLEAN);


    public EntityFaerie(World worldIn) {
        super(worldIn);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        // faeries heal once every 5 minutes on average
        if (!world.isRemote && rand.nextInt(6000) == 0) {
            this.setTired(false);
        }
    }

    @Override
    protected void selectBlockTarget() {
        this.xTarget = this.posX + rand.nextGaussian() * 10;
        this.yTarget = this.posY + rand.nextGaussian() * 10;
        this.zTarget = this.posZ + rand.nextGaussian() * 10;
        targetChangeCooldown = rand.nextInt() % 200;
    }

    @Override
    protected Entity selectTarget() {
        return this.world.getClosestPlayer(this.posX, this.posY, this.posZ, 32.0,
                player -> player instanceof EntityPlayer && !((EntityPlayer) player).isSpectator()
                        && CapabilityIncorporealHandler.getHandler(player).isPresent());
    }

    @Override
    public void onCollideWithPlayer(EntityPlayer entityIn) {
        if(!world.isRemote && !this.isTired()) {
            EntityLivingBase possessed = CapabilityIncorporealHandler.getHandler(entityIn).getPossessed();
            if (possessed != null) {
                possessed.addPotionEffect(new PotionEffect(ModPotions.PURIFICATION, 200, 1));
            } else {
                entityIn.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 200));
            }
            this.setTired(true);
        }
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.getDataManager().register(TIRED, false);
    }

    public Boolean isTired() {
        return this.getDataManager().get(TIRED);
    }

    public void setTired(boolean tired) {
        this.getDataManager().set(TIRED, tired);
    }

    @Override
    public SoulType getSoulType() {
        return isTired() ? SoulType.TIRED_FAERIE : SoulType.FAERIE;
    }

    @Override
    public Light provideLight() {
        return Light.builder().pos(this).radius(this.isTired() ? 2 : 5).color(0.9f, 0.4f, 0.7f).build();
    }
}
