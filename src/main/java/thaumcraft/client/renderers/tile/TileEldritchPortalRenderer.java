package thaumcraft.client.renderers.tile;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.tiles.TileEldritchPortal;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.Tessellator;

public class TileEldritchPortalRenderer extends TileEntitySpecialRenderer<TileEntity> {
   public static final ResourceLocation portaltex = new ResourceLocation("thaumcraft", "textures/misc/eldritch_portal.png");

   @Override


   public void render(TileEntity te, double x, double y, double z, float f, int destroyStage, float alpha) {
      GlStateManager.pushMatrix();
      if (te.getWorld() != null) {
         this.renderPortal((TileEldritchPortal)te, x, y, z, f);
      }

      GlStateManager.popMatrix();
   }

   private void renderPortal(TileEldritchPortal te, double x, double y, double z, float f) {
      long nt = System.nanoTime();
      long time = nt / 50000000L;
      int c = (int)Math.min(30.0F, (float)te.opencount + f);
      int e = (int)Math.min(5.0F, (float)te.opencount + f);
      float scale = (float)e / 5.0F;
      float scaley = (float)c / 30.0F;
      UtilsFX.bindTexture(portaltex);
      GlStateManager.pushMatrix();
      GlStateManager.depthMask(false);
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(770, 771);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      if (Minecraft.getMinecraft().getRenderViewEntity() instanceof EntityPlayer) {
         Tessellator tessellator = Tessellator.getInstance();
         BufferBuilder buffer = tessellator.getBuffer();
         float arX = ActiveRenderInfo.getRotationX();
         float arZ = ActiveRenderInfo.getRotationZ();
         float arYZ = ActiveRenderInfo.getRotationYZ();
         float arXY = ActiveRenderInfo.getRotationXY();
         float arXZ = ActiveRenderInfo.getRotationXZ();
         buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR); 

         double px = x + (double)0.5F;
         double py = y + (double)0.5F;
         double pz = z + (double)0.5F;
         Vec3d v1 = new Vec3d(-arX - arYZ, -arXZ, -arZ - arXY);
         Vec3d v2 = new Vec3d(-arX + arYZ, arXZ, -arZ + arXY);
         Vec3d v3 = new Vec3d(arX + arYZ, arXZ, arZ + arXY);
         Vec3d v4 = new Vec3d(arX - arYZ, -arXZ, arZ - arXY);
         int frame = (int)time % 16;
         float f2 = (float)frame / 16.0F;
         float f3 = f2 + 0.0625F;
         float f4 = 0.0F;
         float f5 = 1.0F;

         buffer.pos(px + v1.x * (double)scale, py + v1.y * (double)scaley, pz + v1.z * (double)scale).tex(f2, f5).color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
         buffer.pos(px + v2.x * (double)scale, py + v2.y * (double)scaley, pz + v2.z * (double)scale).tex(f3, f5).color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
         buffer.pos(px + v3.x * (double)scale, py + v3.y * (double)scaley, pz + v3.z * (double)scale).tex(f3, f4).color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
         buffer.pos(px + v4.x * (double)scale, py + v4.y * (double)scaley, pz + v4.z * (double)scale).tex(f2, f4).color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
         tessellator.draw();
      }

      GlStateManager.disableBlend();
      GlStateManager.depthMask(true);
      GlStateManager.popMatrix();
   }
}
