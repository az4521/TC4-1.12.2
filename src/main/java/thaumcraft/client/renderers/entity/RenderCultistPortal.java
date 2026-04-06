package thaumcraft.client.renderers.entity;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.entities.monster.boss.EntityCultistPortal;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

@SideOnly(Side.CLIENT)
public class RenderCultistPortal extends Render<EntityCultistPortal> {
   public static final ResourceLocation portaltex = new ResourceLocation("thaumcraft", "textures/misc/cultist_portal.png");

   public RenderCultistPortal(RenderManager renderManager) {
      super(renderManager);
      this.shadowSize = 0.1F;
      this.shadowOpaque = 0.5F;
   }

   public void renderPortal(EntityCultistPortal portal, double px, double py, double pz, float par8, float f) {
      long nt = System.nanoTime();
      long time = nt / 50000000L;
      float scaley = 1.5F;
      int e = (int)Math.min(50.0F, (float)portal.ticksExisted + f);
      if (portal.hurtTime > 0) {
         double d = Math.sin((double)(portal.hurtTime * 72) * Math.PI / (double)180.0F);
         scaley = (float)((double)scaley - d / (double)4.0F);
         e = (int)((double)e + (double)6.0F * d);
      }

      if (portal.pulse > 0) {
         double d = Math.sin((double)(portal.pulse * 36) * Math.PI / (double)180.0F);
         scaley = (float)((double)scaley + d / (double)4.0F);
         e = (int)((double)e + (double)12.0F * d);
      }

      float scale = (float)e / 50.0F * 1.3F;
      py += portal.height / 2.0F;
      float m = (1.0F - portal.getHealth() / portal.getMaxHealth()) / 3.0F;
      float bob = MathHelper.sin((float)portal.ticksExisted / (5.0F - 12.0F * m)) * m + m;
      float bob2 = MathHelper.sin((float)portal.ticksExisted / (6.0F - 15.0F * m)) * m + m;
      float alpha = 1.0F - bob;
      scaley -= bob / 4.0F;
      scale -= bob2 / 3.0F;
      UtilsFX.bindTexture(portaltex);
      GlStateManager.pushMatrix();
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(770, 771);
      GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
      if (Minecraft.getMinecraft().getRenderViewEntity() instanceof EntityPlayer) {
         Tessellator tessellator = Tessellator.getInstance();
         BufferBuilder buffer = tessellator.getBuffer();
         float arX = ActiveRenderInfo.getRotationX();
         float arZ = ActiveRenderInfo.getRotationZ();
         float arYZ = ActiveRenderInfo.getRotationYZ();
         float arXY = ActiveRenderInfo.getRotationXY();
         float arXZ = ActiveRenderInfo.getRotationXZ();
         EntityPlayer player = (EntityPlayer)Minecraft.getMinecraft().getRenderViewEntity();
         double var10000 = player.prevPosX + (player.posX - player.prevPosX) * (double)f;
         var10000 = player.prevPosY + (player.posY - player.prevPosY) * (double)f;
         var10000 = player.prevPosZ + (player.posZ - player.prevPosZ) * (double)f;
         buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR); 
        
        
         Vec3d v1 = new Vec3d(-arX - arYZ, -arXZ, -arZ - arXY);
         Vec3d v2 = new Vec3d(-arX + arYZ, arXZ, -arZ + arXY);
         Vec3d v3 = new Vec3d(arX + arYZ, arXZ, arZ + arXY);
         Vec3d v4 = new Vec3d(arX - arYZ, -arXZ, arZ - arXY);
         int frame = 15 - (int)time % 16;
         float f2 = (float)frame / 16.0F;
         float f3 = f2 + 0.0625F;
         float f4 = 0.0F;
         float f5 = 1.0F;
         buffer.pos(px + v1.x * (double)scale, py + v1.y * (double)scaley, pz + v1.z * (double)scale).tex(f3, f4).color(1.0f, 1.0f, 1.0f, alpha).endVertex();
         buffer.pos(px + v2.x * (double)scale, py + v2.y * (double)scaley, pz + v2.z * (double)scale).tex(f3, f5).color(1.0f, 1.0f, 1.0f, alpha).endVertex();
         buffer.pos(px + v3.x * (double)scale, py + v3.y * (double)scaley, pz + v3.z * (double)scale).tex(f2, f5).color(1.0f, 1.0f, 1.0f, alpha).endVertex();
         buffer.pos(px + v4.x * (double)scale, py + v4.y * (double)scaley, pz + v4.z * (double)scale).tex(f2, f4).color(1.0f, 1.0f, 1.0f, alpha).endVertex();
         tessellator.draw();
      }

      GlStateManager.disableRescaleNormal();
      GlStateManager.disableBlend();
      GlStateManager.popMatrix();
   }

   @Override
   public void doRender(EntityCultistPortal par1Entity, double par2, double par4, double par6, float par8, float par9) {
      this.renderPortal(par1Entity, par2, par4, par6, par8, par9);
   }

   @Override
   protected ResourceLocation getEntityTexture(EntityCultistPortal entity) {
      return portaltex;
   }
}
