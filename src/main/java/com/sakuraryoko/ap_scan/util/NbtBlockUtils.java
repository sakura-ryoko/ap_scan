package com.sakuraryoko.ap_scan.util;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

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
	public static @Nullable BlockEntityType<?> getBlockEntityTypeFromNbt(@Nonnull CompoundTag nbt)
	{
		if (nbt.contains(NbtKeys.ID))
		{
			return BuiltInRegistries.BLOCK_ENTITY_TYPE.getOptional(Identifier.tryParse(nbt.getStringOr(NbtKeys.ID, ""))).orElse(null);
		}

		return null;
	}

	public static @Nullable Component getCustomNameFromNbt(@Nonnull CompoundTag nbt, @Nonnull RegistryAccess registry, String key)
	{
		NbtView view = NbtView.getReader(nbt, registry);
		return BlockEntity.parseCustomNameSafe(Objects.requireNonNull(view.getReader()), key);
	}

	@Nullable
	public static BlockPos readBlockPos(@Nullable CompoundTag tag)
	{
		if (tag != null &&
			tag.contains("x") &&
			tag.contains("y") &&
			tag.contains("z"))
		{
			return new BlockPos(tag.getIntOr("x", 0), tag.getIntOr("y", 0), tag.getIntOr("z", 0));
		}

		return null;
	}
}
