package com.sakuraryoko.ap_scan.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

/**
 * Cloned from MaLiLib
 */
public class NbtEntityUtils
{
	/**
	 * Get an EntityType from NBT.
	 *
	 * @param nbt ()
	 * @return ()
	 */
	public static @Nullable EntityType<?> getEntityTypeFromNbt(@Nonnull CompoundTag nbt)
	{
		if (nbt.contains(NbtKeys.ID))
		{
			return BuiltInRegistries.ENTITY_TYPE.getOptional(Identifier.tryParse(nbt.getStringOr(NbtKeys.ID, ""))).orElse(null);
		}

		return null;
	}
}
