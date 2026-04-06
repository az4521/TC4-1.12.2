package thaumcraft.common.tiles;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.EnumDifficulty;
import thaumcraft.api.TileThaumcraft;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.entities.monster.EntityTaintacle;
import thaumcraft.common.entities.monster.boss.EntityCultistPortal;
import thaumcraft.common.entities.monster.boss.EntityEldritchGolem;
import thaumcraft.common.entities.monster.boss.EntityEldritchWarden;
import thaumcraft.common.entities.monster.boss.EntityTaintacleGiant;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.fx.PacketFXBlockSparkle;
import thaumcraft.common.lib.utils.BlockUtils;
import thaumcraft.common.lib.utils.EntityUtils;
import thaumcraft.common.lib.utils.Utils;
import thaumcraft.common.lib.world.ThaumcraftWorldGenerator;
import thaumcraft.common.lib.world.dim.Cell;
import thaumcraft.common.lib.world.dim.CellLoc;
import thaumcraft.common.lib.world.dim.GenCommon;
import thaumcraft.common.lib.world.dim.MapBossData;
import thaumcraft.common.lib.world.dim.MazeHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.storage.MapStorage;

public class TileEldritchLock extends TileThaumcraft {
   public int count = -1;
   int[][] ped = new int[][]{{2, 2, 2}, {0, -1, 1}, {3, 3, 3}};
   byte facing = 0;

   public void updateEntity() {
            if (this.count != -1) {
         ++this.count;
         if (this.count % 5 == 0) {
            this.world.playSound(null, this.getPos(), new net.minecraft.util.SoundEvent(new net.minecraft.util.ResourceLocation("thaumcraft", "pump")), net.minecraft.util.SoundCategory.BLOCKS, 1.0F, 1.0F);
         }

         if (this.count > 100) {
            this.doBossSpawn();
         }
      }

   }

   private void doBossSpawn() {
      this.world.playSound(null, this.getPos(), new net.minecraft.util.SoundEvent(new net.minecraft.util.ResourceLocation("thaumcraft", "ice")), net.minecraft.util.SoundCategory.BLOCKS, 1.0F, 1.0F);
      if (!this.world.isRemote) {
         int cx = this.getPos().getX() >> 4;
         int cz = this.getPos().getZ() >> 4;
         int centerx = this.getPos().getX() >> 4;
         int centerz = this.getPos().getZ() >> 4;
         int exit = 0;

         for(int a = -2; a <= 2; ++a) {
            for(int b = -2; b <= 2; ++b) {
               Cell c = MazeHandler.getFromHashMap(new CellLoc(cx + a, cz + b));
               if (c != null && c.feature == 2) {
                  centerx = cx + a;
                  centerz = cz + b;
               }

               if (c != null && c.feature >= 2 && c.feature <= 5 && (c.north || c.south || c.east || c.west)) {
                  exit = c.feature;
               }
            }
         }

         MapStorage storage = this.world.getMapStorage();
         MapBossData mbd = (MapBossData) storage.getOrLoadData(MapBossData.class, "BossMapData");
         if (mbd == null) {
            mbd = new MapBossData("BossMapData");
            mbd.bossCount = 0;
            mbd.markDirty();
            storage.setData("BossMapData", mbd);
         }

         ++mbd.bossCount;
         if (this.world.rand.nextFloat() < 0.25F) {
            ++mbd.bossCount;
         }

         mbd.markDirty();
         switch (mbd.bossCount % 4) {
            case 0:
               this.spawnGolemBossRoom(centerx, centerz, exit);
               break;
            case 1:
               this.spawnWardenBossRoom(centerx, centerz, exit);
               break;
            case 2:
               this.spawnCultistBossRoom(centerx, centerz, exit);
               break;
            case 3:
               this.spawnTaintBossRoom(centerx, centerz, exit);
         }

         for(int a = -2; a <= 2; ++a) {
            for(int b = -2; b <= 2; ++b) {
               for(int c = -2; c <= 2; ++c) {
                  if (this.world.getBlockState(new net.minecraft.util.math.BlockPos(this.getPos().getX() + a, this.getPos().getY() + b, this.getPos().getZ() + c)).getBlock() == ConfigBlocks.blockAiry) {
                     PacketHandler.INSTANCE.sendToAllAround(new PacketFXBlockSparkle(this.getPos().getX() + a, this.getPos().getY() + b, this.getPos().getZ() + c, 4194368), new NetworkRegistry.TargetPoint(this.world.provider.getDimension(), this.getPos().getX() + a, this.getPos().getY() + b, this.getPos().getZ() + c, 32.0F));
                     this.world.setBlockToAir(new BlockPos(this.getPos().getX() + a, this.getPos().getY() + b, this.getPos().getZ() + c));
                  }
               }
            }
         }

         this.world.setBlockToAir(new BlockPos(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ()));
      }

   }

   private void spawnWardenBossRoom(int cx, int cz, int exit) {
      for(int i = 0; i < this.world.playerEntities.size(); ++i) {
         EntityPlayer ep = (EntityPlayer)this.world.playerEntities.get(i);
         if (ep.getDistanceSq(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ()) < (double)300.0F) {
            ep.sendMessage(new TextComponentString(I18n.translateToLocal("tc.boss.warden")));
         }
      }

      int x = cx * 16 + 16;
      int y = 50;
      int z = cz * 16 + 16;
      int x2 = x;
      int z2 = z;
      switch (exit) {
         case 2:
            x2 = x + 8;
            z2 = z + 8;
            break;
         case 3:
            x2 = x - 8;
            z2 = z + 8;
            break;
         case 4:
            x2 = x + 8;
            z2 = z - 8;
            break;
         case 5:
            x2 = x - 8;
            z2 = z - 8;
      }

      GenCommon.genObelisk(this.world, x2, y + 4, z);
      GenCommon.genObelisk(this.world, x, y + 4, z2);
      this.
        world.setBlockState(new net.minecraft.util.math.BlockPos(x2, y + 2, z), (ConfigBlocks.blockEldritch).getStateFromMeta(3), 3);
      this.
        world.setBlockState(new net.minecraft.util.math.BlockPos(x, y + 2, z2), (ConfigBlocks.blockEldritch).getStateFromMeta(3), 3);

      for(int a = -1; a <= 1; ++a) {
         for(int b = -1; b <= 1; ++b) {
            if (a != 0 && b != 0 && this.world.rand.nextFloat() < 0.9F) {
               float rr = this.world.rand.nextFloat();
               int md = rr < 0.1F ? 2 : (rr < 0.3F ? 1 : 0);
               this.
        world.setBlockState(new net.minecraft.util.math.BlockPos(x2 + a, y + 2, z + b), (ConfigBlocks.blockLootUrn).getStateFromMeta(md), 3);
            }

            if (a != 0 && b != 0 && this.world.rand.nextFloat() < 0.9F) {
               float rr = this.world.rand.nextFloat();
               int md = rr < 0.1F ? 2 : (rr < 0.3F ? 1 : 0);
               this.
        world.setBlockState(new net.minecraft.util.math.BlockPos(x + a, y + 2, z2 + b), (ConfigBlocks.blockLootUrn).getStateFromMeta(md), 3);
            }
         }
      }

      this.
        world.setBlockState(new net.minecraft.util.math.BlockPos(x - 2, y + 3, z - 2), (ConfigBlocks.blockEldritch).getStateFromMeta(10), 3);
      this.
        world.setBlockState(new net.minecraft.util.math.BlockPos(x - 2, y + 3, z + 2), (ConfigBlocks.blockEldritch).getStateFromMeta(10), 3);
      this.
        world.setBlockState(new net.minecraft.util.math.BlockPos(x + 2, y + 3, z + 2), (ConfigBlocks.blockEldritch).getStateFromMeta(10), 3);
      this.
        world.setBlockState(new net.minecraft.util.math.BlockPos(x + 2, y + 3, z - 2), (ConfigBlocks.blockEldritch).getStateFromMeta(10), 3);
      this.
        world.setBlockState(new net.minecraft.util.math.BlockPos(x - 2, y + 2, z - 2), (ConfigBlocks.blockCosmeticSolid).getStateFromMeta(15), 3);
      this.
        world.setBlockState(new net.minecraft.util.math.BlockPos(x - 2, y + 2, z + 2), (ConfigBlocks.blockCosmeticSolid).getStateFromMeta(15), 3);
      this.
        world.setBlockState(new net.minecraft.util.math.BlockPos(x + 2, y + 2, z + 2), (ConfigBlocks.blockCosmeticSolid).getStateFromMeta(15), 3);
      this.
        world.setBlockState(new net.minecraft.util.math.BlockPos(x + 2, y + 2, z - 2), (ConfigBlocks.blockCosmeticSolid).getStateFromMeta(15), 3);

      for(int a = 0; a < 3; ++a) {
         for(int b = 0; b < 3; ++b) {
            if (this.ped[a][b] < 0) {
               this.
        world.setBlockState(new net.minecraft.util.math.BlockPos(x2 - 1 + b, y + 2, z2 - 1 + a), (ConfigBlocks.blockEldritch).getStateFromMeta(4), 3);
            } else {
               this.
        world.setBlockState(new net.minecraft.util.math.BlockPos(x2 - 1 + b, y + 2, z2 - 1 + a), (ConfigBlocks.blockStairsEldritch).getStateFromMeta(this.ped[a][b]), 3);
            }
         }
      }

      EntityEldritchWarden boss = new EntityEldritchWarden(this.world);
      double d0 = (double)this.getPos().getX() - ((double)x2 + (double)0.5F);
      double d1 = (float)this.getPos().getY() - ((float)(y + 3) + boss.getEyeHeight());
      double d2 = (double)this.getPos().getZ() - ((double)z2 + (double)0.5F);
      double d3 = MathHelper.sqrt(d0 * d0 + d2 * d2);
      float f = (float)(Math.atan2(d2, d0) * (double)180.0F / Math.PI) - 90.0F;
      float f1 = (float)(-(Math.atan2(d1, d3) * (double)180.0F / Math.PI));
      boss.setLocationAndAngles((double)x2 + (double)0.5F, y + 3, (double)z2 + (double)0.5F, f, f1);
      boss.onInitialSpawn(this.world.getDifficultyForLocation(boss.getPosition()), null);
      boss.setHomePosAndDistance(new BlockPos(x, y + 2, z), 32);
      this.world.spawnEntity(boss);
   }

   private void spawnGolemBossRoom(int cx, int cz, int exit) {
      for(int i = 0; i < this.world.playerEntities.size(); ++i) {
         EntityPlayer ep = (EntityPlayer)this.world.playerEntities.get(i);
         if (ep.getDistanceSq(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ()) < (double)300.0F) {
            ep.sendMessage(new TextComponentString(I18n.translateToLocal("tc.boss.golem")));
         }
      }

      int x = cx * 16 + 16;
      int y = 50;
      int z = cz * 16 + 16;
      int x2 = 0;
      int z2 = 0;
      switch (exit) {
         case 2:
            x2 = 8;
            z2 = 8;
            break;
         case 3:
            x2 = -8;
            z2 = 8;
            break;
         case 4:
            x2 = 8;
            z2 = -8;
            break;
         case 5:
            x2 = -8;
            z2 = -8;
      }

      GenCommon.genObelisk(this.world, x + x2, y + 4, z + z2);
      GenCommon.genObelisk(this.world, x - x2, y + 4, z + z2);
      GenCommon.genObelisk(this.world, x + x2, y + 4, z - z2);
      this.
        world.setBlockState(new net.minecraft.util.math.BlockPos(x + x2, y + 2, z + z2), (ConfigBlocks.blockEldritch).getStateFromMeta(3), 3);
      this.
        world.setBlockState(new net.minecraft.util.math.BlockPos(x - x2, y + 2, z + z2), (ConfigBlocks.blockEldritch).getStateFromMeta(3), 3);
      this.
        world.setBlockState(new net.minecraft.util.math.BlockPos(x + x2, y + 2, z - z2), (ConfigBlocks.blockEldritch).getStateFromMeta(3), 3);

      for(int a = 0; a < 3; ++a) {
         for(int b = 0; b < 3; ++b) {
            if (this.ped[a][b] < 0) {
               this.
        world.setBlockState(new net.minecraft.util.math.BlockPos(x - 1 + b, y + 2, z - 1 + a), (ConfigBlocks.blockEldritch).getStateFromMeta(4), 3);
            } else {
               this.
        world.setBlockState(new net.minecraft.util.math.BlockPos(x - 1 + b, y + 2, z - 1 + a), (ConfigBlocks.blockStairsEldritch).getStateFromMeta(this.ped[a][b]), 3);
            }
         }
      }

      for(int a = -10; a <= 10; ++a) {
         for(int b = -10; b <= 10; ++b) {
            if ((a < -2 && b < -2 || a > 2 && b > 2 || a < -2 && b > 2 || a > 2 && b < -2) && this.world.rand.nextFloat() < 0.15F && this.world.isAirBlock(new BlockPos(x + a, y + 2, z + b))) {
               float rr = this.world.rand.nextFloat();
               int md = rr < 0.05F ? 2 : (rr < 0.2F ? 1 : 0);
               this.
        world.setBlockState(new net.minecraft.util.math.BlockPos(x + a, y + 2, z + b), (this.world.rand.nextFloat() < 0.3F ? ConfigBlocks.blockLootCrate : ConfigBlocks.blockLootUrn).getStateFromMeta(md), 3);
            }
         }
      }

      EntityEldritchGolem boss = new EntityEldritchGolem(this.world);
      double d0 = (double)this.getPos().getX() - ((double)x + (double)0.5F);
      double d1 = (float)this.getPos().getY() - ((float)(y + 3) + boss.getEyeHeight());
      double d2 = (double)this.getPos().getZ() - ((double)z + (double)0.5F);
      double d3 = MathHelper.sqrt(d0 * d0 + d2 * d2);
      float f = (float)(Math.atan2(d2, d0) * (double)180.0F / Math.PI) - 90.0F;
      float f1 = (float)(-(Math.atan2(d1, d3) * (double)180.0F / Math.PI));
      boss.setLocationAndAngles((double)x + (double)0.5F, y + 3, (double)z + (double)0.5F, f, f1);
      boss.onInitialSpawn(this.world.getDifficultyForLocation(boss.getPosition()), null);
      this.world.spawnEntity(boss);
   }

   private void spawnCultistBossRoom(int cx, int cz, int exit) {
      for(int i = 0; i < this.world.playerEntities.size(); ++i) {
         EntityPlayer ep = (EntityPlayer)this.world.playerEntities.get(i);
         if (ep.getDistanceSq(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ()) < (double)300.0F) {
            ep.sendMessage(new TextComponentString(I18n.translateToLocal("tc.boss.crimson")));
         }
      }

      int x = cx * 16 + 16;
      int y = 50;
      int z = cz * 16 + 16;

      for(int a = -4; a <= 4; ++a) {
         for(int b = -4; b <= 4; ++b) {
            if ((Math.abs(a) != 2 && Math.abs(b) != 2 || !this.world.rand.nextBoolean()) && (Math.abs(a) != 3 && Math.abs(b) != 3 || !(this.world.rand.nextFloat() > 0.33F)) && (Math.abs(a) != 4 && Math.abs(b) != 4 || !(this.world.rand.nextFloat() > 0.25F))) {
               this.
        world.setBlockState(new net.minecraft.util.math.BlockPos(x + b, y + 1, z + a), (ConfigBlocks.blockEldritch).getStateFromMeta(7), 3);
            }
         }
      }

      for(int a = 0; a < 5; ++a) {
         for(int b = 0; b < 5; ++b) {
            if (a == 0 || a == 4 || b == 0 || b == 4) {
               this.
        world.setBlockState(new net.minecraft.util.math.BlockPos(x - 8 + b * 4, y + 2, z - 8 + a * 4), (ConfigBlocks.blockCosmeticSolid).getStateFromMeta(11), 3);
               this.
        world.setBlockState(new net.minecraft.util.math.BlockPos(x - 8 + b * 4, y + 3, z - 8 + a * 4), (ConfigBlocks.blockEldritch).getStateFromMeta(5), 3);
               this.
        world.setBlockState(new net.minecraft.util.math.BlockPos(x - 8 + b * 4, y + 4, z - 8 + a * 4), (ConfigBlocks.blockSlabStone).getStateFromMeta(1), 3);
               this.
        world.setBlockState(new net.minecraft.util.math.BlockPos(x - 8 + b * 4, y + 10, z - 8 + a * 4), (ConfigBlocks.blockCosmeticSolid).getStateFromMeta(11), 3);
               this.
        world.setBlockState(new net.minecraft.util.math.BlockPos(x - 8 + b * 4, y + 9, z - 8 + a * 4), (ConfigBlocks.blockEldritch).getStateFromMeta(5), 3);
               this.
        world.setBlockState(new net.minecraft.util.math.BlockPos(x - 8 + b * 4, y + 8, z - 8 + a * 4), (ConfigBlocks.blockSlabStone).getStateFromMeta(9), 3);
            }
         }
      }

      EntityCultistPortal boss = new EntityCultistPortal(this.world);
      boss.setLocationAndAngles((double)x + (double)0.5F, y + 2, (double)z + (double)0.5F, 0.0F, 0.0F);
      this.world.spawnEntity(boss);
   }

   private void spawnTaintBossRoom(int cx, int cz, int exit) {
      for(int i = 0; i < this.world.playerEntities.size(); ++i) {
         EntityPlayer ep = (EntityPlayer)this.world.playerEntities.get(i);
         if (ep.getDistanceSq(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ()) < (double)300.0F) {
            ep.sendMessage(new TextComponentString(I18n.translateToLocal("tc.boss.taint")));
         }
      }

      int x = cx * 16 + 16;
      int y = 50;
      int z = cz * 16 + 16;

      for(int a = -12; a <= 12; ++a) {
         for(int b = -12; b <= 12; ++b) {
            Utils.setBiomeAt(this.world, x + b, z + a, ThaumcraftWorldGenerator.biomeTaint);

            for(int c = 0; c < 9; ++c) {
               if (this.world.isAirBlock(new BlockPos(x + b, y + 2 + c, z + a)) && BlockUtils.isAdjacentToSolidBlock(this.world, x + b, y + 2 + c, z + a) && this.world.rand.nextInt(3) != 0) {
                  this.
        world.setBlockState(new net.minecraft.util.math.BlockPos(x + b, y + 2 + c, z + a), (ConfigBlocks.blockTaintFibres).getStateFromMeta(this.world.rand.nextInt(4) == 0 ? 1 : 0), 3);
               }
            }

            if ((double)this.world.rand.nextFloat() < 0.15) {
               this.
        world.setBlockState(new net.minecraft.util.math.BlockPos(x + b, y + 2, z + a), (ConfigBlocks.blockTaint).getStateFromMeta(0), 3);
               if ((double)this.world.rand.nextFloat() < 0.2) {
                  this.
        world.setBlockState(new net.minecraft.util.math.BlockPos(x + b, y + 3, z + a), (ConfigBlocks.blockTaint).getStateFromMeta(0), 3);
               }
            }

            if ((Math.abs(a) != 4 && Math.abs(b) != 4 || !this.world.rand.nextBoolean()) && (Math.abs(a) < 5 && Math.abs(b) < 5 || !(this.world.rand.nextFloat() > 0.33F)) && (Math.abs(a) < 7 && Math.abs(b) < 7 || !(this.world.rand.nextFloat() > 0.25F))) {
               this.
        world.setBlockState(new net.minecraft.util.math.BlockPos(x + b, y + 1, z + a), (ConfigBlocks.blockTaint).getStateFromMeta(1), 3);
            }
         }
      }

      EntityTaintacle boss1 = this.world.getDifficulty() != EnumDifficulty.HARD ? new EntityTaintacle(this.world) : new EntityTaintacleGiant(this.world);
      boss1.setLocationAndAngles((double)x + (double)0.5F, y + 3, (double)z + (double)0.5F, 0.0F, 0.0F);
      EntityUtils.makeChampion(boss1, true);
      this.world.spawnEntity(boss1);
      EntityTaintacle boss2 = this.world.rand.nextBoolean() ? new EntityTaintacle(this.world) : new EntityTaintacleGiant(this.world);
      boss2.setLocationAndAngles((double)x + (double)3.5F, y + 3, (double)z + (double)3.5F, 0.0F, 0.0F);
      EntityUtils.makeChampion(boss2, true);
      this.world.spawnEntity(boss2);
      EntityTaintacle boss3 = boss2 instanceof EntityTaintacleGiant ? new EntityTaintacle(this.world) : new EntityTaintacleGiant(this.world);
      boss3.setLocationAndAngles((double)x - (double)2.5F, y + 3, (double)z + (double)3.5F, 0.0F, 0.0F);
      EntityUtils.makeChampion(boss3, true);
      this.world.spawnEntity(boss3);
      EntityTaintacle boss4 = this.world.rand.nextBoolean() ? new EntityTaintacle(this.world) : new EntityTaintacleGiant(this.world);
      boss4.setLocationAndAngles((double)x + (double)3.5F, y + 3, (double)z - (double)2.5F, 0.0F, 0.0F);
      EntityUtils.makeChampion(boss4, true);
      this.world.spawnEntity(boss4);
      EntityTaintacle boss5 = boss4 instanceof EntityTaintacleGiant ? new EntityTaintacle(this.world) : new EntityTaintacleGiant(this.world);
      boss5.setLocationAndAngles((double)x - (double)2.5F, y + 3, (double)z - (double)2.5F, 0.0F, 0.0F);
      EntityUtils.makeChampion(boss5, true);
      this.world.spawnEntity(boss5);
   }

   public double getMaxRenderDistanceSquared() {
      return 9216.0F;
   }

   @SideOnly(Side.CLIENT)
   public AxisAlignedBB getRenderBoundingBox() {
      return new AxisAlignedBB((double)this.getPos().getX() - (double)2.25F, (double)this.getPos().getY() - (double)2.25F, (double)this.getPos().getZ() - (double)2.25F, (double)this.getPos().getX() + (double)3.25F, (double)this.getPos().getY() + (double)3.25F, (double)this.getPos().getZ() + (double)3.25F);
   }

   public byte getFacing() {
      return this.facing;
   }

   public void setFacing(byte face) {
      this.facing = face;
      { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
      this.markDirty();
   }

   public void readCustomNBT(NBTTagCompound nbttagcompound) {
      this.facing = nbttagcompound.getByte("facing");
      this.count = nbttagcompound.getShort("count");
   }

   public void writeCustomNBT(NBTTagCompound nbttagcompound) {
      nbttagcompound.setByte("facing", this.facing);
      nbttagcompound.setShort("count", (short)this.count);
   }
}
