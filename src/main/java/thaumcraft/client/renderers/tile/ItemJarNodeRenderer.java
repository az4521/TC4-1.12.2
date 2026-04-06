package thaumcraft.client.renderers.tile;

import net.minecraft.client.Minecraft;
import thaumcraft.client.renderers.compat.RenderBlocks;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import thaumcraft.client.renderers.compat.IItemRenderer;
import thaumcraft.client.renderers.compat.IItemRenderer.ItemRenderType;
import thaumcraft.client.renderers.compat.IItemRenderer.ItemRendererHelper;

import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.blocks.ItemJarNode;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.tiles.TileJar;
import thaumcraft.common.tiles.TileJarNode;
import net.minecraft.client.renderer.GlStateManager;

public class ItemJarNodeRenderer implements IItemRenderer {
   RenderBlocks rb = new RenderBlocks();

   public boolean handleRenderType(ItemStack item, IItemRenderer.ItemRenderType type) {
      return item != null && item.getItem() == ConfigItems.itemJarNode;
   }

   public boolean shouldUseRenderHelper(IItemRenderer.ItemRenderType type, ItemStack item, IItemRenderer.ItemRendererHelper helper) {
      return helper != ItemRendererHelper.EQUIPPED_BLOCK;
   }

   public void renderItem(IItemRenderer.ItemRenderType type, ItemStack item, Object... data) {
      if (item.getItem() == ConfigItems.itemJarNode) {
         if (type == ItemRenderType.ENTITY) {
            GlStateManager.translate(-0.5F, -0.25F, -0.5F);
         } else if (type == ItemRenderType.EQUIPPED && data[1] instanceof EntityPlayer) {
            GlStateManager.translate(0.0F, 0.0F, -0.5F);
         }

         TileJarNode tjf = new TileJarNode();
         if (item.hasTagCompound()) {
            AspectList aspects = ((ItemJarNode)item.getItem()).getAspects(item);
            if (aspects != null) {
               tjf.setAspects(aspects);
               tjf.setNodeType(((ItemJarNode)item.getItem()).getNodeType(item));
               tjf.setNodeModifier(((ItemJarNode)item.getItem()).getNodeModifier(item));
            }
         }

         GlStateManager.pushMatrix();
         TileEntityRendererDispatcher.instance.render(new TileJar(), 0.0D, 0.0D, 0.0D, 0.0F);
         GlStateManager.popMatrix();
         GlStateManager.pushMatrix();
         GlStateManager.translate(0.5F, 0.4, 0.5F);
         ItemNodeRenderer.renderItemNode(tjf);
         GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
         ItemNodeRenderer.renderItemNode(tjf);
         GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
         ItemNodeRenderer.renderItemNode(tjf);
         GlStateManager.popMatrix();
         GlStateManager.pushMatrix();
         GlStateManager.translate(0.5F, 0.5F, 0.5F);
         GlStateManager.enableBlend();
         GlStateManager.blendFunc(770, 771);
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
         GlStateManager.popMatrix();
         GlStateManager.enableRescaleNormal();
      }

   }
}
