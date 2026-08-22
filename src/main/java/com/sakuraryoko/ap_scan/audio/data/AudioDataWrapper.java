package com.sakuraryoko.ap_scan.audio.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nullable;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.tuple.Pair;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockBox;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import com.sakuraryoko.ap_scan.audio.data.modules.*;
import com.sakuraryoko.ap_scan.util.NbtView;

public class AudioDataWrapper
{
	public static final String AUDIO_PLAYER_TAG = "audioplayer";
	public static final String LEGACY_AUDIO_CUSTOM_SOUND = "CustomSound";
	public static final String LEGACY_AUDIO_CUSTOM_RANGE = "CustomSoundRange";
	public static final String LEGACY_AUDIO_STATIC_SOUND = "IsStaticCustomSound";
	public static final String LEGACY_AUDIO_RANDOM_SOUND = "CustomSoundRandomized";

	private final AudioModuleList list;
	private final ConcurrentHashMap<Identifier, JsonObject> others;

	public AudioDataWrapper()
	{
		this.list = new AudioModuleList();
		this.others = new ConcurrentHashMap<>();
	}

	public AudioModuleList modules()
	{
		return this.list;
	}

	@Nullable
	public JsonObject other(Identifier id)
	{
		if (this.others.containsKey(id))
		{
			return this.others.get(id);
		}

		return null;
	}

	@Nullable
	public static AudioDataWrapper fromJson(JsonElement ele)
	{
		if (ele == null || !ele.isJsonObject())
		{
			return null;
		}

		JsonObject obj = ele.getAsJsonObject();
		AudioDataWrapper data = new AudioDataWrapper();

		for (Map.Entry<String, JsonElement> entry : obj.entrySet())
		{
			Identifier id = Identifier.tryParse(entry.getKey());
			if (id == null)
			{
				continue;
			}

			AbstractAudioModule mod = AudioModuleTypes.fromJson(id, entry.getValue());

			if (mod == null && entry.getValue().isJsonObject())
			{
				data.others.put(id, entry.getValue().getAsJsonObject());
				continue;
			}

			if (mod != null && mod.isValid())
			{
				data.list.add(mod);
			}
		}

		if (data.list.isEmpty())
		{
			return null;
		}

		return data;
	}

	@Nullable
	public static AudioDataWrapper fromItem(ItemStack stack)
	{
		CustomData cd = stack.get(DataComponents.CUSTOM_DATA);

		if (cd != null)
		{
			CompoundTag tags = cd.copyTag();
			return fromNbt(tags);
		}

		return null;
	}

	@Nullable
	public static AudioDataWrapper fromNbt(CompoundTag tags)
	{
		if (tags.contains(AUDIO_PLAYER_TAG))
		{
			return fromString(tags.getStringOr(AUDIO_PLAYER_TAG, (String) null));
		}

		AudioDataWrapper data = new AudioDataWrapper();
		UUID id = null;
		Float range = null;
		Boolean isStatic = null;
		List<UUID> ids = new ArrayList<>();

		if (tags.contains(LEGACY_AUDIO_CUSTOM_SOUND))
		{
			id = tags.read(LEGACY_AUDIO_CUSTOM_SOUND, UUIDUtil.CODEC).orElse(null);
		}
		if (tags.contains(LEGACY_AUDIO_CUSTOM_RANGE))
		{
			range = tags.getFloatOr(LEGACY_AUDIO_CUSTOM_RANGE, (Float) null);
		}
		if (tags.contains(LEGACY_AUDIO_STATIC_SOUND))
		{
			isStatic = tags.getBooleanOr(LEGACY_AUDIO_STATIC_SOUND, (Boolean) null);
		}
		if (tags.contains(LEGACY_AUDIO_RANDOM_SOUND))
		{
			Codec<List<UUID>> CODEC = Codec.list(UUIDUtil.CODEC);
			ids = tags.read(LEGACY_AUDIO_RANDOM_SOUND, CODEC).orElse(null);
		}

		if (id != null)
		{
			data.list.add(BaseModule.create(id, range));
		}

		if (isStatic != null)
		{
			data.list.add(StaticModule.create());
		}

		if (ids != null)
		{
			data.list.add(RandomPlaybackModule.create(ids));
		}

		if (!data.list.isEmpty())
		{
			return data;
		}

		return null;
	}

	@Nullable
	public static AudioDataWrapper fromValue(NbtView view)
	{
		if (view.isReader())
		{
			CompoundTag tags = view.readNbt();

			if (tags != null)
			{
				return fromNbt(tags);
			}
		}

		return null;
	}

	@Nullable
	public static AudioDataWrapper fromString(@Nullable String json)
	{
		if (json == null || json.isEmpty())
		{
			return null;
		}

		try
		{
			JsonElement ele = JsonParser.parseString(json);
			if (ele == null || !ele.isJsonObject())
			{
				return null;
			}

			return fromJson(ele);
		}
		catch (Exception ignored) {}

		return null;
	}

	public static AudioDataWrapper fromId(UUID uuid, @Nullable Float range)
	{
		if (uuid == null)
		{
			return null;
		}
		AudioDataWrapper data = new AudioDataWrapper();
		data.list.add(BaseModule.create(uuid, range));
		return data;
	}

	@Nullable
	public UUID getId()
	{
		BaseModule mod = (BaseModule) this.list.get(BaseModule.TYPE);

		if (mod != null && mod.isValid())
		{
			return mod.getSoundId();
		}

		return null;
	}

	@Nullable
	public Float getRange()
	{
		BaseModule mod = (BaseModule) this.list.get(BaseModule.TYPE);

		if (mod != null && mod.isValid())
		{
			return mod.getRange();
		}

		return null;
	}

	public boolean isStatic()
	{
		StaticModule mod = (StaticModule) this.list.get(StaticModule.TYPE);
		return mod != null && mod.isValid();
	}

	public String getVolumeCategory()
	{
		VolumeCategoryModule mod = (VolumeCategoryModule) this.list.get(VolumeCategoryModule.TYPE);

		if (mod != null && mod.isValid())
		{
			return mod.getCategory();
		}

		return null;
	}

	public Pair<String, RegionModule.Type> getRegionId()
	{
		RegionModule mod = (RegionModule) this.list.get(RegionModule.TYPE);

		if (mod != null && mod.isValid())
		{
			String id = mod.regionId();
			RegionModule.Type mode = mod.type();

			if (id != null && mode != null)
			{
				return Pair.of(id, mode);
			}
		}

		return null;
	}

	public Pair<BlockBox, RegionModule.Type> getRegionBox()
	{
		RegionModule mod = (RegionModule) this.list.get(RegionModule.TYPE);

		if (mod != null && mod.isValid())
		{
			BlockBox box = mod.asBlockBox();
			RegionModule.Type mode = mod.type();

			if (box != null && mode != null)
			{
				return Pair.of(box, mode);
			}
		}

		return null;
	}

	public String getRandomPlaylist()
	{
		RandomPlaybackModule mod = (RandomPlaybackModule) this.list.get(RandomPlaybackModule.TYPE);

		if (mod != null && mod.isValid())
		{
			return mod.getPlaylistId();
		}

		return null;
	}

	public List<UUID> getRandomIds()
	{
		RandomPlaybackModule mod = (RandomPlaybackModule) this.list.get(RandomPlaybackModule.TYPE);

		if (mod != null && mod.isValid())
		{
			return mod.getIds();
		}

		return null;
	}

	public JsonObject toJson()
	{
		JsonObject obj = new JsonObject();

		if (!this.others.isEmpty())
		{
			for (Map.Entry<Identifier, JsonObject> entry : this.others.entrySet())
			{
				obj.add(entry.getKey().toString(), entry.getValue());
			}
		}

		ImmutableList<AbstractAudioModule> modules = this.list.getModules();

		modules.forEach(
				m ->
				{
					Identifier type = m.getId();
					JsonObject data = new JsonObject();
					m.toJson(data);

					if (!data.isEmpty())
					{
						obj.add(type.toString(), data);
					}
				}
		);

		return obj;
	}

	public String asString()
	{
		StringBuilder sb = new StringBuilder("AudioDataWrapper[");
		AtomicBoolean first = new AtomicBoolean(true);

		this.others.forEach(
				(key, o) ->
				{
					if (!first.get())
					{
						sb.append(",");
					}
					sb.append("{").append(key.toString()).append("=").append(o).append("}");
					if (first.get())
					{
						first.set(false);
					}
				}
		);

		first.set(true);

		ImmutableList<AbstractAudioModule> modules = this.list.getModules();

		modules.forEach(
				m ->
				{
					if (!first.get())
					{
						sb.append(",");
					}
					sb.append("{").append(m.asString()).append("}");
					if (first.get())
					{
						first.set(false);
					}
				}
		);

		sb.append("]");
		return sb.toString();
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
		AudioDataWrapper that = (AudioDataWrapper) o;

		if (this.list.size() != that.list.size())
		{
			return false;
		}
		if (this.others.size() != that.others.size())
		{
			return false;
		}

		if (this.list.equals(that.list))
		{
			return this.others.equals(that.others);
		}

		return false;
	}

	@Override
	public int hashCode()
	{
		int result = this.list.hashCode();
		result = 31 * result + this.others.hashCode();
		return result;
	}
}
