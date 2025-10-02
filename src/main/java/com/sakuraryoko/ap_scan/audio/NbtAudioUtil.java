package com.sakuraryoko.ap_scan.audio;

import java.util.Objects;
import javax.annotation.Nonnull;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.ProfileComponent;
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

import com.sakuraryoko.ap_scan.data.DataManager;
import com.sakuraryoko.ap_scan.util.InventoryUtils;
import com.sakuraryoko.ap_scan.util.NbtKeys;

public class NbtAudioUtil
{
	public static final String CUSTOM_SOUND = "CustomSound";
    public static final String CUSTOM_SOUND_RANDOM = "CustomSoundRandomized";

	public static void processEachNbt(NbtCompound nbt, @Nonnull DynamicRegistryManager registry, int oldDataVersion,
	                                  LocationType type, String desc)
	{
		processEachInventory(InventoryUtils.getNbtInventory(nbt, registry), registry, oldDataVersion, type, desc);
	}

	public static void processEachInventory(Inventory inv, @Nonnull DynamicRegistryManager registry, int oldDataVersion,
	                                        LocationType type, String desc)
	{
		if (inv == null || inv.isEmpty()) return;

		for (int i = 0; i < inv.size(); i++)
		{
			ItemStack entry = inv.getStack(i);

			if (!entry.isEmpty())
			{
				if (entry.isIn(ItemTags.BUNDLES))
				{
					processEachBundle(entry, registry, oldDataVersion, type, desc);
				}
				else if (entry.contains(DataComponentTypes.CONTAINER))
				{
					processEachContainer(entry, registry, oldDataVersion, type, desc);
				}
				else
				{
                    Pair<AudioFileList, LocationsList> pair = fromItemStack(entry, type, desc);

					if (!pair.getLeft().isEmpty())
					{
						DataManager.getInstance().getWorldList().addList(pair.getLeft());
						DataManager.getInstance().getLocationsList().addList(pair.getRight());
					}
				}
			}
		}
	}

	public static void processEachStacks(DefaultedList<ItemStack> stacks, @Nonnull DynamicRegistryManager registry, int oldDataVersion,
	                                     LocationType type, String desc)
	{
		if (stacks.isEmpty()) return;

		for (ItemStack stack : stacks)
		{
			if (stack.isIn(ItemTags.BUNDLES))
			{
				processEachBundle(stack, registry, oldDataVersion, type, desc);
			}
			else if (stack.contains(DataComponentTypes.CONTAINER))
			{
				processEachContainer(stack, registry, oldDataVersion, type, desc);
			}
			else
			{
				Pair<AudioFileList, LocationsList> pair = fromItemStack(stack, type, desc);

				if (!pair.getLeft().isEmpty())
				{
					DataManager.getInstance().getWorldList().addList(pair.getLeft());
					DataManager.getInstance().getLocationsList().addList(pair.getRight());
				}
			}
		}
	}

	public static void processEachBundle(ItemStack stack, @Nonnull DynamicRegistryManager registry, int oldDataVersion,
	                                     LocationType type, String desc)
	{
		processEachStacks(InventoryUtils.getBundleItems(stack), registry, oldDataVersion, type, desc);
	}

	public static void processEachContainer(ItemStack stack, @Nonnull DynamicRegistryManager registry, int oldDataVersion,
	                                        LocationType type, String desc)
	{
		processEachStacks(InventoryUtils.getStoredItems(stack), registry, oldDataVersion, type, desc);
	}

	public static Pair<AudioFileList, LocationsList> fromItemStack(ItemStack stack, LocationType type, String desc)
	{
		AudioFileList files = new AudioFileList();
		LocationsList locations = new LocationsList();

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
											(uuid) -> {
												files.add(new AudioFile(uuid.toString(), lore2));
												locations.add(new AudioDataLocation(uuid.toString(), type, desc));
											}
									);
						}
					}
                }
                else if (nbt.contains(CUSTOM_SOUND))
				{
					nbt.get(CUSTOM_SOUND, Uuids.INT_STREAM_CODEC).ifPresent(
							(uuid) -> {
								files.add(new AudioFile(uuid.toString(), lore2));
								locations.add(new AudioDataLocation(uuid.toString(), type, desc));
							}
					);
				}
			}
		}

		return Pair.of(files, locations);
	}

	public static void processEachSkull(NbtCompound nbt, @Nonnull DynamicRegistryManager registry, int oldDataVersion,
	                                    LocationType type, String desc)
	{
		AudioFileList files = new AudioFileList();
		LocationsList locations = new LocationsList();

		if (nbt.contains(NbtKeys.COMPONENTS))
		{
			NbtCompound comp = nbt.getCompoundOrEmpty(NbtKeys.COMPONENTS);
			NbtCompound data = null;
			String lore = null;
			String profile = null;

			if (!comp.isEmpty())
			{
				for (String key : comp.getKeys())
				{
					switch (key)
					{
						case "minecraft:custom_data" -> data = comp.get(key, NbtComponent.CODEC).orElse(NbtComponent.DEFAULT).copyNbt();
						case "minecraft:lore" ->
						{
							LoreComponent loreComp = comp.get(key, LoreComponent.CODEC).orElse(LoreComponent.DEFAULT);

							if (!loreComp.lines().isEmpty())
							{
								lore = loreComp.lines().getFirst().getString();
							}
						}
						case "minecraft:profile" ->
						{
							ProfileComponent profileComp = comp.get(key, ProfileComponent.CODEC).orElse(null);

							if (profileComp != null)
							{
								profile = profileComp.name().isPresent() ? profileComp.name().get() :
								          profileComp.uuid().isPresent() ? profileComp.uuid().get().toString() : null;
							}
						}
					}
				}

				if (data != null && !data.isEmpty())
				{
					final String lore2 = lore != null && !lore.isEmpty() ? lore :
					                     profile != null ? profile : "skull";

					if (data.contains(CUSTOM_SOUND))
					{
						data.get(CUSTOM_SOUND, Uuids.INT_STREAM_CODEC).ifPresent(
								(uuid) ->
								{
									files.add(new AudioFile(uuid.toString(), lore2));
									locations.add(new AudioDataLocation(uuid.toString(), type, desc));
								}
						);
					}
					else if (data.contains(CUSTOM_SOUND_RANDOM))
					{
						NbtList uuids = Objects.requireNonNull(nbt.get(CUSTOM_SOUND_RANDOM)).asNbtList().orElse(null);

						if (uuids != null)
						{
							for (NbtElement element : uuids)
							{
								Uuids.INT_STREAM_CODEC.parse(NbtOps.INSTANCE, element).resultOrPartial().ifPresent(
										(uuid) -> {
											files.add(new AudioFile(uuid.toString(), lore2));
											locations.add(new AudioDataLocation(uuid.toString(), type, desc));
										}
								);
							}
						}
					}
					else
					{
						return;
					}

					DataManager.getInstance().getWorldList().addList(files);
					DataManager.getInstance().getLocationsList().addList(locations);
				}
			}
		}
	}
}
