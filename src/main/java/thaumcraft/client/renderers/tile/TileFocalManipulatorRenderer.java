package thaumcraft.client.renderers.tile;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.MathHelper;

import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.client.renderers.models.ModelArcaneWorkbench;
import thaumcraft.common.tiles.TileFocalManipulator;
import net.minecraft.client.renderer.GlStateManager;

@SideOnly(Side.CLIENT)
public class TileFocalManipulatorRenderer extends TileEntitySpecialRenderer<TileFocalManipulator> {
   private ModelArcaneWorkbench tableModel = new ModelArcaneWorkbench();
   EntityItem entityitem = null;

   @Override
   public void render(TileFocalManipulator table, double par2, double par4, double par6, float par8, int destroyStage, float alpha) {
      GlStateManager.pushMatrix();
      UtilsFX.bindTexture("textures/models/wandtable.png");
      GlStateManager.translate((float)par2 + 0.5F, (float)par4 + 1.0F, (float)par6 + 0.5F);
      GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      this.tableModel.renderAll();
      GlStateManager.popMatrix();
      if (table.getWorld() != null && !table.getStackInSlot(0).isEmpty() && table.getStackInSlot(0).getItem() instanceof ItemFocusBasic) {
         float ticks = (float)Minecraft.getMinecraft().getRenderViewEntity().ticksExisted + par8;
         GlStateManager.pushMatrix();
         GlStateManager.translate((float)par2 + 0.5F, (float)par4 + 1.0F, (float)par6 + 0.5F);
         GlStateManager.rotate(ticks % 360.0F, 0.0F, 1.0F, 0.0F);
         ItemStack is = table.getStackInSlot(0).copy();
         this.entityitem = new EntityItem(table.getWorld(), 0.0F, 0.0F, 0.0F, is);
         this.entityitem.hoverStart = MathHelper.sin(ticks / 14.0F) * 0.2F + 0.2F;
         GlStateManager.popMatrix();
      }

   }

}
