package com.sakuraryoko.ap_scan.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;

import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.inventory.StackWithSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.MathHelper;

import com.sakuraryoko.ap_scan.ApScan;
import com.sakuraryoko.ap_scan.Reference;
import com.sakuraryoko.ap_scan.data.IDList;

/**
 * This makes reading / Writing Inventories to / from NBT a piece of cake.
 * Supports Inventory, Nbt, or DefaultList<> interfaces; and uses the newer Mojang
 * 'StackWithSlot' system.
 *********************** -
 * Cloned from MaLiLib
 */
public class NbtInventory implements AutoCloseable
{
	public static Logger LOGGER = ApScan.LOGGER;
    public static final int VILLAGER_SIZE = 8;
    public static final int DEFAULT_SIZE = 27;
    public static final int PLAYER_SIZE = 36;
    public static final int DOUBLE_SIZE = 54;
    public static final int MAX_SIZE = 256;
    private HashSet<StackWithSlot> items;

    private NbtInventory() {}

    public static NbtInventory create(int size)
    {
        NbtInventory newInv = new NbtInventory();

        //LOGGER.info("init() size: [{}]", size);
        size = getAdjustedSize(MathHelper.clamp(size, 1, MAX_SIZE));
        newInv.buildEmptyList(size);

        return newInv;
    }

    private void buildEmptyList(int size) throws RuntimeException
    {
        if (this.items != null)
        {
            throw new RuntimeException("List not empty!");
        }

        this.items = new HashSet<>();

        for (int i = 0; i < size; i++)
        {
            this.items.add(new StackWithSlot(i, ItemStack.EMPTY));
        }
    }

    public boolean isEmpty()
    {
        if (this.items == null || this.items.isEmpty())
        {
            return true;
        }

        AtomicBoolean bool = new AtomicBoolean(true);

        this.items.forEach(
                (slot) ->
                {
                    if (!slot.stack().isEmpty())
                    {
                        bool.set(false);
                    }
                }
        );

        return bool.get();
    }

    public int size()
    {
        if (this.items == null)
        {
            return -1;
        }

        return this.items.size();
    }

    /**
     * Return this Inventory as a DefaultList<ItemStack>
     * @return ()
     */
    public DefaultedList<ItemStack> toVanillaList(int size)
    {
        if (this.isEmpty())
        {
            return DefaultedList.of();
        }

        size = getAdjustedSize(Math.clamp(size, this.size(), MAX_SIZE));

        DefaultedList<ItemStack> list = DefaultedList.ofSize(size, ItemStack.EMPTY);
        AtomicInteger i = new AtomicInteger(0);

        this.items.forEach(
                (slot) ->
                    {
                        list.set(slot.slot(), slot.stack());
                        //LOGGER.info("toVanillaList():[{}]: slot [{}], stack: [{}]", i.get(), slot.slot(), slot.stack().toString());
                        i.getAndIncrement();
                    }
        );

        return list;
    }

    /**
     * Create a new NbtInventory from a DefaultedList<ItemStack>; making all the slot numbers the stack index.
     * @param list ()
     * @return ()
     */
    public static @Nullable NbtInventory fromVanillaList(@Nonnull DefaultedList<ItemStack> list)
    {
        int size = list.size();

        if (size < 1)
        {
            return null;
        }

        size = getAdjustedSize(MathHelper.clamp(size, 1, MAX_SIZE));
        NbtInventory newInv = new NbtInventory();
        newInv.items = new HashSet<>();

        for (int i = 0; i < size; i++)
        {
            StackWithSlot slot = new StackWithSlot(i, list.get(i));
            //LOGGER.info("fromVanillaList():[{}]: slot [{}], stack: [{}]", i, slot.slot(), slot.stack().toString());
            newInv.items.add(slot);
        }

        return newInv;
    }

    /**
     * Convert this Inventory to a Vanilla Inventory object.
     * Supports oversized Inventories (MAX_SIZE) and DoubleInventory (DOUBLE_SIZE); or defaults to (DEFAULT_SIZE)
     * @return ()
     */
    public @Nullable Inventory toInventory(final int size)
    {
        if (this.isEmpty())
        {
            return null;
        }

        int sizeAdj = getAdjustedSize(Math.clamp(size, this.size(), MAX_SIZE));
        Inventory inv = new SimpleInventory(sizeAdj);

        //LOGGER.warn("toInventory(): sizeAdj [{}] -> inv size [{}]", sizeAdj, inv.size());
        AtomicInteger i = new AtomicInteger(0);

        this.items.forEach(
                (slot) ->
                {
                    //LOGGER.info("toInventory():[{}]: slot [{}], stack: [{}]", i.get(), slot.slot(), slot.stack().toString());
                    inv.setStack(slot.slot(), slot.stack());
                    i.getAndIncrement();
                }
        );

        return inv;
    }

    /**
     * Creates a new NbtInventory from a vanilla Inventory object; making all the slot numbers the stack index.
     * @param inv ()
     * @return ()
     */
    public static NbtInventory fromInventory(@Nonnull Inventory inv)
    {
        NbtInventory newInv = new NbtInventory();
        List<Integer> slotsUsed = new ArrayList<>();
        int size = inv.size();
        int maxSlot = 0;

        size = getAdjustedSize(MathHelper.clamp(size, 1, MAX_SIZE));
        newInv.items = new HashSet<>();

        for (int i = 0; i < size; i++)
        {
            StackWithSlot slot = new StackWithSlot(i, inv.getStack(i));
            //LOGGER.info("fromInventory():[{}]: slot [{}], stack: [{}]", i, slot.slot(), slot.stack().toString());
            newInv.items.add(slot);
            slotsUsed.add(slot.slot());

            if (slot.slot() > maxSlot)
            {
                maxSlot = slot.slot();
            }
        }

        newInv.verifySize(slotsUsed, maxSlot);

        return newInv;
    }

    /**
     * Uses the newer Vanilla 'WriterView' interface to write this Inventory to it; using our 'NbtView' wrapper.
     * @param registry ()
     * @return ()
     */
    public @Nullable NbtView toNbtWriterView(@Nonnull DynamicRegistryManager registry)
    {
        if (this.isEmpty())
        {
            return null;
        }

        final int size = getAdjustedSize(this.size());

        NbtView view = NbtView.getWriter(registry);
        DefaultedList<ItemStack> list = this.toVanillaList(size);

        Inventories.writeData(Objects.requireNonNull(view.getWriter()), list);

        return view;
    }

    /**
     * Uses the newer Vanilla 'ReaderView' interface to create a new NbtInventory; using our 'NbtView' wrapper.
     * @param view ()
     * @param size ()
     * @return ()
     */
    public static @Nullable NbtInventory fromNbtReaderView(@Nonnull NbtView view, int size)
    {
        if (size < 1)
        {
            return null;
        }

        size = getAdjustedSize(MathHelper.clamp(size, 1, MAX_SIZE));
        DefaultedList<ItemStack> list = DefaultedList.ofSize(size, ItemStack.EMPTY);

        Inventories.readData(Objects.requireNonNull(view.getReader()), list);
        return fromVanillaList(list);
    }

    /**
     * Converts the first Inventory element to a single NbtElement.
     * @return ()
     * @throws RuntimeException ()
     */
    public NbtElement toNbtSingle(@Nonnull DynamicRegistryManager registry) throws RuntimeException
    {
        if (this.size() > 1)
        {
            throw new RuntimeException("Inventory is too large for a single entry!");
        }

        StackWithSlot slot = this.items.stream().findFirst().orElseThrow();

        if (!slot.stack().isEmpty())
        {
            NbtElement element = StackWithSlot.CODEC.encodeStart(registry.getOps(NbtOps.INSTANCE), slot).getPartialOrThrow();
//            LOGGER.info("toNbtSingle(): --> nbt: [{}]", element.toString());
            return element;
        }

        return new NbtCompound();
    }

    /**
     * Converts this Inventory to a basic NbtList with Slot information.
     * @return ()
     * @throws RuntimeException ()
     */
    public NbtList toNbtList(@Nonnull DynamicRegistryManager registry) throws RuntimeException
    {
        NbtList nbt = new NbtList();

        if (this.isEmpty())
        {
            return nbt;
        }

        this.items.forEach(
                (slot) ->
                {
                    if (!slot.stack().isEmpty())
                    {
                        NbtElement element = StackWithSlot.CODEC.encodeStart(registry.getOps(NbtOps.INSTANCE), slot).getPartialOrThrow();
                        //LOGGER.info("toNbtList(): slot [{}] --> nbt: [{}]", slot.slot(), element.toString());
                        nbt.add(element);
                    }
                }
        );

        return nbt;
    }

    /**
     * Writes this Inventory to a Nbt Type (List or Compound) using a key; with slot information.
     * @param type ()
     * @param key ()
     * @return ()
     * @throws RuntimeException ()
     */
    public NbtCompound toNbt(NbtType<?> type, String key, @Nonnull DynamicRegistryManager registry) throws RuntimeException
    {
        NbtCompound nbt = new NbtCompound();

        if (type == NbtList.TYPE)
        {
            NbtList list = this.toNbtList(registry);

            if (list.isEmpty())
            {
                return nbt;
            }

            nbt.put(key, list);

            return nbt;
        }
        else if (type == NbtCompound.TYPE)
        {
            nbt.put(key, this.toNbtSingle(registry));

            return nbt;
        }

        throw new RuntimeException("Unsupported Nbt Type!");
    }

    /**
     * Creates a new NbtInventory from a Nbt Type (List or Compound) using a key; retains slot information.
     * @param nbtIn ()
     * @param key ()
     * @param noSlotId (If the List doesn't include Slots, generate them using inventory index)
     * @return ()
     * @throws RuntimeException ()
     */
    public static @Nullable NbtInventory fromNbt(@Nonnull NbtCompound nbtIn, String key, boolean noSlotId, @Nonnull DynamicRegistryManager registry) throws RuntimeException
    {
        if (nbtIn.isEmpty() || !nbtIn.contains(key))
        {
            return null;
        }

        if (Objects.requireNonNull(nbtIn.get(key)).getNbtType() == NbtList.TYPE)
        {
            return fromNbtList(nbtIn.getListOrEmpty(key), noSlotId, registry);
        }
        else if (Objects.requireNonNull(nbtIn.get(key)).getNbtType() == NbtCompound.TYPE)
        {
            return fromNbtSingle(nbtIn.getCompoundOrEmpty(key), registry);
        }
        else
        {
            throw new RuntimeException("Invalid Nbt Type!");
        }
    }

    /**
     * Creates a new NbtInventory from a single-member NbtCompound containing a single item with a slot number.
     * @param nbt ()
     * @return ()
     * @throws RuntimeException ()
     */
    public static @Nullable NbtInventory fromNbtSingle(@Nonnull NbtCompound nbt, @Nonnull DynamicRegistryManager registry) throws RuntimeException
    {
        if (nbt.isEmpty())
        {
            return null;
        }

        NbtInventory newInv = new NbtInventory();

        newInv.items = new HashSet<>();
        StackWithSlot slot = StackWithSlot.CODEC.parse(registry.getOps(NbtOps.INSTANCE), nbt).getPartialOrThrow();
        LOGGER.info("fromNbtSingle(): slot [{}], stack: [{}]", slot.slot(), slot.stack().toString());
        newInv.items.add(slot);

        return newInv;
    }

    /**
     * Creates a new NbtInventory from an NbtList; utilizing Slot information.
     * @param list ()
     * @param noSlotId (If the List doesn't include Slots, generate them using inventory index)
     * @return ()
     * @throws RuntimeException ()
     */
    public static @Nullable NbtInventory fromNbtList(@Nonnull NbtList list, boolean noSlotId, @Nonnull DynamicRegistryManager registry) throws RuntimeException
    {
        if (list.isEmpty())
        {
            return null;
        }
        else if (list.size() > MAX_SIZE)
        {
            throw new RuntimeException("Nbt List is too large!");
        }

        int size = list.size();
        size = getAdjustedSize(MathHelper.clamp(size, 1, MAX_SIZE));
        NbtInventory newInv = new NbtInventory();
        List<Integer> slotsUsed = new ArrayList<>();
        int maxSlot = 0;

        newInv.items = new HashSet<>();
        //LOGGER.info("fromNbtList(): listSize: [{}], invSize: [{}]", list.size(), size);

        for (int i = 0; i < list.size(); i++)
        {
            StackWithSlot slot;
			NbtCompound nbt = checkForIDOverrides((NbtCompound) list.get(i));
			String id = nbt.getString(NbtKeys.ID, "");

			if (IDList.ITEM_ID_LIST.contains(id) && Reference.DEBUG)
			{
				LOGGER.info("fromNbtList(): [{}]: NBT/ITEM: [{}]", i, nbt.toString());
			}

	        if (IDList.TILE_ID_LIST.contains(id) && Reference.DEBUG)
	        {
		        LOGGER.info("fromNbtList(): [{}]: NBT/TILE: [{}]", i, nbt.toString());
	        }

	        // Some lists, such as the "Inventory" tag does not include slot ID's
            if (noSlotId)
            {
                slot = new StackWithSlot(i, ItemStack.CODEC.parse(registry.getOps(NbtOps.INSTANCE), nbt).getPartialOrThrow());
            }
            else
            {
                slot = StackWithSlot.CODEC.parse(registry.getOps(NbtOps.INSTANCE), nbt).getPartialOrThrow();
            }

//            LOGGER.info("fromNbtList(): [{}]: slot [{}], stack: [{}]", i, slot.slot(), slot.stack().toString());
            newInv.items.add(slot);
            slotsUsed.add(slot.slot());

            if (slot.slot() > maxSlot)
            {
                maxSlot = slot.slot();
            }
        }

        newInv.verifySize(slotsUsed, maxSlot);
//        newInv.dumpInv();

        return newInv;
    }

	private static NbtCompound checkForIDOverrides(NbtCompound in)
	{
		String id = in.getString(NbtKeys.ID, "");

		if (IDList.ID_OVERRIDES.containsKey(id))
		{
			id = IDList.ID_OVERRIDES.get(id);
			in.putString(NbtKeys.ID, id);
		}

		return in;
	}

    /**
     * This exists because an NBT List can have empty slots not accounted for in the middle of its current size;
     * Such as an empty slot in the middle of a Hopper Minecart.  This code fixes this problem.
     * @param slotsUsed ()
     */
    private void verifySize(List<Integer> slotsUsed, int maxSlot)
    {
        int size = Math.max(this.size(), maxSlot);

        size = getAdjustedSize(size);

        for (int i = 0; i < size; i++)
        {
            if (!slotsUsed.contains(i))
            {
                //LOGGER.info("verifySize(): [{}]: found unused slot Number; adding Empty slot...", i);
                this.items.add(new StackWithSlot(i, ItemStack.EMPTY));
            }
        }
    }

    /**
     * Common Function to try to get the "corrected" Inventory size based on
     * an existing `list.size()` for example.
     * @param size ()
     * @return ()
     */
    public static int getAdjustedSize(int size)
    {
        //LOGGER.debug("getAdjustedSize(): sizeIn: [{}]", size);

        if (size <= VILLAGER_SIZE)
        {
            return size;
        }
        else if (size <= DEFAULT_SIZE)
        {
            return DEFAULT_SIZE;
        }
        else if (size <= PLAYER_SIZE)
        {
            return PLAYER_SIZE;
        }
        else if (size <= DOUBLE_SIZE)
        {
            return DOUBLE_SIZE;
        }
        else
        {
            return Math.min(size, MAX_SIZE);
        }
    }

	@VisibleForTesting
    public void dumpInv()
    {
        AtomicInteger i = new AtomicInteger(0);
        LOGGER.info("dumpInv() --> START");

        this.items.forEach(
                (slot) ->
                {
					LOGGER.info("[{}]: slot [{}], stack: [{}]", i.get(), slot.slot(), slot.stack().toString());
                    i.getAndIncrement();
                }
        );

		LOGGER.info("dumpInv() --> END");
    }

    @Override
    public void close() throws Exception
    {
        this.items.clear();
    }
}
