package thaumcraft.client.fx.particles;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import thaumcraft.common.config.ConfigBlocks;

@SideOnly(Side.CLIENT)
public class FXDrop extends Particle {
   int bobTimer;

   public FXDrop(World par1World, double par2, double par4, double par6, float r, float g, float b) {
      super(par1World, par2, par4, par6, 0.0F, 0.0F, 0.0F);
      this.motionX = this.motionY = this.motionZ = 0.0F;
      this.particleRed = r;
      this.particleGreen = g;
      this.particleBlue = b;
      this.setParticleTextureIndex(113);
      this.particleGravity = 0.06F;
      this.bobTimer = 40;
      this.particleMaxAge = (int)((double)64.0F / (Math.random() * 0.8 + 0.2));
   }

   public int getBrightnessForRender(float par1) {
      return 257;
   }

   public float getBrightness(float par1) {
      return 1.0F;
   }

   public void onUpdate() {
      this.prevPosX = this.posX;
      this.prevPosY = this.posY;
      this.prevPosZ = this.posZ;
      this.motionY -= this.particleGravity;
      if (this.bobTimer-- > 0) {
         this.motionX *= 0.02;
         this.motionY *= 0.02;
         this.motionZ *= 0.02;
         this.setParticleTextureIndex(113);
      } else {
         this.setParticleTextureIndex(112);
      }

      BlockPos pos = new BlockPos(MathHelper.floor(this.posX), MathHelper.floor(this.posY), MathHelper.floor(this.posZ));
      if (this.canCollide && this.world.getBlockState(pos).getBlock() == ConfigBlocks.blockJar) {
         // inside jar block — bounce off
         this.posX = this.prevPosX;
         this.posY = this.prevPosY;
         this.posZ = this.prevPosZ;
         this.motionY = 0.0F;
         this.onGround = true;
      } else {
         this.move(this.motionX, this.motionY, this.motionZ);
      }

      this.motionX *= 0.98F;
      this.motionY *= 0.98F;
      this.motionZ *= 0.98F;
      if (this.particleMaxAge-- <= 0) {
         this.setExpired();
      }
      if (this.onGround) {
         this.setParticleTextureIndex(114);
         this.motionX *= 0.7F;
         this.motionZ *= 0.7F;
      }

      // expire in liquid
      Material material = this.world.getBlockState(pos).getMaterial();
      if (material.isLiquid()) {
         this.setExpired();
      }
   }
}
