package com.sakuraryoko.ap_scan.audio.data;

import javax.annotation.Nullable;

import net.minecraft.resources.Identifier;

import com.sakuraryoko.ap_scan.audio.data.modules.AbstractAudioModule;

public class AudioModuleType<T extends AbstractAudioModule>
{
	private final Builder<? extends T> builder;
	private final Identifier type;

	public static <T extends AbstractAudioModule> AudioModuleType<T> build(Identifier type, Builder<? extends T> builder)
	{
		return new AudioModuleType<>(type, builder);
	}

	public AudioModuleType(Identifier type, Builder<? extends T> builder)
	{
		this.builder = builder;
		this.type = type;
	}

	@Nullable
	public T init(Identifier type) { return this.builder.build(type); }

	public Identifier getType()
	{
		return this.type;
	}

	@FunctionalInterface
	public interface Builder<T extends AbstractAudioModule>
	{
		T build(Identifier type);
	}
}
