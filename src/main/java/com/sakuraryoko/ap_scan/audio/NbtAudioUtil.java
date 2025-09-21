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
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Uuids;
import net.minecraft.util.collection.DefaultedList;

import com.sakuraryoko.ap_scan.ApScan;
import com.sakuraryoko.ap_scan.data.DataManager;
import com.sakuraryoko.ap_scan.util.InventoryUtils;

public class NbtAudioUtil
{
	public static final String CUSTOM_SOUND = "CustomSound";

	public static void processEachNbt(NbtCompound nbt, @Nonnull DynamicRegistryManager registry, int oldDataVersion)
	{
		processEachInventory(InventoryUtils.getNbtInventory(nbt, registry), registry, oldDataVersion);
	}

	public static void processEachInventory(Inventory inv, @Nonnull DynamicRegistryManager registry, int oldDataVersion)
	{
		if (inv == null || inv.isEmpty()) return;

		for (int i = 0; i < inv.size(); i++)
		{
			ItemStack entry = inv.getStack(i);

			if (!entry.isEmpty())
			{
				if (entry.isIn(ItemTags.BUNDLES))
				{
					processEachBundle(entry, registry, oldDataVersion);
				}
				else if (entry.contains(DataComponentTypes.CONTAINER))
				{
					processEachContainer(entry, registry, oldDataVersion);
				}
				else
				{
					AudioFile file = fromItemStack(entry);

					if (file != null)
					{
						ApScan.LOGGER.warn("processEachInventory(): [INV] {}", file.toString());
						DataManager.getInstance().getWorldList().add(file);
					}
				}
			}
		}
	}

	public static void processEachStacks(DefaultedList<ItemStack> stacks, @Nonnull DynamicRegistryManager registry, int oldDataVersion)
	{
		if (stacks.isEmpty()) return;

		for (ItemStack stack : stacks)
		{
			if (stack.isIn(ItemTags.BUNDLES))
			{
				processEachBundle(stack, registry, oldDataVersion);
			}
			else if (stack.contains(DataComponentTypes.CONTAINER))
			{
				processEachContainer(stack, registry, oldDataVersion);
			}
			else
			{
				AudioFile file = fromItemStack(stack);

				if (file != null)
				{
					ApScan.LOGGER.warn("processEachStacks(): [STACKS] {}", file.toString());
					DataManager.getInstance().getWorldList().add(file);
				}
			}
		}
	}

	public static void processEachBundle(ItemStack stack, @Nonnull DynamicRegistryManager registry, int oldDataVersion)
	{
		processEachStacks(InventoryUtils.getBundleItems(stack), registry, oldDataVersion);
	}

	public static void processEachContainer(ItemStack stack, @Nonnull DynamicRegistryManager registry, int oldDataVersion)
	{
		processEachStacks(InventoryUtils.getStoredItems(stack), registry, oldDataVersion);
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

							if (loreComp != null && !loreComp.lines().isEmpty())
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
