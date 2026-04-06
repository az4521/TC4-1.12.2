package thaumcraft.common.tiles;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.awt.Color;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import thaumcraft.api.TileThaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.crafting.CrucibleRecipe;
import thaumcraft.api.wands.IWandable;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.container.InventoryFake;
import thaumcraft.common.entities.EntitySpecialItem;
import thaumcraft.common.lib.crafting.ThaumcraftCraftingManager;
import net.minecraft.util.math.BlockPos;

public class TileCrucible extends TileThaumcraft implements IFluidHandler, IWandable, IAspectContainer {
   public short heat;
   public AspectList aspects = new AspectList();
   public final int maxTags = 100;
   int bellows = -1;
   private int delay = 0;
   public FluidTank tank;
   private long counter;
   int prevcolor;
   int prevx;
   int prevy;

   public TileCrucible() {
      this.tank = new FluidTank(FluidRegistry.WATER, 0, 1000);
      this.counter = -100L;
      this.prevcolor = 0;
      this.prevx = 0;
      this.prevy = 0;
      this.heat = 0;
   }

   public void readCustomNBT(NBTTagCompound nbttagcompound) {
      this.heat = nbttagcompound.getShort("Heat");
      this.tank.readFromNBT(nbttagcompound);
      if (nbttagcompound.hasKey("Empty")) {
         this.tank.setFluid(null);
      }

      this.aspects.readFromNBT(nbttagcompound);
   }

   public void writeCustomNBT(NBTTagCompound nbttagcompound) {
      nbttagcompound.setShort("Heat", this.heat);
      this.tank.writeToNBT(nbttagcompound);
      this.aspects.writeToNBT(nbttagcompound);
   }

   public void updateEntity() {
      ++this.counter;
      int prevheat = this.heat;
      if (!this.world.isRemote) {
         if (this.bellows < 0) {
            this.getBellows();
         }

         if (this.tank.getFluidAmount() <= 0) {
            if (this.heat > 0) {
               --this.heat;
            }
         } else {
            Material mat = this.world.getBlockState(new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY() - 1, this.getPos().getZ())).getMaterial();
            Block bi = this.world.getBlockState(new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY() - 1, this.getPos().getZ())).getBlock();
            int md = this.
        world.getBlockState(new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY() - 1, this.getPos().getZ())).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY() - 1, this.getPos().getZ())));
            if (mat == Material.LAVA || mat == Material.FIRE || bi == ConfigBlocks.blockAiry && md == 1) {
               if (this.heat < 200) {
                  this.heat = (short)(this.heat + 1 + this.bellows * 2);
                  if (prevheat < 151 && this.heat >= 151) {
                     this.markDirty();
                     { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
                  }
               }
            } else if (this.heat > 0) {
               --this.heat;
               if (this.heat == 149) {
                  this.markDirty();
                  net.minecraft.block.state.IBlockState state = this.world.getBlockState(this.pos);
                  this.world.notifyBlockUpdate(this.pos, state, state, 3);
               }
            }
         }

         if (this.tagAmount() > 100 && this.counter % 5L == 0L) {
            AspectList tt = this.takeRandomFromSource();
            this.spill();
         }

         if (this.counter > 100L && this.heat > 150) {
            this.counter = 0L;
            if (this.tagAmount() > 0) {
               int s = this.aspects.getAspects().length;
               Aspect a = this.aspects.getAspects()[this.world.rand.nextInt(s)];
               if (a.isPrimal()) {
                  a = this.aspects.getAspects()[this.world.rand.nextInt(s)];
               }

               this.tank.drain(2, true);
               this.aspects.remove(a, 1);
               if (!a.isPrimal()) {
                  if (this.world.rand.nextBoolean()) {
                     this.aspects.add(a.getComponents()[0], 1);
                  } else {
                     this.aspects.add(a.getComponents()[1], 1);
                  }
               } else {
                  this.spill();
               }
            }

            this.markDirty();
            net.minecraft.block.state.IBlockState state = this.world.getBlockState(this.pos);
            this.world.notifyBlockUpdate(this.pos, state, state, 3);
         }
      } else if (this.tank.getFluidAmount() > 0) {
         this.drawEffects();
      }

      if (this.world.isRemote && prevheat < 151 && this.heat >= 151) {
         ++this.heat;
      }

   }

   private void drawEffects() {
      if (this.heat > 150) {
         Thaumcraft.proxy.crucibleFroth(this.world, (float)this.getPos().getX() + 0.2F + this.world.rand.nextFloat() * 0.6F, (float)this.getPos().getY() + this.getFluidHeight(), (float)this.getPos().getZ() + 0.2F + this.world.rand.nextFloat() * 0.6F);
         if (this.tagAmount() > 100) {
            for(int a = 0; a < 2; ++a) {
               Thaumcraft.proxy.crucibleFrothDown(this.world, (float)this.getPos().getX(), (float)(this.getPos().getY() + 1), (float)this.getPos().getZ() + this.world.rand.nextFloat());
               Thaumcraft.proxy.crucibleFrothDown(this.world, (float)(this.getPos().getX() + 1), (float)(this.getPos().getY() + 1), (float)this.getPos().getZ() + this.world.rand.nextFloat());
               Thaumcraft.proxy.crucibleFrothDown(this.world, (float)this.getPos().getX() + this.world.rand.nextFloat(), (float)(this.getPos().getY() + 1), (float)this.getPos().getZ());
               Thaumcraft.proxy.crucibleFrothDown(this.world, (float)this.getPos().getX() + this.world.rand.nextFloat(), (float)(this.getPos().getY() + 1), (float)(this.getPos().getZ() + 1));
            }
         }
      }

      if (this.world.rand.nextInt(6) == 0 && this.aspects.size() > 0) {
         int color = this.aspects.getAspects()[this.world.rand.nextInt(this.aspects.size())].getColor() - 16777216;
         int x = 5 + this.world.rand.nextInt(22);
         int y = 5 + this.world.rand.nextInt(22);
         this.delay = this.world.rand.nextInt(10);
         this.prevcolor = color;
         this.prevx = x;
         this.prevy = y;
         Color c = new Color(color);
         float r = (float)c.getRed() / 255.0F;
         float g = (float)c.getGreen() / 255.0F;
         float b = (float)c.getBlue() / 255.0F;
         Thaumcraft.proxy.crucibleBubble(this.world, (float)this.getPos().getX() + (float)x / 32.0F + 0.015625F, (float)this.getPos().getY() + 0.05F + this.getFluidHeight(), (float)this.getPos().getZ() + (float)y / 32.0F + 0.015625F, r, g, b);
      }

   }

   public void spill() {
      if (this.world.rand.nextInt(4) == 0) {
         if (this.world.isAirBlock(new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY() + 1, this.getPos().getZ()))) {
            if (this.world.rand.nextBoolean()) {
               world.setBlockState(new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY() + 1, this.getPos().getZ()), ConfigBlocks.blockFluxGas.getDefaultState(), 3);
            } else {
               world.setBlockState(new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY() + 1, this.getPos().getZ()), ConfigBlocks.blockFluxGoo.getDefaultState(), 3);
            }
         } else {
            Block bi = this.world.getBlockState(new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY() + 1, this.getPos().getZ())).getBlock();
            int md = this.
        world.getBlockState(new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY() + 1, this.getPos().getZ())).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY() + 1, this.getPos().getZ())));
            if (bi == ConfigBlocks.blockFluxGoo && md < 7) {
               world.setBlockState(new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY() + 1, this.getPos().getZ()), ConfigBlocks.blockFluxGoo.getStateFromMeta(md + 1), 3);
            } else if (bi == ConfigBlocks.blockFluxGas && md < 7) {
               world.setBlockState(new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY() + 1, this.getPos().getZ()), ConfigBlocks.blockFluxGas.getStateFromMeta(md + 1), 3);
            } else {
               int x = -1 + this.world.rand.nextInt(3);
               int y = -1 + this.world.rand.nextInt(3);
               int z = -1 + this.world.rand.nextInt(3);
               if (this.world.isAirBlock(new net.minecraft.util.math.BlockPos(this.getPos().getX() + x, this.getPos().getY() + y, this.getPos().getZ() + z))) {
                  if (this.world.rand.nextBoolean()) {
                     world.setBlockState(new net.minecraft.util.math.BlockPos(this.getPos().getX() + x, this.getPos().getY() + y, this.getPos().getZ() + z), ConfigBlocks.blockFluxGas.getDefaultState(), 3);
                  } else {
                     world.setBlockState(new net.minecraft.util.math.BlockPos(this.getPos().getX() + x, this.getPos().getY() + y, this.getPos().getZ() + z), ConfigBlocks.blockFluxGoo.getDefaultState(), 3);
                  }
               }
            }
         }
      }

   }

   public void spillRemnants() {
      if (this.tank.getFluidAmount() > 0 || this.aspects.visSize() > 0) {
         this.tank.setFluid(null);

         for(int a = 0; a < this.aspects.visSize() / 2; ++a) {
            this.spill();
         }

         this.aspects = new AspectList();
         this.markDirty();
         { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
         this.world.addBlockEvent(new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ()), ConfigBlocks.blockMetalDevice, 2, 5);
      }

   }

   public void ejectItem(ItemStack items) {
      int stacks = 1;
      boolean first = true;

      do {
         ItemStack spitout = items.copy();
         if (spitout.getCount() > spitout.getMaxStackSize()) {
            spitout.setCount(spitout.getMaxStackSize());
         }

         items.shrink(spitout.getCount());
         EntitySpecialItem entityitem = new EntitySpecialItem(this.world, (float)this.getPos().getX() + 0.5F, (float)this.getPos().getY() + 0.71F, (float)this.getPos().getZ() + 0.5F, spitout);
         entityitem.motionY = 0.1F;
         entityitem.motionX = first ? (double)0.0F : (double)((this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.01F);
         entityitem.motionZ = first ? (double)0.0F : (double)((this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.01F);
         this.world.spawnEntity(entityitem);
         first = false;
      } while(items.getCount() > 0);

   }

   public void attemptSmelt(EntityItem entity) {
      boolean bubble = false;
      boolean event = false;
      ItemStack item = entity.getItem();
      NBTTagCompound itemData = entity.getEntityData();
      String username = itemData.getString("thrower");
      int stacksize = item.getCount();

      for(int a = 0; a < stacksize; ++a) {
         CrucibleRecipe rc = ThaumcraftCraftingManager.findMatchingCrucibleRecipe(username, this.aspects, item);
         if (rc != null && this.tank.getFluidAmount() > 0) {
            ItemStack out = rc.getRecipeOutput().copy();
            EntityPlayer p = this.world.getPlayerEntityByName(username);
            if (p != null) {
               FMLCommonHandler.instance().firePlayerCraftingEvent(p, out, new InventoryFake(new ItemStack[]{item}));
            }

            this.aspects = rc.removeMatching(this.aspects);
            this.tank.drain(50, true);
            this.ejectItem(out);
            event = true;
            --stacksize;
            this.counter = -250L;
         } else {
            AspectList ot = ThaumcraftCraftingManager.getObjectTags(item);
            ot = ThaumcraftCraftingManager.getBonusTags(item, ot);
            if (ot == null || ot.size() == 0) {
               entity.motionY = 0.35F;
               entity.motionX = (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.2F;
               entity.motionZ = (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.2F;
               this.world.playSound(null, entity.posX, entity.posY, entity.posZ, net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("random.pop")), net.minecraft.util.SoundCategory.NEUTRAL, 0.2F, (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.7F + 1.0F);
               return;
            }

            for(Aspect tag : ot.getAspects()) {
               this.aspects.add(tag, ot.getAmount(tag));
            }

            bubble = true;
            --stacksize;
            this.counter = -150L;
         }
      }

      if (bubble) {
         this.world.playSound(null, entity.posX, entity.posY, entity.posZ, net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft", "bubble")), net.minecraft.util.SoundCategory.NEUTRAL, 0.2F, 1.0F + this.world.rand.nextFloat() * 0.4F);
         { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
         this.world.addBlockEvent(new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ()), ConfigBlocks.blockMetalDevice, 2, 1);
      }

      if (event) {
         { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
         this.world.addBlockEvent(new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ()), ConfigBlocks.blockMetalDevice, 2, 5);
      }

      if (stacksize <= 0) {
         entity.setDead();
      } else {
         item.setCount(stacksize);
         entity.setItem(item);
      }

      this.markDirty();
   }

   public int tagAmount() {
      int tt = 0;
      if (this.aspects.size() <= 0) {
         return 0;
      } else {
         for(Aspect tag : this.aspects.getAspects()) {
            tt += this.aspects.getAmount(tag);
         }

         return tt;
      }
   }

   public float getFluidHeight() {
      float base = 0.3F + 0.5F * ((float)this.tank.getFluidAmount() / (float)this.tank.getCapacity());
      float out = base + (float)this.tagAmount() / 100.0F * (1.0F - base);
      if (out > 1.0F) {
         out = 1.001F;
      }

      if (out == 1.0F) {
         out = 0.9999F;
      }

      return out;
   }

   public AspectList takeRandomFromSource() {
      AspectList output = new AspectList();
      if (this.aspects.size() > 0) {
         Aspect tag = this.aspects.getAspects()[this.world.rand.nextInt(this.aspects.getAspects().length)];
         output.add(tag, 1);
         this.aspects.remove(tag, 1);
      }

      this.markDirty();
      { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
      return output;
   }

   public boolean receiveClientEvent(int i, int j) {
      if (i == 1) {
         if (this.world.isRemote) {
            Thaumcraft.proxy.blockSparkle(this.world, this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), -9999, 5);
         }

         return true;
      } else if (i != 2) {
         return super.receiveClientEvent(i, j);
      } else {
         Thaumcraft.proxy.crucibleBoilSound(this.world, this.getPos().getX(), this.getPos().getY(), this.getPos().getZ());
         if (this.world.isRemote) {
            for(int q = 0; q < 10; ++q) {
               int x = 5 + this.world.rand.nextInt(22);
               int y = 5 + this.world.rand.nextInt(22);
               Thaumcraft.proxy.crucibleBoil(this.world, this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), this, j);
            }
         }

         return true;
      }
   }

   public void getBellows() {
      this.bellows = 0;

      for(int a = 2; a < 6; ++a) {
         EnumFacing dir = EnumFacing.byIndex(a);
         int xx = this.getPos().getX() + dir.getXOffset();
         int zz = this.getPos().getZ() + dir.getZOffset();
         Block bi = this.world.getBlockState(new BlockPos(xx, this.getPos().getY(), zz)).getBlock();
         int md = this.
        world.getBlockState(new net.minecraft.util.math.BlockPos(xx, this.getPos().getY(), zz)).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(xx, this.getPos().getY(), zz)));
         if (bi == ConfigBlocks.blockWoodenDevice && md == 0) {
            ++this.bellows;
         }
      }

   }

   
   @Override
   public IFluidTankProperties[] getTankProperties() {
      return tank.getTankProperties();
   }

   @Override
   public int fill(FluidStack resource, boolean doFill) {
      if (resource != null && resource.getFluid() != FluidRegistry.WATER) {
         return 0;
      } else {
         if (doFill) {
            this.markDirty();
            net.minecraft.block.state.IBlockState state = this.world.getBlockState(this.pos);
            this.world.notifyBlockUpdate(this.pos, state, state, 3);
         }

         return this.tank.fill(resource, doFill);
      }
   }

   @Override
   public FluidStack drain(FluidStack resource, boolean doDrain) {
      if (resource != null && resource.isFluidEqual(this.tank.getFluid())) {
         if (doDrain) {
            this.markDirty();
            net.minecraft.block.state.IBlockState state = this.world.getBlockState(this.pos);
            this.world.notifyBlockUpdate(this.pos, state, state, 3);
         }

         return this.tank.drain(resource.amount, doDrain);
      } else {
         return null;
      }
   }

   @Override
   public FluidStack drain(int maxDrain, boolean doDrain) {
      return this.tank.drain(maxDrain, doDrain);
   }

   public boolean canFill(EnumFacing from, Fluid fluid) {
      return fluid != null && fluid == FluidRegistry.WATER;
   }

   public boolean canDrain(EnumFacing from, Fluid fluid) {
      return true;
   }

   public IFluidTankProperties[] getTankInfo(EnumFacing from) {
      return new IFluidTankProperties[]{this.tank.getTankProperties()[0]};
   }

   public int onWandRightClick(World world, ItemStack wandstack, EntityPlayer player, int x, int y, int z, int side, int md) {
      return 0;
   }

   public ItemStack onWandRightClick(World world, ItemStack wandstack, EntityPlayer player) {
      if (!world.isRemote && player.isSneaking()) {
         this.spillRemnants();
      }

      return wandstack;
   }

   public void onUsingWandTick(ItemStack wandstack, EntityPlayer player, int count) {
   }

   public void onWandStoppedUsing(ItemStack wandstack, World world, EntityPlayer player, int count) {
   }

   @SideOnly(Side.CLIENT)
   public AxisAlignedBB getRenderBoundingBox() {
      return new AxisAlignedBB(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), this.getPos().getX() + 1, this.getPos().getY() + 1, this.getPos().getZ() + 1);
   }

   public AspectList getAspects() {
      return this.aspects;
   }

   public void setAspects(AspectList aspects) {
   }

   public int addToContainer(Aspect tag, int amount) {
      return 0;
   }

   public boolean takeFromContainer(Aspect tag, int amount) {
      return false;
   }

   public boolean takeFromContainer(AspectList ot) {
      return false;
   }

   public boolean doesContainerContainAmount(Aspect tag, int amount) {
      return false;
   }

   public boolean doesContainerContain(AspectList ot) {
      return false;
   }

   public int containerContains(Aspect tag) {
      return 0;
   }

   public boolean doesContainerAccept(Aspect tag) {
      return true;
   }
}
