package ladysnake.dissolution.api;

import java.util.stream.StreamSupport;

public interface IDistillateHandler extends Iterable<DistillateStack> {

    /**
     * @return the amount of suction this handler applies
     */
    float getSuction(DistillateTypes type);

    /**
     * Sets the suction of this handler
     *
     * @param suction the amount of suction
     * @param type    the type of distillate sucked in
     */
    void setSuction(DistillateTypes type, float suction);

    /**
     * @param type the type of distillate requested
     * @return a stack equal to the content of this handler
     */
    DistillateStack readContent(DistillateTypes type);

    /**
     * @return the maximum amount of distillate this handler can contain per channel
     */
    int getMaxSize();

    /**
     * @return the number of channels currently used by this handler
     */
    int getChannels();

    /**
     * @return the maximum number of channels this handler can sustain
     */
    int getMaxChannels();

    /**
     * @param type the type of distillate for the check
     * @return true if there's no space left for this distillate type
     */
    default boolean isFull(DistillateTypes type) {
        return type == DistillateTypes.UNTYPED ? isFull() : this.readContent(type).getCount() > this.getMaxSize();
    }

    /**
     * @return true if there's no space left for any distillate
     */
    default boolean isFull() {
        for (DistillateStack stack : this) {
            if ((getChannels() < getMaxChannels() || this.getMaxSize() > stack.getCount()))
                return false;
        }
        return true;
    }

    /**
     * @param stack
     * @return a stack containing the distillate that could not fit in
     */
    DistillateStack insert(GenericStack<DistillateTypes> stack);

    /**
     * @param amount
     * @return a stack containing the extracted distillate
     */
    DistillateStack extract(int amount, DistillateTypes type);

    /**
     * Attempts to flush everything in the passed handler
     *
     * @param dest the receiving handler
     */
    default void flow(IDistillateHandler dest) {
        StreamSupport.stream(this.spliterator(), false)
                .map(DistillateStack::getType)
                .filter(type -> dest.getSuction(type) > this.getSuction(type))
                .forEach(type -> this.insert(dest.insert(this.extract(1, type))));
    }

    /**
     * Attempts to make distillate flow from this handler to the destination
     */
    default void flow(IDistillateHandler dest, DistillateTypes type) {
        if (dest.getSuction(type) > this.getSuction(type))
            this.insert(dest.insert(this.extract(1, type)));
    }

}