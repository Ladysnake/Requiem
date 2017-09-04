package ladysnake.dissolution.common.blocks.alchemysystem;

public enum AlchemyModules {
	
	CONTAINER(1),
	PIPE(1),
	INTERFACE(1);
	
	public final int maxTier;

	private AlchemyModules(int maxTier) {
		this.maxTier = maxTier;
	}
	
}
