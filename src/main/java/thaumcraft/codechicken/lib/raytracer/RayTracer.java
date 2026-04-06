package thaumcraft.codechicken.lib.raytracer;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import thaumcraft.codechicken.lib.math.MathHelper;
import thaumcraft.codechicken.lib.vec.BlockCoord;
import thaumcraft.codechicken.lib.vec.Cuboid6;
import thaumcraft.codechicken.lib.vec.Vector3;

public class RayTracer {
   private Vector3 vec = new Vector3();
   private Vector3 vec2 = new Vector3();
   private Vector3 s_vec = new Vector3();
   private double s_dist;
   private int s_side;
   private IndexedCuboid6 c_cuboid;
   private static final ThreadLocal<RayTracer> t_inst = new ThreadLocal<>();

   public static RayTracer instance() {
      RayTracer inst = t_inst.get();
      if (inst == null) {
         t_inst.set(inst = new RayTracer());
      }

      return inst;
   }

   private void traceSide(int side, Vector3 start, Vector3 end, Cuboid6 cuboid) {
      this.vec.set(start);
      Vector3 hit = null;
      switch (side) {
         case 0:
            hit = this.vec.XZintercept(end, cuboid.min.y);
            break;
         case 1:
            hit = this.vec.XZintercept(end, cuboid.max.y);
            break;
         case 2:
            hit = this.vec.XYintercept(end, cuboid.min.z);
            break;
         case 3:
            hit = this.vec.XYintercept(end, cuboid.max.z);
            break;
         case 4:
            hit = this.vec.YZintercept(end, cuboid.min.x);
            break;
         case 5:
            hit = this.vec.YZintercept(end, cuboid.max.x);
      }

      if (hit != null) {
         switch (side) {
            case 0:
            case 1:
               if (!MathHelper.between(cuboid.min.x, hit.x, cuboid.max.x) || !MathHelper.between(cuboid.min.z, hit.z, cuboid.max.z)) {
                  return;
               }
               break;
            case 2:
            case 3:
               if (!MathHelper.between(cuboid.min.x, hit.x, cuboid.max.x) || !MathHelper.between(cuboid.min.y, hit.y, cuboid.max.y)) {
                  return;
               }
               break;
            case 4:
            case 5:
               if (!MathHelper.between(cuboid.min.y, hit.y, cuboid.max.y) || !MathHelper.between(cuboid.min.z, hit.z, cuboid.max.z)) {
                  return;
               }
         }

         double dist = this.vec2.set(hit).subtract(start).magSquared();
         if (dist < this.s_dist) {
            this.s_side = side;
            this.s_dist = dist;
            this.s_vec.set(this.vec);
         }

      }
   }

   public RayTraceResult rayTraceCuboid(Vector3 start, Vector3 end, Cuboid6 cuboid) {
      this.s_dist = Double.MAX_VALUE;
      this.s_side = -1;

      for(int i = 0; i < 6; ++i) {
         this.traceSide(i, start, end, cuboid);
      }

      if (this.s_side < 0) {
         return null;
      } else {
         RayTraceResult mop = new RayTraceResult(RayTraceResult.Type.MISS, this.s_vec.toVec3D(), EnumFacing.byIndex(this.s_side), BlockPos.ORIGIN);
         return mop;
      }
   }

   public RayTraceResult rayTraceCuboids(Vector3 start, Vector3 end, List<IndexedCuboid6> cuboids) {
      double c_dist = Double.MAX_VALUE;
      RayTraceResult c_hit = null;

      for(IndexedCuboid6 cuboid : cuboids) {
         RayTraceResult mop = this.rayTraceCuboid(start, end, cuboid);
         if (mop != null && this.s_dist < c_dist) {
            c_dist = this.s_dist;
            c_hit = mop;
            this.c_cuboid = cuboid;
         }
      }

      return c_hit;
   }

   public RayTraceResult rayTraceCuboids(Vector3 start, Vector3 end, List<IndexedCuboid6> cuboids, BlockPos pos, Block block) {
      RayTraceResult mop = this.rayTraceCuboids(start, end, cuboids);
      if (mop != null) {
         mop = new RayTraceResult(RayTraceResult.Type.BLOCK, mop.hitVec, mop.sideHit, pos);
         if (block != null) {
            this.c_cuboid.add(new Vector3(-pos.getX(), -pos.getY(), -pos.getZ())).setBlockBounds(block);
         }
      }

      return mop;
   }


   public static RayTraceResult retraceBlock(World world, EntityPlayer player, int x, int y, int z) {
      BlockPos blockPos = new BlockPos(x, y, z);
      IBlockState state = world.getBlockState(blockPos);
      Block block = state.getBlock();
      Vec3d headVec = getCorrectedHeadVec(player);
      Vec3d lookVec = player.getLook(1.0F);
      double reach = getBlockReachDistance(player);
      Vec3d endVec = headVec.add(lookVec.x * reach,
              lookVec.y * reach,
              lookVec.z * reach);
      return block.collisionRayTrace(state, world, blockPos, headVec, endVec);
   }

   private static double getBlockReachDistance_server(EntityPlayerMP player) {
      return player.interactionManager.getBlockReachDistance();
   }

   @SideOnly(Side.CLIENT)
   private static double getBlockReachDistance_client() {
      return Minecraft.getMinecraft().playerController.getBlockReachDistance();
   }

   public static Vec3d getCorrectedHeadVec(EntityPlayer player) {
      double vx = player.posX;
      double vy = player.posY;
      double vz = player.posZ;
      if (player.world.isRemote) {
         vy += player.getEyeHeight() - player.getDefaultEyeHeight();
      } else {
         vy += player.getEyeHeight();
         if (player instanceof EntityPlayerMP && player.isSneaking()) {
            vy -= 0.08;
         }
      }
      return new Vec3d(vx, vy, vz);
   }

   public static double getBlockReachDistance(EntityPlayer player) {
      return player.world.isRemote ? getBlockReachDistance_client() : (player instanceof EntityPlayerMP ? getBlockReachDistance_server((EntityPlayerMP)player) : (double)5.0F);
   }

}
