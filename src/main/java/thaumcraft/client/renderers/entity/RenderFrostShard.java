package thaumcraft.client.renderers.entity;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.Random;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import thaumcraft.client.renderers.models.AdvancedModelLoader;
import thaumcraft.client.renderers.models.IModelCustom;

import thaumcraft.common.entities.projectile.EntityFrostShard;
import net.minecraft.client.renderer.GlStateManager;

@SideOnly(Side.CLIENT)
public class RenderFrostShard extends Render {
   private IModelCustom model;
   private static final ResourceLocation ORB = new ResourceLocation("thaumcraft", "textures/models/orb.obj");
   private static final ResourceLocation rl = new ResourceLocation("thaumcraft", "textures/blocks/frostshard.png");

   public RenderFrostShard(RenderManager renderManager) {
      super(renderManager);
      this.model = AdvancedModelLoader.loadModel(ORB);
   }

   public void renderShard(EntityFrostShard shard, double par2, double par4, double par6, float par8, float par9) {
      this.bindEntityTexture(shard);
      GlStateManager.pushMatrix();
      GlStateManager.enableRescaleNormal();
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(770, 771);
      GlStateManager.translate((float)par2, (float)par4, (float)par6);
      Random rnd = new Random(shard.getEntityId());
      GlStateManager.rotate(shard.prevRotationYaw + (shard.rotationYaw - shard.prevRotationYaw) * par9, 0.0F, 1.0F, 0.0F);
      GlStateManager.rotate(shard.prevRotationPitch + (shard.rotationPitch - shard.prevRotationPitch) * par9, 0.0F, 0.0F, 1.0F);
      GlStateManager.pushMatrix();
      float base = shard.getDamage() * 0.1F;
      GlStateManager.scale(base + rnd.nextFloat() * 0.1F, base + rnd.nextFloat() * 0.1F, base + rnd.nextFloat() * 0.1F);
      this.model.renderAll();
      GlStateManager.popMatrix();
      GlStateManager.disableBlend();
      GlStateManager.disableRescaleNormal();
      GlStateManager.popMatrix();
   }

   public void doRender(Entity par1Entity, double par2, double par4, double par6, float par8, float par9) {
      this.renderShard((EntityFrostShard)par1Entity, par2, par4, par6, par8, par9);
   }

   protected ResourceLocation getEntityTexture(Entity entity) {
      return rl;
   }
}
