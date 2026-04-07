package thaumcraft.client.renderers.tile;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import thaumcraft.client.renderers.models.AdvancedModelLoader;
import thaumcraft.client.renderers.models.IModelCustom;
import net.minecraft.util.EnumFacing;

import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.client.renderers.models.ModelBoreBase;
import thaumcraft.common.tiles.TileAlembic;
import thaumcraft.common.tiles.TileTube;
import net.minecraft.client.renderer.GlStateManager;

@SideOnly(Side.CLIENT)
public class TileAlembicRenderer extends TileEntitySpecialRenderer<TileAlembic> {
   private IModelCustom model;
   private static final ResourceLocation ALEMBIC = new ResourceLocation("thaumcraft", "textures/models/alembic.obj");
   private ModelBoreBase modelBore;

   public TileAlembicRenderer() {
      this.model = AdvancedModelLoader.loadModel(ALEMBIC);
      this.modelBore = new ModelBoreBase();
   }

   @Override
   public void render(TileAlembic tile, double par2, double par4, double par6, float par8, int destroyStage, float alpha) {
      if (tile == null){return;}
      GlStateManager.pushMatrix();
      GlStateManager.translate((float)par2 + 0.5F, (float)par4, (float)par6 + 0.5F);
      GlStateManager.pushMatrix();
      GlStateManager.rotate(90.0F, -1.0F, 0.0F, 0.0F);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      UtilsFX.bindTexture("textures/models/alembic.png");
      int md = 0;
      if (tile.hasWorld()) {
         switch (tile.facing) {
            case 2:
               GlStateManager.rotate(270.0F, 0.0F, 0.0F, 1.0F);
               break;
            case 3:
               GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
            case 4:
            default:
               break;
            case 5:
               GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
         }

         if (tile.aboveFurnace) {
            this.model.renderPart("TubeMain");
            this.model.renderPart("Legs");
         } else if (tile.aboveAlembic) {
            this.model.renderPart("TubeMain");
            this.model.renderPart("TubeSmall");
         } else {
            this.model.renderPart("Legs");
         }
      } else {
         GlStateManager.translate(0.0F, 0.0F, -0.4F);
         this.model.renderPart("Legs");
      }

      this.model.renderPart("Pot");
      this.model.renderPart("Panel");
      GlStateManager.popMatrix();
      if (tile.aspectFilter != null) {
         GlStateManager.pushMatrix();
         GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
         switch (tile.facing) {
            case 2:
               GlStateManager.rotate(270.0F, 0.0F, 1.0F, 0.0F);
               break;
            case 3:
               GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
            case 4:
            default:
               break;
            case 5:
               GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
         }

         GlStateManager.pushMatrix();
         GlStateManager.translate(0.0F, 0.468F, -0.409F);
         UtilsFX.renderQuadCenteredFromTexture("textures/models/label.png", 0.27F, 1.0F, 1.0F, 1.0F, -99, 771, 1.0F);
         GlStateManager.popMatrix();
         GlStateManager.pushMatrix();
         GlStateManager.translate(0.0F, 0.468F, -0.41F);
         GlStateManager.scale(0.013, 0.013, 0.013);
         GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
         UtilsFX.drawTag(-8, -8, tile.aspectFilter);
         GlStateManager.popMatrix();
         GlStateManager.popMatrix();
      }

      GlStateManager.popMatrix();
      if (tile.getWorld() != null) {
         UtilsFX.bindTexture("textures/models/bore.png");

         for(EnumFacing dir : EnumFacing.values()) {
            if (tile.canOutputTo(dir)) {
               TileEntity te = ThaumcraftApiHelper.getConnectableTile(tile.getWorld(), tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ(), dir);
               if (te instanceof IEssentiaTransport && !(te instanceof TileTube)) {
                  GlStateManager.pushMatrix();
                  GlStateManager.translate((float)par2 + 0.5F, (float)par4, (float)par6 + 0.5F);
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

                  this.modelBore.renderNozzle();
                  GlStateManager.popMatrix();
               }
            }
         }
      }

   }
}
