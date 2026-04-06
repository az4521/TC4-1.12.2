package thaumcraft.client.fx.particles;

import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

@SideOnly(Side.CLIENT)
public class FXBoreParticles extends Particle {
   private Block blockInstance;
   private Item itemInstance;
   private int metadata;
   private int side;
   private double targetX;
   private double targetY;
   private double targetZ;

   public FXBoreParticles(World par1World, double par2, double par4, double par6, double tx, double ty, double tz, Block par14Block, int par15, int par16) {
      super(par1World, par2, par4, par6, 0.0F, 0.0F, 0.0F);
      this.blockInstance = par14Block;
      try {
         IBlockState state = par14Block.getStateFromMeta(par16);
         this.setParticleTexture(Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getTexture(state));
      } catch (Exception ignored) {}
      this.particleGravity = 0.2F;
      this.particleRed = this.particleGreen = this.particleBlue = 0.6F;
      this.particleScale = this.rand.nextFloat() * 0.3F + 0.4F;
      this.side = par15;
      this.targetX = tx;
      this.targetY = ty;
      this.targetZ = tz;
      double dx = tx - this.posX;
      double dy = ty - this.posY;
      double dz = tz - this.posZ;
      int base = (int)(MathHelper.sqrt(dx * dx + dy * dy + dz * dz) * 3.0F);
      if (base < 1) {
         base = 1;
      }
      this.particleMaxAge = base / 2 + this.rand.nextInt(base);
      float f3 = 0.01F;
      this.motionX = (float)this.world.rand.nextGaussian() * f3;
      this.motionY = (float)this.world.rand.nextGaussian() * f3;
      this.motionZ = (float)this.world.rand.nextGaussian() * f3;
      this.canCollide = true;
      EntityLivingBase renderentity = (EntityLivingBase)FMLClientHandler.instance().getClient().getRenderViewEntity();
      int visibleDistance = 64;
      if (!FMLClientHandler.instance().getClient().gameSettings.fancyGraphics) {
         visibleDistance = 32;
      }
      if (renderentity != null && renderentity.getDistance(this.posX, this.posY, this.posZ) > (double)visibleDistance) {
         this.particleMaxAge = 0;
      }
   }

   public FXBoreParticles(World par1World, double par2, double par4, double par6, double tx, double ty, double tz, Item item, int par15, int par16) {
      super(par1World, par2, par4, par6, 0.0F, 0.0F, 0.0F);
      this.itemInstance = item;
      this.metadata = par16;
      this.particleGravity = 0.2F;
      this.particleRed = this.particleGreen = this.particleBlue = 0.6F;
      this.particleScale = this.rand.nextFloat() * 0.3F + 0.4F;
      this.side = par15;
      this.targetX = tx;
      this.targetY = ty;
      this.targetZ = tz;
      double dx = tx - this.posX;
      double dy = ty - this.posY;
      double dz = tz - this.posZ;
      int base = (int)(MathHelper.sqrt(dx * dx + dy * dy + dz * dz) * 3.0F);
      if (base < 1) {
         base = 1;
      }
      this.particleMaxAge = base / 2 + this.rand.nextInt(base);
      float f3 = 0.01F;
      this.motionX = (float)this.world.rand.nextGaussian() * f3;
      this.motionY = (float)this.world.rand.nextGaussian() * f3;
      this.motionZ = (float)this.world.rand.nextGaussian() * f3;
      this.canCollide = true;
      EntityLivingBase renderentity = (EntityLivingBase)FMLClientHandler.instance().getClient().getRenderViewEntity();
      int visibleDistance = 64;
      if (!FMLClientHandler.instance().getClient().gameSettings.fancyGraphics) {
         visibleDistance = 32;
      }
      if (renderentity != null && renderentity.getDistance(this.posX, this.posY, this.posZ) > (double)visibleDistance) {
         this.particleMaxAge = 0;
      }
   }

   public void onUpdate() {
      this.prevPosX = this.posX;
      this.prevPosY = this.posY;
      this.prevPosZ = this.posZ;
      if (this.particleAge++ < this.particleMaxAge && (MathHelper.floor(this.posX) != MathHelper.floor(this.targetX) || MathHelper.floor(this.posY) != MathHelper.floor(this.targetY) || MathHelper.floor(this.posZ) != MathHelper.floor(this.targetZ))) {
         if (this.canCollide) {
            this.pushOutOfBlocks(this.posX, this.posY, this.posZ);
         }
         this.move(this.motionX, this.motionY, this.motionZ);
         this.motionX *= 0.985;
         this.motionY *= 0.985;
         this.motionZ *= 0.985;
         double dx = this.targetX - this.posX;
         double dy = this.targetY - this.posY;
         double dz = this.targetZ - this.posZ;
         double d13 = 0.3;
         double d11 = MathHelper.sqrt(dx * dx + dy * dy + dz * dz);
         if (d11 < (double)4.0F) {
            this.particleScale *= 0.9F;
            d13 = 0.6;
         }
         dx /= d11;
         dy /= d11;
         dz /= d11;
         this.motionX += dx * d13;
         this.motionY += dy * d13;
         this.motionZ += dz * d13;
         this.motionX = MathHelper.clamp((float)this.motionX, -0.35F, 0.35F);
         this.motionY = MathHelper.clamp((float)this.motionY, -0.35F, 0.35F);
         this.motionZ = MathHelper.clamp((float)this.motionZ, -0.35F, 0.35F);
      } else {
         this.setExpired();
      }
   }

   public FXBoreParticles applyColourMultiplier(int par1, int par2, int par3) {
      if (this.blockInstance != null) {
         try {
            BlockPos pos = new BlockPos(par1, par2, par3);
            int var4 = Minecraft.getMinecraft().getBlockColors().colorMultiplier(this.world.getBlockState(pos), this.world, pos, 0);
            this.particleRed *= (float)(var4 >> 16 & 255) / 255.0F;
            this.particleGreen *= (float)(var4 >> 8 & 255) / 255.0F;
            this.particleBlue *= (float)(var4 & 255) / 255.0F;
         } catch (Exception ignored) {}
      } else if (this.itemInstance != null) {
         try {
            int var4 = Minecraft.getMinecraft().getItemColors().colorMultiplier(new ItemStack(this.itemInstance, 1, this.metadata), 0);
            this.particleRed *= (float)(var4 >> 16 & 255) / 255.0F;
            this.particleGreen *= (float)(var4 >> 8 & 255) / 255.0F;
            this.particleBlue *= (float)(var4 & 255) / 255.0F;
         } catch (Exception ignored) {}
      }
      return this;
   }

   public FXBoreParticles applyRenderColor(int par1) {
      return applyColourMultiplier(0, 0, 0);
   }

   public void setMotion(double mx, double my, double mz) {
      this.motionX = mx;
      this.motionY = my;
      this.motionZ = mz;
   }

   public int getFXLayer() {
      return this.blockInstance != null ? 1 : 2;
   }

   public void renderParticle(BufferBuilder buffer, Entity entityIn, float par2, float par3, float par4, float par5, float par6, float par7) {
      float f6 = this.particleTexture != null ? this.particleTexture.getMinU() : 0.0F;
      float f7 = this.particleTexture != null ? this.particleTexture.getMaxU() : 1.0F;
      float f8 = this.particleTexture != null ? this.particleTexture.getMinV() : 0.0F;
      float f9 = this.particleTexture != null ? this.particleTexture.getMaxV() : 1.0F;
      float f10 = 0.1F * this.particleScale;
      float f11 = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)par2 - interpPosX);
      float f12 = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)par2 - interpPosY);
      float f13 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)par2 - interpPosZ);
      float red = this.particleRed;
      float green = this.particleGreen;
      float blue = this.particleBlue;
      buffer.pos(f11 - par3 * f10 - par6 * f10, f12 - par4 * f10, f13 - par5 * f10 - par7 * f10).tex(f6, f9).color(red, green, blue, 1.0f).endVertex();
      buffer.pos(f11 - par3 * f10 + par6 * f10, f12 + par4 * f10, f13 - par5 * f10 + par7 * f10).tex(f6, f8).color(red, green, blue, 1.0f).endVertex();
      buffer.pos(f11 + par3 * f10 + par6 * f10, f12 + par4 * f10, f13 + par5 * f10 + par7 * f10).tex(f7, f8).color(red, green, blue, 1.0f).endVertex();
      buffer.pos(f11 + par3 * f10 - par6 * f10, f12 - par4 * f10, f13 + par5 * f10 - par7 * f10).tex(f7, f9).color(red, green, blue, 1.0f).endVertex();
   }

   protected boolean pushOutOfBlocks(double par1, double par3, double par5) {
      int var7 = MathHelper.floor(par1);
      int var8 = MathHelper.floor(par3);
      int var9 = MathHelper.floor(par5);
      double var10 = par1 - (double)var7;
      double var12 = par3 - (double)var8;
      double var14 = par5 - (double)var9;
      BlockPos pos = new BlockPos(var7, var8, var9);
      if (!this.world.isAirBlock(pos) && this.world.getBlockState(pos).getMaterial().blocksMovement()) {
         boolean var16 = !this.world.getBlockState(new BlockPos(var7 - 1, var8, var9)).getMaterial().blocksMovement();
         boolean var17 = !this.world.getBlockState(new BlockPos(var7 + 1, var8, var9)).getMaterial().blocksMovement();
         boolean var18 = !this.world.getBlockState(new BlockPos(var7, var8 - 1, var9)).getMaterial().blocksMovement();
         boolean var19 = !this.world.getBlockState(new BlockPos(var7, var8 + 1, var9)).getMaterial().blocksMovement();
         boolean var20 = !this.world.getBlockState(new BlockPos(var7, var8, var9 - 1)).getMaterial().blocksMovement();
         boolean var21 = !this.world.getBlockState(new BlockPos(var7, var8, var9 + 1)).getMaterial().blocksMovement();
         byte var22 = -1;
         double var23 = 9999.0F;
         if (var16 && var10 < var23) { var23 = var10; var22 = 0; }
         if (var17 && (double)1.0F - var10 < var23) { var23 = (double)1.0F - var10; var22 = 1; }
         if (var18 && var12 < var23) { var23 = var12; var22 = 2; }
         if (var19 && (double)1.0F - var12 < var23) { var23 = (double)1.0F - var12; var22 = 3; }
         if (var20 && var14 < var23) { var23 = var14; var22 = 4; }
         if (var21 && (double)1.0F - var14 < var23) { var22 = 5; }
         float var25 = this.rand.nextFloat() * 0.05F + 0.025F;
         float var26 = (this.rand.nextFloat() - this.rand.nextFloat()) * 0.1F;
         if (var22 == 0) { this.motionX = -var25; this.motionY = this.motionZ = var26; }
         if (var22 == 1) { this.motionX = var25; this.motionY = this.motionZ = var26; }
         if (var22 == 2) { this.motionY = -var25; this.motionX = this.motionZ = var26; }
         if (var22 == 3) { this.motionY = var25; this.motionX = this.motionZ = var26; }
         if (var22 == 4) { this.motionZ = -var25; this.motionY = this.motionX = var26; }
         if (var22 == 5) { this.motionZ = var25; this.motionY = this.motionX = var26; }
         return true;
      } else {
         return false;
      }
   }
}
