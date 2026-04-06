package thaumcraft.client.renderers.tile;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import thaumcraft.api.IScribeTools;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.client.renderers.models.ModelResearchTable;
import thaumcraft.common.blocks.BlockTable;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.lib.research.ResearchManager;
import thaumcraft.common.lib.research.ResearchNoteData;
import thaumcraft.common.tiles.TileResearchTable;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

@SideOnly(Side.CLIENT)
public class TileResearchTableRenderer extends TileEntitySpecialRenderer<TileResearchTable> {
   private ModelResearchTable tableModel = new ModelResearchTable();

   @Override
   public void render(TileResearchTable table, double par2, double par4, double par6, float par8, int destroyStage, float alpha) {
      int md = 0;
      if (table.getWorld() != null) {
         md = table.getWorld().getBlockState(table.getPos()).getBlock().getMetaFromState(table.getWorld().getBlockState(table.getPos()));
      }

      GlStateManager.pushMatrix();
      UtilsFX.bindTexture("textures/models/restable.png");
      GlStateManager.translate((float)par2 + 0.5F, (float)par4 + 1.0F, (float)par6 + 0.5F);
      GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
      switch (md) {
         case 2:
            GlStateManager.rotate(270.0F, 0.0F, 1.0F, 0.0F);
            break;
         case 3:
            GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
            break;
         case 4:
            GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
      }

      this.tableModel.renderAll();
      if (!table.getStackInSlot(0).isEmpty() && table.getStackInSlot(0).getItem() instanceof IScribeTools) {
         this.tableModel.renderInkwell();
         GlStateManager.pushMatrix();
         GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
         GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
         GlStateManager.translate(-0.17F, 0.1F, -0.15F);
         GlStateManager.rotate(15.0F, 0.0F, 1.0F, 0.0F);
         GlStateManager.scale(0.5F, 0.5F, 0.5F);
         UtilsFX.bindTexture("textures/blocks/tablequill.png");
         Tessellator tessellator = Tessellator.getInstance();
         BufferBuilder buffer = tessellator.getBuffer();
         buffer.begin(org.lwjgl.opengl.GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
         buffer.pos(0, 1, 0).tex(1, 0).endVertex();
         buffer.pos(1, 1, 0).tex(0, 0).endVertex();
         buffer.pos(1, 0, 0).tex(0, 1).endVertex();
         buffer.pos(0, 0, 0).tex(1, 1).endVertex();
         tessellator.draw();
         GlStateManager.popMatrix();
      }

      for(int a = 0; a < 6; ++a) {
         GlStateManager.pushMatrix();
         GlStateManager.translate(0.1F, -0.01F - (float)a * 0.015F, 0.35F);
         GlStateManager.rotate(90.0F, -1.0F, 0.0F, 0.0F);
         GlStateManager.rotate((float)(15 + a % 3 * 2), 0.0F, 0.0F, 1.0F);
         GlStateManager.scale(0.5F, 0.6F, 0.6F);
         UtilsFX.renderQuad("textures/misc/parchment.png", 771, 1.0F);
         GlStateManager.popMatrix();
      }

      if (!table.getStackInSlot(1).isEmpty() && table.getStackInSlot(1).getItem() == ConfigItems.itemResearchNotes) {
         UtilsFX.bindTexture("textures/models/restable2.png");
         ResearchNoteData rd = ResearchManager.getData(table.getStackInSlot(1));
         int color = 10066329;
         if (rd != null) {
            color = rd.color;
         }

         this.tableModel.renderScroll(color);
      }

      GlStateManager.popMatrix();
   }

}
