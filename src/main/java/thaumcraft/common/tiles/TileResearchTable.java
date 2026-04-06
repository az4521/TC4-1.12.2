package thaumcraft.common.tiles;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.translation.I18n;
import thaumcraft.api.IScribeTools;
import thaumcraft.api.TileThaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.items.ItemResearchNotes;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.playerdata.PacketAspectPool;
import thaumcraft.common.lib.research.ResearchManager;
import thaumcraft.common.lib.research.ResearchNoteData;
import thaumcraft.common.lib.utils.HexUtils;
import thaumcraft.common.lib.utils.InventoryUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.block.state.IBlockState;

public class TileResearchTable extends TileThaumcraft implements IInventory, net.minecraft.util.ITickable {
   public ItemStack[] contents = new ItemStack[2];
   public AspectList bonusAspects = new AspectList();
   int nextRecalc = 0;
   EntityPlayer researcher = null;
   public ResearchNoteData data = null;

   public void readCustomNBT(NBTTagCompound nbttagcompound) {
      NBTTagList var2 = nbttagcompound.getTagList("Inventory", 10);
      this.contents = new ItemStack[this.getSizeInventory()];

      for(int var3 = 0; var3 < Math.min(2, var2.tagCount()); ++var3) {
         NBTTagCompound var4 = var2.getCompoundTagAt(var3);
         int var5 = var4.getByte("Slot") & 255;
         if (var5 >= 0 && var5 < this.contents.length) {
            this.contents[var5] = new ItemStack(var4);
         }
      }

      this.nextRecalc = nbttagcompound.getInteger("nextRecalc");
      this.bonusAspects = new AspectList();
      var2 = nbttagcompound.getTagList("bonusAspects", 10);

      for(int var3 = 0; var3 < var2.tagCount(); ++var3) {
         NBTTagCompound var4 = var2.getCompoundTagAt(var3);
         String var5 = var4.getString("tag");
         if (Aspect.getAspect(var5) != null) {
            this.bonusAspects.merge(Aspect.getAspect(var5), 1);
         }
      }

   }

   public void writeCustomNBT(NBTTagCompound nbttagcompound) {
      NBTTagList var2 = new NBTTagList();

      for(int var3 = 0; var3 < this.contents.length; ++var3) {
         if (this.contents[var3] != null) {
            NBTTagCompound var4 = new NBTTagCompound();
            var4.setByte("Slot", (byte)var3);
            this.contents[var3].writeToNBT(var4);
            var2.appendTag(var4);
         }
      }

      nbttagcompound.setTag("Inventory", var2);
      nbttagcompound.setInteger("nextRecalc", this.nextRecalc);
      var2 = new NBTTagList();

      for(Aspect aspect : this.bonusAspects.getAspects()) {
         if (aspect != null && this.bonusAspects.getAmount(aspect) > 0) {
            NBTTagCompound var4 = new NBTTagCompound();
            var4.setString("tag", aspect.getTag());
            var2.appendTag(var4);
         }
      }

      nbttagcompound.setTag("bonusAspects", var2);
   }

   public void update() {
      if (!this.world.isRemote && this.nextRecalc++ > 600) {
         this.nextRecalc = 0;
         this.recalculateBonus();
         { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
         this.markDirty();
      }

   }

   public boolean canUpdate() {
       return true;
   }

   public void markDirty() {
      super.markDirty();
      this.gatherResults();
   }

   public void gatherResults() {
      this.data = null;
      if (this.contents[1] != null && this.contents[1].getItem() instanceof ItemResearchNotes) {
         this.data = ResearchManager.getData(this.contents[1]);
      }

   }

   public void placeAspect(int q, int r, Aspect aspect, EntityPlayer player) {
      if (this.data == null) {
         this.gatherResults();
      }

      if (ResearchManager.consumeInkFromTable(this.contents[0], false)) {
         if (this.contents[1] != null && this.contents[1].getItem() instanceof ItemResearchNotes && this.data != null && this.contents[1].getItemDamage() < 64) {
            boolean r1 = ResearchManager.isResearchComplete(player.getName(), "RESEARCHER1");
            boolean r2 = ResearchManager.isResearchComplete(player.getName(), "RESEARCHER2");
            HexUtils.Hex hex = new HexUtils.Hex(q, r);
            ResearchManager.HexEntry he = null;
            if (aspect != null) {
               he = new ResearchManager.HexEntry(aspect, 2);
               if (r2 && this.world.rand.nextFloat() < 0.1F) {
                  player.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.2F, 0.9F + player.world.rand.nextFloat() * 0.2F);
               } else if (Thaumcraft.proxy.playerKnowledge.getAspectPoolFor(player.getName(), aspect) <= 0) {
                  this.bonusAspects.remove(aspect, 1);
                  { net.minecraft.block.state.IBlockState _bs = player.world.getBlockState(this.pos); player.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
                  this.markDirty();
               } else {
                  Thaumcraft.proxy.playerKnowledge.addAspectPool(player.getName(), aspect, (short)-1);
                  ResearchManager.scheduleSave(player);
                  PacketHandler.INSTANCE.sendTo(new PacketAspectPool(aspect.getTag(), (short) 0, Thaumcraft.proxy.playerKnowledge.getAspectPoolFor(player.getName(), aspect)), (EntityPlayerMP)player);
               }
            } else {
               float f = this.world.rand.nextFloat();
               if (this.data.hexEntries.get(hex.toString()).aspect != null && (r1 && f < 0.25F || r2 && f < 0.5F)) {
                  player.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.2F, 0.9F + player.world.rand.nextFloat() * 0.2F);
                  Thaumcraft.proxy.playerKnowledge.addAspectPool(player.getName(), this.data.hexEntries.get(hex.toString()).aspect, (short)1);
                  ResearchManager.scheduleSave(player);
                  PacketHandler.INSTANCE.sendTo(new PacketAspectPool(this.data.hexEntries.get(hex.toString()).aspect.getTag(), (short) 0, Thaumcraft.proxy.playerKnowledge.getAspectPoolFor(player.getName(), this.data.hexEntries.get(hex.toString()).aspect)), (EntityPlayerMP)player);
               }

               he = new ResearchManager.HexEntry(null, 0);
            }

            this.data.hexEntries.put(hex.toString(), he);
            this.data.hexes.put(hex.toString(), hex);
            ResearchManager.updateData(this.contents[1], this.data);
            ResearchManager.consumeInkFromTable(this.contents[0], true);
            if (!this.world.isRemote && ResearchManager.checkResearchCompletion(this.contents[1], this.data, player.getName())) {
               this.contents[1].setItemDamage(64);
               this.world.addBlockEvent(new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ()), ConfigBlocks.blockTable, 1, 1);
            }
         }

         { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
         this.markDirty();
      }
   }

   private void recalculateBonus() {
      if (!this.world.isDaytime() && this.world.getLightFor(net.minecraft.world.EnumSkyBlock.BLOCK, new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY() + 1, this.getPos().getZ())) < 4 && !this.world.canSeeSky(new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY() + 1, this.getPos().getZ())) && this.world.rand.nextInt(20) == 0) {
         this.bonusAspects.merge(Aspect.ENTROPY, 1);
      }

      if ((float)this.getPos().getY() > (float)this.world.getActualHeight() * 0.5F && this.world.rand.nextInt(20) == 0) {
         this.bonusAspects.merge(Aspect.AIR, 1);
      }

      if ((float)this.getPos().getY() > (float)this.world.getActualHeight() * 0.66F && this.world.rand.nextInt(20) == 0) {
         this.bonusAspects.merge(Aspect.AIR, 1);
      }

      if ((float)this.getPos().getY() > (float)this.world.getActualHeight() * 0.75F && this.world.rand.nextInt(20) == 0) {
         this.bonusAspects.merge(Aspect.AIR, 1);
      }

      for(int x = -8; x <= 8; ++x) {
         for(int z = -8; z <= 8; ++z) {
            for(int y = -8; y <= 8; ++y) {
               if (y + this.getPos().getY() > 0 && y + this.getPos().getY() < this.world.getActualHeight()) {
                  IBlockState _bs = this.world.getBlockState(new net.minecraft.util.math.BlockPos(x + this.getPos().getX(), y + this.getPos().getY(), z + this.getPos().getZ()));
                  Block bi = _bs.getBlock();
                  int md = bi.getMetaFromState(_bs);
                  Material bm = _bs.getMaterial();
                  if (bi == ConfigBlocks.blockCustomOre && md == 1) {
                     if (this.bonusAspects.getAmount(Aspect.AIR) < 1 && this.world.rand.nextInt(20) == 0) {
                        this.bonusAspects.merge(Aspect.AIR, 1);
                        return;
                     }
                  } else if (bi == ConfigBlocks.blockCrystal && md == 0) {
                     if (this.bonusAspects.getAmount(Aspect.AIR) < 1 && this.world.rand.nextInt(10) == 0) {
                        this.bonusAspects.merge(Aspect.AIR, 1);
                        return;
                     }
                  }
                  else if (bm != Material.FIRE && bm != Material.LAVA && (bi != ConfigBlocks.blockCustomOre || md != 2)) {
                     if (bi == ConfigBlocks.blockCrystal && md == 1) {
                        if (this.bonusAspects.getAmount(Aspect.FIRE) < 1 && this.world.rand.nextInt(10) == 0) {
                           this.bonusAspects.merge(Aspect.FIRE, 1);
                           return;
                        }
                     } else if (bm == Material.GROUND) {
                        if (this.bonusAspects.getAmount(Aspect.EARTH) < 1 && this.world.rand.nextInt(20) == 0) {
                           this.bonusAspects.merge(Aspect.EARTH, 1);
                           return;
                        }
                     } else if (bi == ConfigBlocks.blockCustomOre && md == 4) {
                        if (this.bonusAspects.getAmount(Aspect.EARTH) < 1 && this.world.rand.nextInt(20) == 0) {
                           this.bonusAspects.merge(Aspect.EARTH, 1);
                           return;
                        }
                     } else if (bi == ConfigBlocks.blockCrystal && md == 3) {
                        if (this.bonusAspects.getAmount(Aspect.EARTH) < 1 && this.world.rand.nextInt(10) == 0) {
                           this.bonusAspects.merge(Aspect.EARTH, 1);
                           return;
                        }
                     } else if (bm == Material.WATER) {
                        if (this.bonusAspects.getAmount(Aspect.WATER) < 1 && this.world.rand.nextInt(15) == 0) {
                           this.bonusAspects.merge(Aspect.WATER, 1);
                           return;
                        }
                     } else if (bi == ConfigBlocks.blockCustomOre && md == 3) {
                        if (this.bonusAspects.getAmount(Aspect.WATER) < 1 && this.world.rand.nextInt(20) == 0) {
                           this.bonusAspects.merge(Aspect.WATER, 1);
                           return;
                        }
                     } else if (bi == ConfigBlocks.blockCrystal && md == 2) {
                        if (this.bonusAspects.getAmount(Aspect.WATER) < 1 && this.world.rand.nextInt(10) == 0) {
                           this.bonusAspects.merge(Aspect.WATER, 1);
                           return;
                        }
                     } else if (bm != Material.CIRCUITS && bm != Material.PISTON) {
                        if (bi == ConfigBlocks.blockCustomOre && md == 5) {
                           if (this.bonusAspects.getAmount(Aspect.ORDER) < 1 && this.world.rand.nextInt(20) == 0) {
                              this.bonusAspects.merge(Aspect.ORDER, 1);
                              return;
                           }
                        } else if (bi == ConfigBlocks.blockCrystal && md == 4) {
                           if (this.bonusAspects.getAmount(Aspect.ORDER) < 1 && this.world.rand.nextInt(10) == 0) {
                              this.bonusAspects.merge(Aspect.ORDER, 1);
                              return;
                           }
                        } else if (bi == ConfigBlocks.blockCustomOre && md == 6) {
                           if (this.bonusAspects.getAmount(Aspect.ENTROPY) < 1 && this.world.rand.nextInt(20) == 0) {
                              this.bonusAspects.merge(Aspect.ENTROPY, 1);
                              return;
                           }
                        } else if (bi == ConfigBlocks.blockCrystal && md == 5
                                && this.bonusAspects.getAmount(Aspect.ENTROPY) < 1 && this.world.rand.nextInt(10) == 0) {
                           this.bonusAspects.merge(Aspect.ENTROPY, 1);
                           return;
                        }
                     } else if (this.bonusAspects.getAmount(Aspect.ORDER) < 1 && this.world.rand.nextInt(20) == 0) {
                        this.bonusAspects.merge(Aspect.ORDER, 1);
                        return;
                     }
                  }
                  else if (this.bonusAspects.getAmount(Aspect.FIRE) < 1 && this.world.rand.nextInt(20) == 0) {
                     this.bonusAspects.merge(Aspect.FIRE, 1);
                     return;
                  }

                  if ((bi == Blocks.BOOKSHELF
                          && this.world.rand.nextInt(300) == 0)
                          ||
                          (bi == ConfigBlocks.blockJar
                                  && md == 1
                                  && this.world.rand.nextInt(200) == 0)) {
                     Aspect[] aspects = new Aspect[0];
                     aspects = Aspect.aspects.values().toArray(aspects);
                     this.bonusAspects.merge(aspects[this.world.rand.nextInt(aspects.length)], 1);
                     return;
                  }
               }
            }
         }
      }

   }

   public void clear() { java.util.Arrays.fill(this.contents, null); }

   public int getSizeInventory() {
      return 2;
   }

   public ItemStack getStackInSlot(int var1) {
      ItemStack s = this.contents[var1]; return s != null ? s : ItemStack.EMPTY;
   }

   public ItemStack decrStackSize(int var1, int var2) {
      if (this.contents[var1] != null) {
          ItemStack var3;
          if (this.contents[var1].getCount() <= var2) {
              var3 = this.contents[var1];
            this.contents[var1] = null;
          } else {
              var3 = this.contents[var1].splitStack(var2);
            if (this.contents[var1].isEmpty()) {
               this.contents[var1] = null;
            }

          }
          this.markDirty();
          return var3;
      } else {
         return null;
      }
   }

   public ItemStack getStackInSlotOnClosing(int var1) {
      if (this.contents[var1] != null) {
         ItemStack var2 = this.contents[var1];
         this.contents[var1] = null;
         return var2;
      } else {
         return null;
      }
   }

   public ItemStack removeStackFromSlot(int index) {
      if (this.contents[index] != null) {
         ItemStack itemstack = this.contents[index];
         this.contents[index] = null;
         return itemstack;
      }
      return null;
   }

   public void setInventorySlotContents(int var1, ItemStack var2) {
      this.contents[var1] = var2;
      if (var2 != null && var2.getCount() > this.getInventoryStackLimit()) {
         var2.setCount(this.getInventoryStackLimit());
      }

      this.markDirty();
   }

   public String getInventoryName() {
      return I18n.translateToLocal("tile.blockTable.research.name");
   }

   public int getInventoryStackLimit() {
      return 64;
   }

   public boolean isUsableByPlayer(EntityPlayer var1) {
      return this.world.getTileEntity(this.getPos()) == this && var1.getDistanceSq((double) this.getPos().getX() + (double) 0.5F, (double) this.getPos().getY() + (double) 0.5F, (double) this.getPos().getZ() + (double) 0.5F) <= (double) 64.0F;
   }

   public void openInventory(EntityPlayer player) {
   }

   public void closeInventory(EntityPlayer player) {
   }

   public boolean hasCustomInventoryName() {
      return false;
   }

   public boolean hasCustomName() {
      return false;
   }

   public String getName() {
      return I18n.translateToLocal("tile.blockTable.research.name");
   }

   public boolean isItemValidForSlot(int i, ItemStack itemstack) {
       if (itemstack != null) {
           switch (i) {
               case 0:
                   if (itemstack.getItem() instanceof IScribeTools) {
                       return true;
                   }
                   break;
               case 1:
                   if (itemstack.getItem() == ConfigItems.itemResearchNotes && itemstack.getItemDamage() < 64) {
                       return true;
                   }
           }

       }
       return false;
   }

   @SideOnly(Side.CLIENT)
   public AxisAlignedBB getRenderBoundingBox() {
      return new AxisAlignedBB(this.getPos().getX() - 1, this.getPos().getY(), this.getPos().getZ() - 1, this.getPos().getX() + 2, this.getPos().getY() + 2, this.getPos().getZ() + 2);
   }

   public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
      super.onDataPacket(net, pkt);
      if (this.world != null && this.world.isRemote) {
         { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
      }

   }

   public boolean receiveClientEvent(int i, int j) {
      if (i == 1) {
         if (this.world.isRemote) {
            this.world.playSound(null, this.getPos(), net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft", "learn")), SoundCategory.BLOCKS, 1.0F, 1.0F);
         }

         return true;
      } else {
         return super.receiveClientEvent(i, j);
      }
   }

   public void duplicate(EntityPlayer player) {
      if (this.data == null) {
         this.gatherResults();
      }

      if (this.contents[1] != null
              && this.contents[1].getItem() instanceof ItemResearchNotes
              && this.contents[1].getItemDamage() == 64
              && this.data != null
              && InventoryUtils.isPlayerCarrying(player, new ItemStack(Items.PAPER)) >= 0
              && InventoryUtils.isPlayerCarrying(player, new ItemStack(Items.DYE, 1, 0)) >= 0
      ) {
         ResearchItem rr = ResearchCategories.getResearch(this.data.key);

         for(Aspect aspect : rr.tags.getAspects()) {
            if (Thaumcraft.proxy.playerKnowledge.getAspectPoolFor(player.getName(), aspect) < rr.tags.getAmount(aspect) + this.data.copies) {
               return;
            }
         }

         for(Aspect aspect : rr.tags.getAspects()) {
            Thaumcraft.proxy.playerKnowledge.addAspectPool(player.getName(), aspect, (short)(-(rr.tags.getAmount(aspect) + this.data.copies)));
            ResearchManager.scheduleSave(player);
            PacketHandler.INSTANCE.sendTo(new PacketAspectPool(aspect.getTag(), (short) 0, Thaumcraft.proxy.playerKnowledge.getAspectPoolFor(player.getName(), aspect)), (EntityPlayerMP)player);
         }

         InventoryUtils.consumeInventoryItem(player, Items.PAPER, 0);
         InventoryUtils.consumeInventoryItem(player, Items.DYE, 0);
         this.world.addBlockEvent(new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ()), ConfigBlocks.blockTable, 1, 1);
         ++this.data.copies;
         ResearchManager.updateData(this.contents[1], this.data);
         this.contents[1].grow(1);
         { net.minecraft.block.state.IBlockState _bs = this.world.getBlockState(this.pos); this.world.notifyBlockUpdate(this.pos, _bs, _bs, 3); }
         this.markDirty();
      }
   }

   public boolean isEmpty() { for (ItemStack s : contents) { if (s != null && !s.isEmpty()) return false; } return true; }
   public int getField(int id) { return 0; }
   public void setField(int id, int value) {}
   public int getFieldCount() { return 0; }
}