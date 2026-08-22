package com.sakuraryoko.ap_scan.audio.data.modules;

import javax.annotation.Nullable;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import net.minecraft.core.BlockBox;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

public class RegionModule extends AbstractAudioModule
{
	public static final Identifier TYPE = Identifier.fromNamespaceAndPath("audioplayer_roleplay", "regions");
	@Nullable
	private BlockPos pos1;
	@Nullable
	private BlockPos pos2;
	@Nullable
	private String id;
	@Nullable
	private Type mode;

	public RegionModule(Identifier type)
	{
		super(type);
	}

	public RegionModule()
	{
		super(TYPE);
	}

	public static RegionModule create(BlockPos pos1, BlockPos pos2, Type mode)
	{
		RegionModule m = new RegionModule();
		m.pos1 = pos1;
		m.pos2 = pos2;
		m.mode = mode;
		return m;
	}

	public static RegionModule create(String id, Type mode)
	{
		RegionModule m = new RegionModule();
		m.id = id;
		m.mode = mode;
		return m;
	}

	@Override
	public void fromJson(JsonObject obj)
	{
		try
		{
			JsonElement pos = obj.get("pos");
			JsonElement mode = obj.get("mode");

			if (pos != null)
			{
				if (pos.isJsonArray())
				{
					JsonArray arr = pos.getAsJsonArray();

					if (arr.size() == 6)
					{
						this.pos1 = new BlockPos(arr.get(0).getAsInt(), arr.get(1).getAsInt(), arr.get(2).getAsInt());
						this.pos2 = new BlockPos(arr.get(3).getAsInt(), arr.get(4).getAsInt(), arr.get(5).getAsInt());
					}
				}
				else if (pos.isJsonPrimitive())
				{
					if (pos.getAsJsonPrimitive().isString())
					{
						this.id = pos.getAsString();
					}
				}
			}
			else
			{
				return;
			}

			if (mode != null)
			{
				if (mode.isJsonPrimitive())
				{
					if (mode.getAsJsonPrimitive().isString())
					{
						for (Type t : Type.values())
						{
							if (t.name().equalsIgnoreCase(mode.getAsString()))
							{
								this.mode = t;
							}
						}
					}
				}
			}

			if (this.mode == null)
			{
				this.mode = Type.CLIP;
			}
		}
		catch (Exception ignored) {}
	}

	@Override
	public void toJson(JsonObject obj)
	{
		if (this.isValid())
		{
			if (this.pos1 != null && this.pos2 != null)
			{
				JsonArray arr = new JsonArray();

				arr.add(this.pos1.getX());
				arr.add(this.pos1.getY());
				arr.add(this.pos1.getZ());
				arr.add(this.pos2.getX());
				arr.add(this.pos2.getY());
				arr.add(this.pos2.getZ());

				obj.add("pos", arr);
			}
			else if (this.id != null)
			{
				obj.add("id", new JsonPrimitive(this.id));
			}

			if (this.mode != null)
			{
				obj.add("mode", new JsonPrimitive(this.mode.name()));
			}
		}
	}

	@Override
	public boolean isValid()
	{
		boolean result = false;

		if (this.pos1 != null && this.pos2 != null)
		{
			result = true;
		}
		else if (this.id != null)
		{
			result = true;
		}

		return (result && this.mode != null);
	}

	@Override
	public String asString()
	{
		if (this.isValid())
		{
			StringBuilder sb = new StringBuilder("region=[");

			if (this.pos1 != null && this.pos2 != null)
			{
				sb.append("{min=").append(this.pos1.toShortString()).append(",max=").append(this.pos2.toShortString()).append("}");
			}
			else if (this.id != null)
			{
				sb.append("{id=").append(this.id).append("}");
			}

			if (this.mode != null)
			{
				sb.append(",{mode=").append(this.mode.name()).append("}");
			}

			sb.append("]");

			return sb.toString();
		}

		return "";
	}

	@Nullable
	public BlockPos pos1()
	{
		return this.pos1;
	}

	@Nullable
	public BlockPos pos2()
	{
		return this.pos2;
	}

	@Nullable
	public BlockBox asBlockBox()
	{
		if (this.pos1 != null && this.pos2 != null)
		{
			return new BlockBox(this.pos1, this.pos2);
		}

		return null;
	}

	@Nullable
	public String regionId()
	{
		return this.id;
	}

	@Nullable
	public Type type()
	{
		return this.mode;
	}

	public enum Type
	{
		CLIP,
		CLIP_MANUAL_RANGE,
		FALLOFF
	}
}
