package thaumcraft.common.entities;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

public class ItemSpawnerEgg extends Item {
   static ArrayList spawnList = new ArrayList<>();

   public static void addMapping(String name, int c1, int c2) {
      spawnList.add(new EntityEggStuff("Thaumcraft." + name, c1, c2));
   }

   public ItemSpawnerEgg() {
      this.setHasSubtypes(true);
      this.setCreativeTab(CreativeTabs.MISC);
   }

   public String getItemStackDisplayName(ItemStack par1ItemStack) {
      String s = (I18n.translateToLocal("item.monsterPlacer.name")).trim();
      int damage = par1ItemStack.getItemDamage();
      if (damage < 0 || damage >= spawnList.size()) return s;
      String s1 = ((EntityEggStuff)spawnList.get(damage)).name;
      if (s1 != null) {
         s = s + " " + I18n.translateToLocal("entity." + s1 + ".name");
      }

      return s;
   }

   @SideOnly(Side.CLIENT)
   public int getColorFromItemStack(ItemStack stack, int layer) {
      int damage = stack.getItemDamage();
      if (damage < 0 || damage >= spawnList.size()) return 16777215;
      EntityEggStuff entityegginfo = (EntityEggStuff)spawnList.get(damage);
      return entityegginfo != null ? (layer == 0 ? entityegginfo.color1 : entityegginfo.color2) : 16777215;
   }

   public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
      ItemStack stack = player.getHeldItem(hand);
      if (!world.isRemote) {
         Block block = world.getBlockState(pos).getBlock();
         BlockPos spawnPos = pos.offset(facing);
         double d0 = 0.0;
         if (facing == EnumFacing.UP && (block instanceof net.minecraft.block.BlockFence || block instanceof net.minecraft.block.BlockFenceGate || block instanceof net.minecraft.block.BlockWall)) {
            d0 = 0.5;
         }

         Entity entity = spawnCreature(world, stack.getItemDamage(), spawnPos.getX() + 0.5, spawnPos.getY() + d0, spawnPos.getZ() + 0.5);
         if (entity != null) {
            if (entity instanceof EntityLivingBase && stack.hasDisplayName()) {
               ((EntityLiving)entity).setCustomNameTag(stack.getDisplayName());
            }

            if (!player.capabilities.isCreativeMode) {
               stack.shrink(1);
            }
         }
      }
      return EnumActionResult.SUCCESS;
   }

   public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
      ItemStack stack = player.getHeldItem(hand);
      if (!world.isRemote) {
         RayTraceResult mop = this.rayTrace(world, player, true);
         if (mop != null && mop.typeOfHit == RayTraceResult.Type.BLOCK) {
            BlockPos pos = mop.getBlockPos();
            if (world.getBlockState(pos).getBlock() instanceof BlockLiquid) {
               Entity entity = spawnCreature(world, stack.getItemDamage(), pos.getX(), pos.getY(), pos.getZ());
               if (entity != null) {
                  if (entity instanceof EntityLivingBase && stack.hasDisplayName()) {
                     ((EntityLiving)entity).setCustomNameTag(stack.getDisplayName());
                  }

                  if (!player.capabilities.isCreativeMode) {
                     stack.shrink(1);
                  }
               }
            }
         }
      }
      return new ActionResult<>(EnumActionResult.PASS, stack);
   }

   public static Entity spawnCreature(World par0World, int par1, double par2, double par4, double par6) {
      if (par1 < 0 || par1 >= spawnList.size() || spawnList.get(par1) == null) {
         return null;
      } else {
         Entity entity = null;

         for (int j = 0; j < 1; ++j) {
            entity = EntityList.createEntityByIDFromName(new ResourceLocation(((EntityEggStuff)spawnList.get(par1)).name), par0World);
            if (entity instanceof EntityLivingBase) {
               EntityLiving entityliving = (EntityLiving)entity;
               entity.setLocationAndAngles(par2, par4, par6, MathHelper.wrapDegrees(par0World.rand.nextFloat() * 360.0F), 0.0F);
               entityliving.rotationYawHead = entityliving.rotationYaw;
               entityliving.renderYawOffset = entityliving.rotationYaw;
               entityliving.onInitialSpawn(par0World.getDifficultyForLocation(new BlockPos(entity)), null);
               par0World.spawnEntity(entity);
               entityliving.playLivingSound();
            }
         }

         return entity;
      }
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void getSubItems(CreativeTabs tab, net.minecraft.util.NonNullList<ItemStack> subItems) {
      for (int a = 0; a < spawnList.size(); ++a) {
         subItems.add(new ItemStack(this, 1, a));
      }
   }

   static class EntityEggStuff {
      String name;
      int color1;
      int color2;

      public EntityEggStuff(String name, int color1, int color2) {
         this.name = name;
         this.color1 = color1;
         this.color2 = color2;
      }
   }
}
