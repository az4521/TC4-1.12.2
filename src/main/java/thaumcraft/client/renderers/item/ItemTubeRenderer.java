package thaumcraft.client.renderers.item;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import thaumcraft.client.renderers.block.BlockTubeRenderer;
import thaumcraft.client.renderers.compat.IItemRenderer;
import thaumcraft.client.renderers.compat.RenderBlocks;
import thaumcraft.common.config.ConfigBlocks;

public class ItemTubeRenderer implements IItemRenderer {
   private final BlockTubeRenderer blockRenderer = new BlockTubeRenderer();

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
      GlStateManager.pushMatrix();
      if (type == ItemRenderType.INVENTORY || type == ItemRenderType.ENTITY) {
         GlStateManager.rotate(30.0F, 1.0F, 0.0F, 0.0F);
         GlStateManager.rotate(225.0F, 0.0F, 1.0F, 0.0F);
         GlStateManager.scale(0.625F, 0.625F, 0.625F);
      } else if (type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON) {
         GlStateManager.scale(0.625F, 0.625F, 0.625F);
      }

      this.blockRenderer.renderInventoryBlock(ConfigBlocks.blockTube, item.getItemDamage(), ConfigBlocks.blockTubeRI, new RenderBlocks());
      GlStateManager.popMatrix();
   }
}
