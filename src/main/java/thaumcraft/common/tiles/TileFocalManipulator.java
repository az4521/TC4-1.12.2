package thaumcraft.common.tiles;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.visnet.VisNetHandler;
import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.lib.research.ResearchManager;
import net.minecraft.util.math.BlockPos;

public class TileFocalManipulator extends TileThaumcraftInventory implements ITickable {
   public AspectList aspects = new AspectList();
   public int size = 0;
   public int upgrade = -1;
   public int rank = -1;
   int ticks = 0;
   public boolean reset = false;
   public static final int XP_MULT = 8;
   public static final int VIS_MULT = 200;

   public TileFocalManipulator() {
      this.itemStacks = new ItemStack[1];
      this.syncedSlots = new int[]{0};
   }

   public void readCustomNBT(NBTTagCompound nbttagcompound) {
      this.itemStacks = new ItemStack[1];
      this.syncedSlots = new int[]{0};
      super.readCustomNBT(nbttagcompound);
      this.aspects.readFromNBT(nbttagcompound);
      this.size = nbttagcompound.getInteger("size");
      this.upgrade = nbttagcompound.getInteger("upgrade");
      this.rank = nbttagcompound.getInteger("rank");
   }

   public void writeCustomNBT(NBTTagCompound nbttagcompound) {
      super.writeCustomNBT(nbttagcompound);
      this.aspects.writeToNBT(nbttagcompound);
      nbttagcompound.setInteger("size", this.size);
      nbttagcompound.setInteger("upgrade", this.upgrade);
      nbttagcompound.setInteger("rank", this.rank);
   }

   @SideOnly(Side.CLIENT)
   public AxisAlignedBB getRenderBoundingBox() {
      return new AxisAlignedBB(this.getPos().getX(), this.getPos().getY() - 1, this.getPos().getZ(), this.getPos().getX() + 1, this.getPos().getY() + 1, this.getPos().getZ() + 1);
   }

   public void setInventorySlotContents(int par1, ItemStack par2ItemStack) {
      super.setInventorySlotContents(par1, par2ItemStack);
      if (this.world.isRemote) {
         this.reset = true;
      } else {
         this.aspects = new AspectList();
      }

   }

   @Override
   public void update() {
      boolean complete = false;
      if (!this.world.isRemote) {
         if (this.rank < 0) {
            this.rank = 0;
         }

         ++this.ticks;
         if (this.ticks % 5 == 0) {
            if (this.size > 0 && (this.aspects.visSize() <= 0 || this.getStackInSlot(0).isEmpty())) {
               complete = true;
               this.world.playSound(null, this.getPos(), SoundEvent.REGISTRY.getObject(new ResourceLocation("thaumcraft", "craftfail")), SoundCategory.BLOCKS, 0.33F, 1.0F);
            }

            if (this.size > 0) {
               for(Aspect aspect : this.aspects.getAspectsSortedAmount()) {
                  int drain = VisNetHandler.drainVis(this.world, this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), aspect, Math.min(100, this.aspects.getAmount(aspect)));
                  if (drain > 0) {
                     this.aspects.reduce(aspect, drain);
                     { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
                     this.markDirty();
                  }
               }

               if (this.aspects.visSize() <= 0 && !this.getStackInSlot(0).isEmpty()) {
                  complete = true;
                  ItemFocusBasic focus = (ItemFocusBasic)this.getStackInSlot(0).getItem();
                  focus.applyUpgrade(this.getStackInSlot(0), FocusUpgradeType.types[this.upgrade], this.rank);
                  this.world.playSound(null, this.getPos(), SoundEvent.REGISTRY.getObject(new ResourceLocation("thaumcraft", "wand")), SoundCategory.BLOCKS, 1.0F, 1.0F);
               }
            }
         }
      } else if (this.size > 0) {
         Thaumcraft.proxy.drawGenericParticles(this.world, (double)this.getPos().getX() + (double)0.5F + (double)((this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.3F), (double)this.getPos().getY() + (double)1.25F + (double)((this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.3F), (double)this.getPos().getZ() + (double)0.5F + (double)((this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.3F), 0.0F, 0.0F, 0.0F, 0.5F + this.world.rand.nextFloat() * 0.4F, 1.0F - this.world.rand.nextFloat() * 0.4F, 1.0F - this.world.rand.nextFloat() * 0.4F, 0.8F, false, 112, 9, 1, 6 + this.world.rand.nextInt(5), 0, 0.7F + this.world.rand.nextFloat() * 0.4F);
      }

      if (complete) {
         this.size = 0;
         this.rank = -1;
         this.aspects = new AspectList();
         { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
         this.markDirty();
      }

   }

   public boolean startCraft(int id, EntityPlayer p) {
      if (this.size <= 0 && !this.getStackInSlot(0).isEmpty() && this.getStackInSlot(0).getItem() instanceof ItemFocusBasic) {
         ItemFocusBasic focus = (ItemFocusBasic)this.getStackInSlot(0).getItem();
         short[] s = focus.getAppliedUpgrades(this.getStackInSlot(0));

         for(this.rank = 1; this.rank <= 5 && s[this.rank - 1] != -1; ++this.rank) {
         }

         int xp = this.rank * 8;
         if (p.experienceLevel < xp) {
            return false;
         } else {
            FocusUpgradeType[] ut = focus.getPossibleUpgradesByRank(this.getStackInSlot(0), this.rank);
            if (ut == null) {
               return false;
            } else {
               boolean b = false;

                for (FocusUpgradeType focusUpgradeType : ut) {
                    if (focusUpgradeType.id == id) {
                        b = true;
                        break;
                    }
                }

               if (!b) {
                  return false;
               } else if (id <= FocusUpgradeType.types.length - 1 && FocusUpgradeType.types[id] != null && focus.canApplyUpgrade(this.getStackInSlot(0), p, FocusUpgradeType.types[id], this.rank)) {
                  int amt = 200;

                  for(int a = 1; a < this.rank; ++a) {
                     amt *= 2;
                  }

                  AspectList tal = new AspectList();

                  for(Aspect as : FocusUpgradeType.types[id].aspects.getAspects()) {
                     tal.add(as, amt);
                  }

                  this.aspects = ResearchManager.reduceToPrimals(tal);
                  this.size = this.aspects.visSize();
                  this.upgrade = id;
                  if (!p.capabilities.isCreativeMode) {
                     p.addExperienceLevel(-xp);
                  }

                  this.markDirty();
                  { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
                  this.world.playSound(null, this.getPos(), SoundEvent.REGISTRY.getObject(new ResourceLocation("thaumcraft", "craftstart")), SoundCategory.BLOCKS, 0.25F, 1.0F);
                  return true;
               } else {
                  return false;
               }
            }
         }
      } else {
         return false;
      }
   }

   public boolean isItemValidForSlot(int par1, ItemStack par2ItemStack) {
      return par2ItemStack != null && par2ItemStack.getItem() instanceof ItemFocusBasic;
   }
}
