package thaumcraft.client.renderers.tile;

import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import thaumcraft.client.lib.UtilsFX;
import thaumcraft.client.renderers.models.ModelBore;
import thaumcraft.client.renderers.models.ModelBoreEmit;
import thaumcraft.client.renderers.models.ModelJar;
import thaumcraft.common.tiles.TileArcaneBore;
import net.minecraft.client.renderer.GlStateManager;

public class TileArcaneBoreRenderer extends TileEntitySpecialRenderer<TileEntity> {
   private ModelBoreEmit modelEmit = new ModelBoreEmit();
   private ModelBore modelBore = new ModelBore();
   private ModelJar modelJar = new ModelJar();

   public void renderEntityAt(TileArcaneBore bore, double x, double y, double z, float fq) {
      if (bore == null){return;}
      Minecraft mc = FMLClientHandler.instance().getClient();
      UtilsFX.bindTexture("textures/models/bore.png");
      GlStateManager.pushMatrix();
      GlStateManager.translate((float)x + 0.5F, (float)y + 0.5F, (float)z + 0.5F);
      GlStateManager.rotate((float)bore.rotX - bore.vRadX + fq * (float)bore.speedX, 0.0F, 1.0F, 0.0F);
      GlStateManager.pushMatrix();
      if (bore.baseOrientation.ordinal() == 0) {
         GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
      }

      GlStateManager.translate(0.0F, -0.5F, 0.0F);
      this.modelBore.renderBase();
      GlStateManager.popMatrix();
      GlStateManager.rotate((float)bore.rotZ - bore.vRadZ + fq * (float)bore.speedZ, 0.0F, 0.0F, 1.0F);
      GlStateManager.pushMatrix();
      GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
      GlStateManager.translate(0.0F, -0.5F, 0.0F);
      this.modelBore.renderNozzle();
      GlStateManager.popMatrix();
      GlStateManager.pushMatrix();
      GlStateManager.rotate((float)bore.topRotation, 0.0F, 1.0F, 0.0F);
      GlStateManager.translate(0.0F, 0.5F, 0.0F);
      this.modelEmit.render(bore.hasFocus);
      GlStateManager.popMatrix();
      GlStateManager.pushMatrix();
      float rotation = (float)(Minecraft.getMinecraft().getRenderViewEntity().ticksExisted % 45) + fq;
      GlStateManager.translate(0.0F, -0.17F, 0.0F);
      GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
      GlStateManager.rotate(-(rotation * 8.0F), 0.0F, 0.0F, 1.0F);
      GlStateManager.rotate(10.0F, 0.0F, 1.0F, 0.0F);
      UtilsFX.renderQuadCenteredFromTexture("textures/misc/vortex.png", 0.4F, 1.0F, 1.0F, 1.0F, 200, 771, 1.0F);
      GlStateManager.popMatrix();
      GlStateManager.pushMatrix();
      rotation = (float)(Minecraft.getMinecraft().getRenderViewEntity().ticksExisted % 45) + fq;
      GlStateManager.translate(0.0F, -0.21F, 0.0F);
      GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
      GlStateManager.rotate(rotation * 8.0F, 0.0F, 0.0F, 1.0F);
      GlStateManager.rotate(10.0F, 0.0F, 1.0F, 0.0F);
      UtilsFX.renderQuadCenteredFromTexture("textures/misc/vortex.png", 0.3F, 1.0F, 1.0F, 1.0F, 200, 771, 0.8F);
      GlStateManager.popMatrix();
      GlStateManager.pushMatrix();
      rotation = (float)(Minecraft.getMinecraft().getRenderViewEntity().ticksExisted % 45) + fq;
      GlStateManager.translate(0.0F, -0.25F, 0.0F);
      GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
      GlStateManager.rotate(-(rotation * 8.0F), 0.0F, 0.0F, 1.0F);
      GlStateManager.rotate(-10.0F, 0.0F, 1.0F, 0.0F);
      UtilsFX.renderQuadCenteredFromTexture("textures/misc/vortex.png", 0.2F, 1.0F, 1.0F, 1.0F, 200, 771, 0.8F);
      GlStateManager.popMatrix();
      GlStateManager.pushMatrix();
      UtilsFX.bindTexture("textures/models/jar.png");
      GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
      GlStateManager.translate(0.0F, 0.3F, 0.0F);
      GlStateManager.scale(0.6F, 0.6F, 0.6F);
      GlStateManager.depthMask(false);
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(770, 771);
      this.modelJar.Core.render(0.0625F);
      GlStateManager.disableBlend();
      GlStateManager.depthMask(true);
      GlStateManager.scale(1.0F, 1.0F, 1.0F);
      GlStateManager.popMatrix();
      GlStateManager.popMatrix();
   }

   @Override


   public void render(TileEntity tileentity, double d, double d1, double d2, float f, int destroyStage, float alpha) {
      if (! (tileentity instanceof TileArcaneBore)) {return;}
      this.renderEntityAt((TileArcaneBore)tileentity, d, d1, d2, f);
   }
}
