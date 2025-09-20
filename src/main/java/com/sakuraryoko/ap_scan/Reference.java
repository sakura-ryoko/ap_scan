package com.sakuraryoko.ap_scan;

import net.fabricmc.loader.api.FabricLoader;

public class Reference
{
	public static final String MOD_ID = "ap_scan";
	public static final String MOD_NAME = "Audio Player Scan";
	public static final String MOD_VERSION = FabricLoader.getInstance().getModContainer(MOD_ID).get().getMetadata().getVersion().getFriendlyString();
	public static final boolean DEBUG = true;
}
