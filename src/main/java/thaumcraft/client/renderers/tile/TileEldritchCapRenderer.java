package thaumcraft.client.renderers.tile;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import thaumcraft.client.renderers.models.AdvancedModelLoader;
import thaumcraft.client.renderers.models.IModelCustom;

import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.config.Config;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.tiles.TileEldritchAltar;

import static thaumcraft.client.renderers.tile.TileBlockInfoGetter.getBlockTypeSafely;
import net.minecraft.client.renderer.GlStateManager;

public class TileEldritchCapRenderer extends TileEntitySpecialRenderer<TileEntity> {
   private IModelCustom model;
   private static final ResourceLocation CAP = new ResourceLocation("thaumcraft", "textures/models/obelisk_cap.obj");
   private String tex = "textures/models/obelisk_cap.png";
   private String tex2 = "textures/models/obelisk_cap_2.png";
   private ItemStack eye = null;
   EntityItem entityitem = null;

   public TileEldritchCapRenderer(String texture) {
      this.tex = texture;
      this.model = AdvancedModelLoader.loadModel(CAP);
   }

   public TileEldritchCapRenderer() {
      this.model = AdvancedModelLoader.loadModel(CAP);
   }

   @Override


   public void render(TileEntity te, double x, double y, double z, float f, int destroyStage, float alpha) {
      String tempTex = this.tex;
      GlStateManager.pushMatrix();
      Block blockType = getBlockTypeSafely(te);
      if (blockType != null) {
         int j = te.getWorld().getCombinedLight(te.getPos(), 0);
         int k = j & 0xFFFF;
         int l = (j >> 16) & 0xFFFF;
         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) k, (float) l);
         if (te.getWorld().provider.getDimension() == Config.dimensionOuterId) {
            tempTex = this.tex2;
         }
      }

      GlStateManager.pushMatrix();
      UtilsFX.bindTexture(tempTex);
      GlStateManager.translate(x + (double)0.5F, y, z + (double)0.5F);
      GlStateManager.rotate(90.0F, -1.0F, 0.0F, 0.0F);
      this.model.renderPart("Cap");
      GlStateManager.popMatrix();
      if (te.getWorld() != null && te instanceof TileEldritchAltar && ((TileEldritchAltar)te).getEyes() > 0) {
         GlStateManager.pushMatrix();
         GlStateManager.translate((float)x + 0.5F, (float)y + 0.0F, (float)z + 0.5F);
         if (this.entityitem == null || this.eye == null) {
            this.eye = new ItemStack(ConfigItems.itemEldritchObject, 1, 0);
            this.entityitem = new EntityItem(te.getWorld(), 0.0F, 0.0F, 0.0F, this.eye);
            this.entityitem.hoverStart = 0.0F;
         }

         if (this.eye != null) {
            for(int a = 0; a < ((TileEldritchAltar)te).getEyes(); ++a) {
               GlStateManager.pushMatrix();
               GlStateManager.rotate(a * 90, 0.0F, 1.0F, 0.0F);
               GlStateManager.translate(0.46F, 0.2F, 0.0F);
               GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
               GlStateManager.rotate(18.0F, -1.0F, 0.0F, 0.0F);
               GlStateManager.popMatrix();
            }
         }

         GlStateManager.popMatrix();
      }

      GlStateManager.popMatrix();
   }
}
