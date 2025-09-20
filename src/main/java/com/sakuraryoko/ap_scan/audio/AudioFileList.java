package com.sakuraryoko.ap_scan.audio;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.sakuraryoko.ap_scan.ApScan;

public class AudioFileList
{
	private final List<AudioFile> files;

	public AudioFileList()
	{
		this.files = new ArrayList<>();
	}

	public void add(AudioFile file)
	{
		this.files.add(file);
	}

	public void remove(AudioFile file)
	{
		this.files.remove(file);
	}

	public boolean isEmpty() { return this.files.isEmpty(); }

	public int size() { return this.files.size(); }

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
