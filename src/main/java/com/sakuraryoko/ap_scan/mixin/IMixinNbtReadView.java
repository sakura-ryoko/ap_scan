package com.sakuraryoko.ap_scan.mixin;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.ReadContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Copied from MaLiLib
 */
@Mixin(NbtReadView.class)
public interface IMixinNbtReadView
{
    @Accessor("context")
    ReadContext ap_scan$getContext();

    @Accessor("nbt")
    NbtCompound ap_scan$getNbt();
}
