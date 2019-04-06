package ladysnake.dissolution.common.entity;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.Dynamic;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.world.World;

public class AwakenedPlayerShellEntity extends PlayerShellEntity {
    private static final ImmutableList<MemoryModuleType<?>> MEMORY_MODULES = ImmutableList.of(
            MemoryModuleType.HOME,
            MemoryModuleType.NEAREST_HOSTILE
    );
    private static final ImmutableList<SensorType<? extends Sensor<? super VillagerEntity>>> SENSORS = ImmutableList.of(
            SensorType.NEAREST_LIVING_ENTITIES,
            SensorType.NEAREST_PLAYERS,
            SensorType.HURT_BY
    );

    protected AwakenedPlayerShellEntity(EntityType<? extends PlayerShellEntity> type, World world) {
        super(type, world);
    }

    @Override
    protected Brain<?> createBrain(Dynamic<?> dynamic) {
        Brain<VillagerEntity> brain = new Brain<>(MEMORY_MODULES, SENSORS, dynamic);
        this.initBrain(brain);
        return brain;
    }

    private void initBrain(Brain<VillagerEntity> brain) {
        float speed = (float)this.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED).getValue();

    }

}
