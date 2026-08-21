package com.sakuraryoko.ap_scan.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.sakuraryoko.ap_scan.data.ChunkData;
import com.sakuraryoko.ap_scan.data.DataManager;
import com.sakuraryoko.ap_scan.data.EntityData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.worldupdate.RegionStorageUpgrader;
import net.minecraft.world.level.chunk.storage.SimpleRegionStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RegionStorageUpgrader.class)
public abstract class MixinRegionStorageUpgrader
{
	@Shadow @Final protected DataFixTypes dataFixType;

	@WrapOperation(method = "upgradeTag",
	               at = @At(value = "INVOKE",
				            target = "Lnet/minecraft/world/level/chunk/storage/SimpleRegionStorage;upgradeChunkTag(Lnet/minecraft/nbt/CompoundTag;ILnet/minecraft/nbt/CompoundTag;I)Lnet/minecraft/nbt/CompoundTag;")
	)
	private CompoundTag ap_scan$onUpgradeChunkTag(SimpleRegionStorage instance, CompoundTag chunkTag, int defaultVersion,
	                                              CompoundTag dataFixContextTag, int targetVersion,
	                                              Operation<CompoundTag> original)
	{
		CompoundTag fixTag = original.call(instance, chunkTag, defaultVersion, dataFixContextTag, targetVersion);

		if (DataManager.getInstance().shouldRunTasks())
		{
			if (this.dataFixType.equals(DataFixTypes.ENTITY_CHUNK))
			{
				EntityData.processEntityData(fixTag, fixTag.getIntOr("DataVersion", -1));
			}
			else if (this.dataFixType.equals(DataFixTypes.CHUNK))
			{
				ChunkData.processChunkData(fixTag, fixTag.getIntOr("DataVersion", -1));
			}
		}

		return fixTag;
	}
}
