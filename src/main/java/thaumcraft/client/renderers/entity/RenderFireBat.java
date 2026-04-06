package thaumcraft.client.renderers.entity;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.ResourceLocation;
import thaumcraft.client.renderers.models.entities.ModelFireBat;
import thaumcraft.common.entities.monster.EntityFireBat;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;

@SideOnly(Side.CLIENT)
public class RenderFireBat extends RenderLiving<EntityFireBat> {
   private static final ResourceLocation rl = new ResourceLocation("thaumcraft", "textures/models/firebat.png");
   private static final ResourceLocation rl2 = new ResourceLocation("thaumcraft", "textures/models/vampirebat.png");

   public RenderFireBat(RenderManager renderManager) {
      super(renderManager, new ModelFireBat(), 0.25F);
   }

   // No @Override — Java generic bridge clash; still called polymorphically at runtime
   protected void preRenderCallback(EntityFireBat bat, float partialTick) {
      if (!bat.getIsDevil() && !bat.getIsVampire()) {
         GlStateManager.scale(0.35F, 0.35F, 0.35F);
      } else {
         GlStateManager.scale(0.6F, 0.6F, 0.6F);
      }
   }

   // No @Override — same reason
   protected void rotateCorpse(EntityFireBat bat, float age, float yaw, float partialTick) {
      if (!bat.getIsBatHanging()) {
         GlStateManager.translate(0.0F, MathHelper.cos(age * 0.3F) * 0.1F, 0.0F);
      } else {
         GlStateManager.translate(0.0F, -0.1F, 0.0F);
      }
   }

   @Override
   protected ResourceLocation getEntityTexture(EntityFireBat entity) {
      return entity.getIsVampire() ? rl2 : rl;
   }
}
