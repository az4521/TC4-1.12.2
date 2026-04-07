package thaumcraft.common.lib.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.enchantment.EnchantmentDurability;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumcraft.common.tiles.TileArcaneWorkbench;
import thaumcraft.common.tiles.TileResearchTable;

public class InventoryUtils {
   public static ItemStack placeItemStackIntoInventory(ItemStack stack, IInventory inventory, int side, boolean doit) {
      ItemStack itemstack = stack.copy();
      ItemStack itemstack1 = insertStack(inventory, itemstack, side, doit);
      if (itemstack1 != null && itemstack1.getCount() != 0) {
         return itemstack1.copy();
      } else {
         if (doit) {
            inventory.markDirty();
         }

         return ItemStack.EMPTY;
      }
   }

   public static ItemStack insertStack(IInventory inventory, ItemStack stack1, int side, boolean doit) {
      if (inventory instanceof ISidedInventory && side > -1) {
         ISidedInventory isidedinventory = (ISidedInventory)inventory;
         int[] aint = isidedinventory.getSlotsForFace(EnumFacing.byIndex(side));
         if (aint != null) {
            for(int j = 0; j < aint.length && stack1 != null && stack1.getCount() > 0; ++j) {
               if (!inventory.getStackInSlot(aint[j]).isEmpty() && inventory.getStackInSlot(aint[j]).isItemEqual(stack1)) {
                  stack1 = attemptInsertion(inventory, stack1, aint[j], side, doit);
               }

               if (stack1 == null || stack1.getCount() == 0) {
                  break;
               }
            }
         }

         if (aint != null && stack1 != null && stack1.getCount() > 0) {
            for(int j = 0; j < aint.length && stack1 != null && stack1.getCount() > 0; ++j) {
               stack1 = attemptInsertion(inventory, stack1, aint[j], side, doit);
               if (stack1 == null || stack1.getCount() == 0) {
                  break;
               }
            }
         }
      } else {
         int k = inventory.getSizeInventory();

         for(int l = 0; l < k && stack1 != null && stack1.getCount() > 0; ++l) {
            if (!inventory.getStackInSlot(l).isEmpty() && inventory.getStackInSlot(l).isItemEqual(stack1)) {
               stack1 = attemptInsertion(inventory, stack1, l, side, doit);
            }

            if (stack1 == null || stack1.getCount() == 0) {
               break;
            }
         }

         if (stack1 != null && stack1.getCount() > 0) {
            TileEntityChest dc = null;
            if (inventory instanceof TileEntity) {
               dc = getDoubleChest((TileEntity)inventory);
               if (dc != null) {
                  int k2 = dc.getSizeInventory();

                  for(int l = 0; l < k2 && stack1 != null && stack1.getCount() > 0; ++l) {
                     if (!dc.getStackInSlot(l).isEmpty() && dc.getStackInSlot(l).isItemEqual(stack1)) {
                        stack1 = attemptInsertion(dc, stack1, l, side, doit);
                     }

                     if (stack1 == null || stack1.getCount() == 0) {
                        break;
                     }
                  }
               }
            }

            if (stack1 != null && stack1.getCount() > 0) {
               for(int l = 0; l < k && stack1 != null && stack1.getCount() > 0; ++l) {
                  stack1 = attemptInsertion(inventory, stack1, l, side, doit);
                  if (stack1 == null || stack1.getCount() == 0) {
                     break;
                  }
               }

               if (stack1 != null && stack1.getCount() > 0 && dc != null) {
                  int k2 = dc.getSizeInventory();

                  for(int l = 0; l < k2 && stack1 != null && stack1.getCount() > 0; ++l) {
                     if (!dc.getStackInSlot(l).isEmpty() && dc.getStackInSlot(l).isItemEqual(stack1)) {
                        stack1 = attemptInsertion(dc, stack1, l, side, doit);
                     }

                     if (stack1 == null || stack1.getCount() == 0) {
                        break;
                     }
                  }
               }
            }
         }
      }

      if (stack1 != null && stack1.getCount() == 0) {
         stack1 = null;
      }

      return stack1;
   }

   private static ItemStack attemptInsertion(IInventory inventory, ItemStack stack, int slot, int side, boolean doit) {
      ItemStack slotStack = inventory.getStackInSlot(slot);
      if (canInsertItemToInventory(inventory, stack, slot, side)) {
         boolean flag = false;
         if (slotStack == null || slotStack.isEmpty()) {
            if (inventory.getInventoryStackLimit() < stack.getCount()) {
               ItemStack in = stack.splitStack(inventory.getInventoryStackLimit());
               if (doit) {
                  inventory.setInventorySlotContents(slot, in);
               }
            } else {
               if (doit) {
                  inventory.setInventorySlotContents(slot, stack);
               }

               stack = null;
            }

            flag = true;
         } else if (areItemStacksEqualStrict(slotStack, stack)) {
            int k = Math.min(inventory.getInventoryStackLimit() - slotStack.getCount(), stack.getMaxStackSize() - slotStack.getCount());
            int l = Math.min(stack.getCount(), k);
            stack.setCount(stack.getCount() - l);
            if (doit) {
               slotStack.setCount(slotStack.getCount() + l);
            }

            flag = l > 0;
         }

         if (flag && doit) {
            if (inventory instanceof TileEntityHopper) {
               ((TileEntityHopper)inventory).setTransferCooldown(8);
               inventory.markDirty();
            }

            inventory.markDirty();
         }
      }

      return stack;
   }

   public static ItemStack getFirstItemInInventory(IInventory inventory, int size, int side, boolean doit) {
      ItemStack stack1 = null;
      if (inventory instanceof ISidedInventory && side > -1) {
         ISidedInventory isidedinventory = (ISidedInventory)inventory;
         int[] aint = isidedinventory.getSlotsForFace(EnumFacing.byIndex(side));

          for (int i : aint) {
              if (stack1 == null && !inventory.getStackInSlot(i).isEmpty()) {
                  stack1 = inventory.getStackInSlot(i).copy();
                  stack1.setCount(size);
              }

              if (stack1 != null) {
                  stack1 = attemptExtraction(inventory, stack1, i, side, false, false, false, doit);
              }

              if (stack1 != null) {
                  break;
              }
          }
      } else {
         int k = inventory.getSizeInventory();

         for(int l = 0; l < k; ++l) {
            if (stack1 == null && !inventory.getStackInSlot(l).isEmpty()) {
               stack1 = inventory.getStackInSlot(l).copy();
               stack1.setCount(size);
            }

            if (stack1 != null) {
               stack1 = attemptExtraction(inventory, stack1, l, side, false, false, false, doit);
            }

            if (stack1 != null) {
               break;
            }
         }
      }

      if (stack1 != null && stack1.getCount() != 0) {
         return stack1.copy();
      } else {
         if (doit) {
            inventory.markDirty();
         }

         return ItemStack.EMPTY;
      }
   }

   public static boolean inventoryContains(IInventory inventory, ItemStack stack, int side, boolean useOre, boolean ignoreDamage, boolean ignoreNBT) {
      return !extractStack(inventory, stack, side, useOre, ignoreDamage, ignoreNBT, false).isEmpty();
   }

   public static ItemStack extractStack(IInventory inventory, ItemStack stack1, int side, boolean useOre, boolean ignoreDamage, boolean ignoreNBT, boolean doit) {
      ItemStack outStack = null;
      if (inventory instanceof ISidedInventory && side > -1) {
         ISidedInventory isidedinventory = (ISidedInventory)inventory;
         int[] aint = isidedinventory.getSlotsForFace(EnumFacing.byIndex(side));

         for(int j = 0; j < aint.length && stack1 != null && stack1.getCount() > 0 && outStack == null; ++j) {
            outStack = attemptExtraction(inventory, stack1, aint[j], side, useOre, ignoreDamage, ignoreNBT, doit);
         }
      } else {
         int k = inventory.getSizeInventory();

         for(int l = 0; l < k && stack1 != null && stack1.getCount() > 0 && outStack == null; ++l) {
            outStack = attemptExtraction(inventory, stack1, l, side, useOre, ignoreDamage, ignoreNBT, doit);
         }
      }

      return outStack != null && outStack.getCount() != 0 ? outStack.copy() : ItemStack.EMPTY;
   }

   public static ItemStack attemptExtraction(IInventory inventory, ItemStack stack, int slot, int side, boolean useOre, boolean ignoreDamage, boolean ignoreNBT, boolean doit) {
      ItemStack slotStack = inventory.getStackInSlot(slot);
      ItemStack outStack = stack.copy();
      if (canExtractItemFromInventory(inventory, slotStack, slot, side)) {
         boolean flag = false;
         if (areItemStacksEqual(slotStack, stack, useOre, ignoreDamage, ignoreNBT)) {
            outStack = slotStack.copy();
            outStack.setCount(stack.getCount());
            int k = stack.getCount() - slotStack.getCount();
            if (k >= 0) {
               outStack.setCount(outStack.getCount() - k);
               if (doit) {
                  inventory.setInventorySlotContents(slot, ItemStack.EMPTY);
               }
            } else if (doit) {
               slotStack.setCount(slotStack.getCount() - outStack.getCount());
               inventory.setInventorySlotContents(slot, slotStack);
            }

            flag = true;
            if (flag && doit) {
               inventory.markDirty();
            }

            return outStack;
         } else {
            return ItemStack.EMPTY;
         }
      } else {
         return ItemStack.EMPTY;
      }
   }

   public static boolean canInsertItemToInventory(IInventory inventory, ItemStack stack1, int par2, int par3) {
      return stack1 != null && inventory.isItemValidForSlot(par2, stack1) && (!(inventory instanceof ISidedInventory) || ((ISidedInventory)inventory).canInsertItem(par2, stack1, EnumFacing.byIndex(par3)));
   }

   public static boolean canExtractItemFromInventory(IInventory inventory, ItemStack stack1, int par2, int par3) {
      return stack1 != null && (!(inventory instanceof ISidedInventory) || ((ISidedInventory)inventory).canExtractItem(par2, stack1, EnumFacing.byIndex(par3)));
   }

   public static boolean compareMultipleItems(ItemStack c1, ItemStack[] c2) {
      if (c1 != null && c1.getCount() > 0) {
         for(ItemStack is : c2) {
            if (is != null && c1.isItemEqual(is) && ItemStack.areItemStackTagsEqual(c1, is)) {
               return true;
            }
         }

      }
       return false;
   }

   public static boolean areItemStacksEqualStrict(ItemStack stack0, ItemStack stack1) {
      return areItemStacksEqual(stack0, stack1, false, false, false);
   }

   public static boolean areItemStacksEqualForCrafting(ItemStack stack0, ItemStack stack1, boolean useOre, boolean ignoreDamage, boolean ignoreNBT) {
      if (stack0 == null && stack1 != null) {
         return false;
      } else if (stack0 != null && stack1 == null) {
         return false;
      } else if (stack0 == null && stack1 == null) {
         return true;
      } else {
         if (useOre && !stack0.isEmpty()) {
            int[] oreIDs = OreDictionary.getOreIDs(stack0);
            for (int od : oreIDs) {
               String oreName = OreDictionary.getOreName(od);
               if (!oreName.equals("Unknown")) {
                  ItemStack[] ores = OreDictionary.getOres(oreName).toArray(new ItemStack[0]);
                  if (ThaumcraftApiHelper.containsMatch(false, new ItemStack[]{stack1}, ores)) {
                     return true;
                  }
               }
            }
         }

         boolean t1 = true;
         if (!ignoreNBT) {
            t1 = ThaumcraftApiHelper.areItemStackTagsEqualForCrafting(stack0, stack1);
         }

         boolean t2 = stack0.getItemDamage() != stack1.getItemDamage();
         if (ignoreDamage && stack0.isItemStackDamageable() && stack1.isItemStackDamageable()) {
            t2 = false;
         }

         if (t2 && ignoreDamage && (stack0.getItemDamage() == 32767 || stack1.getItemDamage() == 32767)) {
            t2 = false;
         }

         return stack0.getItem() == stack1.getItem() && (!t2 && (stack0.getCount() <= stack0.getMaxStackSize() && t1));
      }
   }

   public static boolean areItemStacksEqual(ItemStack stack0, ItemStack stack1, boolean useOre, boolean ignoreDamage, boolean ignoreNBT) {
      if (stack0 == null && stack1 != null) {
         return false;
      } else if (stack0 != null && stack1 == null) {
         return false;
      } else if (stack0 == null) {
         return true;
      } else {
         if (useOre && !stack0.isEmpty()) {
            int[] oreIDs = OreDictionary.getOreIDs(stack0);
            for (int od : oreIDs) {
               String oreName = OreDictionary.getOreName(od);
               if (!oreName.equals("Unknown")) {
                  ItemStack[] ores = OreDictionary.getOres(oreName).toArray(new ItemStack[0]);
                  if (ThaumcraftApiHelper.containsMatch(false, new ItemStack[]{stack1}, ores)) {
                     return true;
                  }
               }
            }
         }

         boolean t1 = true;
         if (!ignoreNBT) {
            t1 = ItemStack.areItemStackTagsEqual(stack0, stack1);
         }

         boolean t2 = stack0.getItemDamage() != stack1.getItemDamage();
         if (ignoreDamage && stack0.isItemStackDamageable() && stack1.isItemStackDamageable()) {
            t2 = false;
         }

         if (t2 && ignoreDamage && (stack0.getItemDamage() == 32767 || stack1.getItemDamage() == 32767)) {
            t2 = false;
         }

         return stack0.getItem() == stack1.getItem() && (!t2 && t1);
      }
   }

   public static boolean consumeInventoryItem(EntityPlayer player, Item item, int md) {
      for(int var2 = 0; var2 < player.inventory.mainInventory.size(); ++var2) {
         if (player.inventory.mainInventory.get(var2) != null && player.inventory.mainInventory.get(var2).getItem() == item && player.inventory.mainInventory.get(var2).getItemDamage() == md) {
            player.inventory.mainInventory.get(var2).setCount(player.inventory.mainInventory.get(var2).getCount() - 1);
            if (player.inventory.mainInventory.get(var2).getCount() <= 0) {
               player.inventory.mainInventory.set(var2, ItemStack.EMPTY);
            }

            return true;
         }
      }

      return false;
   }

   public static void dropItems(World world, int x, int y, int z) {
      Random rand = new Random();
      int md =
        world.getBlockState(new net.minecraft.util.math.BlockPos(x, y, z)).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(x, y, z)));
      TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
      if (tileEntity instanceof IInventory) {
         IInventory inventory = (IInventory)tileEntity;

         for(int i = 0; i < inventory.getSizeInventory(); ++i) {
            if ((!(tileEntity instanceof TileResearchTable) || md != 15 || i != 9) && (!(tileEntity instanceof TileArcaneWorkbench) || i != 9)) {
               ItemStack item = inventory.getStackInSlot(i);
               if (item != null && item.getCount() > 0) {
                  float rx = rand.nextFloat() * 0.8F + 0.1F;
                  float ry = rand.nextFloat() * 0.8F + 0.1F;
                  float rz = rand.nextFloat() * 0.8F + 0.1F;
                  EntityItem entityItem = new EntityItem(world, (float)x + rx, (float)y + ry, (float)z + rz, new ItemStack(item.getItem(), item.getCount(), item.getItemDamage()));
                  if (item.hasTagCompound()) {
                     entityItem.getItem().setTagCompound((NBTTagCompound)item.getTagCompound().copy());
                  }

                  float factor = 0.05F;
                  entityItem.motionX = rand.nextGaussian() * (double)factor;
                  entityItem.motionY = rand.nextGaussian() * (double)factor + (double)0.2F;
                  entityItem.motionZ = rand.nextGaussian() * (double)factor;
                  world.spawnEntity(entityItem);
                  inventory.setInventorySlotContents(i, ItemStack.EMPTY);
               }
            }
         }

      }
   }

   public static void dropItemsAtEntity(World world, int x, int y, int z, Entity entity) {
      new Random();
      int md =
        world.getBlockState(new net.minecraft.util.math.BlockPos(x, y, z)).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(x, y, z)));
      TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
      if (tileEntity instanceof IInventory) {
         IInventory inventory = (IInventory)tileEntity;

         for(int i = 0; i < inventory.getSizeInventory(); ++i) {
            if ((!(tileEntity instanceof TileResearchTable) || md != 15 || i != 9) && (!(tileEntity instanceof TileArcaneWorkbench) || i != 9)) {
               ItemStack item = inventory.getStackInSlot(i);
               if (item != null && item.getCount() > 0) {
                  EntityItem entityItem = new EntityItem(world, entity.posX, entity.posY + (double)(entity.getEyeHeight() / 2.0F), entity.posZ, item.copy());
                  world.spawnEntity(entityItem);
                  inventory.setInventorySlotContents(i, ItemStack.EMPTY);
               }
            }
         }

      }
   }

   public static int isWandInHotbarWithRoom(Aspect aspect, int amount, EntityPlayer player) {
      int var2 = 0;

      while(true) {
         InventoryPlayer var10001 = player.inventory;
         if (var2 >= InventoryPlayer.getHotbarSize()) {
            return -1;
         }

         if (player.inventory.mainInventory.get(var2) != null && player.inventory.mainInventory.get(var2).getItem() instanceof ItemWandCasting) {
            ItemWandCasting wand = (ItemWandCasting)player.inventory.mainInventory.get(var2).getItem();
            if (wand.addVis(player.inventory.mainInventory.get(var2), aspect, amount, false) < amount) {
               return var2;
            }
         }

         ++var2;
      }
   }

   public static int isPlayerCarrying(EntityPlayer player, ItemStack stack) {
      for(int var2 = 0; var2 < player.inventory.mainInventory.size(); ++var2) {
         if (player.inventory.mainInventory.get(var2) != null && player.inventory.mainInventory.get(var2).isItemEqual(stack)) {
            return var2;
         }
      }

      return -1;
   }

   public static ItemStack damageItem(int par1, ItemStack stack, World world) {
      if (stack.isItemStackDamageable()) {
         if (par1 > 0) {
            int var3 = EnchantmentHelper.getEnchantmentLevel(Enchantments.UNBREAKING, stack);
            int var4 = 0;

            for(int var5 = 0; var3 > 0 && var5 < par1; ++var5) {
               if (EnchantmentDurability.negateDamage(stack, var3, world.rand)) {
                  ++var4;
               }
            }

            par1 -= var4;
            if (par1 <= 0) {
               return stack;
            }
         }

         stack.setItemDamage(stack.getItemDamage() + par1);
         if (stack.getItemDamage() > stack.getMaxDamage()) {
            stack.setCount(stack.getCount() - 1);
            if (stack.getCount() < 0) {
               stack.setCount(0);
            }

            stack.setItemDamage(0);
         }
      }

      return stack;
   }

   public static void dropItemsWithChance(World world, int x, int y, int z, float chance, int fortune, ArrayList<ItemStack> items) {
      for(ItemStack item : items) {
         if (world.rand.nextFloat() <= chance && item.getCount() > 0 && !world.isRemote && world.getGameRules().getBoolean("doTileDrops")) {
            float var6 = 0.7F;
            double var7 = (double)(world.rand.nextFloat() * var6) + (double)(1.0F - var6) * (double)0.5F;
            double var9 = (double)(world.rand.nextFloat() * var6) + (double)(1.0F - var6) * (double)0.5F;
            double var11 = (double)(world.rand.nextFloat() * var6) + (double)(1.0F - var6) * (double)0.5F;
            EntityItem var13 = new EntityItem(world, (double)x + var7, (double)y + var9, (double)z + var11, item);
            var13.setPickupDelay(10);
            world.spawnEntity(var13);
         }
      }

   }

   public static TileEntityChest getDoubleChest(TileEntity tile) {
      if (tile instanceof TileEntityChest) {
         if (((TileEntityChest)tile).adjacentChestXNeg != null) {
            return ((TileEntityChest)tile).adjacentChestXNeg;
         }

         if (((TileEntityChest)tile).adjacentChestXPos != null) {
            return ((TileEntityChest)tile).adjacentChestXPos;
         }

         if (((TileEntityChest)tile).adjacentChestZNeg != null) {
            return ((TileEntityChest)tile).adjacentChestZNeg;
         }

         if (((TileEntityChest)tile).adjacentChestZPos != null) {
            return ((TileEntityChest)tile).adjacentChestZPos;
         }
      }

      return null;
   }

   public static ItemStack cycleItemStack(Object input) {
      if (input == null) return ItemStack.EMPTY;
      ItemStack it = null;
      if (input instanceof net.minecraft.item.crafting.Ingredient) {
         ItemStack[] stacks = ((net.minecraft.item.crafting.Ingredient) input).getMatchingStacks();
         if (stacks.length > 0) {
            int idx = (int)(System.currentTimeMillis() / 1000L % (long)stacks.length);
            it = stacks[idx];
         }
         return it != null ? it : ItemStack.EMPTY;
      } else if (input instanceof ItemStack) {
         it = (ItemStack)input;
         if (it.getItemDamage() == 32767 && it.getItem().getHasSubtypes()) {
            NonNullList<ItemStack> subItems = NonNullList.create();
            net.minecraft.creativetab.CreativeTabs tab = it.getItem().getCreativeTab();
            it.getItem().getSubItems(tab != null ? tab : net.minecraft.creativetab.CreativeTabs.SEARCH, subItems);
            if (!subItems.isEmpty()) {
               int md = (int)(System.currentTimeMillis() / 1000L % (long)subItems.size());
               ItemStack it2 = new ItemStack(it.getItem(), 1, md);
               it2.setTagCompound(it.getTagCompound());
               it = it2;
            }
         } else if (it.getItemDamage() == 32767 && it.isItemStackDamageable()) {
            int md = (int)(System.currentTimeMillis() / 10L % (long)it.getMaxDamage());
            ItemStack it2 = new ItemStack(it.getItem(), 1, md);
            it2.setTagCompound(it.getTagCompound());
            it = it2;
         }
      } else if (input instanceof ArrayList) {
         ArrayList<ItemStack> q = (ArrayList)input;
         if (q != null && !q.isEmpty()) {
            int idx = (int)(System.currentTimeMillis() / 1000L % (long)q.size());
            it = cycleItemStack(q.get(idx));
         }
      } else if (input instanceof String) {
         List<ItemStack> q = OreDictionary.getOres((String)input);
         if (q != null && !q.isEmpty()) {
            int idx = (int)(System.currentTimeMillis() / 1000L % (long)q.size());
            it = cycleItemStack(q.get(idx));
         }
      }

      return it != null ? it : ItemStack.EMPTY;
   }
}
