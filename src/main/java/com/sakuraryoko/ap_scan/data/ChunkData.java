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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import com.sakuraryoko.ap_scan.audio.LocationType;
import com.sakuraryoko.ap_scan.audio.NbtAudioUtil;
import com.sakuraryoko.ap_scan.util.*;

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
				id = nbt.getString("id", "");
				Identifier identifier = Identifier.tryParse(id);
				EntityType<?> entityType = NbtEntityUtils.getEntityTypeFromNbt(fixedNbt);
				final Text defName = entityType != null ? entityType.getName() : (identifier != null ? Text.of(identifier.getPath()) : Text.of(id));
				final Text customName = fixedNbt.get(NbtKeys.CUSTOM_NAME, TextCodecs.CODEC).orElse(defName);
				final UUID uuid = fixedNbt.get(NbtKeys.UUID, Uuids.INT_STREAM_CODEC).orElse(Uuids.getOfflinePlayerUuid(customName.getString()));
				final Vec3d pos = fixedNbt.get(NbtKeys.POS, Vec3d.CODEC).orElse(Vec3d.ZERO);

				NbtAudioUtil.processEachNbt(fixedNbt, registry, oldDataVersion, LocationType.ENTITY, EntityData.getEntityDesc(defName, customName, uuid, pos));
			}
		}
	}

	public static String getTileEntityDesc(Text defName, Text customName, BlockPos pos)
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

	private static void processChunkTileEntityEach(NbtCompound nbt, DynamicRegistryManager registry, int oldDataVersion)
	{
		String id = nbt.getString("id", "");
		boolean shouldFix = nbt.getInt("DataVersion", -1) < DataFixerUtils.CURRENT_SCHEMA;

		// Filter out all the unwanted Tile Entity Types
		if (IDList.TILE_ID_LIST.contains(id.toLowerCase()))
		{
			NbtCompound fixedNbt = shouldFix ? DataFixerUtils.fixTileEntity(nbt, oldDataVersion) : nbt;

//			System.out.printf("[TE] (MATCHED) nbt [%s]\n", fixedNbt.toString());
			id = nbt.getString("id", "");
			Identifier identifier = Identifier.tryParse(id);
			BlockPos pos = NbtBlockUtils.readBlockPos(fixedNbt);

			if (pos == null)
			{
				pos = BlockPos.ORIGIN;
			}

			final Text defName = (identifier != null ? Text.of(identifier.getPath()) : Text.of(id));
			Text customName = NbtBlockUtils.getCustomNameFromNbt(fixedNbt, registry, NbtKeys.SKULL_NAME);

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
