package com.sakuraryoko.ap_scan.audio;

import java.util.UUID;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.NotNull;

import com.sakuraryoko.ap_scan.ApScan;
import com.sakuraryoko.ap_scan.audio.data.AudioDataWrapper;
import com.sakuraryoko.ap_scan.audio.v1.AudioDataLocationV1;

public record AudioDataLocationV2(UUID id, LocationType type, String desc, AudioDataWrapper data)
{
	public static AudioDataLocationV2 fromV1(AudioDataLocationV1 v1)
	{
		return new AudioDataLocationV2(UUID.fromString(v1.id()), v1.type(), v1.desc(),
		                               AudioDataWrapper.fromId(UUID.fromString(v1.id()), null)
		);
	}

	public static AudioDataLocationV2 fromJson(JsonElement element)
	{
		try
		{
			if (element.isJsonObject())
			{
				JsonObject obj = element.getAsJsonObject();

				UUID id = null;
				LocationType type = null;
				String desc = "";
				AudioDataWrapper data = null;

				if (obj.has("id"))
				{
					JsonElement idE = obj.get("id");

					if (idE.isJsonPrimitive())
					{
						JsonPrimitive idP = idE.getAsJsonPrimitive();

						if (idP.isString())
						{
							id = UUID.fromString(idE.getAsString());
						}
					}
				}
				if (obj.has("desc"))
				{
					desc = obj.get("desc").getAsString();
				}
				if (obj.has("type"))
				{
					type = LocationType.fromJson(obj.get("type"));
				}
				if (obj.has("data"))
				{
					try
					{
//						Method method = AudioData.class.getDeclaredMethod("fromJson", JsonObject.class);
//						method.setAccessible(true);
//						data = (AudioData) method.invoke(null, obj.get("data").getAsJsonObject());
						data = AudioDataWrapper.fromJson(obj.get("data"));
					}
					catch (Exception e)
					{
//						Throwable cause = (e instanceof java.lang.reflect.InvocationTargetException) ? e.getCause() : e;
						ApScan.LOGGER.error("AudioDataLocationV2#fromJson(): Exception parsing JSON audio data entry; {}", e.getLocalizedMessage());
						return null;
					}
				}

				if (id == null || type == null)
				{
					ApScan.LOGGER.error("AudioDataLocationV2#fromJson(): Exception parsing JSON entry; id or type is empty.");
					return null;
				}

				return new AudioDataLocationV2(id, type, desc, data);
			}
		}
		catch (Exception err)
		{
			ApScan.LOGGER.error("AudioDataLocationV2#fromJson(): Exception parsing JSON entry; {}", err.getLocalizedMessage());
		}

		return null;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}

		AudioDataLocationV2 that = (AudioDataLocationV2) o;
		return this.id.equals(that.id) && this.type == that.type && this.desc.equals(that.desc) && this.data.equals(that.data);
	}

	@Override
	public int hashCode()
	{
		int result = this.id.hashCode();
		result = 31 * result + this.type.hashCode();
		result = 31 * result + this.desc.hashCode();
		result = 31 * result + this.data.hashCode();
		return result;
	}

	@Override
	public @NotNull String toString()
	{
		return "AudioDataLocationV2[" +
				"{id=" + this.id + "}" +
				",{type=" + this.type.name() + "}" +
				",{desc=" + this.desc + "}" +
				",{data=" + this.data.asString() + "}" +
				"]";
	}
}
