package com.sakuraryoko.ap_scan.audio;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import com.google.common.collect.Iterables;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import com.sakuraryoko.ap_scan.ApScan;

public class AudioFileList
{
	private final List<AudioFile> files;

	public AudioFileList()
	{
		this.files = new ArrayList<>();
	}

	public boolean contains(String id)
	{
		AtomicBoolean bool = new AtomicBoolean(false);

		this.files.forEach(
				(entry) ->
				{
					if (entry.getId().equalsIgnoreCase(id))
					{
						bool.set(true);
					}
				}
		);

		return bool.get();
	}

	@Nullable
	public AudioFile getById(String id)
	{
		for (AudioFile entry : this.files)
		{
			if (entry.getId().equalsIgnoreCase(id))
			{
				return entry;
			}
		}

		return null;
	}

	@Nullable
	public AudioFile getByUuid(UUID uuid)
	{
		return this.getById(uuid.toString());
	}

	@Nullable
	public AudioFile get(int index)
	{
		if (index > -1 && index < this.size())
		{
			return this.files.get(index);
		}

		return null;
	}

	public void set(int index, AudioFile file) throws IndexOutOfBoundsException
	{
		if (index > -1 && index < this.size())
		{
		}
		else
		{
			throw new IndexOutOfBoundsException("index "+index+" out of bounds for a list of size '"+this.size()+"'");
		}
	}

	public void add(AudioFile file)
	{
		// Don't duplicate
		if (this.contains(file.getId()))
		{
			return;
		}

		this.files.add(file);
	}

	public void remove(AudioFile file)
	{
		this.files.remove(file);
	}

	public boolean isEmpty() { return this.files.isEmpty(); }

	public int size() { return this.files.size(); }

	public Iterable<AudioFile> iterator()
	{
		return Iterables.concat(this.files);
	}

	public Stream<AudioFile> stream()
	{
		return this.files.stream();
	}

	public void clear() { this.files.clear(); }

	public static AudioFileList fromJson(JsonElement element)
	{
		AudioFileList list = new AudioFileList();

		try
		{
			if (element.isJsonObject())
			{
				JsonObject obj = element.getAsJsonObject();
				Set<String> set = obj.keySet();

				for (String key : set)
				{
					JsonElement entry = obj.get(key);

					AudioFile file = AudioFile.fromJson(key, entry);

					if (file != null)
					{
						list.add(file);
					}
				}

				return list;
			}
		}
		catch (Exception err)
		{
			ApScan.LOGGER.error("AudioFileList#fromJson(): Exception parsing Audio File list; {}", err.getLocalizedMessage());
		}

		return list;
	}

	public void dump()
	{
		System.out.print("AudioFileList: DUMP -->\n");

		for (int i = 0; i < this.files.size(); i++)
		{
			System.out.printf("  [%04d]: %s\n", i, this.files.get(i).toString());
		}

		System.out.printf("AudioFileList: EOF [Size: %d]\n", this.files.size());
	}
}
