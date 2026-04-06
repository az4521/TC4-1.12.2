package thaumcraft.client.renderers.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import thaumcraft.client.renderers.compat.IItemRenderer;
import thaumcraft.client.renderers.compat.IItemRenderer.ItemRenderType;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.tiles.TileBanner;
import net.minecraft.client.renderer.GlStateManager;

public class ItemBannerRenderer implements IItemRenderer {
   public boolean handleRenderType(ItemStack item, IItemRenderer.ItemRenderType type) {
      if (item.hasTagCompound() && item.getItemDamage() == 8) {
         return item.getTagCompound().getString("aspect") != null && item.getTagCompound().getByte("color") >= 0;
      } else {
         return false;
      }
   }

   public boolean shouldUseRenderHelper(IItemRenderer.ItemRenderType type, ItemStack item, IItemRenderer.ItemRendererHelper helper) {
      return true;
   }

   public void renderItem(IItemRenderer.ItemRenderType type, ItemStack item, Object... data) {
      Minecraft mc = Minecraft.getMinecraft();
      GlStateManager.pushMatrix();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      short var11 = 0;
      if (type != ItemRenderType.EQUIPPED && type != ItemRenderType.EQUIPPED_FIRST_PERSON) {
         GlStateManager.rotate(var11, 0.0F, 1.0F, 0.0F);
      } else {
         GlStateManager.translate(1.0F, 1.0F, 1.0F);
      }

      GlStateManager.translate(-0.5F, -0.5F, -0.5F);
      GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
      GlStateManager.translate(-0.5F, -1.0F, -0.5F);
      TileBanner tb = new TileBanner();
      tb.setColor(item.getTagCompound().getByte("color"));
      if (item.getTagCompound().getString("aspect") != null) {
         tb.setAspect(Aspect.getAspect(item.getTagCompound().getString("aspect")));
      }

      TileEntityRendererDispatcher.instance.render(tb, 0.0D, 0.0D, 0.0D, 0.0F);
      GlStateManager.popMatrix();
   }
}
