package thaumcraft.client.renderers.models.gear;

import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.HashMap;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.ResourceLocation;
import thaumcraft.client.renderers.models.AdvancedModelLoader;
import thaumcraft.client.renderers.models.IModelCustom;
import org.lwjgl.opengl.GL11;
import thaumcraft.client.fx.bolt.FXLightningBolt;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.items.armor.ItemHoverHarness;
import thaumcraft.common.lib.utils.BlockUtils;
import net.minecraft.client.renderer.GlStateManager;

@SideOnly(Side.CLIENT)
public class ModelHoverHarness extends ModelBiped {
   HashMap<Integer,Long> timingShock = new HashMap<>();
   private static final ResourceLocation HARNESS = new ResourceLocation("thaumcraft", "textures/models/hoverharness.obj");
   private IModelCustom modelBack;

   public ModelHoverHarness() {
      this.bipedBody = new ModelRenderer(this, 16, 16);
      this.bipedBody.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, 0.6F);
      this.modelBack = AdvancedModelLoader.loadModel(HARNESS);
   }

   public void render(Entity entity, float par2, float par3, float par4, float par5, float par6, float par7) {
      GlStateManager.pushMatrix();
      GlStateManager.pushMatrix();
      if (entity != null && entity.isSneaking()) {
         GlStateManager.rotate(28.64789F, 1.0F, 0.0F, 0.0F);
      }

      this.bipedBody.render(par7);
      GlStateManager.popMatrix();
      GlStateManager.pushMatrix();
      GlStateManager.disableLighting();
      GlStateManager.scale(0.1F, 0.1F, 0.1F);
      GlStateManager.rotate(90.0F, -1.0F, 0.0F, 0.0F);
      if (entity != null && entity.isSneaking()) {
         GlStateManager.rotate(28.64789F, 1.0F, 0.0F, 0.0F);
      }

      GlStateManager.translate(0.0F, 0.33F, -3.7F);
      FMLClientHandler.instance().getClient().renderEngine.bindTexture(new ResourceLocation("thaumcraft", "textures/models/hoverharness2.png"));
      this.modelBack.renderAll();
      GlStateManager.enableLighting();
      GlStateManager.popMatrix();
      if (entity instanceof EntityPlayer && !GL11.glIsEnabled(GL11.GL_BLEND) && GL11.glGetInteger(2976) == 5888 && ((EntityPlayer) entity).inventory.armorItemInSlot(2).hasTagCompound() && ((EntityPlayer) entity).inventory.armorItemInSlot(2).getTagCompound().hasKey("hover") && ((EntityPlayer) entity).inventory.armorItemInSlot(2).getTagCompound().getByte("hover") == 1) {
         long currenttime = System.currentTimeMillis();
         long timeShock = 0L;
         if (this.timingShock.get(entity.getEntityId()) != null) {
            timeShock = this.timingShock.get(entity.getEntityId());
         }

         GlStateManager.pushMatrix();
         float mod = 0.0F;
         if (entity.isSneaking()) {
            GlStateManager.rotate(28.64789F, 1.0F, 0.0F, 0.0F);
            GlStateManager.translate(0.0F, 0.075F, -0.05F);
            mod = 0.075F;
         }

         GlStateManager.translate(0.0F, 0.2F, 0.55F);
         GlStateManager.pushMatrix();
         UtilsFX.renderQuadCenteredFromIcon(false, ((ItemHoverHarness)((EntityPlayer)entity).inventory.armorItemInSlot(2).getItem()).iconLightningRing, 2.5F, 1.0F, 1.0F, 1.0F, 230, 1, 1.0F);
         GlStateManager.popMatrix();
         GlStateManager.pushMatrix();
         GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
         GlStateManager.translate(0.0F, 0.0F, 0.03F);
         UtilsFX.renderQuadCenteredFromIcon(false, ((ItemHoverHarness)((EntityPlayer)entity).inventory.armorItemInSlot(2).getItem()).iconLightningRing, 1.5F, 1.0F, 0.5F, 1.0F, 230, 1, 1.0F);
         GlStateManager.popMatrix();
         GlStateManager.popMatrix();
         if (timeShock < currenttime) {
            timeShock = currenttime + 50L + (long)entity.world.rand.nextInt(50);
            this.timingShock.put(entity.getEntityId(), timeShock);
            RayTraceResult mop = BlockUtils.getTargetBlock(entity.world, entity.posX, entity.posY - (double)0.45F - (double)mod, entity.posZ, ((EntityPlayer)entity).renderYawOffset - 90.0F - (float)entity.world.rand.nextInt(180), (float)(-80 + entity.world.rand.nextInt(160)), false, 6.0F);
            if (mop != null) {
               double px = mop.hitVec.x;
               double py = mop.hitVec.y;
               double pz = mop.hitVec.z;
               FXLightningBolt bolt = new FXLightningBolt(entity.world, entity.posX - (double)(MathHelper.cos((((EntityPlayer)entity).renderYawOffset + 90.0F) / 180.0F * 3.141593F) * 0.5F), entity.posY - (double)0.45F - (double)mod, entity.posZ - (double)(MathHelper.sin((((EntityPlayer)entity).renderYawOffset + 90.0F) / 180.0F * 3.141593F) * 0.5F), px, py, pz, entity.world.rand.nextLong(), 1, 2.0F, 3);
               bolt.defaultFractal();
               bolt.setType(6);
               bolt.setWidth(0.015F);
               bolt.finalizeBolt();
            }
         }
      }

      GlStateManager.popMatrix();
   }
}
