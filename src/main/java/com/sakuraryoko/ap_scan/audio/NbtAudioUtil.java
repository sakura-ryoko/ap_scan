package com.sakuraryoko.ap_scan.audio;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nonnull;
import de.maxhenkel.audioplayer.audioloader.Metadata;
import org.apache.commons.lang3.tuple.Pair;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.ResolvableProfile;

import com.sakuraryoko.ap_scan.ApScan;
import com.sakuraryoko.ap_scan.Reference;
import com.sakuraryoko.ap_scan.audio.data.AudioDataWrapper;
import com.sakuraryoko.ap_scan.data.DataManager;
import com.sakuraryoko.ap_scan.util.InventoryUtils;
import com.sakuraryoko.ap_scan.util.NbtKeys;

public class NbtAudioUtil
{
	public static void processEachNbt(CompoundTag nbt, @Nonnull RegistryAccess registry, int oldDataVersion,
	                                  LocationType type, String desc)
	{
		processEachInventory(InventoryUtils.getNbtInventory(nbt, registry), registry, oldDataVersion, type, desc);
	}

	public static void processEachInventory(Container inv, @Nonnull RegistryAccess registry, int oldDataVersion,
	                                        LocationType type, String desc)
	{
		if (inv == null || inv.isEmpty()) return;

		for (int i = 0; i < inv.getContainerSize(); i++)
		{
			ItemStack entry = inv.getItem(i);

			if (!entry.isEmpty())
			{
				if (entry.is(ItemTags.BUNDLES))
				{
					processEachBundle(entry, registry, oldDataVersion, type, desc);
				}
				else if (entry.has(DataComponents.CONTAINER))
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

	public static void processEachStacks(NonNullList<ItemStack> stacks, @Nonnull RegistryAccess registry, int oldDataVersion,
	                                     LocationType type, String desc)
	{
		if (stacks.isEmpty()) return;

		for (ItemStack stack : stacks)
		{
			if (stack.is(ItemTags.BUNDLES))
			{
				processEachBundle(stack, registry, oldDataVersion, type, desc);
			}
			else if (stack.has(DataComponents.CONTAINER))
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

	public static void processEachBundle(ItemStack stack, @Nonnull RegistryAccess registry, int oldDataVersion,
	                                     LocationType type, String desc)
	{
		processEachStacks(InventoryUtils.getBundleItems(stack), registry, oldDataVersion, type, desc);
	}

	public static void processEachContainer(ItemStack stack, @Nonnull RegistryAccess registry, int oldDataVersion,
	                                        LocationType type, String desc)
	{
		processEachStacks(InventoryUtils.getStoredItems(stack), registry, oldDataVersion, type, desc);
	}

	public static Pair<AudioFileList, LocationsList> fromItemStack(ItemStack stack, LocationType type, String desc)
	{
		AudioFileList files = new AudioFileList();
		LocationsList locations = new LocationsList();

		AudioDataWrapper data = AudioDataWrapper.fromItem(stack);

		if (data != null)
		{
			String lore = stack.getHoverName().getString();

			if (stack.has(DataComponents.LORE))
			{
				ItemLore loreComp = stack.getOrDefault(DataComponents.LORE, ItemLore.EMPTY);

				if (loreComp != null && !loreComp.lines().isEmpty())
				{
					lore = loreComp.lines().getFirst().getString();
				}
			}

			final UUID uuid = data.getId();
			Metadata meta = new Metadata(uuid);

			meta.setFileName(lore);
			files.add(new AudioFileV2(uuid, meta));
			locations.add(new AudioDataLocationV2(uuid, type, desc, data));

			// Unroll the RNG module
			List<UUID> rngList = data.getRandomIds();

			if (rngList != null && !rngList.isEmpty())
			{
				for (UUID id : rngList)
				{
					if (!id.equals(uuid))
					{
						AudioDataWrapper rngData = AudioDataWrapper.fromId(id, null);
						Metadata rngMeta = new Metadata(id);
						files.add(new AudioFileV2(id, rngMeta));
						locations.add(new AudioDataLocationV2(id, type, desc, rngData));
					}
				}
			}
		}

		return Pair.of(files, locations);
	}

	public static void processEachSkull(CompoundTag nbt, @Nonnull RegistryAccess registry, int oldDataVersion,
	                                    LocationType type, String desc)
	{
		AudioFileList files = new AudioFileList();
		LocationsList locations = new LocationsList();
		CustomData cd = null;
		CompoundTag data = null;
		String lore = null;
		String profile = null;
		Component itemName = null;
		Component customName = null;
		String adjDesc = "";

		if (nbt.contains(NbtKeys.COMPONENTS))
		{
			CompoundTag comp = nbt.getCompoundOrEmpty(NbtKeys.COMPONENTS);

			if (!comp.isEmpty())
			{
				for (String key : comp.keySet())
				{
					switch (key)
					{
						case "minecraft:custom_data", "custom_data" ->
								cd = comp.read(key, CustomData.CODEC).orElse(CustomData.EMPTY);
						case "minecraft:lore", "lore" ->
						{
							ItemLore loreComp = comp.read(key, ItemLore.CODEC).orElse(ItemLore.EMPTY);

							if (!loreComp.lines().isEmpty())
							{
								lore = loreComp.lines().getFirst().getString();
							}
						}
						case "minecraft:profile", "profile" ->
						{
							ResolvableProfile profileComp = comp.read(key, ResolvableProfile.CODEC).orElse(null);

							if (profileComp != null)
							{
								GameProfile gameProfile = profileComp.partialProfile();
								Optional<String> optName = profileComp.name();
								AtomicReference<String> name = new AtomicReference<>();
								optName.ifPresentOrElse(name::set, () -> name.set(gameProfile.name()));
								profile = name.get();
							}
						}
						case "minecraft:item_name", "item_name" ->
								itemName = comp.read(key, ComponentSerialization.CODEC).orElse(null);
						case "minecraft:custom_name", "custom_name" ->
								customName = comp.read(key, ComponentSerialization.CODEC).orElse(null);
					}
				}
			}

			// It might not always be listed under the "components" tag
			if (nbt.contains(NbtKeys.PROFILE))
			{
				ResolvableProfile profileComp = nbt.read(NbtKeys.PROFILE, ResolvableProfile.CODEC).orElse(null);

				if (profileComp != null)
				{
					GameProfile gameProfile = profileComp.partialProfile();
					Optional<String> optName = profileComp.name();
					AtomicReference<String> name = new AtomicReference<>();
					optName.ifPresentOrElse(name::set, () -> name.set(gameProfile.name()));
					profile = name.get();
				}
			}

			String lore2;

			if (lore != null && !lore.isEmpty())
			{
				lore2 = lore;
			}
			else if (profile != null && !profile.isEmpty())
			{
				lore2 = profile;
			}
			else if (itemName != null && !itemName.getString().isEmpty())
			{
				lore2 = itemName.getString();
			}
			else if (customName != null && !customName.getString().isEmpty())
			{
				lore2 = customName.getString();
			}
			else
			{
				lore2 = "skull";
			}

			if (profile == null || profile.isEmpty())
			{
				profile = lore2;
			}

			adjDesc = desc.replaceAll("Name=skull", String.format("Name=\"%s\"", profile));

//			if (Reference.DEBUG)
//			{
//				System.out.printf("SKULL LORE2: --> %s\n", lore2);
//				System.out.printf("SKULL ADJ-DESC: --> %s\n", adjDesc);
//			}
		}

		if (cd != null)
		{
			data = cd.copyTag();
		}
		else if (nbt.contains(AudioDataWrapper.AUDIO_PLAYER_TAG))
		{
			data = nbt.getCompoundOrEmpty(AudioDataWrapper.AUDIO_PLAYER_TAG);
		}
		else
		{
			data = new CompoundTag();
		}

		if (!data.isEmpty())
		{
			try
			{
				AudioDataWrapper audioData = AudioDataWrapper.fromNbt(data);

				if (audioData != null)
				{
					final UUID uuid = audioData.getId();
					Metadata meta = new Metadata(uuid);

					meta.setFileName(lore);
					files.add(new AudioFileV2(uuid, meta));
					locations.add(new AudioDataLocationV2(uuid, type, adjDesc, audioData));

					// Unroll the RNG module
					List<UUID> rngList = audioData.getRandomIds();

					if (rngList != null && !rngList.isEmpty())
					{
						for (UUID id : rngList)
						{
							if (!id.equals(uuid))
							{
								AudioDataWrapper rngData = AudioDataWrapper.fromId(id, null);
								Metadata rngMeta = new Metadata(id);
								files.add(new AudioFileV2(id, rngMeta));
								locations.add(new AudioDataLocationV2(id, type, adjDesc, rngData));
							}
						}
					}

				}
			}
			catch (Exception e)
			{
				if (Reference.DEBUG)
				{
					ApScan.LOGGER.error("NbtAudioUtil#processEachSkull(): Exception parsing skull entry; {}", e.getLocalizedMessage());
				}
			}

            if (!files.isEmpty())
            {
                DataManager.getInstance().getWorldList().addList(files);
                DataManager.getInstance().getLocationsList().addList(locations);
            }
        }
	}
}
