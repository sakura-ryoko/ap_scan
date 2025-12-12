package com.sakuraryoko.ap_scan.mixin;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import net.minecraft.world.level.chunk.storage.RegionFileVersion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.sakuraryoko.ap_scan.data.DataManager;

@Mixin(RegionFileVersion.class)
public class MixinChunkCompressionFormat
{
	@Mutable @Shadow @Final private RegionFileVersion.StreamWrapper<OutputStream> outputWrapper;

	@Inject(method = "<init>", at = @At("TAIL"))
	private <O> void ap_scan$increaseDeflateLevel9(int id, String name, RegionFileVersion.StreamWrapper<O> inputStreamWrapper, RegionFileVersion.StreamWrapper<O> outputStreamWrapper, CallbackInfo ci)
	{
		if (DataManager.getInstance().shouldAdjustDeflateLevel() && name != null)
		{
			if (name.equalsIgnoreCase("deflate"))
			{
				this.outputWrapper = stream -> new BufferedOutputStream(new DeflaterOutputStream(stream, new Deflater(9)));
			}
		}
	}
}
