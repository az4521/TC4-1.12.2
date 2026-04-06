package thaumcraft.client.renderers.tile;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.ResourceLocation;
import thaumcraft.client.renderers.models.AdvancedModelLoader;
import thaumcraft.client.renderers.models.IModelCustom;

import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.tiles.TileNodeConverter;

import static thaumcraft.client.renderers.tile.TileBlockInfoGetter.getBlockTypeSafely;
import net.minecraft.client.renderer.GlStateManager;

@SideOnly(Side.CLIENT)
public class TileNodeConverterRenderer extends TileEntitySpecialRenderer<TileNodeConverter> {
   private IModelCustom model;
   private static final ResourceLocation MODEL = new ResourceLocation("thaumcraft", "textures/models/node_stabilizer.obj");

   public TileNodeConverterRenderer() {
      this.model = AdvancedModelLoader.loadModel(MODEL);
   }

   @Override
   public void render(TileNodeConverter tile, double par2, double par4, double par6, float par8, int destroyStage, float alpha) {
      if (tile == null){return;}
      int bright = 20;
      Block blockType = getBlockTypeSafely(tile);
      if (blockType != null
      ) {
         bright = tile.getWorld().getCombinedLight(tile.getPos(), 0);
      }

      GlStateManager.pushMatrix();
      GlStateManager.translate((float)par2 + 0.5F, (float)par4 + 1.0F, (float)par6 + 0.5F);
      UtilsFX.bindTexture("textures/models/node_converter.png");
      GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      float v = (float)Math.min(50, tile.count) / 137.0F;
      this.model.renderPart("lock");
      if (tile.status == 2) {
         GlStateManager.color(1.0F, 0.0F, 0.3F, 1.0F);
      } else if (tile.status == 1) {
         GlStateManager.color(1.0F, 0.6F, 0.1F, 1.0F);
      } else {
         GlStateManager.color(0.5F, 1.0F, 0.5F, 1.0F);
      }

      if (tile.getWorld() != null) {
         float scale = MathHelper.sin((float)Minecraft.getMinecraft().getRenderViewEntity().ticksExisted / 3.0F) * 0.1F + 0.9F;
         int j = 50 + (int)(170.0F * v * 2.5F * scale);
         int k = j % 65536;
         int l = j / 65536;
         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) k, (float) l);
      }

      UtilsFX.bindTexture("textures/models/node_converter_over.png");
      this.model.renderPart("lock");
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

      for(int a = 0; a < 4; ++a) {
         GlStateManager.pushMatrix();
         if (tile.getWorld() != null) {
            int k = bright % 65536;
            int l = bright / 65536;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) k, (float) l);
         }

         GlStateManager.rotate((float)(90 * a), 0.0F, 0.0F, 1.0F);
         GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
         GlStateManager.translate(0.0F, 0.0F, v);
         UtilsFX.bindTexture("textures/models/node_converter.png");
         this.model.renderPart("piston");
         if (tile.status == 2) {
            GlStateManager.color(1.0F, 0.0F, 0.3F, 1.0F);
         } else if (tile.status == 1) {
            GlStateManager.color(1.0F, 0.6F, 0.1F, 1.0F);
         } else {
            GlStateManager.color(0.5F, 1.0F, 0.5F, 1.0F);
         }

         if (tile.getWorld() != null) {
            float scale = MathHelper.sin((float)(Minecraft.getMinecraft().getRenderViewEntity().ticksExisted + a * 5) / 3.0F) * 0.1F + 0.9F;
            int j = 50 + (int)(170.0F * v * 2.5F * scale);
            int k = j % 65536;
            int l = j / 65536;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) k, (float) l);
         }

         UtilsFX.bindTexture("textures/models/node_converter_over.png");
         this.model.renderPart("piston");
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.popMatrix();
      }

      GlStateManager.popMatrix();
   }
}
