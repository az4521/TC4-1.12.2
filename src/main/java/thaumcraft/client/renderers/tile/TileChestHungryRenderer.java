package thaumcraft.client.renderers.tile;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelChest;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.tiles.TileChestHungry;

import static thaumcraft.client.renderers.tile.TileBlockInfoGetter.getBlockMetaSafely;
import net.minecraft.client.renderer.GlStateManager;

@SideOnly(Side.CLIENT)
public class TileChestHungryRenderer extends TileEntitySpecialRenderer<TileEntity> {
   private static final ModelChest chestModel = new ModelChest();

   public void renderTileEntityChestAt(TileChestHungry chest, double par2, double par4, double par6, float par8) {
      int direction = getBlockMetaSafely(chest);
      if (direction == -1){
         direction = 0;
      }


      ModelChest var14 = chestModel;
      UtilsFX.bindTexture("textures/models/chesthungry.png");
      GlStateManager.pushMatrix();
      GlStateManager.enableRescaleNormal();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.translate((float)par2, (float)par4 + 1.0F, (float)par6 + 1.0F);
      GlStateManager.scale(1.0F, -1.0F, -1.0F);
      GlStateManager.translate(0.5F, 0.5F, 0.5F);
      short rotateDegree = 0;
      if (direction == 4) {
         rotateDegree = 90;
      }
      if (direction == 5) {
         rotateDegree = -90;
      }
      if (direction == 2) {
         rotateDegree = 180;
      }

      GlStateManager.rotate(rotateDegree, 0.0F, 1.0F, 0.0F);
      GlStateManager.translate(-0.5F, -0.5F, -0.5F);
      float var12 = chest.prevLidAngle + (chest.lidAngle - chest.prevLidAngle) * par8;
      var12 = 1.0F - var12;
      var12 = 1.0F - var12 * var12 * var12;
      var14.chestLid.rotateAngleX = -(var12 * (float)Math.PI / 2.0F);
      var14.renderAll();
      GlStateManager.disableRescaleNormal();
      GlStateManager.popMatrix();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
   }

   @Override


   public void render(TileEntity tileEntity, double par2, double par4, double par6, float par8, int destroyStage, float alpha) {
      if (!(tileEntity instanceof TileChestHungry)){
         System.out.println("TileChestHungryRenderer.renderTileEntityAt:" + tileEntity.getClass().getName());
         return;
      }
      this.renderTileEntityChestAt((TileChestHungry)tileEntity, par2, par4, par6, par8);
   }
}
