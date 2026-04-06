package thaumcraft.common.tiles;

import net.minecraft.tileentity.TileEntity;
import thaumcraft.common.Thaumcraft;

public class TileNitor extends TileEntity implements net.minecraft.util.ITickable {
   @Override
   public void update() { updateEntity(); }

   public void updateEntity() {
            if (this.world.isRemote) {
         if (this.world.rand.nextInt(9 - Thaumcraft.proxy.particleCount(2)) == 0) {
            Thaumcraft.proxy.wispFX3(this.world, (float)this.getPos().getX() + 0.5F, (float)this.getPos().getY() + 0.5F, (float)this.getPos().getZ() + 0.5F, (float)this.getPos().getX() + 0.3F + this.world.rand.nextFloat() * 0.4F, (float)this.getPos().getY() + 0.5F, (float)this.getPos().getZ() + 0.3F + this.world.rand.nextFloat() * 0.4F, 0.5F, 4, true, -0.025F);
         }

         if (this.world.rand.nextInt(15 - Thaumcraft.proxy.particleCount(4)) == 0) {
            Thaumcraft.proxy.wispFX3(this.world, (float)this.getPos().getX() + 0.5F, (float)this.getPos().getY() + 0.5F, (float)this.getPos().getZ() + 0.5F, (float)this.getPos().getX() + 0.4F + this.world.rand.nextFloat() * 0.2F, (float)this.getPos().getY() + 0.5F, (float)this.getPos().getZ() + 0.4F + this.world.rand.nextFloat() * 0.2F, 0.25F, 1, true, -0.02F);
         }
      }

   }
}
