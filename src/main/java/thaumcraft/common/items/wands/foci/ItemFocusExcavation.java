package thaumcraft.common.items.wands.foci;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import thaumcraft.client.renderers.compat.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumcraft.common.lib.utils.BlockUtils;

public class ItemFocusExcavation extends ItemFocusBasic {
   private static final AspectList cost;
   private static AspectList cost2;
   static HashMap soundDelay;
   static HashMap beam;
   static HashMap breakcount;
   static HashMap lastX;
   static HashMap lastY;
   static HashMap lastZ;
   public static FocusUpgradeType dowsing;

   public ItemFocusExcavation() {
      this.setCreativeTab(Thaumcraft.tabTC);
   }

   @SideOnly(Side.CLIENT)
   public void registerIcons(IIconRegister ir) {
      this.icon = ir.registerSprite("thaumcraft:focus_excavation");
   }

   public String getSortingHelper(ItemStack itemstack) {
      return "BE" + super.getSortingHelper(itemstack);
   }

   public int getFocusColor(ItemStack itemstack) {
      return 409606;
   }

   public AspectList getVisCost(ItemStack itemstack) {
      if (this.isUpgradedWith(itemstack, FocusUpgradeType.silktouch)) {
         if (cost2 == null) {
            cost2 = (new AspectList()).add(Aspect.AIR, 1).add(Aspect.FIRE, 1).add(Aspect.EARTH, 1).add(Aspect.WATER, 1).add(Aspect.ORDER, 1).add(Aspect.ENTROPY, 1);
            cost2.add(cost);
         }

         return cost2;
      } else if (this.isUpgradedWith(itemstack, dowsing)) {
         if (cost2 == null) {
            cost2 = (new AspectList()).add(Aspect.FIRE, 2).add(Aspect.ORDER, 2);
            cost2.add(cost);
         }

         return cost2;
      } else {
         return cost;
      }
   }

   public boolean isVisCostPerTick(ItemStack itemstack) {
      return true;
   }

   public ItemStack onFocusRightClick(ItemStack itemstack, World world, EntityPlayer p, RayTraceResult mop) {
      p.setActiveHand(p.getActiveHand());
      return itemstack;
   }

   public void onUsingFocusTick(ItemStack stack, EntityPlayer p, int count) {
      ItemWandCasting wand = (ItemWandCasting)stack.getItem();
      if (!wand.consumeAllVis(stack, p, this.getVisCost(stack), false, false)) {
         p.stopActiveHand();
      } else {
         String pp = "R" + p.getName();
         if (!p.world.isRemote) {
            pp = "S" + p.getName();
         }

          soundDelay.putIfAbsent(pp, 0L);

          breakcount.putIfAbsent(pp, 0.0F);

          lastX.putIfAbsent(pp, 0);

          lastY.putIfAbsent(pp, 0);

          lastZ.putIfAbsent(pp, 0);

         RayTraceResult mop = BlockUtils.getTargetBlock(p.world, p, false);
         Vec3d v = p.getLookVec();
         double tx = p.posX + v.x * (double)10.0F;
         double ty = p.posY + v.y * (double)10.0F;
         double tz = p.posZ + v.z * (double)10.0F;
         int impact = 0;
         if (mop != null) {
            tx = mop.hitVec.x;
            ty = mop.hitVec.y;
            tz = mop.hitVec.z;
            impact = 5;
            if (!p.world.isRemote && (Long)soundDelay.get(pp) < System.currentTimeMillis()) {
               { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:rumble")); if (_snd != null) p.world.playSound(null, tx, ty, tz, _snd, net.minecraft.util.SoundCategory.NEUTRAL, 0.3F, 1.0F); }
               soundDelay.put(pp, System.currentTimeMillis() + 1200L);
            }
         } else {
            soundDelay.put(pp, 0L);
         }

         if (p.world.isRemote) {
            beam.put(pp, Thaumcraft.proxy.beamCont(p.world, p, tx, ty, tz, 2, 65382, false, impact > 0 ? 2.0F : 0.0F, beam.get(pp), impact));
         }

         if (mop != null && mop.typeOfHit == RayTraceResult.Type.BLOCK && p.world.isBlockModifiable(p, mop.getBlockPos())) {
            BlockPos mopPos = mop.getBlockPos();
            World world = p.world;
            Block bi = world.getBlockState(mopPos).getBlock();
            IBlockState biState = world.getBlockState(mopPos);
            int md = biState.getBlock().getMetaFromState(biState);
            float hardness = bi.getBlockHardness(biState, world, mopPos);
            if (hardness >= 0.0F) {
               int pot = wand.getFocusPotency(stack);
               float speed = 0.05F + (float)pot * 0.1F;
               Material mat = biState.getMaterial();
               if (mat == Material.ROCK || mat == Material.GRASS || mat == Material.GROUND || mat == Material.SAND) {
                  speed = 0.25F + (float)pot * 0.25F;
               }

               if (bi == Blocks.OBSIDIAN) {
                  speed *= 3.0F;
               }

               if ((Integer)lastX.get(pp) == mopPos.getX() && (Integer)lastY.get(pp) == mopPos.getY() && (Integer)lastZ.get(pp) == mopPos.getZ()) {
                  float bc = (Float)breakcount.get(pp);
                  if (p.world.isRemote && bc > 0.0F && bi != Blocks.AIR) {
                     int progress = (int)(bc / hardness * 9.0F);
                     Thaumcraft.proxy.excavateFX(mopPos.getX(), mopPos.getY(), mopPos.getZ(), p, Block.getIdFromBlock(bi), md, progress);
                  }

                  if (p.world.isRemote) {
                     if (bc >= hardness) {
                        breakcount.put(pp, 0.0F);
                     } else {
                        breakcount.put(pp, bc + speed);
                     }
                  } else if (bc >= hardness && wand.consumeAllVis(stack, p, this.getVisCost(stack), true, false)) {
                     if (this.excavate(p.world, stack, p, bi, md, mopPos.getX(), mopPos.getY(), mopPos.getZ())) {
                        for(int a = 0; a < wand.getFocusEnlarge(stack); ++a) {
                           if (wand.consumeAllVis(stack, p, this.getVisCost(stack), false, false) && this.breakNeighbour(p, mopPos.getX(), mopPos.getY(), mopPos.getZ(), bi, md, stack)) {
                              wand.consumeAllVis(stack, p, this.getVisCost(stack), true, false);
                           }
                        }
                     }

                     lastX.put(pp, Integer.MAX_VALUE);
                     lastY.put(pp, Integer.MAX_VALUE);
                     lastZ.put(pp, Integer.MAX_VALUE);
                     breakcount.put(pp, 0.0F);
                  } else {
                     breakcount.put(pp, bc + speed);
                  }
               } else {
                  lastX.put(pp, mopPos.getX());
                  lastY.put(pp, mopPos.getY());
                  lastZ.put(pp, mopPos.getZ());
                  breakcount.put(pp, 0.0F);
               }
            }
         } else {
            lastX.put(pp, Integer.MAX_VALUE);
            lastY.put(pp, Integer.MAX_VALUE);
            lastZ.put(pp, Integer.MAX_VALUE);
            breakcount.put(pp, 0.0F);
         }

      }
   }

   private boolean excavate(World world, ItemStack stack, EntityPlayer player, Block block, int md, int x, int y, int z) {
      GameType gt = GameType.SURVIVAL;
      if (player.capabilities.allowEdit) {
         if (player.capabilities.isCreativeMode) {
            gt = GameType.CREATIVE;
         }
      } else {
         gt = GameType.ADVENTURE;
      }

      BlockPos pos = new BlockPos(x, y, z);
      int breakResult = ForgeHooks.onBlockBreakEvent(world, gt, (EntityPlayerMP)player, pos);
      if (breakResult == -1) {
         return false;
      } else {
         ItemWandCasting wand = (ItemWandCasting)stack.getItem();
         int fortune = wand.getFocusTreasure(stack);
         boolean silk = this.isUpgradedWith(wand.getFocusItem(stack), FocusUpgradeType.silktouch);
         IBlockState state = world.getBlockState(pos);
         if (silk && block.canSilkHarvest(world, pos, state, player)) {
            ArrayList<ItemStack> items = new ArrayList<>();
            ItemStack itemstack = BlockUtils.createStackedBlock(block, md);
            if (itemstack != null) {
               items.add(itemstack);
            }

            ForgeEventFactory.fireBlockHarvesting(items, world, pos, state, 0, 1.0F, true, player);

            for(ItemStack is : items) {
               BlockUtils.dropBlockAsItem(world, x, y, z, is, block);
            }
         } else {
            BlockUtils.dropBlockAsItemWithChance(world, block, x, y, z, md, 1.0F, fortune, player);
            block.dropXpOnBlockBreak(world, pos, block.getExpDrop(state, world, pos, fortune));
         }

         world.setBlockToAir(pos);
         world.playEvent(2001, pos, Block.getIdFromBlock(block) + (md << 12));
         return true;
      }
   }

   boolean breakNeighbour(EntityPlayer p, int x, int y, int z, Block block, int md, ItemStack stack) {
      List<EnumFacing> directions = Arrays.asList(EnumFacing.DOWN, EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.EAST, EnumFacing.WEST);
      Collections.shuffle(directions, p.world.rand);

      for(EnumFacing dir : directions) {
         BlockPos nPos = new BlockPos(x + dir.getXOffset(), y + dir.getYOffset(), z + dir.getZOffset());
         IBlockState nState = p.world.getBlockState(nPos);
         if (nState.getBlock() == block && nState.getBlock().getMetaFromState(nState) == md && this.excavate(p.world, stack, p, block, md, nPos.getX(), nPos.getY(), nPos.getZ())) {
            return true;
         }
      }

      return false;
   }

   public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase living, int count) {
      EntityPlayer p = (EntityPlayer) living;
      String pp = "R" + p.getName();
      if (!p.world.isRemote) {
         pp = "S" + p.getName();
      }

       soundDelay.putIfAbsent(pp, 0L);

       breakcount.putIfAbsent(pp, 0.0F);

       lastX.putIfAbsent(pp, 0);

       lastY.putIfAbsent(pp, 0);

       lastZ.putIfAbsent(pp, 0);

      beam.put(pp, null);
      lastX.put(pp, Integer.MAX_VALUE);
      lastY.put(pp, Integer.MAX_VALUE);
      lastZ.put(pp, Integer.MAX_VALUE);
      breakcount.put(pp, 0.0F);
   }

   public ItemFocusBasic.WandFocusAnimation getAnimation(ItemStack itemstack) {
      return ItemFocusBasic.WandFocusAnimation.CHARGE;
   }

   public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack itemstack, int rank) {
      switch (rank) {
         case 1:
            return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency, FocusUpgradeType.treasure};
         case 2:
            return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency, FocusUpgradeType.enlarge};
         case 3:
            return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency, FocusUpgradeType.treasure, dowsing};
         case 4:
            return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency, FocusUpgradeType.enlarge};
         case 5:
            return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency, FocusUpgradeType.treasure, FocusUpgradeType.silktouch};
         default:
            return null;
      }
   }

   static {
      cost = (new AspectList()).add(Aspect.EARTH, 15);
      cost2 = null;
      soundDelay = new HashMap<>();
      beam = new HashMap<>();
      breakcount = new HashMap<>();
      lastX = new HashMap<>();
      lastY = new HashMap<>();
      lastZ = new HashMap<>();
      dowsing = new FocusUpgradeType(20, new ResourceLocation("thaumcraft", "textures/foci/dowsing.png"), "focus.upgrade.dowsing.name", "focus.upgrade.dowsing.text", (new AspectList()).add(Aspect.MINE, 1));
   }
}
