package com.sakuraryoko.ap_scan.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.sakuraryoko.ap_scan.data.EntityData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.worldupdate.WorldUpgrader;
import net.minecraft.world.level.chunk.storage.SimpleRegionStorage;

@Mixin(WorldUpgrader.EntityUpgrader.class)
public class MixinWorldUpdater_Entities
{
	@Inject(method = "upgradeTag", at = @At("RETURN"))
	private void ap_scan$onEntityUpdate(SimpleRegionStorage storage, CompoundTag nbt,
										CallbackInfoReturnable<CompoundTag> cir)
	{
		EntityData.processEntityData(cir.getReturnValue(), nbt.getIntOr("DataVersion", -1));
	}
}
