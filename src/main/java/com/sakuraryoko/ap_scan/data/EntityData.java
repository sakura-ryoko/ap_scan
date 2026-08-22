package com.sakuraryoko.ap_scan.data;

import java.util.UUID;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import com.sakuraryoko.ap_scan.audio.LocationType;
import com.sakuraryoko.ap_scan.audio.NbtAudioUtil;
import com.sakuraryoko.ap_scan.util.DataFixerUtils;
import com.sakuraryoko.ap_scan.util.InventoryUtils;
import com.sakuraryoko.ap_scan.util.NbtEntityUtils;
import com.sakuraryoko.ap_scan.util.NbtKeys;

public class EntityData
{
	public static void processEntityData(CompoundTag nbt, int oldDataVersion)
	{
		ListTag list = nbt.getListOrEmpty("Entities");
		RegistryAccess registry = DataManager.getInstance().getRegistry();

		for (int i = 0; i < list.size(); i++)
		{
			processEntityDataEach(list.getCompoundOrEmpty(i), registry, oldDataVersion);
		}
	}

	public static String getEntityDesc(Component defName, Component customName, UUID uuid, Vec3 pos)
	{
		return "Entity[" +
				"{DefName="+defName.getString()+"}" +
				",{CustomName="+customName.getString()+"}" +
				",{UUID="+uuid.toString()+"}" +
				",{Pos="+pos.toString()+"}" +
				"]";
	}

	private static void processEntityDataEach(CompoundTag nbt, RegistryAccess registry, int oldDataVersion)
	{
		String id = nbt.getStringOr("id", "");
		boolean shouldFix = nbt.getIntOr("DataVersion", -1) < DataFixerUtils.CURRENT_SCHEMA;

		// Filter out all the unwanted Entity Types
		if (IDList.ENTITY_ID_LIST.contains(id))
		{
			CompoundTag fixedNbt = shouldFix ? DataFixerUtils.fixEntity(nbt, oldDataVersion) : nbt;

//			System.out.printf("[ENT] (MATCHED) nbt [%s]\n", fixedNbt.toString());
			if (InventoryUtils.hasNbtItems(fixedNbt))
			{
				Identifier identifier = Identifier.tryParse(id);
				EntityType<?> entityType = NbtEntityUtils.getEntityTypeFromNbt(fixedNbt);
				final Component defName = entityType != null ? entityType.getDescription() : (identifier != null ? Component.nullToEmpty(identifier.getPath()) : Component.nullToEmpty(id));
				final Component customName = fixedNbt.read(NbtKeys.CUSTOM_NAME, ComponentSerialization.CODEC).orElse(defName);
				final UUID uuid = fixedNbt.read(NbtKeys.UUID, UUIDUtil.CODEC).orElse(UUIDUtil.createOfflinePlayerUUID(customName.getString()));
				final Vec3 pos = fixedNbt.read(NbtKeys.POS, Vec3.CODEC).orElse(Vec3.ZERO);

				NbtAudioUtil.processEachNbt(fixedNbt, registry, oldDataVersion, LocationType.ENTITY, getEntityDesc(defName, customName, uuid, pos));
			}
		}
//		else
//		{
//			System.out.printf("[ENT] (NOT-MATCHED) nbt [%s]\n", nbt.toString());
//		}
	}
}
