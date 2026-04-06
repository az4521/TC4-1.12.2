package thaumcraft.common.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.init.SoundEvents;

/**
 * Custom step sound using vanilla stone sounds as base.
 * The original TC4 had custom sounds per-block; this uses vanilla equivalents.
 */
public class CustomStepSound extends SoundType {
    public String soundName;

    public CustomStepSound(String par1Str, float volume, float pitch) {
        super(volume, pitch,
            SoundEvents.BLOCK_STONE_BREAK,
            SoundEvents.BLOCK_STONE_STEP,
            SoundEvents.BLOCK_STONE_PLACE,
            SoundEvents.BLOCK_STONE_HIT,
            SoundEvents.BLOCK_STONE_FALL);
        this.soundName = par1Str;
    }
}
