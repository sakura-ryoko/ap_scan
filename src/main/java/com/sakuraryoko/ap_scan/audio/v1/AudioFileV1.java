package com.sakuraryoko.ap_scan.audio.v1;

import com.google.gson.JsonElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.sakuraryoko.ap_scan.ApScan;

@Deprecated
public record AudioFileV1(String id, String name)
{
	@Nullable
	public static AudioFileV1 fromJson(String key, JsonElement element)
	{
		try
		{
			if (element.isJsonPrimitive())
			{
				return new AudioFileV1(key, element.getAsString());
			}
		}
		catch (Exception err)
		{
			ApScan.LOGGER.error("AudioFile#fromJson(): Exception parsing JSON entry; {}", err.getLocalizedMessage());
		}

		return null;
	}

	@Override
	public @NotNull String toString()
	{
		return "AudioFile[" +
				"{id=" + this.id + "}" +
				",{name=" + this.name + "}" +
				"]";
	}
}
