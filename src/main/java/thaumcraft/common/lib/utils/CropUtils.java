package thaumcraft.common.lib.utils;

import java.util.ArrayList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockStem;
import net.minecraft.block.IGrowable;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;

public class CropUtils {
   public static ArrayList<String> clickableCrops = new ArrayList<>();
   public static ArrayList<String> standardCrops = new ArrayList<>();
   public static ArrayList<String> stackedCrops = new ArrayList<>();
   public static ArrayList<String> lampBlacklist = new ArrayList<>();

   public static void addStandardCrop(ItemStack stack, int grownMeta) {
      if (Block.getBlockFromItem(stack.getItem()) != null) {
         if (grownMeta == 32767) {
            for(int a = 0; a < 16; ++a) {
               standardCrops.add(Block.getBlockFromItem(stack.getItem()).getTranslationKey() + a);
            }
         } else {
            standardCrops.add(Block.getBlockFromItem(stack.getItem()).getTranslationKey() + grownMeta);
         }

         if (Block.getBlockFromItem(stack.getItem()) instanceof BlockCrops && grownMeta != 7) {
            standardCrops.add(Block.getBlockFromItem(stack.getItem()).getTranslationKey() + "7");
         }

      }
   }

   public static void addClickableCrop(ItemStack stack, int grownMeta) {
      if (Block.getBlockFromItem(stack.getItem()) != null) {
         if (grownMeta == 32767) {
            for(int a = 0; a < 16; ++a) {
               clickableCrops.add(Block.getBlockFromItem(stack.getItem()).getTranslationKey() + a);
            }
         } else {
            clickableCrops.add(Block.getBlockFromItem(stack.getItem()).getTranslationKey() + grownMeta);
         }

         if (Block.getBlockFromItem(stack.getItem()) instanceof BlockCrops && grownMeta != 7) {
            clickableCrops.add(Block.getBlockFromItem(stack.getItem()).getTranslationKey() + "7");
         }

      }
   }

   public static void addStackedCrop(ItemStack stack, int grownMeta) {
      if (Block.getBlockFromItem(stack.getItem()) != null) {
         addStackedCrop(Block.getBlockFromItem(stack.getItem()), grownMeta);
      }
   }

   public static void addStackedCrop(Block block, int grownMeta) {
      if (grownMeta == 32767) {
         for(int a = 0; a < 16; ++a) {
            stackedCrops.add(block.getTranslationKey() + a);
         }
      } else {
         stackedCrops.add(block.getTranslationKey() + grownMeta);
      }

      if (block instanceof BlockCrops && grownMeta != 7) {
         stackedCrops.add(block.getTranslationKey() + "7");
      }

   }

   public static boolean isGrownCrop(World world, int x, int y, int z) {
      if (world.isAirBlock(new BlockPos(x, y, z))) {
         return false;
      } else {
         boolean found = false;
         Block bi = world.getBlockState(new BlockPos(x, y, z)).getBlock();

         for(int a = 0; a < 16; ++a) {
            if (standardCrops.contains(bi.getTranslationKey() + a) || clickableCrops.contains(bi.getTranslationKey() + a) || stackedCrops.contains(bi.getTranslationKey() + a)) {
               found = true;
               break;
            }
         }

         world.getBlockState(new net.minecraft.util.math.BlockPos(x, y + 1, z)).getBlock();
         Block biB = world.getBlockState(new net.minecraft.util.math.BlockPos(x, y - 1, z)).getBlock();
         int md =
        world.getBlockState(new net.minecraft.util.math.BlockPos(x, y, z)).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(x, y, z)));
         return bi instanceof IGrowable && !((IGrowable)bi).canGrow(world, new BlockPos(x, y, z), world.getBlockState(new BlockPos(x, y, z)), world.isRemote) && !(bi instanceof BlockStem) || bi instanceof BlockCrops && md == 7 && !found || bi == Blocks.NETHER_WART && md >= 3 || bi == Blocks.COCOA && (md & 12) >> 2 >= 2 || standardCrops.contains(bi.getTranslationKey() + md) || clickableCrops.contains(bi.getTranslationKey() + md) || stackedCrops.contains(bi.getTranslationKey() + md) && biB == bi;
      }
   }

   public static void blacklistLamp(ItemStack stack, int meta) {
      if (Block.getBlockFromItem(stack.getItem()) != null) {
         if (meta == 32767) {
            for(int a = 0; a < 16; ++a) {
               lampBlacklist.add(Block.getBlockFromItem(stack.getItem()).getTranslationKey() + a);
            }
         } else {
            lampBlacklist.add(Block.getBlockFromItem(stack.getItem()).getTranslationKey() + meta);
         }

      }
   }

   public static boolean doesLampGrow(World world, int x, int y, int z) {
      Block bi = world.getBlockState(new BlockPos(x, y, z)).getBlock();
      int md =
        world.getBlockState(new net.minecraft.util.math.BlockPos(x, y, z)).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(x, y, z)));
      return !lampBlacklist.contains(bi.getTranslationKey() + md);
   }
}
