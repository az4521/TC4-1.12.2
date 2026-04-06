package thaumcraft.client.renderers.tile;

import java.nio.FloatBuffer;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;
import thaumcraft.client.lib.UtilsFX;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class TileEldritchNothingRenderer extends TileEntitySpecialRenderer<TileEntity> {
   FloatBuffer fBuffer = GLAllocation.createDirectFloatBuffer(16);
   private boolean inrange;
   private String t1 = "textures/misc/tunnel.png";
   private String t2 = "textures/misc/particlefield.png";
   private String t3 = "textures/misc/particlefield32.png";

   @Override


   public void render(TileEntity te, double x, double y, double z, float f, int destroyStage, float alpha) {
      double var10002 = (double)te.getPos().getX() + (double)0.5F;
      double var10003 = (double)te.getPos().getY() + (double)0.5F;
      double var10004 = te.getPos().getZ();
      this.inrange = Minecraft.getMinecraft().getRenderViewEntity().getDistanceSq(var10002, var10003, var10004 + (double)0.5F) < (double)512.0F;
      GlStateManager.disableFog();
      if (!te.getWorld().getBlockState(te.getPos().up()).isOpaqueCube()) {
         this.drawPlaneYNeg(x, y, z, f);
      }

      if (!te.getWorld().getBlockState(te.getPos().down()).isOpaqueCube()) {
         this.drawPlaneYPos(x, y, z, f);
      }

      if (!te.getWorld().getBlockState(te.getPos().north()).isOpaqueCube()) {
         this.drawPlaneZPos(x, y, z, f);
      }

      if (!te.getWorld().getBlockState(te.getPos().south()).isOpaqueCube()) {
         this.drawPlaneZNeg(x, y, z, f);
      }

      if (!te.getWorld().getBlockState(te.getPos().west()).isOpaqueCube()) {
         this.drawPlaneXPos(x, y, z, f);
      }

      if (!te.getWorld().getBlockState(te.getPos().east()).isOpaqueCube()) {
         this.drawPlaneXNeg(x, y, z, f);
      }

      GlStateManager.enableFog();
   }

   public void drawPlaneYPos(double x, double y, double z, float f) {
      float px = (float)TileEntityRendererDispatcher.staticPlayerX;
      float py = (float)TileEntityRendererDispatcher.staticPlayerY;
      float pz = (float)TileEntityRendererDispatcher.staticPlayerZ;
      GlStateManager.disableLighting();
      Random random = new Random(31100L);
      float offset = 0.0F;
      if (this.inrange) {
         for(int i = 0; i < 16; ++i) {
            GlStateManager.pushMatrix();
            float f5 = (float)(16 - i);
            float f6 = 0.0625F;
            float f7 = 1.0F / (f5 + 1.0F);
            if (i == 0) {
               UtilsFX.bindTexture(this.t1);
               f7 = 0.1F;
               f5 = 65.0F;
               f6 = 0.125F;
               GlStateManager.enableBlend();
               GlStateManager.blendFunc(770, 771);
            }

            if (i == 1) {
               UtilsFX.bindTexture(this.t2);
               GlStateManager.enableBlend();
               GlStateManager.blendFunc(1, 1);
               f6 = 0.5F;
            }

            float f8 = (float)(y + (double)offset);
            float f9 = f8 - (float)TileEntityRendererDispatcher.staticPlayerY;
            float f10 = f8 + f5 - (float)TileEntityRendererDispatcher.staticPlayerY;
            float f11 = f9 / f10;
            f11 = (float)(y + (double)offset) + f11;
            GlStateManager.translate(px, f11, pz);
            GL11.glTexGeni(8192, 9472, 9217);
            GL11.glTexGeni(8193, 9472, 9217);
            GL11.glTexGeni(8194, 9472, 9217);
            GL11.glTexGeni(8195, 9472, 9216);
            GL11.glTexGen(8192, 9473, this.calcFloatBuffer(1.0F, 0.0F, 0.0F, 0.0F));
            GL11.glTexGen(8193, 9473, this.calcFloatBuffer(0.0F, 0.0F, 1.0F, 0.0F));
            GL11.glTexGen(8194, 9473, this.calcFloatBuffer(0.0F, 0.0F, 0.0F, 1.0F));
            GL11.glTexGen(8195, 9474, this.calcFloatBuffer(0.0F, 1.0F, 0.0F, 0.0F));
            GL11.glEnable(3168);
            GL11.glEnable(3169);
            GL11.glEnable(3170);
            GL11.glEnable(3171);
            GlStateManager.popMatrix();
            GL11.glMatrixMode(5890);
            GlStateManager.pushMatrix();
            GL11.glLoadIdentity();
            GlStateManager.translate(0.0F, (float)(System.currentTimeMillis() % 700000L) / 250000.0F, 0.0F);
            GlStateManager.scale(f6, f6, f6);
            GlStateManager.translate(0.5F, 0.5F, 0.0F);
            GlStateManager.rotate((float)(i * i * 4321 + i * 9) * 2.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.translate(-0.5F, -0.5F, 0.0F);
            GlStateManager.translate(-px, -pz, -py);
            GlStateManager.translate((float)TileEntityRendererDispatcher.staticPlayerX * f5 / f9, (float)TileEntityRendererDispatcher.staticPlayerZ * f5 / f9, -py);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX); 
            f11 = random.nextFloat() * 0.5F + 0.1F;
            float f12 = random.nextFloat() * 0.5F + 0.4F;
            float f13 = random.nextFloat() * 0.5F + 0.5F;
            if (i == 0) {
               f13 = 1.0F;
               f12 = 1.0F;
               f11 = 1.0F;
            }

           
           
            buffer.pos(x, y + (double)offset, z + (double)1.0F);
            buffer.pos(x, y + (double)offset, z).endVertex();
            buffer.pos(x + (double)1.0F, y + (double)offset, z).endVertex();
            buffer.pos(x + (double)1.0F, y + (double)offset, z + (double)1.0F);
            tessellator.draw();
            GlStateManager.popMatrix();
            GL11.glMatrixMode(5888);
         }
      } else {
         GlStateManager.pushMatrix();
         UtilsFX.bindTexture(this.t3);
         Tessellator tessellator = Tessellator.getInstance();
         BufferBuilder buffer = tessellator.getBuffer();
         buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX); 
        
         buffer.pos(x, y + (double)offset, z + (double)1.0F).tex(1.0F, 1.0F)
        .endVertex();
         buffer.pos(x, y + (double)offset, z).tex(1.0F, 0.0F)
        .endVertex();
         buffer.pos(x + (double)1.0F, y + (double)offset, z).tex(0.0F, 0.0F)
        .endVertex();
         buffer.pos(x + (double)1.0F, y + (double)offset, z + (double)1.0F).tex(0.0F, 1.0F)
        .endVertex();
         tessellator.draw();
         GlStateManager.popMatrix();
      }

      GlStateManager.disableBlend();
      GL11.glDisable(3168);
      GL11.glDisable(3169);
      GL11.glDisable(3170);
      GL11.glDisable(3171);
      GlStateManager.enableLighting();
   }

   public void drawPlaneYNeg(double x, double y, double z, float f) {
      float f1 = (float)TileEntityRendererDispatcher.staticPlayerX;
      float f2 = (float)TileEntityRendererDispatcher.staticPlayerY;
      float f3 = (float)TileEntityRendererDispatcher.staticPlayerZ;
      GlStateManager.disableLighting();
      Random random = new Random(31100L);
      float offset = 1.0F;
      if (this.inrange) {
         for(int i = 0; i < 16; ++i) {
            GlStateManager.pushMatrix();
            float f5 = (float)(16 - i);
            float f6 = 0.0625F;
            float f7 = 1.0F / (f5 + 1.0F);
            if (i == 0) {
               UtilsFX.bindTexture(this.t1);
               f7 = 0.1F;
               f5 = 65.0F;
               f6 = 0.125F;
               GlStateManager.enableBlend();
               GlStateManager.blendFunc(770, 771);
            }

            if (i == 1) {
               UtilsFX.bindTexture(this.t2);
               GlStateManager.enableBlend();
               GlStateManager.blendFunc(1, 1);
               f6 = 0.5F;
            }

            float f8 = (float)(-(y + (double)offset));
            float f9 = f8 + (float)TileEntityRendererDispatcher.staticPlayerY;
            float f10 = f8 + f5 + (float)TileEntityRendererDispatcher.staticPlayerY;
            float f11 = f9 / f10;
            f11 = (float)(y + (double)offset) + f11;
            GlStateManager.translate(f1, f11, f3);
            GL11.glTexGeni(8192, 9472, 9217);
            GL11.glTexGeni(8193, 9472, 9217);
            GL11.glTexGeni(8194, 9472, 9217);
            GL11.glTexGeni(8195, 9472, 9216);
            GL11.glTexGen(8192, 9473, this.calcFloatBuffer(1.0F, 0.0F, 0.0F, 0.0F));
            GL11.glTexGen(8193, 9473, this.calcFloatBuffer(0.0F, 0.0F, 1.0F, 0.0F));
            GL11.glTexGen(8194, 9473, this.calcFloatBuffer(0.0F, 0.0F, 0.0F, 1.0F));
            GL11.glTexGen(8195, 9474, this.calcFloatBuffer(0.0F, 1.0F, 0.0F, 0.0F));
            GL11.glEnable(3168);
            GL11.glEnable(3169);
            GL11.glEnable(3170);
            GL11.glEnable(3171);
            GlStateManager.popMatrix();
            GL11.glMatrixMode(5890);
            GlStateManager.pushMatrix();
            GL11.glLoadIdentity();
            GlStateManager.translate(0.0F, (float)(System.currentTimeMillis() % 700000L) / 250000.0F, 0.0F);
            GlStateManager.scale(f6, f6, f6);
            GlStateManager.translate(0.5F, 0.5F, 0.0F);
            GlStateManager.rotate((float)(i * i * 4321 + i * 9) * 2.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.translate(-0.5F, -0.5F, 0.0F);
            GlStateManager.translate(-f1, -f3, -f2);
            GlStateManager.translate((float)TileEntityRendererDispatcher.staticPlayerX * f5 / f9, (float)TileEntityRendererDispatcher.staticPlayerZ * f5 / f9, -f2);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX); 
            f11 = random.nextFloat() * 0.5F + 0.1F;
            float f12 = random.nextFloat() * 0.5F + 0.4F;
            float f13 = random.nextFloat() * 0.5F + 0.5F;
            if (i == 0) {
               f13 = 1.0F;
               f12 = 1.0F;
               f11 = 1.0F;
            }

           
           
            buffer.pos(x, y + (double)offset, z).endVertex();
            buffer.pos(x, y + (double)offset, z + (double)1.0F);
            buffer.pos(x + (double)1.0F, y + (double)offset, z + (double)1.0F);
            buffer.pos(x + (double)1.0F, y + (double)offset, z).endVertex();
            tessellator.draw();
            GlStateManager.popMatrix();
            GL11.glMatrixMode(5888);
         }
      } else {
         GlStateManager.pushMatrix();
         UtilsFX.bindTexture(this.t3);
         Tessellator tessellator = Tessellator.getInstance();
         BufferBuilder buffer = tessellator.getBuffer();
         buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX); 
        
         buffer.pos(x, y + (double)offset, z).tex(1.0F, 1.0F)
        .endVertex();
         buffer.pos(x, y + (double)offset, z + (double)1.0F).tex(1.0F, 0.0F)
        .endVertex();
         buffer.pos(x + (double)1.0F, y + (double)offset, z + (double)1.0F).tex(0.0F, 0.0F)
        .endVertex();
         buffer.pos(x + (double)1.0F, y + (double)offset, z).tex(0.0F, 1.0F)
        .endVertex();
         tessellator.draw();
         GlStateManager.popMatrix();
      }

      GlStateManager.disableBlend();
      GL11.glDisable(3168);
      GL11.glDisable(3169);
      GL11.glDisable(3170);
      GL11.glDisable(3171);
      GlStateManager.enableLighting();
   }

   public void drawPlaneZNeg(double x, double y, double z, float f) {
      float px = (float)TileEntityRendererDispatcher.staticPlayerX;
      float py = (float)TileEntityRendererDispatcher.staticPlayerY;
      float pz = (float)TileEntityRendererDispatcher.staticPlayerZ;
      GlStateManager.disableLighting();
      Random random = new Random(31100L);
      float offset = 1.0F;
      if (this.inrange) {
         for(int i = 0; i < 16; ++i) {
            GlStateManager.pushMatrix();
            float f5 = (float)(16 - i);
            float f6 = 0.0625F;
            float f7 = 1.0F / (f5 + 1.0F);
            if (i == 0) {
               UtilsFX.bindTexture(this.t1);
               f7 = 0.1F;
               f5 = 65.0F;
               f6 = 0.125F;
               GlStateManager.enableBlend();
               GlStateManager.blendFunc(770, 771);
            }

            if (i == 1) {
               UtilsFX.bindTexture(this.t2);
               GlStateManager.enableBlend();
               GlStateManager.blendFunc(1, 1);
               f6 = 0.5F;
            }

            float f8 = (float)(-(z + (double)offset));
            float f9 = f8 + (float)TileEntityRendererDispatcher.staticPlayerZ;
            float f10 = f8 + f5 + (float)TileEntityRendererDispatcher.staticPlayerZ;
            float f11 = f9 / f10;
            f11 = (float)(z + (double)offset) + f11;
            GlStateManager.translate(px, py, f11);
            GL11.glTexGeni(8192, 9472, 9217);
            GL11.glTexGeni(8193, 9472, 9217);
            GL11.glTexGeni(8194, 9472, 9217);
            GL11.glTexGeni(8195, 9472, 9216);
            GL11.glTexGen(8192, 9473, this.calcFloatBuffer(1.0F, 0.0F, 0.0F, 0.0F));
            GL11.glTexGen(8193, 9473, this.calcFloatBuffer(0.0F, 1.0F, 0.0F, 0.0F));
            GL11.glTexGen(8194, 9473, this.calcFloatBuffer(0.0F, 0.0F, 0.0F, 1.0F));
            GL11.glTexGen(8195, 9474, this.calcFloatBuffer(0.0F, 0.0F, 1.0F, 0.0F));
            GL11.glEnable(3168);
            GL11.glEnable(3169);
            GL11.glEnable(3170);
            GL11.glEnable(3171);
            GlStateManager.popMatrix();
            GL11.glMatrixMode(5890);
            GlStateManager.pushMatrix();
            GL11.glLoadIdentity();
            GlStateManager.translate(0.0F, (float)(System.currentTimeMillis() % 700000L) / 250000.0F, 0.0F);
            GlStateManager.scale(f6, f6, f6);
            GlStateManager.translate(0.5F, 0.5F, 0.0F);
            GlStateManager.rotate((float)(i * i * 4321 + i * 9) * 2.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.translate(-0.5F, -0.5F, 0.0F);
            GlStateManager.translate(-px, -py, -pz);
            GlStateManager.translate((float)TileEntityRendererDispatcher.staticPlayerX * f5 / f9, (float)TileEntityRendererDispatcher.staticPlayerY * f5 / f9, -pz);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX); 
            f11 = random.nextFloat() * 0.5F + 0.1F;
            float f12 = random.nextFloat() * 0.5F + 0.4F;
            float f13 = random.nextFloat() * 0.5F + 0.5F;
            if (i == 0) {
               f13 = 1.0F;
               f12 = 1.0F;
               f11 = 1.0F;
            }

           
           
            buffer.pos(x, y + (double)1.0F, z + (double)offset);
            buffer.pos(x, y, z + (double)offset);
            buffer.pos(x + (double)1.0F, y, z + (double)offset);
            buffer.pos(x + (double)1.0F, y + (double)1.0F, z + (double)offset);
            tessellator.draw();
            GlStateManager.popMatrix();
            GL11.glMatrixMode(5888);
         }
      } else {
         GlStateManager.pushMatrix();
         UtilsFX.bindTexture(this.t3);
         Tessellator tessellator = Tessellator.getInstance();
         BufferBuilder buffer = tessellator.getBuffer();
         buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX); 
        
         buffer.pos(x, y + (double)1.0F, z + (double)offset).tex(1.0F, 1.0F)
        .endVertex();
         buffer.pos(x, y, z + (double)offset).tex(1.0F, 0.0F)
        .endVertex();
         buffer.pos(x + (double)1.0F, y, z + (double)offset).tex(0.0F, 0.0F)
        .endVertex();
         buffer.pos(x + (double)1.0F, y + (double)1.0F, z + (double)offset).tex(0.0F, 1.0F)
        .endVertex();
         tessellator.draw();
         GlStateManager.popMatrix();
      }

      GlStateManager.disableBlend();
      GL11.glDisable(3168);
      GL11.glDisable(3169);
      GL11.glDisable(3170);
      GL11.glDisable(3171);
      GlStateManager.enableLighting();
   }

   public void drawPlaneZPos(double x, double y, double z, float f) {
      float px = (float)TileEntityRendererDispatcher.staticPlayerX;
      float py = (float)TileEntityRendererDispatcher.staticPlayerY;
      float pz = (float)TileEntityRendererDispatcher.staticPlayerZ;
      GlStateManager.disableLighting();
      Random random = new Random(31100L);
      float offset = 0.0F;
      if (this.inrange) {
         for(int i = 0; i < 16; ++i) {
            GlStateManager.pushMatrix();
            float f5 = (float)(16 - i);
            float f6 = 0.0625F;
            float f7 = 1.0F / (f5 + 1.0F);
            if (i == 0) {
               UtilsFX.bindTexture(this.t1);
               f7 = 0.1F;
               f5 = 65.0F;
               f6 = 0.125F;
               GlStateManager.enableBlend();
               GlStateManager.blendFunc(770, 771);
            }

            if (i == 1) {
               UtilsFX.bindTexture(this.t2);
               GlStateManager.enableBlend();
               GlStateManager.blendFunc(1, 1);
               f6 = 0.5F;
            }

            float f8 = (float)(z + (double)offset);
            float f9 = f8 - (float)TileEntityRendererDispatcher.staticPlayerZ;
            float f10 = f8 + f5 - (float)TileEntityRendererDispatcher.staticPlayerZ;
            float f11 = f9 / f10;
            f11 = (float)(z + (double)offset) + f11;
            GlStateManager.translate(px, py, f11);
            GL11.glTexGeni(8192, 9472, 9217);
            GL11.glTexGeni(8193, 9472, 9217);
            GL11.glTexGeni(8194, 9472, 9217);
            GL11.glTexGeni(8195, 9472, 9216);
            GL11.glTexGen(8192, 9473, this.calcFloatBuffer(1.0F, 0.0F, 0.0F, 0.0F));
            GL11.glTexGen(8193, 9473, this.calcFloatBuffer(0.0F, 1.0F, 0.0F, 0.0F));
            GL11.glTexGen(8194, 9473, this.calcFloatBuffer(0.0F, 0.0F, 0.0F, 1.0F));
            GL11.glTexGen(8195, 9474, this.calcFloatBuffer(0.0F, 0.0F, 1.0F, 0.0F));
            GL11.glEnable(3168);
            GL11.glEnable(3169);
            GL11.glEnable(3170);
            GL11.glEnable(3171);
            GlStateManager.popMatrix();
            GL11.glMatrixMode(5890);
            GlStateManager.pushMatrix();
            GL11.glLoadIdentity();
            GlStateManager.translate(0.0F, (float)(System.currentTimeMillis() % 700000L) / 250000.0F, 0.0F);
            GlStateManager.scale(f6, f6, f6);
            GlStateManager.translate(0.5F, 0.5F, 0.0F);
            GlStateManager.rotate((float)(i * i * 4321 + i * 9) * 2.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.translate(-0.5F, -0.5F, 0.0F);
            GlStateManager.translate(-px, -py, -pz);
            GlStateManager.translate((float)TileEntityRendererDispatcher.staticPlayerX * f5 / f9, (float)TileEntityRendererDispatcher.staticPlayerY * f5 / f9, -pz);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX); 
            f11 = random.nextFloat() * 0.5F + 0.1F;
            float f12 = random.nextFloat() * 0.5F + 0.4F;
            float f13 = random.nextFloat() * 0.5F + 0.5F;
            if (i == 0) {
               f13 = 1.0F;
               f12 = 1.0F;
               f11 = 1.0F;
            }

           
           
            buffer.pos(x, y, z + (double)offset);
            buffer.pos(x, y + (double)1.0F, z + (double)offset);
            buffer.pos(x + (double)1.0F, y + (double)1.0F, z + (double)offset);
            buffer.pos(x + (double)1.0F, y, z + (double)offset);
            tessellator.draw();
            GlStateManager.popMatrix();
            GL11.glMatrixMode(5888);
         }
      } else {
         GlStateManager.pushMatrix();
         UtilsFX.bindTexture(this.t3);
         Tessellator tessellator = Tessellator.getInstance();
         BufferBuilder buffer = tessellator.getBuffer();
         buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX); 
        
         buffer.pos(x, y, z + (double)offset).tex(1.0F, 1.0F)
        .endVertex();
         buffer.pos(x, y + (double)1.0F, z + (double)offset).tex(1.0F, 0.0F)
        .endVertex();
         buffer.pos(x + (double)1.0F, y + (double)1.0F, z + (double)offset).tex(0.0F, 0.0F)
        .endVertex();
         buffer.pos(x + (double)1.0F, y, z + (double)offset).tex(0.0F, 1.0F)
        .endVertex();
         tessellator.draw();
         GlStateManager.popMatrix();
      }

      GlStateManager.disableBlend();
      GL11.glDisable(3168);
      GL11.glDisable(3169);
      GL11.glDisable(3170);
      GL11.glDisable(3171);
      GlStateManager.enableLighting();
   }

   public void drawPlaneXNeg(double x, double y, double z, float f) {
      float px = (float)TileEntityRendererDispatcher.staticPlayerX;
      float py = (float)TileEntityRendererDispatcher.staticPlayerY;
      float pz = (float)TileEntityRendererDispatcher.staticPlayerZ;
      GlStateManager.disableLighting();
      Random random = new Random(31100L);
      float offset = 1.0F;
      if (this.inrange) {
         for(int i = 0; i < 16; ++i) {
            GlStateManager.pushMatrix();
            float f5 = (float)(16 - i);
            float f6 = 0.0625F;
            float f7 = 1.0F / (f5 + 1.0F);
            if (i == 0) {
               UtilsFX.bindTexture(this.t1);
               f7 = 0.1F;
               f5 = 65.0F;
               f6 = 0.125F;
               GlStateManager.enableBlend();
               GlStateManager.blendFunc(770, 771);
            }

            if (i == 1) {
               UtilsFX.bindTexture(this.t2);
               GlStateManager.enableBlend();
               GlStateManager.blendFunc(1, 1);
               f6 = 0.5F;
            }

            float f8 = (float)(-(x + (double)offset));
            float f9 = f8 + (float)TileEntityRendererDispatcher.staticPlayerX;
            float f10 = f8 + f5 + (float)TileEntityRendererDispatcher.staticPlayerX;
            float f11 = f9 / f10;
            f11 = (float)(x + (double)offset) + f11;
            GlStateManager.translate(f11, py, pz);
            GL11.glTexGeni(8192, 9472, 9217);
            GL11.glTexGeni(8193, 9472, 9217);
            GL11.glTexGeni(8194, 9472, 9217);
            GL11.glTexGeni(8195, 9472, 9216);
            GL11.glTexGen(8193, 9473, this.calcFloatBuffer(0.0F, 1.0F, 0.0F, 0.0F));
            GL11.glTexGen(8192, 9473, this.calcFloatBuffer(0.0F, 0.0F, 1.0F, 0.0F));
            GL11.glTexGen(8194, 9473, this.calcFloatBuffer(0.0F, 0.0F, 0.0F, 1.0F));
            GL11.glTexGen(8195, 9474, this.calcFloatBuffer(1.0F, 0.0F, 0.0F, 0.0F));
            GL11.glEnable(3168);
            GL11.glEnable(3169);
            GL11.glEnable(3170);
            GL11.glEnable(3171);
            GlStateManager.popMatrix();
            GL11.glMatrixMode(5890);
            GlStateManager.pushMatrix();
            GL11.glLoadIdentity();
            GlStateManager.translate(0.0F, (float)(System.currentTimeMillis() % 700000L) / 250000.0F, 0.0F);
            GlStateManager.scale(f6, f6, f6);
            GlStateManager.translate(0.5F, 0.5F, 0.0F);
            GlStateManager.rotate((float)(i * i * 4321 + i * 9) * 2.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.translate(-0.5F, -0.5F, 0.0F);
            GlStateManager.translate(-pz, -py, -px);
            GlStateManager.translate((float)TileEntityRendererDispatcher.staticPlayerZ * f5 / f9, (float)TileEntityRendererDispatcher.staticPlayerY * f5 / f9, -px);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX); 
            f11 = random.nextFloat() * 0.5F + 0.1F;
            float f12 = random.nextFloat() * 0.5F + 0.4F;
            float f13 = random.nextFloat() * 0.5F + 0.5F;
            if (i == 0) {
               f13 = 1.0F;
               f12 = 1.0F;
               f11 = 1.0F;
            }

           
           
            buffer.pos(x + (double)offset, y + (double)1.0F, z).endVertex();
            buffer.pos(x + (double)offset, y + (double)1.0F, z + (double)1.0F);
            buffer.pos(x + (double)offset, y, z + (double)1.0F);
            buffer.pos(x + (double)offset, y, z).endVertex();
            tessellator.draw();
            GlStateManager.popMatrix();
            GL11.glMatrixMode(5888);
         }
      } else {
         GlStateManager.pushMatrix();
         UtilsFX.bindTexture(this.t3);
         Tessellator tessellator = Tessellator.getInstance();
         BufferBuilder buffer = tessellator.getBuffer();
         buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX); 
        
         buffer.pos(x + (double)offset, y + (double)1.0F, z).tex(1.0F, 1.0F)
        .endVertex();
         buffer.pos(x + (double)offset, y + (double)1.0F, z + (double)1.0F).tex(1.0F, 0.0F)
        .endVertex();
         buffer.pos(x + (double)offset, y, z + (double)1.0F).tex(0.0F, 0.0F)
        .endVertex();
         buffer.pos(x + (double)offset, y, z).tex(0.0F, 1.0F)
        .endVertex();
         tessellator.draw();
         GlStateManager.popMatrix();
      }

      GlStateManager.disableBlend();
      GL11.glDisable(3168);
      GL11.glDisable(3169);
      GL11.glDisable(3170);
      GL11.glDisable(3171);
      GlStateManager.enableLighting();
   }

   public void drawPlaneXPos(double x, double y, double z, float f) {
      float px = (float)TileEntityRendererDispatcher.staticPlayerX;
      float py = (float)TileEntityRendererDispatcher.staticPlayerY;
      float pz = (float)TileEntityRendererDispatcher.staticPlayerZ;
      GlStateManager.disableLighting();
      Random random = new Random(31100L);
      float offset = 0.0F;
      if (this.inrange) {
         for(int i = 0; i < 16; ++i) {
            GlStateManager.pushMatrix();
            float f5 = (float)(16 - i);
            float f6 = 0.0625F;
            float f7 = 1.0F / (f5 + 1.0F);
            if (i == 0) {
               UtilsFX.bindTexture(this.t1);
               f7 = 0.1F;
               f5 = 65.0F;
               f6 = 0.125F;
               GlStateManager.enableBlend();
               GlStateManager.blendFunc(770, 771);
            }

            if (i == 1) {
               UtilsFX.bindTexture(this.t2);
               GlStateManager.enableBlend();
               GlStateManager.blendFunc(1, 1);
               f6 = 0.5F;
            }

            float f8 = (float)(x + (double)offset);
            float f9 = f8 - (float)TileEntityRendererDispatcher.staticPlayerX;
            float f10 = f8 + f5 - (float)TileEntityRendererDispatcher.staticPlayerX;
            float f11 = f9 / f10;
            f11 = (float)(x + (double)offset) + f11;
            GlStateManager.translate(f11, py, pz);
            GL11.glTexGeni(8192, 9472, 9217);
            GL11.glTexGeni(8193, 9472, 9217);
            GL11.glTexGeni(8194, 9472, 9217);
            GL11.glTexGeni(8195, 9472, 9216);
            GL11.glTexGen(8193, 9473, this.calcFloatBuffer(0.0F, 1.0F, 0.0F, 0.0F));
            GL11.glTexGen(8192, 9473, this.calcFloatBuffer(0.0F, 0.0F, 1.0F, 0.0F));
            GL11.glTexGen(8194, 9473, this.calcFloatBuffer(0.0F, 0.0F, 0.0F, 1.0F));
            GL11.glTexGen(8195, 9474, this.calcFloatBuffer(1.0F, 0.0F, 0.0F, 0.0F));
            GL11.glEnable(3168);
            GL11.glEnable(3169);
            GL11.glEnable(3170);
            GL11.glEnable(3171);
            GlStateManager.popMatrix();
            GL11.glMatrixMode(5890);
            GlStateManager.pushMatrix();
            GL11.glLoadIdentity();
            GlStateManager.translate(0.0F, (float)(System.currentTimeMillis() % 700000L) / 250000.0F, 0.0F);
            GlStateManager.scale(f6, f6, f6);
            GlStateManager.translate(0.5F, 0.5F, 0.0F);
            GlStateManager.rotate((float)(i * i * 4321 + i * 9) * 2.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.translate(-0.5F, -0.5F, 0.0F);
            GlStateManager.translate(-pz, -py, -px);
            GlStateManager.translate((float)TileEntityRendererDispatcher.staticPlayerZ * f5 / f9, (float)TileEntityRendererDispatcher.staticPlayerY * f5 / f9, -px);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX); 
            f11 = random.nextFloat() * 0.5F + 0.1F;
            float f12 = random.nextFloat() * 0.5F + 0.4F;
            float f13 = random.nextFloat() * 0.5F + 0.5F;
            if (i == 0) {
               f13 = 1.0F;
               f12 = 1.0F;
               f11 = 1.0F;
            }

           
           
            buffer.pos(x + (double)offset, y, z).endVertex();
            buffer.pos(x + (double)offset, y, z + (double)1.0F);
            buffer.pos(x + (double)offset, y + (double)1.0F, z + (double)1.0F);
            buffer.pos(x + (double)offset, y + (double)1.0F, z).endVertex();
            tessellator.draw();
            GlStateManager.popMatrix();
            GL11.glMatrixMode(5888);
         }
      } else {
         GlStateManager.pushMatrix();
         UtilsFX.bindTexture(this.t3);
         Tessellator tessellator = Tessellator.getInstance();
         BufferBuilder buffer = tessellator.getBuffer();
         buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX); 
        
         buffer.pos(x + (double)offset, y, z).tex(1.0F, 1.0F)
        .endVertex();
         buffer.pos(x + (double)offset, y, z + (double)1.0F).tex(1.0F, 0.0F)
        .endVertex();
         buffer.pos(x + (double)offset, y + (double)1.0F, z + (double)1.0F).tex(0.0F, 0.0F)
        .endVertex();
         buffer.pos(x + (double)offset, y + (double)1.0F, z).tex(0.0F, 1.0F)
        .endVertex();
         tessellator.draw();
         GlStateManager.popMatrix();
      }

      GlStateManager.disableBlend();
      GL11.glDisable(3168);
      GL11.glDisable(3169);
      GL11.glDisable(3170);
      GL11.glDisable(3171);
      GlStateManager.enableLighting();
   }

   private FloatBuffer calcFloatBuffer(float f, float f1, float f2, float f3) {
      this.fBuffer.clear();
      this.fBuffer.put(f).put(f1).put(f2).put(f3);
      this.fBuffer.flip();
      return this.fBuffer;
   }
}
