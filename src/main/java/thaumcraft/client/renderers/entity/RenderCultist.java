package thaumcraft.client.renderers.entity;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.awt.Color;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.ResourceLocation;

import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.config.Config;
import thaumcraft.common.entities.monster.EntityCultistCleric;
import thaumcraft.common.entities.monster.boss.EntityCultistLeader;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.entity.RenderManager;
@SideOnly(Side.CLIENT)
public class RenderCultist extends RenderBiped<EntityLiving> {
   private static final ResourceLocation skin = new ResourceLocation("thaumcraft", "textures/models/cultist.png");

   public RenderCultist(RenderManager renderManager) {
      super(renderManager, new ModelBiped(), 0.5F);
   }

   @Override
   protected ResourceLocation getEntityTexture(EntityLiving entity) {
      return skin;
   }

   @Override
   protected void preRenderCallback(EntityLiving par1EntityLiving, float par2) {
      if (par1EntityLiving instanceof EntityCultistLeader) {
         GlStateManager.scale(1.25F, 1.25F, 1.25F);
      }
   }

   @Override
   public void doRender(EntityLiving entity, double x, double y, double z, float entityYaw, float partialTicks) {
      GlStateManager.pushMatrix();
      float bob = 0.0F;
      boolean rit = entity instanceof EntityCultistCleric && ((EntityCultistCleric)entity).getIsRitualist();
      if (rit) {
         int val = (new Random(entity.getEntityId())).nextInt(1000);
         float c = (float) entity.ticksExisted + partialTicks + (float)val;
         bob = MathHelper.sin(c / 9.0F) * 0.1F + 0.21F;
         GlStateManager.translate(0.0F, bob, 0.0F);
      }

      super.doRender(entity, x, y, z, entityYaw, partialTicks);
      if (rit) {
         GlStateManager.pushMatrix();
         this.drawFloatyLine(entity.posX, entity.posY + (double)(entity.getEyeHeight() * 1.2F), entity.posZ, (double)((EntityCultistCleric)entity).getHomePosition().getX() + 0.5, (double)((EntityCultistCleric)entity).getHomePosition().getY() + 1.5 - (double)bob, (double)((EntityCultistCleric)entity).getHomePosition().getZ() + 0.5, partialTicks, 1114129, "textures/misc/wispy.png", -0.03F, (float)Math.min(entity.ticksExisted, 10) / 10.0F, 0.25F);
         GlStateManager.popMatrix();
      }

      GlStateManager.popMatrix();
   }

   private void drawFloatyLine(double x, double y, double z, double x2, double y2, double z2, float partialTicks, int color, String texture, float speed, float distance, float width) {
      net.minecraft.entity.Entity player = Minecraft.getMinecraft().getRenderViewEntity();
      double iPX = player.prevPosX + (player.posX - player.prevPosX) * (double)partialTicks;
      double iPY = player.prevPosY + (player.posY - player.prevPosY) * (double)partialTicks;
      double iPZ = player.prevPosZ + (player.posZ - player.prevPosZ) * (double)partialTicks;
      GlStateManager.translate(-iPX + x2, -iPY + y2, -iPZ + z2);
      float time = (float)(System.nanoTime() / 30000000L);
      Color co = new Color(color);
      float r = (float)co.getRed() / 255.0F;
      float g = (float)co.getGreen() / 255.0F;
      float b = (float)co.getBlue() / 255.0F;
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(770, 771);
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder buffer = tessellator.getBuffer();
      double dc1x = (float)(x - x2);
      double dc1y = (float)(y - y2);
      double dc1z = (float)(z - z2);
      UtilsFX.bindTexture(texture);
      buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX_COLOR);
      float dist = MathHelper.sqrt((x-x2)*(x-x2) + (y-y2)*(y-y2) + (z-z2)*(z-z2));
      float blocks = (float)Math.round(dist);
      float length = blocks * ((float)Config.golemLinkQuality / 2.0F);
      float f9 = 0.0F;
      float f10 = 1.0F;

      for(int i = 0; (float)i <= length * distance; ++i) {
         float f2 = (float)i / length;
         float f3 = 1.0F - Math.abs((float)i - length / 2.0F) / (length / 2.0F);
         double dx = dc1x + (double)(MathHelper.sin((float)((z % 16.0 + (double)(dist * (1.0F - f2) * (float)Config.golemLinkQuality / 2.0F) - (double)(time % 32767.0F / 5.0F)) / 4.0)) * 0.5F * f3);
         double dy = dc1y + (double)(MathHelper.sin((float)((x % 16.0 + (double)(dist * (1.0F - f2) * (float)Config.golemLinkQuality / 2.0F) - (double)(time % 32767.0F / 5.0F)) / 3.0)) * 0.5F * f3);
         double dz = dc1z + (double)(MathHelper.sin((float)((y % 16.0 + (double)(dist * (1.0F - f2) * (float)Config.golemLinkQuality / 2.0F) - (double)(time % 32767.0F / 5.0F)) / 2.0)) * 0.5F * f3);
         float f13 = (1.0F - f2) * dist - time * speed;
         buffer.pos(dx * f2, dy * f2 - width, dz * f2).tex(f13, f10).color(r, g, b, 0.8F).endVertex();
         buffer.pos(dx * f2, dy * f2 + width, dz * f2).tex(f13, f9).color(r, g, b, 0.8F).endVertex();
      }

      tessellator.draw();
      buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX_COLOR);

      for(int i = 0; (float)i <= length * distance; ++i) {
         float f2 = (float)i / length;
         float f3 = 1.0F - Math.abs((float)i - length / 2.0F) / (length / 2.0F);
         double dx = dc1x + (double)(MathHelper.sin((float)((z % 16.0 + (double)(dist * (1.0F - f2) * (float)Config.golemLinkQuality / 2.0F) - (double)(time % 32767.0F / 5.0F)) / 4.0)) * 0.5F * f3);
         double dy = dc1y + (double)(MathHelper.sin((float)((x % 16.0 + (double)(dist * (1.0F - f2) * (float)Config.golemLinkQuality / 2.0F) - (double)(time % 32767.0F / 5.0F)) / 3.0)) * 0.5F * f3);
         double dz = dc1z + (double)(MathHelper.sin((float)((y % 16.0 + (double)(dist * (1.0F - f2) * (float)Config.golemLinkQuality / 2.0F) - (double)(time % 32767.0F / 5.0F)) / 2.0)) * 0.5F * f3);
         float f13 = (1.0F - f2) * dist - time * speed;
         buffer.pos(dx * f2 - width, dy * f2, dz * f2).tex(f13, f10).color(r, g, b, 0.8F).endVertex();
         buffer.pos(dx * f2 + width, dy * f2, dz * f2).tex(f13, f9).color(r, g, b, 0.8F).endVertex();
      }

      tessellator.draw();
      GlStateManager.disableBlend();
   }
}
