package com.sakuraryoko.ap_scan.audio.data;

import java.util.Objects;
import javax.annotation.Nullable;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.resources.Identifier;

import com.sakuraryoko.ap_scan.audio.data.modules.*;

public class AudioModuleTypes
{
	public static final ImmutableMap<Identifier, AudioModuleType<?>> REGISTRY;

	public static final AudioModuleType<BaseModule>             BASE    = AudioModuleType.build(BaseModule.TYPE, BaseModule::new);
	public static final AudioModuleType<StaticModule>           STATIC  = AudioModuleType.build(StaticModule.TYPE, StaticModule::new);
	public static final AudioModuleType<VolumeCategoryModule>   VOLUME  = AudioModuleType.build(VolumeCategoryModule.TYPE, VolumeCategoryModule::new);
	public static final AudioModuleType<RegionModule>           REGION  = AudioModuleType.build(RegionModule.TYPE, RegionModule::new);
	public static final AudioModuleType<RandomPlaybackModule>   RANDOM  = AudioModuleType.build(RandomPlaybackModule.TYPE, RandomPlaybackModule::new);

	@Nullable
	public static AbstractAudioModule create(Identifier id)
	{
		if (REGISTRY.containsKey(id))
		{
			return Objects.requireNonNull(REGISTRY.get(id)).init(id);
		}

		return null;
	}

	@Nullable
	public static AbstractAudioModule fromJson(Identifier type, JsonElement json)
	{
		if (json == null || !json.isJsonObject())
		{
			return null;
		}

		AbstractAudioModule result = create(type);

		if (result != null && json.isJsonObject())
		{
			JsonObject obj = json.getAsJsonObject();

//			System.out.printf("[MODULE] fromJson/%s: -> %s\n", type.toString(), obj.toString());
			result.fromJson(obj);

			if (result.isValid())
			{
//				System.out.printf("[MODULE] fromJson/%s: [VALID] -> %s\n", type.toString(), result.asString());
				return result;
			}
//			else
//			{
//				System.out.printf("[MODULE] fromJson/%s: [ERR] -> %s\n", type.toString(), result.asString());
//			}
		}

		return null;
	}

	static
	{
		ImmutableMap.Builder<Identifier, AudioModuleType<?>> builder = ImmutableMap.builder();

		builder.put(BaseModule.TYPE, BASE);
		builder.put(StaticModule.TYPE, STATIC);
		builder.put(VolumeCategoryModule.TYPE, VOLUME);
		builder.put(RegionModule.TYPE, REGION);
		builder.put(RandomPlaybackModule.TYPE, RANDOM);

		REGISTRY = builder.build();
	}
}
