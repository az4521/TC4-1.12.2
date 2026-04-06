package thaumcraft.client.renderers.tile;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import thaumcraft.client.lib.UtilsFX;
import thaumcraft.client.renderers.models.ModelCube;
import thaumcraft.common.blocks.BlockCustomPlant;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.tiles.TileEtherealBloom;

import static thaumcraft.client.renderers.tile.TileNodeRenderer.renderFacingStrip_tweaked;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

@SideOnly(Side.CLIENT)
public class TileEtherealBloomRenderer extends TileEntitySpecialRenderer<TileEntity> {
   String tx2 = "textures/models/crystalcapacitor.png";
   BlockCustomPlant block;
   private ModelCube model = new ModelCube();

   public TileEtherealBloomRenderer() {
      this.block = (BlockCustomPlant)ConfigBlocks.blockCustomPlant;
   }

   @Override


   public void render(TileEntity tile, double x, double y, double z, float par8, int destroyStage, float alpha) {
      float rc1 = (float)((TileEtherealBloom)tile).growthCounter + par8;
      float rc2 = rc1;
      float rc3 = rc1 - 33.0F;
      float rc4 = rc1 - 66.0F;
      if (rc1 > 100.0F) {
         rc1 = 100.0F;
      }

      if (rc2 > 50.0F) {
         rc2 = 50.0F;
      }

      if (rc3 < 0.0F) {
         rc3 = 0.0F;
      }

      if (rc3 > 33.0F) {
         rc3 = 33.0F;
      }

      if (rc4 < 0.0F) {
         rc4 = 0.0F;
      }

      if (rc4 > 33.0F) {
         rc4 = 33.0F;
      }

      float scale1 = rc1 / 100.0F;
      float scale2 = rc2 / 60.0F + 0.1666666F;
      float scale3 = rc3 / 33.0F;
      float scale4 = rc4 / 33.0F * 0.7F;
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder buffer = tessellator.getBuffer();
      GlStateManager.pushMatrix();
      GlStateManager.alphaFunc(516, 0.003921569F);
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(770, 1);
      GlStateManager.pushMatrix();
      GlStateManager.depthMask(false);
      GlStateManager.disableCull();
      int i = ((TileEtherealBloom)tile).counter % 32;
      UtilsFX.bindTexture(TileNodeRenderer.nodetex);
      //            UtilsFX.renderFacingStrip
      renderFacingStrip_tweaked
              ((double)tile.getPos().getX() + 0.5, (double)tile.getPos().getY() + scale1, (double)tile.getPos().getZ() + 0.5, 0.0F, scale1, 1.0F, 32, 6, i, par8, 11197951);
      GlStateManager.enableCull();
      GlStateManager.depthMask(true);
      GlStateManager.popMatrix();
      GlStateManager.pushMatrix();
      GlStateManager.translate(x + (double)0.5F - (double)(scale4 / 8.0F), y + (double)scale1 - (double)(scale4 / 6.0F), z + (double)0.5F - (double)(scale4 / 8.0F));
      GlStateManager.scale(scale4 / 4.0F, scale4 / 3.0F, scale4 / 4.0F);
      UtilsFX.bindTexture(this.tx2);
      this.model.render();
      GlStateManager.popMatrix();
      GlStateManager.pushMatrix();
      GlStateManager.translate(x + (double)0.5F, y + (double)0.25F, z + (double)0.5F);
      GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);

      for(int a = 0; a < 4; ++a) {
         GlStateManager.pushMatrix();
         GlStateManager.scale(scale3, scale1, scale3);
         GlStateManager.rotate((float)(90 * a), 0.0F, 1.0F, 0.0F);
         UtilsFX.renderQuadCenteredFromIcon(true, this.block.iconLeaves, 1.0F, 1.0F, 1.0F, 1.0F, 200, 771, 1.0F);
         GlStateManager.popMatrix();
      }

      GlStateManager.popMatrix();
      GlStateManager.pushMatrix();
      GlStateManager.translate(x + (double)0.5F, y + 0.6, z + (double)0.5F);
      GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
      GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);

      for(int a = 0; a < 4; ++a) {
         GlStateManager.pushMatrix();
         GlStateManager.scale(scale4, scale1 * 0.7F, scale4);
         GlStateManager.rotate((float)(90 * a), 0.0F, 1.0F, 0.0F);
         UtilsFX.renderQuadCenteredFromIcon(true, this.block.iconLeaves, 1.0F, 1.0F, 1.0F, 1.0F, 200, 771, 1.0F);
         GlStateManager.popMatrix();
      }

      GlStateManager.popMatrix();
      GlStateManager.pushMatrix();
      GlStateManager.translate(x + (double)0.5F, y + (double)0.5F, z + (double)0.5F);
      GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);

      for(int a = 0; a < 4; ++a) {
         GlStateManager.pushMatrix();
         GlStateManager.translate(0.0F, (1.0F - scale1) / 2.0F, 0.0F);
         GlStateManager.scale(scale2, scale1, scale2);
         GlStateManager.rotate((float)(90 * a), 0.0F, 1.0F, 0.0F);
         UtilsFX.renderQuadCenteredFromIcon(true, this.block.iconStalk, 1.0F, 1.0F, 1.0F, 1.0F, 200, 771, 1.0F);
         GlStateManager.popMatrix();
      }

      GlStateManager.popMatrix();
      GlStateManager.disableBlend();
      GlStateManager.alphaFunc(516, 0.1F);
      GlStateManager.popMatrix();
   }
}
