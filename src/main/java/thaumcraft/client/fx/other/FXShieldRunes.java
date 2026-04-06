package thaumcraft.client.fx.other;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import thaumcraft.client.renderers.models.AdvancedModelLoader;
import thaumcraft.client.renderers.models.IModelCustom;

import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.entities.monster.EntityCultist;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class FXShieldRunes extends Particle {
   Entity target = null;
   float yaw = 0.0F;
   float pitch = 0.0F;
   private IModelCustom model;
   private static final ResourceLocation MODEL = new ResourceLocation("thaumcraft", "textures/models/hemis.obj");

   public FXShieldRunes(World world, double d, double d1, double d2, Entity target, int age, float yaw, float pitch) {
      super(world, d, d1, d2, 0.0F, 0.0F, 0.0F);
      this.particleRed = 1.0F;
      this.particleGreen = 1.0F;
      this.particleBlue = 1.0F;
      this.particleGravity = 0.0F;
      this.motionX = this.motionY = this.motionZ = 0.0F;
      this.particleMaxAge = age + this.rand.nextInt(age / 2);
      this.canCollide = true;
      this.setSize(0.01F, 0.01F);
      this.canCollide = false;
      this.particleScale = 1.0F;
      this.target = target;
      this.yaw = yaw;
      this.pitch = pitch;
      this.prevPosX = this.posX = target.posX;
      this.prevPosY = this.posY = (target.getEntityBoundingBox().minY + target.getEntityBoundingBox().maxY) / (double)2.0F;
      this.prevPosZ = this.posZ = target.posZ;
   }

   public void renderParticle(BufferBuilder buffer, Entity entityIn, float f, float f1, float f2, float f3, float f4, float f5) {
      Tessellator tessellator = Tessellator.getInstance();
      GlStateManager.pushMatrix();
      GlStateManager.disableCull();
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(770, 1);
      if (this.model == null) {
         this.model = AdvancedModelLoader.loadModel(MODEL);
      }

      float fade = ((float)this.particleAge + f) / (float)this.particleMaxAge;
      float xx = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)f - interpPosX);
      float yy = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)f - interpPosY);
      float zz = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)f - interpPosZ);
      GlStateManager.translate(xx, yy, zz);
      float b = 1.0F;
      int frame = Math.min(15, (int)(14.0F * fade) + 1);
      if (this.target instanceof EntityMob && !(this.target instanceof EntityCultist)) {
         UtilsFX.bindTexture("textures/models/ripple" + frame + ".png");
         b = 0.5F;
      } else {
         UtilsFX.bindTexture("textures/models/hemis" + frame + ".png");
      }

      int i = 220;
      int j = i % 65536;
      int k = i / 65536;
      OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) j, (float) k);
      GlStateManager.rotate(180.0F - this.yaw, 0.0F, 1.0F, 0.0F);
      GlStateManager.rotate(-this.pitch, 1.0F, 0.0F, 0.0F);
      GlStateManager.scale(0.4 * (double)this.target.height, 0.4 * (double)this.target.height, 0.4 * (double)this.target.height);
      GlStateManager.color(b, b, b, Math.min(1.0F, (1.0F - fade) * 3.0F));
      this.model.renderAll();
      GlStateManager.disableBlend();
      GlStateManager.enableCull();
      GlStateManager.popMatrix();
   }

   public void onUpdate() {
      this.prevPosX = this.posX;
      this.prevPosY = this.posY;
      this.prevPosZ = this.posZ;
      if (this.particleAge++ >= this.particleMaxAge) {
         this.setExpired();
      }

      this.posX = this.target.posX;
      this.posY = (this.target.getEntityBoundingBox().minY + this.target.getEntityBoundingBox().maxY) / (double)2.0F;
      this.posZ = this.target.posZ;
   }
}
