package com.sakuraryoko.ap_scan.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.DynamicRegistryManager;

import com.sakuraryoko.ap_scan.audio.NbtAudioUtil;
import com.sakuraryoko.ap_scan.util.InventoryUtils;

public class EntityData
{
	public static void processEntityData(NbtCompound nbt)
	{
		NbtList list = nbt.getListOrEmpty("Entities");
		DynamicRegistryManager registry = DataManager.getInstance().getRegistry();

		for (int i = 0; i < list.size(); i++)
		{
			processEntityDataEach(list.getCompoundOrEmpty(i), registry);
		}
	}

	private static void processEntityDataEach(NbtCompound nbt, DynamicRegistryManager registry)
	{
		String id = nbt.getString("id", "");

		// Filter out all the unwanted Entity Types
		if (IDList.ENTITY_ID_LIST.contains(id) && InventoryUtils.hasNbtItems(nbt))
		{
			NbtAudioUtil.processEachNbt(nbt, registry);
		}
	}
}
