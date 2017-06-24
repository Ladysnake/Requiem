package ladysnake.dissolution.common.entity;

import java.util.Random;
import java.util.UUID;

import javax.annotation.Nullable;

import ladysnake.dissolution.common.Reference;
import ladysnake.dissolution.common.capabilities.IIncorporealHandler;
import ladysnake.dissolution.common.capabilities.IncorporealDataHandler;
import ladysnake.dissolution.common.init.ModBlocks;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
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
import net.minecraft.init.Blocks;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
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
    public void onUpdate() {
    	
    		Random r = new Random();
    	
            float f = (float)Math.min(16, 4);
            
            BlockPos.MutableBlockPos blockpos = new BlockPos.MutableBlockPos(0, 0, 0);

            for (BlockPos.MutableBlockPos blockpos1 : BlockPos.getAllInBoxMutable(this.getPosition().add((double)(-f), -1.0D, (double)(-f)), this.getPosition().add((double)f, -2.0D, (double)f)))
            {
                if (blockpos1.distanceSqToCenter(this.posX, this.posY, this.posZ) <= (double)(f * f))
                {
                	blockpos.setPos(blockpos1.getX(), blockpos1.getY() + 1, blockpos1.getZ());
                    IBlockState iblockstate = this.world.getBlockState(blockpos);

                    if (iblockstate.getMaterial() == Material.AIR)
                    {
                        IBlockState iblockstate1 = this.world.getBlockState(blockpos1);
                        IBlockState iblockstate2 = this.world.getBlockState(blockpos1);

                        if (iblockstate1.getMaterial() == Material.WATER && ((Integer)iblockstate1.getValue(BlockLiquid.LEVEL)).intValue() == 0 && this.world.mayPlace(Blocks.FROSTED_ICE, blockpos1, false, EnumFacing.DOWN, (Entity)null))
                        {
                            this.world.setBlockState(blockpos1, Blocks.FROSTED_ICE.getDefaultState());
                            this.world.scheduleUpdate(blockpos1.toImmutable(), Blocks.FROSTED_ICE, MathHelper.getInt(this.getRNG(), 60, 120));
                        }
                        else if (iblockstate2.getMaterial() == Material.LAVA && ((Integer)iblockstate2.getValue(BlockLiquid.LEVEL)).intValue() == 0 && this.world.mayPlace(ModBlocks.DRIED_LAVA, blockpos1, false, EnumFacing.DOWN, (Entity)null))
                        {
                            this.world.setBlockState(blockpos1, ModBlocks.DRIED_LAVA.getDefaultState());
                            this.world.scheduleUpdate(blockpos1.toImmutable(), ModBlocks.DRIED_LAVA, MathHelper.getInt(this.getRNG(), 60, 120));
                        }
                        if (iblockstate1.getMaterial() == Material.GRASS)
                        {
                        	f = (float)Math.min(8, 2);
                        	if((int)r.nextInt(100) < 1){
                        		this.world.setBlockState(blockpos1, Blocks.DIRT.getDefaultState());
                        		for(int i = 0; i < 10; i++){
                    				Random rand = new Random();
                    				double motionX = rand.nextGaussian() * 0.05D;
                    				double motionY = rand.nextGaussian() * 0.05D;
                    				double motionZ = rand.nextGaussian() * 0.05D;
                    				this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, false, blockpos1.getX() ,  blockpos1.getY() + 1.0D, blockpos1.getZ(), motionX, motionY, motionZ, new int[0]);
                    			}
                        	}
                        }
                        
                    }
                }
            }
        
    	
    	super.onUpdate();
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
		
		if (source.getTrueSource() instanceof EntityPlayer || source.canHarmInCreative()){
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
