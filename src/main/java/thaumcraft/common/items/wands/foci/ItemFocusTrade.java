package thaumcraft.common.items.wands.foci;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.ArrayList;
import net.minecraft.block.Block;
import thaumcraft.client.renderers.compat.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import thaumcraft.api.BlockCoordinates;
import thaumcraft.api.IArchitect;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.Config;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumcraft.common.items.wands.WandManager;
import thaumcraft.common.lib.events.ServerTickEventsFML;
import thaumcraft.common.lib.utils.BlockUtils;
import net.minecraft.util.math.BlockPos;

public class ItemFocusTrade extends ItemFocusBasic implements IArchitect {
   public TextureAtlasSprite iconOrnament;
   private static final AspectList cost;
   private static AspectList cost2;
   ArrayList<BlockCoordinates> checked = new ArrayList<>();

   public ItemFocusTrade() {
      this.setCreativeTab(Thaumcraft.tabTC);
   }

   public String getSortingHelper(ItemStack itemstack) {
      return "BT" + super.getSortingHelper(itemstack);
   }

   @SideOnly(Side.CLIENT)
   public void registerIcons(IIconRegister ir) {
      this.icon = ir.registerSprite("thaumcraft:focus_trade");
      this.iconOrnament = ir.registerSprite("thaumcraft:focus_trade_orn");
   }

   @SideOnly(Side.CLIENT)
   public TextureAtlasSprite getIconFromDamageForRenderPass(int par1, int renderPass) {
      return renderPass == 1 ? this.icon : this.iconOrnament;
   }

   @SideOnly(Side.CLIENT)
   public boolean requiresMultipleRenderPasses() {
      return true;
   }

   public TextureAtlasSprite getOrnament(ItemStack itemstack) {
      return this.iconOrnament;
   }

   public ItemStack onFocusRightClick(ItemStack itemstack, World world, EntityPlayer player, RayTraceResult movingobjectposition) {
      RayTraceResult mop = rayTrace(world, player, false);
      ItemWandCasting wand = (ItemWandCasting)itemstack.getItem();
      if (mop != null && mop.typeOfHit == RayTraceResult.Type.BLOCK) {
         int x = mop.getBlockPos().getX();
         int y = mop.getBlockPos().getY();
         int z = mop.getBlockPos().getZ();
         Block bi = world.getBlockState(new BlockPos(x, y, z)).getBlock();
         int md = world.getBlockState(new BlockPos(x, y, z)).getBlock().getMetaFromState(world.getBlockState(new BlockPos(x, y, z)));
         if (player.isSneaking()) {
            if (!world.isRemote && world.getTileEntity(new BlockPos(x, y, z)) == null) {
               ItemStack isout = new ItemStack(bi, 1, md);

               try {
                  if (bi != Blocks.AIR) {
                     ItemStack is = BlockUtils.createStackedBlock(bi, md);
                     if (is != null) {
                        isout = is.copy();
                     }
                  }
               } catch (Exception ignored) {
               }

               this.storePickedBlock(itemstack, isout);
            } else {
               player.swingArm(net.minecraft.util.EnumHand.MAIN_HAND);
            }
         } else {
            ItemStack pb = this.getPickedBlock(itemstack);
            if (pb != null && world.isRemote) {
               player.swingArm(net.minecraft.util.EnumHand.MAIN_HAND);
            } else if (pb != null && world.getTileEntity(new BlockPos(x, y, z)) == null && world.getBlockState(new BlockPos(x, y, z)).getMaterial() != Config.taintMaterial) {
               if (this.isUpgradedWith(wand.getFocusItem(itemstack), FocusUpgradeType.architect)) {
                  for(BlockCoordinates c : this.getArchitectBlocks(itemstack, world, x, y, z, mop.sideHit, player)) {
                     ServerTickEventsFML.addSwapper(world, c.x, c.y, c.z, world.getBlockState(new BlockPos(c.x, c.y, c.z)).getBlock(),
                        world.getBlockState(new BlockPos(c.x, c.y, c.z)).getBlock().getMetaFromState(world.getBlockState(new BlockPos(c.x, c.y, c.z))), pb, 0, player, player.inventory.currentItem);
                  }
               } else {
                  ServerTickEventsFML.addSwapper(world, x, y, z, world.getBlockState(new BlockPos(x, y, z)).getBlock(),
                     world.getBlockState(new BlockPos(x, y, z)).getBlock().getMetaFromState(world.getBlockState(new BlockPos(x, y, z))), pb, 3 + wand.getFocusEnlarge(itemstack), player, player.inventory.currentItem);
               }
            }
         }
      }

      return itemstack;
   }

   public float getStrVsBlock(ItemStack itemstack, Block block) {
      return 0.0F;
   }

   public boolean onEntitySwing(EntityLivingBase player, ItemStack stack) {
      if (!player.world.isRemote && player instanceof EntityPlayer) {
         ItemStack pb = this.getPickedBlock(stack);
         RayTraceResult mop = rayTrace(player.world, (EntityPlayer)player, false);
         if (mop != null && mop.typeOfHit == RayTraceResult.Type.BLOCK) {
            int x = mop.getBlockPos().getX();
            int y = mop.getBlockPos().getY();
            int z = mop.getBlockPos().getZ();
            if (pb != null && player.world.getTileEntity(new BlockPos(x, y, z)) == null && player.world.getBlockState(new BlockPos(x, y, z)).getMaterial() != Config.taintMaterial) {
               int md = player.world.getBlockState(new BlockPos(x, y, z)).getBlock().getMetaFromState(player.world.getBlockState(new BlockPos(x, y, z)));
               ServerTickEventsFML.addSwapper(player.world, x, y, z, player.world.getBlockState(new BlockPos(x, y, z)).getBlock(), md, pb, 0, (EntityPlayer)player, ((EntityPlayer)player).inventory.currentItem);
            }
         }
      }

      return super.onEntitySwing(player, stack);
   }

   public void storePickedBlock(ItemStack stack, ItemStack stackout) {
      NBTTagCompound item = new NBTTagCompound();
      stack.setTagInfo("picked", stackout.writeToNBT(item));
   }

   public ItemStack getPickedBlock(ItemStack stack) {
      ItemStack out = null;
      if (stack.hasTagCompound() && stack.getTagCompound().hasKey("picked")) {
         out = new ItemStack(Blocks.AIR);
         out.deserializeNBT(stack.getTagCompound().getCompoundTag("picked"));
      }

      return out;
   }

   public int getFocusColor(ItemStack itemstack) {
      return 8747923;
   }

   public AspectList getVisCost(ItemStack itemstack) {
      if (this.isUpgradedWith(itemstack, FocusUpgradeType.silktouch)) {
         if (cost2 == null) {
            cost2 = (new AspectList()).add(Aspect.AIR, 1).add(Aspect.FIRE, 1).add(Aspect.EARTH, 1).add(Aspect.WATER, 1).add(Aspect.ORDER, 1).add(Aspect.ENTROPY, 1);
            cost2.add(cost);
         }

         return cost2;
      } else {
         return cost;
      }
   }

   public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack itemstack, int rank) {
      switch (rank) {
         case 1:
            return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.enlarge};
         case 2:
            return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.enlarge};
         case 3:
            return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.enlarge, FocusUpgradeType.treasure, FocusUpgradeType.architect};
         case 4:
            return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.enlarge};
         case 5:
            return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.enlarge, FocusUpgradeType.silktouch};
         default:
            return null;
      }
   }

   public int getMaxAreaSize(ItemStack focusstack) {
      return 3 + this.getUpgradeLevel(focusstack, FocusUpgradeType.enlarge) * 2;
   }

   public ArrayList<BlockCoordinates> getArchitectBlocks(ItemStack stack, World world, int x, int y, int z, EnumFacing side, EntityPlayer player) {
      ItemWandCasting wand = (ItemWandCasting)stack.getItem();
      wand.getFocus(stack);
      Block bi = world.getBlockState(new BlockPos(x, y, z)).getBlock();
      int md = world.getBlockState(new BlockPos(x, y, z)).getBlock().getMetaFromState(world.getBlockState(new BlockPos(x, y, z)));
      ArrayList<BlockCoordinates> out = new ArrayList<>();
      this.checked.clear();
      int sideIdx = side.getIndex();
      if (sideIdx != 2 && sideIdx != 3) {
         this.checkNeighbours(world, x, y, z, bi, md, new BlockCoordinates(x, y, z), sideIdx, WandManager.getAreaX(stack), WandManager.getAreaY(stack), WandManager.getAreaZ(stack), out, player);
      } else {
         this.checkNeighbours(world, x, y, z, bi, md, new BlockCoordinates(x, y, z), sideIdx, WandManager.getAreaZ(stack), WandManager.getAreaY(stack), WandManager.getAreaX(stack), out, player);
      }

      return out;
   }

   public void checkNeighbours(World world, int x, int y, int z, Block bi, int md, BlockCoordinates pos, int side, int sizeX, int sizeY, int sizeZ, ArrayList<BlockCoordinates> list, EntityPlayer player) {
      if (!this.checked.contains(pos)) {
         this.checked.add(pos);
         switch (side) {
            case 0:
            case 1:
               if (Math.abs(pos.x - x) > sizeX) {
                  return;
               }

               if (Math.abs(pos.z - z) > sizeZ) {
                  return;
               }
               break;
            case 2:
            case 3:
               if (Math.abs(pos.x - x) > sizeX) {
                  return;
               }

               if (Math.abs(pos.y - y) > sizeZ) {
                  return;
               }
               break;
            case 4:
            case 5:
               if (Math.abs(pos.y - y) > sizeX) {
                  return;
               }

               if (Math.abs(pos.z - z) > sizeZ) {
                  return;
               }
         }

         BlockPos bpos = new BlockPos(pos.x, pos.y, pos.z);
         if (world.getBlockState(bpos).getBlock() == bi
               && world.getBlockState(bpos).getBlock().getMetaFromState(world.getBlockState(bpos)) == md
               && BlockUtils.isBlockExposed(world, pos.x, pos.y, pos.z)
               && !world.isAirBlock(bpos)
               && world.getBlockState(bpos).getBlock().getBlockHardness(world.getBlockState(bpos), world, bpos) >= 0.0F
               && world.isBlockModifiable(player, bpos)) {
            list.add(pos);

            for(EnumFacing dir : EnumFacing.VALUES) {
               if (dir.getIndex() != side && dir.getOpposite().getIndex() != side) {
                  BlockCoordinates cc = new BlockCoordinates(pos.x + dir.getXOffset(), pos.y + dir.getYOffset(), pos.z + dir.getZOffset());
                  this.checkNeighbours(world, x, y, z, bi, md, cc, side, sizeX, sizeY, sizeZ, list, player);
               }
            }

         }
      }
   }

   public boolean showAxis(ItemStack stack, World world, EntityPlayer player, EnumFacing side, IArchitect.EnumAxis axis) {
      int dim = WandManager.getAreaDim(stack);
      switch (side.getIndex()) {
         case 0:
         case 1:
            if (axis == IArchitect.EnumAxis.X && (dim == 0 || dim == 1) || axis == IArchitect.EnumAxis.Z && (dim == 0 || dim == 2)) {
               return true;
            }
            break;
         case 2:
         case 3:
            if (axis == IArchitect.EnumAxis.Y && (dim == 0 || dim == 1) || axis == IArchitect.EnumAxis.X && (dim == 0 || dim == 2)) {
               return true;
            }
            break;
         case 4:
         case 5:
            if (axis == IArchitect.EnumAxis.Y && (dim == 0 || dim == 1) || axis == IArchitect.EnumAxis.Z && (dim == 0 || dim == 2)) {
               return true;
            }
      }

      return false;
   }

   static {
      cost = (new AspectList()).add(Aspect.ENTROPY, 5).add(Aspect.EARTH, 5).add(Aspect.ORDER, 5);
      cost2 = null;
   }
}
