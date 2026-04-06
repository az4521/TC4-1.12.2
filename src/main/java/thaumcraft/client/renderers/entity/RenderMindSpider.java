package thaumcraft.client.renderers.entity;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelSpider;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.util.ResourceLocation;

import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.entities.monster.EntityMindSpider;
import net.minecraft.client.renderer.GlStateManager;

import net.minecraft.client.renderer.entity.RenderManager;
@SideOnly(Side.CLIENT)
public class RenderMindSpider extends RenderLiving<EntityMindSpider> {
   private static final ResourceLocation rl = new ResourceLocation("thaumcraft", "textures/models/taint_spider.png");

   public RenderMindSpider(RenderManager renderManager) {
      super(renderManager, new ModelSpider(), 0.0F);
      // setRenderPassModel removed in 1.12.2
   }

   protected float setSpiderDeathMaxRotation(EntitySpider par1EntitySpider) {
      return 180.0F;
   }

   @Override
   protected ResourceLocation getEntityTexture(EntityMindSpider entity) {
      return rl;
   }

   @Override
   public void doRender(EntityMindSpider entity, double x, double y, double z, float entityYaw, float partialTicks) {
      net.minecraft.entity.player.EntityPlayer localPlayer = Minecraft.getMinecraft().player;
      if (entity.getViewer().isEmpty() || (localPlayer != null && entity.getViewer().equals(localPlayer.getName()))) {
         super.doRender(entity, x, y, z, entityYaw, partialTicks);
      }
   }

   // no @Override — specific type to avoid generic erasure name clash
   protected void renderModel(EntityMindSpider entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
      this.bindEntityTexture(entity);
      GlStateManager.pushMatrix();
      GlStateManager.color(1.0F, 1.0F, 1.0F, Math.min(0.1F, (float)entity.ticksExisted / 100.0F));
      GlStateManager.depthMask(false);
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(770, 771);
      GlStateManager.alphaFunc(516, 0.003921569F);
      this.mainModel.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
      GlStateManager.disableBlend();
      GlStateManager.alphaFunc(516, 0.1F);
      GlStateManager.popMatrix();
      GlStateManager.depthMask(true);
   }

   protected void scaleSpider(EntityMindSpider par1EntitySpider, float par2) {
      float f1 = par1EntitySpider.spiderScaleAmount();
      GlStateManager.scale(f1, f1, f1);
   }

   // preRenderCallback — no @Override due to generic bridge issue; still called polymorphically
   protected void preRenderCallback(EntityMindSpider entity, float partialTick) {
      this.scaleSpider(entity, partialTick);
   }

   // no @Override — specific type avoids generic erasure name clash
   protected float getDeathMaxRotation(EntityMindSpider par1EntityLiving) {
      return this.setSpiderDeathMaxRotation(par1EntityLiving);
   }

   // shouldRenderPass removed in 1.12.2; eye-glow layer should use LayerRenderer
   // setSpiderEyeBrightness kept as helper if needed later
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
