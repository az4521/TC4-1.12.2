package thaumcraft.client.renderers.entity;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import thaumcraft.client.renderers.models.entities.ModelTaintSporeSwarmer;

import net.minecraft.client.renderer.entity.RenderManager;
@SideOnly(Side.CLIENT)
public class RenderTaintSporeSwarmer extends RenderLiving {
   private static final ResourceLocation rl = new ResourceLocation("thaumcraft", "textures/models/taint_spore.png");

   public RenderTaintSporeSwarmer(RenderManager renderManager) {
      super(renderManager, new ModelTaintSporeSwarmer(), 0.25F);
   }

   protected float getDeathMaxRotation(EntityLivingBase par1EntityLiving) {
      return 0.0F;
   }

   protected ResourceLocation getEntityTexture(Entity entity) {
      return rl;
   }
}
