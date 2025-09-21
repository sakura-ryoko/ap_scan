package com.sakuraryoko.ap_scan.mixin;

import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.VersionedChunkStorage;
import net.minecraft.world.updater.WorldUpdater;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.sakuraryoko.ap_scan.data.ChunkData;

@Mixin(WorldUpdater.RegionUpdate.class)
public class MixinWorldUpdater_RegionUpdate
{
	@Inject(method = "update(Lnet/minecraft/world/storage/VersionedChunkStorage;Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/registry/RegistryKey;)Z",
			at = @At(value = "INVOKE",
					 target = "Lnet/minecraft/world/storage/VersionedChunkStorage;updateChunkNbt(Lnet/minecraft/registry/RegistryKey;Ljava/util/function/Supplier;Lnet/minecraft/nbt/NbtCompound;Ljava/util/Optional;)Lnet/minecraft/nbt/NbtCompound;"))
	private void onChunkUpdate(VersionedChunkStorage versionedChunkStorage, ChunkPos chunkPos, RegistryKey<World> registryKey, CallbackInfoReturnable<Boolean> cir,
							   @Local NbtCompound nbtCompound2)
	{
		ChunkData.processChunkData(nbtCompound2);
	}
}
