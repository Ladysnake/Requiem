package ladysnake.tartaros.common.entity;

import java.util.UUID;

import javax.annotation.Nullable;

import ladysnake.tartaros.common.Reference;
import ladysnake.tartaros.common.capabilities.IIncorporealHandler;
import ladysnake.tartaros.common.capabilities.IncorporealDataHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityWanderingSoul extends EntityMob {

	public static final ResourceLocation LOOT = new ResourceLocation(Reference.MOD_ID, "entities/wandering_soul");
	public int texture_id;
	public static final int texture_total = 4;

    public EntityWanderingSoul(World worldIn)
    {
        super(worldIn);
        texture_id = this.world.rand.nextInt(texture_total) + 1;
        setSize(0.6F, 1.95F);
    }
    
    @Override
    protected void entityInit() {
        super.entityInit();
    }
    
    @Override
    protected void initEntityAI() {
    	clearAITasks();
    	tasks.addTask(8, new EntityAIWander(this, 1.0D));
    	tasks.addTask(5, new EntityAILookIdle(this));
    	tasks.addTask(5, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
    }
    

    
    protected void clearAITasks() {
    	tasks.taskEntries.clear();
    	targetTasks.taskEntries.clear();
    }
    
    
    protected boolean isAIEnabled() {
    	return true;
    }
    
    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return LOOT;
    }

	@Override
	public boolean isEntityInvulnerable(DamageSource source)
	{
		if(super.isEntityInvulnerable(source))
			return true;
		
		if (source.getEntity() instanceof EntityPlayer || source.isUnblockable()){
			return false;
		}
	    return true;
	}



    public float getBlockPathWeight(BlockPos pos)
    {
        return 0.0F;
    }

    /**
     * Checks if the entity's current position is a valid location to spawn this entity.
     */
    public boolean getCanSpawnHere()
    {
        return true;
    }
    
    @Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(35.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.23000000417232513D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(3.0D);
	}
    
    @Override
    public int getMaxSpawnedInChunk() {
    	return 1;
    }


}
