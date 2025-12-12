package com.sakuraryoko.ap_scan.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInputContextHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Copied from MaLiLib
 */
@Mixin(TagValueInput.class)
public interface IMixinNbtReadView
{
    @Accessor("context")
    ValueInputContextHelper ap_scan$getContext();

    @Accessor("input")
    CompoundTag ap_scan$getNbt();
}
