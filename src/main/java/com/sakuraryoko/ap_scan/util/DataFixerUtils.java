package com.sakuraryoko.ap_scan.util;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;

/**
 * A collection of common Data Fixer procedures
 */
public class DataFixerUtils
{
	public static final DataFixer DATA_FIXER = DataFixers.getDataFixer();
	public static final int CURRENT_SCHEMA = SharedConstants.getCurrentVersion().dataVersion().version();

	public static CompoundTag fixBlockName(CompoundTag in, int oldDataVersion)
	{
		try
		{
			return (CompoundTag) DATA_FIXER.update(References.BLOCK_NAME, new Dynamic<>(NbtOps.INSTANCE, in), oldDataVersion, CURRENT_SCHEMA).getValue();
		}
		catch (Exception ignored)
		{
			return in;
		}
	}

	public static CompoundTag fixBlockState(CompoundTag in, int oldDataVersion)
	{
		try
		{
			return (CompoundTag) DATA_FIXER.update(References.BLOCK_STATE, new Dynamic<>(NbtOps.INSTANCE, in), oldDataVersion, CURRENT_SCHEMA).getValue();
		}
		catch (Exception ignored)
		{
			return in;
		}
	}

	public static CompoundTag fixEntity(CompoundTag in, int oldDataVersion)
	{
		try
		{
			return (CompoundTag) DATA_FIXER.update(References.ENTITY, new Dynamic<>(NbtOps.INSTANCE, in), oldDataVersion, CURRENT_SCHEMA).getValue();
		}
		catch (Exception ignored)
		{
			return in;
		}
	}

	public static CompoundTag fixTileEntity(CompoundTag in, int oldDataVersion)
	{
		try
		{
			return (CompoundTag) DATA_FIXER.update(References.BLOCK_ENTITY, new Dynamic<>(NbtOps.INSTANCE, in), oldDataVersion, CURRENT_SCHEMA).getValue();
		}
		catch (Exception ignored)
		{
			return in;
		}
	}

	public static CompoundTag fixItemName(CompoundTag in, int oldDataVersion)
	{
		try
		{
			return (CompoundTag) DATA_FIXER.update(References.ITEM_NAME, new Dynamic<>(NbtOps.INSTANCE, in), oldDataVersion, CURRENT_SCHEMA).getValue();
		}
		catch (Exception ignored)
		{
			return in;
		}
	}

	public static CompoundTag fixItemStack(CompoundTag in, int oldDataVersion)
	{
		try
		{
			return (CompoundTag) DATA_FIXER.update(References.ITEM_STACK, new Dynamic<>(NbtOps.INSTANCE, in), oldDataVersion, CURRENT_SCHEMA).getValue();
		}
		catch (Exception ignored)
		{
			return in;
		}
	}

	public static CompoundTag fixPlayer(CompoundTag in, int oldDataVersion)
	{
		try
		{
			return (CompoundTag) DATA_FIXER.update(References.PLAYER, new Dynamic<>(NbtOps.INSTANCE, in), oldDataVersion, CURRENT_SCHEMA).getValue();
		}
		catch (Exception ignored)
		{
			return in;
		}
	}
}
