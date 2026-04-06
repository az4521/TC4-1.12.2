package thaumcraft.client.renderers.tile;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

import tc4tweak.ConfigurationHandler;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.nodes.INode;
import thaumcraft.api.nodes.IRevealer;
import thaumcraft.api.nodes.NodeModifier;
import thaumcraft.api.nodes.NodeType;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.items.relics.ItemThaumometer;
import thaumcraft.common.tiles.TileJarNode;
import thaumcraft.common.tiles.TileNode;
import net.minecraft.client.renderer.GlStateManager;

@SideOnly(Side.CLIENT)
public class TileNodeRenderer extends TileEntitySpecialRenderer<TileEntity> {
   public static final ResourceLocation nodetex = new ResourceLocation("thaumcraft", "textures/misc/nodes.png");

   @Override
   public boolean isGlobalRenderer(TileEntity te) {
      return true;
   }

   public static void renderFacingStrip_tweaked(double px, double py, double pz, float angle, float scale, float alpha, int frames, int strip, int frame, float partialTicks, int color) {
      UtilsFX.renderFacingStrip(px, py, pz, angle,
              Math.min(scale, ConfigurationHandler.INSTANCE.getNodeVisualSizeLimit()),
              alpha, frames, strip, frame, partialTicks, color);
   }
   public static void renderNode(EntityLivingBase viewer, double viewDistance, boolean visible, boolean depthIgnore, float size, int x, int y, int z, float partialTicks, AspectList aspects, NodeType type, NodeModifier mod) {
      long nt = System.nanoTime();
      UtilsFX.bindTexture(nodetex);
      int frames = 32;
      if (aspects.size() > 0 && visible) {
         double distance = viewer.getDistance((double)x + (double)0.5F, (double)y + (double)0.5F, (double)z + (double)0.5F);
         if (distance > viewDistance) {
            return;
         }

         float alpha = (float)((viewDistance - distance) / viewDistance);
         if (mod != null) {
            switch (mod) {
               case BRIGHT:
                  alpha *= 1.5F;
                  break;
               case PALE:
                  alpha *= 0.66F;
                  break;
               case FADING:
                  alpha *= MathHelper.sin((float)viewer.ticksExisted / 3.0F) * 0.25F + 0.33F;
            }
         }

         GlStateManager.pushMatrix();
         GlStateManager.alphaFunc(516, 0.003921569F);
         if (depthIgnore) {
            GlStateManager.disableDepth();
            GlStateManager.depthMask(false);
         }

         GlStateManager.disableCull();
         long time = nt / 5000000L;
         float bscale = 0.25F;
         GlStateManager.pushMatrix();
         float rad = ((float)Math.PI * 2F);
         GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
         int i = (int)((nt / 40000000L + (long)x) % (long)frames);
         int count = 0;
         float scale = 0.0F;
         float angle = 0.0F;
         float average = 0.0F;

         for(Aspect aspect : aspects.getAspects()) {
            if (aspect.getBlend() == 771) {
               alpha = (float)((double)alpha * (double)1.5F);
            }

            average += (float)aspects.getAmount(aspect);
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(770, aspect.getBlend());
            scale = MathHelper.sin((float)viewer.ticksExisted / (14.0F - (float)count)) * bscale + bscale * 2.0F;
            scale = 0.2F + scale * ((float)aspects.getAmount(aspect) / 50.0F);
            scale *= size;
            angle = (float)(time % (5000 + 500L * count)) / (5000.0F + (float)(500 * count)) * rad;
//            UtilsFX.renderFacingStrip
            renderFacingStrip_tweaked
                    ((double)x + (double)0.5F, (double)y + (double)0.5F, (double)z + (double)0.5F, angle, scale, alpha / Math.max(1.0F, (float)aspects.size() / 2.0F), frames, 0, i, partialTicks, aspect.getColor());
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
            ++count;
            if (aspect.getBlend() == 771) {
               alpha = (float)((double)alpha / (double)1.5F);
            }
         }

         average /= (float)aspects.size();
         GlStateManager.pushMatrix();
         GlStateManager.enableBlend();
         i = (int)((nt / 40000000L + (long)x) % (long)frames);
         scale = 0.1F + average / 150.0F;
         scale *= size;
         int strip = 1;
         switch (type) {
            case NORMAL:
               GlStateManager.blendFunc(770, 1);
               break;
            case UNSTABLE:
               GlStateManager.blendFunc(770, 1);
               strip = 6;
               angle = 0.0F;
               break;
            case DARK:
               GlStateManager.blendFunc(770, 771);
               strip = 2;
               break;
            case TAINTED:
               GlStateManager.blendFunc(770, 771);
               strip = 5;
               break;
            case PURE:
               GlStateManager.blendFunc(770, 1);
               strip = 4;
               break;
            case HUNGRY:
               scale *= 0.75F;
               GlStateManager.blendFunc(770, 1);
               strip = 3;
         }

         GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
         renderFacingStrip_tweaked
                 ((double)x + (double)0.5F, (double)y + (double)0.5F, (double)z + (double)0.5F, angle, scale, alpha, frames, strip, i, partialTicks, 16777215);
         GlStateManager.disableBlend();
         GlStateManager.popMatrix();
         GlStateManager.popMatrix();
         GlStateManager.enableCull();
         if (depthIgnore) {
            GlStateManager.enableDepth();
         }

         GlStateManager.depthMask(true);
         GlStateManager.alphaFunc(516, 0.1F);
         GlStateManager.popMatrix();
      } else {
         GlStateManager.pushMatrix();
         GlStateManager.alphaFunc(516, 0.003921569F);
         GlStateManager.enableBlend();
         GlStateManager.blendFunc(770, 1);
         int i = (int)((nt / 40000000L + (long)x) % (long)frames);
         GlStateManager.color(1.0F, 1.0F, 1.0F, 0.15F);
         renderFacingStrip_tweaked
                 ((double)x + (double)0.5F, (double)y + (double)0.5F, (double)z + (double)0.5F, 0.0F, 0.5F, 0.15F, frames, 1, i, partialTicks, 16777215);
         GlStateManager.disableBlend();
         GlStateManager.alphaFunc(516, 0.1F);
         GlStateManager.popMatrix();
      }

   }

   @Override
   public void render(TileEntity tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
      if (tile instanceof INode) {
         float size = 1.0F;
         INode node = (INode)tile;
         double viewDistance = 64.0F;
         EntityLivingBase viewer = Minecraft.getMinecraft().player;
         boolean condition = false;
         boolean depthIgnore = false;
         if (viewer instanceof EntityPlayer) {
            if (tile != null && tile instanceof TileJarNode) {
               condition = true;
               size = 0.7F;
            } else if (!((EntityPlayer)viewer).inventory.armorInventory.get(3).isEmpty() && ((EntityPlayer)viewer).inventory.armorInventory.get(3).getItem() instanceof IRevealer && ((IRevealer)((EntityPlayer)viewer).inventory.armorInventory.get(3).getItem()).showNodes(((EntityPlayer)viewer).inventory.armorInventory.get(3), viewer)) {
               condition = true;
               depthIgnore = true;
            } else if (!((EntityPlayer)viewer).inventory.getCurrentItem().isEmpty() && ((EntityPlayer)viewer).inventory.getCurrentItem().getItem() instanceof ItemThaumometer && UtilsFX.isVisibleTo(0.44F, viewer, tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ())) {
               condition = true;
               depthIgnore = true;
               viewDistance = 48.0F;
            }
         }

         renderNode(viewer, viewDistance, condition, depthIgnore, size, tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ(), partialTicks, ((INode)tile).getAspects(), ((INode)tile).getNodeType(), ((INode)tile).getNodeModifier());
         if (tile instanceof TileNode && ((TileNode)tile).drainEntity != null && ((TileNode)tile).drainCollision != null) {
            Entity drainEntity = ((TileNode)tile).drainEntity;
            if (drainEntity instanceof EntityPlayer && !((EntityPlayer)drainEntity).isHandActive()) {
               ((TileNode)tile).drainEntity = null;
               ((TileNode)tile).drainCollision = null;
               return;
            }

            RayTraceResult drainCollision = ((TileNode)tile).drainCollision;
            GlStateManager.pushMatrix();
            float f10 = 0.0F;
            int iiud = ((EntityPlayer)drainEntity).getItemInUseCount();
            if (drainEntity instanceof EntityPlayer) {
               f10 = MathHelper.sin((float)iiud / 10.0F) * 10.0F;
            }

            Vec3d vec3 = new Vec3d(-0.1, -0.1, 0.5F);
            vec3 = vec3.rotatePitch(-(drainEntity.prevRotationPitch + (drainEntity.rotationPitch - drainEntity.prevRotationPitch) * partialTicks) * (float)Math.PI / 180.0F);
            vec3 = vec3.rotateYaw(-(drainEntity.prevRotationYaw + (drainEntity.rotationYaw - drainEntity.prevRotationYaw) * partialTicks) * (float)Math.PI / 180.0F);
            vec3 = vec3.rotateYaw(-f10 * 0.01F);
            vec3 = vec3.rotatePitch(-f10 * 0.015F);
            double d3 = drainEntity.prevPosX + (drainEntity.posX - drainEntity.prevPosX) * (double)partialTicks + vec3.x;
            double d4 = drainEntity.prevPosY + (drainEntity.posY - drainEntity.prevPosY) * (double)partialTicks + vec3.y;
            double d5 = drainEntity.prevPosZ + (drainEntity.posZ - drainEntity.prevPosZ) * (double)partialTicks + vec3.z;
            double d6 = drainEntity == Minecraft.getMinecraft().player ? (double)0.0F : (double)drainEntity.getEyeHeight();
            UtilsFX.drawFloatyLine(d3, d4 + d6, d5, (double)drainCollision.getBlockPos().getX() + (double)0.5F, (double)drainCollision.getBlockPos().getY() + (double)0.5F, (double)drainCollision.getBlockPos().getZ() + (double)0.5F, partialTicks, ((TileNode)tile).color.getRGB(), "textures/misc/wispy.png", -0.02F, (float)Math.min(iiud, 10) / 10.0F);
            GlStateManager.popMatrix();
         }
      }
   }
}
