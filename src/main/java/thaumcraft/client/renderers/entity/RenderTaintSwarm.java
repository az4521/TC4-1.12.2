package thaumcraft.client.renderers.entity;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;

import net.minecraft.client.renderer.entity.RenderManager;
@SideOnly(Side.CLIENT)
public class RenderTaintSwarm extends RenderLiving {
   public RenderTaintSwarm(RenderManager renderManager) {
      super(renderManager, null, 0.0F);
   }

   public void doRender(EntityLiving par1EntityLiving, double par2, double par4, double par6, float par8, float par9) {
   }

   public void doRender(Entity par1Entity, double par2, double par4, double par6, float par8, float par9) {
   }

   protected ResourceLocation getEntityTexture(Entity entity) {
      return new net.minecraft.util.ResourceLocation("textures/entity/steve.png");
   }
}
