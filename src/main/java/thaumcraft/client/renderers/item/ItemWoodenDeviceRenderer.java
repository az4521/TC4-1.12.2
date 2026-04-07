package thaumcraft.client.renderers.item;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.client.renderers.block.BlockWoodenDeviceRenderer;
import thaumcraft.client.renderers.compat.IItemRenderer;
import thaumcraft.client.renderers.compat.RenderBlocks;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.tiles.TileBanner;

public class ItemWoodenDeviceRenderer implements IItemRenderer {
   private final BlockWoodenDeviceRenderer blockRenderer = new BlockWoodenDeviceRenderer();

   @Override
   public boolean handleRenderType(ItemStack item, ItemRenderType type) {
      return true;
   }

   @Override
   public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
      return true;
   }

   @Override
   public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
      int meta = item.getItemDamage();
      if (meta == 8) {
         GlStateManager.pushMatrix();
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         if (type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON) {
            GlStateManager.translate(1.0F, 1.0F, 1.0F);
            GlStateManager.translate(-0.15F, 0.0F, -0.15F);
            GlStateManager.scale(0.75F, 0.75F, 0.75F);
         } else {
            GlStateManager.translate(0.0F, 0.3F, 0.0F);
            GlStateManager.scale(0.5F, 0.5F, 0.5F);
         }

         GlStateManager.translate(-0.5F, -0.5F, -0.5F);
         GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
         GlStateManager.translate(-0.5F, -1.0F, -0.5F);
         TileBanner tb = new TileBanner();
         if (item.hasTagCompound()) {
            if (item.getTagCompound().hasKey("color")) {
               tb.setColor(item.getTagCompound().getByte("color"));
            }
            String aspectTag = item.getTagCompound().getString("aspect");
            if (!aspectTag.isEmpty()) {
               tb.setAspect(Aspect.getAspect(aspectTag));
            }
         }

         TileEntityRendererDispatcher.instance.render(tb, 0.0D, 0.0D, 0.0D, 0.0F);
         GlStateManager.popMatrix();
         return;
      }

      this.blockRenderer.renderInventoryBlock(ConfigBlocks.blockWoodenDevice, meta, ConfigBlocks.blockWoodenDeviceRI, new RenderBlocks());
   }
}
