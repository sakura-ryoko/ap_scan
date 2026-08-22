package com.sakuraryoko.ap_scan.audio.data.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import net.minecraft.resources.Identifier;

public class RandomPlaybackModule extends AbstractAudioModule
{
	public static final Identifier TYPE = Identifier.fromNamespaceAndPath("audioplayer_roleplay", "rng_playback");
	@Nullable
	private String id;
	@Nullable
	private List<UUID> ids;

	public RandomPlaybackModule(Identifier type)
	{
		super(type);
	}

	public RandomPlaybackModule()
	{
		super(TYPE);
	}

	public static RandomPlaybackModule create(String playlist)
	{
		RandomPlaybackModule m = new RandomPlaybackModule();
		m.id = playlist;
		return m;
	}

	public static RandomPlaybackModule create(List<UUID> ids)
	{
		RandomPlaybackModule m = new RandomPlaybackModule();
		m.ids = ids;
		return m;
	}

	@Override
	public void fromJson(JsonObject obj)
	{
		if (this.ids != null)
		{
			this.ids.clear();
		}
		else
		{
			this.ids = new ArrayList<>();
		}

		try
		{
			JsonElement ids = obj.get("ids");
			JsonElement id = obj.get("id");

			if (ids != null && id.isJsonArray())
			{
				JsonArray arr = id.getAsJsonArray();

				for (int i = 0; i < arr.size(); i++)
				{
					JsonElement entry = arr.get(i);

					if (entry.isJsonArray())
					{
						JsonArray entryArr = entry.getAsJsonArray();

						if (entryArr.size() == 2)
						{
							JsonElement uuidA = entryArr.get(0);
							JsonElement uuidB = entryArr.get(1);

							if (uuidA.isJsonPrimitive() && uuidB.isJsonPrimitive())
							{
								JsonPrimitive uuidAP = uuidA.getAsJsonPrimitive();
								JsonPrimitive uuidBP = uuidB.getAsJsonPrimitive();

								if (uuidAP.isNumber() && uuidBP.isNumber())
								{
									try
									{
										this.ids.add(new UUID(uuidAP.getAsLong(), uuidBP.getAsLong()));
									}
									catch (Exception ignored) {}
								}
							}
						}
					}
				}
			}

			if (id != null && id.isJsonPrimitive())
			{
				JsonPrimitive idP = id.getAsJsonPrimitive();

				if (idP.isString())
				{
					this.id = idP.getAsString();
				}
			}
		}
		catch (Exception ignored) {}
	}

	@Override
	public void toJson(JsonObject obj)
	{
		if (this.isValid())
		{
			JsonArray arr = new JsonArray();

			if (this.ids != null)
			{
				for (UUID id : this.ids)
				{
					JsonArray entry = new JsonArray();
					entry.add(id.getLeastSignificantBits());
					entry.add(id.getMostSignificantBits());
					arr.add(entry);
				}
			}

			obj.add("ids", arr);

			if (this.id != null)
			{
				obj.add("id", new JsonPrimitive(this.id));
			}
		}
	}

	@Override
	public boolean isValid()
	{
		return (this.ids != null && !this.ids.isEmpty()) || this.id != null;
	}

	@Override
	public String asString()
	{
		if (this.isValid())
		{
			StringBuilder sb = new StringBuilder("rng=[");

			if (this.id != null)
			{
				sb.append("{playlist=").append(this.id).append("}");
			}
			else if (this.ids != null)
			{
				boolean first = true;

				for (UUID id : this.ids)
				{
					if (!first) sb.append(",");
					sb.append("{id=").append(id.toString()).append("}");
					if (first) first = false;
				}
			}

			sb.append("]");
			return sb.toString();
		}

		return "";
	}

	@Nullable
	public List<UUID> getIds()
	{
		return this.ids;
	}

	@Nullable
	public String getPlaylistId()
	{
		return this.id;
	}
}
