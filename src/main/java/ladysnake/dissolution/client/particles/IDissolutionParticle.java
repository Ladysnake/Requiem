package ladysnake.dissolution.client.particles;

public interface IDissolutionParticle {

    boolean isAdditive();

    default boolean renderThroughBlocks() {
        return false;
    }

}
