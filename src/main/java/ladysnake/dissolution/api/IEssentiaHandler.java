package ladysnake.dissolution.api;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public interface IEssentiaHandler {
	
	float getSuction();
	
	EssentiaTypes getSuctionType();
	
	void setSuction(float suction, EssentiaTypes type);
	
	/**
	 * @return a stack equal to the content of this handler
	 */
	EssentiaStack readContent();
	
	/**
	 * @return the maximum amount of essentia this handler can contain
	 */
	int getMaxSize();
	
	default boolean isFull() {
		return this.getMaxSize() <= this.readContent().getCount();
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
	EssentiaStack extract(int amount);
	
	/**
	 * Attempts to make essentia flow from this handler to the destination
	 * @param dest
	 */
	default void flow(IEssentiaHandler dest) {
		if(dest.getSuction() > this.getSuction() && (dest.getSuctionType() == EssentiaTypes.UNTYPED || dest.getSuctionType() == this.getSuctionType())) {
			EssentiaStack in = this.extract(1);
			EssentiaStack out = dest.insert(in);
			this.insert(out);
		}
	}
	
	/**
	 * Checks if the block at this position accepts a connection
	 * @param pos the position of the block being checked
	 * @param facing the face on which the connection is attempted
	 * @return true if there should be a connection
	 */
	default boolean shouldEssentiaConnect(EnumFacing facing) {
		return true;
	}
}