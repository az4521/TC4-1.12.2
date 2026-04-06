package thaumcraft.common.tiles;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.codechicken.lib.raytracer.RayTracer;
import net.minecraft.util.math.BlockPos;

public class TileTubeValve extends TileTube {
   public boolean allowFlow = true;
   boolean wasPoweredLastTick = false;
   public float rotation = 0.0F;

   public void updateEntity() {
      if (!this.world.isRemote && this.count % 5 == 0) {
         boolean gettingPower = this.gettingPower();
         if (this.wasPoweredLastTick && !gettingPower && !this.allowFlow) {
            this.allowFlow = true;
            this.world.playSound(null, this.getPos(), new net.minecraft.util.SoundEvent(new net.minecraft.util.ResourceLocation("thaumcraft", "squeek")), net.minecraft.util.SoundCategory.BLOCKS, 0.7F, 0.9F + this.world.rand.nextFloat() * 0.2F);
            { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
            this.markDirty();
         }

         if (!this.wasPoweredLastTick && gettingPower && this.allowFlow) {
            this.allowFlow = false;
            this.world.playSound(null, this.getPos(), new net.minecraft.util.SoundEvent(new net.minecraft.util.ResourceLocation("thaumcraft", "squeek")), net.minecraft.util.SoundCategory.BLOCKS, 0.7F, 0.9F + this.world.rand.nextFloat() * 0.2F);
            { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
            this.markDirty();
         }

         this.wasPoweredLastTick = gettingPower;
      }

      if (this.world.isRemote) {
         if (!this.allowFlow && this.rotation < 360.0F) {
            this.rotation += 20.0F;
         } else if (this.allowFlow && this.rotation > 0.0F) {
            this.rotation -= 20.0F;
         }
      }

         }

   public int onWandRightClick(World world, ItemStack wandstack, EntityPlayer player, int x, int y, int z, int side, int md) {
      RayTraceResult hit = RayTracer.retraceBlock(world, player, x, y, z);
       if (hit != null) {
           if (hit.subHit >= 0 && hit.subHit < 6) {
               player.world.playSound(null, new BlockPos(x, y, z), new net.minecraft.util.SoundEvent(new net.minecraft.util.ResourceLocation("thaumcraft", "tool")), net.minecraft.util.SoundCategory.BLOCKS, 0.5F, 0.9F + player.world.rand.nextFloat() * 0.2F);
               player.swingArm(net.minecraft.util.EnumHand.MAIN_HAND);
               this.markDirty();
               { net.minecraft.block.state.IBlockState _bs = world.getBlockState(new BlockPos(x, y, z)); world.notifyBlockUpdate(new BlockPos(x, y, z), _bs, _bs, 3); }
               this.openSides[hit.subHit] = !this.openSides[hit.subHit];
               EnumFacing dir = EnumFacing.byIndex(hit.subHit);
               TileEntity tile = this.world.getTileEntity(new BlockPos(this.getPos().getX() + dir.getXOffset(), this.getPos().getY() + dir.getYOffset(), this.getPos().getZ() + dir.getZOffset()));
               if (tile instanceof TileTube) {
                   ((TileTube) tile).openSides[dir.getOpposite().ordinal()] = this.openSides[hit.subHit];
                   { BlockPos _np = this.getPos().offset(dir); net.minecraft.block.state.IBlockState _bs = world.getBlockState(_np); world.notifyBlockUpdate(_np, _bs, _bs, 3); }
                   tile.markDirty();
               }
           }

           if (hit.subHit == 6) {
               player.world.playSound(null, new BlockPos(x, y, z), new net.minecraft.util.SoundEvent(new net.minecraft.util.ResourceLocation("thaumcraft", "tool")), net.minecraft.util.SoundCategory.BLOCKS, 0.5F, 0.9F + player.world.rand.nextFloat() * 0.2F);
               player.swingArm(net.minecraft.util.EnumHand.MAIN_HAND);
               int a = this.facing.ordinal();
               this.markDirty();

               while (true) {
                   ++a;
                   if (a >= 20) {
                       break;
                   }

                   if (!this.canConnectSide(EnumFacing.byIndex(a % 6).ordinal())) {
                       a %= 6;
                       this.facing = EnumFacing.byIndex(a);
                       { net.minecraft.block.state.IBlockState _bs = world.getBlockState(new BlockPos(x, y, z)); world.notifyBlockUpdate(new BlockPos(x, y, z), _bs, _bs, 3); }
                       break;
                   }
               }
           }

       }
       return 0;
   }

   public void readCustomNBT(NBTTagCompound nbttagcompound) {
      super.readCustomNBT(nbttagcompound);
      this.allowFlow = nbttagcompound.getBoolean("flow");
      this.wasPoweredLastTick = nbttagcompound.getBoolean("hadpower");
   }

   public void writeCustomNBT(NBTTagCompound nbttagcompound) {
      super.writeCustomNBT(nbttagcompound);
      nbttagcompound.setBoolean("flow", this.allowFlow);
      nbttagcompound.setBoolean("hadpower", this.wasPoweredLastTick);
   }

   public boolean isConnectable(EnumFacing face) {
      return face != this.facing && super.isConnectable(face);
   }

   public void setSuction(Aspect aspect, int amount) {
      if (this.allowFlow) {
         super.setSuction(aspect, amount);
      }

   }

   public boolean gettingPower() {
      return this.world.isBlockPowered(this.getPos());
   }
}
