package thaumcraft.common.tiles;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.Random;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.ResourceLocation;
import thaumcraft.api.TileThaumcraft;

public class TileJar extends TileThaumcraft {
   protected static Random rand = new Random();
   ResourceLocation texture = new ResourceLocation("thaumcraft", "textures/models/jar.png");

   @SideOnly(Side.CLIENT)
   public AxisAlignedBB getRenderBoundingBox() {
      return new AxisAlignedBB(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), this.getPos().getX() + 1, this.getPos().getY() + 1, this.getPos().getZ() + 1);
   }

   public void updateEntity() {
         }

   public ResourceLocation getTexture() {
      return this.texture;
   }
}
