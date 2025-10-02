package com.sakuraryoko.ap_scan.audio;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import com.sakuraryoko.ap_scan.ApScan;
import com.sakuraryoko.ap_scan.Reference;

public class LocationsList
{
	private final Int2ObjectArrayMap<AudioDataLocation> locations;

	public LocationsList() { this.locations = new Int2ObjectArrayMap<>(); }

	public boolean contains(String id)
	{
		AtomicBoolean bool = new AtomicBoolean(false);

		this.locations.forEach(
				(i, data) ->
				{
					if (data.id().equalsIgnoreCase(id))
					{
						bool.set(true);
					}
				}
		);

		return bool.get();
	}

	@Nullable
	public List<AudioDataLocation> getById(String id)
	{
		List<AudioDataLocation> list = new ArrayList<>();

		this.locations.forEach(
				(i, data) ->
				{
					if (data.id().equalsIgnoreCase(id))
					{
						list.add(data);
					}
				});

		return list;
	}

	@Nullable
	public List<AudioDataLocation> getByUuid(UUID uuid) { return this.getById(uuid.toString()); }

	@Nullable
	public AudioDataLocation get(int index)
	{
		if (index > -1 && index < this.size())
		{
			return this.locations.get(index);
		}

		return null;
	}

	public void set(int index, AudioDataLocation file) throws IndexOutOfBoundsException
	{
		if (index > -1 && index < this.size())
		{
			this.locations.put(index, file);
		}
		else
		{
			throw new IndexOutOfBoundsException("index "+index+" out of bounds for a list of size '"+this.size()+"'");
		}
	}

	public void add(AudioDataLocation file)
	{
		this.locations.put(this.locations.size(), file);
	}

	public void addList(LocationsList otherList)
	{
		otherList.locations.forEach(
				(i, data) ->
				{
					if (Reference.DEBUG)
					{
						ApScan.LOGGER.warn("addList(): [STACKS] {}", data.toString());
					}

					this.add(data);
				});
	}

	public boolean isEmpty() { return this.locations.isEmpty(); }

	public int size() { return this.locations.size(); }

	public ImmutableList<AudioDataLocation> asList()
	{
		ImmutableList.Builder<AudioDataLocation> builder = new ImmutableList.Builder<>();

		this.locations.forEach(
				(i, data) ->
						builder.add(data)
		);

		return builder.build();
	}

	public void clear() { this.locations.clear(); }

	public static LocationsList fromJson(JsonElement element)
	{
		LocationsList list = new LocationsList();

		try
		{
			if (element.isJsonArray())
			{
				JsonArray arr = element.getAsJsonArray();

				for (int i = 0; i < arr.size(); i++)
				{
					AudioDataLocation location = AudioDataLocation.fromJson(arr.get(i));

					if (location != null)
					{
						list.add(location);
					}
				}

				return list;
			}
		}
		catch (Exception err)
		{
			ApScan.LOGGER.error("LocationsList#fromJson(): Exception parsing Audio File list; {}", err.getLocalizedMessage());
		}

		return list;
	}

	@VisibleForTesting
	public void dump()
	{
		System.out.print("LocationsList: DUMP -->\n");

		for (int i = 0; i < this.locations.size(); i++)
		{
			System.out.printf("  [%04d]: %s\n", i, this.locations.get(i).toString());
		}

		System.out.printf("LocationsList: EOF [Size: %d]\n", this.locations.size());
	}
}
