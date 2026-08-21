package com.sakuraryoko.ap_scan.compat.audioplayer;

import com.sakuraryoko.ap_scan.compat.ModIds;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.util.Optional;

// TODO
public class AudioPlayerCompat
{
	private static final boolean isAudioPlayerLoaded;
	private static final String version;

	static
	{
		Optional<ModContainer> opt = FabricLoader.getInstance().getModContainer(ModIds.audio_player);

		isAudioPlayerLoaded = opt.isPresent();

		if (isAudioPlayerLoaded)
		{
			version = opt.get().getMetadata().getVersion().getFriendlyString();
		}
		else
		{
			version = "";
		}
	}

	public static boolean hasAudioPlayer()
	{
		return isAudioPlayerLoaded;
	}

	public static String getVersion()
	{
		return version;
	}
}
