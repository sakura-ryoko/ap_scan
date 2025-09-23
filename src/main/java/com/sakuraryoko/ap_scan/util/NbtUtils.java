package com.sakuraryoko.ap_scan.util;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import net.minecraft.nbt.*;

import com.sakuraryoko.ap_scan.ApScan;

/**
 * Cloned from MaLiLib
 */
public class NbtUtils
{
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
