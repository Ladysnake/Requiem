package ladysnake.dissolution.common.entity;

import com.mojang.authlib.GameProfile;

import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;

public class EntityPlayerCorpse extends EntityMinion {
	
	public EntityPlayerCorpse(World worldIn) {
		super(worldIn);
	}

	private boolean inert;

	@Override
	public void setCorpse(boolean inert) {
		this.inert = inert;
	}

	@Override
	public boolean isCorpse() {
		return inert;
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

}
