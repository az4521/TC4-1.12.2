package thaumcraft.client.renderers.entity;

import net.minecraft.client.model.ModelVillager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.util.ResourceLocation;
import thaumcraft.common.entities.monster.EntityTaintVillager;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;

public class RenderTaintVillager extends RenderLiving<EntityTaintVillager> {
   protected ModelVillager villagerModel;
   private static final ResourceLocation rl = new ResourceLocation("thaumcraft", "textures/models/villager.png");

   public RenderTaintVillager(RenderManager renderManager) {
      super(renderManager, new ModelVillager(0.0F), 0.5F);
      this.villagerModel = (ModelVillager)this.mainModel;
   }

   @Override
   protected ResourceLocation getEntityTexture(EntityTaintVillager entity) {
      return rl;
   }

   // no @Override — avoids generic erasure name clash
   protected void preRenderCallback(EntityTaintVillager entity, float partialTick) {
      float scale = 0.9375F;
      this.shadowSize = 0.5F;
      GlStateManager.scale(scale, scale, scale);
   }
}
