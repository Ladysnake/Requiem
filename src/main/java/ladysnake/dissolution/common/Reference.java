package ladysnake.dissolution.common;

import net.minecraft.block.ITileEntityProvider;

public class Reference {
	public static final String MOD_ID = "dissolution";
	public static final String MOD_NAME = "Dissolution";
	public static final String VERSION = "0.5";
	public static final String MCVERSION = "[1.12]";
	public static final double CONFIG_VERSION = 2.0;

	public static final String CLIENT_PROXY_CLASS = "ladysnake.dissolution.client.proxy.ClientProxy";
	public static final String SERVER_PROXY_CLASS = "ladysnake.dissolution.server.proxy.ServerProxy";

	public static enum Items {
		BASE_RESOURCE,
		DEBUG("debug_item"),
		EYE_DEAD("eye_of_the_undead"),
		GRAND_FAUX,
		SCARAB_OF_ETERNITY,
		SCYTHE_IRON("iron_scythe"),
		SOULGEM("soul_gem"),
		SOULINABOTTLE("soul_in_a_bottle"),
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

	public static enum Blocks {
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
		
		Blocks(String unlocalizedName) {
			this(unlocalizedName, unlocalizedName);
		}

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
