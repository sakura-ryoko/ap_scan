package com.sakuraryoko.ap_scan.data;

import java.util.UUID;

import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.Vec3d;

import com.sakuraryoko.ap_scan.audio.LocationType;
import com.sakuraryoko.ap_scan.audio.NbtAudioUtil;
import com.sakuraryoko.ap_scan.util.DataFixerUtils;
import com.sakuraryoko.ap_scan.util.InventoryUtils;
import com.sakuraryoko.ap_scan.util.NbtEntityUtils;
import com.sakuraryoko.ap_scan.util.NbtKeys;

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

	public static String getEntityDesc(Text defName, Text customName, UUID uuid, Vec3d pos)
	{
		return "Entity[" +
				"{DefName="+defName.getString()+"}" +
				",{CustomName="+customName.getString()+"}" +
				",{UUID="+uuid.toString()+"}" +
				",{Pos="+pos.toString()+"}" +
				"]";
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
				Identifier identifier = Identifier.tryParse(id);
				EntityType<?> entityType = NbtEntityUtils.getEntityTypeFromNbt(fixedNbt);
				final Text defName = entityType != null ? entityType.getName() : (identifier != null ? Text.of(identifier.getPath()) : Text.of(id));
				final Text customName = fixedNbt.get(NbtKeys.CUSTOM_NAME, TextCodecs.CODEC).orElse(defName);
				final UUID uuid = fixedNbt.get(NbtKeys.UUID, Uuids.INT_STREAM_CODEC).orElse(Uuids.getOfflinePlayerUuid(customName.getString()));
				final Vec3d pos = fixedNbt.get(NbtKeys.POS, Vec3d.CODEC).orElse(Vec3d.ZERO);

				NbtAudioUtil.processEachNbt(fixedNbt, registry, oldDataVersion, LocationType.ENTITY, getEntityDesc(defName, customName, uuid, pos));
			}
		}
	}
}
