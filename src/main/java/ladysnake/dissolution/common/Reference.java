package ladysnake.dissolution.common;

public class Reference {
	public static final String MOD_ID = "dissolution";
	static final String MOD_NAME = "Dissolution";
	static final String VERSION = "0.5.1";
	static final String MCVERSION = "[1.12]";
	static final String DEPENDENCIES = "after:albedo;after:baubles;";
	static final double CONFIG_VERSION = 2.0;

	static final String CLIENT_PROXY_CLASS = "ladysnake.dissolution.client.proxy.ClientProxy";
	static final String SERVER_PROXY_CLASS = "ladysnake.dissolution.server.proxy.ServerProxy";

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
