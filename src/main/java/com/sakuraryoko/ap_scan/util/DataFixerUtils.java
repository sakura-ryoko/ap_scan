package com.sakuraryoko.ap_scan.util;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import net.minecraft.SharedConstants;
import net.minecraft.datafixer.Schemas;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;

public class DataFixerUtils
{
	public static final DataFixer DATA_FIXER = Schemas.getFixer();
	public static final int CURRENT_SCHEMA = SharedConstants.getGameVersion().dataVersion().id();

	public static NbtCompound fixBlockName(NbtCompound in, int oldDataVersion)
	{
		try
		{
			return (NbtCompound) DATA_FIXER.update(TypeReferences.BLOCK_NAME, new Dynamic<>(NbtOps.INSTANCE, in), oldDataVersion, CURRENT_SCHEMA).getValue();
		}
		catch (Exception ignored)
		{
			return in;
		}
	}

	public static NbtCompound fixBlockState(NbtCompound in, int oldDataVersion)
	{
		try
		{
			return (NbtCompound) DATA_FIXER.update(TypeReferences.BLOCK_STATE, new Dynamic<>(NbtOps.INSTANCE, in), oldDataVersion, CURRENT_SCHEMA).getValue();
		}
		catch (Exception ignored)
		{
			return in;
		}
	}

	public static NbtCompound fixEntity(NbtCompound in, int oldDataVersion)
	{
		try
		{
			return (NbtCompound) DATA_FIXER.update(TypeReferences.ENTITY, new Dynamic<>(NbtOps.INSTANCE, in), oldDataVersion, CURRENT_SCHEMA).getValue();
		}
		catch (Exception ignored)
		{
			return in;
		}
	}

	public static NbtCompound fixTileEntity(NbtCompound in, int oldDataVersion)
	{
		try
		{
			return (NbtCompound) DATA_FIXER.update(TypeReferences.BLOCK_ENTITY, new Dynamic<>(NbtOps.INSTANCE, in), oldDataVersion, CURRENT_SCHEMA).getValue();
		}
		catch (Exception ignored)
		{
			return in;
		}
	}

	public static NbtCompound fixItemName(NbtCompound in, int oldDataVersion)
	{
		try
		{
			return (NbtCompound) DATA_FIXER.update(TypeReferences.ITEM_NAME, new Dynamic<>(NbtOps.INSTANCE, in), oldDataVersion, CURRENT_SCHEMA).getValue();
		}
		catch (Exception ignored)
		{
			return in;
		}
	}

	public static NbtCompound fixItemStack(NbtCompound in, int oldDataVersion)
	{
		try
		{
			return (NbtCompound) DATA_FIXER.update(TypeReferences.ITEM_STACK, new Dynamic<>(NbtOps.INSTANCE, in), oldDataVersion, CURRENT_SCHEMA).getValue();
		}
		catch (Exception ignored)
		{
			return in;
		}
	}

	public static NbtCompound fixPlayer(NbtCompound in, int oldDataVersion)
	{
		try
		{
			return (NbtCompound) DATA_FIXER.update(TypeReferences.PLAYER, new Dynamic<>(NbtOps.INSTANCE, in), oldDataVersion, CURRENT_SCHEMA).getValue();
		}
		catch (Exception ignored)
		{
			return in;
		}
	}
}
