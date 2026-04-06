package thaumcraft.client.renderers.tile;

import java.nio.FloatBuffer;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.ResourceLocation;
import thaumcraft.client.renderers.models.AdvancedModelLoader;
import thaumcraft.client.renderers.models.IModelCustom;
import org.lwjgl.opengl.GL11;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.config.Config;

import static thaumcraft.client.renderers.tile.TileBlockInfoGetter.getBlockTypeSafely;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class TileEldritchObeliskRenderer extends TileEntitySpecialRenderer<TileEntity> {
   FloatBuffer fBuffer = GLAllocation.createDirectFloatBuffer(16);
   private boolean inrange;
   private IModelCustom model;
   private static final ResourceLocation CAP = new ResourceLocation("thaumcraft", "textures/models/obelisk_cap.obj");
   private String t1 = "textures/misc/tunnel.png";
   private String t2 = "textures/misc/particlefield.png";
   private String t3 = "textures/misc/particlefield32.png";
   private String t4 = "textures/models/obelisk_side.png";
   private String t5 = "textures/models/obelisk_cap.png";
   private String t6 = "textures/models/obelisk_side_2.png";
   private String t7 = "textures/models/obelisk_cap_2.png";

   public TileEldritchObeliskRenderer() {
      this.model = AdvancedModelLoader.loadModel(CAP);
   }

   @Override


   public void render(TileEntity te, double x, double y, double z, float f, int destroyStage, float alpha) {
      double var10002 = (double)te.getPos().getX() + (double)0.5F;
      double var10003 = (double)te.getPos().getY() + (double)0.5F;
      double var10004 = te.getPos().getZ();
      this.inrange = Minecraft.getMinecraft().getRenderViewEntity().getDistanceSq(var10002, var10003, var10004 + (double)0.5F) < (double)512.0F;
      float bob = 0.0F;
      float count = (float)Minecraft.getMinecraft().getRenderViewEntity().ticksExisted + f;
      bob = MathHelper.sin(count / 10.0F) * 0.1F + 0.1F;
      GlStateManager.pushMatrix();
      GlStateManager.disableFog();
      this.drawPlaneZNeg(x, y + (double)1.0F + (double)bob, z, f, 3);
      this.drawPlaneZPos(x, y + (double)1.0F + (double)bob, z, f, 3);
      this.drawPlaneXNeg(x, y + (double)1.0F + (double)bob, z, f, 3);
      this.drawPlaneXPos(x, y + (double)1.0F + (double)bob, z, f, 3);
      GlStateManager.enableFog();
      GlStateManager.popMatrix();
      GlStateManager.pushMatrix();
      GlStateManager.enableLighting();
      GlStateManager.enableRescaleNormal();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(770, 771);
      String tempTex1 = this.t4;
      String tempTex2 = this.t5;
      Block blockType = getBlockTypeSafely(te);
      if (blockType != null) {
         int j = te.getWorld().getCombinedLight(te.getPos().up(5), 0);
         int k = j & 0xFFFF;
         int l = (j >> 16) & 0xFFFF;
         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) k, (float) l);
         if (te.hasWorld() && te.getWorld().provider.getDimension() == Config.dimensionOuterId) {
            tempTex1 = this.t6;
            tempTex2 = this.t7;
         }
      }

      GlStateManager.pushMatrix();
      UtilsFX.bindTexture(tempTex1);
      GlStateManager.translate(x + (double)0.5F, y + (double)1.0F + (double)bob, z + (double)0.5F);

      for(int a = 0; a < 4; ++a) {
         GlStateManager.pushMatrix();
         GlStateManager.rotate((float)(a * 90), 0.0F, 1.0F, 0.0F);
         GlStateManager.translate(0.0F, 0.0F, -0.5F);
         this.renderSide(3);
         GlStateManager.popMatrix();
      }

      GlStateManager.popMatrix();
      GlStateManager.pushMatrix();
      GlStateManager.translate(x + (double)0.5F, y + (double)1.0F + (double)bob, z + (double)0.5F);
      GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
      UtilsFX.bindTexture(tempTex2);
      this.model.renderPart("Cap");
      GlStateManager.popMatrix();
      GlStateManager.pushMatrix();
      GlStateManager.translate(x + (double)0.5F, y + (double)4.0F + (double)bob, z + (double)0.5F);
      GlStateManager.rotate(90.0F, -1.0F, 0.0F, 0.0F);
      this.model.renderPart("Cap");
      GlStateManager.popMatrix();
      GlStateManager.disableBlend();
      GlStateManager.disableRescaleNormal();
      GlStateManager.popMatrix();
   }

   public void drawPlaneZPos(double x, double y, double z, float f, int height) {
      float px = (float)TileEntityRendererDispatcher.staticPlayerX;
      float py = (float)TileEntityRendererDispatcher.staticPlayerY;
      float pz = (float)TileEntityRendererDispatcher.staticPlayerZ;
      GlStateManager.disableLighting();
      Random random = new Random(31100L);
      float offset = 0.99F;
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

           
           
            buffer.pos(x, y + (double)height, z + (double)offset);
            buffer.pos(x, y, z + (double)offset);
            buffer.pos(x + (double)1.0F, y, z + (double)offset);
            buffer.pos(x + (double)1.0F, y + (double)height, z + (double)offset);
            tessellator.draw();
            GlStateManager.popMatrix();
            GL11.glMatrixMode(5888);
         }
      } else {
         GlStateManager.pushMatrix();
         UtilsFX.bindTexture(this.t3);
         Tessellator tessellator = Tessellator.getInstance();
         BufferBuilder buffer = tessellator.getBuffer();
         buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR); 
        
        
         buffer.pos(x, y + (double)height, z + (double)offset).tex(1.0F, 1.0F).color(0.5F, 0.5F, 0.5F, 1.0F)
        .endVertex();
         buffer.pos(x, y, z + (double)offset).tex(1.0F, 0.0F).color(0.5F, 0.5F, 0.5F, 1.0F)
        .endVertex();
         buffer.pos(x + (double)1.0F, y, z + (double)offset).tex(0.0F, 0.0F).color(0.5F, 0.5F, 0.5F, 1.0F)
        .endVertex();
         buffer.pos(x + (double)1.0F, y + (double)height, z + (double)offset).tex(0.0F, 1.0F).color(0.5F, 0.5F, 0.5F, 1.0F)
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

   public void drawPlaneZNeg(double x, double y, double z, float f, int height) {
      float px = (float)TileEntityRendererDispatcher.staticPlayerX;
      float py = (float)TileEntityRendererDispatcher.staticPlayerY;
      float pz = (float)TileEntityRendererDispatcher.staticPlayerZ;
      GlStateManager.disableLighting();
      Random random = new Random(31100L);
      float offset = 0.01F;
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
            buffer.pos(x, y + (double)height, z + (double)offset);
            buffer.pos(x + (double)1.0F, y + (double)height, z + (double)offset);
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
         buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR); 
        
        
         buffer.pos(x, y, z + (double)offset).tex(1.0F, 1.0F).color(0.5F, 0.5F, 0.5F, 1.0F)
        .endVertex();
         buffer.pos(x, y + (double)height, z + (double)offset).tex(1.0F, 0.0F).color(0.5F, 0.5F, 0.5F, 1.0F)
        .endVertex();
         buffer.pos(x + (double)1.0F, y + (double)height, z + (double)offset).tex(0.0F, 0.0F).color(0.5F, 0.5F, 0.5F, 1.0F)
        .endVertex();
         buffer.pos(x + (double)1.0F, y, z + (double)offset).tex(0.0F, 1.0F).color(0.5F, 0.5F, 0.5F, 1.0F)
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

   public void drawPlaneXPos(double x, double y, double z, float f, int height) {
      float px = (float)TileEntityRendererDispatcher.staticPlayerX;
      float py = (float)TileEntityRendererDispatcher.staticPlayerY;
      float pz = (float)TileEntityRendererDispatcher.staticPlayerZ;
      GlStateManager.disableLighting();
      Random random = new Random(31100L);
      float offset = 0.99F;
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

           
           
            buffer.pos(x + (double)offset, y + (double)height, z).endVertex();
            buffer.pos(x + (double)offset, y + (double)height, z + (double)1.0F);
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
         buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR); 
        
        
         buffer.pos(x + (double)offset, y + (double)height, z).tex(1.0F, 1.0F).color(0.5F, 0.5F, 0.5F, 1.0F)
        .endVertex();
         buffer.pos(x + (double)offset, y + (double)height, z + (double)1.0F).tex(1.0F, 0.0F).color(0.5F, 0.5F, 0.5F, 1.0F)
        .endVertex();
         buffer.pos(x + (double)offset, y, z + (double)1.0F).tex(0.0F, 0.0F).color(0.5F, 0.5F, 0.5F, 1.0F)
        .endVertex();
         buffer.pos(x + (double)offset, y, z).tex(0.0F, 1.0F).color(0.5F, 0.5F, 0.5F, 1.0F)
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

   public void drawPlaneXNeg(double x, double y, double z, float f, int height) {
      float px = (float)TileEntityRendererDispatcher.staticPlayerX;
      float py = (float)TileEntityRendererDispatcher.staticPlayerY;
      float pz = (float)TileEntityRendererDispatcher.staticPlayerZ;
      GlStateManager.disableLighting();
      Random random = new Random(31100L);
      float offset = 0.01F;
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
            buffer.pos(x + (double)offset, y + (double)height, z + (double)1.0F);
            buffer.pos(x + (double)offset, y + (double)height, z).endVertex();
            tessellator.draw();
            GlStateManager.popMatrix();
            GL11.glMatrixMode(5888);
         }
      } else {
         GlStateManager.pushMatrix();
         UtilsFX.bindTexture(this.t3);
         Tessellator tessellator = Tessellator.getInstance();
         BufferBuilder buffer = tessellator.getBuffer();
         buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR); 
        
        
         buffer.pos(x + (double)offset, y, z).tex(1.0F, 1.0F).color(0.5F, 0.5F, 0.5F, 1.0F)
        .endVertex();
         buffer.pos(x + (double)offset, y, z + (double)1.0F).tex(1.0F, 0.0F).color(0.5F, 0.5F, 0.5F, 1.0F)
        .endVertex();
         buffer.pos(x + (double)offset, y + (double)height, z + (double)1.0F).tex(0.0F, 0.0F).color(0.5F, 0.5F, 0.5F, 1.0F)
        .endVertex();
         buffer.pos(x + (double)offset, y + (double)height, z).tex(0.0F, 1.0F).color(0.5F, 0.5F, 0.5F, 1.0F)
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

   public void renderSide(int h) {
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder buffer = tessellator.getBuffer();
      GlStateManager.enableRescaleNormal();
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(770, 771);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR); 
     
      buffer.pos(-0.5F, h, 0.0F).tex(0.0F, 1.0F).color(0.5F, 0.5F, 0.5F, 1.0F)
        .endVertex();
      buffer.pos(0.5F, h, 0.0F).tex(1.0F, 1.0F).color(0.5F, 0.5F, 0.5F, 1.0F)
        .endVertex();
      buffer.pos(0.5F, 0.0F, 0.0F).tex(1.0F, 0.0F).color(0.5F, 0.5F, 0.5F, 1.0F)
        .endVertex();
      buffer.pos(-0.5F, 0.0F, 0.0F).tex(0.0F, 0.0F).color(0.5F, 0.5F, 0.5F, 1.0F)
        .endVertex();
      tessellator.draw();
      GlStateManager.disableBlend();
      GlStateManager.disableRescaleNormal();
   }
}
