package thaumcraft.client.renderers.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import thaumcraft.client.renderers.models.AdvancedModelLoader;
import thaumcraft.client.renderers.models.IModelCustom;
import org.lwjgl.opengl.GL11;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.client.renderers.models.entities.ModelGolem;
import thaumcraft.client.renderers.models.entities.ModelGolemAccessories;
import thaumcraft.common.blocks.ItemJarFilled;
import thaumcraft.common.entities.golems.EntityGolemBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;

public class RenderGolemBase extends RenderLiving<EntityGolemBase> {
   ModelBase damage;
   ModelBase accessories;
   private static final ResourceLocation BUCKET = new ResourceLocation("thaumcraft", "textures/models/bucket.obj");
   private IModelCustom model;
   private static final ResourceLocation clay    = new ResourceLocation("thaumcraft", "textures/models/golem_clay.png");
   private static final ResourceLocation stone   = new ResourceLocation("thaumcraft", "textures/models/golem_stone.png");
   private static final ResourceLocation wood    = new ResourceLocation("thaumcraft", "textures/models/golem_wood.png");
   private static final ResourceLocation tallow  = new ResourceLocation("thaumcraft", "textures/models/golem_tallow.png");
   private static final ResourceLocation iron    = new ResourceLocation("thaumcraft", "textures/models/golem_iron.png");
   private static final ResourceLocation straw   = new ResourceLocation("thaumcraft", "textures/models/golem_straw.png");
   private static final ResourceLocation flesh   = new ResourceLocation("thaumcraft", "textures/models/golem_flesh.png");
   private static final ResourceLocation thaumium = new ResourceLocation("thaumcraft", "textures/models/golem_thaumium.png");

   public RenderGolemBase(RenderManager renderManager, ModelBase par1ModelBase) {
      super(renderManager, par1ModelBase, 0.25F);
      if (par1ModelBase instanceof ModelGolem) {
         ModelGolem mg = new ModelGolem(false);
         mg.pass = 2;
         this.damage = mg;
      }
      this.accessories = new ModelGolemAccessories(0.0F, 30.0F);
      this.model = AdvancedModelLoader.loadModel(BUCKET);
   }

   protected void renderWithSway(EntityGolemBase e, float par2, float par3, float par4) {
      // Inline RenderLivingBase.rotateCorpse default logic
      GlStateManager.rotate(180.0F - par3, 0.0F, 1.0F, 0.0F);
      if (e.deathTime > 0) {
         float f = ((float)e.deathTime + par4 - 1.0F) / 20.0F * 1.6F;
         f = MathHelper.sqrt(f);
         if (f > 1.0F) f = 1.0F;
         GlStateManager.rotate(f * this.getDeathMaxRotation(e), 0.0F, 0.0F, 1.0F);
      }
      if ((double)e.limbSwingAmount >= 0.01) {
         float var5 = 13.0F;
         float var6 = e.limbSwing - e.limbSwingAmount * (1.0F - par4) + 6.0F;
         float var7 = (Math.abs(var6 % var5 - var5 * 0.5F) - var5 * 0.25F) / (var5 * 0.25F);
         GlStateManager.rotate(6.5F * var7, 0.0F, 0.0F, 1.0F);
      }
   }

   protected void renderCarriedItems(EntityGolemBase e, float par2) {
      ItemStack var3 = e.getCarriedForDisplay();

      if (e.getCore() == 5) {
         GlStateManager.pushMatrix();
         GlStateManager.scale(0.4, 0.4, 0.4);
         GlStateManager.translate(0.0F, 3.0F, -1.1F);
         GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
         UtilsFX.bindTexture("textures/models/bucket.png");
         this.model.renderPart("Bucket");
         GlStateManager.popMatrix();
      } else if (var3 != null && e.deathTime == 0 && e.getCore() != 5) {
         GlStateManager.pushMatrix();
         GlStateManager.scale(0.4, 0.4, 0.4);
         if (var3.getItem() instanceof ItemJarFilled) {
            GlStateManager.translate(0.0F, 2.5F, -1.0F);
            if (e.getCore() == 6) {
               double s = (double)0.5F + (double)Math.min(64, e.getCarryLimit()) / (double)128.0F;
               GlStateManager.scale(s, s, s);
            }
            GlStateManager.translate(-0.5F, 0.0F, -0.8F);
            GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(335.0F, 0.0F, 0.0F, -1.0F);
            GlStateManager.rotate(50.0F, 0.0F, -1.0F, 0.0F);
         } else {
            GlStateManager.translate(-0.5F, 2.5F, -1.25F);
            GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
         }
         Minecraft.getMinecraft().getRenderItem().renderItem(var3,
            net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType.NONE);
         GlStateManager.popMatrix();
      }
   }

   protected void rotateCorpse(EntityGolemBase entity, float par2, float par3, float par4) {
      this.renderWithSway(entity, par2, par3, par4);
   }

   @Override
   protected ResourceLocation getEntityTexture(EntityGolemBase entity) {
      switch (entity.getGolemType()) {
         case STRAW:    return straw;
         case WOOD:     return wood;
         case CLAY:     return clay;
         case STONE:    return stone;
         case IRON:     return iron;
         case TALLOW:   return tallow;
         case FLESH:    return flesh;
         case THAUMIUM: return thaumium;
         default:       return new ResourceLocation("textures/entity/steve.png");
      }
   }
}
