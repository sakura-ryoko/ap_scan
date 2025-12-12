package com.sakuraryoko.ap_scan.data;

import java.util.UUID;
import net.minecraft.core.BlockPos;
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
import com.sakuraryoko.ap_scan.util.*;

public class ChunkData
{
	public static void processChunkData(CompoundTag nbt, int oldDataVersion)
	{
		if (nbt.getString("Status").isEmpty())
		{
			return;
		}

		RegistryAccess registry = DataManager.getInstance().getRegistry();
		processChunkEntities(nbt.getListOrEmpty("entities"), registry, oldDataVersion);
		processChunkTileEntities(nbt.getListOrEmpty("block_entities"), registry, oldDataVersion);
	}

	private static void processChunkEntities(ListTag list, RegistryAccess registry, int oldDataVersion)
	{
		for (int i = 0; i < list.size(); i++)
		{
			processChunkEntityEach(list.getCompoundOrEmpty(i), registry, oldDataVersion);
		}
	}

	private static void processChunkTileEntities(ListTag list, RegistryAccess registry, int oldDataVersion)
	{
		for (int i = 0; i < list.size(); i++)
		{
			processChunkTileEntityEach(list.getCompoundOrEmpty(i), registry, oldDataVersion);
		}
	}

	private static void processChunkEntityEach(CompoundTag nbt, RegistryAccess registry, int oldDataVersion)
	{
		String id = nbt.getStringOr("id", "");
		boolean shouldFix = nbt.getIntOr("DataVersion", -1) < DataFixerUtils.CURRENT_SCHEMA;

		// Filter out all the unwanted Entity Types
		if (IDList.ENTITY_ID_LIST.contains(id))
		{
			CompoundTag fixedNbt = shouldFix ? DataFixerUtils.fixEntity(nbt, oldDataVersion) : nbt;

			if (InventoryUtils.hasNbtItems(fixedNbt))
			{
				id = nbt.getStringOr("id", "");
				Identifier identifier = Identifier.tryParse(id);
				EntityType<?> entityType = NbtEntityUtils.getEntityTypeFromNbt(fixedNbt);
				final Component defName = entityType != null ? entityType.getDescription() : (identifier != null ? Component.nullToEmpty(identifier.getPath()) : Component.nullToEmpty(id));
				final Component customName = fixedNbt.read(NbtKeys.CUSTOM_NAME, ComponentSerialization.CODEC).orElse(defName);
				final UUID uuid = fixedNbt.read(NbtKeys.UUID, UUIDUtil.CODEC).orElse(UUIDUtil.createOfflinePlayerUUID(customName.getString()));
				final Vec3 pos = fixedNbt.read(NbtKeys.POS, Vec3.CODEC).orElse(Vec3.ZERO);

				NbtAudioUtil.processEachNbt(fixedNbt, registry, oldDataVersion, LocationType.ENTITY, EntityData.getEntityDesc(defName, customName, uuid, pos));
			}
		}
	}

	public static String getTileEntityDesc(Component defName, Component customName, BlockPos pos)
	{
		if (customName.equals(defName))
		{
			return "TileEntity[" +
					"{Name="+defName.getString()+"}" +
					",{Pos="+pos.toShortString()+"}" +
					"]";
		}

		return "TileEntity[" +
				"{Name="+defName.getString()+"}" +
				",{CustomName="+customName.getString()+"}" +
				",{Pos="+pos.toShortString()+"}" +
				"]";
	}

	private static void processChunkTileEntityEach(CompoundTag nbt, RegistryAccess registry, int oldDataVersion)
	{
		String id = nbt.getStringOr("id", "");
		boolean shouldFix = nbt.getIntOr("DataVersion", -1) < DataFixerUtils.CURRENT_SCHEMA;

		// Filter out all the unwanted Tile Entity Types
		if (IDList.TILE_ID_LIST.contains(id.toLowerCase()))
		{
			CompoundTag fixedNbt = shouldFix ? DataFixerUtils.fixTileEntity(nbt, oldDataVersion) : nbt;

//			System.out.printf("[TE] (MATCHED) nbt [%s]\n", fixedNbt.toString());
			id = nbt.getStringOr("id", "");
			Identifier identifier = Identifier.tryParse(id);
			BlockPos pos = NbtBlockUtils.readBlockPos(fixedNbt);

			if (pos == null)
			{
				pos = BlockPos.ZERO;
			}

			final Component defName = (identifier != null ? Component.nullToEmpty(identifier.getPath()) : Component.nullToEmpty(id));
			Component customName = NbtBlockUtils.getCustomNameFromNbt(fixedNbt, registry, NbtKeys.SKULL_NAME);

			if (customName == null)
			{
				customName = defName;
			}

			if (InventoryUtils.hasNbtItems(fixedNbt))
			{
				NbtAudioUtil.processEachNbt(fixedNbt, registry, oldDataVersion, LocationType.TILE_ENTITY, getTileEntityDesc(defName, customName, pos));
			}
			else if (id.equalsIgnoreCase("minecraft:skull"))
			{
				NbtAudioUtil.processEachSkull(fixedNbt, registry, oldDataVersion, LocationType.SKULL, getTileEntityDesc(defName, customName, pos));
			}
		}
//		else
//		{
//			System.out.printf("[TE] (NOT-MATCHED) nbt [%s]\n", nbt.toString());
//		}
	}
}
