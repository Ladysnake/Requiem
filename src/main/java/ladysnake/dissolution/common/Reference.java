package ladysnake.dissolution.common;

import net.minecraft.block.ITileEntityProvider;

public class Reference {
	public static final String MOD_ID = "dissolution";
	public static final String MOD_NAME = "Dissolution";
	public static final String VERSION = "0.4.2.2";
	public static final String MCVERSION = "[1.12]";
	
//	public static final String GUI_FACTORY = "ladysnake.dissolution.common.inventory.TartarosGuiFactory";	
	public static final String CLIENT_PROXY_CLASS = "ladysnake.dissolution.client.proxy.ClientProxy";
	public static final String SERVER_PROXY_CLASS = "ladysnake.dissolution.server.proxy.ServerProxy";
	
	public static enum Items {
		BASE_RESOURCE("base_resource", "itemResource"),
		DEBUG("debug_item", "itemDebug"),
		ECTOPLASM("ectoplasm", "itemEctoplasm"),
		ECTOPLASMA("ectoplasma", "itemEctoplasma"),
		EYE_DEAD("eye_of_the_undead", "itemEyeOfUndead"),
		GRAND_FAUX("grand_faux", "itemGrandFaux"),
		SCARAB_OF_ETERNITY("scarab_of_eternity", "itemScarabOfEternity"),
		SCYTHE_IRON("iron_scythe", "itemIronScythe"),
		SOULGEM("soul_gem", "itemSoulGem"),
		SOULINABOTTLE("soul_in_a_bottle", "itemSoulInABottle"),
		SEPULTURE("sepulture", "itemSepulture");

		
		
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
		BASE_MACHINE("base_machine", "baseMachine"),
		CRYSTALLIZER("crystallizer", "blockCrystallizer"),
		DRIED_LAVA("dried_lava", "blockDriedLava"),
		ECTOPLASM("ectoplasm_block", "blockEctoplasm"),
		ECTOPLASMA("ectoplasma_block", "blockEctoplasma"),
		MERCURY_CANDLE("mercury_candle", "blockMercuryCandle"),
		MERCURIUS_WAYSTONE("mercurius_waystone", "blockMercuriusWaystone"),
		POWER_CORE("power_core", "powerCore"),
		POWER_CABLE("power_cable", "powerCable"),
		SEPULTURE("sepulture", "blockSepulture"),
		SOUL_ANCHOR("soul_anchor", "blockSoulAnchor"),
		SOUL_EXTRACTOR("soul_extractor", "blockSoulExtractor"),
		SULFUR_CANDLE("sulfur_candle", "blockSulfurCandle");

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
