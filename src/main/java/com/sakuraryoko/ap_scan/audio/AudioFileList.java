package com.sakuraryoko.ap_scan.audio;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import com.google.common.collect.Iterables;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import com.sakuraryoko.ap_scan.ApScan;
import com.sakuraryoko.ap_scan.Reference;
import com.sakuraryoko.ap_scan.audio.v1.AudioFileV1;
import com.sakuraryoko.ap_scan.data.DataManager;

public class AudioFileList
{
	private final List<AudioFileV2> files;

	public AudioFileList() { this.files = new ArrayList<>(); }

	public boolean contains(UUID id)
	{
		AtomicBoolean bool = new AtomicBoolean(false);

		this.files.forEach(
				(entry) ->
				{
					if (entry.id().equals(id))
					{
						bool.set(true);
					}
				}
		);

		return bool.get();
	}

	@Nullable
	public AudioFileV2 getById(UUID id)
	{
		for (AudioFileV2 entry : this.files)
		{
			if (entry.id().equals(id))
			{
				return entry;
			}
		}

		return null;
	}

	@Nullable
	public AudioFileV2 getByString(String id) { return this.getById(UUID.fromString(id)); }

	@Nullable
	public AudioFileV2 get(int index)
	{
		if (index > -1 && index < this.size())
		{
			return this.files.get(index);
		}

		return null;
	}

	public void set(int index, AudioFileV2 file) throws IndexOutOfBoundsException
	{
		if (index > -1 && index < this.size())
		{
			this.files.set(index, file);
		}
		else
		{
			throw new IndexOutOfBoundsException("index "+index+" out of bounds for a list of size '"+this.size()+"'");
		}
	}

	public void add(AudioFileV2 file)
	{
		// Don't duplicate
		if (this.contains(file.id()))
		{
			return;
		}

		this.files.add(file);
	}

	public void addList(AudioFileList otherList)
	{
		for (AudioFileV2 entry : otherList.files)
		{
			if (Reference.DEBUG)
			{
				ApScan.LOGGER.warn("AudioFileList#addList(): [ADD] {}", entry.toString());
			}

			this.add(entry);
		}
	}

	public void remove(AudioFileV2 file) { this.files.remove(file); }

	public boolean isEmpty() { return this.files.isEmpty(); }

	public int size() { return this.files.size(); }

	public List<AudioFileV2> asList() { return this.files; }

	public Iterable<AudioFileV2> iterator() { return Iterables.concat(this.files); }

	public Stream<AudioFileV2> stream() { return this.files.stream(); }

	public void clear() { this.files.clear(); }

	public static AudioFileList fromJsonV1(JsonElement element)
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

					AudioFileV1 file = AudioFileV1.fromJson(key, entry);

					if (file != null)
					{
						list.add(AudioFileV2.fromV1(file));
					}
				}

				return list;
			}
		}
		catch (Exception err)
		{
			ApScan.LOGGER.error("AudioFileList#fromJsonV1(): Exception parsing Audio File list; {}", err.getLocalizedMessage());
		}

		return list;
	}

	public static AudioFileList fromJsonV2(JsonElement element)
	{
		AudioFileList list = new AudioFileList();

		try
		{
			if (element.isJsonObject())
			{
				JsonObject obj = element.getAsJsonObject();
				JsonElement version = obj.get("version");

				if (version == null)
				{
					ApScan.LOGGER.error("AudioFileList#fromJsonV2(): Error parsing Audio File list; Unexpected or missing version!");
//					list.dump();
					return list;
				}
				else
				{
					int ver = version.getAsInt();
					DataManager.getInstance().setAudioConfigVersion(ver);
				}

				if (obj.has("files"))
				{
					JsonObject files = obj.getAsJsonObject("files");

					if (files != null)
					{
						Set<String> set = files.keySet();

						for (String key : set)
						{
							JsonElement entry = files.get(key);
							UUID uuid = UUID.fromString(key);
							AudioFileV2 file = AudioFileV2.fromJson(uuid, entry);

							if (file != null)
							{
								list.add(file);
							}
						}
					}

//					list.dump();
					return list;
				}
			}
		}
		catch (Exception err)
		{
			ApScan.LOGGER.error("AudioFileList#fromJsonV2(): Exception parsing Audio File list; {}", err.getLocalizedMessage());
		}

//		list.dump();
		return list;
	}

	@VisibleForTesting
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
