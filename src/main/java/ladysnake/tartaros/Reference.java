package ladysnake.tartaros;

public class Reference {
	public static final String MOD_ID = "tartaros";
	public static final String MOD_NAME = "Tartaros";
	public static final String VERSION = "1.0";
	public static final String MCVERSION = "[1.11.2]";
	
	public static final String CLIENT_PROXY_CLASS = "ladysnake.tartaros.proxy.ClientProxy";
	public static final String SERVER_PROXY_CLASS = "ladysnake.tartaros.proxy.ServerProxy";

	public static enum Blocks {
		CRYSTALLIZER("crystallizer", "blockCrystallizer");
		
		private String unlocalizedName;
		private String registryName;
		
		Blocks(String unlocalizedName, String registryName) {
			this.unlocalizedName = unlocalizedName;
			this.registryName = registryName;
		}
		
		public String getRegistryName() {
			return this.registryName;
		}
		
		public String getUnlocalizedName() {
			return this.unlocalizedName;
		}
	}
}
