package com.sakuraryoko.ap_scan.compat.mixin;

import com.sakuraryoko.ap_scan.ApScan;
import me.fallenbreath.conditionalmixin.api.mixin.RestrictiveMixinConfigPlugin;

import java.util.List;
import java.util.Set;

public class ApScanMixinConfigPlugin extends RestrictiveMixinConfigPlugin
{
	@Override
	protected void onRestrictionCheckFailed(String mixinClassName, String reason)
	{
		ApScan.LOGGER.warn("Disabled mixin '{}' due to: '{}'", mixinClassName, reason);
	}

	@Override
	public String getRefMapperConfig()
	{
		return null;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets)
	{
	}

	@Override
	public List<String> getMixins()
	{
		return null;
	}
}
