package thaumcraft.common.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.init.SoundEvents;

/**
 * Custom step sound for jars using vanilla glass sounds.
 */
public class JarStepSound extends SoundType {
    public String soundName;

    public JarStepSound(String par1Str, float volume, float pitch) {
        super(volume, pitch,
            SoundEvents.BLOCK_GLASS_BREAK,
            SoundEvents.BLOCK_GLASS_STEP,
            SoundEvents.BLOCK_GLASS_PLACE,
            SoundEvents.BLOCK_GLASS_HIT,
            SoundEvents.BLOCK_GLASS_FALL);
        this.soundName = par1Str;
    }
}
