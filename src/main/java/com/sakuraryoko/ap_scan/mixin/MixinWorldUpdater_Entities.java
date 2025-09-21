package com.sakuraryoko.ap_scan.mixin;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.storage.ChunkPosKeyedStorage;
import net.minecraft.world.updater.WorldUpdater;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.sakuraryoko.ap_scan.data.EntityData;

@Mixin(WorldUpdater.EntitiesUpdate.class)
public class MixinWorldUpdater_Entities
{
	@Inject(method = "updateNbt", at = @At("RETURN"))
	private void onEntityUpdate(ChunkPosKeyedStorage storage, NbtCompound nbt, CallbackInfoReturnable<NbtCompound> cir)
	{
		EntityData.processEntityData(cir.getReturnValue());
	}
}
