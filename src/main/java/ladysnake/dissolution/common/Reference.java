package ladysnake.dissolution.common;

public class Reference {
	public static final String MOD_ID = "dissolution";
	static final String MOD_NAME = "Dissolution";
	static final String VERSION = "0.5";
	static final String MCVERSION = "[1.12]";
	static final double CONFIG_VERSION = 2.0;

	static final String CLIENT_PROXY_CLASS = "ladysnake.dissolution.client.proxy.ClientProxy";
	static final String SERVER_PROXY_CLASS = "ladysnake.dissolution.server.proxy.ServerProxy";

	public enum Items {
		CINNABAR,
		DEBUG("debug_item"),
		EYE_DEAD("eye_of_the_undead"),
		LURKING_SCYTHE,
		SCARAB_OF_ETERNITY,
		SCYTHE_IRON("iron_scythe"),
		SOUL_GEM("soul_gem"),
		SOUL_IN_A_BOTTLE("soul_in_a_bottle"),
		SEPULTURE;

		private String unlocalizedName;
		private String registryName;
		
		Items() {
			this.unlocalizedName = name().toLowerCase();
			this.registryName = unlocalizedName;
		}
		
		Items(String unlocalizedName) {
			this(unlocalizedName, unlocalizedName);
		}

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

	public enum Blocks {
		BASE_MACHINE,
		BARRAGE,
		CRYSTALLIZER,
		DRIED_LAVA,
		ECTOPLASM,
		ECTOPLASMA,
		MERCURY_CANDLE,
		MERCURIUS_WAYSTONE,
		POWER_CORE,
		POWER_CABLE,
		SEPULTURE,
		SOUL_ANCHOR,
		SOUL_EXTRACTOR,
		SULFUR_CANDLE;

		private String unlocalizedName;
		private String registryName;
		
		Blocks() {
			this.unlocalizedName = this.name().toLowerCase();
			this.registryName = unlocalizedName;
		}

		public String getRegistryName() {
			return this.registryName;
		}

		public String getUnlocalizedName() {
			return this.unlocalizedName;
		}
	}
}
