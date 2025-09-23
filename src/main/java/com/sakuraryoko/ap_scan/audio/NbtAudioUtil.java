package com.sakuraryoko.ap_scan.audio;

import java.util.Objects;
import javax.annotation.Nonnull;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
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
    public static final String CUSTOM_SOUND_RANDOM = "CustomSoundRandomized";

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
                    AudioFileList files = fromItemStack(entry);

					for (AudioFile file : files.asList())
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
				DataManager.getInstance().getWorldList().addList(fromItemStack(stack));
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

	public static AudioFileList fromItemStack(ItemStack stack)
	{
		AudioFileList files = new AudioFileList();

		if (!stack.isEmpty() && stack.contains(DataComponentTypes.CUSTOM_DATA))
		{
			NbtComponent comp = stack.get(DataComponentTypes.CUSTOM_DATA);

			if (comp != null)
			{
				NbtCompound nbt = comp.copyNbt();

                String lore = stack.getName().getString();

                if (stack.contains(DataComponentTypes.LORE))
                {
                    LoreComponent loreComp = stack.getOrDefault(DataComponentTypes.LORE, LoreComponent.DEFAULT);

                    if (loreComp != null && !loreComp.lines().isEmpty())
                    {
                        lore = loreComp.lines().getFirst().getString();
                    }
                }

				final String lore2 = lore;

				if (nbt.contains(CUSTOM_SOUND_RANDOM))
                {
                    NbtList uuids = Objects.requireNonNull(nbt.get(CUSTOM_SOUND_RANDOM)).asNbtList().orElse(null);

					if (uuids != null)
					{
						for (NbtElement element : uuids)
						{
							Uuids.INT_STREAM_CODEC.parse(NbtOps.INSTANCE, element).resultOrPartial().ifPresent(
											(uuid) -> files.add(new AudioFile(uuid.toString(), lore2))
									);
						}
					}
                }
                else if (nbt.contains(CUSTOM_SOUND))
				{
					nbt.get(CUSTOM_SOUND, Uuids.INT_STREAM_CODEC).ifPresent(
							(uuid) -> files.add(new AudioFile(uuid.toString(), lore2))
					);
				}
			}
		}

		return files;
	}
}
