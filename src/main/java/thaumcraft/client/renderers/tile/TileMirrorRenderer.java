package thaumcraft.client.renderers.tile;

import net.minecraftforge.fml.client.FMLClientHandler;
import java.nio.FloatBuffer;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import org.lwjgl.opengl.GL11;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.tiles.TileMirror;
import thaumcraft.common.tiles.TileMirrorEssentia;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class TileMirrorRenderer extends TileEntitySpecialRenderer<TileEntity> {
   FloatBuffer fBuffer = GLAllocation.createDirectFloatBuffer(16);
   private String t1 = "textures/misc/tunnel.png";
   private String t2 = "textures/misc/particlefield.png";

   public void drawPlaneYPos(TileEntity tileentityendportal, double x, double y, double z, float f) {
      float px = (float)TileEntityRendererDispatcher.staticPlayerX;
      float py = (float)TileEntityRendererDispatcher.staticPlayerY;
      float pz = (float)TileEntityRendererDispatcher.staticPlayerZ;
      GlStateManager.disableLighting();
      Random random = new Random(31100L);
      float offset = 0.99F;
      float p = 0.1875F;

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

        
        
         buffer.pos(x + (double)p, y + (double)offset, z + (double)1.0F - (double)p);
         buffer.pos(x + (double)p, y + (double)offset, z + (double)p);
         buffer.pos(x + (double)1.0F - (double)p, y + (double)offset, z + (double)p);
         buffer.pos(x + (double)1.0F - (double)p, y + (double)offset, z + (double)1.0F - (double)p);
         tessellator.draw();
         GlStateManager.popMatrix();
         GL11.glMatrixMode(5888);
      }

      GlStateManager.disableBlend();
      GL11.glDisable(3168);
      GL11.glDisable(3169);
      GL11.glDisable(3170);
      GL11.glDisable(3171);
      GlStateManager.enableLighting();
   }

   public void drawPlaneYNeg(TileEntity tileentityendportal, double x, double y, double z, float f) {
      float f1 = (float)TileEntityRendererDispatcher.staticPlayerX;
      float f2 = (float)TileEntityRendererDispatcher.staticPlayerY;
      float f3 = (float)TileEntityRendererDispatcher.staticPlayerZ;
      GlStateManager.disableLighting();
      Random random = new Random(31100L);
      float offset = 0.01F;
      float p = 0.1875F;

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

        
        
         buffer.pos(x + (double)p, y + (double)offset, z + (double)p);
         buffer.pos(x + (double)p, y + (double)offset, z + (double)1.0F - (double)p);
         buffer.pos(x + (double)1.0F - (double)p, y + (double)offset, z + (double)1.0F - (double)p);
         buffer.pos(x + (double)1.0F - (double)p, y + (double)offset, z + (double)p);
         tessellator.draw();
         GlStateManager.popMatrix();
         GL11.glMatrixMode(5888);
      }

      GlStateManager.disableBlend();
      GL11.glDisable(3168);
      GL11.glDisable(3169);
      GL11.glDisable(3170);
      GL11.glDisable(3171);
      GlStateManager.enableLighting();
   }

   public void drawPlaneZNeg(TileEntity tileentityendportal, double x, double y, double z, float f) {
      float px = (float)TileEntityRendererDispatcher.staticPlayerX;
      float py = (float)TileEntityRendererDispatcher.staticPlayerY;
      float pz = (float)TileEntityRendererDispatcher.staticPlayerZ;
      GlStateManager.disableLighting();
      Random random = new Random(31100L);
      float offset = 0.01F;
      float p = 0.1875F;

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

        
        
         buffer.pos(x + (double)p, y + (double)1.0F - (double)p, z + (double)offset);
         buffer.pos(x + (double)p, y + (double)p, z + (double)offset);
         buffer.pos(x + (double)1.0F - (double)p, y + (double)p, z + (double)offset);
         buffer.pos(x + (double)1.0F - (double)p, y + (double)1.0F - (double)p, z + (double)offset);
         tessellator.draw();
         GlStateManager.popMatrix();
         GL11.glMatrixMode(5888);
      }

      GlStateManager.disableBlend();
      GL11.glDisable(3168);
      GL11.glDisable(3169);
      GL11.glDisable(3170);
      GL11.glDisable(3171);
      GlStateManager.enableLighting();
   }

   public void drawPlaneZPos(TileEntity tileentityendportal, double x, double y, double z, float f) {
      float px = (float)TileEntityRendererDispatcher.staticPlayerX;
      float py = (float)TileEntityRendererDispatcher.staticPlayerY;
      float pz = (float)TileEntityRendererDispatcher.staticPlayerZ;
      GlStateManager.disableLighting();
      Random random = new Random(31100L);
      float offset = 0.99F;
      float p = 0.1875F;

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

        
        
         buffer.pos(x + (double)p, y + (double)p, z + (double)offset);
         buffer.pos(x + (double)p, y + (double)1.0F - (double)p, z + (double)offset);
         buffer.pos(x + (double)1.0F - (double)p, y + (double)1.0F - (double)p, z + (double)offset);
         buffer.pos(x + (double)1.0F - (double)p, y + (double)p, z + (double)offset);
         tessellator.draw();
         GlStateManager.popMatrix();
         GL11.glMatrixMode(5888);
      }

      GlStateManager.disableBlend();
      GL11.glDisable(3168);
      GL11.glDisable(3169);
      GL11.glDisable(3170);
      GL11.glDisable(3171);
      GlStateManager.enableLighting();
   }

   public void drawPlaneXNeg(TileEntity tileentityendportal, double x, double y, double z, float f) {
      float px = (float)TileEntityRendererDispatcher.staticPlayerX;
      float py = (float)TileEntityRendererDispatcher.staticPlayerY;
      float pz = (float)TileEntityRendererDispatcher.staticPlayerZ;
      GlStateManager.disableLighting();
      Random random = new Random(31100L);
      float offset = 0.01F;
      float p = 0.1875F;

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

        
        
         buffer.pos(x + (double)offset, y + (double)1.0F - (double)p, z + (double)p);
         buffer.pos(x + (double)offset, y + (double)1.0F - (double)p, z + (double)1.0F - (double)p);
         buffer.pos(x + (double)offset, y + (double)p, z + (double)1.0F - (double)p);
         buffer.pos(x + (double)offset, y + (double)p, z + (double)p);
         tessellator.draw();
         GlStateManager.popMatrix();
         GL11.glMatrixMode(5888);
      }

      GlStateManager.disableBlend();
      GL11.glDisable(3168);
      GL11.glDisable(3169);
      GL11.glDisable(3170);
      GL11.glDisable(3171);
      GlStateManager.enableLighting();
   }

   public void drawPlaneXPos(TileEntity tileentityendportal, double x, double y, double z, float f) {
      float px = (float)TileEntityRendererDispatcher.staticPlayerX;
      float py = (float)TileEntityRendererDispatcher.staticPlayerY;
      float pz = (float)TileEntityRendererDispatcher.staticPlayerZ;
      GlStateManager.disableLighting();
      Random random = new Random(31100L);
      float offset = 0.99F;
      float p = 0.1875F;

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

        
        
         buffer.pos(x + (double)offset, y + (double)p, z + (double)p);
         buffer.pos(x + (double)offset, y + (double)p, z + (double)1.0F - (double)p);
         buffer.pos(x + (double)offset, y + (double)1.0F - (double)p, z + (double)1.0F - (double)p);
         buffer.pos(x + (double)offset, y + (double)1.0F - (double)p, z + (double)p);
         tessellator.draw();
         GlStateManager.popMatrix();
         GL11.glMatrixMode(5888);
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

   @Override


   public void render(TileEntity te, double x, double y, double z, float f, int destroyStage, float alpha) {
      EnumFacing dir = EnumFacing.byIndex(
        te.getBlockMetadata() % 6);
      boolean linked = false;
      float instability = 0.0F;
      if (te instanceof TileMirror) {
         linked = ((TileMirror)te).linked;
         if (((TileMirror)te).instability > 0) {
            instability = Minecraft.getMinecraft().world.rand.nextFloat() * ((float)((TileMirror)te).instability / 10000.0F);
         }
      }

      if (te instanceof TileMirrorEssentia) {
         linked = ((TileMirrorEssentia)te).linked;
      }

      label30: {
         int b = te.getWorld().getCombinedLight(te.getPos(), 0);
         if (linked) {
            double var10002 = (double)te.getPos().getX() + (double)0.5F;
            double var10003 = (double)te.getPos().getY() + (double)0.5F;
            double var10004 = te.getPos().getZ();
            if (UtilsFX.isVisibleTo(1.5F, FMLClientHandler.instance().getClient().player, var10002, var10003, var10004 + (double)0.5F)) {
               GlStateManager.pushMatrix();
               switch (dir) {
                  case DOWN:
                     this.drawPlaneYPos(te, x, y, z, f);
                     break;
                  case UP:
                     this.drawPlaneYNeg(te, x, y, z, f);
                     break;
                  case WEST:
                     this.drawPlaneXPos(te, x, y, z, f);
                     break;
                  case EAST:
                     this.drawPlaneXNeg(te, x, y, z, f);
                     break;
                  case NORTH:
                     this.drawPlaneZPos(te, x, y, z, f);
                     break;
                  case SOUTH:
                     this.drawPlaneZNeg(te, x, y, z, f);
               }

               GlStateManager.popMatrix();
               GlStateManager.pushMatrix();
               this.translateFromOrientation((float)x, (float)y, (float)z, dir.ordinal(), 0.02F + instability);
               UtilsFX.renderQuadFromTexture("textures/blocks/mirrorpanetrans.png", 1, 0, 1.0F, 1.0F, 1.0F, 1.0F, b, 771, 1.0F);
               GlStateManager.popMatrix();
               break label30;
            }
         }

         GlStateManager.pushMatrix();
         this.translateFromOrientation((float)x, (float)y, (float)z, dir.ordinal(), 0.02F + instability);
         UtilsFX.renderQuadFromTexture("textures/blocks/mirrorpane.png", 1, 0, 1.0F, 1.0F, 1.0F, 1.0F, b, 771, 1.0F);
         GlStateManager.popMatrix();
      }

      GlStateManager.pushMatrix();
      this.translateFromOrientation((float)x, (float)y, (float)z, dir.ordinal(), 0.0F);
      TextureAtlasSprite icon = net.minecraft.client.Minecraft.getMinecraft()
            .getBlockRendererDispatcher().getBlockModelShapes()
            .getTexture(te.getWorld().getBlockState(te.getPos()));
      float f1 = icon.getMaxU();
      float f2 = icon.getMinV();
      float f3 = icon.getMinU();
      float f4 = icon.getMaxV();
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder buffer = tessellator.getBuffer();
      this.rendererDispatcher.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
      GlStateManager.popMatrix();
   }

   private void translateFromOrientation(float x, float y, float z, int orientation, float off) {
      if (orientation == 0) {
         GlStateManager.translate(x, y + 1.0F, z + 1.0F);
         GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
      } else if (orientation == 1) {
         GlStateManager.translate(x, y, z);
         GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
      } else if (orientation == 2) {
         GlStateManager.translate(x, y, z + 1.0F);
      } else if (orientation == 3) {
         GlStateManager.translate(x + 1.0F, y, z);
         GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
      } else if (orientation == 4) {
         GlStateManager.translate(x + 1.0F, y, z + 1.0F);
         GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
      } else if (orientation == 5) {
         GlStateManager.translate(x, y, z);
         GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
      }

      GlStateManager.translate(0.0F, 0.0F, -off);
   }
}
