package com.sakuraryoko.ap_scan.mixin;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import net.minecraft.world.storage.ChunkCompressionFormat;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.sakuraryoko.ap_scan.data.DataManager;

@Mixin(ChunkCompressionFormat.class)
public class MixinChunkCompressionFormat
{
	@Mutable @Shadow @Final private ChunkCompressionFormat.Wrapper<OutputStream> outputStreamWrapper;

	@Inject(method = "<init>", at = @At("TAIL"))
	private <O> void increaseDeflateLevel9(int id, String name, ChunkCompressionFormat.Wrapper<O> inputStreamWrapper, ChunkCompressionFormat.Wrapper<O> outputStreamWrapper, CallbackInfo ci)
	{
		if (DataManager.getInstance().shouldAdjustDeflateLevel() && name != null)
		{
			if (name.equalsIgnoreCase("deflate"))
			{
				this.outputStreamWrapper = stream -> new BufferedOutputStream(new DeflaterOutputStream(stream, new Deflater(9)));
			}
		}
	}
}
