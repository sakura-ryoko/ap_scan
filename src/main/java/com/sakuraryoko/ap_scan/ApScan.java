package com.sakuraryoko.ap_scan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;

public class ApScan implements ModInitializer
{
	public static final Logger LOGGER = LoggerFactory.getLogger(Reference.MOD_ID);

	@Override
	public void onInitialize()
	{
	}

	public static void debugLog(String str, Object... args)
	{
		if (Reference.DEBUG)
		{
			LOGGER.info(str, args);
		}
	}
}