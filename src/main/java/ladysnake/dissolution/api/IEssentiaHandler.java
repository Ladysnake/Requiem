package ladysnake.dissolution.api;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import net.minecraft.util.EnumFacing;

public interface IEssentiaHandler extends Iterable<EssentiaStack> {

	/**
	 * @return the amount of suction this handler applies
	 */
	float getSuction();

	/**
	 * @return the type of essentia this handler's suction applies to
	 */
	EssentiaTypes getSuctionType();

	/**
	 * Sets the suction of this handler
	 * 
	 * @param suction
	 *            the amount of suction
	 * @param type
	 *            the type of essentia sucked in
	 */
	void setSuction(float suction, EssentiaTypes type);

	/**
	 * @param type
	 *            the type of essentia requested
	 * @return a stack equal to the content of this handler
	 */
	EssentiaStack readContent(EssentiaTypes type);

	/**
	 * @return the maximum amount of essentia this handler can contain per channel
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
	 * @param type
	 *            the type of essentia for the check
	 * @return true if there's no space left for this essentia type
	 */
	default boolean isFull(EssentiaTypes type) {
		return type == EssentiaTypes.UNTYPED ? isFull() : this.readContent(type).getCount() > this.getMaxSize();
	}

	/**
	 * @return true if there's no space left for any essentia
	 */
	default boolean isFull() {
		for (EssentiaStack stack : this) {
			if ((getChannels() < getMaxChannels() || this.getMaxSize() > stack.getCount()))
				return false;
		}
		return true;
	}

	/**
	 * 
	 * @param stack
	 * @return a stack containing the essentia that could not fit in
	 */
	EssentiaStack insert(EssentiaStack stack);

	/**
	 * @param amount
	 * @return a stack containing the extracted essentia
	 */
	EssentiaStack extract(int amount, EssentiaTypes type);

	/**
	 * Attempts to make essentia flow from this handler to the destination
	 * 
	 * @param dest
	 */
	default void flow(IEssentiaHandler dest) {
		if (dest.getSuction() > this.getSuction()) {
			StreamSupport.stream(this.spliterator(), false)
					.filter(e -> dest.getSuctionType() == EssentiaTypes.UNTYPED || dest.getSuctionType() == e.getType())
					.forEach(e -> this.insert(dest.insert(this.extract(1, e.getType()))));
			/*
			 * EssentiaStack in = this.extract(1, dest.getSuctionType()); 
			 * EssentiaStack out = dest.insert(in); 
			 * this.insert(out);
			 */
		}
	}

}