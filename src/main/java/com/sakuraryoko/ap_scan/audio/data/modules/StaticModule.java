package com.sakuraryoko.ap_scan.audio.data.modules;

import com.google.gson.JsonObject;

import net.minecraft.resources.Identifier;

public class StaticModule extends AbstractAudioModule
{
	public static final Identifier TYPE = Identifier.fromNamespaceAndPath("audioplayer_roleplay", "static");
	private boolean enabled;

	public StaticModule(Identifier type)
	{
		super(type);
	}

	public StaticModule()
	{
		super(TYPE);
	}

	public static StaticModule create()
	{
		StaticModule m = new StaticModule();
		m.enabled = true;
		return m;
	}

	@Override
	public void fromJson(JsonObject obj)
	{
		this.enabled = true;
	}

	@Override
	public void toJson(JsonObject obj)
	{
	}

	@Override
	public boolean isValid()
	{
		return this.enabled();
	}

	@Override
	public String asString()
	{
		if (this.isValid())
		{
			return "static=true";
		}

		return "";
	}

	public boolean enabled()
	{
		return this.enabled;
	}
}
