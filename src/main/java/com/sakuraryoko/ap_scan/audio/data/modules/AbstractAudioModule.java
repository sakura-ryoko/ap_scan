package com.sakuraryoko.ap_scan.audio.data.modules;

import com.google.gson.JsonObject;

import net.minecraft.resources.Identifier;

public abstract class AbstractAudioModule
{
	private final Identifier type;

	public AbstractAudioModule(Identifier type)
	{
		this.type = type;
	}

	public Identifier getId()
	{
		return this.type;
	}

	public abstract void fromJson(JsonObject obj);

	public abstract void toJson(JsonObject obj);

	public abstract boolean isValid();

	public abstract String asString();
}
