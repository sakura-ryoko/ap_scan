package com.sakuraryoko.ap_scan.util;

import java.util.Iterator;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.math.Fraction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;

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
	public static NonNullList<ItemStack> getStoredItems(ItemStack stackIn)
	{
		ItemContainerContents container = stackIn.getComponents().get(DataComponents.CONTAINER);

		if (container != null)
		{
			Iterator<ItemStack> iter = container.nonEmptyItemCopyStream().iterator();
			NonNullList<ItemStack> items = NonNullList.createWithCapacity((int) container.nonEmptyItemCopyStream().count());
			int i = 0;

			// Using 'container.copyTo(items)' will break Litematica's Material List
			while (iter.hasNext())
			{
				items.add(iter.next().copy());
				i++;
			}

			return items;
		}

		return NonNullList.create();
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
	public static NonNullList<ItemStack> getStoredItems(ItemStack stackIn, int slotCount)
	{
		ItemContainerContents itemContainer = stackIn.getComponents().get(DataComponents.CONTAINER);

		// Using itemContainer.copyTo() does not preserve empty stacks.
		if (itemContainer != null)
		{
			long defSlotCount = itemContainer.allItemsCopyStream().count();

			// ContainerComponent.MAX_SLOTS = 256
			if (slotCount < 1)
			{
				slotCount = defSlotCount < 256 ? (int) defSlotCount : 256;
			}
			else
			{
				slotCount = Math.min(slotCount, 256);
			}

			NonNullList<ItemStack> items = NonNullList.createWithCapacity(slotCount);
			Iterator<ItemStack> iter = itemContainer.allItemsCopyStream().iterator();

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
			return NonNullList.create();
		}
	}

	/**
	 * Returns whether a bundle item stack has Items
	 * @param stack ()
	 * @return ()
	 */
	public static boolean bundleHasItems(ItemStack stack)
	{
		BundleContents bundleContainer = stack.getComponents().get(DataComponents.BUNDLE_CONTENTS);

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
		BundleContents bundleContainer = stack.getComponents().get(DataComponents.BUNDLE_CONTENTS);

		if (bundleContainer != null)
		{
			return bundleContainer.weight().getOrThrow();
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
		BundleContents bundleContainer = stack.getComponents().get(DataComponents.BUNDLE_CONTENTS);

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
	public static NonNullList<ItemStack> getBundleItems(ItemStack stackIn)
	{
		BundleContents bundleContainer = stackIn.getComponents().getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);

		if (bundleContainer != null && bundleContainer.equals(BundleContents.EMPTY) == false)
		{
			int maxSlots = bundleContainer.size();
			NonNullList<ItemStack> items = NonNullList.createWithCapacity(maxSlots);
			Iterator<ItemStack> iter = bundleContainer.itemCopyStream().iterator();

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

		return NonNullList.create();
	}

	/**
	 * Returns a list of ItemStacks from the Bundle.  Preserves Empty Stacks up to maxSlots.
	 *
	 * @param stackIn ()
	 * @param maxSlots ()
	 * @return ()
	 */
	public static NonNullList<ItemStack> getBundleItems(ItemStack stackIn, int maxSlots)
	{
		BundleContents bundleContainer = stackIn.getComponents().getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);

		if (bundleContainer != null && bundleContainer.equals(BundleContents.EMPTY) == false)
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

			NonNullList<ItemStack> items = NonNullList.createWithCapacity(maxSlots);
			Iterator<ItemStack> iter = bundleContainer.itemCopyStream().iterator();
			int limit = 0;

			while (iter.hasNext() && limit < maxSlots)
			{
				items.add(iter.next().copy());
				limit++;
			}

			return items;
		}

		return NonNullList.create();
	}

	/**
	 * Checks if the given NBT currently contains any items, using the NBT Items[] interface.
	 *
	 * @param nbt ()
	 * @return ()
	 */
	public static boolean hasNbtItems(CompoundTag nbt)
	{
		if (nbt.contains(NbtKeys.ITEMS))
		{
			ListTag tagList = nbt.getListOrEmpty(NbtKeys.ITEMS);
			return !tagList.isEmpty();
		}
		else if (nbt.contains(NbtKeys.INVENTORY))
		{
			ListTag tagList = nbt.getListOrEmpty(NbtKeys.INVENTORY);
			return !tagList.isEmpty();
		}
		else if (nbt.contains(NbtKeys.ENDER_ITEMS))
		{
			ListTag tagList = nbt.getListOrEmpty(NbtKeys.ENDER_ITEMS);
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
	public static Container getNbtInventory(@Nonnull CompoundTag nbt, @Nonnull RegistryAccess registry)
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
	public static Container getNbtInventory(@Nonnull CompoundTag nbt, int slotCount, @Nonnull RegistryAccess registry)
	{
		if (slotCount > NbtInventory.MAX_SIZE)
		{
			slotCount = NbtInventory.MAX_SIZE;
		}

		if (nbt.contains(NbtKeys.LOOT_TABLE)) { return null; }
		if (nbt.contains(NbtKeys.ITEMS))
		{
			// Standard 'Items' tag for most Block Entities --
			// -- Furnace, Brewing Stand, Shulker Box, Crafter, Barrel, Chest, Dispenser, Hopper, Bookshelf, Campfire
			if (slotCount < 0)
			{
				// Uses slots
				ListTag list = nbt.getListOrEmpty(NbtKeys.ITEMS);
				slotCount = list.size();
			}

			slotCount = NbtInventory.getAdjustedSize(slotCount);

			NbtInventory nbtInv = NbtInventory.fromNbt(nbt, NbtKeys.ITEMS, false, registry);

			if (nbtInv == null || nbtInv.isEmpty())
			{
				return null;
			}

			return nbtInv.sorted().toInventory(slotCount);
		}
		else if (nbt.contains(NbtKeys.INVENTORY))
		{
			String id = nbt.getStringOr(NbtKeys.ID, "");
			boolean isPlayer = Objects.equals(id, "minecraft:player");
			ListTag list = nbt.getListOrEmpty(NbtKeys.INVENTORY);
			boolean noSlotId = list.isEmpty() ? !isPlayer : !nbtInventoryHasSlots(list);

			// Entities use this (Piglin, Villager, a few others)
			if (slotCount < 0)
			{
				// Doesn't use slots
				slotCount = list.size();
			}

			slotCount = NbtInventory.getAdjustedSize(slotCount);

			// "Inventory" tags might not include Slot ID's, but a Player will.
			NbtInventory nbtInv = NbtInventory.fromNbt(nbt, NbtKeys.INVENTORY, noSlotId, registry);

			if (nbtInv == null || nbtInv.isEmpty())
			{
				return null;
			}

			return nbtInv.sorted().toInventory(slotCount);
		}
		else if (nbt.contains(NbtKeys.ENDER_ITEMS))
		{
			// Ender Chest
			ListTag list = nbt.getListOrEmpty(NbtKeys.ENDER_ITEMS);

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

			return nbtInv.sorted().toInventory(Math.max(slotCount, NbtInventory.DEFAULT_SIZE));
		}
		else if (nbt.contains(NbtKeys.ITEM))
		{
			// item (DecoratedPot, ItemEntity)
			ItemStack entry = fromNbtOrEmpty(registry, nbt.get(NbtKeys.ITEM));
			SimpleContainer inv = new SimpleContainer(1);
			inv.setItem(0, entry.copy());

			return inv;
		}
		else if (nbt.contains(NbtKeys.ITEM_2))
		{
			// Item (Item Frame)
			ItemStack entry = fromNbtOrEmpty(registry, nbt.get(NbtKeys.ITEM_2));
			SimpleContainer inv = new SimpleContainer(1);
			inv.setItem(0, entry.copy());

			return inv;
		}
		else if (nbt.contains(NbtKeys.BOOK))
		{
			// Book (Lectern)
			ItemStack entry = fromNbtOrEmpty(registry, nbt.get(NbtKeys.BOOK));
			SimpleContainer inv = new SimpleContainer(1);
			inv.setItem(0, entry.copy());

			return inv;
		}
		else if (nbt.contains(NbtKeys.RECORD))
		{
			// RecordItem (Jukebox)
			ItemStack entry = fromNbtOrEmpty(registry, nbt.get(NbtKeys.RECORD));
			SimpleContainer inv = new SimpleContainer(1);
			inv.setItem(0, entry.copy());

			return inv;
		}

		return null;
	}

	private static boolean nbtInventoryHasSlots(@Nonnull ListTag list)
	{
		for (int i = 0; i < list.size(); i++)
		{
			CompoundTag entry = list.getCompoundOrEmpty(i);
			if (entry.contains(NbtKeys.SLOT)) { return true; }
		}

		return false;
	}

	/**
	 * Return an item stack from NBT, or Empty
	 * @param tag ()
	 * @param registry ()
	 * @return ()
	 */
	public static ItemStack fromNbtOrEmpty(@Nonnull RegistryAccess registry, @Nullable Tag tag)
	{
		if (tag == null)
		{
			return ItemStack.EMPTY;
		}

		return ItemStack.CODEC.parse(registry.createSerializationContext(NbtOps.INSTANCE), tag).resultOrPartial().orElse(ItemStack.EMPTY);
	}

	/**
	 * Return an item stack from an NBT key
	 * @param nbt ()
	 * @param registry ()
	 * @param key ()
	 * @return ()
	 */
	public static ItemStack getStackCodec(@Nonnull CompoundTag nbt, @Nonnull RegistryAccess registry, String key)
	{
		return nbt.read(key, ItemStack.CODEC, registry.createSerializationContext(NbtOps.INSTANCE)).orElse(ItemStack.EMPTY);
	}

	/**
	 * Insert an item stack into NBT using a key
	 * @param nbt ()
	 * @param registry ()
	 * @param stack ()
	 * @param key ()
	 * @return ()
	 */
	public static CompoundTag putStackCodec(@Nonnull CompoundTag nbt, @Nonnull RegistryAccess registry, @Nonnull ItemStack stack, String key)
	{
		nbt.store(key, ItemStack.CODEC, registry.createSerializationContext(NbtOps.INSTANCE), stack);
		return nbt;
	}
}
