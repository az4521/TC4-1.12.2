package thaumcraft.client.renderers.entity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.entities.monster.EntityTaintSheep;

public class RenderTaintSheep extends RenderLiving {
   private static final ResourceLocation rl = new ResourceLocation("thaumcraft", "textures/models/sheep.png");

   public RenderTaintSheep(RenderManager renderManager, ModelBase par1ModelBase, ModelBase par2ModelBase, float par3) {
      super(renderManager, par1ModelBase, par3);
   }

   protected ResourceLocation getEntityTexture(Entity entity) {
      return rl;
   }
}
