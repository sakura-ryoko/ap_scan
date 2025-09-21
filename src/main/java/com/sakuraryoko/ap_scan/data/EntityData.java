package com.sakuraryoko.ap_scan.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.DynamicRegistryManager;

import com.sakuraryoko.ap_scan.audio.NbtAudioUtil;
import com.sakuraryoko.ap_scan.util.DataFixerUtils;
import com.sakuraryoko.ap_scan.util.InventoryUtils;

public class EntityData
{
	public static void processEntityData(NbtCompound nbt, int oldDataVersion)
	{
		NbtList list = nbt.getListOrEmpty("Entities");
		DynamicRegistryManager registry = DataManager.getInstance().getRegistry();

		for (int i = 0; i < list.size(); i++)
		{
			processEntityDataEach(list.getCompoundOrEmpty(i), registry, oldDataVersion);
		}
	}

	private static void processEntityDataEach(NbtCompound nbt, DynamicRegistryManager registry, int oldDataVersion)
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
}
