package com.sakuraryoko.ap_scan.audio.data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import com.google.common.collect.ImmutableList;

import net.minecraft.resources.Identifier;

import com.sakuraryoko.ap_scan.audio.data.modules.AbstractAudioModule;

public class AudioModuleList
{
	private final ConcurrentHashMap<Identifier, AbstractAudioModule> modules;

	public AudioModuleList()
	{
		this.modules = new ConcurrentHashMap<>();
	}

	public void add(AbstractAudioModule module)
	{
		this.modules.put(module.getId(), module);
	}

	public void remove(AbstractAudioModule module)
	{
		this.modules.remove(module.getId());
	}

	public boolean has(Identifier id)
	{
		return this.modules.containsKey(id);
	}

	@Nullable
	public AbstractAudioModule get(Identifier id)
	{
		return this.modules.getOrDefault(id, null);
	}

	public ImmutableList<AbstractAudioModule> getModules()
	{
		return ImmutableList.copyOf(this.modules.values());
	}

	public boolean isEmpty()
	{
		return this.modules.isEmpty();
	}

	public int size()
	{
		return this.modules.size();
	}

	public void clear()
	{
		this.modules.clear();
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AudioModuleList that = (AudioModuleList) o;

		if (this.modules.size() != that.modules.size()) return false;

		for (Identifier id : this.modules.keySet())
		{
			if (!that.modules.containsKey(id))
			{
				return false;
			}
			else if (!this.modules.get(id).equals(that.modules.get(id)))
			{
				return false;
			}
		}

		return true;
	}

	@Override
	public int hashCode()
	{
		int result = 1;

		for (Map.Entry<Identifier, AbstractAudioModule> entry : this.modules.entrySet())
		{
			int entryHash = 1;
			entryHash = 31 * entryHash + (entry.getKey() != null ? entry.getKey().hashCode() : 0);
			entryHash = 31 * entryHash + (entry.getValue() != null ? entry.getValue().hashCode() : 0);
			result += entryHash;
		}

		return result;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder("AudioModuleList[");

		this.modules.values().forEach(
				m -> sb.append("{").append(m.asString()).append("}")
		);

		sb.append("]");

		return sb.toString();
	}
}
