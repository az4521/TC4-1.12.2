package thaumcraft.client.renderers.entity;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import thaumcraft.common.entities.monster.EntityThaumicSlime;

@SideOnly(Side.CLIENT)
public class RenderThaumicSlime extends RenderLiving<EntityThaumicSlime> {
   private static final ResourceLocation slimeTextures = new ResourceLocation("thaumcraft", "textures/models/tslime.png");

   public RenderThaumicSlime(RenderManager renderManager, ModelBase par1ModelBase, ModelBase par2ModelBase, float par3) {
      super(renderManager, par1ModelBase, par3);
   }

   @Override
   protected ResourceLocation getEntityTexture(EntityThaumicSlime entity) {
      return slimeTextures;
   }

   protected void scaleSlime(EntityThaumicSlime par1EntitySlime, float par2) {
      float f1 = (float)Math.sqrt(par1EntitySlime.getSlimeSize());
      float f2 = (par1EntitySlime.prevSquishFactor + (par1EntitySlime.squishFactor - par1EntitySlime.prevSquishFactor) * par2) / (f1 * 0.25F + 1.0F);
      float f3 = 1.0F / (f2 + 1.0F);
      GlStateManager.scale(f3 * f1 + 0.1F, 1.0F / f3 * f1 + 0.1F, f3 * f1 + 0.1F);
   }

   protected void preRenderCallback(EntityThaumicSlime par1EntityLiving, float par2) {
      this.scaleSlime(par1EntityLiving, par2);
   }
}
