package thaumcraft.common.entities.ai.interact;

import com.mojang.authlib.GameProfile;
import java.util.Iterator;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import thaumcraft.common.entities.golems.EntityGolemBase;
import thaumcraft.common.entities.golems.GolemHelper;
import thaumcraft.common.entities.golems.Marker;

public class AIUseItem extends EntityAIBase {
   private EntityGolemBase theGolem;
   private int xx;
   private int yy;
   private int zz;
   private float movementSpeed;
   private float distance;
   private World theWorld;
   private IBlockState blockState;
   FakePlayer player;
   private int count;
   private int color;
   PlayerInteractionManager im;
   int nextTick;

   public AIUseItem(EntityGolemBase par1EntityCreature) {
      this.blockState = Blocks.AIR.getDefaultState();
      this.count = 0;
      this.color = -1;
      this.nextTick = 0;
      this.theGolem = par1EntityCreature;
      this.theWorld = par1EntityCreature.world;
      this.setMutexBits(3);
      this.distance = (float)MathHelper.ceil(this.theGolem.getRange() / 3.0F);
      if (this.theWorld instanceof WorldServer) {
         this.player = FakePlayerFactory.get((WorldServer)this.theWorld, new GameProfile(null, "FakeThaumcraftGolem"));
      }

      try {
         this.nextTick = this.theGolem.ticksExisted + this.theWorld.rand.nextInt(6);
      } catch (Exception ignored) {
      }

   }

   public boolean shouldExecute() {
      boolean ignoreItem = false;
      BlockPos home = this.theGolem.getHomePosition();
      EnumFacing facing = EnumFacing.byIndex(this.theGolem.homeFacing);
      int cX = home.getX() - facing.getXOffset();
      int cY = home.getY() - facing.getYOffset();
      int cZ = home.getZ() - facing.getZOffset();
      TileEntity tile = this.theGolem.world.getTileEntity(new BlockPos(cX, cY, cZ));
      if (!(tile instanceof IInventory)) {
         ignoreItem = true;
      }

      int d = 5 - this.theGolem.ticksExisted;
      if (d < 1) {
         d = 1;
      }

      if ((!this.theGolem.itemCarried.isEmpty() || ignoreItem) && this.theGolem.ticksExisted >= this.nextTick && this.theGolem.getNavigator().noPath()) {
         this.nextTick = this.theGolem.ticksExisted + d * 3;
         return this.findSomething();
      } else {
         return false;
      }
   }

   public boolean continueExecuting() {
      return this.theWorld.getBlockState(new BlockPos(this.xx, this.yy, this.zz)) == this.blockState && this.count-- > 0 && !this.theGolem.getNavigator().noPath();
   }

   public void updateTask() {
      this.theGolem.getLookHelper().setLookPosition((double)this.xx + (double)0.5F, (double)this.yy + (double)0.5F, (double)this.zz + (double)0.5F, 30.0F, 30.0F);
      double dist = this.theGolem.getDistanceSq((double)this.xx + (double)0.5F, (double)this.yy + (double)0.5F, (double)this.zz + (double)0.5F);
      if (dist <= (double)4.0F) {
         this.click();
      }

   }

   public void resetTask() {
      this.count = 0;
      this.theGolem.getNavigator().clearPath();
   }

   public void startExecuting() {
      this.count = 200;
      this.theGolem.getNavigator().tryMoveToXYZ((double)this.xx + (double)0.5F, (double)this.yy + (double)0.5F, (double)this.zz + (double)0.5F, this.theGolem.getAIMoveSpeed());
   }

   void click() {
      BlockPos home = this.theGolem.getHomePosition();
      boolean ignoreItem = false;
      EnumFacing facing = EnumFacing.byIndex(this.theGolem.homeFacing);
      int cX = home.getX() - facing.getXOffset();
      int cY = home.getY() - facing.getYOffset();
      int cZ = home.getZ() - facing.getZOffset();
      TileEntity tile = this.theGolem.world.getTileEntity(new BlockPos(cX, cY, cZ));
      if (!(tile instanceof IInventory)) {
         ignoreItem = true;
      }

      this.player.setPositionAndRotation(this.theGolem.posX, this.theGolem.posY, this.theGolem.posZ, this.theGolem.rotationYaw, this.theGolem.rotationPitch);
      this.player.setHeldItem(EnumHand.MAIN_HAND, this.theGolem.itemCarried);
      this.player.setSneaking(this.theGolem.getToggles()[2]);
      Iterator i$ = GolemHelper.getMarkedSides(this.theGolem, this.xx, this.yy, this.zz, this.theGolem.world.provider.getDimension(), (byte)this.color).iterator();
      if (i$.hasNext()) {
         Integer side = (Integer)i$.next();
         int x = 0;
         int y = 0;
         int z = 0;
         if (this.theGolem.world.isAirBlock(new BlockPos(this.xx, this.yy, this.zz))) {
            x = EnumFacing.byIndex(side).getOpposite().getXOffset();
            y = EnumFacing.byIndex(side).getOpposite().getYOffset();
            z = EnumFacing.byIndex(side).getOpposite().getZOffset();
         }

         if (this.im == null) {
            this.im = new PlayerInteractionManager(this.theGolem.world);
         }

         if (this.theGolem.itemCarried.isEmpty() && !ignoreItem) {
            this.resetTask();
         } else {
            try {
               BlockPos targetPos = new BlockPos(this.xx + x, this.yy + y, this.zz + z);
               EnumFacing targetFacing = EnumFacing.byIndex(side);
               if (this.theGolem.getToggles()[1]) {
                  this.theGolem.startLeftArmTimer();
                  this.im.onBlockClicked(targetPos, targetFacing);
               } else if (this.im.processRightClickBlock(this.player, this.theGolem.world, this.theGolem.itemCarried, EnumHand.MAIN_HAND, targetPos, targetFacing, 0.5F, 0.5F, 0.5F) == EnumActionResult.SUCCESS) {
                  this.theGolem.startRightArmTimer();
               }

               this.theGolem.itemCarried = this.player.getHeldItemMainhand();
               if (this.theGolem.itemCarried.isEmpty()) {
                  this.theGolem.itemCarried = ItemStack.EMPTY;
               }

               for(int a = 1; a < this.player.inventory.mainInventory.size(); ++a) {
                  if (!this.player.inventory.getStackInSlot(a).isEmpty()) {
                     if (this.theGolem.itemCarried.isEmpty()) {
                        this.theGolem.itemCarried = this.player.inventory.getStackInSlot(a).copy();
                     } else {
                        this.player.dropItem(this.player.inventory.getStackInSlot(a), false);
                     }

                     this.player.inventory.setInventorySlotContents(a, ItemStack.EMPTY);
                  }
               }

               this.theGolem.updateCarried();
               this.resetTask();
            } catch (Exception var14) {
               this.resetTask();
            }
         }
      }
   }

   boolean findSomething() {
      for(byte col : this.theGolem.getColorsMatching(this.theGolem.itemCarried)) {
         for(Marker marker : this.theGolem.getMarkers()) {
            if ((marker.color == col || col == -1) && (!this.theGolem.getToggles()[0] || this.theGolem.world.isAirBlock(new BlockPos(marker.x, marker.y, marker.z))) && (this.theGolem.getToggles()[0] || !this.theGolem.world.isAirBlock(new BlockPos(marker.x, marker.y, marker.z)))) {
               EnumFacing opp = EnumFacing.byIndex(marker.side);
               if (this.theGolem.world.isAirBlock(new BlockPos(marker.x + opp.getXOffset(), marker.y + opp.getYOffset(), marker.z + opp.getZOffset()))) {
                  this.color = col;
                  this.xx = marker.x;
                  this.yy = marker.y;
                  this.zz = marker.z;
                  this.blockState = this.theWorld.getBlockState(new BlockPos(this.xx, this.yy, this.zz));
                  return true;
               }
            }
         }
      }

      return false;
   }
}
