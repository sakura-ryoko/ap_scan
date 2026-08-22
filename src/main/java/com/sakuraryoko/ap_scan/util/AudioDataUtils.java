package com.sakuraryoko.ap_scan.util;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.maxhenkel.audioplayer.audioloader.AudioData;
import org.apache.commons.lang3.tuple.Pair;
import xyz.breadloaf.audioplayerroleplay.modules.CustomVolumeCategory.CustomVolumeCategory;
import xyz.breadloaf.audioplayerroleplay.modules.CustomVolumeCategory.VolumeCategoryModule;
import xyz.breadloaf.audioplayerroleplay.modules.RandomizedPlayback.RandomizedPlayback;
import xyz.breadloaf.audioplayerroleplay.modules.RandomizedPlayback.RandomizedSoundModule;
import xyz.breadloaf.audioplayerroleplay.modules.Regions.Region;
import xyz.breadloaf.audioplayerroleplay.modules.Regions.RegionDataModule;
import xyz.breadloaf.audioplayerroleplay.modules.Regions.RegionMode;
import xyz.breadloaf.audioplayerroleplay.modules.Regions.RegionsModule;
import xyz.breadloaf.audioplayerroleplay.modules.StaticPlayback.StaticPlayback;
import xyz.breadloaf.audioplayerroleplay.modules.StaticPlayback.StaticPlaybackModule;

import net.minecraft.core.BlockBox;
import net.minecraft.core.BlockPos;

@Deprecated(forRemoval = true)
public class AudioDataUtils
{
	public static String toString(AudioData data)
	{
		StringBuilder builder = new StringBuilder("AudioData[");

		if (data != null)
		{
			Float range = data.getRange();
			boolean isStatic = getStaticPlayback(data);
			String category = getVolumeCategory(data);
			Pair<Region, RegionMode> region = getRegion(data);
			List<UUID> rng = getRngUUIDs(data);
			boolean append = false;

			if (range != null)
			{
				builder.append("{range=").append(range.toString()).append("}");
				append = true;
			}
			if (isStatic)
			{
				if (append)
				{
					builder.append(",");
				}
				builder.append("{static=true}");
				append = true;
			}
			if (category != null)
			{
				if (append)
				{
					builder.append(",");
				}
				builder.append("{category=").append(category).append("}");
				append = true;
			}
			if (region != null)
			{
				if (append)
				{
					builder.append(",");
				}
				builder.append("{region=[");

				BlockBox bb = getRegionAsBox(region.getLeft());
				String id = getRegionAsId(region.getLeft());
				boolean append2 = false;

				if (bb != null)
				{
					builder.append("{min=").append(bb.min().toShortString()).append("},{max=").append(bb.max().toShortString()).append("}");
					append2 = true;
				}
				else if (id != null)
				{
					builder.append("{id=").append(id).append("}");
					append2 = true;
				}

				RegionMode mode = region.getRight();

				if (mode != null)
				{
					if (append2)
					{
						builder.append(",");
					}
					builder.append("{mode=").append(mode.toString()).append("}");
				}

				builder.append("]");
				append = true;
			}
			if (rng != null && !rng.isEmpty())
			{
				if (append)
				{
					builder.append(",");
				}

				builder.append("{rng=[");
				boolean first = true;

				for (UUID uuid : rng)
				{
					if (!first)
					{
						builder.append(",");
					}
					builder.append("{id=").append(uuid.toString()).append("}");

					if (first)
					{
						first = false;
					}
				}
			}
		}

		builder.append("]");
		return builder.toString();
	}

	public static boolean getStaticPlayback(@Nonnull AudioData data)
	{
		try
		{
			StaticPlaybackModule staticModule = data.getModule(StaticPlayback.STATIC_PLAYBACK_MODULE).orElse(null);
			return staticModule != null;
		}
		catch (Exception ignored)
		{
		}

		return false;
	}

	@Nullable
	public static List<UUID> getRngUUIDs(@Nonnull AudioData data)
	{
		try
		{
			RandomizedSoundModule rngModule = data.getModule(RandomizedPlayback.RANDOM_PLAYBACK_MODULE).orElse(null);

			if (rngModule != null)
			{
				return rngModule.getSoundIds();
			}
		}
		catch (Exception ignored)
		{
		}

		return null;
	}

	@Nullable
	public static Pair<Region, RegionMode> getRegion(@Nonnull AudioData data)
	{
		try
		{
			RegionDataModule regionsModule = data.getModule(RegionsModule.REGIONS_DATA_MODULE).orElse(null);

			if (regionsModule != null)
			{
				return Pair.of(regionsModule.region, regionsModule.regionMode);
			}
		}
		catch (Exception ignored)
		{
		}

		return null;
	}

	@Nullable
	public static BlockBox getRegionAsBox(@Nonnull Region region)
	{
		JsonElement ele = region.toJson();

		if (ele.isJsonArray())
		{
			JsonArray arr = ele.getAsJsonArray();

			if (arr.size() == 6)
			{
				int minX = arr.get(0).getAsInt();
				int minY = arr.get(1).getAsInt();
				int minZ = arr.get(2).getAsInt();
				int maxX = arr.get(3).getAsInt();
				int maxY = arr.get(4).getAsInt();
				int maxZ = arr.get(5).getAsInt();

				BlockPos pos1 = new BlockPos(minX, minY, minZ);
				BlockPos pos2 = new BlockPos(maxX, maxY, maxZ);

				return new BlockBox(pos1, pos2);
			}
		}

		return null;
	}

	@Nullable
	public static String getRegionAsId(@Nonnull Region region)
	{
		JsonElement ele = region.toJson();

		if (ele.isJsonPrimitive())
		{
			return ele.getAsString();
		}

		return null;
	}

	@Nullable
	public static String getVolumeCategory(@Nonnull AudioData data)
	{
		try
		{
			VolumeCategoryModule volumeModule = data.getModule(CustomVolumeCategory.CUSTOM_VOLUME_CATEGORY_MODULE).orElse(null);

			if (volumeModule != null)
			{
				try
				{
					JsonObject obj = new JsonObject();
					volumeModule.save(obj);

					if (obj.has("id"))
					{
						return obj.getAsJsonPrimitive("id").getAsString();
					}
				}
				catch (Exception ignored)
				{
				}
			}
		}
		catch (Exception ignored)
		{
		}

		return null;
	}
}
