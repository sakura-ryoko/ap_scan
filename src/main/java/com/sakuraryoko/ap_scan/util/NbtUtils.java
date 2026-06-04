package com.sakuraryoko.ap_scan.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
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
	/**
	 * See {@link #readNbtFromFileAsPath}
	 */
	@Nullable
	public static CompoundTag readNbtFromFile(@Nonnull Path file)
	{
		return readNbtFromFile(file, NbtAccounter.unlimitedHeap());
	}

	/**
	 * @deprecated Please migrate to using 'readNbtFromFile' again
	 */
	@Deprecated(forRemoval = true)
	@Nullable
	public static CompoundTag readNbtFromFileAsPath(@Nonnull Path file)
	{
		return readNbtFromFileAsPath(file, NbtAccounter.unlimitedHeap());
	}

	@Nullable
	public static CompoundTag readNbtFromFile(@Nonnull Path file, NbtAccounter tracker)
	{
		if (!Files.exists(file) || !Files.isReadable(file))
		{
			return null;
		}

		InputStream is;

		try
		{
			is = Files.newInputStream(file, StandardOpenOption.READ);
		}
		catch (Exception e)
		{
			ApScan.LOGGER.warn("readNbtFromFile: Failed to read NBT data from file '{}' (failed to create the input stream)", file.toAbsolutePath());
			return null;
		}

		CompoundTag nbt = null;

		if (is != null)
		{
			try
			{
				nbt = NbtIo.read(new DataInputStream(new BufferedInputStream(new GZIPInputStream(is))), tracker);
			}
			catch (Exception e)
			{
				try
				{
					is.close();
					is = Files.newInputStream(file, StandardOpenOption.READ);
					nbt = NbtIo.read(new DataInputStream(new BufferedInputStream(is)), tracker);
				}
				catch (Exception ignore) {}
			}

			try
			{
				is.close();
			}
			catch (Exception ignore) {}
		}

		if (nbt == null || nbt.getId() == Constants.NBT.TAG_END)
		{
			ApScan.LOGGER.warn("readNbtFromFile: Failed to read NBT data from file '{}'", file.toAbsolutePath());
		}

		return nbt;
	}

	/**
	 * @deprecated Please migrate to using 'readNbtFromFile' again
	 */
	@Deprecated(forRemoval = true)
	@Nullable
	public static CompoundTag readNbtFromFileAsPath(@Nonnull Path file, NbtAccounter tracker)
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
	 * @deprecated Please migrate to using 'writeCompoundTagToCompressedFile' again
	 */
	@Deprecated(forRemoval = true)
	public static void writeCompressed(@Nonnull CompoundTag tag, @Nonnull OutputStream outputStream)
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

	/**
	 * @deprecated Please migrate to using 'writeCompoundTagToCompressedFile' again
	 */
	@Deprecated(forRemoval = true)
	public static void writeCompressed(@Nonnull CompoundTag tag, @Nonnull Path file)
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

	public static boolean writeCompoundTagToCompressedFile(@Nonnull CompoundTag tag, @Nonnull Path file)
	{
		return writeCompoundTagToCompressedFile(tag, file, "");
	}

	public static boolean writeCompoundTagToCompressedFile(@Nonnull CompoundTag tag, @Nonnull Path file, String tagName)
	{
		try (DataOutputStream os = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(Files.newOutputStream(file)))))
		{
//			NbtIo.write(tag, dos);
			return writeToNbtStream(tag, os, tagName);
		}
		catch (Exception e)
		{
			ApScan.LOGGER.warn("writeCompressedTest: Failed to write NBT data to file '{}'; {}", file.toAbsolutePath(), e.getLocalizedMessage());
		}

		return false;
	}

	public static boolean writeToNbtStream(@Nonnull Tag tag, @Nonnull DataOutput os)
	{
		return writeToNbtStream(tag, os, "");
	}

	public static boolean writeToNbtStream(@Nonnull Tag tag, @Nonnull DataOutput os, String tagName)
	{
		try
		{
			os.writeByte(tag.getId());

			if (tag.getId() != Constants.NBT.TAG_END)
			{
				os.writeUTF(tagName);
				tag.write(os);
			}

			return true;
		}
		catch (Exception e)
		{
			ApScan.LOGGER.warn("writeToNbtStream: Exception while writing NBT data; {}", e.getLocalizedMessage());
		}

		return false;
	}

	/**
	 * Reads in a Flat Map from NBT -- this way we don't need Mojang's code complexity
	 * @param <T> ()
	 * @param nbt ()
	 * @param mapCodec ()
	 * @return ()
	 */
	public static <T> Optional<T> readFlatMap(@Nonnull CompoundTag nbt, MapCodec<T> mapCodec)
	{
		DynamicOps<Tag> ops = NbtOps.INSTANCE;

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
	public static <T> CompoundTag writeFlatMap(MapCodec<T> mapCodec, T value)
	{
		DynamicOps<Tag> ops = NbtOps.INSTANCE;
		CompoundTag nbt = new CompoundTag();

		switch (mapCodec.encoder().encodeStart(ops, value))
		{
			case DataResult.Success<Tag> result -> nbt.merge((CompoundTag) result.value());
			case DataResult.Error<Tag> error -> error.partialValue().ifPresent(partial -> nbt.merge((CompoundTag) partial));
		}

		return nbt;
	}
}
