package ladysnake.requiem.core.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.Box;

import java.util.List;

/**
 * Simple mob detection helper thingy
 *
 * @author SciRave
 */
public class DetectionHelper {

    //Controls what is defined as a valid enemy for the system to anger.
    public static boolean isValidEnemy(Entity mob) {
        return mob instanceof HostileEntity && !(mob instanceof Angerable);
    }

    //Incites an individual mob to attack the host of a demon.
    public static void inciteMob(MobEntity host, HostileEntity mob) {
        mob.setTarget(host);
        mob.getBrain().remember(MemoryModuleType.ANGRY_AT, host.getUuid(), 600L);
    }

    //Incites an individual mob and their buddies in a range. Currently it is capped at 50 because no vanilla mob usually exceeds that.
    public static void inciteMobAndAllies(MobEntity host, HostileEntity mob) {
        inciteMob(host, mob);

        List<HostileEntity> sawExchange = mob.world.getEntitiesByClass(HostileEntity.class, Box.from(mob.getPos()).expand(50, 50, 50), hostileEntity -> isValidEnemy(hostileEntity) && hostileEntity.isInWalkTargetRange(host.getBlockPos()) && hostileEntity.canSee(host));

        sawExchange.forEach(hostileEntity -> inciteMob(host, hostileEntity));

    }

}
