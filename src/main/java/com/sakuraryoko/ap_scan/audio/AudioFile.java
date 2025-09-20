package com.sakuraryoko.ap_scan.audio;

import com.google.gson.JsonElement;
import org.jetbrains.annotations.Nullable;

import com.sakuraryoko.ap_scan.ApScan;

public class AudioFile
{
	private final String id;
	private final String name;

	public AudioFile(String id, String name)
	{
		this.id = id;
		this.name = name;
	}

	public String getId() { return this.id; }

	public String getName() { return this.name; }

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
	public String toString()
	{
		return "AudioFile[" +
				"{id="+this.id+"}" +
				",{name="+this.name+"}" +
				"]";
	}
}
