package thaumcraft.client.fx.particles;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.particle.Particle;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

@SideOnly(Side.CLIENT)
public class FXBreaking extends Particle {
   public void setParticleMaxAge(int particleMaxAge) {
      this.particleMaxAge = particleMaxAge;
   }

   public void setMotion(double mx, double my, double mz) {
      this.motionX = mx;
      this.motionY = my;
      this.motionZ = mz;
   }

   public FXBreaking(World par1World, double par2, double par4, double par6, Item par8Item) {
      super(par1World, par2, par4, par6, 0.0F, 0.0F, 0.0F);
      this.particleRed = this.particleGreen = this.particleBlue = 1.0F;
      this.particleGravity = 0.2F;
      this.particleScale /= 2.0F;
   }

   public FXBreaking(World par1World, double par2, double par4, double par6, double par8, double par10, double par12, Item par14Item) {
      this(par1World, par2, par4, par6, par14Item);
      this.motionX *= 0.1F;
      this.motionY *= 0.1F;
      this.motionZ *= 0.1F;
      this.motionX += par8;
      this.motionY += par10;
      this.motionZ += par12;
   }

   public int getFXLayer() {
      return 2;
   }

   public void renderParticle(BufferBuilder buffer, Entity entityIn, float par2, float par3, float par4, float par5, float par6, float par7) {
      float f6 = this.particleTexture != null ? this.particleTexture.getMinU() : 0.0F;
      float f7 = this.particleTexture != null ? this.particleTexture.getMaxU() : 1.0F;
      float f8 = this.particleTexture != null ? this.particleTexture.getMinV() : 0.0F;
      float f9 = this.particleTexture != null ? this.particleTexture.getMaxV() : 1.0F;
      float f10 = 0.1F * this.particleScale;
      float fade = 1.0F - (float)this.particleAge / (float)this.particleMaxAge;
      f10 *= fade;
      float f11 = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)par2 - interpPosX);
      float f12 = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)par2 - interpPosY);
      float f13 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)par2 - interpPosZ);
      float r = this.particleRed, g = this.particleGreen, b = this.particleBlue;
      buffer.pos(f11 - par3 * f10 - par6 * f10, f12 - par4 * f10, f13 - par5 * f10 - par7 * f10).tex(f6, f9).color(r, g, b, 1.0f).endVertex();
      buffer.pos(f11 - par3 * f10 + par6 * f10, f12 + par4 * f10, f13 - par5 * f10 + par7 * f10).tex(f6, f8).color(r, g, b, 1.0f).endVertex();
      buffer.pos(f11 + par3 * f10 + par6 * f10, f12 + par4 * f10, f13 + par5 * f10 + par7 * f10).tex(f7, f8).color(r, g, b, 1.0f).endVertex();
      buffer.pos(f11 + par3 * f10 - par6 * f10, f12 - par4 * f10, f13 + par5 * f10 - par7 * f10).tex(f7, f9).color(r, g, b, 1.0f).endVertex();
   }
}
