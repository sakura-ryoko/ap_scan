package com.sakuraryoko.ap_scan.audio;

import javax.annotation.Nullable;
import com.google.gson.JsonElement;

import com.sakuraryoko.ap_scan.ApScan;

public enum LocationType
{
	SKULL,
	TILE_ENTITY,
	ENTITY,
	PLAYER_INVENTORY,
	PLAYER_ENDER_CHEST,
	;

	public static @Nullable LocationType fromJson(JsonElement element)
	{
		try
		{
			if (element.isJsonPrimitive())
			{
				return fromString(element.getAsString());
			}
		}
		catch (Exception err)
		{
			ApScan.LOGGER.error("LocationType#fromJson(): Exception parsing JSON entry; {}", err.getLocalizedMessage());
		}

		return null;
	}

	public static @Nullable LocationType fromString(String input)
	{
		for (LocationType entry : values())
		{
			if (entry.name().equalsIgnoreCase(input))
			{
				return entry;
			}
		}

		return null;
	}

	@Override
	public String toString()
	{
		return "LocationType[" + this.name().toLowerCase() + "]";
	}
}
