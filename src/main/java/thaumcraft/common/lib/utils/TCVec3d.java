package thaumcraft.common.lib.utils;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.util.math.MathHelper;

public class TCVec3d {
   public static final TCVec3Pool vec3dPool = new TCVec3Pool(-1, -1);
   public final TCVec3Pool myVec3LocalPool;
   public double x;
   public double y;
   public double z;

   public static TCVec3d createVectorHelper(double par0, double par2, double par4) {
      return new TCVec3d(vec3dPool, par0, par2, par4);
   }

   protected TCVec3d(TCVec3Pool par1Vec3Pool, double par2, double par4, double par6) {
      if (par2 == (double)-0.0F) {
         par2 = 0.0F;
      }

      if (par4 == (double)-0.0F) {
         par4 = 0.0F;
      }

      if (par6 == (double)-0.0F) {
         par6 = 0.0F;
      }

      this.x = par2;
      this.y = par4;
      this.z = par6;
      this.myVec3LocalPool = par1Vec3Pool;
   }

   protected TCVec3d setComponents(double par1, double par3, double par5) {
      this.x = par1;
      this.y = par3;
      this.z = par5;
      return this;
   }

   @SideOnly(Side.CLIENT)
   public TCVec3d subtract(TCVec3d par1Vec3) {
      return this.myVec3LocalPool.getVecFromPool(par1Vec3.x - this.x, par1Vec3.y - this.y, par1Vec3.z - this.z);
   }

   public TCVec3d normalize() {
      double var1 = MathHelper.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
      return var1 < 1.0E-4 ? this.myVec3LocalPool.getVecFromPool(0.0F, 0.0F, 0.0F) : this.myVec3LocalPool.getVecFromPool(this.x / var1, this.y / var1, this.z / var1);
   }

   public double dotProduct(TCVec3d par1Vec3) {
      return this.x * par1Vec3.x + this.y * par1Vec3.y + this.z * par1Vec3.z;
   }

   @SideOnly(Side.CLIENT)
   public TCVec3d crossProduct(TCVec3d par1Vec3) {
      return this.myVec3LocalPool.getVecFromPool(this.y * par1Vec3.z - this.z * par1Vec3.y, this.z * par1Vec3.x - this.x * par1Vec3.z, this.x * par1Vec3.y - this.y * par1Vec3.x);
   }

   public TCVec3d addVector(double par1, double par3, double par5) {
      return this.myVec3LocalPool.getVecFromPool(this.x + par1, this.y + par3, this.z + par5);
   }

   public double distanceTo(TCVec3d par1Vec3) {
      double var2 = par1Vec3.x - this.x;
      double var4 = par1Vec3.y - this.y;
      double var6 = par1Vec3.z - this.z;
      return MathHelper.sqrt(var2 * var2 + var4 * var4 + var6 * var6);
   }

   public double squareDistanceTo(TCVec3d par1Vec3) {
      double var2 = par1Vec3.x - this.x;
      double var4 = par1Vec3.y - this.y;
      double var6 = par1Vec3.z - this.z;
      return var2 * var2 + var4 * var4 + var6 * var6;
   }

   public double squareDistanceTo(double par1, double par3, double par5) {
      double var7 = par1 - this.x;
      double var9 = par3 - this.y;
      double var11 = par5 - this.z;
      return var7 * var7 + var9 * var9 + var11 * var11;
   }

   public double lengthVector() {
      return MathHelper.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
   }

   public TCVec3d getIntermediateWithXValue(TCVec3d par1Vec3, double par2) {
      double var4 = par1Vec3.x - this.x;
      double var6 = par1Vec3.y - this.y;
      double var8 = par1Vec3.z - this.z;
      if (var4 * var4 < (double)1.0E-7F) {
         return null;
      } else {
         double var10 = (par2 - this.x) / var4;
         return var10 >= (double)0.0F && var10 <= (double)1.0F ? this.myVec3LocalPool.getVecFromPool(this.x + var4 * var10, this.y + var6 * var10, this.z + var8 * var10) : null;
      }
   }

   public TCVec3d getIntermediateWithYValue(TCVec3d par1Vec3, double par2) {
      double var4 = par1Vec3.x - this.x;
      double var6 = par1Vec3.y - this.y;
      double var8 = par1Vec3.z - this.z;
      if (var6 * var6 < (double)1.0E-7F) {
         return null;
      } else {
         double var10 = (par2 - this.y) / var6;
         return var10 >= (double)0.0F && var10 <= (double)1.0F ? this.myVec3LocalPool.getVecFromPool(this.x + var4 * var10, this.y + var6 * var10, this.z + var8 * var10) : null;
      }
   }

   public TCVec3d getIntermediateWithZValue(TCVec3d par1Vec3, double par2) {
      double var4 = par1Vec3.x - this.x;
      double var6 = par1Vec3.y - this.y;
      double var8 = par1Vec3.z - this.z;
      if (var8 * var8 < (double)1.0E-7F) {
         return null;
      } else {
         double var10 = (par2 - this.z) / var8;
         return var10 >= (double)0.0F && var10 <= (double)1.0F ? this.myVec3LocalPool.getVecFromPool(this.x + var4 * var10, this.y + var6 * var10, this.z + var8 * var10) : null;
      }
   }

   public String toString() {
      return "(" + this.x + ", " + this.y + ", " + this.z + ")";
   }

   public void rotateAroundX(float par1) {
      float var2 = MathHelper.cos(par1);
      float var3 = MathHelper.sin(par1);
      double var4 = this.x;
      double var6 = this.y * (double)var2 + this.z * (double)var3;
      double var8 = this.z * (double)var2 - this.y * (double)var3;
      this.x = var4;
      this.y = var6;
      this.z = var8;
   }

   public void rotateAroundY(float par1) {
      float var2 = MathHelper.cos(par1);
      float var3 = MathHelper.sin(par1);
      double var4 = this.x * (double)var2 + this.z * (double)var3;
      double var6 = this.y;
      double var8 = this.z * (double)var2 - this.x * (double)var3;
      this.x = var4;
      this.y = var6;
      this.z = var8;
   }

   public void rotateAroundZ(float par1) {
      float var2 = MathHelper.cos(par1);
      float var3 = MathHelper.sin(par1);
      double var4 = this.x * (double)var2 + this.y * (double)var3;
      double var6 = this.y * (double)var2 - this.x * (double)var3;
      double var8 = this.z;
      this.x = var4;
      this.y = var6;
      this.z = var8;
   }
}
