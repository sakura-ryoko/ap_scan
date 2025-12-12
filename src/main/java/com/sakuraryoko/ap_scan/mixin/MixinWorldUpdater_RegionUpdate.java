package com.sakuraryoko.ap_scan.mixin;

import com.llamalad7.mixinextras.sugar.Local;

import com.sakuraryoko.ap_scan.data.DataManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.worldupdate.WorldUpgrader;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.storage.SimpleRegionStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.sakuraryoko.ap_scan.data.ChunkData;

@Mixin(WorldUpgrader.ChunkUpgrader.class)
public class MixinWorldUpdater_RegionUpdate
{
	@Inject(method = "tryProcessOnePosition(Lnet/minecraft/world/level/chunk/storage/SimpleRegionStorage;Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/resources/ResourceKey;)Z",
			at = @At(value = "INVOKE",
					 target = "Lnet/minecraft/world/level/chunk/storage/SimpleRegionStorage;upgradeChunkTag(Lnet/minecraft/nbt/CompoundTag;ILnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/nbt/CompoundTag;"))
	private void ap_scan$onChunkUpdate(SimpleRegionStorage versionedChunkStorage,
									   ChunkPos chunkPos,
									   ResourceKey<Level> registryKey,
									   CallbackInfoReturnable<Boolean> cir,
									   @Local CompoundTag nbtCompound,
									   @Local CompoundTag nbtCompound2)
	{
		ChunkData.processChunkData(nbtCompound2, nbtCompound.getIntOr("DataVersion", -1));
	}

    @ModifyConstant(method = "tryProcessOnePosition(Lnet/minecraft/world/level/chunk/storage/SimpleRegionStorage;Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/resources/ResourceKey;)Z",
                    constant = { @Constant(stringValue = "sections")})
    private String ap_scan$modifyLightmapPurge(String constant)
    {
        if (DataManager.getInstance().shouldDisableLightmapPrune())
        {
            return "notSections";
        }

        return constant;
    }
}
