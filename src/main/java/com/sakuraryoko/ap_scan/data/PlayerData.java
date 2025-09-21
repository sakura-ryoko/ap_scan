package com.sakuraryoko.ap_scan.data;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import net.minecraft.inventory.Inventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.DynamicRegistryManager;

import com.sakuraryoko.ap_scan.ApScan;
import com.sakuraryoko.ap_scan.audio.NbtAudioUtil;
import com.sakuraryoko.ap_scan.util.NbtInventory;
import com.sakuraryoko.ap_scan.util.NbtKeys;
import com.sakuraryoko.ap_scan.util.NbtUtils;

public class PlayerData
{
	public static final PlayersFileFilter PLAYERS_FILE_FILTER	= new PlayersFileFilter();

	public static void readAudioFileListFromPath(Path dir)
	{
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, PLAYERS_FILE_FILTER))
		{
			int count = 0;

			for (Path entry : stream)
			{
				processEachPlayerDat(entry);
				count++;
			}

			ApScan.LOGGER.info("PlayerData: Scanned {} Player Data files.", count);
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
		NbtList enderItems = nbt.getListOrEmpty(NbtKeys.ENDER_ITEMS);
		NbtList inventory = nbt.getListOrEmpty(NbtKeys.INVENTORY);

		try (NbtInventory enderInv = NbtInventory.fromNbtList(enderItems, false, registry))
		{
			if (enderInv != null)
			{
//				enderInv.dumpInv();
				processEachInventory(enderInv.toInventory(NbtInventory.DEFAULT_SIZE));
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
				processEachInventory(inv.toInventory(NbtInventory.PLAYER_SIZE));
			}
		}
		catch (Exception err)
		{
			ApScan.LOGGER.error("processEachPlayerDat(): [INV] Exception from NbtInventory; {}", err.getLocalizedMessage());
		}
	}

	public static void processEachInventory(Inventory inv)
	{
		if (inv == null || inv.isEmpty())
		{
			return;
		}

		NbtAudioUtil.processEachInventory(inv, DataManager.getInstance().getRegistry());
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
