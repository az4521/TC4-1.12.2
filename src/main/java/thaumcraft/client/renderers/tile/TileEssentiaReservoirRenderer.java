package thaumcraft.client.renderers.tile;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.client.renderers.compat.RenderBlocks;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import thaumcraft.client.renderers.models.AdvancedModelLoader;
import thaumcraft.client.renderers.models.IModelCustom;
import org.lwjgl.opengl.GL11;

import thaumcraft.client.lib.UtilsFX;
import thaumcraft.client.renderers.block.BlockRenderer;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.tiles.TileEssentiaReservoir;
import net.minecraft.client.renderer.GlStateManager;

@SideOnly(Side.CLIENT)
public class TileEssentiaReservoirRenderer extends TileEntitySpecialRenderer<TileEssentiaReservoir> {
   private IModelCustom model;
   private static final ResourceLocation RELAY = new ResourceLocation("thaumcraft", "textures/models/reservoir.obj");

   public TileEssentiaReservoirRenderer() {
      this.model = AdvancedModelLoader.loadModel(RELAY);
   }

   @Override
   public void render(TileEssentiaReservoir tile, double par2, double par4, double par6, float par8, int destroyStage, float alpha) {
      int facing = tile.facing.ordinal();
      GlStateManager.pushMatrix();
      this.translateFromOrientation(par2, par4, par6, facing);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      UtilsFX.bindTexture("textures/models/reservoir.png");
      this.model.renderAll();
      GlStateManager.popMatrix();
      GlStateManager.pushMatrix();
      GlStateManager.translate(par2, par4 - (double)0.5F, par6);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      this.renderLiquid(tile, par2, par4, par6, par8);
      GlStateManager.popMatrix();
   }

   public void renderLiquid(TileEssentiaReservoir te, double x, double y, double z, float f) {
      if (this.rendererDispatcher.renderEngine != null && te.displayAspect != null && te.essentia.visSize() != 0) {
         GlStateManager.pushMatrix();
         GlStateManager.enableBlend();
         GlStateManager.blendFunc(770, 771);
         GlStateManager.disableLighting();
         GlStateManager.color(te.cr, te.cg, te.cb, 0.9F);
         World world = te.getWorld();
         RenderBlocks renderBlocks = new RenderBlocks();
         float level = (float)te.essentia.visSize() / (float)te.maxAmount;
         renderBlocks.setRenderBounds(BlockRenderer.W3, BlockRenderer.W3, BlockRenderer.W3, BlockRenderer.W13, BlockRenderer.W3 + BlockRenderer.W10 * level, BlockRenderer.W13);
         TextureAtlasSprite icon = net.minecraft.client.Minecraft.getMinecraft()
               .getTextureMapBlocks()
               .getAtlasSprite("thaumcraft:blocks/animatedglow");
         this.rendererDispatcher.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
         renderBlocks.renderFaceYNeg(ConfigBlocks.blockEssentiaReservoir, 0.0F, 0.5F, 0.0F, icon);
         renderBlocks.renderFaceYPos(ConfigBlocks.blockEssentiaReservoir, 0.0F, 0.5F, 0.0F, icon);
         renderBlocks.renderFaceZNeg(ConfigBlocks.blockEssentiaReservoir, 0.0F, 0.5F, 0.0F, icon);
         renderBlocks.renderFaceZPos(ConfigBlocks.blockEssentiaReservoir, 0.0F, 0.5F, 0.0F, icon);
         renderBlocks.renderFaceXNeg(ConfigBlocks.blockEssentiaReservoir, 0.0F, 0.5F, 0.0F, icon);
         renderBlocks.renderFaceXPos(ConfigBlocks.blockEssentiaReservoir, 0.0F, 0.5F, 0.0F, icon);
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.enableLighting();
         GlStateManager.disableBlend();
         GlStateManager.popMatrix();
      }
   }

   private void translateFromOrientation(double x, double y, double z, int orientation) {
      GlStateManager.translate(x + (double)0.5F, y + (double)0.5F, z + (double)0.5F);
      if (orientation == 0) {
         GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
      } else if (orientation == 1) {
         GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
      } else if (orientation != 2) {
         if (orientation == 3) {
            GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
         } else if (orientation == 4) {
            GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
         } else if (orientation == 5) {
            GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
         }
      }

      GlStateManager.translate(0.0F, 0.0F, -0.5F);
   }

}
