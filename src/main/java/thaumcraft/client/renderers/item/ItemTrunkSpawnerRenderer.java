package thaumcraft.client.renderers.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelChest;
import net.minecraft.item.ItemStack;
import thaumcraft.client.renderers.compat.IItemRenderer;
import thaumcraft.client.renderers.compat.IItemRenderer.ItemRenderType;

import thaumcraft.client.lib.UtilsFX;
import net.minecraft.client.renderer.GlStateManager;

public class ItemTrunkSpawnerRenderer implements IItemRenderer {
   private ModelChest chest = new ModelChest();

   public boolean handleRenderType(ItemStack item, IItemRenderer.ItemRenderType type) {
      return true;
   }

   public boolean shouldUseRenderHelper(IItemRenderer.ItemRenderType type, ItemStack item, IItemRenderer.ItemRendererHelper helper) {
      return true;
   }

   public void renderItem(IItemRenderer.ItemRenderType type, ItemStack item, Object... data) {
      Minecraft mc = Minecraft.getMinecraft();
      GlStateManager.pushMatrix();
      UtilsFX.bindTexture("textures/models/trunk.png");
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.scale(1.0F, -1.0F, -1.0F);
      short var11 = 0;
      if (type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON) {
         GlStateManager.translate(-0.25F, -0.5F, -0.25F);
         if (type == ItemRenderType.EQUIPPED && type != ItemRenderType.EQUIPPED_FIRST_PERSON) {
            GlStateManager.translate(1.0F, 0.0F, 0.0F);
         }
      }

      GlStateManager.rotate(var11, 0.0F, 1.0F, 0.0F);
      GlStateManager.translate(-0.5F, -0.5F, -0.5F);
      this.chest.renderAll();
      GlStateManager.popMatrix();
   }
}
