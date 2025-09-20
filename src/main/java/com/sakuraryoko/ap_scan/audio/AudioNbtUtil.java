package com.sakuraryoko.ap_scan.audio;

import java.util.UUID;
import javax.annotation.Nonnull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Uuids;

import com.sakuraryoko.ap_scan.ApScan;
import com.sakuraryoko.ap_scan.data.DataManager;
import com.sakuraryoko.ap_scan.util.InventoryUtils;

public class AudioNbtUtil
{
	public static final String CUSTOM_SOUND = "CustomSound";

	public static void processEachInventory(NbtCompound nbt, @Nonnull DynamicRegistryManager registry)
	{
		Inventory inv = InventoryUtils.getNbtInventory(nbt, registry);

		for (int i = 0; i < inv.size(); i++)
		{
			ItemStack entry = inv.getStack(i);

			if (!entry.isEmpty())
			{
				AudioFile file = fromItemStack(entry);

				if (file != null)
				{
					ApScan.LOGGER.warn("processEachInventory(): {}", file.toString());
					DataManager.getInstance().getWorldList().add(file);
				}
			}
		}
	}

	@Nullable
	public static AudioFile fromItemStack(ItemStack stack)
	{
		if (!stack.isEmpty() && stack.contains(DataComponentTypes.CUSTOM_DATA))
		{
			NbtComponent comp = stack.get(DataComponentTypes.CUSTOM_DATA);

			if (comp != null)
			{
				NbtCompound data = comp.copyNbt();

				if (data.contains(CUSTOM_SOUND))
				{
					UUID uuid = data.get(CUSTOM_SOUND, Uuids.INT_STREAM_CODEC).orElse(null);

					if (uuid != null)
					{
						String lore = stack.getName().getString();

						if (stack.contains(DataComponentTypes.LORE))
						{
							LoreComponent loreComp = stack.get(DataComponentTypes.LORE);

							if (loreComp != null)
							{
								lore = loreComp.lines().getFirst().getString();
							}
						}

						return new AudioFile(uuid.toString(), lore);
					}
				}
			}
		}

		return null;
	}
}
