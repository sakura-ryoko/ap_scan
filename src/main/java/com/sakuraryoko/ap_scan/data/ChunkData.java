package com.sakuraryoko.ap_scan.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.DynamicRegistryManager;

import com.sakuraryoko.ap_scan.audio.AudioNbtUtil;
import com.sakuraryoko.ap_scan.util.InventoryUtils;

public class ChunkData
{
	public static void processChunkData(NbtCompound nbt)
	{
		if (nbt.getString("Status").isEmpty())
		{
			return;
		}

		DynamicRegistryManager registry = DataManager.getInstance().getRegistry();
		processChunkEntities(nbt.getListOrEmpty("entities"), registry);
		processChunkTileEntities(nbt.getListOrEmpty("block_entities"), registry);
	}

	private static void processChunkEntities(NbtList list, DynamicRegistryManager registry)
	{
		for (int i = 0; i < list.size(); i++)
		{
			processChunkEntityEach(list.getCompoundOrEmpty(i), registry);
		}
	}

	private static void processChunkTileEntities(NbtList list, DynamicRegistryManager registry)
	{
		for (int i = 0; i < list.size(); i++)
		{
			processChunkTileEntityEach(list.getCompoundOrEmpty(i), registry);
		}
	}

	private static void processChunkEntityEach(NbtCompound nbt, DynamicRegistryManager registry)
	{
		String id = nbt.getString("id", "");

		// Filter out all the unwanted Entity Types
		if (IDList.ENTITY_ID_LIST.contains(id) && InventoryUtils.hasNbtItems(nbt))
		{
			AudioNbtUtil.processEachInventory(nbt, registry);
		}
	}

	private static void processChunkTileEntityEach(NbtCompound nbt, DynamicRegistryManager registry)
	{
		String id = nbt.getString("id", "");

		// Filter out all the unwanted Tile Entity Types
		if (IDList.TILE_ID_LIST.contains(id) && InventoryUtils.hasNbtItems(nbt))
		{
			AudioNbtUtil.processEachInventory(nbt, registry);
		}
	}
}
