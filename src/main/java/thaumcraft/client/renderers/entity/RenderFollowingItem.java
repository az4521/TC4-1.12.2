package thaumcraft.client.renderers.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import thaumcraft.common.entities.EntitySpecialItem;

public class RenderFollowingItem extends Render<EntitySpecialItem> {
   public RenderFollowingItem(RenderManager renderManager) {
      super(renderManager);
      this.shadowSize = 0.15F;
      this.shadowOpaque = 0.75F;
   }

   public void doRenderItem(EntitySpecialItem par1EntityItem, double par2, double par4, double par6, float par8, float pticks) {
      ItemStack stack = par1EntityItem.getItem();
      if (!stack.isEmpty()) {
         GlStateManager.pushMatrix();
         GlStateManager.translate((float)par2, (float)par4, (float)par6);
         Minecraft.getMinecraft().getRenderItem().renderItem(stack,
            net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType.GROUND);
         GlStateManager.popMatrix();
      }
   }

   @Override
   public void doRender(EntitySpecialItem entity, double par2, double par4, double par6, float par8, float par9) {
      this.doRenderItem(entity, par2, par4, par6, par8, par9);
   }

   @Override
   protected ResourceLocation getEntityTexture(EntitySpecialItem entity) {
      return new ResourceLocation("textures/entity/steve.png");
   }
}
