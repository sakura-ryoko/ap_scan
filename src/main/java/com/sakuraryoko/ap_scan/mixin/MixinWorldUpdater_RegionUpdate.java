package com.sakuraryoko.ap_scan.mixin;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.storage.VersionedChunkStorage;
import net.minecraft.world.updater.WorldUpdater;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.sakuraryoko.ap_scan.data.ChunkData;

@Mixin(WorldUpdater.RegionUpdate.class)
public class MixinWorldUpdater_RegionUpdate
{
	@Redirect(method = "update(Lnet/minecraft/world/storage/VersionedChunkStorage;Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/registry/RegistryKey;)Z",
			  at = @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/VersionedChunkStorage;getDataVersion(Lnet/minecraft/nbt/NbtCompound;)I"))
	private int onChunkUpdate(NbtCompound nbt)
	{
		ChunkData.processChunkData(nbt);
		return VersionedChunkStorage.getDataVersion(nbt);
	}
}
