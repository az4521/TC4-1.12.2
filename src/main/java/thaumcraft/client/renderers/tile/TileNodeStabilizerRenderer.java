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
import thaumcraft.common.tiles.TileNodeStabilizer;

import static thaumcraft.client.renderers.tile.TileBlockInfoGetter.getBlockTypeSafely;
import net.minecraft.client.renderer.GlStateManager;

@SideOnly(Side.CLIENT)
public class TileNodeStabilizerRenderer extends TileEntitySpecialRenderer<TileNodeStabilizer> {
   private IModelCustom model;
   private static final ResourceLocation MODEL = new ResourceLocation("thaumcraft", "textures/models/node_stabilizer.obj");

   public TileNodeStabilizerRenderer() {
      this.model = AdvancedModelLoader.loadModel(MODEL);
   }

   @Override
   public void render(TileNodeStabilizer tile, double par2, double par4, double par6, float par8, int destroyStage, float alpha) {
      int lock = 1;
      int bright = 20;
      Block blockType = getBlockTypeSafely(tile);
      if (blockType != null) {
         if (tile.hasWorld() && tile.getBlockMetadata() == 10) {
            lock = 2;
         }

         bright = tile.getWorld().getCombinedLight(tile.getPos(), 0);
      } else {
         lock = tile.lock;
      }

      GlStateManager.pushMatrix();
      GlStateManager.translate((float)par2 + 0.5F, (float)par4, (float)par6 + 0.5F);
      GlStateManager.rotate(90.0F, -1.0F, 0.0F, 0.0F);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      UtilsFX.bindTexture("textures/models/node_stabilizer.png");
      this.model.renderPart("lock");

      for(int a = 0; a < 4; ++a) {
         GlStateManager.pushMatrix();
         if (tile.getWorld() != null) {
            int k = bright % 65536;
            int l = bright / 65536;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) k, (float) l);
         }

         GlStateManager.rotate((float)(90 * a), 0.0F, 0.0F, 1.0F);
         GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
         GlStateManager.translate(0.0F, 0.0F, (float)tile.count / 100.0F);
         UtilsFX.bindTexture("textures/models/node_stabilizer.png");
         this.model.renderPart("piston");
         if (lock == 2) {
            GlStateManager.color(1.0F, 0.2F, 0.2F, 1.0F);
         }

         if (tile.getWorld() != null) {
            float scale = MathHelper.sin((float)(Minecraft.getMinecraft().getRenderViewEntity().ticksExisted + a * 5) / 3.0F) * 0.1F + 0.9F;
            int j = 50 + (int)(170.0F * (float)tile.count / 37.0F * scale);
            int k = j % 65536;
            int l = j / 65536;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) k, (float) l);
         }

         UtilsFX.bindTexture("textures/models/node_stabilizer_over.png");
         this.model.renderPart("piston");
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.popMatrix();
      }

      GlStateManager.popMatrix();
      if (tile.count > 0) {
         GlStateManager.pushMatrix();
         GlStateManager.alphaFunc(516, 0.003921569F);
         GlStateManager.enableBlend();
         GlStateManager.blendFunc(770, 1);
         GlStateManager.depthMask(false);
         float alphaVal = MathHelper.sin((float)Minecraft.getMinecraft().getRenderViewEntity().ticksExisted / 8.0F) * 0.1F + 0.5F;
         UtilsFX.bindTexture("textures/misc/node_bubble.png");
         UtilsFX.renderFacingQuad((double)tile.getPos().getX() + (double)0.5F, (double)tile.getPos().getY() + (double)1.5F, (double)tile.getPos().getZ() + (double)0.5F, 0.0F, 0.9F, (float)tile.count / 37.0F * alphaVal, 1, 0, par8, lock == 1 ? 16777215 : 16729156);
         GlStateManager.depthMask(true);
         GlStateManager.disableBlend();
         GlStateManager.alphaFunc(516, 0.1F);
         GlStateManager.popMatrix();
      }

   }
}
