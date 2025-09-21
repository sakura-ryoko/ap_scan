package com.sakuraryoko.ap_scan.util;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import net.minecraft.nbt.*;
import net.minecraft.util.Uuids;

import com.sakuraryoko.ap_scan.ApScan;

/**
 * Cloned from MaLiLib
 */
public class NbtUtils
{
	@Nullable
	public static UUID readUUID(@Nonnull NbtCompound tag)
	{
		return readUUID(tag, "UUIDM", "UUIDL");
	}

	@Nullable
	public static UUID readUUID(@Nonnull NbtCompound tag, String keyM, String keyL)
	{
		if (tag.contains(keyM) && tag.contains(keyL))
		{
			return new UUID(tag.getLong(keyM, 0L), tag.getLong(keyL, 0L));
		}

		return null;
	}

	public static void writeUUID(@Nonnull NbtCompound tag, UUID uuid)
	{
		writeUUID(tag, uuid, "UUIDM", "UUIDL");
	}

	public static void writeUUID(@Nonnull NbtCompound tag, UUID uuid, String keyM, String keyL)
	{
		tag.putLong(keyM, uuid.getMostSignificantBits());
		tag.putLong(keyL, uuid.getLeastSignificantBits());
	}

	public static NbtCompound getOrCreateCompound(@Nonnull NbtCompound tagIn, String tagName)
	{
		NbtCompound nbt;

		if (tagIn.contains(tagName))
		{
			nbt = tagIn.getCompoundOrEmpty(tagName);
		}
		else
		{
			nbt = new NbtCompound();
			tagIn.put(tagName, nbt);
		}

		return nbt;
	}

	public static <T> NbtList asListTag(Collection<T> values, Function<T, NbtElement> tagFactory)
	{
		NbtList list = new NbtList();

		for (T val : values)
		{
			list.add(tagFactory.apply(val));
		}

		return list;
	}

	/**
	 * Get the Entity's UUID from NBT.
	 *
	 * @param nbt ()
	 * @return ()
	 */
	public static @Nullable UUID getUUIDCodec(@Nonnull NbtCompound nbt)
	{
		return getUUIDCodec(nbt, NbtKeys.UUID);
	}

	/**
	 * Get the Entity's UUID from NBT.
	 *
	 * @param nbt ()
	 * @param key ()
	 * @return ()
	 */
	public static @Nullable UUID getUUIDCodec(@Nonnull NbtCompound nbt, String key)
	{
		if (nbt.contains(key))
		{
			return nbt.get(key, Uuids.INT_STREAM_CODEC).orElse(null);
		}

		return null;
	}

	/**
	 * Get the Entity's UUID from NBT.
	 *
	 * @param nbtIn ()
	 * @param key ()
	 * @param uuid ()
	 * @return ()
	 */
	public static NbtCompound putUUIDCodec(@Nonnull NbtCompound nbtIn, @Nonnull UUID uuid, String key)
	{
		nbtIn.put(key, Uuids.INT_STREAM_CODEC, uuid);
		return nbtIn;
	}

	@Nullable
	public static NbtCompound readNbtFromFileAsPath(@Nonnull Path file)
	{
		return readNbtFromFileAsPath(file, NbtSizeTracker.ofUnlimitedBytes());
	}

	@Nullable
	public static NbtCompound readNbtFromFileAsPath(@Nonnull Path file, NbtSizeTracker tracker)
	{
		if (!Files.exists(file) || !Files.isReadable(file))
		{
			return null;
		}

		try
		{
			return NbtIo.readCompressed(Files.newInputStream(file), tracker);
		}
		catch (Exception e)
		{
			ApScan.LOGGER.warn("readNbtFromFileAsPath: Failed to read NBT data from file '{}'", file.toString());
		}

		return null;
	}

	/**
	 * Write the compound tag, gzipped, to the output stream.
	 */
	public static void writeCompressed(@Nonnull NbtCompound tag, @Nonnull OutputStream outputStream)
    {
		try
		{
			NbtIo.writeCompressed(tag, outputStream);
		}
		catch (Exception err)
		{
			ApScan.LOGGER.warn("writeCompressed: Failed to write NBT data to output stream");
		}
	}

	public static void writeCompressed(@Nonnull NbtCompound tag, @Nonnull Path file)
	{
		try
		{
			NbtIo.writeCompressed(tag, file);
		}
		catch (Exception err)
		{
			ApScan.LOGGER.warn("writeCompressed: Failed to write NBT data to file");
		}
	}

	/**
	 * Reads in a Flat Map from NBT -- this way we don't need Mojang's code complexity
	 * @param <T> ()
	 * @param nbt ()
	 * @param mapCodec ()
	 * @return ()
	 */
	public static <T> Optional<T> readFlatMap(@Nonnull NbtCompound nbt, MapCodec<T> mapCodec)
	{
		DynamicOps<NbtElement> ops = NbtOps.INSTANCE;

		return switch (ops.getMap(nbt).flatMap(map -> mapCodec.decode(ops, map)))
		{
			case DataResult.Success<T> result -> Optional.of(result.value());
			case DataResult.Error<T> error -> error.partialValue();
			default -> Optional.empty();
        };
	}

	/**
	 * Writes a Flat Map to NBT -- this way we don't need Mojang's code complexity
	 * @param <T> ()
	 * @param mapCodec ()
	 * @param value ()
	 * @return ()
	 */
	public static <T> NbtCompound writeFlatMap(MapCodec<T> mapCodec, T value)
	{
		DynamicOps<NbtElement> ops = NbtOps.INSTANCE;
		NbtCompound nbt = new NbtCompound();

		switch (mapCodec.encoder().encodeStart(ops, value))
		{
			case DataResult.Success<NbtElement> result -> nbt.copyFrom((NbtCompound) result.value());
			case DataResult.Error<NbtElement> error -> error.partialValue().ifPresent(partial -> nbt.copyFrom((NbtCompound) partial));
		}

		return nbt;
	}
}
