package com.sakuraryoko.ap_scan.audio;

import java.util.Objects;
import java.util.UUID;
import com.google.gson.JsonElement;
import de.maxhenkel.audioplayer.audioloader.Metadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.sakuraryoko.ap_scan.ApScan;
import com.sakuraryoko.ap_scan.audio.v1.AudioFileV1;
import com.sakuraryoko.ap_scan.util.MetadataUtils;

public record AudioFileV2(UUID id, Metadata meta)
{
	public static AudioFileV2 fromV1(AudioFileV1 file)
	{
		UUID uuid = UUID.fromString(file.id());
		Metadata meta = new Metadata(uuid);
		meta.setFileName(file.name());
		return new AudioFileV2(uuid, meta);
	}

	@Nullable
	public static AudioFileV2 fromJson(UUID key, JsonElement element)
	{
		try
		{
			if (element.isJsonObject())
			{
				AudioFileV2 result = new AudioFileV2(key, Metadata.fromJson(key, element.getAsJsonObject()));
//				ApScan.LOGGER.error("AudioFileV2:fromJson() -> {}", result.toString());
				return result;
			}
		}
		catch (Exception err)
		{
			ApScan.LOGGER.error("AudioFileV2#fromJson(): Exception parsing JSON entry; {}", err.getLocalizedMessage());
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
		AudioFileV2 that = (AudioFileV2) o;
		return Objects.equals(this.id, that.id) && Objects.equals(this.meta, that.meta);
	}

	@Override
	public int hashCode()
	{
		int result = this.id.hashCode();
		result = 31 * result + this.meta.hashCode();
		return result;
	}

	@Override
	public @NotNull String toString()
	{
		if (this.meta != null)
		{
			return "AudioFileV2[" + "{id=" + this.id.toString() + "},[meta=" + MetadataUtils.toString(this.meta) + "]";
		}
		else
		{
			return "AudioFileV2[" + "{id=" + this.id.toString() + "}";
		}
	}
}
