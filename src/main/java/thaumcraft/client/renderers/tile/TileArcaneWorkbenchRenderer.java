package thaumcraft.client.renderers.tile;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import thaumcraft.client.lib.UtilsFX;
import thaumcraft.client.renderers.models.ModelArcaneWorkbench;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumcraft.common.tiles.TileArcaneWorkbench;
import net.minecraft.client.renderer.GlStateManager;

@SideOnly(Side.CLIENT)
public class TileArcaneWorkbenchRenderer extends TileEntitySpecialRenderer<TileArcaneWorkbench> {
   private ModelArcaneWorkbench tableModel = new ModelArcaneWorkbench();

   @Override
   public void render(TileArcaneWorkbench table, double par2, double par4, double par6, float par8, int destroyStage, float alpha) {
      if (table == null){return;}
      GlStateManager.pushMatrix();
      UtilsFX.bindTexture("textures/models/worktable.png");
      GlStateManager.translate((float)par2 + 0.5F, (float)par4 + 1.0F, (float)par6 + 0.5F);
      GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      this.tableModel.renderAll();
      GlStateManager.popMatrix();
      if (table.getWorld() != null && !table.getStackInSlot(10).isEmpty() && table.getStackInSlot(10).getItem() instanceof ItemWandCasting) {
         GlStateManager.pushMatrix();
         GlStateManager.translate((float)par2 + 0.65F, (float)par4 + 1.0625F, (float)par6 + 0.25F);
         GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
         GlStateManager.rotate(20.0F, 0.0F, 0.0F, 1.0F);
         ItemStack is = table.getStackInSlot(10).copy();
         is.setCount(1);
         GlStateManager.scale(0.5F, 0.5F, 0.5F);
         net.minecraft.client.renderer.RenderHelper.enableStandardItemLighting();
         net.minecraft.client.Minecraft.getMinecraft().getRenderItem().renderItem(is, net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType.FIXED);
         net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
         GlStateManager.popMatrix();
      }

   }
}
