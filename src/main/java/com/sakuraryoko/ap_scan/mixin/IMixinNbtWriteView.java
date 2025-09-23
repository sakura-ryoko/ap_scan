package com.sakuraryoko.ap_scan.mixin;

import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.storage.NbtWriteView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Copied from MaLiLib
 */
@Mixin(NbtWriteView.class)
public interface IMixinNbtWriteView
{
    @Accessor("ops")
    DynamicOps<?> ap_scan$getOps();

    @Accessor("nbt")
    NbtCompound ap_scan$getNbt();
}
