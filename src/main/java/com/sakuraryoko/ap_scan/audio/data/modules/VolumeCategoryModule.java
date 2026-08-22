package com.sakuraryoko.ap_scan.audio.data.modules;

import javax.annotation.Nullable;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import net.minecraft.resources.Identifier;

public class VolumeCategoryModule extends AbstractAudioModule
{
	public static final Identifier TYPE = Identifier.fromNamespaceAndPath("audioplayer_roleplay", "custom_volume_category");
	@Nullable
	private String id;

	public VolumeCategoryModule(Identifier type)
	{
		super(type);
	}

	public VolumeCategoryModule()
	{
		super(TYPE);
	}

	public static VolumeCategoryModule create(String category)
	{
		VolumeCategoryModule m = new VolumeCategoryModule();
		m.id = category;
		return m;
	}

	@Override
	public void fromJson(JsonObject obj)
	{
		try
		{
			JsonElement ele = obj.get("id");

			if (ele.isJsonPrimitive())
			{
				JsonPrimitive p = ele.getAsJsonPrimitive();

				if (p.isString())
				{
					this.id = p.getAsString();
				}
			}
		}
		catch (Exception ignored) {}
	}

	@Override
	public void toJson(JsonObject obj)
	{
		if (this.isValid() && this.id != null)
		{
			obj.add("id", new JsonPrimitive(this.id));
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
		if (this.isValid())
		{
			return "volume_category=" + this.id;
		}

		return "";
	}

	@Nullable
	public String getCategory()
	{
		return this.id;
	}
}
