package thaumcraft.client.renderers.entity;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import thaumcraft.client.renderers.models.entities.ModelWatcher;
import thaumcraft.common.entities.monster.EntityWatcher;

@SideOnly(Side.CLIENT)
public class RenderWatcher extends RenderLiving<EntityWatcher> {
   private static final ResourceLocation field_177114_e = new ResourceLocation("thaumcraft", "textures/models/watcher.png");
   private static final ResourceLocation field_177117_k = new ResourceLocation("thaumcraft", "textures/models/watcher_beam.png");
   int field_177115_a;

   public RenderWatcher(RenderManager renderManager) {
      super(renderManager, new ModelWatcher(), 0.5F);
      this.field_177115_a = ((ModelWatcher)this.mainModel).func_178706_a();
   }

   private Vec3d func_177110_a(EntityWatcher p_177110_1_, double p_177110_2_, float p_177110_4_) {
      double d1 = p_177110_1_.lastTickPosX + (p_177110_1_.posX - p_177110_1_.lastTickPosX) * (double)p_177110_4_;
      double d2 = p_177110_2_ + p_177110_1_.lastTickPosY + (p_177110_1_.posY - p_177110_1_.lastTickPosY) * (double)p_177110_4_;
      double d3 = p_177110_1_.lastTickPosZ + (p_177110_1_.posZ - p_177110_1_.lastTickPosZ) * (double)p_177110_4_;
      return new Vec3d(d1, d2, d3);
   }

   private Vec3d func_177110_b(net.minecraft.entity.EntityLivingBase p_177110_1_, double p_177110_2_, float p_177110_4_) {
      double d1 = p_177110_1_.lastTickPosX + (p_177110_1_.posX - p_177110_1_.lastTickPosX) * (double)p_177110_4_;
      double d2 = p_177110_2_ + p_177110_1_.lastTickPosY + (p_177110_1_.posY - p_177110_1_.lastTickPosY) * (double)p_177110_4_;
      double d3 = p_177110_1_.lastTickPosZ + (p_177110_1_.posZ - p_177110_1_.lastTickPosZ) * (double)p_177110_4_;
      return new Vec3d(d1, d2, d3);
   }

   @Override
   public void doRender(EntityWatcher p_177109_1_, double p_177109_2_, double p_177109_4_, double p_177109_6_, float p_177109_8_, float p_177109_9_) {
      if (this.field_177115_a != ((ModelWatcher)this.mainModel).func_178706_a()) {
         this.mainModel = new ModelWatcher();
         this.field_177115_a = ((ModelWatcher)this.mainModel).func_178706_a();
      }

      super.doRender(p_177109_1_, p_177109_2_, p_177109_4_, p_177109_6_, p_177109_8_, p_177109_9_);
      net.minecraft.entity.EntityLivingBase entitylivingbase = p_177109_1_.getTargetedEntity();
      if (entitylivingbase != null) {
         float f2 = p_177109_1_.func_175477_p(p_177109_9_);
         Tessellator tessellator = Tessellator.getInstance();
         BufferBuilder buffer = tessellator.getBuffer();
         this.bindTexture(field_177117_k);
         GL11.glTexParameterf(3553, 10242, 10497.0F);
         GL11.glTexParameterf(3553, 10243, 10497.0F);
         GlStateManager.disableLighting();
         GlStateManager.disableCull();
         GlStateManager.disableBlend();
         GlStateManager.depthMask(true);
         float f3 = 240.0F;
         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, f3, f3);
         GlStateManager.enableBlend();
         GlStateManager.blendFunc(770, 1);
         float f4 = (float)p_177109_1_.world.getTotalWorldTime() + p_177109_9_;
         float f5 = f4 * 0.5F % 1.0F;
         float f6 = p_177109_1_.getEyeHeight();
         GlStateManager.pushMatrix();
         GlStateManager.translate((float)p_177109_2_, (float)p_177109_4_ + f6, (float)p_177109_6_);
         Vec3d vec3 = this.func_177110_b(entitylivingbase, (double)entitylivingbase.height * 0.5D, p_177109_9_);
         Vec3d vec31 = this.func_177110_a(p_177109_1_, f6, p_177109_9_);
         Vec3d vec32 = vec3.subtract(vec31);
         double d3 = vec32.length() + 1.0D;
         vec32 = vec32.normalize();
         float f7 = (float)Math.acos(vec32.y);
         float f8 = (float)Math.atan2(vec32.z, vec32.x);
         GlStateManager.rotate((((float)Math.PI / 2F) - f8) * (180F / (float)Math.PI), 0.0F, 1.0F, 0.0F);
         GlStateManager.rotate(f7 * (180F / (float)Math.PI), 1.0F, 0.0F, 0.0F);
         byte b0 = 1;
         double d4 = (double)f4 * 0.05D * (1.0D - (double)(b0 & 1) * 2.5D);
         float f9 = f2 * f2;
         float cr = (64 + (int)(f9 * 240.0F)) / 255.0F;
         float cg = (32 + (int)(f9 * 192.0F)) / 255.0F;
         float cb = (128 - (int)(f9 * 64.0F)) / 255.0F;
         float ca = 1.0F;
         double d5 = (double)b0 * 0.2D;
         double d6 = d5 * 1.41D;
         double d7 = Math.cos(d4 + 2.356194490192345D) * d6;
         double d8 = Math.sin(d4 + 2.356194490192345D) * d6;
         double d9 = Math.cos(d4 + (Math.PI / 4D)) * d6;
         double d10 = Math.sin(d4 + (Math.PI / 4D)) * d6;
         double d11 = Math.cos(d4 + 3.9269908169872414D) * d6;
         double d12 = Math.sin(d4 + 3.9269908169872414D) * d6;
         double d13 = Math.cos(d4 + 5.497787143782138D) * d6;
         double d14 = Math.sin(d4 + 5.497787143782138D) * d6;
         double d15 = Math.cos(d4 + Math.PI) * d5;
         double d16 = Math.sin(d4 + Math.PI) * d5;
         double d17 = Math.cos(d4) * d5;
         double d18 = Math.sin(d4) * d5;
         double d19 = Math.cos(d4 + (Math.PI / 2D)) * d5;
         double d20 = Math.sin(d4 + (Math.PI / 2D)) * d5;
         double d21 = Math.cos(d4 + (Math.PI * 1.5D)) * d5;
         double d22 = Math.sin(d4 + (Math.PI * 1.5D)) * d5;
         double d23 = 0.0D;
         double d24 = 0.4999D;
         double d25 = -1.0D + f5;
         double d26 = d3 * (0.5D / d5) + d25;
         buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
         buffer.pos(d15, d3, d16).tex(d24, d26).color(cr, cg, cb, ca).endVertex();
         buffer.pos(d15, 0.0D, d16).tex(d24, d25).color(cr, cg, cb, ca).endVertex();
         buffer.pos(d17, 0.0D, d18).tex(d23, d25).color(cr, cg, cb, ca).endVertex();
         buffer.pos(d17, d3, d18).tex(d23, d26).color(cr, cg, cb, ca).endVertex();
         buffer.pos(d19, d3, d20).tex(d24, d26).color(cr, cg, cb, ca).endVertex();
         buffer.pos(d19, 0.0D, d20).tex(d24, d25).color(cr, cg, cb, ca).endVertex();
         buffer.pos(d21, 0.0D, d22).tex(d23, d25).color(cr, cg, cb, ca).endVertex();
         buffer.pos(d21, d3, d22).tex(d23, d26).color(cr, cg, cb, ca).endVertex();
         double d27 = 0.0D;
         if (p_177109_1_.ticksExisted % 2 == 0) {
            d27 = 0.5D;
         }
         buffer.pos(d7, d3, d8).tex(0.5D, d27 + 0.5D).color(cr, cg, cb, ca).endVertex();
         buffer.pos(d9, d3, d10).tex(1.0D, d27 + 0.5D).color(cr, cg, cb, ca).endVertex();
         buffer.pos(d13, d3, d14).tex(1.0D, d27).color(cr, cg, cb, ca).endVertex();
         buffer.pos(d11, d3, d12).tex(0.5D, d27).color(cr, cg, cb, ca).endVertex();
         tessellator.draw();
         GlStateManager.popMatrix();
      }
   }

   @Override
   protected ResourceLocation getEntityTexture(EntityWatcher entity) {
      return field_177114_e;
   }
}
