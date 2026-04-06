package thaumcraft.client.renderers.entity;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

import thaumcraft.client.renderers.models.entities.ModelEldritchCrab;
import net.minecraft.client.renderer.GlStateManager;

import net.minecraft.client.renderer.entity.RenderManager;
@SideOnly(Side.CLIENT)
public class RenderEldritchCrab extends RenderLiving {
   private static final ResourceLocation[] skin = new ResourceLocation[]{new ResourceLocation("thaumcraft", "textures/models/crab.png"), new ResourceLocation("thaumcraft", "textures/models/craboverlay.png")};

   public RenderEldritchCrab(RenderManager renderManager) {
      super(renderManager, new ModelEldritchCrab(), 1.0F);
   }

   protected ResourceLocation getEntityTexture(Entity entity) {
      return skin[0];
   }

   public void renderCrab(EntityLiving crab, double par2, double par4, double par6, float par8, float par9) {
      super.doRender(crab, par2, par4, par6, par8, par9);
   }

   protected int shouldRenderPass(EntityLivingBase par1EntityLiving, int par2, float par3) {
      if (par2 != 0) {
         return -1;
      } else {
         this.bindTexture(skin[1]);
         GlStateManager.enableBlend();
         GlStateManager.blendFunc(770, 771);
          GlStateManager.depthMask(!par1EntityLiving.isInvisible());

         char c0 = 200;
         int j = c0 % 65536;
         int k = c0 / 65536;
         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) j, (float) k);
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         return 1;
      }
   }

   public void doRender(Entity par1Entity, double par2, double par4, double par6, float par8, float par9) {
      this.renderCrab((EntityLiving)par1Entity, par2, par4, par6, par8, par9);
   }
}
