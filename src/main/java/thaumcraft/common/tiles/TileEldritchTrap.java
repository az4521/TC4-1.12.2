package thaumcraft.common.tiles;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.fx.PacketFXBlockZap;

public class TileEldritchTrap extends TileEntity implements net.minecraft.util.ITickable {
   int count = 20;

   @Override
   public void update() { updateEntity(); }

   public void updateEntity() {
            if (!this.world.isRemote && this.count-- <= 0) {
         this.count = 10 + this.world.rand.nextInt(25);
         EntityPlayer p = this.world.getClosestPlayer((double)this.getPos().getX() + (double)0.5F, (double)this.getPos().getY() + (double)0.5F, (double)this.getPos().getZ() + (double)0.5F, 3.0F, false);
         if (p != null) {
            p.attackEntityFrom(DamageSource.MAGIC, 2.0F);
            if (this.world.rand.nextBoolean()) {
               Thaumcraft.addWarpToPlayer(p, 1 + this.world.rand.nextInt(2), true);
            }

            PacketHandler.INSTANCE.sendToAllAround(new PacketFXBlockZap((float)this.getPos().getX() + 0.5F, (float)this.getPos().getY() + 0.5F, (float)this.getPos().getZ() + 0.5F, (float)p.posX, (float)p.getEntityBoundingBox().minY + p.eyeHeight, (float)p.posZ), new NetworkRegistry.TargetPoint(this.world.provider.getDimension(), this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), 32.0F));
         }
      }

   }
}
