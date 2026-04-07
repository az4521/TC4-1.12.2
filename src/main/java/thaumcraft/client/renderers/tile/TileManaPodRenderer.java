package thaumcraft.client.renderers.tile;

import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.ResourceLocation;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.client.renderers.models.ModelManaPod;
import thaumcraft.common.tiles.TileManaPod;

import static thaumcraft.client.renderers.tile.TileBlockInfoGetter.getBlockMetaSafely;

public class TileManaPodRenderer extends TileEntitySpecialRenderer<TileEntity> {
   private ModelManaPod model = new ModelManaPod();
   private static final ResourceLocation pod0tex = new ResourceLocation("thaumcraft", "textures/models/manapod_0.png");
   private static final ResourceLocation pod2tex = new ResourceLocation("thaumcraft", "textures/models/manapod_2.png");

   public void renderEntityAt(TileManaPod pod, double x, double y, double z, float fq) {
      int meta = 0;
      Aspect aspect = Aspect.PLANT;

      if (!pod.hasWorld()) {
         meta = 5;
      } else {
         meta = getBlockMetaSafely(pod);
         if (meta == -1){
            meta = 5;
         }
         if (pod.aspect != null) {
            aspect = pod.aspect;
         }

      }

      if (meta > 1) {
         float br = 0.14509805F;
         float bg = 0.6156863F;
         float bb = 0.45882353F;
         float fr = br;
         float fg = bg;
         float fb = bb;
         if (pod.aspect != null) {
            Color color = new Color(aspect.getColor());
            float ar = (float)color.getRed() / 255.0F;
            float ag = (float)color.getGreen() / 255.0F;
            float ab = (float)color.getBlue() / 255.0F;
            if (meta == 7) {
               fr = ar;
               fg = ag;
               fb = ab;
            } else {
               float m = (float)(meta - 2);
               fr = (br + ar * m) / (m + 1.0F);
               fg = (bg + ag * m) / (m + 1.0F);
               fb = (bb + ab * m) / (m + 1.0F);
            }
         }

         GlStateManager.pushMatrix();
         RenderHelper.disableStandardItemLighting();
         GlStateManager.disableLighting();
         GlStateManager.enableBlend();
         GlStateManager.enableRescaleNormal();
         GlStateManager.disableCull();
         GlStateManager.blendFunc(770, 771);
         GlStateManager.translate(x + 0.5D, y + 0.75D, z + 0.5D);
         GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
         if (meta > 2) {
            EntityPlayer p = Minecraft.getMinecraft().player;
            float scale = MathHelper.sin((float)(p.ticksExisted + pod.hashCode() % 100) / 8.0F) * 0.1F + 0.9F;
            GlStateManager.pushMatrix();
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
            GlStateManager.translate(0.0F, 0.1D, 0.0D);
            GlStateManager.scale(0.125D * (double)meta * (double)scale, 0.125D * (double)meta * (double)scale, 0.125D * (double)meta * (double)scale);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            UtilsFX.bindTexture(pod0tex);
            this.model.pod0.render(0.0625F);
            GlStateManager.popMatrix();
         }

         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
         GlStateManager.scale(0.15D * (double)meta, 0.15D * (double)meta, 0.15D * (double)meta);
         GlStateManager.color(fr, fg, fb, 0.9F);
         UtilsFX.bindTexture(pod2tex);
         this.model.pod2.render(0.0625F);
         GlStateManager.enableLighting();
         GlStateManager.enableCull();
         GlStateManager.disableRescaleNormal();
         GlStateManager.disableBlend();
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.popMatrix();
      }

   }

   @Override


   public void render(TileEntity te, double d, double d1, double d2, float f, int destroyStage, float alpha) {

      if (!(te instanceof TileManaPod)) {return;}
      this.renderEntityAt((TileManaPod) te, d, d1, d2, f);
   }
}
