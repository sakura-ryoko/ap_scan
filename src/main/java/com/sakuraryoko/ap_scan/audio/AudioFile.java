package com.sakuraryoko.ap_scan.audio;

import com.google.gson.JsonElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.sakuraryoko.ap_scan.ApScan;

public record AudioFile(String id, String name)
{
	@Nullable
	public static AudioFile fromJson(String key, JsonElement element)
	{
		try
		{
			if (element.isJsonPrimitive())
			{
				return new AudioFile(key, element.getAsString());
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
