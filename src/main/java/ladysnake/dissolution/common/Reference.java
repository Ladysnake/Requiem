package ladysnake.dissolution.common;

import ladylib.LadyLib;

public class Reference {
    public static final String MOD_ID = "dissolution";
    public static final String MOD_NAME = "Dissolution";
    static final String VERSION = "@VERSION@";
    static final String MCVERSION = "[1.12.2]";
    static final String DEPENDENCIES =
            "required-after:forge@[14.23.3.2665,);" +
                    "required-after:" + LadyLib.MOD_ID + "@" + LadyLib.VERSION + ";" +
            "after:albedo;" +
            "after:baubles;" +
            "after:thaumcraft;";
    public static final double CONFIG_VERSION = 4.0;

    static final String CLIENT_PROXY_CLASS = "ladysnake.dissolution.client.ClientProxy";
    static final String SERVER_PROXY_CLASS = "ladysnake.dissolution.common.init.CommonProxy";
    static final String GUI_FACTORY_CLASS = "ladysnake.dissolution.client.config.DissolutionGuiFactory";

}
