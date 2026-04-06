package thaumcraft.common.tiles;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.TileThaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.visnet.VisNetHandler;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigBlocks;
import net.minecraft.util.math.BlockPos;

public class TileArcaneFurnace extends TileThaumcraft {
   private ItemStack[] furnaceItemStacks = new ItemStack[32];
   public int furnaceCookTime = 0;
   public int furnaceMaxCookTime = 0;
   public int speedyTime = 0;
   public int facingX = -5;
   public int facingZ = -5;

   public int getSizeInventory() {
      return this.furnaceItemStacks.length;
   }

   public ItemStack getStackInSlot(int i) {
      ItemStack s = this.furnaceItemStacks[i]; return s != null ? s : ItemStack.EMPTY;
   }

   public ItemStack decrStackSize(int i, int j) {
      if (this.furnaceItemStacks[i] != null) {
         if (this.furnaceItemStacks[i].getCount() <= j) {
            ItemStack itemstack = this.furnaceItemStacks[i];
            this.furnaceItemStacks[i] = null;
            this.markDirty();
            return itemstack;
         } else {
            ItemStack itemstack1 = this.furnaceItemStacks[i].splitStack(j);
            if (this.furnaceItemStacks[i].isEmpty()) {
               this.furnaceItemStacks[i] = null;
            }

            this.markDirty();
            return itemstack1;
         }
      } else {
         return null;
      }
   }

   public void setInventorySlotContents(int i, ItemStack itemstack) {
      this.furnaceItemStacks[i] = itemstack;
      if (itemstack != null && itemstack.getCount() > this.getInventoryStackLimit()) {
         itemstack.setCount(this.getInventoryStackLimit());
      }

      this.markDirty();
   }

   private int getInventoryStackLimit() {
      return 64;
   }

   public void readFromNBT(NBTTagCompound nbttagcompound) {
      super.readFromNBT(nbttagcompound);
      NBTTagList nbttaglist = nbttagcompound.getTagList("Items", 10);
      this.furnaceItemStacks = new ItemStack[this.getSizeInventory()];

      for(int i = 0; i < nbttaglist.tagCount(); ++i) {
         NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
         byte byte0 = nbttagcompound1.getByte("Slot");
         if (byte0 >= 0 && byte0 < this.furnaceItemStacks.length) {
            this.furnaceItemStacks[byte0] = new ItemStack(nbttagcompound1);
         }
      }

      this.furnaceCookTime = nbttagcompound.getShort("CookTime");
      this.speedyTime = nbttagcompound.getShort("SpeedyTime");
   }

   public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
      super.writeToNBT(nbttagcompound);
      nbttagcompound.setShort("CookTime", (short)this.furnaceCookTime);
      nbttagcompound.setShort("SpeedyTime", (short)this.speedyTime);
      NBTTagList nbttaglist = new NBTTagList();

      for(int i = 0; i < this.furnaceItemStacks.length; ++i) {
         if (this.furnaceItemStacks[i] != null) {
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();
            nbttagcompound1.setByte("Slot", (byte)i);
            this.furnaceItemStacks[i].writeToNBT(nbttagcompound1);
            nbttaglist.appendTag(nbttagcompound1);
         }
      }

      nbttagcompound.setTag("Items", nbttaglist);
      return nbttagcompound;
   }

   public void updateEntity() {
            if (this.facingX == -5) {
         this.getFacing();
      }

      if (!this.world.isRemote) {
         boolean cookedflag = false;
         if (this.furnaceCookTime > 0) {
            --this.furnaceCookTime;
            cookedflag = true;
         }

         if (cookedflag && this.speedyTime > 0) {
            --this.speedyTime;
         }

         if (this.speedyTime <= 0) {
            this.speedyTime = VisNetHandler.drainVis(this.world, this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), Aspect.FIRE, 5);
         }

         if (this.furnaceMaxCookTime == 0) {
            this.furnaceMaxCookTime = this.calcCookTime();
         }

         if (this.furnaceCookTime > this.furnaceMaxCookTime) {
            this.furnaceCookTime = this.furnaceMaxCookTime;
         }

         if (this.furnaceCookTime == 0 && cookedflag) {
            for(int a = 0; a < this.getSizeInventory(); ++a) {
               if (this.furnaceItemStacks[a] != null) {
                  ItemStack itemstack = FurnaceRecipes.instance().getSmeltingResult(this.furnaceItemStacks[a]);
                  if (itemstack != null) {
                     this.ejectItem(itemstack.copy(), this.furnaceItemStacks[a]);
                     this.world.addBlockEvent(this.getPos(), ConfigBlocks.blockArcaneFurnace, 3, 0);
                     this.furnaceItemStacks[a].shrink(1);
                     if (this.furnaceItemStacks[a].isEmpty()) {
                        this.furnaceItemStacks[a] = null;
                     }
                     break;
                  }
               }
            }
         }

         if (this.furnaceCookTime == 0 && !cookedflag) {
            for(int a = 0; a < this.getSizeInventory(); ++a) {
               if (this.furnaceItemStacks[a] != null && this.canSmelt(a)) {
                  this.furnaceMaxCookTime = this.calcCookTime();
                  this.furnaceCookTime = this.furnaceMaxCookTime;
                  break;
               }
            }
         }
      }

   }

   private int getBellows() {
      int bellows = 0;

      for(EnumFacing dir : EnumFacing.values()) {
         if (dir != EnumFacing.UP) {
            int xx = this.getPos().getX() + dir.getXOffset() * 2;
            int yy = this.getPos().getY() + dir.getYOffset() * 2;
            int zz = this.getPos().getZ() + dir.getZOffset() * 2;
            TileEntity tile = this.world.getTileEntity(new BlockPos(xx, yy, zz));
            if (tile instanceof TileBellows && ((TileBellows) tile).orientation == dir.getOpposite().ordinal() && !this.world.isBlockPowered(new BlockPos(xx, yy, zz))) {
               ++bellows;
            }
         }
      }

      return Math.min(3, bellows);
   }

   private int calcCookTime() {
      return (this.speedyTime > 0 ? 80 : 140) - 20 * this.getBellows();
   }

   public boolean addItemsToInventory(ItemStack items) {
      for(int a = 0; a < this.getSizeInventory(); ++a) {
         if (this.furnaceItemStacks[a] != null && this.furnaceItemStacks[a].isItemEqual(items) && this.furnaceItemStacks[a].getCount() + items.getCount() <= items.getMaxStackSize()) {
            ItemStack var10000 = this.furnaceItemStacks[a];
            var10000.grow(items.getCount());
            if (!this.canSmelt(a)) {
               this.destroyItem(a);
            }

            this.markDirty();
            return true;
         }

         if (this.furnaceItemStacks[a] == null) {
            this.setInventorySlotContents(a, items);
            if (!this.canSmelt(a)) {
               this.destroyItem(a);
            }

            this.markDirty();
            return true;
         }
      }

      return false;
   }

   private void destroyItem(int slot) {
      this.furnaceItemStacks[slot] = null;
      { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("minecraft:random.fizz")); if (_snd != null) this.world.playSound(null, (float)this.getPos().getX() + 0.5F, (float)this.getPos().getY() + 0.5F, (float)this.getPos().getZ() + 0.5F, _snd, net.minecraft.util.SoundCategory.NEUTRAL, 0.3F, 2.6F + (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.8F); };
      double var21 = (float)this.getPos().getX() + this.world.rand.nextFloat();
      double var22 = this.getPos().getY() + 1;
      double var23 = (float)this.getPos().getZ() + this.world.rand.nextFloat();
      this.world.spawnParticle(EnumParticleTypes.LAVA, var21, var22, var23, 0.0F, 0.0F, 0.0F);
   }

   private void getFacing() {
      this.facingX = 0;
      this.facingZ = 0;
      if (this.world.getBlockState(new net.minecraft.util.math.BlockPos(this.getPos().getX() - 1, this.getPos().getY(), this.getPos().getZ())).getBlock() == ConfigBlocks.blockArcaneFurnace && this.
        world.getBlockState(new net.minecraft.util.math.BlockPos(this.getPos().getX() - 1, this.getPos().getY(), this.getPos().getZ())).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(this.getPos().getX() - 1, this.getPos().getY(), this.getPos().getZ()))) == 10) {
         this.facingX = -1;
      } else if (this.world.getBlockState(new net.minecraft.util.math.BlockPos(this.getPos().getX() + 1, this.getPos().getY(), this.getPos().getZ())).getBlock() == ConfigBlocks.blockArcaneFurnace && this.
        world.getBlockState(new net.minecraft.util.math.BlockPos(this.getPos().getX() + 1, this.getPos().getY(), this.getPos().getZ())).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(this.getPos().getX() + 1, this.getPos().getY(), this.getPos().getZ()))) == 10) {
         this.facingX = 1;
      } else if (this.world.getBlockState(new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ() - 1)).getBlock() == ConfigBlocks.blockArcaneFurnace && this.
        world.getBlockState(new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ() - 1)).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ() - 1))) == 10) {
         this.facingZ = -1;
      } else {
         this.facingZ = 1;
      }

   }

   public void ejectItem(ItemStack items, ItemStack furnaceItemStack) {
      if (items != null) {
         ItemStack bit = items.copy();
         int bellows = this.getBellows();
         float lx = 0.5F;
         lx += (float)this.facingX * 1.2F;
         float lz = 0.5F;
         lz += (float)this.facingZ * 1.2F;
         float mx = this.facingX == 0 ? (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.03F : (float)this.facingX * 0.13F;
         float mz = this.facingZ == 0 ? (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.03F : (float)this.facingZ * 0.13F;
         EntityItem entityitem = new EntityItem(this.world, (float)this.getPos().getX() + lx, (float)this.getPos().getY() + 0.4F, (float)this.getPos().getZ() + lz, items);
         entityitem.motionX = mx;
         entityitem.motionZ = mz;
         entityitem.motionY = 0.0F;
         this.world.spawnEntity(entityitem);
         if (ThaumcraftApi.getSmeltingBonus(furnaceItemStack) != null) {
            ItemStack bonus = ThaumcraftApi.getSmeltingBonus(furnaceItemStack).copy();
            if (bonus != null) {
               if (bellows == 0) {
                  if (this.world.rand.nextInt(4) == 0) {
                     bonus.grow(1);
                  }
               } else {
                  for(int a = 0; a < bellows; ++a) {
                     if (this.world.rand.nextFloat() < 0.44F) {
                        bonus.grow(1);
                     }
                  }
               }
            }

            if (bonus != null && bonus.getCount() > 0) {
               mx = this.facingX == 0 ? (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.03F : (float)this.facingX * 0.13F;
               mz = this.facingZ == 0 ? (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.03F : (float)this.facingZ * 0.13F;
               EntityItem entityitem2 = new EntityItem(this.world, (float)this.getPos().getX() + lx, (float)this.getPos().getY() + 0.4F, (float)this.getPos().getZ() + lz, bonus);
               entityitem2.motionX = mx;
               entityitem2.motionZ = mz;
               entityitem2.motionY = 0.0F;
               this.world.spawnEntity(entityitem2);
            }
         }

         int var2 = items.getCount();
         float var3 = FurnaceRecipes.instance().getSmeltingExperience(bit);
         if (var3 == 0.0F) {
            var2 = 0;
         } else if (var3 < 1.0F) {
            int var4 = MathHelper.floor((float)var2 * var3);
            if (var4 < MathHelper.ceil((float)var2 * var3) && (float)Math.random() < (float)var2 * var3 - (float)var4) {
               ++var4;
            }

            var2 = var4;
         }

         while(var2 > 0) {
            int var4 = EntityXPOrb.getXPSplit(var2);
            var2 -= var4;
            EntityXPOrb xp = new EntityXPOrb(this.world, (float)this.getPos().getX() + lx, (float)this.getPos().getY() + 0.4F, (float)this.getPos().getZ() + lz, var4);
            mx = this.facingX == 0 ? (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.025F : (float)this.facingX * 0.13F;
            mz = this.facingZ == 0 ? (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.025F : (float)this.facingZ * 0.13F;
            xp.motionX = mx;
            xp.motionZ = mz;
            xp.motionY = 0.0F;
            this.world.spawnEntity(xp);
         }

      }
   }

   private boolean canSmelt(int slotIn) {
      if (this.furnaceItemStacks[slotIn] == null) {
         return false;
      } else {
         ItemStack itemstack = FurnaceRecipes.instance().getSmeltingResult(this.furnaceItemStacks[slotIn]);
         return itemstack != null;
      }
   }

   public boolean receiveClientEvent(int i, int j) {
      if (i != 3) {
         return super.receiveClientEvent(i, j);
      } else {
         if (this.world.isRemote) {
            for(int a = 0; a < 5; ++a) {
               Thaumcraft.proxy.furnaceLavaFx(this.world, this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), this.facingX, this.facingZ);
               { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("minecraft:liquid.lavapop")); if (_snd != null) this.world.playSound(null, (float)this.getPos().getX() + 0.5F, (float)this.getPos().getY() + 0.5F, (float)this.getPos().getZ() + 0.5F, _snd, net.minecraft.util.SoundCategory.NEUTRAL, 0.1F + this.world.rand.nextFloat() * 0.1F, 0.9F + this.world.rand.nextFloat() * 0.15F); };
            }
         }

         return true;
      }
   }
}
