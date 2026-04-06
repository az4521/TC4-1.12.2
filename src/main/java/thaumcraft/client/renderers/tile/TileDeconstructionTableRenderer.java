package thaumcraft.client.renderers.tile;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

import thaumcraft.client.lib.UtilsFX;
import thaumcraft.client.renderers.models.ModelArcaneWorkbench;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.tiles.TileDeconstructionTable;
import net.minecraft.client.renderer.GlStateManager;

@SideOnly(Side.CLIENT)
public class TileDeconstructionTableRenderer extends TileEntitySpecialRenderer<TileDeconstructionTable> {
   private ModelArcaneWorkbench tableModel = new ModelArcaneWorkbench();
   ItemStack tm;

   public TileDeconstructionTableRenderer() {
      this.tm = new ItemStack(ConfigItems.itemThaumometer);
   }

   @Override
   public void render(TileDeconstructionTable table, double par2, double par4, double par6, float par8, int destroyStage, float alpha) {
      GlStateManager.pushMatrix();
      UtilsFX.bindTexture("textures/models/decontable.png");
      GlStateManager.translate((float)par2 + 0.5F, (float)par4 + 1.0F, (float)par6 + 0.5F);
      GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      this.tableModel.renderAll();
      GlStateManager.popMatrix();
      GlStateManager.pushMatrix();
      GlStateManager.translate((float)par2 + 0.5F, (float)par4 + 0.92F, (float)par6 + 0.5F);
      GlStateManager.scale(0.8, 0.8, 0.8);
      net.minecraft.client.renderer.RenderHelper.enableStandardItemLighting();
      Minecraft.getMinecraft().getRenderItem().renderItem(this.tm, net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType.FIXED);
      net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
      GlStateManager.popMatrix();
      float ticks = (float)Minecraft.getMinecraft().getRenderViewEntity().ticksExisted + par8;
      if (table != null && table.getWorld() != null && !table.getStackInSlot(0).isEmpty()) {
         GlStateManager.pushMatrix();
         GlStateManager.translate((float)par2 + 0.5F, (float)par4 + 1.15F + MathHelper.sin(ticks / 14.0F) * 0.05F, (float)par6 + 0.5F);
         GlStateManager.rotate(ticks % 360.0F, 0.0F, 1.0F, 0.0F);
         GlStateManager.enableBlend();
         GlStateManager.blendFunc(770, 1);
         GlStateManager.color(1.0F, 1.0F, 1.0F, 0.75F);
         ItemStack is = table.getStackInSlot(0).copy();
         is.setCount(1);
         net.minecraft.client.renderer.RenderHelper.enableStandardItemLighting();
         Minecraft.getMinecraft().getRenderItem().renderItem(is, net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType.FIXED);
         net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
         GlStateManager.disableBlend();
         GlStateManager.popMatrix();
      }

      if (table != null && table.getWorld() != null && table.aspect != null) {
         GlStateManager.pushMatrix();
         GlStateManager.translate((float)par2 + 0.5F, (float)par4 + 1.081F, (float)par6 + 0.5F);
         GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
         GlStateManager.rotate(ticks % 360.0F, 0.0F, 0.0F, 1.0F);
         GlStateManager.scale(0.024, 0.024, 0.024);
         UtilsFX.drawTag(-8, -8, table.aspect, 0.0F, 0, 0.0F, 1, 0.8F, false);
         GlStateManager.popMatrix();
      }

   }

}
