package com.sakuraryoko.ap_scan;

import com.sakuraryoko.ap_scan.compat.audioplayer.AudioPlayerCompat;
import com.sakuraryoko.ap_scan.compat.audioplayer_roleplay.AudioPlayerRoleplayCompat;
import com.sakuraryoko.ap_scan.compat.voice_chat.VoiceChatCompat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;

public class ApScan implements ModInitializer
{
	public static final Logger LOGGER = LoggerFactory.getLogger(Reference.MOD_ID);

	@Override
	public void onInitialize()
	{
		LOGGER.info("VC: {}, AP: {}, APR: {}", VoiceChatCompat.getVersion(), AudioPlayerCompat.getVersion(), AudioPlayerRoleplayCompat.getVersion());
	}

	public static void debugLog(String str, Object... args)
	{
		if (Reference.DEBUG)
		{
			final String msg = "[DEBUG]: " + str;
			LOGGER.info(msg, args);
		}
	}
}