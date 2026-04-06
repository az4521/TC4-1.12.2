package thaumcraft.client.renderers.entity;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.ResourceLocation;

import thaumcraft.client.renderers.models.entities.ModelTaintSpore;
import thaumcraft.common.entities.monster.EntityTaintSpore;
import net.minecraft.client.renderer.GlStateManager;

import net.minecraft.client.renderer.entity.RenderManager;
@SideOnly(Side.CLIENT)
public class RenderTaintSpore extends RenderLiving {
   private static final ResourceLocation rl = new ResourceLocation("thaumcraft", "textures/models/taint_spore.png");

   public RenderTaintSpore(RenderManager renderManager) {
      super(renderManager, new ModelTaintSpore(), 0.25F);
   }

   protected ResourceLocation getEntityTexture(Entity entity) {
      return rl;
   }

   protected void preRenderCallback(EntityLivingBase par1EntityLiving, float par2) {
      this.scaleSpore((EntityTaintSpore)par1EntityLiving, par2);
   }

   protected void scaleSpore(EntityTaintSpore spore, float par2) {
      float f1 = spore.displaySize;
      if (spore.displaySize < (float)spore.getSporeSize()) {
         f1 += 0.02F * par2;
      }

      float f3 = -0.12F;
      float pulse = 0.025F * MathHelper.sin((float)spore.ticksExisted * 0.075F);
      GlStateManager.scale(f3 * f1 - pulse, f3 * f1 + pulse, f3 * f1 - pulse);
   }

   protected float getDeathMaxRotation(EntityLivingBase par1EntityLiving) {
      return 0.0F;
   }
}
