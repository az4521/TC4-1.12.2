package thaumcraft.client.renderers.tile;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import thaumcraft.client.renderers.compat.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import thaumcraft.client.lib.UtilsFX;
import thaumcraft.client.renderers.models.ModelBrain;
import thaumcraft.client.renderers.models.ModelJar;
import thaumcraft.common.blocks.BlockJar;
import thaumcraft.common.config.Config;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.tiles.TileJar;
import thaumcraft.common.tiles.TileJarBrain;
import thaumcraft.common.tiles.TileJarFillable;
import thaumcraft.common.tiles.TileJarNode;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

@SideOnly(Side.CLIENT)
public class TileJarRenderer extends TileEntitySpecialRenderer<TileJar> {
   private ModelJar model = new ModelJar();
   private ModelBrain brain = new ModelBrain();
   private TileNodeRenderer tnr = new TileNodeRenderer();

   @Override
   public void render(TileJar tile, double x, double y, double z, float f, int destroyStage, float alpha) {
      if (tile instanceof TileJarNode) {
         GlStateManager.pushMatrix();
         GlStateManager.translate(0.0F, -0.1F, 0.0F);
         this.tnr.render(tile, x, y, z, f, destroyStage, alpha);
         GlStateManager.popMatrix();
      }

      GlStateManager.pushMatrix();
      GlStateManager.disableCull();
      GlStateManager.translate((float)x + 0.5F, (float)y + 0.01F, (float)z + 0.5F);
      GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      if (tile instanceof TileJarBrain) {
         this.renderBrain((TileJarBrain)tile, x, y, z, f);
      } else if (tile instanceof TileJarFillable) {
         if (((TileJarFillable)tile).amount > 0) {
            this.renderLiquid((TileJarFillable)tile, x, y, z, f);
         }

         if (((TileJarFillable)tile).aspectFilter != null) {
            GlStateManager.pushMatrix();
            switch (((TileJarFillable)tile).facing) {
               case 3:
                  GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                  break;
               case 4:
                  GlStateManager.rotate(270.0F, 0.0F, 1.0F, 0.0F);
                  break;
               case 5:
                  GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
            }

            float rot = (float)((((TileJarFillable)tile).aspectFilter.getTag().hashCode() + tile.getPos().getX() + ((TileJarFillable)tile).facing) % 4 - 2);
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0F, -0.4F, 0.315F);
            if (Config.crooked) {
               GlStateManager.rotate(rot, 0.0F, 0.0F, 1.0F);
            }

            UtilsFX.renderQuadCenteredFromTexture("textures/models/label.png", 0.5F, 1.0F, 1.0F, 1.0F, -99, 771, 1.0F);
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0F, -0.4F, 0.316F);
            if (Config.crooked) {
               GlStateManager.rotate(rot, 0.0F, 0.0F, 1.0F);
            }

            GlStateManager.scale(0.021, 0.021, 0.021);
            GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
            UtilsFX.drawTag(-8, -8, ((TileJarFillable)tile).aspectFilter);
            GlStateManager.popMatrix();
            GlStateManager.popMatrix();
         }
      }

      this.bindTexture(tile.getTexture());
      if (tile instanceof TileJarNode && ((TileJarNode)tile).animate > 0L) {
         long time = System.currentTimeMillis();
         if (((TileJarNode)tile).animate > time) {
            float size = 1.0F + 2.0F * ((float)(((TileJarNode)tile).animate - time) / 1000.0F);
            GlStateManager.scale(size, size, size);
         } else {
            ((TileJarNode)tile).animate = 0L;
         }
      }

      GlStateManager.enableCull();
      GlStateManager.popMatrix();
   }

   private static final net.minecraft.util.ResourceLocation TEX_LIQUID = new net.minecraft.util.ResourceLocation("thaumcraft", "blocks/animatedglow");

   public void renderLiquid(TileJarFillable te, double x, double y, double z, float f) {
      if (this.rendererDispatcher.renderEngine != null) {
         GlStateManager.pushMatrix();
         GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
         GlStateManager.disableLighting();

         float level = (float)te.amount / (float)te.maxAmount * 0.625F;
         float cr = 1.0F, cg = 1.0F, cb = 1.0F;
         if (te.aspect != null) {
            int col = te.aspect.getColor();
            cr = ((col >> 16) & 0xFF) / 255.0F;
            cg = ((col >> 8) & 0xFF) / 255.0F;
            cb = (col & 0xFF) / 255.0F;
         }

         this.rendererDispatcher.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
         TextureAtlasSprite icon = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(TEX_LIQUID.toString());

         float x1 = 0.25F, z1 = 0.25F, x2 = 0.75F, z2 = 0.75F;
         float y1 = 0.0625F, y2 = 0.0625F + level;
         float u0 = icon.getMinU(), u1 = icon.getMaxU();
         float v0 = icon.getMinV(), v1 = icon.getMaxV();

         Tessellator tess = Tessellator.getInstance();
         BufferBuilder buf = tess.getBuffer();
         buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

         // Offset to center (jar model is centered at 0.5, but we're in a rotated frame)
         float ox = -0.5F, oz = -0.5F;

         // Bottom
         buf.pos(ox + x1, y1, oz + z2).tex(u0, v1).color(cr, cg, cb, 1.0F).endVertex();
         buf.pos(ox + x1, y1, oz + z1).tex(u0, v0).color(cr, cg, cb, 1.0F).endVertex();
         buf.pos(ox + x2, y1, oz + z1).tex(u1, v0).color(cr, cg, cb, 1.0F).endVertex();
         buf.pos(ox + x2, y1, oz + z2).tex(u1, v1).color(cr, cg, cb, 1.0F).endVertex();
         // Top
         buf.pos(ox + x1, y2, oz + z1).tex(u0, v0).color(cr, cg, cb, 1.0F).endVertex();
         buf.pos(ox + x1, y2, oz + z2).tex(u0, v1).color(cr, cg, cb, 1.0F).endVertex();
         buf.pos(ox + x2, y2, oz + z2).tex(u1, v1).color(cr, cg, cb, 1.0F).endVertex();
         buf.pos(ox + x2, y2, oz + z1).tex(u1, v0).color(cr, cg, cb, 1.0F).endVertex();
         // North
         buf.pos(ox + x2, y2, oz + z1).tex(u0, v0).color(cr, cg, cb, 1.0F).endVertex();
         buf.pos(ox + x2, y1, oz + z1).tex(u0, v1).color(cr, cg, cb, 1.0F).endVertex();
         buf.pos(ox + x1, y1, oz + z1).tex(u1, v1).color(cr, cg, cb, 1.0F).endVertex();
         buf.pos(ox + x1, y2, oz + z1).tex(u1, v0).color(cr, cg, cb, 1.0F).endVertex();
         // South
         buf.pos(ox + x1, y2, oz + z2).tex(u0, v0).color(cr, cg, cb, 1.0F).endVertex();
         buf.pos(ox + x1, y1, oz + z2).tex(u0, v1).color(cr, cg, cb, 1.0F).endVertex();
         buf.pos(ox + x2, y1, oz + z2).tex(u1, v1).color(cr, cg, cb, 1.0F).endVertex();
         buf.pos(ox + x2, y2, oz + z2).tex(u1, v0).color(cr, cg, cb, 1.0F).endVertex();
         // West
         buf.pos(ox + x1, y2, oz + z1).tex(u0, v0).color(cr, cg, cb, 1.0F).endVertex();
         buf.pos(ox + x1, y1, oz + z1).tex(u0, v1).color(cr, cg, cb, 1.0F).endVertex();
         buf.pos(ox + x1, y1, oz + z2).tex(u1, v1).color(cr, cg, cb, 1.0F).endVertex();
         buf.pos(ox + x1, y2, oz + z2).tex(u1, v0).color(cr, cg, cb, 1.0F).endVertex();
         // East
         buf.pos(ox + x2, y2, oz + z2).tex(u0, v0).color(cr, cg, cb, 1.0F).endVertex();
         buf.pos(ox + x2, y1, oz + z2).tex(u0, v1).color(cr, cg, cb, 1.0F).endVertex();
         buf.pos(ox + x2, y1, oz + z1).tex(u1, v1).color(cr, cg, cb, 1.0F).endVertex();
         buf.pos(ox + x2, y2, oz + z1).tex(u1, v0).color(cr, cg, cb, 1.0F).endVertex();

         tess.draw();
         GlStateManager.enableLighting();
         GlStateManager.popMatrix();
         GlStateManager.color(1.0F, 1.0F, 1.0F);
      }
   }

   public void renderBrain(TileJarBrain te, double x, double y, double z, float f) {
      float bob = MathHelper.sin((float)Minecraft.getMinecraft().player.ticksExisted / 14.0F) * 0.03F + 0.03F;
      GlStateManager.pushMatrix();
      GlStateManager.translate(0.0F, -0.8F + bob, 0.0F);

      float f2;
      for(f2 = te.rota - te.rotb; f2 >= 3.141593F; f2 -= 6.283185F) {
      }

      while(f2 < -3.141593F) {
         f2 += 6.283185F;
      }

      float f3 = te.rotb + f2 * f;
      GlStateManager.rotate(f3 * 180.0F / 3.141593F, 0.0F, 1.0F, 0.0F);
      GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
      UtilsFX.bindTexture("textures/models/brain2.png");
      GlStateManager.scale(0.4F, 0.4F, 0.4F);
      this.brain.render();
      GlStateManager.scale(1.0F, 1.0F, 1.0F);
      GlStateManager.popMatrix();
      UtilsFX.bindTexture("textures/models/jarbrine.png");
      this.model.renderBrine();
   }

}
