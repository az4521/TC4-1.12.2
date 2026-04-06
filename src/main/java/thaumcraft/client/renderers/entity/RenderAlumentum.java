package thaumcraft.client.renderers.entity;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import thaumcraft.common.entities.projectile.EntityAlumentum;

public class RenderAlumentum extends Render<EntityAlumentum> {
   public RenderAlumentum(RenderManager renderManager) {
      super(renderManager);
      this.shadowSize = 0.1F;
   }

   @Override
   public void doRender(EntityAlumentum entity, double d, double d1, double d2, float f, float f1) {
   }

   @Override
   protected ResourceLocation getEntityTexture(EntityAlumentum entity) {
      return new ResourceLocation("textures/entity/steve.png");
   }
}
