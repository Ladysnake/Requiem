package ladysnake.dissolution.api;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityVillager;

import java.util.Arrays;
import java.util.List;

public enum SoulTypes {

    BENIGN(EntityAnimal.class),
    DRACONIC(EntityDragon.class),
    DOOMED(EntityWither.class),
    IMMUTABLE(EntityGolem.class),
    UNHINGED(EntityCreeper.class, EntityEnderman.class),
    PREDATORY(EntityMob.class),
    WISE(EntityVillager.class),
    UNTYPED;

    static {
        byte idCount = 0;
        for (SoulTypes s : SoulTypes.values())
            s.id = idCount++;
    }

    private List<Class<? extends EntityLiving>> sources;
    private byte id;

    SoulTypes(Class<? extends EntityLiving>... sources) {
        this.sources = Arrays.asList(sources);
    }

    public List<Class<? extends EntityLiving>> getSources() {
        return this.sources;
    }

    public Byte getId() {
        return id;
    }

    public static SoulTypes getById(byte id) {
        for (SoulTypes soul : SoulTypes.values())
            if (soul.id == id)
                return soul;
        return SoulTypes.UNTYPED;
    }

    public static SoulTypes getSoulFor(EntityLiving entityIn) {
        for (SoulTypes soul : SoulTypes.values()) {
            for (Class<? extends EntityLiving> cl : soul.getSources()) {
                if (cl.isInstance(entityIn))
                    return soul;
            }
        }
        return SoulTypes.UNTYPED;
    }

}
