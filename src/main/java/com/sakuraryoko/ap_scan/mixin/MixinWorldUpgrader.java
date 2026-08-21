package com.sakuraryoko.ap_scan.mixin;

import com.sakuraryoko.ap_scan.data.DataManager;
import net.minecraft.util.worldupdate.WorldUpgrader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(WorldUpgrader.class)
public abstract class MixinWorldUpgrader
{
	@ModifyConstant(method = "verifyChunkPosAndEraseCache", constant = { @Constant(stringValue = "sections")})
	private static String ap_scan$modifyLightmapPurge(String constant)
	{
		if (DataManager.getInstance().shouldRunTasks() &&
			DataManager.getInstance().shouldDisableLightmapPrune())
		{
			return "notSections";
		}

		return constant;
	}
}
