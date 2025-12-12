package com.sakuraryoko.ap_scan.mixin;

import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.TagValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Copied from MaLiLib
 */
@Mixin(TagValueOutput.class)
public interface IMixinNbtWriteView
{
    @Accessor("ops")
    DynamicOps<?> ap_scan$getOps();

    @Accessor("output")
    CompoundTag ap_scan$getNbt();
}
