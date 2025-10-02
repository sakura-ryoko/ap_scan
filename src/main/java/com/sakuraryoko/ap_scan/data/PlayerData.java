package com.sakuraryoko.ap_scan.data;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import net.minecraft.inventory.Inventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.Vec3d;

import com.sakuraryoko.ap_scan.ApScan;
import com.sakuraryoko.ap_scan.audio.LocationType;
import com.sakuraryoko.ap_scan.audio.NbtAudioUtil;
import com.sakuraryoko.ap_scan.util.*;

public class PlayerData
{
	public static final PlayersFileFilter PLAYERS_FILE_FILTER	= new PlayersFileFilter();

	public static void readAudioFileListFromPath(Path dir)
	{
		ApScan.debugLog("PlayersFileFilter#readAudioFileListFromPath(): Reading Player Data files ...");

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, PLAYERS_FILE_FILTER))
		{
			int count = 0;

			for (Path entry : stream)
			{
				ApScan.debugLog("PlayersFileFilter#readAudioFileListFromPath(): Each file '{}'", entry.getFileName().toString());
				processEachPlayerDat(entry);
				count++;
			}

			ApScan.LOGGER.warn("PlayerData: Scanned [{}] Player Data files.", count);
		}
		catch (Exception err)
		{
			ApScan.LOGGER.error("DataManager#readAudioFileListFromPath(): Exception reading file: '{}'; {}", dir.toAbsolutePath().toString(), err.getLocalizedMessage());
		}
	}

	public static void processEachPlayerDat(Path file)
	{
		NbtCompound nbt = NbtUtils.readNbtFromFileAsPath(file);

		if (nbt == null || nbt.isEmpty())
		{
			return;
		}

		DynamicRegistryManager registry = DataManager.getInstance().getRegistry();
		int oldDataVersion = nbt.getInt("DataVersion", -1);
		boolean shouldFix = oldDataVersion < DataFixerUtils.CURRENT_SCHEMA;
		NbtCompound fixedNbt = shouldFix ? DataFixerUtils.fixPlayer(nbt, oldDataVersion) : nbt;

		NbtList enderItems = fixedNbt.getListOrEmpty(NbtKeys.ENDER_ITEMS);
		NbtList inventory = fixedNbt.getListOrEmpty(NbtKeys.INVENTORY);
		final UUID uuid = fixedNbt.get(NbtKeys.UUID, Uuids.INT_STREAM_CODEC).orElse(UUID.fromString(FileNameUtils.getFileNameWithoutExtension(file.getFileName().toString())));
		final Vec3d pos = fixedNbt.get(NbtKeys.POS, Vec3d.CODEC).orElse(Vec3d.ZERO);
		final String desc = getPlayerDesc(uuid, pos);

//		System.out.printf("PLAYER: nbt [%s]\n", fixedNbt.toString());

		try (NbtInventory enderInv = NbtInventory.fromNbtList(enderItems, false, registry))
		{
			if (enderInv != null)
			{
//				enderInv.dumpInv();
				processEachInventory(enderInv.toInventory(NbtInventory.DEFAULT_SIZE), registry, oldDataVersion,
				                     LocationType.PLAYER_ENDER_CHEST, desc);
			}
		}
		catch (Exception err)
		{
			ApScan.LOGGER.error("processEachPlayerDat(): [ENDER] Exception from NbtInventory; {}", err.getLocalizedMessage());
		}

		try (NbtInventory inv = NbtInventory.fromNbtList(inventory, false, registry))
		{
			if (inv != null)
			{
//				inv.dumpInv();
				processEachInventory(inv.toInventory(NbtInventory.PLAYER_SIZE), registry, oldDataVersion,
				                     LocationType.PLAYER_INVENTORY, desc);
			}
		}
		catch (Exception err)
		{
			ApScan.LOGGER.error("processEachPlayerDat(): [INV] Exception from NbtInventory; {}", err.getLocalizedMessage());
		}
	}

	public static String getPlayerDesc(UUID uuid, Vec3d pos)
	{
		return "Player[" +
				"{UUID="+uuid.toString()+"}" +
				",{Pos="+pos.toString()+"}" +
				"]";
	}

	public static void processEachInventory(Inventory inv, DynamicRegistryManager registry, int oldDataVersion,
	                                        LocationType type, String desc)
	{
		if (inv == null || inv.isEmpty())
		{
			return;
		}

		NbtAudioUtil.processEachInventory(inv, registry, oldDataVersion, type, desc);
	}

	public static class PlayersFileFilter implements DirectoryStream.Filter<Path>
	{
		@Override
		public boolean accept(Path entry) throws IOException
		{
			try
			{
				if (Files.isRegularFile(entry))
				{
					String file = entry.getFileName().toString();

					return (file.endsWith(".dat"));
				}
			}
			catch (Exception err)
			{
				throw new IOException(err.getMessage());
			}

			return false;
		}
	}
}
