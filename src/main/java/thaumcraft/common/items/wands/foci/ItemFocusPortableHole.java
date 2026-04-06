package thaumcraft.common.items.wands.foci;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import thaumcraft.client.renderers.compat.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.Config;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumcraft.common.tiles.TileHole;
import net.minecraft.util.math.BlockPos;

public class ItemFocusPortableHole extends ItemFocusBasic {
   TextureAtlasSprite depthIcon = null;
   private static final AspectList cost;

   public ItemFocusPortableHole() {
      this.setCreativeTab(Thaumcraft.tabTC);
   }

   public String getSortingHelper(ItemStack itemstack) {
      return "BPH" + super.getSortingHelper(itemstack);
   }

   @SideOnly(Side.CLIENT)
   public void registerIcons(IIconRegister ir) {
      this.depthIcon = ir.registerSprite("thaumcraft:focus_portablehole_depth");
      this.icon = ir.registerSprite("thaumcraft:focus_portablehole");
   }

   public TextureAtlasSprite getFocusDepthLayerIcon(ItemStack itemstack) {
      return this.depthIcon;
   }

   public int getFocusColor(ItemStack itemstack) {
      return 594985;
   }

   public AspectList getVisCost(ItemStack itemstack) {
      return cost.copy();
   }

   public static boolean createHole(World world, int ii, int jj, int kk, int side, byte count, int max) {
      Block bi = world.getBlockState(new BlockPos(ii, jj, kk)).getBlock();
      if (world.getTileEntity(new BlockPos(ii, jj, kk)) == null && !ThaumcraftApi.portableHoleBlackList.contains(bi) && bi != Blocks.BEDROCK && bi != ConfigBlocks.blockHole && !world.isAirBlock(new BlockPos(ii, jj, kk)) && !bi.canPlaceBlockAt(world, new BlockPos(ii, jj, kk)) && bi.getBlockHardness(world.getBlockState(new BlockPos(ii, jj, kk)), world, new BlockPos(ii, jj, kk)) != -1.0F) {
         TileHole ts = new TileHole(bi,
        world.getBlockState(new net.minecraft.util.math.BlockPos(ii, jj, kk)).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(ii, jj, kk))), (short)max, count, (byte)side, null);
        world.setBlockState(new net.minecraft.util.math.BlockPos(ii, jj, kk), (Blocks.AIR).getStateFromMeta(0), 0);
         if (
        world.setBlockState(new net.minecraft.util.math.BlockPos(ii, jj, kk), (ConfigBlocks.blockHole).getStateFromMeta(0), 0)) {
            world.setTileEntity(new net.minecraft.util.math.BlockPos(ii, jj, kk), ts);
         }

         { BlockPos _hp = new BlockPos(ii, jj, kk); net.minecraft.block.state.IBlockState _bs = world.getBlockState(_hp); world.notifyBlockUpdate(_hp, _bs, _bs, 3); }
         Thaumcraft.proxy.blockSparkle(world, ii, jj, kk, 4194368, 1);
         return true;
      } else {
         return false;
      }
   }

   public ItemStack onFocusRightClick(ItemStack itemstack, World world, EntityPlayer player, RayTraceResult mop) {
      ItemWandCasting wand = (ItemWandCasting)itemstack.getItem();
      if (mop != null && mop.typeOfHit == RayTraceResult.Type.BLOCK) {
         if (world.provider.getDimension() == Config.dimensionOuterId) {
            if (!world.isRemote) {
               { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:wandfail")); if (_snd != null) world.playSound(null, mop.getBlockPos().getX() + 0.5, mop.getBlockPos().getY() + 0.5, mop.getBlockPos().getZ() + 0.5, _snd, net.minecraft.util.SoundCategory.NEUTRAL, 1.0F, 1.0F); }
            }

            player.swingArm(net.minecraft.util.EnumHand.MAIN_HAND);
            return itemstack;
         }

         int ii = mop.getBlockPos().getX();
         int jj = mop.getBlockPos().getY();
         int kk = mop.getBlockPos().getZ();
         int enlarge = wand.getFocusEnlarge(itemstack);
         int distance = 0;
         int maxdis = 33 + enlarge * 8;

         for(distance = 0; distance < maxdis; ++distance) {
            Block bi = world.getBlockState(new BlockPos(ii, jj, kk)).getBlock();
            if (ThaumcraftApi.portableHoleBlackList.contains(bi) || bi == Blocks.BEDROCK || bi == ConfigBlocks.blockHole || world.isAirBlock(new BlockPos(ii, jj, kk)) || bi.getBlockHardness(world.getBlockState(new BlockPos(ii, jj, kk)), world, new BlockPos(ii, jj, kk)) == -1.0F) {
               break;
            }

            switch (mop.sideHit.getIndex()) {
               case 0:
                  ++jj;
                  break;
               case 1:
                  --jj;
                  break;
               case 2:
                  ++kk;
                  break;
               case 3:
                  --kk;
                  break;
               case 4:
                  ++ii;
                  break;
               case 5:
                  --ii;
            }
         }

         AspectList c = this.getVisCost(itemstack);

         for(Aspect a : c.getAspects()) {
            c.merge(a, c.getAmount(a) * distance);
         }

         if (wand.consumeAllVis(itemstack, player, c, true, false)) {
            int di = this.getUpgradeLevel(wand.getFocusItem(itemstack), FocusUpgradeType.extend);
            short dur = (short)(120 + 60 * di);
            createHole(world, mop.getBlockPos().getX(), mop.getBlockPos().getY(), mop.getBlockPos().getZ(), mop.sideHit.getIndex(), (byte)(distance + 1), dur);
         }

         player.swingArm(net.minecraft.util.EnumHand.MAIN_HAND);
         if (!world.isRemote) {
            { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("mob.endermen.portal")); if (_snd != null) world.playSound(null, mop.getBlockPos().getX() + 0.5, mop.getBlockPos().getY() + 0.5, mop.getBlockPos().getZ() + 0.5, _snd, net.minecraft.util.SoundCategory.NEUTRAL, 1.0F, 1.0F); }
         }
      }

      return itemstack;
   }

   public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack itemstack, int rank) {
      switch (rank) {
         case 1:
            return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.enlarge, FocusUpgradeType.extend};
         case 2:
            return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.enlarge, FocusUpgradeType.extend};
         case 3:
            return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.enlarge, FocusUpgradeType.extend};
         case 4:
            return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.enlarge, FocusUpgradeType.extend};
         case 5:
            return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.enlarge, FocusUpgradeType.extend};
         default:
            return null;
      }
   }

   static {
      cost = (new AspectList()).add(Aspect.ENTROPY, 10).add(Aspect.AIR, 10);
   }
}
