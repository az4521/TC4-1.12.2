package thaumcraft.common.lib.utils;

import net.minecraftforge.fml.relauncher.ReflectionHelper;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketBlockBreakAnim;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.event.ForgeEventFactory;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.entities.EntityFollowingItem;
import net.minecraft.util.math.BlockPos;

public class BlockUtils {
   static HashMap<Integer,ArrayList[]> blockEventCache = new HashMap<>();
   static int lastx = 0;
   static int lasty = 0;
   static int lastz = 0;
   static double lastdistance = 0.0F;

   public static boolean harvestBlock(World world, EntityPlayer player, int x, int y, int z) {
      return harvestBlock(world, player, x, y, z, false, 0);
   }

   public static boolean harvestBlock(World world, EntityPlayer player, int x, int y, int z, boolean followItem, int color) {
      Block block = world.getBlockState(new BlockPos(x, y, z)).getBlock();
      int i1 =
        world.getBlockState(new net.minecraft.util.math.BlockPos(x, y, z)).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(x, y, z)));
      if (world.getBlockState(new BlockPos(x, y, z)).getBlockHardness(world, new BlockPos(x, y, z)) < 0.0F) {
         return false;
      } else {
         world.playEvent(2001, new BlockPos(x, y, z), Block.getIdFromBlock(block) + (i1 << 12));
         boolean flag = false;
         if (player.capabilities.isCreativeMode) {
            removeBlock(world, x, y, z, player);
         } else {
            boolean flag1 = false;
            if (block != null) {
               flag1 = block.canHarvestBlock(world, new BlockPos(x, y, z), player);
            }

            flag = removeBlock(world, x, y, z, player);
            if (flag && flag1) {
               block.harvestBlock(world, player, new BlockPos(x, y, z), world.getBlockState(new BlockPos(x, y, z)), world.getTileEntity(new BlockPos(x, y, z)), player.getHeldItemMainhand());
               if (followItem) {
                  ArrayList<Entity> entities = EntityUtils.getEntitiesInRange(world, (double)x + (double)0.5F, (double)y + (double)0.5F, (double)z + (double)0.5F, player, EntityItem.class, 2.0F);
                  if (entities != null && !entities.isEmpty()) {
                     for(Entity e : entities) {
                        if (!e.isDead && e instanceof EntityItem && e.ticksExisted == 0 && !(e instanceof EntityFollowingItem)) {
                           EntityFollowingItem fi = new EntityFollowingItem(world, e.posX, e.posY, e.posZ, ((EntityItem)e).getItem().copy(), player, color);
                           fi.motionX = e.motionX;
                           fi.motionY = e.motionY;
                           fi.motionZ = e.motionZ;
                           world.spawnEntity(fi);
                           e.setDead();
                        }
                     }
                  }
               }
            }
         }

         return true;
      }
   }

   public static ArrayList[] getBlockEventList(WorldServer world) {
      if (!blockEventCache.containsKey(world.provider.getDimension())) {
         try {
            blockEventCache.put(world.provider.getDimension(),
                    ReflectionHelper.getPrivateValue(WorldServer.class, world, new String[]{"blockEventQueue"}));
         } catch (Exception var2) {
            return null;
         }
      }

      return blockEventCache.get(world.provider.getDimension());
   }

   public static ItemStack createStackedBlock(Block block, int md) {
      return new ItemStack(block, 1, md);
   }

   public static void dropBlockAsItem(World world, int x, int y, int z, ItemStack stack, Block block) {
      Block.spawnAsEntity(world, new BlockPos(x, y, z), stack);
   }

   public static void dropBlockAsItemWithChance(World world, Block block, int x, int y, int z, int meta, float dropchance, int fortune, EntityPlayer player) {
      if (!world.isRemote && !world.restoringBlockSnapshots) {
         ArrayList<ItemStack> items = new ArrayList<>(block.getDrops(world, new BlockPos(x, y, z), world.getBlockState(new BlockPos(x, y, z)), fortune));
         dropchance = ForgeEventFactory.fireBlockHarvesting(items, world, new BlockPos(x, y, z), world.getBlockState(new BlockPos(x, y, z)), fortune, dropchance, false, player);

         for(ItemStack item : items) {
            if (world.rand.nextFloat() <= dropchance) {
               dropBlockAsItem(world, x, y, z, item, block);
            }
         }
      }

   }

   public static void destroyBlockPartially(World world, int par1, int par2, int par3, int par4, int par5) {
      net.minecraft.world.WorldServer ws = (net.minecraft.world.WorldServer)world;
      for(EntityPlayerMP entityplayermp : ws.getMinecraftServer().getPlayerList().getPlayers()) {
         if (entityplayermp != null && entityplayermp.world == world && entityplayermp.getEntityId() != par1) {
            double d0 = (double)par2 - entityplayermp.posX;
            double d1 = (double)par3 - entityplayermp.posY;
            double d2 = (double)par4 - entityplayermp.posZ;
            if (d0 * d0 + d1 * d1 + d2 * d2 < (double)1024.0F) {
               entityplayermp.connection.sendPacket(new SPacketBlockBreakAnim(par1, new BlockPos(par2, par3, par4), par5));
            }
         }
      }

   }

   public static boolean removeBlock(World world, int par1, int par2, int par3, EntityPlayer player) {
      Block block = world.getBlockState(new BlockPos(par1, par2, par3)).getBlock();
      if (block != null) {
         block.onBlockHarvested(world, new BlockPos(par1, par2, par3), world.getBlockState(new BlockPos(par1, par2, par3)), player);
      }

      boolean flag = block != null && block.removedByPlayer(world.getBlockState(new BlockPos(par1, par2, par3)), world, new BlockPos(par1, par2, par3), player, true);
      if (block != null && flag) {
         block.onPlayerDestroy(world, new BlockPos(par1, par2, par3), world.getBlockState(new BlockPos(par1, par2, par3)));
      }

      return flag;
   }

   public static void findBlocks(World world, int x, int y, int z, Block block) {
      int count = 0;

      for(int xx = -2; xx <= 2; ++xx) {
         for(int yy = 2; yy >= -2; --yy) {
            for(int zz = -2; zz <= 2; ++zz) {
               if (Math.abs(lastx + xx - x) > 24) {
                  return;
               }

               if (Math.abs(lasty + yy - y) > 48) {
                  return;
               }

               if (Math.abs(lastz + zz - z) > 24) {
                  return;
               }

               if (world.getBlockState(new net.minecraft.util.math.BlockPos(lastx + xx, lasty + yy, lastz + zz)).getBlock() == block && Utils.isWoodLog(world, lastx + xx, lasty + yy, lastz + zz) && world.getBlockState(new BlockPos(lastx + xx, lasty + yy, lastz + zz)).getBlockHardness(world, new BlockPos(lastx + xx, lasty + yy, lastz + zz)) >= 0.0F) {
                  double xd = lastx + xx - x;
                  double yd = lasty + yy - y;
                  double zd = lastz + zz - z;
                  double d = xd * xd + yd * yd + zd * zd;
                  if (d > lastdistance) {
                     lastdistance = d;
                     lastx += xx;
                     lasty += yy;
                     lastz += zz;
                     findBlocks(world, x, y, z, block);
                     return;
                  }
               }
            }
         }
      }

   }

   public static boolean breakFurthestBlock(World world, int x, int y, int z, Block block, EntityPlayer player) {
      return breakFurthestBlock(world, x, y, z, block, player, false, 0);
   }

   public static boolean breakFurthestBlock(World world, int x, int y, int z, Block block, EntityPlayer player, boolean followitem, int color) {
      lastx = x;
      lasty = y;
      lastz = z;
      lastdistance = 0.0F;
      findBlocks(world, x, y, z, block);
      boolean worked = harvestBlock(world, player, lastx, lasty, lastz, followitem, color);
      { net.minecraft.block.state.IBlockState _bs = world.getBlockState(new BlockPos(x, y, z)); world.notifyBlockUpdate(new BlockPos(x, y, z), _bs, _bs, 3); }
      if (worked) {
         { BlockPos _lp = new BlockPos(lastx, lasty, lastz); net.minecraft.block.state.IBlockState _bs = world.getBlockState(_lp); world.notifyBlockUpdate(_lp, _bs, _bs, 3); }

         for(int xx = -3; xx <= 3; ++xx) {
            for(int yy = -3; yy <= 3; ++yy) {
               for(int zz = -3; zz <= 3; ++zz) {
                  world.scheduleUpdate(new BlockPos(lastx + xx, lasty + yy, lastz + zz), world.getBlockState(new BlockPos(lastx + xx, lasty + yy, lastz + zz)).getBlock(), 150 + world.rand.nextInt(150));
               }
            }
         }
      }

      return worked;
   }

   public static RayTraceResult getTargetBlock(World world, double x, double y, double z, float yaw, float pitch, boolean par3, double range) {
      Vec3d var13 = new Vec3d(x, y, z);
      float var14 = MathHelper.cos(-yaw * ((float)Math.PI / 180F) - (float)Math.PI);
      float var15 = MathHelper.sin(-yaw * ((float)Math.PI / 180F) - (float)Math.PI);
      float var16 = -MathHelper.cos(-pitch * ((float)Math.PI / 180F));
      float var17 = MathHelper.sin(-pitch * ((float)Math.PI / 180F));
      float var18 = var15 * var16;
      float var20 = var14 * var16;
      Vec3d var23 = var13.add((double)var18 * range, (double)var17 * range, (double)var20 * range);
      return world.rayTraceBlocks(var13, var23, par3, !par3, false);
   }

   public static RayTraceResult getTargetBlock(World world, Entity entity, boolean par3) {
      float var4 = 1.0F;
      float var5 = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * var4;
      float var6 = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * var4;
      double var7 = entity.prevPosX + (entity.posX - entity.prevPosX) * (double)var4;
      double var9 = entity.prevPosY + (entity.posY - entity.prevPosY) * (double)var4 + (double)entity.getEyeHeight();
      double var11 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double)var4;
      Vec3d var13 = new Vec3d(var7, var9, var11);
      float var14 = MathHelper.cos(-var6 * ((float)Math.PI / 180F) - (float)Math.PI);
      float var15 = MathHelper.sin(-var6 * ((float)Math.PI / 180F) - (float)Math.PI);
      float var16 = -MathHelper.cos(-var5 * ((float)Math.PI / 180F));
      float var17 = MathHelper.sin(-var5 * ((float)Math.PI / 180F));
      float var18 = var15 * var16;
      float var20 = var14 * var16;
      double var21 = 10.0F;
      Vec3d var23 = var13.add((double)var18 * var21, (double)var17 * var21, (double)var20 * var21);
      return world.rayTraceBlocks(var13, var23, par3, !par3, false);
   }

   public static boolean isBlockAdjacentToAtleast(IBlockAccess world, int x, int y, int z, Block id, int md, int amount) {
      return isBlockAdjacentToAtleast(world, x, y, z, id, md, amount, 1);
   }

   public static boolean isBlockAdjacentToAtleast(IBlockAccess world, int x, int y, int z, Block id, int md, int amount, int range) {
      int count = 0;

      for(int xx = -range; xx <= range; ++xx) {
         for(int yy = -range; yy <= range; ++yy) {
            for(int zz = -range; zz <= range; ++zz) {
               if (xx != 0 || yy != 0 || zz != 0) {
                  if (world.getBlockState(new net.minecraft.util.math.BlockPos(x + xx, y + yy, z + zz)).getBlock() == id && (md == 32767 ||
        world.getBlockState(new net.minecraft.util.math.BlockPos(x + xx, y + yy, z + zz)).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(x + xx, y + yy, z + zz))) == md)) {
                     ++count;
                  }

                  if (count >= amount) {
                     return true;
                  }
               }
            }
         }
      }

      return count >= amount;
   }

   public static List<EntityItem> getContentsOfBlock(World world, int x, int y, int z) {
       return (List<EntityItem>)world.getEntitiesWithinAABB(
               EntityItem.class,
               new AxisAlignedBB(x, y, z, (double)x + (double)1.0F, (double)y + (double)1.0F, (double)z + (double)1.0F)
       );
   }

   public static int countExposedSides(World world, int x, int y, int z) {
      int count = 0;

      for(EnumFacing dir : EnumFacing.VALUES) {
         if (world.isAirBlock(new BlockPos(x + dir.getXOffset(), y + dir.getYOffset(), z + dir.getZOffset()))) {
            ++count;
         }
      }

      return count;
   }

   public static boolean isBlockExposed(World world, int x, int y, int z) {
      return !world.getBlockState(new net.minecraft.util.math.BlockPos(x, y, z + 1)).isOpaqueCube() || !world.getBlockState(new net.minecraft.util.math.BlockPos(x, y, z - 1)).isOpaqueCube() || !world.getBlockState(new net.minecraft.util.math.BlockPos(x + 1, y, z)).isOpaqueCube() || !world.getBlockState(new net.minecraft.util.math.BlockPos(x - 1, y, z)).isOpaqueCube() || !world.getBlockState(new net.minecraft.util.math.BlockPos(x, y + 1, z)).isOpaqueCube() || !world.getBlockState(new net.minecraft.util.math.BlockPos(x, y - 1, z)).isOpaqueCube();
   }

   public static boolean isAdjacentToSolidBlock(World world, int x, int y, int z) {
      for(int a = 0; a < 6; ++a) {
         EnumFacing d = EnumFacing.byIndex(a);
         if (world.isSideSolid(new BlockPos(x + d.getXOffset(), y + d.getYOffset(), z + d.getZOffset()), d.getOpposite(), false)) {
            return true;
         }
      }

      return false;
   }

   public static boolean isBlockTouching(IBlockAccess world, int x, int y, int z, Block id) {
      return world.getBlockState(new net.minecraft.util.math.BlockPos(x, y, z + 1)).getBlock() == id || world.getBlockState(new net.minecraft.util.math.BlockPos(x, y, z - 1)).getBlock() == id || world.getBlockState(new net.minecraft.util.math.BlockPos(x + 1, y, z)).getBlock() == id || world.getBlockState(new net.minecraft.util.math.BlockPos(x - 1, y, z)).getBlock() == id || world.getBlockState(new net.minecraft.util.math.BlockPos(x, y + 1, z)).getBlock() == id || world.getBlockState(new net.minecraft.util.math.BlockPos(x, y - 1, z)).getBlock() == id;
   }

   public static boolean isBlockTouching(IBlockAccess world, int x, int y, int z, Block id, int md) {
      return world.getBlockState(new net.minecraft.util.math.BlockPos(x, y, z + 1)).getBlock() == id &&
        world.getBlockState(new net.minecraft.util.math.BlockPos(x, y, z + 1)).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(x, y, z + 1))) == md || world.getBlockState(new net.minecraft.util.math.BlockPos(x, y, z - 1)).getBlock() == id && world.getBlockState(new net.minecraft.util.math.BlockPos(x, y, z - 1)).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(x, y, z - 1))) == md || world.getBlockState(new net.minecraft.util.math.BlockPos(x + 1, y, z)).getBlock() == id && world.getBlockState(new net.minecraft.util.math.BlockPos(x + 1, y, z)).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(x + 1, y, z))) == md || world.getBlockState(new net.minecraft.util.math.BlockPos(x - 1, y, z)).getBlock() == id && world.getBlockState(new net.minecraft.util.math.BlockPos(x - 1, y, z)).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(x - 1, y, z))) == md || world.getBlockState(new net.minecraft.util.math.BlockPos(x, y + 1, z)).getBlock() == id && world.getBlockState(new net.minecraft.util.math.BlockPos(x, y + 1, z)).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(x, y + 1, z))) == md || world.getBlockState(new net.minecraft.util.math.BlockPos(x, y - 1, z)).getBlock() == id && world.getBlockState(new net.minecraft.util.math.BlockPos(x, y - 1, z)).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(x, y - 1, z))) == md;
   }

   public static boolean isBlockTouchingOnSide(IBlockAccess world, int x, int y, int z, Block id, int md, int side) {
      if ((side <= 3 || world.getBlockState(new net.minecraft.util.math.BlockPos(x, y, z + 1)).getBlock() != id ||
        world.getBlockState(new net.minecraft.util.math.BlockPos(x, y, z + 1)).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(x, y, z + 1))) != md) && (side <= 3 || world.getBlockState(new net.minecraft.util.math.BlockPos(x, y, z - 1)).getBlock() != id || world.getBlockState(new net.minecraft.util.math.BlockPos(x, y, z - 1)).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(x, y, z - 1))) != md) && (side <= 1 || side >= 4 || world.getBlockState(new net.minecraft.util.math.BlockPos(x + 1, y, z)).getBlock() != id || world.getBlockState(new net.minecraft.util.math.BlockPos(x + 1, y, z)).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(x + 1, y, z))) != md) && (side <= 1 || side >= 4 || world.getBlockState(new net.minecraft.util.math.BlockPos(x - 1, y, z)).getBlock() != id || world.getBlockState(new net.minecraft.util.math.BlockPos(x - 1, y, z)).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(x - 1, y, z))) != md) && (side <= 1 || world.getBlockState(new net.minecraft.util.math.BlockPos(x, y + 1, z)).getBlock() != id || world.getBlockState(new net.minecraft.util.math.BlockPos(x, y + 1, z)).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(x, y + 1, z))) != md) && (side <= 1 || world.getBlockState(new net.minecraft.util.math.BlockPos(x, y - 1, z)).getBlock() != id || world.getBlockState(new net.minecraft.util.math.BlockPos(x, y - 1, z)).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(x, y - 1, z))) != md)) {
         if ((side <= 3 || world.getBlockState(new net.minecraft.util.math.BlockPos(x, y + 1, z + 1)).getBlock() != id || world.getBlockState(new net.minecraft.util.math.BlockPos(x, y + 1, z + 1)).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(x, y + 1, z + 1))) != md) && (side <= 3 || world.getBlockState(new net.minecraft.util.math.BlockPos(x, y + 1, z - 1)).getBlock() != id || world.getBlockState(new net.minecraft.util.math.BlockPos(x, y + 1, z - 1)).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(x, y + 1, z - 1))) != md) && (side <= 1 || side >= 4 || world.getBlockState(new net.minecraft.util.math.BlockPos(x + 1, y + 1, z)).getBlock() != id || world.getBlockState(new net.minecraft.util.math.BlockPos(x + 1, y + 1, z)).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(x + 1, y + 1, z))) != md) && (side <= 1 || side >= 4 || world.getBlockState(new net.minecraft.util.math.BlockPos(x - 1, y + 1, z)).getBlock() != id || world.getBlockState(new net.minecraft.util.math.BlockPos(x - 1, y + 1, z)).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(x - 1, y + 1, z))) != md)) {
            if ((side <= 3 || world.getBlockState(new net.minecraft.util.math.BlockPos(x, y - 1, z + 1)).getBlock() != id || world.getBlockState(new net.minecraft.util.math.BlockPos(x, y - 1, z + 1)).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(x, y - 1, z + 1))) != md) && (side <= 3 || world.getBlockState(new net.minecraft.util.math.BlockPos(x, y - 1, z - 1)).getBlock() != id || world.getBlockState(new net.minecraft.util.math.BlockPos(x, y - 1, z - 1)).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(x, y - 1, z - 1))) != md) && (side <= 1 || side >= 4 || world.getBlockState(new net.minecraft.util.math.BlockPos(x + 1, y - 1, z)).getBlock() != id || world.getBlockState(new net.minecraft.util.math.BlockPos(x + 1, y - 1, z)).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(x + 1, y - 1, z))) != md) && (side <= 1 || side >= 4 || world.getBlockState(new net.minecraft.util.math.BlockPos(x - 1, y - 1, z)).getBlock() != id || world.getBlockState(new net.minecraft.util.math.BlockPos(x - 1, y - 1, z)).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(x - 1, y - 1, z))) != md)) {
               switch (side) {
                  case 0:
                     if (world.getBlockState(new net.minecraft.util.math.BlockPos(x, y - 1, z)).getBlock() == id && world.getBlockState(new net.minecraft.util.math.BlockPos(x, y - 1, z)).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(x, y - 1, z))) == md) {
                        return true;
                     }
                     break;
                  case 1:
                     if (world.getBlockState(new net.minecraft.util.math.BlockPos(x, y + 1, z)).getBlock() == id &&
        world.getBlockState(new net.minecraft.util.math.BlockPos(x, y + 1, z)).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(x, y + 1, z))) == md) {
                        return true;
                     }
               }

               return false;
            } else {
               return true;
            }
         } else {
            return true;
         }
      } else {
         return true;
      }
   }
}
