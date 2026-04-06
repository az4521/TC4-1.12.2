package thaumcraft.client.renderers.tile;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import thaumcraft.client.renderers.models.AdvancedModelLoader;
import thaumcraft.client.renderers.models.IModelCustom;
import org.lwjgl.opengl.GL11;

import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.tiles.TileAlchemyFurnaceAdvanced;

import static thaumcraft.client.renderers.tile.TileBlockInfoGetter.getBlockTypeSafely;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

@SideOnly(Side.CLIENT)
public class TileAlchemyFurnaceAdvancedRenderer extends TileEntitySpecialRenderer<TileAlchemyFurnaceAdvanced> {
   public static IModelCustom model = null;
   public static final ResourceLocation FURNACE = new ResourceLocation("thaumcraft", "textures/models/adv_alch_furnace.obj");

   public TileAlchemyFurnaceAdvancedRenderer() {
      if (model == null){
         model = AdvancedModelLoader.loadModel(FURNACE);
      }
   }

   @Override
   public void render(TileAlchemyFurnaceAdvanced tile, double par2, double par4, double par6, float par8, int destroyStage, float alpha) {
      if (tile == null){return;}
      Block blockType = getBlockTypeSafely(tile);
      GlStateManager.pushMatrix();
      GlStateManager.translate((float)par2 + 0.5F, (float)par4, (float)par6 + 0.5F);
      GlStateManager.rotate(90.0F, -1.0F, 0.0F, 0.0F);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      if (tile.heat <= 100) {
         UtilsFX.bindTexture("textures/models/alch_furnace.png");
      } else {
         UtilsFX.bindTexture("textures/models/alch_furnace_on.png");
      }

      model.renderPart("Base");
      if (tile.vis <= 0) {
         UtilsFX.bindTexture("textures/models/alch_furnace_tank.png");
      } else {
         UtilsFX.bindTexture("textures/models/alch_furnace_tank_on.png");
      }

      for(int a = 0; a < 4; ++a) {
         GlStateManager.pushMatrix();
         GlStateManager.rotate((float)(90 * a), 0.0F, 0.0F, 1.0F);
         model.renderPart("Tank");
         GlStateManager.popMatrix();
      }

      if (tile.vis > 0) {
         GlStateManager.pushMatrix();
         GlStateManager.translate(0.5F, -0.5F, 1.1F);
         GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
         this.renderQuadCenteredFromIcon(Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite(), 190, 0.0F);
         GlStateManager.popMatrix();
         GlStateManager.pushMatrix();
         float f = 1.0F - (float)tile.vis / (float)tile.maxVis;

         for(int a = 0; a < 4; ++a) {
            GlStateManager.pushMatrix();
            GlStateManager.pushMatrix();
            GlStateManager.rotate((float)(90 * a), 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(90.0F, -1.0F, 0.0F, 0.0F);
            GlStateManager.translate(0.85F, -1.8F, -1.4F);
            GlStateManager.scale(0.3, 0.6, 1.0F);
            if (blockType != null){
               this.renderQuadCenteredFromIcon(Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite(), 150, 0.0F);
            }
            GlStateManager.translate(0.0F, 0.0F, -0.01F);
            this.renderQuadCenteredFromIcon(Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite(), 190, f);
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            GlStateManager.rotate((float)(90 * a), 0.0F, 0.0F, -1.0F);
            GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.translate(1.15F, 1.8F, -1.4F);
            GlStateManager.scale(-0.3, -0.6, -1.0F);
            if (blockType != null){
               this.renderQuadCenteredFromIcon(Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite(), 150, 0.0F);
            }
            GlStateManager.translate(0.0F, 0.0F, 0.01F);
            this.renderQuadCenteredFromIcon(Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite(), 190, f);
            GlStateManager.popMatrix();
            GlStateManager.popMatrix();
         }

         GlStateManager.popMatrix();
      }

      if (tile.heat > 100) {
         GlStateManager.pushMatrix();
         GlStateManager.translate(0.0F, 0.0F, 1.0F);

         for(int a = 0; a < 4; ++a) {
            GlStateManager.pushMatrix();
            GlStateManager.rotate((float)(90 * a), 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(135.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.translate(-0.5F, 0.0F, -1.0F);
            GlStateManager.pushMatrix();
            this.renderQuadCenteredFromIcon(Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite(), 220, 1.0F - Math.min(1.0F, (float)tile.heat / (float)tile.maxPower));
            GlStateManager.popMatrix();
            GlStateManager.translate(0.0F, 0.0F, 0.05F);
            if (blockType != null){
               this.renderQuadCenteredFromIcon(Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite(), 150, 0.0F);
            }
            GlStateManager.popMatrix();
         }

         GlStateManager.popMatrix();
      }

      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.popMatrix();
   }

   public void renderQuadCenteredFromIcon(TextureAtlasSprite icon, int brightness, float width) {
      Minecraft.getMinecraft().getTextureManager().bindTexture(net.minecraft.client.renderer.texture.TextureMap.LOCATION_BLOCKS_TEXTURE);

      GlStateManager.disableLighting();
//      RenderHelper.disableStandardItemLighting();

      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder buffer = tessellator.getBuffer();
      float f1 = icon.getMaxU();
      float f2 = icon.getMinV();
      float f3 = icon.getMinU();
      float f4 = icon.getMaxV();
      GlStateManager.enableRescaleNormal();
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(770, 771);
      buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX); 
     
      // color set per-vertex below
      buffer.pos(0.0F, 1.0F, 0.0F).tex(f1, f4)
        .endVertex();
      buffer.pos(1.0F, 1.0F, 0.0F).tex(f3, f4)
        .endVertex();
      buffer.pos(1.0F, width, 0.0F).tex(f3, f2)
        .endVertex();
      buffer.pos(0.0F, width, 0.0F).tex(f1, f2)
        .endVertex();
      tessellator.draw();
      GlStateManager.disableBlend();
      GlStateManager.disableRescaleNormal();
      GlStateManager.enableLighting();
//      RenderHelper.enableStandardItemLighting();
   }
}
