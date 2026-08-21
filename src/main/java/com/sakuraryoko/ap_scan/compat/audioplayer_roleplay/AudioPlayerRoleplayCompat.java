package com.sakuraryoko.ap_scan.compat.audioplayer_roleplay;

import com.sakuraryoko.ap_scan.compat.ModIds;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.util.Optional;

// TODO
public class AudioPlayerRoleplayCompat
{
	private static final boolean isAudioPlayerRoleplayLoaded;
	private static final String version;

	static
	{
		Optional<ModContainer> opt = FabricLoader.getInstance().getModContainer(ModIds.audio_player_roleplay);

		isAudioPlayerRoleplayLoaded = opt.isPresent();

		if (isAudioPlayerRoleplayLoaded)
		{
			version = opt.get().getMetadata().getVersion().getFriendlyString();
		}
		else
		{
			version = "";
		}
	}

	public static boolean hasAudioPlayerRoleplay()
	{
		return isAudioPlayerRoleplayLoaded;
	}

	public static String getVersion()
	{
		return version;
	}
}
