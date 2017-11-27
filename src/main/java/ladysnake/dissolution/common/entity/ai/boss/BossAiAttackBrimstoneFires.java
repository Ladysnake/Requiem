package ladysnake.dissolution.common.entity.ai.boss;

import ladysnake.dissolution.common.entity.boss.EntityBrimstoneFire;
import ladysnake.dissolution.common.entity.boss.EntityMawOfTheVoid;
import net.minecraft.entity.ai.EntityAIBase;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class BossAiAttackBrimstoneFires extends EntityAIBase {

    public static final int ATTACK_ID = 1;
    public static final int MAX_FIRES = 20;
    private static final int PAUSE = 5;

    protected EntityMawOfTheVoid attacker;
    /**
     * The amount of ticks this attack has been active
     */
    protected int attackTicks;
    /**
     * The max duration for this attack <br> not actually used
     */
    protected int maxAttackDuration;
    private List<EntityBrimstoneFire> allFires;

    public BossAiAttackBrimstoneFires(EntityMawOfTheVoid entity, List<EntityBrimstoneFire> allFires) {
        this.setMutexBits(0b0111);
        this.allFires = allFires;
        this.attacker = entity;
    }

    @Override
    public boolean shouldExecute() {
        return attacker.lastAttack != ATTACK_ID;
    }

    @Override
    public void startExecuting() {
        attacker.lastAttack = ATTACK_ID;
        System.out.println("starting brimstone attack");
    }

    @Override
    public boolean shouldContinueExecuting() {
        return attackTicks <= (MAX_FIRES + 1) * PAUSE;
    }

    @Override
    public void updateTask() {
        if (++this.attackTicks % PAUSE == 0) {
            System.out.println("number of fires : " + allFires.size());
            System.out.println("updating brimstone attack (" + attackTicks + "/" + (MAX_FIRES * PAUSE) + ")");
            if (allFires.size() < MAX_FIRES) {
                EntityBrimstoneFire fire = new EntityBrimstoneFire(this.attacker.world);
                Random rand = new Random();
                fire.posX = this.attacker.posX + rand.nextGaussian() * 5;
                fire.posY = this.attacker.posY + rand.nextGaussian() * 5;
                fire.posZ = this.attacker.posZ + rand.nextGaussian() * 5;
                if (!allFires.isEmpty())
                    allFires.get(allFires.size() - 1).setTarget(fire.getPosition());
                this.attacker.world.spawnEntity(fire);
                allFires.add(fire);
                System.out.print(" : spawned fire");
            } else if (allFires.size() >= MAX_FIRES) {
                Iterator<EntityBrimstoneFire> i = allFires.iterator();
                while (i.hasNext()) {
                    i.next().fire();
                    i.remove();
                }
                System.out.print(" : removed fires");
            }
            System.out.print("\n");
        }
    }

    @Override
    public void resetTask() {
        System.out.println("reset brimstone attack");
        this.attackTicks = 0;
    }

}
