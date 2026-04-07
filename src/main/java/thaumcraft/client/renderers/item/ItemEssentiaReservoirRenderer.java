package thaumcraft.client.renderers.item;

import net.minecraft.item.ItemStack;
import thaumcraft.client.renderers.block.BlockEssentiaReservoirRenderer;
import thaumcraft.client.renderers.compat.IItemRenderer;
import thaumcraft.client.renderers.compat.RenderBlocks;
import thaumcraft.common.config.ConfigBlocks;

public class ItemEssentiaReservoirRenderer implements IItemRenderer {
   private final BlockEssentiaReservoirRenderer renderer = new BlockEssentiaReservoirRenderer();
   private final RenderBlocks renderBlocks = new RenderBlocks();

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
      if (type == ItemRenderType.INVENTORY || type == ItemRenderType.ENTITY) {
         org.lwjgl.opengl.GL11.glPushMatrix();
         org.lwjgl.opengl.GL11.glRotatef(30.0F, 1.0F, 0.0F, 0.0F);
         org.lwjgl.opengl.GL11.glRotatef(225.0F, 0.0F, 1.0F, 0.0F);
         org.lwjgl.opengl.GL11.glScalef(0.625F, 0.625F, 0.625F);
         this.renderer.renderInventoryBlock(ConfigBlocks.blockEssentiaReservoir, item.getItemDamage(), ConfigBlocks.blockEssentiaReservoirRI, this.renderBlocks);
         org.lwjgl.opengl.GL11.glPopMatrix();
      } else {
         this.renderer.renderInventoryBlock(ConfigBlocks.blockEssentiaReservoir, item.getItemDamage(), ConfigBlocks.blockEssentiaReservoirRI, this.renderBlocks);
      }
   }
}
