package com.sakuraryoko.ap_scan.audio.data.modules;

import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import net.minecraft.resources.Identifier;

public class BaseModule extends AbstractAudioModule
{
	public static final Identifier TYPE = Identifier.fromNamespaceAndPath("audioplayer", "audio");
	@Nullable
	private UUID id;
	@Nullable
	private Float range;

	public BaseModule(Identifier type)
	{
		super(type);
	}

	public BaseModule()
	{
		super(TYPE);
	}

	public static BaseModule create(@Nonnull UUID id, @Nullable Float range)
	{
		BaseModule m = new BaseModule();
		m.id = id;
		m.range = range;
		return m;
	}

	@Override
	public void fromJson(JsonObject obj)
	{
		try
		{
			JsonElement id = obj.get("id");
			JsonElement range = obj.get("range");

			if (id != null && id.isJsonPrimitive())
			{
				JsonPrimitive idP = id.getAsJsonPrimitive();

				if (idP.isString())
				{
					this.id = UUID.fromString(idP.getAsString());
				}
			}
			if (range != null && range.isJsonPrimitive())
			{
				JsonPrimitive rangeP = range.getAsJsonPrimitive();

				if (rangeP.isNumber())
				{
					this.range = rangeP.getAsFloat();
				}
			}
		}
		catch (Exception ignored)
		{
		}
	}

	@Override
	public void toJson(JsonObject obj)
	{
		if (this.isValid())
		{
			if (this.id != null)
			{
				obj.add("id", new JsonPrimitive(this.id.toString()));
			}
			if (this.range != null)
			{
				obj.add("range", new JsonPrimitive(this.range));
			}
		}
	}

	@Override
	public boolean isValid()
	{
		return this.id != null;
	}

	@Override
	public String asString()
	{
		if (this.isValid() && this.id != null)
		{
			if (this.range != null)
			{
				return "id=" + this.id.toString() + ",range=" + this.range;
			}

			return "id=" + this.id.toString();
		}

		return "";
	}

	@Nullable
	public UUID getSoundId()
	{
		return this.id;
	}

	@Nullable
	public Float getRange()
	{
		return this.range;
	}
}
