package ladysnake.tartaros.common;

public class Reference {
	public static final String MOD_ID = "tartaros";
	public static final String MOD_NAME = "Tartaros";
	public static final String VERSION = "1.0";
	public static final String MCVERSION = "[1.11.2]";
	
	public static final String CLIENT_PROXY_CLASS = "ladysnake.tartaros.client.proxy.ClientProxy";
	public static final String SERVER_PROXY_CLASS = "ladysnake.tartaros.server.proxy.ServerProxy";
	
	public static enum Items {
		ECTOPLASM("ectoplasm", "itemEctoplasm"),
		ECTOPLASMA("ectoplasma", "itemEctoplasma"),
		EYE_DEAD("eye_of_the_undead", "itemEyeDead"),
		SCYTHE_IRON("iron_scythe", "itemIronScythe"),
		SOULGEM("soul_gem", "itemSoulGem"),
		SOULINABOTTLE("soul_in_a_bottle", "itemSoulInABottle"),
		SEPULTURE("sepulture", "itemSepulture"),
		SEPULTUREFRAMING("sepulture_framing", "itemSepultureFraming");
		
		
		private String unlocalizedName;
		private String registryName;
		
		Items(String unlocalizedName, String registryName) {
			this.unlocalizedName = unlocalizedName;
			this.registryName = registryName;
		}
		
		public String getRegistryName() {
			return registryName;
		}
		
		public String getUnlocalizedName() {
			return unlocalizedName;
		}
	}

	public static enum Blocks {
		CRYSTALLIZER("crystallizer", "blockCrystallizer"),
		ECTOPLASM("ectoplasm_block", "blockEctoplasm"),
		ECTOPLASMA("ectoplasma_block", "blockEctoplasma"),
		SOUL_ANCHOR("soul_anchor", "blockSoulAnchor"),
		SOUL_EXTRACTOR("soul_extractor", "blockSoulExtractor"),
		MERCURIUS_WAYSTONE("mercurius_waystone", "blockMercuriusWaystone"),
		SEPULTURE("sepulture", "blockSepulture");
		
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
