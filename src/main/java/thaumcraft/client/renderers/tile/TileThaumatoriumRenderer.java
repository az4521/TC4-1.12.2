package thaumcraft.client.renderers.tile;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import thaumcraft.client.renderers.models.AdvancedModelLoader;
import thaumcraft.client.renderers.models.IModelCustom;

import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.crafting.CrucibleRecipe;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.tiles.TileThaumatorium;
import net.minecraft.client.renderer.GlStateManager;

@SideOnly(Side.CLIENT)
public class TileThaumatoriumRenderer extends TileEntitySpecialRenderer<TileThaumatorium> {
   private IModelCustom model;
   private static final ResourceLocation TM = new ResourceLocation("thaumcraft", "textures/models/thaumatorium.obj");
   EntityItem entityitem = null;

   public TileThaumatoriumRenderer() {
      this.model = AdvancedModelLoader.loadModel(TM);
   }

   @Override
   public void render(TileThaumatorium tile, double par2, double par4, double par6, float par8, int destroyStage, float alpha) {
      GlStateManager.pushMatrix();
      GlStateManager.translate((float)par2 + 0.5F, (float)par4, (float)par6 + 0.5F);
      GlStateManager.rotate(90.0F, -1.0F, 0.0F, 0.0F);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      UtilsFX.bindTexture("textures/models/thaumatorium.png");
      int md = 0;
      if (tile.getWorld() != null) {
         switch (tile.facing.ordinal()) {
            case 2:
               GlStateManager.rotate(270.0F, 0.0F, 0.0F, 1.0F);
               break;
            case 3:
               GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
            case 4:
            default:
               break;
            case 5:
               GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
         }
      }

      this.model.renderAll();
      GlStateManager.popMatrix();
      float ticks = (float)Minecraft.getMinecraft().getRenderViewEntity().ticksExisted + par8;
      if (tile != null && tile.getWorld() != null && tile.recipeHash != null && !tile.recipeHash.isEmpty()) {
         int stack = Minecraft.getMinecraft().getRenderViewEntity().ticksExisted / 40 % tile.recipeHash.size();
         CrucibleRecipe recipe = ThaumcraftApi.getCrucibleRecipeFromHash(tile.recipeHash.get(stack));
         if (recipe != null) {
            GlStateManager.pushMatrix();
            GlStateManager.translate((float)par2 + 0.5F + (float)tile.facing.getXOffset() / 1.99F, (float)par4 + 1.325F, (float)par6 + 0.5F + (float)tile.facing.getZOffset() / 1.99F);
            switch (tile.facing.ordinal()) {
               case 2:
                  GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
               case 3:
               default:
                  break;
               case 4:
                  GlStateManager.rotate(270.0F, 0.0F, 1.0F, 0.0F);
                  break;
               case 5:
                  GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
            }

            GlStateManager.scale(0.75F, 0.75F, 0.75F);
            ItemStack is = recipe.getRecipeOutput().copy();
            is.setCount(1);
            this.entityitem = new EntityItem(tile.getWorld(), 0.0F, 0.0F, 0.0F, is);
            this.entityitem.hoverStart = 0.0F;
            Minecraft.getMinecraft().getRenderManager().renderEntity(this.entityitem, 0.0D, 0.0D, 0.0D, 0.0F, par8, false);
            GlStateManager.popMatrix();
         }
      }

   }

}
