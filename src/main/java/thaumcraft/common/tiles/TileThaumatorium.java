package thaumcraft.common.tiles;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.ArrayList;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.TileThaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.api.crafting.CrucibleRecipe;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.container.InventoryFake;
import thaumcraft.common.lib.utils.InventoryUtils;

public class TileThaumatorium extends TileThaumcraft implements IAspectContainer, IEssentiaTransport, ISidedInventory, ITickable {
   public ItemStack inputStack = null;
   public AspectList essentia = new AspectList();
   public ArrayList<Integer> recipeHash = new ArrayList<>();
   public ArrayList<AspectList> recipeEssentia = new ArrayList<>();
   public ArrayList<String> recipePlayer = new ArrayList<>();
   public int currentCraft = -1;
   public int maxRecipes = 1;
   public EnumFacing facing;
   public Aspect currentSuction;
   int venting;
   int counter;
   boolean heated;
   CrucibleRecipe currentRecipe;
   public Container eventHandler;

   public TileThaumatorium() {
      this.facing = EnumFacing.NORTH;
      this.currentSuction = null;
      this.venting = 0;
      this.counter = 0;
      this.heated = false;
      this.currentRecipe = null;
   }

   @SideOnly(Side.CLIENT)
   public AxisAlignedBB getRenderBoundingBox() {
      return new AxisAlignedBB(this.getPos().getX() - 1, this.getPos().getY(), this.getPos().getZ() - 1, this.getPos().getX() + 2, this.getPos().getY() + 2, this.getPos().getZ() + 2);
   }

   public void readCustomNBT(NBTTagCompound nbttagcompound) {
      this.facing = EnumFacing.byIndex(nbttagcompound.getByte("facing"));
      this.essentia.readFromNBT(nbttagcompound);
      this.maxRecipes = nbttagcompound.getByte("maxrec");
      this.recipeEssentia = new ArrayList<>();
      this.recipeHash = new ArrayList<>();
      this.recipePlayer = new ArrayList<>();
      int[] hashes = nbttagcompound.getIntArray("recipes");
      if (hashes != null) {
         for(int hash : hashes) {
            CrucibleRecipe recipe = ThaumcraftApi.getCrucibleRecipeFromHash(hash);
            if (recipe != null) {
               this.recipeEssentia.add(recipe.aspects.copy());
               this.recipePlayer.add("");
               this.recipeHash.add(hash);
            }
         }
      }

   }

   public void writeCustomNBT(NBTTagCompound nbttagcompound) {
      nbttagcompound.setByte("facing", (byte)this.facing.ordinal());
      nbttagcompound.setByte("maxrec", (byte)this.maxRecipes);
      this.essentia.writeToNBT(nbttagcompound);
      int[] hashes = new int[this.recipeHash.size()];
      int a = 0;

      for(Integer i : this.recipeHash) {
         hashes[a] = i;
         ++a;
      }

      nbttagcompound.setIntArray("recipes", hashes);
   }

   public void readFromNBT(NBTTagCompound nbtCompound) {
      super.readFromNBT(nbtCompound);
      NBTTagList nbttaglist = nbtCompound.getTagList("Items", 10);
      if (nbttaglist.tagCount() > 0) {
         this.inputStack = new ItemStack(nbttaglist.getCompoundTagAt(0));
      }

      NBTTagList nbttaglist2 = nbtCompound.getTagList("OutputPlayer", 8);

      for(int a = 0; a < nbttaglist2.tagCount(); ++a) {
         if (this.recipePlayer.size() > a) {
            this.recipePlayer.set(a, nbttaglist2.getStringTagAt(a));
         }
      }

   }

   public NBTTagCompound writeToNBT(NBTTagCompound nbtCompound) {
      super.writeToNBT(nbtCompound);
      NBTTagList nbttaglist = new NBTTagList();
      if (this.inputStack != null) {
         NBTTagCompound nbttagcompound1 = new NBTTagCompound();
         nbttagcompound1.setByte("Slot", (byte)0);
         this.inputStack.writeToNBT(nbttagcompound1);
         nbttaglist.appendTag(nbttagcompound1);
      }

      nbtCompound.setTag("Items", nbttaglist);
      NBTTagList nbttaglist2 = new NBTTagList();
      if (!this.recipePlayer.isEmpty()) {
          for (String s : this.recipePlayer) {
              if (s != null) {
                  NBTTagString nbttagcompound1 = new NBTTagString(s);
                  nbttaglist2.appendTag(nbttagcompound1);
              }
          }
      }

      nbtCompound.setTag("OutputPlayer", nbttaglist2);
      return nbtCompound;
   }

   boolean checkHeat() {
      Material mat = this.world.getBlockState(new BlockPos(this.getPos().getX(), this.getPos().getY() - 2, this.getPos().getZ())).getMaterial();
      Block bi = this.world.getBlockState(new BlockPos(this.getPos().getX(), this.getPos().getY() - 2, this.getPos().getZ())).getBlock();
      int md = bi.getMetaFromState(this.world.getBlockState(new BlockPos(this.getPos().getX(), this.getPos().getY() - 2, this.getPos().getZ())));
      return mat == Material.LAVA || mat == Material.FIRE || bi == ConfigBlocks.blockAiry && md == 1;
   }

   public ItemStack getCurrentOutputRecipe() {
      ItemStack out = null;
      if (this.currentCraft >= 0 && this.recipeHash != null && !this.recipeHash.isEmpty()) {
         CrucibleRecipe recipe = ThaumcraftApi.getCrucibleRecipeFromHash(this.recipeHash.get(this.currentCraft));
         if (recipe != null) {
            out = recipe.getRecipeOutput().copy();
         }
      }

      return out;
   }

   @Override
   public void update() {
      if (!this.world.isRemote) {
         if (this.counter == 0 || this.counter % 40 == 0) {
            this.heated = this.checkHeat();
            this.getUpgrades();
         }

         ++this.counter;
         if (this.heated && !this.gettingPower() && this.counter % 5 == 0 && this.recipeHash != null && !this.recipeHash.isEmpty()) {
            if (this.inputStack == null) {
               this.currentSuction = null;
               return;
            }

            if (this.currentCraft < 0 || this.currentCraft >= this.recipeHash.size() || this.currentRecipe == null || !this.currentRecipe.catalystMatches(this.inputStack)) {
               for(int a = 0; a < this.recipeHash.size(); ++a) {
                  CrucibleRecipe recipe = ThaumcraftApi.getCrucibleRecipeFromHash(this.recipeHash.get(a));
                  if (recipe.catalystMatches(this.inputStack)) {
                     this.currentCraft = a;
                     this.currentRecipe = recipe;
                     break;
                  }
               }
            }

            if (this.currentCraft < 0 || this.currentCraft >= this.recipeHash.size()) {
               return;
            }

            TileEntity inventory = this.world.getTileEntity(this.getPos().add(this.facing.getXOffset(), 0, this.facing.getZOffset()));
            if (inventory instanceof IInventory) {
               ItemStack dropped = this.getCurrentOutputRecipe();
               dropped = InventoryUtils.placeItemStackIntoInventory(dropped, (IInventory)inventory, this.facing.getOpposite().ordinal(), false);
               if (dropped != null) {
                  return;
               }
            }

            boolean done = true;
            this.currentSuction = null;

            for(Aspect aspect : this.recipeEssentia.get(this.currentCraft).getAspectsSorted()) {
               if (this.essentia.getAmount(aspect) < this.recipeEssentia.get(this.currentCraft).getAmount(aspect)) {
                  this.currentSuction = aspect;
                  done = false;
                  break;
               }
            }

            if (done) {
               this.completeRecipe();
            } else if (this.currentSuction != null) {
               this.fill();
            }
         }
      } else if (this.venting > 0) {
         --this.venting;
         float fx = 0.1F - this.world.rand.nextFloat() * 0.2F;
         float fz = 0.1F - this.world.rand.nextFloat() * 0.2F;
         float fy = 0.1F - this.world.rand.nextFloat() * 0.2F;
         float fx2 = 0.1F - this.world.rand.nextFloat() * 0.2F;
         float fz2 = 0.1F - this.world.rand.nextFloat() * 0.2F;
         float fy2 = 0.1F - this.world.rand.nextFloat() * 0.2F;
         int color = 16777215;
         Thaumcraft.proxy.drawVentParticles(this.world, (float)this.getPos().getX() + 0.5F + fx + (float)this.facing.getXOffset() / 2.0F, (float)this.getPos().getY() + 0.5F + fy, (float)this.getPos().getZ() + 0.5F + fz + (float)this.facing.getZOffset() / 2.0F, (float)this.facing.getXOffset() / 4.0F + fx2, fy2, (float)this.facing.getZOffset() / 4.0F + fz2, color);
      }

   }

   private void completeRecipe() {
      if (this.currentRecipe != null && this.currentCraft < this.recipeHash.size() && this.currentRecipe.matches(this.essentia, this.inputStack) && this.decrStackSize(0, 1) != null) {
         this.essentia = new AspectList();
         ItemStack dropped = this.getCurrentOutputRecipe();
         EntityPlayer p = this.world.getPlayerEntityByName(this.recipePlayer.get(this.currentCraft));
         if (p != null) {
            FMLCommonHandler.instance().firePlayerCraftingEvent(p, dropped, new InventoryFake(new ItemStack[]{this.inputStack}));
         }

         TileEntity inventory = this.world.getTileEntity(this.getPos().add(this.facing.getXOffset(), 0, this.facing.getZOffset()));
         if (inventory instanceof IInventory) {
            dropped = InventoryUtils.placeItemStackIntoInventory(dropped, (IInventory)inventory, this.facing.getOpposite().ordinal(), true);
         }

         if (dropped != null) {
            EntityItem ei = new EntityItem(this.world, (double)this.getPos().getX() + (double)0.5F + (double)this.facing.getXOffset() * 0.66, (double)this.getPos().getY() + 0.33 + (double)this.facing.getOpposite().getYOffset(), (double)this.getPos().getZ() + (double)0.5F + (double)this.facing.getZOffset() * 0.66, dropped.copy());
            ei.motionX = 0.075F * (float)this.facing.getXOffset();
            ei.motionY = 0.025F;
            ei.motionZ = 0.075F * (float)this.facing.getZOffset();
            this.world.spawnEntity(ei);
            this.world.addBlockEvent(new BlockPos(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ()), this.getBlockType(), 0, 0);
         }

         net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("minecraft:block.fire.extinguish"));
         if (_snd != null) this.world.playSound(null, this.getPos(), _snd, net.minecraft.util.SoundCategory.BLOCKS, 0.25F, 2.6F + (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.8F);
         this.currentCraft = -1;
         { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
         this.markDirty();
      }

   }

   void fill() {
      TileEntity te = null;
      IEssentiaTransport ic = null;

      for(int y = 0; y <= 1; ++y) {
         for(EnumFacing dir : EnumFacing.values()) {
            if (dir != this.facing && dir != EnumFacing.DOWN && (y != 0 || dir != EnumFacing.UP)) {
               te = ThaumcraftApiHelper.getConnectableTile(this.world, this.getPos().getX(), this.getPos().getY() + y, this.getPos().getZ(), dir);
               if (te != null) {
                  ic = (IEssentiaTransport)te;
                  if (ic.getEssentiaAmount(dir.getOpposite()) > 0 && ic.getSuctionAmount(dir.getOpposite()) < this.getSuctionAmount(null) && this.getSuctionAmount(null) >= ic.getMinimumSuction()) {
                     int ess = ic.takeEssentia(this.currentSuction, 1, dir.getOpposite());
                     if (ess > 0) {
                        this.addToContainer(this.currentSuction, ess);
                        return;
                     }
                  }
               }
            }
         }
      }

   }

   public int addToContainer(Aspect tt, int am) {
      int ce = this.currentRecipe.aspects.getAmount(tt) - this.essentia.getAmount(tt);
      if (this.currentRecipe != null && ce > 0) {
         int add = Math.min(ce, am);
         this.essentia.add(tt, add);
         { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
         this.markDirty();
         return am - add;
      } else {
         return am;
      }
   }

   public boolean takeFromContainer(Aspect tt, int am) {
      if (this.essentia.getAmount(tt) >= am) {
         this.essentia.remove(tt, am);
         { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
         this.markDirty();
         return true;
      } else {
         return false;
      }
   }

   public boolean takeFromContainer(AspectList ot) {
      return false;
   }

   public boolean doesContainerContain(AspectList ot) {
      return false;
   }

   public boolean doesContainerContainAmount(Aspect tt, int am) {
      return this.essentia.getAmount(tt) >= am;
   }

   public int containerContains(Aspect tt) {
      return this.essentia.getAmount(tt);
   }

   public boolean doesContainerAccept(Aspect tag) {
      return true;
   }

   public boolean receiveClientEvent(int i, int j) {
      if (i >= 0) {
         if (this.world.isRemote) {
            this.venting = 7;
         }

         return true;
      } else {
         return super.receiveClientEvent(i, j);
      }
   }

   public boolean isConnectable(EnumFacing face) {
      return face != this.facing;
   }

   public boolean canInputFrom(EnumFacing face) {
      return face != this.facing;
   }

   public boolean canOutputTo(EnumFacing face) {
      return false;
   }

   public void setSuction(Aspect aspect, int amount) {
      this.currentSuction = aspect;
   }

   public Aspect getSuctionType(EnumFacing loc) {
      return this.currentSuction;
   }

   public int getSuctionAmount(EnumFacing loc) {
      return this.currentSuction != null ? 128 : 0;
   }

   public Aspect getEssentiaType(EnumFacing loc) {
      return null;
   }

   public int getEssentiaAmount(EnumFacing loc) {
      return 0;
   }

   public int takeEssentia(Aspect aspect, int amount, EnumFacing face) {
      return this.canOutputTo(face) && this.takeFromContainer(aspect, amount) ? amount : 0;
   }

   public int addEssentia(Aspect aspect, int amount, EnumFacing face) {
      return this.canInputFrom(face) ? amount - this.addToContainer(aspect, amount) : 0;
   }

   public int getMinimumSuction() {
      return 0;
   }

   public boolean renderExtendedTube() {
      return false;
   }

   public AspectList getAspects() {
      return this.essentia;
   }

   public void setAspects(AspectList aspects) {
      this.essentia = aspects;
   }

   public int getSizeInventory() {
      return 1;
   }

   public boolean isEmpty() {
      return this.inputStack == null || this.inputStack.isEmpty();
   }

   public ItemStack getStackInSlot(int par1) {
      return this.inputStack == null ? ItemStack.EMPTY : this.inputStack;
   }

   public ItemStack decrStackSize(int par1, int par2) {
      if (this.inputStack != null && !this.inputStack.isEmpty()) {
          ItemStack itemstack;
          if (this.inputStack.getCount() <= par2) {
              itemstack = this.inputStack;
              this.inputStack = ItemStack.EMPTY;
          } else {
              itemstack = this.inputStack.splitStack(par2);
              if (this.inputStack.isEmpty()) {
                 this.inputStack = ItemStack.EMPTY;
              }
          }
          if (this.eventHandler != null) {
             this.eventHandler.onCraftMatrixChanged(this);
          }
          return itemstack;
      } else {
         return ItemStack.EMPTY;
      }
   }

   public ItemStack removeStackFromSlot(int par1) {
      if (this.inputStack != null && !this.inputStack.isEmpty()) {
         ItemStack itemstack = this.inputStack;
         this.inputStack = ItemStack.EMPTY;
         return itemstack;
      } else {
         return ItemStack.EMPTY;
      }
   }

   public void setInventorySlotContents(int par1, ItemStack par2ItemStack) {
      this.inputStack = par2ItemStack == null ? ItemStack.EMPTY : par2ItemStack;
      if (!this.inputStack.isEmpty() && this.inputStack.getCount() > this.getInventoryStackLimit()) {
         this.inputStack.setCount(this.getInventoryStackLimit());
      }

      if (this.eventHandler != null) {
         this.eventHandler.onCraftMatrixChanged(this);
      }

   }

   public String getName() {
      return "container.alchemyfurnace";
   }

   public boolean hasCustomName() {
      return false;
   }

   public ITextComponent getDisplayName() {
      return new TextComponentTranslation(getName());
   }

   public int getInventoryStackLimit() {
      return 64;
   }

   public boolean isUsableByPlayer(EntityPlayer par1EntityPlayer) {
      return this.world.getTileEntity(this.getPos()) == this && par1EntityPlayer.getDistanceSq((double) this.getPos().getX() + (double) 0.5F, (double) this.getPos().getY() + (double) 0.5F, (double) this.getPos().getZ() + (double) 0.5F) <= (double) 64.0F;
   }

   public void openInventory(EntityPlayer player) {
   }

   public void closeInventory(EntityPlayer player) {
   }

   public boolean isItemValidForSlot(int par1, ItemStack par2ItemStack) {
      return true;
   }

   public void clear() {
      this.inputStack = null;
   }

   public int getFieldCount() {
      return 0;
   }

   public int getField(int id) {
      return 0;
   }

   public void setField(int id, int value) {
   }

   public int[] getSlotsForFace(EnumFacing side) {
      return new int[]{0};
   }

   public boolean canInsertItem(int par1, ItemStack par2ItemStack, EnumFacing par3) {
      return true;
   }

   public boolean canExtractItem(int par1, ItemStack par2ItemStack, EnumFacing par3) {
      return true;
   }

   public boolean gettingPower() {
      return this.world.getRedstonePowerFromNeighbors(new BlockPos(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ())) > 0
          || this.world.getRedstonePowerFromNeighbors(new BlockPos(this.getPos().getX(), this.getPos().getY() - 1, this.getPos().getZ())) > 0
          || this.world.getRedstonePowerFromNeighbors(new BlockPos(this.getPos().getX(), this.getPos().getY() + 1, this.getPos().getZ())) > 0;
   }

   public void getUpgrades() {
      int mr = 1;

      for(int yy = 0; yy <= 1; ++yy) {
         for(EnumFacing dir : EnumFacing.values()) {
            if (dir != EnumFacing.DOWN && dir != this.facing) {
               int xx = this.getPos().getX() + dir.getXOffset();
               int zz = this.getPos().getZ() + dir.getZOffset();
               int targetY = this.getPos().getY() + yy + dir.getYOffset();
               BlockPos targetPos = new BlockPos(xx, targetY, zz);
               Block bi = this.world.getBlockState(targetPos).getBlock();
               int md = bi.getMetaFromState(this.world.getBlockState(targetPos));
               if (bi == ConfigBlocks.blockMetalDevice && md == 12) {
                  TileEntity te = this.world.getTileEntity(targetPos);
                  if (te instanceof TileBrainbox && ((TileBrainbox) te).facing == dir.getOpposite()) {
                     mr += 2;
                  }
               }
            }
         }
      }

      if (mr != this.maxRecipes) {
         this.maxRecipes = mr;

         while(this.recipeHash.size() > this.maxRecipes) {
            this.recipeHash.remove(this.recipeHash.size() - 1);
         }

         { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
         this.markDirty();
      }

   }
}
