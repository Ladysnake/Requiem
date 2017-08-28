package ladysnake.dissolution.client.particles;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.player.EntityPlayer;

public interface IDissolutionParticle {
	
	boolean isAdditive();
	
	default boolean renderThroughBlocks() {
		return false;
	}

}
