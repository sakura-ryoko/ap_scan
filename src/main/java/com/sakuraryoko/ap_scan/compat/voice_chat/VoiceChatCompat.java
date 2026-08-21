package com.sakuraryoko.ap_scan.compat.voice_chat;

import com.sakuraryoko.ap_scan.compat.ModIds;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.util.Optional;

// TODO
public class VoiceChatCompat
{
	private static final boolean isVoiceChatLoaded;
	private static final String version;

	static
	{
		Optional<ModContainer> opt = FabricLoader.getInstance().getModContainer(ModIds.voice_chat);

		isVoiceChatLoaded = opt.isPresent();

		if (isVoiceChatLoaded)
		{
			version = opt.get().getMetadata().getVersion().getFriendlyString();
		}
		else
		{
			version = "";
		}
	}

	public static boolean hasVoiceChat()
	{
		return isVoiceChatLoaded;
	}

	public static String getVersion()
	{
		return version;
	}
}
