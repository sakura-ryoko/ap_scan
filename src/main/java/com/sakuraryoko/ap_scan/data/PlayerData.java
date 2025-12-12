package com.sakuraryoko.ap_scan.data;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.Container;
import net.minecraft.world.phys.Vec3;
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
		CompoundTag nbt = NbtUtils.readNbtFromFileAsPath(file);

		if (nbt == null || nbt.isEmpty())
		{
			return;
		}

		RegistryAccess registry = DataManager.getInstance().getRegistry();
		int oldDataVersion = nbt.getIntOr("DataVersion", -1);
		boolean shouldFix = oldDataVersion < DataFixerUtils.CURRENT_SCHEMA;
		CompoundTag fixedNbt = shouldFix ? DataFixerUtils.fixPlayer(nbt, oldDataVersion) : nbt;

		ListTag enderItems = fixedNbt.getListOrEmpty(NbtKeys.ENDER_ITEMS);
		ListTag inventory = fixedNbt.getListOrEmpty(NbtKeys.INVENTORY);
		final UUID uuid = fixedNbt.read(NbtKeys.UUID, UUIDUtil.CODEC).orElse(UUID.fromString(FileNameUtils.getFileNameWithoutExtension(file.getFileName().toString())));
		final Vec3 pos = fixedNbt.read(NbtKeys.POS, Vec3.CODEC).orElse(Vec3.ZERO);
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

	public static String getPlayerDesc(UUID uuid, Vec3 pos)
	{
		return "Player[" +
				"{UUID="+uuid.toString()+"}" +
				",{Pos="+pos.toString()+"}" +
				"]";
	}

	public static void processEachInventory(Container inv, RegistryAccess registry, int oldDataVersion,
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
