package com.sakuraryoko.ap_scan.util;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/**
 * Cloned from MaLiLib
 */
public class NbtBlockUtils
{
	/**
	 * Get the Block Entity Type from the NBT Tag.
	 *
	 * @param nbt ()
	 * @return ()
	 */
	public static @Nullable BlockEntityType<?> getBlockEntityTypeFromNbt(@Nonnull NbtCompound nbt)
	{
		if (nbt.contains(NbtKeys.ID))
		{
			return Registries.BLOCK_ENTITY_TYPE.getOptionalValue(Identifier.tryParse(nbt.getString(NbtKeys.ID, ""))).orElse(null);
		}

		return null;
	}

	public static @Nullable Text getCustomNameFromNbt(@Nonnull NbtCompound nbt, @Nonnull DynamicRegistryManager registry, String key)
	{
		NbtView view = NbtView.getReader(nbt, registry);
		return BlockEntity.tryParseCustomName(Objects.requireNonNull(view.getReader()), key);
	}

	@Nullable
	public static BlockPos readBlockPos(@Nullable NbtCompound tag)
	{
		if (tag != null &&
			tag.contains("x") &&
			tag.contains("y") &&
			tag.contains("z"))
		{
			return new BlockPos(tag.getInt("x", 0), tag.getInt("y", 0), tag.getInt("z", 0));
		}

		return null;
	}
}
