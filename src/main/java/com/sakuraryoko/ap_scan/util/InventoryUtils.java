package com.sakuraryoko.ap_scan.util;

import java.util.Iterator;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.math.Fraction;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.collection.DefaultedList;

/**
 * Cloned from MaLiLib
 */
public class InventoryUtils
{
	/**
	 * Returns the list of items currently stored in the given Shulker Box
	 * (or other storage item with the same NBT data structure).
	 * Does not keep empty slots.
	 *
	 * @param stackIn The item holding the inventory contents
	 * @return ()
	 */
	public static DefaultedList<ItemStack> getStoredItems(ItemStack stackIn)
	{
		ContainerComponent container = stackIn.getComponents().get(DataComponentTypes.CONTAINER);

		if (container != null)
		{
			Iterator<ItemStack> iter = container.streamNonEmpty().iterator();
			DefaultedList<ItemStack> items = DefaultedList.ofSize((int) container.streamNonEmpty().count());
			int i = 0;

			// Using 'container.copyTo(items)' will break Litematica's Material List
			while (iter.hasNext())
			{
				items.add(iter.next().copy());
				i++;
			}

			return items;
		}

		return DefaultedList.of();
	}

	/**
	 * Returns the list of items currently stored in the given Shulker Box
	 * (or other storage item with the same NBT data structure).
	 * Preserves empty slots.
	 *
	 * @param stackIn   The item holding the inventory contents
	 * @param slotCount the maximum number of slots, and thus also the size of the list to create
	 * @return ()
	 */
	public static DefaultedList<ItemStack> getStoredItems(ItemStack stackIn, int slotCount)
	{
		ContainerComponent itemContainer = stackIn.getComponents().get(DataComponentTypes.CONTAINER);

		// Using itemContainer.copyTo() does not preserve empty stacks.
		if (itemContainer != null)
		{
			long defSlotCount = itemContainer.stream().count();

			// ContainerComponent.MAX_SLOTS = 256
			if (slotCount < 1)
			{
				slotCount = defSlotCount < 256 ? (int) defSlotCount : 256;
			}
			else
			{
				slotCount = Math.min(slotCount, 256);
			}

			DefaultedList<ItemStack> items = DefaultedList.ofSize(slotCount);
			Iterator<ItemStack> iter = itemContainer.stream().iterator();

			for (int i = 0; i < slotCount; i++)
			{
				ItemStack entry;

				if (iter.hasNext())
				{
					entry = iter.next();
				}
				else
				{
					entry = ItemStack.EMPTY;
				}

				items.add(entry.copy());
//                LOGGER.debug("getStoredItems()[{}] entry [{}], items [{}]", i, entry.toString(), items.get(i).toString());
			}

			return items;
		}
		else
		{
			return DefaultedList.of();
		}
	}

	/**
	 * Returns whether a bundle item stack has Items
	 * @param stack ()
	 * @return ()
	 */
	public static boolean bundleHasItems(ItemStack stack)
	{
		BundleContentsComponent bundleContainer = stack.getComponents().get(DataComponentTypes.BUNDLE_CONTENTS);

		if (bundleContainer != null)
		{
			return bundleContainer.isEmpty() == false;
		}

		return false;
	}

	/**
	 * Returns a Fraction value, probably indicating fill % value, rather than an actual item count.
	 *
	 * @param stack ()
	 * @return ()
	 */
	public static Fraction bundleOccupancy(ItemStack stack)
	{
		BundleContentsComponent bundleContainer = stack.getComponents().get(DataComponentTypes.BUNDLE_CONTENTS);

		if (bundleContainer != null)
		{
			return bundleContainer.getOccupancy();
		}

		return Fraction.ZERO;
	}

	/**
	 * Returns the "slot count" (Item Stacks) in the Bundle.
	 *
	 * @param stack ()
	 * @return ()
	 */
	public static int bundleCountItems(ItemStack stack)
	{
		BundleContentsComponent bundleContainer = stack.getComponents().get(DataComponentTypes.BUNDLE_CONTENTS);

		if (bundleContainer != null)
		{
			return bundleContainer.size();
		}

		return -1;
	}

	/**
	 * Returns a list of ItemStacks from the Bundle.  Does not preserve Empty Stacks.
	 *
	 * @param stackIn ()
	 * @return ()
	 */
	public static DefaultedList<ItemStack> getBundleItems(ItemStack stackIn)
	{
		BundleContentsComponent bundleContainer = stackIn.getComponents().getOrDefault(DataComponentTypes.BUNDLE_CONTENTS, BundleContentsComponent.DEFAULT);

		if (bundleContainer != null && bundleContainer.equals(BundleContentsComponent.DEFAULT) == false)
		{
			int maxSlots = bundleContainer.size();
			DefaultedList<ItemStack> items = DefaultedList.ofSize(maxSlots);
			Iterator<ItemStack> iter = bundleContainer.stream().iterator();

			while (iter.hasNext())
			{
				ItemStack slot = iter.next();

				if (slot.isEmpty() == false)
				{
					items.add(slot.copy());
				}
			}

			return items;
		}

		return DefaultedList.of();
	}

	/**
	 * Returns a list of ItemStacks from the Bundle.  Preserves Empty Stacks up to maxSlots.
	 *
	 * @param stackIn ()
	 * @param maxSlots ()
	 * @return ()
	 */
	public static DefaultedList<ItemStack> getBundleItems(ItemStack stackIn, int maxSlots)
	{
		BundleContentsComponent bundleContainer = stackIn.getComponents().getOrDefault(DataComponentTypes.BUNDLE_CONTENTS, BundleContentsComponent.DEFAULT);

		if (bundleContainer != null && bundleContainer.equals(BundleContentsComponent.DEFAULT) == false)
		{
			int defMaxSlots = bundleContainer.size();

			if (maxSlots < 1)
			{
				maxSlots = defMaxSlots;
			}
			else
			{
				maxSlots = maxSlots < 64 ? maxSlots : defMaxSlots;
			}

			DefaultedList<ItemStack> items = DefaultedList.ofSize(maxSlots);
			Iterator<ItemStack> iter = bundleContainer.stream().iterator();
			int limit = 0;

			while (iter.hasNext() && limit < maxSlots)
			{
				items.add(iter.next().copy());
				limit++;
			}

			return items;
		}

		return DefaultedList.of();
	}

	/**
	 * Checks if the given NBT currently contains any items, using the NBT Items[] interface.
	 *
	 * @param nbt ()
	 * @return ()
	 */
	public static boolean hasNbtItems(NbtCompound nbt)
	{
		if (nbt.contains(NbtKeys.ITEMS))
		{
			NbtList tagList = nbt.getListOrEmpty(NbtKeys.ITEMS);
			return !tagList.isEmpty();
		}
		else if (nbt.contains(NbtKeys.INVENTORY))
		{
			NbtList tagList = nbt.getListOrEmpty(NbtKeys.INVENTORY);
			return !tagList.isEmpty();
		}
		else if (nbt.contains(NbtKeys.ENDER_ITEMS))
		{
			NbtList tagList = nbt.getListOrEmpty(NbtKeys.ENDER_ITEMS);
			return !tagList.isEmpty();
		}
		else if (nbt.contains(NbtKeys.ITEM))
		{
			return true;
		}
		else if (nbt.contains(NbtKeys.ITEM_2))
		{
			return true;
		}
		else if (nbt.contains(NbtKeys.BOOK))
		{
			return true;
		}
		else return nbt.contains(NbtKeys.RECORD);
	}

	/**
	 * Returns Inventory of items currently stored in the given NBT Items[] interface.
	 * Preserves empty slots, unless the "Inventory" interface is used.
	 *
	 * @param nbt     The tag holding the inventory contents
	 * @return ()
	 */
	public static Inventory getNbtInventory(@Nonnull NbtCompound nbt, @Nonnull DynamicRegistryManager registry)
	{
		return getNbtInventory(nbt, -1, registry);
	}

	/**
	 * Returns Inventory of items currently stored in the given NBT Items[] interface.
	 * Preserves empty slots, unless the "Inventory" interface is used.
	 *
	 * @param nbt       The tag holding the inventory contents
	 * @param slotCount the maximum number of slots, and thus also the size of the list to create
	 * @param registry  The Dynamic Registry object
	 * @return ()
	 */
	public static Inventory getNbtInventory(@Nonnull NbtCompound nbt, int slotCount, @Nonnull DynamicRegistryManager registry)
	{
		if (slotCount > NbtInventory.MAX_SIZE)
		{
			slotCount = NbtInventory.MAX_SIZE;
		}

		if (nbt.contains(NbtKeys.ITEMS))
		{
			// Standard 'Items' tag for most Block Entities --
			// -- Furnace, Brewing Stand, Shulker Box, Crafter, Barrel, Chest, Dispenser, Hopper, Bookshelf, Campfire
			if (slotCount < 0)
			{
				// Uses slots
				NbtList list = nbt.getListOrEmpty(NbtKeys.ITEMS);
				slotCount = list.size();
			}

			slotCount = NbtInventory.getAdjustedSize(slotCount);

			NbtInventory nbtInv = NbtInventory.fromNbt(nbt, NbtKeys.ITEMS, false, registry);

			if (nbtInv == null || nbtInv.isEmpty())
			{
				return null;
			}

			return nbtInv.toInventory(slotCount);
		}
		else if (nbt.contains(NbtKeys.INVENTORY))
		{
			String id = nbt.getString(NbtKeys.ID, "");
			boolean isPlayer = Objects.equals(id, "minecraft:player");

			// Entities use this (Piglin, Villager, a few others)
			if (slotCount < 0)
			{
				NbtList list = nbt.getListOrEmpty(NbtKeys.INVENTORY);
				// Doesn't use slots
				slotCount = list.size();
			}

			slotCount = NbtInventory.getAdjustedSize(slotCount);

			// "Inventory" tags might not include Slot ID's, but a Player will.
			NbtInventory nbtInv = NbtInventory.fromNbt(nbt, NbtKeys.INVENTORY, !isPlayer, registry);

			if (nbtInv == null || nbtInv.isEmpty())
			{
				return null;
			}

			return nbtInv.toInventory(slotCount);
		}
		else if (nbt.contains(NbtKeys.ENDER_ITEMS))
		{
			// Ender Chest
			NbtList list = nbt.getListOrEmpty(NbtKeys.ENDER_ITEMS);

			if (slotCount < 0)
			{
				// Uses slots
				slotCount = Math.max(list.size(), NbtInventory.DEFAULT_SIZE);
			}

			slotCount = NbtInventory.getAdjustedSize(slotCount);
			NbtInventory nbtInv = NbtInventory.fromNbtList(list, false, registry);

			if (nbtInv == null || nbtInv.isEmpty())
			{
				return null;
			}

			return nbtInv.toInventory(Math.max(slotCount, NbtInventory.DEFAULT_SIZE));
		}
		else if (nbt.contains(NbtKeys.ITEM))
		{
			// item (DecoratedPot, ItemEntity)
			ItemStack entry = fromNbtOrEmpty(registry, nbt.get(NbtKeys.ITEM));
			SimpleInventory inv = new SimpleInventory(1);
			inv.setStack(0, entry.copy());

			return inv;
		}
		else if (nbt.contains(NbtKeys.ITEM_2))
		{
			// Item (Item Frame)
			ItemStack entry = fromNbtOrEmpty(registry, nbt.get(NbtKeys.ITEM_2));
			SimpleInventory inv = new SimpleInventory(1);
			inv.setStack(0, entry.copy());

			return inv;
		}
		else if (nbt.contains(NbtKeys.BOOK))
		{
			// Book (Lectern)
			ItemStack entry = fromNbtOrEmpty(registry, nbt.get(NbtKeys.BOOK));
			SimpleInventory inv = new SimpleInventory(1);
			inv.setStack(0, entry.copy());

			return inv;
		}
		else if (nbt.contains(NbtKeys.RECORD))
		{
			// RecordItem (Jukebox)
			ItemStack entry = fromNbtOrEmpty(registry, nbt.get(NbtKeys.RECORD));
			SimpleInventory inv = new SimpleInventory(1);
			inv.setStack(0, entry.copy());

			return inv;
		}

		return null;
	}

	/**
	 * Return an item stack from NBT, or Empty
	 * @param tag ()
	 * @param registry ()
	 * @return ()
	 */
	public static ItemStack fromNbtOrEmpty(@Nonnull DynamicRegistryManager registry, @Nullable NbtElement tag)
	{
		if (tag == null)
		{
			return ItemStack.EMPTY;
		}

		return ItemStack.CODEC.parse(registry.getOps(NbtOps.INSTANCE), tag).resultOrPartial().orElse(ItemStack.EMPTY);
	}

	/**
	 * Return an item stack from an NBT key
	 * @param nbt ()
	 * @param registry ()
	 * @param key ()
	 * @return ()
	 */
	public static ItemStack getStackCodec(@Nonnull NbtCompound nbt, @Nonnull DynamicRegistryManager registry, String key)
	{
		return nbt.get(key, ItemStack.CODEC, registry.getOps(NbtOps.INSTANCE)).orElse(ItemStack.EMPTY);
	}

	/**
	 * Insert an item stack into NBT using a key
	 * @param nbt ()
	 * @param registry ()
	 * @param stack ()
	 * @param key ()
	 * @return ()
	 */
	public static NbtCompound putStackCodec(@Nonnull NbtCompound nbt, @Nonnull DynamicRegistryManager registry, @Nonnull ItemStack stack, String key)
	{
		nbt.put(key, ItemStack.CODEC, registry.getOps(NbtOps.INSTANCE), stack);
		return nbt;
	}
}
