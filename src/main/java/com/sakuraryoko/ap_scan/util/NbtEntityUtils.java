package com.sakuraryoko.ap_scan.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

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
	public static @Nullable EntityType<?> getEntityTypeFromNbt(@Nonnull NbtCompound nbt)
	{
		if (nbt.contains(NbtKeys.ID))
		{
			return Registries.ENTITY_TYPE.getOptionalValue(Identifier.tryParse(nbt.getString(NbtKeys.ID, ""))).orElse(null);
		}

		return null;
	}
}
