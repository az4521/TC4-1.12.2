package thaumcraft.client.fx;

import net.minecraft.client.particle.Particle;
import net.minecraft.world.World;

/**
 * EntitySpellParticleFX was renamed to ParticleSpell in MC 1.8.
 * The 1.12.2 ParticleSpell has a package-private constructor; this subclass
 * of Particle is used as a colour-tinted substitute until rendering is migrated.
 */
public class ParticleSpellCustom extends Particle {
    public ParticleSpellCustom(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn,
                               double xSpeedIn, double ySpeedIn, double zSpeedIn) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
    }
}
