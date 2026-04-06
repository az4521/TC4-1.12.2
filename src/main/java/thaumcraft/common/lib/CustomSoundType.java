package thaumcraft.common.lib;

import net.minecraft.block.SoundType;
import net.minecraft.init.SoundEvents;

/**
 * Custom sound type for taint/gore blocks using vanilla slime sounds.
 */
public class CustomSoundType extends SoundType {
    public String soundName;

    public CustomSoundType(String par1Str, float volume, float pitch) {
        super(volume, pitch,
            SoundEvents.BLOCK_SLIME_BREAK,
            SoundEvents.BLOCK_SLIME_STEP,
            SoundEvents.BLOCK_SLIME_PLACE,
            SoundEvents.BLOCK_SLIME_HIT,
            SoundEvents.BLOCK_SLIME_FALL);
        this.soundName = par1Str;
    }
}
