package thaumcraft.client.renderers.entity;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

import net.minecraft.util.ResourceLocation;

import thaumcraft.client.renderers.models.entities.ModelTaintacle;
import thaumcraft.common.entities.monster.boss.EntityTaintacleGiant;
import net.minecraft.client.renderer.GlStateManager;

import net.minecraft.client.renderer.entity.RenderManager;
@SideOnly(Side.CLIENT)
public class RenderTaintacle extends RenderLiving {
   private static final ResourceLocation rl = new ResourceLocation("thaumcraft", "textures/models/taintacle.png");

   public RenderTaintacle(RenderManager renderManager, float shadow, int length) {
      super(renderManager, new ModelTaintacle(length), shadow);
   }

   protected ResourceLocation getEntityTexture(Entity entity) {
      return rl;
   }

   protected void preRenderCallback(EntityLivingBase par1EntityLiving, float par2) {
      if (par1EntityLiving instanceof EntityTaintacleGiant) {
         GlStateManager.scale(1.33F, 1.33F, 1.33F);
      }

      super.preRenderCallback(par1EntityLiving, par2);
   }
}
