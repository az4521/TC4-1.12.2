package thaumcraft.common.entities.golems;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.BlockFluidBase;
// import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.common.config.Config;
import thaumcraft.common.lib.utils.InventoryUtils;
import thaumcraft.common.tiles.TileEssentiaReservoir;
import thaumcraft.common.tiles.TileJarFillable;
import thaumcraft.common.tiles.TileJarFillableVoid;

import javax.annotation.Nonnull;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

public class GolemHelper {
   public static final double ADJACENT_RANGE = 4.0F;
   static HashMap<String,TileJarFillable> jarlist = new HashMap<>();
   private static ArrayList<net.minecraftforge.fluids.Fluid> reggedLiquids = null;
   static ArrayList<SortingItemTimeout> itemTimeout = new ArrayList<>();

   public static ArrayList<IInventory> getMarkedContainers(World world, EntityGolemBase golem) {
      ArrayList<IInventory> results = new ArrayList<>();

      for(Marker marker : golem.getMarkers()) {
         TileEntity te = world.getTileEntity(new BlockPos(marker.x, marker.y, marker.z));
         if (marker.dim == world.provider.getDimension() && te instanceof IInventory) {
            results.add((IInventory)te);
            if (InventoryUtils.getDoubleChest(te) != null) {
               results.add(InventoryUtils.getDoubleChest(te));
            }
         }
      }

      return results;
   }

   public static ArrayList<IInventory> getMarkedContainersAdjacentToGolem(World world, EntityGolemBase golem) {
      ArrayList<IInventory> results = new ArrayList<>();

      for(IInventory inventory : getMarkedContainers(world, golem)) {
         TileEntity te = (TileEntity)inventory;
         if (golem.getDistanceSq((double)te.getPos().getX() + (double)0.5F, (double)te.getPos().getY() + (double)0.5F, (double)te.getPos().getZ() + (double)0.5F) < (double)4.0F) {
            results.add(inventory);
            if (InventoryUtils.getDoubleChest(te) != null) {
               results.add(InventoryUtils.getDoubleChest(te));
            }
         }
      }

      return results;
   }

   public static ArrayList<BlockPos> getMarkedBlocksAdjacentToGolem(World world, EntityGolemBase golem, byte color) {
      ArrayList<BlockPos> results = new ArrayList<>();

      for(Marker marker : golem.getMarkers()) {
         if ((marker.color == color || color == -1) && (golem.world.getTileEntity(new BlockPos(marker.x, marker.y, marker.z)) == null || !(golem.world.getTileEntity(new BlockPos(marker.x, marker.y, marker.z)) instanceof IInventory)) && golem.getDistanceSq((double)marker.x + (double)0.5F, (double)marker.y + (double)0.5F, (double)marker.z + (double)0.5F) < (double)4.0F) {
            results.add(new BlockPos(marker.x, marker.y, marker.z));
         }
      }

      return results;
   }

   public static ArrayList<IInventory> getContainersWithRoom(World world, EntityGolemBase golem, byte color) {
      ArrayList<IInventory> results = new ArrayList<>();

      for(IInventory inventory : getMarkedContainers(world, golem)) {
         boolean hasRoom = false;

         for(Integer side : getMarkedSides(golem, (TileEntity)inventory, color)) {
            ItemStack result = InventoryUtils.placeItemStackIntoInventory(golem.getCarried(), inventory, side, false);
            if (!ItemStack.areItemStacksEqual(result, golem.itemCarried)) {
               results.add(inventory);
               break;
            }

            if (InventoryUtils.getDoubleChest((TileEntity)inventory) != null) {
               result = InventoryUtils.placeItemStackIntoInventory(golem.getCarried(), InventoryUtils.getDoubleChest((TileEntity)inventory), side, false);
               if (!ItemStack.areItemStacksEqual(result, golem.itemCarried)) {
                  results.add(InventoryUtils.getDoubleChest((TileEntity)inventory));
               }
            }
         }
      }

      return results;
   }

   public static ArrayList<IInventory> getContainersWithRoom(World world, EntityGolemBase golem, byte color, ItemStack itemToMatch) {
      ArrayList<IInventory> results = new ArrayList<>();

      for(IInventory inventory : getMarkedContainers(world, golem)) {
         boolean hasRoom = false;

         for(Integer side : getMarkedSides(golem, (TileEntity)inventory, color)) {
            ItemStack result = InventoryUtils.placeItemStackIntoInventory(itemToMatch, inventory, side, false);
            if (!ItemStack.areItemStacksEqual(result, itemToMatch)) {
               results.add(inventory);
               break;
            }

            if (InventoryUtils.getDoubleChest((TileEntity)inventory) != null) {
               result = InventoryUtils.placeItemStackIntoInventory(itemToMatch, InventoryUtils.getDoubleChest((TileEntity)inventory), side, false);
               if (!ItemStack.areItemStacksEqual(result, itemToMatch)) {
                  results.add(InventoryUtils.getDoubleChest((TileEntity)inventory));
               }
            }
         }
      }

      return results;
   }

   public static List<Integer> getMarkedSides(EntityGolemBase golem, TileEntity tile, byte color) {
      return getMarkedSides(golem, tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ(), tile.getWorld().provider.getDimension(), color);
   }

   public static List<Integer> getMarkedSides(EntityGolemBase golem, int x, int y, int z, int dim, byte color) {
      List<Integer> out = new ArrayList<>();
      ArrayList<Marker> gm = golem.getMarkers();
      if (gm != null && !gm.isEmpty()) {
         for(int a = 0; a < 6; ++a) {
            Marker marker = new Marker(x, y, z, dim, (byte)a, color);
            if (contained(gm, marker)) {
               out.add(a);
            }
         }

      }
       return out;
   }

   public static boolean contained(ArrayList<Marker> l, Marker m) {
      for(Marker mark : l) {
         if (m.equalsFuzzy(mark)) {
            return true;
         }
      }

      return false;
   }

   public static ArrayList<IInventory> getContainersWithGoods(World world, EntityGolemBase golem, ItemStack goods, byte color) {
      ArrayList<IInventory> results = new ArrayList<>();

      for(IInventory inventory : getMarkedContainers(world, golem)) {
         try {
            for(Integer side : getMarkedSides(golem, (TileEntity)inventory, color)) {
               if (InventoryUtils.extractStack(inventory, goods, side, golem.checkOreDict(), golem.ignoreDamage(), golem.ignoreNBT(), false) != null) {
                  results.add(inventory);
                  break;
               }

               if (InventoryUtils.getDoubleChest((TileEntity)inventory) != null && InventoryUtils.extractStack(InventoryUtils.getDoubleChest((TileEntity)inventory), goods, side, golem.checkOreDict(), golem.ignoreDamage(), golem.ignoreNBT(), false) != null) {
                  results.add(InventoryUtils.getDoubleChest((TileEntity)inventory));
                  break;
               }
            }
         } catch (Exception ignored) {
         }
      }

      return results;
   }

   public static ArrayList getMissingItems(EntityGolemBase golem) {
      EnumFacing facing = EnumFacing.byIndex(golem.homeFacing);
      BlockPos home = golem.getHomePosition();
      int cX = home.getX() - facing.getXOffset();
      int cY = home.getY() - facing.getYOffset();
      int cZ = home.getZ() - facing.getZOffset();
      int slotCount = golem.inventory.slotCount;
      if (golem.getToggles()[0]) {
         ArrayList<ItemStack> qr = new ArrayList<>();

         for(int q = 0; q < slotCount; ++q) {
            ItemStack toCheck = golem.inventory.inventory[q];
            if (toCheck != null) {
               ItemStack ret = toCheck.copy();
               qr.add(ret);
            }
         }

         return qr;
      } else {
         TileEntity tile = golem.world.getTileEntity(new BlockPos(cX, cY, cZ));
         if (tile == null) {
            return null;
         } else {
            ArrayList<ItemStack> qr = new ArrayList<>();

            label105:
            for(int q = 0; q < slotCount; ++q) {
               ItemStack toCheck = golem.inventory.inventory[q];
               if (toCheck != null) {
                  int foundAmount = 0;
                  boolean repeat = true;
                  boolean didRepeat = false;

                  while(repeat) {
                     if (didRepeat) {
                        repeat = false;
                     }

                     if (tile instanceof ISidedInventory && facing.ordinal() > -1) {
                        ISidedInventory isidedinventory = (ISidedInventory)tile;
                        int[] aint = isidedinventory.getSlotsForFace(facing);

                         for (int i : aint) {
                             if (InventoryUtils.areItemStacksEqual(((ISidedInventory) tile).getStackInSlot(i), toCheck, golem.checkOreDict(), golem.ignoreDamage(), golem.ignoreNBT())) {
                                 foundAmount += ((ISidedInventory) tile).getStackInSlot(i).getCount();
                                 if (foundAmount >= golem.inventory.getAmountNeededSmart(((ISidedInventory) tile).getStackInSlot(i), golem.getUpgradeAmount(5) > 0)) {
                                     continue label105;
                                 }
                             }
                         }
                     } else {
                        if (!(tile instanceof IInventory)) {
                           break;
                        }

                        int k = ((IInventory)tile).getSizeInventory();

                        for(int l = 0; l < k; ++l) {
                           if (InventoryUtils.areItemStacksEqual(((IInventory)tile).getStackInSlot(l), toCheck, golem.checkOreDict(), golem.ignoreDamage(), golem.ignoreNBT())) {
                              foundAmount += ((IInventory)tile).getStackInSlot(l).getCount();
                              if (foundAmount >= golem.inventory.getAmountNeededSmart(((IInventory)tile).getStackInSlot(l), golem.getUpgradeAmount(5) > 0)) {
                                 continue label105;
                              }
                           }
                        }
                     }

                     if (!didRepeat && InventoryUtils.getDoubleChest(tile) != null) {
                        tile = InventoryUtils.getDoubleChest(tile);
                        didRepeat = true;
                     } else {
                        repeat = false;
                     }
                  }

                  ItemStack ret = toCheck.copy();
                  ret.setCount(ret.getCount() - foundAmount);
                  qr.add(ret);
               }
            }

            return qr;
         }
      }
   }

   public static BlockPos findJarWithRoom(EntityGolemBase golem) {
      BlockPos dest = null;
      World world = golem.world;
      float dmod = golem.getRange();
      dmod *= dmod;
      ArrayList<TileEntity> jars = new ArrayList<>();
      ArrayList<TileEntity> others = new ArrayList<>();

      for(Marker marker : golem.getMarkers()) {
         TileEntity te = world.getTileEntity(new BlockPos(marker.x, marker.y, marker.z));
         if (marker.dim == world.provider.getDimension() && te instanceof TileJarFillable) {
            if (te.getDistanceSq(golem.getHomePosition().getX(), golem.getHomePosition().getY(), golem.getHomePosition().getZ()) <= (double)dmod) {
               jars.add(te);
            }
         } else if (marker.dim == world.provider.getDimension() && te instanceof TileEssentiaReservoir) {
            TileEssentiaReservoir res = (TileEssentiaReservoir)te;
            if (res.getSuctionAmount(res.facing) > 0 && (res.getSuctionType(res.facing) == null || res.getSuctionType(res.facing) == golem.essentia) && te.getDistanceSq(golem.getHomePosition().getX(), golem.getHomePosition().getY(), golem.getHomePosition().getZ()) <= (double)dmod) {
               others.add(te);
            }
         } else if (marker.dim == world.provider.getDimension() && te instanceof IEssentiaTransport) {
            IEssentiaTransport trans = (IEssentiaTransport)te;
            if (golem.essentia != null && golem.essentiaAmount > 0 && trans.canInputFrom(EnumFacing.byIndex(marker.side)) && trans.getSuctionAmount(EnumFacing.byIndex(marker.side)) > 0 && (trans.getSuctionType(EnumFacing.byIndex(marker.side)) == null || trans.getSuctionType(EnumFacing.byIndex(marker.side)) == golem.essentia) && te.getDistanceSq(golem.getHomePosition().getX(), golem.getHomePosition().getY(), golem.getHomePosition().getZ()) <= (double)dmod) {
               others.add(te);
            }
         }
      }

      if (!jars.isEmpty()) {
         jarlist.clear();

         for(TileEntity jar : jars) {
            jarlist.put(jar.getPos().getX() + ":" + jar.getPos().getY() + ":" + jar.getPos().getZ(), (TileJarFillable)jar);
            getConnectedJars((TileJarFillable)jar);
         }
      } else if (others.isEmpty()) {
         return null;
      }

       jars = new ArrayList<>(others);

      for(TileJarFillable jar : jarlist.values()) {
         if (jar.aspect != null && jar.amount > 0 && jar.amount < jar.maxAmount && jar.aspectFilter != null && golem.essentiaAmount > 0 && jar.aspect.equals(golem.essentia) && jar.doesContainerAccept(golem.essentia)) {
            jars.add(jar);
         }
      }

      if (jars.isEmpty()) {
         for(TileJarFillable jar : jarlist.values()) {
            if ((jar.aspect == null || jar.amount == 0) && jar.aspectFilter != null && jar.doesContainerAccept(golem.essentia)) {
               jars.add(jar);
            }
         }
      }

      if (jars.isEmpty()) {
         for(TileJarFillable jar : jarlist.values()) {
            if (jar.aspect != null && jar.amount >= jar.maxAmount && jar instanceof TileJarFillableVoid && jar.aspectFilter != null && golem.essentiaAmount > 0 && jar.aspect.equals(golem.essentia) && jar.doesContainerAccept(golem.essentia)) {
               jars.add(jar);
            }
         }
      }

      if (jars.isEmpty()) {
         for(TileJarFillable jar : jarlist.values()) {
            if (jar.aspect != null && jar.amount > 0 && jar.amount < jar.maxAmount && jar.aspectFilter == null && golem.essentiaAmount > 0 && jar.aspect.equals(golem.essentia) && jar.doesContainerAccept(golem.essentia)) {
               jars.add(jar);
            }
         }
      }

      if (jars.isEmpty()) {
         for(TileJarFillable jar : jarlist.values()) {
            if ((jar.aspect == null || jar.amount == 0) && jar.aspectFilter == null && !(jar instanceof TileJarFillableVoid) && jar.doesContainerAccept(golem.essentia)) {
               jars.add(jar);
            }
         }
      }

      if (jars.isEmpty()) {
         for(TileJarFillable jar : jarlist.values()) {
            if (jar.aspect != null && jar instanceof TileJarFillableVoid && jar.aspectFilter == null && golem.essentiaAmount > 0 && jar.aspect.equals(golem.essentia) && jar.doesContainerAccept(golem.essentia)) {
               jars.add(jar);
            }
         }
      }

      if (jars.isEmpty()) {
         for(TileJarFillable jar : jarlist.values()) {
            if ((jar.aspect == null || jar.amount == 0) && jar.aspectFilter == null && jar instanceof TileJarFillableVoid && jar.doesContainerAccept(golem.essentia)) {
               jars.add(jar);
            }
         }
      }

      double dist = Double.MAX_VALUE;

      for(TileEntity jar : jars) {
         double d = jar.getDistanceSq(golem.getHomePosition().getX(), golem.getHomePosition().getY(), golem.getHomePosition().getZ());
         if (jar instanceof TileJarFillableVoid) {
            d += dmod;
         }

         if (d < dist) {
            dist = d;
            dest = jar.getPos();
         }
      }

      jarlist.clear();
      return dest;
   }

   private static void getConnectedJars(TileJarFillable jar) {
      World world = jar.getWorld();

      for(int dir = 0; dir < 6; ++dir) {
         EnumFacing fd = EnumFacing.byIndex(dir);
         int xx = jar.getPos().getX() + fd.getXOffset();
         int yy = jar.getPos().getY() + fd.getYOffset();
         int zz = jar.getPos().getZ() + fd.getZOffset();
         if (!jarlist.containsKey(xx + ":" + yy + ":" + zz)) {
            TileEntity te = world.getTileEntity(new BlockPos(xx, yy, zz));
            if (te instanceof TileJarFillable) {
               jarlist.put(te.getPos().getX() + ":" + te.getPos().getY() + ":" + te.getPos().getZ(), (TileJarFillable)te);
               getConnectedJars((TileJarFillable)te);
            }
         }
      }

   }

   public static ArrayList<net.minecraftforge.fluids.Fluid> getReggedLiquids() {
      if (reggedLiquids == null) {
         reggedLiquids = new ArrayList<>();
         reggedLiquids.addAll(FluidRegistry.getRegisteredFluids().values());
      }
      return reggedLiquids;
   }

   public static ArrayList<FluidStack> getMissingLiquids(EntityGolemBase golem) {
      ArrayList<FluidStack> out = new ArrayList<>();
      EnumFacing facing = EnumFacing.byIndex(golem.homeFacing);
      BlockPos home = golem.getHomePosition();
      int cX = home.getX() - facing.getXOffset();
      int cY = home.getY() - facing.getYOffset();
      int cZ = home.getZ() - facing.getZOffset();
      TileEntity tile = golem.world.getTileEntity(new BlockPos(cX, cY, cZ));
      if (tile instanceof IFluidHandler) {
         IFluidHandler fluidhandler = (IFluidHandler)tile;
         Iterator<net.minecraftforge.fluids.Fluid> i$ = getReggedLiquids().iterator();

         while(true) {
            net.minecraftforge.fluids.Fluid id;
            while(true) {
               if (!i$.hasNext()) {
                  return out;
               }

               id = i$.next();
               if ((golem.fluidCarried == null || golem.fluidCarried.amount <= 0 || golem.fluidCarried.getFluid() == id) && fluidhandler.fill(new FluidStack(id, 1), false) > 0) {
                  FluidStack fs = new FluidStack(id, Integer.MAX_VALUE);
                  if (!golem.inventory.hasSomething()) {
                     break;
                  }

                  FluidStack fis = null;
                  boolean found = false;

                  for(int a = 0; a < golem.inventory.slotCount; ++a) {
                     fis = net.minecraftforge.fluids.FluidUtil.getFluidContained(golem.inventory.getStackInSlot(a));
                     if (fis != null && fis.isFluidEqual(fs)) {
                        found = true;
                        break;
                     }
                  }

                  if (found) {
                     break;
                  }
               }
            }

            out.add(new FluidStack(id, Integer.MAX_VALUE));
         }
      } else {
         return out;
      }
   }

   public static Vec3d findPossibleLiquid(FluidStack ls, EntityGolemBase golem) {
      EnumFacing facing = EnumFacing.byIndex(golem.homeFacing);
      BlockPos home = golem.getHomePosition();
      int var10000 = home.getX() - facing.getXOffset();
      var10000 = home.getY() - facing.getYOffset();
      var10000 = home.getZ() - facing.getZOffset();
      float dmod = golem.getRange();
      BlockPos v = null;
      ArrayList<IFluidHandler> fluidhandlers = getMarkedFluidHandlers(ls, golem.world, golem);
      double dd = Double.MAX_VALUE;
      if (fluidhandlers != null) {
         for(IFluidHandler fluidhandler : fluidhandlers) {
            if (fluidhandler != null) {
               TileEntity tile = (TileEntity)fluidhandler;
               double d = golem.getDistanceSq((double)tile.getPos().getX() + (double)0.5F, (double)tile.getPos().getY() + (double)0.5F, (double)tile.getPos().getZ() + (double)0.5F);
               if (d <= (double)(dmod * dmod) && d < dd) {
                  dd = d;
                  v = tile.getPos();
               }
            }
         }
      }

      if (v == null) {
         ArrayList<BlockPos> inworld = getMarkedFluidBlocks(ls, golem.world, golem);
         dd = Double.MAX_VALUE;
         if (inworld != null) {
            for(BlockPos coord : inworld) {
               if (coord != null) {
                  double d = golem.getDistanceSq((double)coord.getX() + (double)0.5F, (double)coord.getY() + (double)0.5F, (double)coord.getZ() + (double)0.5F);
                  if (d <= (double)(dmod * dmod) && d < dd) {
                     dd = d;
                     v = new BlockPos(coord.getX(), coord.getY(), coord.getZ());
                  }
               }
            }
         }
      }

      return v != null ? new Vec3d(v.getX(), v.getY(), v.getZ()) : null;
   }

   public static ArrayList<Marker> getMarkedFluidHandlersAdjacentToGolem(FluidStack ls, World world, EntityGolemBase golem) {
      ArrayList<Marker> results = new ArrayList<>();

      for(Marker marker : golem.getMarkers()) {
         TileEntity te = world.getTileEntity(new BlockPos(marker.x, marker.y, marker.z));
         if (marker.dim == world.provider.getDimension() && te instanceof IFluidHandler && golem.getDistanceSq((double) te.getPos().getX() + (double) 0.5F, (double) te.getPos().getY() + (double) 0.5F, (double) te.getPos().getZ() + (double) 0.5F) < (double) 4.0F) {
            FluidStack fs = ((IFluidHandler)te).drain(new FluidStack(ls.getFluid(), 1), false);
            if (fs != null && fs.amount > 0) {
               results.add(marker);
            }
         }
      }

      return results;
   }

   public static ArrayList getMarkedFluidHandlers(FluidStack ls, World world, EntityGolemBase golem) {
      ArrayList<IFluidHandler> results = new ArrayList<>();

      for(Marker marker : golem.getMarkers()) {
         TileEntity te = world.getTileEntity(new BlockPos(marker.x, marker.y, marker.z));
         if (marker.dim == world.provider.getDimension() && te instanceof IFluidHandler) {
            FluidStack fs = ((IFluidHandler)te).drain(new FluidStack(ls.getFluid(), 1), false);
            if (fs != null && fs.amount > 0) {
               results.add((IFluidHandler)te);
            }
         }
      }

      return results;
   }

   public static ArrayList getMarkedFluidBlocks(FluidStack ls, World world, EntityGolemBase golem) {
      ArrayList<BlockPos> results = new ArrayList<>();

      for(Marker marker : golem.getMarkers()) {
         BlockPos mpos = new BlockPos(marker.x, marker.y, marker.z);
         Block bi = world.getBlockState(mpos).getBlock();
         if (marker.dim == world.provider.getDimension() && ls.getFluid().getBlock() == bi) {
            if (bi instanceof BlockFluidBase && ((BlockFluidBase)bi).canDrain(world, mpos)) {
               results.add(mpos);
            } else if (ls.getFluid() == FluidRegistry.WATER || ls.getFluid() == FluidRegistry.LAVA) {
               net.minecraft.block.state.IBlockState bstate = world.getBlockState(mpos);
               int wmd = bi.getMetaFromState(bstate);
               if ((FluidRegistry.lookupFluidForBlock(bi) == FluidRegistry.WATER && ls.getFluid() == FluidRegistry.WATER || FluidRegistry.lookupFluidForBlock(bi) == FluidRegistry.LAVA && ls.getFluid() == FluidRegistry.LAVA) && wmd == 0) {
                  results.add(mpos);
               }
            }
         }
      }

      return results;
   }

   public static ArrayList getItemsNeeded(EntityGolemBase golem, boolean fuzzy) {
      ArrayList<ItemStack> needed = null;
      switch (golem.getCore()) {
         case 1:
            needed = golem.inventory.getItemsNeeded(golem.getUpgradeAmount(5) > 0);
            if (needed.isEmpty()) {
               return null;
            }

            return filterEmptyCore(golem, needed);
         case 8:
            needed = golem.inventory.getItemsNeeded(golem.getUpgradeAmount(5) > 0);
            if (needed.isEmpty()) {
               return null;
            }

            return filterUseCore(golem, needed);
         case 10:
            needed = getItemsInHomeContainer(golem);
            return filterSortCore(golem, needed);
         default:
            return needed;
      }
   }

   private static ArrayList<ItemStack> filterEmptyCore(EntityGolemBase golem, ArrayList<ItemStack> in) {
      ArrayList<ItemStack> out = new ArrayList<>();

      for(ItemStack itemToMatch : in) {
         if (!isOnTimeOut(golem, itemToMatch) && findSomethingEmptyCore(golem, itemToMatch)) {
            out.add(itemToMatch);
         }
      }

      return out;
   }

   private static ArrayList<ItemStack> filterUseCore(EntityGolemBase golem, ArrayList<ItemStack> in) {
      ArrayList<ItemStack> out = new ArrayList<>();

      for(ItemStack itemToMatch : in) {
         if (!isOnTimeOut(golem, itemToMatch) && findSomethingUseCore(golem, itemToMatch)) {
            out.add(itemToMatch);
         }
      }

      return out;
   }

   private static ArrayList<ItemStack> filterSortCore(EntityGolemBase golem, ArrayList<ItemStack> in) {
      ArrayList<ItemStack> out = new ArrayList<>();

      for(ItemStack itemToMatch : in) {
         if (!isOnTimeOut(golem, itemToMatch) && findSomethingSortCore(golem, itemToMatch)) {
            out.add(itemToMatch);
         }
      }

      return out;
   }

   public static boolean findSomethingUseCore(EntityGolemBase golem, ItemStack itemToMatch) {
      for(byte col : golem.getColorsMatching(itemToMatch)) {
         for(Marker marker : golem.getMarkers()) {
            if ((marker.color == col || col == -1) && (!golem.getToggles()[0] || golem.world.isAirBlock(new BlockPos(marker.x, marker.y, marker.z))) && (golem.getToggles()[0] || !golem.world.isAirBlock(new BlockPos(marker.x, marker.y, marker.z)))) {
               EnumFacing opp = EnumFacing.byIndex(marker.side);
               if (golem.world.isAirBlock(new BlockPos(marker.x + opp.getXOffset(), marker.y + opp.getYOffset(), marker.z + opp.getZOffset()))) {
                  return true;
               }
            }
         }
      }

      itemTimeout.add(new SortingItemTimeout(golem.getEntityId(), itemToMatch.copy(), System.currentTimeMillis() + (long)Config.golemIgnoreDelay));
      return false;
   }

   public static boolean findSomethingEmptyCore(EntityGolemBase golem, ItemStack itemToMatch) {
      ArrayList<Byte> matchingColors = golem.getColorsMatching(itemToMatch);

      for(byte color : matchingColors) {
         ArrayList<IInventory> markers = getContainersWithRoom(golem.world, golem, color, itemToMatch);
         if (!markers.isEmpty()) {
            EnumFacing i$1 = EnumFacing.byIndex(golem.homeFacing);
            BlockPos marker = golem.getHomePosition();
            int cX = marker.getX() - i$1.getXOffset();
            int cY = marker.getY() - i$1.getYOffset();
            int cZ = marker.getZ() - i$1.getZOffset();
            double range = Double.MAX_VALUE;
            float dmod = golem.getRange();

            for(IInventory te : markers) {
               double distance = golem.getDistanceSq((double)((TileEntity)te).getPos().getX() + (double)0.5F, (double)((TileEntity)te).getPos().getY() + (double)0.5F, (double)((TileEntity)te).getPos().getZ() + (double)0.5F);
               if (distance < range && distance <= (double)(dmod * dmod) && (((TileEntity)te).getPos().getX() != cX || ((TileEntity)te).getPos().getY() != cY || ((TileEntity)te).getPos().getZ() != cZ)) {
                  return true;
               }
            }
         }
      }

      for(byte color : matchingColors) {
         for(Marker marker1 : golem.getMarkers()) {
            if ((marker1.color == color || color == -1) && (golem.world.getTileEntity(new BlockPos(marker1.x, marker1.y, marker1.z)) == null || !(golem.world.getTileEntity(new BlockPos(marker1.x, marker1.y, marker1.z)) instanceof IInventory))) {
               return true;
            }
         }
      }

      itemTimeout.add(new SortingItemTimeout(golem.getEntityId(), itemToMatch.copy(), System.currentTimeMillis() + (long)Config.golemIgnoreDelay));
      return false;
   }

   public static boolean findSomethingSortCore(EntityGolemBase golem, ItemStack itemToMatch) {
      ArrayList<IInventory> markers = getContainersWithRoom(golem.world, golem, (byte)-1, itemToMatch);
      if (!markers.isEmpty()) {
         EnumFacing i$1 = EnumFacing.byIndex(golem.homeFacing);
         BlockPos marker = golem.getHomePosition();
         int cX = marker.getX() - i$1.getXOffset();
         int cY = marker.getY() - i$1.getYOffset();
         int cZ = marker.getZ() - i$1.getZOffset();
         double range = Double.MAX_VALUE;
         float dmod = golem.getRange();

         for(IInventory te : markers) {
            double distance = golem.getDistanceSq((double)((TileEntity)te).getPos().getX() + (double)0.5F, (double)((TileEntity)te).getPos().getY() + (double)0.5F, (double)((TileEntity)te).getPos().getZ() + (double)0.5F);
            if (distance < range && distance <= (double)(dmod * dmod) && (((TileEntity)te).getPos().getX() != cX || ((TileEntity)te).getPos().getY() != cY || ((TileEntity)te).getPos().getZ() != cZ)) {
               for(int side : getMarkedSides(golem, (TileEntity)te, (byte)-1)) {
                  if (InventoryUtils.inventoryContains(te, itemToMatch, side, golem.checkOreDict(), golem.ignoreDamage(), golem.ignoreNBT())) {
                     return true;
                  }
               }
            }
         }
      }

      itemTimeout.add(new SortingItemTimeout(golem.getEntityId(), itemToMatch.copy(), System.currentTimeMillis() + (long)Config.golemIgnoreDelay));
      return false;
   }

   public static boolean isOnTimeOut(EntityGolemBase golem, ItemStack stack) {
      SortingItemTimeout tos = new SortingItemTimeout(golem.getEntityId(), stack, 0L);
      if (itemTimeout.contains(tos)) {
         int q = itemTimeout.indexOf(tos);
         SortingItemTimeout tos2 = itemTimeout.get(q);
         if (System.currentTimeMillis() < tos2.time) {
            return true;
         }

         itemTimeout.remove(q);
      }

      return false;
   }

   public static boolean validTargetForItem(EntityGolemBase golem, ItemStack stack) {
      if (isOnTimeOut(golem, stack)) {
         return false;
      } else {
         EnumFacing facing = EnumFacing.byIndex(golem.homeFacing);
         BlockPos home = golem.getHomePosition();
         int cX = home.getX() - facing.getXOffset();
         int cY = home.getY() - facing.getYOffset();
         int cZ = home.getZ() - facing.getZOffset();
         switch (golem.getCore()) {
            case 1:
               return findSomethingEmptyCore(golem, stack);
            case 8:
               return findSomethingUseCore(golem, stack);
            case 10:
               return findSomethingSortCore(golem, stack);
            default:
               golem.world.getTileEntity(new BlockPos(cX, cY, cZ));
               ArrayList<ItemStack> neededList = getItemsNeeded(golem, golem.getUpgradeAmount(5) > 0);
               if (neededList != null && !neededList.isEmpty()) {
                  for(ItemStack ss : neededList) {
                     if (InventoryUtils.areItemStacksEqual(ss, golem.itemCarried, golem.checkOreDict(), golem.ignoreDamage(), golem.ignoreNBT())) {
                        return true;
                     }
                  }
               }

               itemTimeout.add(new SortingItemTimeout(golem.getEntityId(), stack.copy(), System.currentTimeMillis() + (long)Config.golemIgnoreDelay));
               return false;
         }
      }
   }

   public static ItemStack getFirstItemUsingTimeout(EntityGolemBase golem, IInventory inventory, int side, boolean doit) {
      ItemStack stack1 = null;
      if (inventory instanceof ISidedInventory && side > -1) {
         ISidedInventory isidedinventory = (ISidedInventory)inventory;
         int[] aint = isidedinventory.getSlotsForFace(EnumFacing.byIndex(side));

          for (int i : aint) {
              if (stack1 == null && !inventory.getStackInSlot(i).isEmpty()) {
                  if (isOnTimeOut(golem, inventory.getStackInSlot(i))) {
                      continue;
                  }

                  stack1 = inventory.getStackInSlot(i).copy();
                  stack1.setCount(golem.getCarrySpace());
              }

              if (stack1 != null) {
                  stack1 = InventoryUtils.attemptExtraction(inventory, stack1, i, side, false, false, false, doit);
              }

              if (stack1 != null) {
                  break;
              }
          }
      } else {
         int k = inventory.getSizeInventory();

         for(int l = 0; l < k; ++l) {
            if (stack1 == null && !inventory.getStackInSlot(l).isEmpty()) {
               if (isOnTimeOut(golem, inventory.getStackInSlot(l))) {
                  continue;
               }

               stack1 = inventory.getStackInSlot(l).copy();
               stack1.setCount(golem.getCarrySpace());
            }

            if (stack1 != null) {
               stack1 = InventoryUtils.attemptExtraction(inventory, stack1, l, side, false, false, false, doit);
            }

            if (stack1 != null) {
               break;
            }
         }
      }

      if (stack1 != null && !stack1.isEmpty()) {
         return stack1.copy();
      } else {
         if (doit) {
            inventory.markDirty();
         }

         return null;
      }
   }

   public static ArrayList getItemsInHomeContainer(EntityGolemBase golem) {
      EnumFacing facing = EnumFacing.byIndex(golem.homeFacing);
      BlockPos home = golem.getHomePosition();
      int cX = home.getX() - facing.getXOffset();
      int cY = home.getY() - facing.getYOffset();
      int cZ = home.getZ() - facing.getZOffset();
      TileEntity tile = golem.world.getTileEntity(new BlockPos(cX, cY, cZ));
      if (tile instanceof IInventory) {
         int[] aint = null;
         ArrayList<ItemStack> out = new ArrayList<>();
         IInventory inv = (IInventory)tile;
         if (tile instanceof ISidedInventory && facing.ordinal() > -1) {
            aint = ((ISidedInventory)inv).getSlotsForFace(facing);
         } else {
            aint = new int[inv.getSizeInventory()];

            for(int a = 0; a < inv.getSizeInventory(); aint[a] = a++) {
            }
         }

         if (aint != null) {
             for (int i : aint) {
                 if (!inv.getStackInSlot(i).isEmpty()) {
                     out.add(inv.getStackInSlot(i).copy());
                 }
             }
         }

         return out;
      } else {
         return null;
      }
   }

   public static class SortingItemTimeout implements Comparable<SortingItemTimeout> {
      ItemStack stack = null;
      int golemId = 0;
      long time = 0L;

      public SortingItemTimeout(int golemId, ItemStack stack, long time) {
         this.stack = stack;
         this.golemId = golemId;
         this.time = time;
      }

      public int compareTo(@Nonnull SortingItemTimeout arg0) {
         return this.equals(arg0) ? 0 : -1;
      }

      public boolean equals(Object obj) {
         if (obj instanceof SortingItemTimeout) {
            SortingItemTimeout t = (SortingItemTimeout)obj;
            if (this.golemId != t.golemId) {
               return false;
            }

            if (!this.stack.isItemEqual(t.stack)) {
               return false;
            }

             return ItemStack.areItemStackTagsEqual(this.stack, t.stack);
         }

         return true;
      }
   }
}
