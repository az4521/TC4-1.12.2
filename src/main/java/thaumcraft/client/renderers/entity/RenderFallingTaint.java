package thaumcraft.client.renderers.entity;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import thaumcraft.common.entities.EntityFallingTaint;
import net.minecraft.client.renderer.GlStateManager;

@SideOnly(Side.CLIENT)
public class RenderFallingTaint extends Render<EntityFallingTaint> {

   public RenderFallingTaint(RenderManager renderManager) {
      super(renderManager);
      this.shadowSize = 0.5F;
   }

   public void doRenderFalling(EntityFallingTaint entity, double par2, double par4, double par6, float par8, float par9) {
      BlockPos bp = new BlockPos(MathHelper.floor(entity.posX), MathHelper.floor(entity.posY), MathHelper.floor(entity.posZ));
      if (entity.world.getBlockState(bp).getBlock() != entity.block) {
         GlStateManager.pushMatrix();
         GlStateManager.translate((float)par2, (float)par4, (float)par6);
         GlStateManager.popMatrix();
      }
   }

   @Override
   public void doRender(EntityFallingTaint par1Entity, double par2, double par4, double par6, float par8, float par9) {
      this.doRenderFalling(par1Entity, par2, par4, par6, par8, par9);
   }

   @Override
   protected ResourceLocation getEntityTexture(EntityFallingTaint entity) {
      return new ResourceLocation("textures/entity/steve.png");
   }
}
