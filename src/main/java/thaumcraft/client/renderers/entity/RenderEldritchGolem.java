package thaumcraft.client.renderers.entity;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;

import net.minecraft.util.ResourceLocation;

import thaumcraft.client.renderers.models.entities.ModelEldritchGolem;
import thaumcraft.common.entities.monster.boss.EntityEldritchGolem;
import net.minecraft.client.renderer.GlStateManager;

import net.minecraft.client.renderer.entity.RenderManager;
@SideOnly(Side.CLIENT)
public class RenderEldritchGolem extends RenderLiving {
   protected ModelEldritchGolem modelMain;
   private static final ResourceLocation skin = new ResourceLocation("thaumcraft", "textures/models/eldritch_golem.png");

   public RenderEldritchGolem(RenderManager renderManager, ModelEldritchGolem par1ModelBiped, float par2) {
      super(renderManager, par1ModelBiped, par2);
      this.modelMain = par1ModelBiped;
   }

   protected ResourceLocation getEntityTexture(Entity entity) {
      return skin;
   }

   protected void preRenderCallback(EntityLivingBase par1EntityLiving, float par2) {
      GlStateManager.scale(2.15F, 2.15F, 2.15F);
   }

   public void doRenderLiving(EntityLiving golem, double par2, double par4, double par6, float par8, float par9) {
      GlStateManager.enableBlend();
      GlStateManager.alphaFunc(516, 0.003921569F);
      GlStateManager.blendFunc(770, 771);
      super.doRender(golem, par2, par4, par6, par8, par9);
      GlStateManager.disableBlend();
      GlStateManager.alphaFunc(516, 0.1F);
   }

   public void doRender(Entity par1Entity, double par2, double par4, double par6, float par8, float par9) {
      this.doRenderLiving((EntityLiving)par1Entity, par2, par4, par6, par8, par9);
   }
}
