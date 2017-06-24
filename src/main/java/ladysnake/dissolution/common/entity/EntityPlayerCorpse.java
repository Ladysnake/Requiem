package ladysnake.dissolution.common.entity;

import java.util.UUID;

import com.google.common.base.Optional;
import com.mojang.authlib.GameProfile;

import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;

public class EntityPlayerCorpse extends EntityMinion {
	
	public DataParameter<Optional<UUID>> player = EntityDataManager.<Optional<UUID>>createKey(EntityPlayerCorpse.class, DataSerializers.OPTIONAL_UNIQUE_ID);
	
	public EntityPlayerCorpse(World worldIn) {
		super(worldIn);
	}

	@Override
	public void setSwingingArms(boolean swingingArms) {
		
	}

	@Override
	protected void initEntityAI() {
		
	}
	
	@Override
	protected void update() {
		// TODO something I guess ?
	}
	
	
	public void setTarget(UUID id) {
		
        this.getDataManager().set(player, Optional.of(id));
        
    }
	
    public UUID getUUID() {
        if(this.getDataManager().get(player).isPresent())
        	
            return this.getDataManager().get(player).get();
        
        return null;
    }
    
	@Override
    protected void entityInit() {
        this.getDataManager().register(player, Optional.absent());
    }
}
