package com.sakuraryoko.ap_scan.audio;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import com.sakuraryoko.ap_scan.ApScan;

public record AudioDataLocation(String id, LocationType type, String desc)
{
	public static AudioDataLocation fromJson(JsonElement element)
	{
		try
		{
			if (element.isJsonObject())
			{
				JsonObject obj = element.getAsJsonObject();

				String id = "";
				LocationType type = null;
				String desc = "";

				if (obj.has("id"))
				{
					id = obj.get("id").getAsString();
				}
				if (obj.has("desc"))
				{
					desc = obj.get("desc").getAsString();
				}
				if (obj.has("type"))
				{
					type = LocationType.fromJson(obj.get("type"));
				}

				if (id.isEmpty() || type == null)
				{
					ApScan.LOGGER.error("AudioDataLocation#fromJson(): Exception parsing JSON entry; id or type is empty.");
					return null;
				}

				return new AudioDataLocation(id, type, desc);
			}
		}
		catch (Exception err)
		{
			ApScan.LOGGER.error("AudioDataLocation#fromJson(): Exception parsing JSON entry; {}", err.getLocalizedMessage());
		}

		return null;
	}

	@Override
	public @NotNull String toString()
	{
		return "AudioDataLocation[" +
				"{id=" + this.id + "}" +
				",{type=" + this.type.name() + "}" +
				",{desc=" + this.desc + "}" +
				"]";
	}
}
