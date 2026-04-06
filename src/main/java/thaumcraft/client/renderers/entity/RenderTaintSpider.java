package thaumcraft.client.renderers.entity;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelSpider;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.util.ResourceLocation;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.entities.monster.EntityTaintSpider;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;

@SideOnly(Side.CLIENT)
public class RenderTaintSpider extends RenderLiving<EntityTaintSpider> {
   private static final ResourceLocation rl = new ResourceLocation("thaumcraft", "textures/models/taint_spider.png");

   public RenderTaintSpider(RenderManager renderManager) {
      super(renderManager, new ModelSpider(), 0.5F);
   }

   // no @Override — specific type avoids generic erasure name clash
   protected void preRenderCallback(EntityTaintSpider entity, float par2) {
      float f1 = entity.spiderScaleAmount();
      GlStateManager.scale(f1, f1 * 1.25F, f1);
   }

   // no @Override — specific type avoids generic erasure name clash
   protected float getDeathMaxRotation(EntityTaintSpider entity) {
      return 180.0F;
   }

   @Override
   protected ResourceLocation getEntityTexture(EntityTaintSpider entity) {
      return rl;
   }

   // setSpiderEyeBrightness kept as helper; shouldRenderPass removed in 1.12.2
   protected int setSpiderEyeBrightness(EntitySpider par1EntitySpider, int par2, float par3) {
      if (par2 != 0) {
         return -1;
      } else {
         UtilsFX.bindTexture("textures/models/taint_spider_eyes.png");
         float f1 = 1.0F;
         GlStateManager.enableBlend();
         GlStateManager.disableAlpha();
         GlStateManager.blendFunc(1, 1);
         GlStateManager.depthMask(!par1EntitySpider.isInvisible());
         char c0 = '\uf0f0';
         int j = c0 % 65536;
         int k = c0 / 65536;
         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) j, (float) k);
         GlStateManager.color(1.0F, 1.0F, 1.0F, f1);
         return 1;
      }
   }
}
