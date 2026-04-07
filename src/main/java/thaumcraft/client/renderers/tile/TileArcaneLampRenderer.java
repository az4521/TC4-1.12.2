package thaumcraft.client.renderers.tile;

import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import thaumcraft.client.lib.UtilsFX;
import thaumcraft.client.renderers.models.ModelBoreBase;
import thaumcraft.common.tiles.TileArcaneBoreBase;
import thaumcraft.common.tiles.TileArcaneLamp;
import thaumcraft.common.tiles.TileArcaneLampFertility;
import thaumcraft.common.tiles.TileArcaneLampGrowth;
import net.minecraft.client.renderer.GlStateManager;

public class TileArcaneLampRenderer extends TileEntitySpecialRenderer<TileEntity> {
   private ModelBoreBase model = new ModelBoreBase();

   @Override


   public void render(TileEntity tileentity, double x, double y, double z, float f, int destroyStage, float alpha) {
      if (tileentity == null){return;}
      Minecraft mc = FMLClientHandler.instance().getClient();
      if (tileentity.getWorld() != null) {
         EnumFacing dir = EnumFacing.DOWN;
         if (tileentity instanceof TileArcaneLamp) {
            dir = ((TileArcaneLamp)tileentity).facing;
         } else if (tileentity instanceof TileArcaneLampGrowth) {
            dir = ((TileArcaneLampGrowth)tileentity).facing;
         } else if (tileentity instanceof TileArcaneLampFertility) {
            dir = ((TileArcaneLampFertility)tileentity).facing;
         }

         GlStateManager.pushMatrix();
         UtilsFX.bindTexture("textures/models/bore.png");
         if (tileentity.getWorld().getTileEntity(tileentity.getPos().offset(dir)) instanceof TileArcaneBoreBase) {
            GlStateManager.pushMatrix();
            GlStateManager.translate((float)x + 0.5F + (float)dir.getXOffset(), (float)y + (float)dir.getYOffset(), (float)z + 0.5F + (float)dir.getZOffset());
            switch (dir.getOpposite().ordinal()) {
               case 0:
                  GlStateManager.translate(-0.5F, 0.5F, 0.0F);
                  GlStateManager.rotate(90.0F, 0.0F, 0.0F, -1.0F);
                  break;
               case 1:
                  GlStateManager.translate(0.5F, 0.5F, 0.0F);
                  GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
                  break;
               case 2:
                  GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
                  break;
               case 3:
                  GlStateManager.rotate(270.0F, 0.0F, 1.0F, 0.0F);
                  break;
               case 4:
                  GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                  break;
               case 5:
                  GlStateManager.rotate(0.0F, 0.0F, 1.0F, 0.0F);
            }

            this.model.renderNozzle();
            GlStateManager.popMatrix();
         }

         GlStateManager.translate((float)x + 0.5F, (float)y, (float)z + 0.5F);
         GlStateManager.pushMatrix();
         switch (dir.ordinal()) {
            case 0:
               GlStateManager.translate(-0.5F, 0.5F, 0.0F);
               GlStateManager.rotate(90.0F, 0.0F, 0.0F, -1.0F);
               break;
            case 1:
               GlStateManager.translate(0.5F, 0.5F, 0.0F);
               GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
               break;
            case 2:
               GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
               break;
            case 3:
               GlStateManager.rotate(270.0F, 0.0F, 1.0F, 0.0F);
               break;
            case 4:
               GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
               break;
            case 5:
               GlStateManager.rotate(0.0F, 0.0F, 1.0F, 0.0F);
         }

         this.model.renderNozzle();
         GlStateManager.popMatrix();
         GlStateManager.popMatrix();
      }

   }
}
