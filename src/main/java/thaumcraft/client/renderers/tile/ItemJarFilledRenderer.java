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

import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.blocks.ItemJarFilled;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.tiles.TileJarFillable;
import thaumcraft.common.tiles.TileJarFillableVoid;
import net.minecraft.client.renderer.GlStateManager;

public class ItemJarFilledRenderer implements IItemRenderer {
   RenderBlocks rb = new RenderBlocks();

   public boolean handleRenderType(ItemStack item, IItemRenderer.ItemRenderType type) {
      return item != null && item.getItem() == ConfigItems.itemJarFilled;
   }

   public boolean shouldUseRenderHelper(IItemRenderer.ItemRenderType type, ItemStack item, IItemRenderer.ItemRendererHelper helper) {
      return helper != ItemRendererHelper.EQUIPPED_BLOCK;
   }

   public void renderItem(IItemRenderer.ItemRenderType type, ItemStack item, Object... data) {
      if (item.getItem() == ConfigItems.itemJarFilled) {
         if (type == ItemRenderType.ENTITY) {
            GlStateManager.translate(-0.5F, -0.25F, -0.5F);
         } else if (type == ItemRenderType.EQUIPPED && data[1] instanceof EntityPlayer) {
            GlStateManager.translate(0.0F, 0.0F, -0.5F);
         }

         TileJarFillable tjf = new TileJarFillable();
         if (item.hasTagCompound()) {
            if (item.getItemDamage() == 3) {
               tjf = new TileJarFillableVoid();
            }

            AspectList aspects = ((ItemJarFilled)item.getItem()).getAspects(item);
            if (aspects != null && aspects.size() == 1) {
               tjf.amount = aspects.getAmount(aspects.getAspects()[0]);
               tjf.aspect = aspects.getAspects()[0];
            }

            String tf = item.getTagCompound().getString("AspectFilter");
            if (tf != null) {
               tjf.aspectFilter = Aspect.getAspect(tf);
            }
         }

         tjf.facing = 5;
         TileEntityRendererDispatcher.instance.render(tjf, 0.0D, 0.0D, 0.0D, 0.0F);
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
