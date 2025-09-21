package com.sakuraryoko.ap_scan.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.DynamicRegistryManager;

import com.sakuraryoko.ap_scan.audio.NbtAudioUtil;
import com.sakuraryoko.ap_scan.util.DataFixerUtils;
import com.sakuraryoko.ap_scan.util.InventoryUtils;

public class ChunkData
{
	public static void processChunkData(NbtCompound nbt, int oldDataVersion)
	{
		if (nbt.getString("Status").isEmpty())
		{
			return;
		}

		DynamicRegistryManager registry = DataManager.getInstance().getRegistry();
		processChunkEntities(nbt.getListOrEmpty("entities"), registry, oldDataVersion);
		processChunkTileEntities(nbt.getListOrEmpty("block_entities"), registry, oldDataVersion);
	}

	private static void processChunkEntities(NbtList list, DynamicRegistryManager registry, int oldDataVersion)
	{
		for (int i = 0; i < list.size(); i++)
		{
			processChunkEntityEach(list.getCompoundOrEmpty(i), registry, oldDataVersion);
		}
	}

	private static void processChunkTileEntities(NbtList list, DynamicRegistryManager registry, int oldDataVersion)
	{
		for (int i = 0; i < list.size(); i++)
		{
			processChunkTileEntityEach(list.getCompoundOrEmpty(i), registry, oldDataVersion);
		}
	}

	private static void processChunkEntityEach(NbtCompound nbt, DynamicRegistryManager registry, int oldDataVersion)
	{
		String id = nbt.getString("id", "");
		boolean shouldFix = nbt.getInt("DataVersion", -1) < DataFixerUtils.CURRENT_SCHEMA;

		// Filter out all the unwanted Entity Types
		if (IDList.ENTITY_ID_LIST.contains(id))
		{
			NbtCompound fixedNbt = shouldFix ? DataFixerUtils.fixEntity(nbt, oldDataVersion) : nbt;

			if (InventoryUtils.hasNbtItems(fixedNbt))
			{
				NbtAudioUtil.processEachNbt(fixedNbt, registry, oldDataVersion);
			}
		}
	}

	private static void processChunkTileEntityEach(NbtCompound nbt, DynamicRegistryManager registry, int oldDataVersion)
	{
		String id = nbt.getString("id", "");
		boolean shouldFix = nbt.getInt("DataVersion", -1) < DataFixerUtils.CURRENT_SCHEMA;

		// Filter out all the unwanted Tile Entity Types
		if (IDList.TILE_ID_LIST.contains(id))
		{
			NbtCompound fixedNbt = shouldFix ? DataFixerUtils.fixTileEntity(nbt, oldDataVersion) : nbt;

			if (InventoryUtils.hasNbtItems(fixedNbt))
			{
				NbtAudioUtil.processEachNbt(fixedNbt, registry, oldDataVersion);
			}
		}
	}
}
