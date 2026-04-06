package thaumcraft.client.renderers.entity;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderZombie;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.ResourceLocation;

import thaumcraft.common.entities.monster.EntityGiantBrainyZombie;
import net.minecraft.client.renderer.GlStateManager;

import net.minecraft.client.renderer.entity.RenderManager;
@SideOnly(Side.CLIENT)
public class RenderBrainyZombie extends RenderZombie {

   public RenderBrainyZombie(RenderManager renderManager) {
      super(renderManager);
   }
   private static final ResourceLocation zombieTextures = new ResourceLocation("thaumcraft", "textures/models/bzombie.png");

   protected ResourceLocation getEntityTexture(EntityZombie par1EntityZombie) {
      return zombieTextures;
   }

   protected void preRenderCallback(EntityZombie par1EntityLiving, float par2) {
      if (par1EntityLiving instanceof EntityGiantBrainyZombie) {
         GlStateManager.scale(((EntityGiantBrainyZombie)par1EntityLiving).getAnger(),
               ((EntityGiantBrainyZombie)par1EntityLiving).getAnger(),
               ((EntityGiantBrainyZombie)par1EntityLiving).getAnger());
      }
   }
}
