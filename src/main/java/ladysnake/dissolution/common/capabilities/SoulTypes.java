package ladysnake.dissolution.common.capabilities;

import java.util.List;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityVillager;
import scala.actors.threadpool.Arrays;

public enum SoulTypes {

	BENIGN(EntityAnimal.class), 
	DRACONIC(EntityDragon.class),
	DOOMED(EntityWither.class), 
	IMMUTABLE(EntityGolem.class),
	UNHINGED(EntityCreeper.class, EntityEnderman.class),
	PREDATORY(EntityMob.class), 
	WISE(EntityVillager.class), 
	UNTYPED;
	
	private List<Class<? extends EntityLiving>> sources;
	
	SoulTypes(Class<? extends EntityLiving>... sources) {
		this.sources = Arrays.asList(sources);
	}
	
	public List<Class<? extends EntityLiving>> getSources () {
		return this.sources;
	}
	
	public static SoulTypes getSoulFor(EntityLiving entityIn) {
		for(SoulTypes soul : SoulTypes.values()) {
			for(Class<? extends EntityLiving> cl : soul.getSources()) {
				if(cl.isInstance(entityIn))
					return soul;
			}
		}
		return SoulTypes.UNTYPED;
	}

}
