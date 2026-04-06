package thaumcraft.common.items.wands.foci;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.HashMap;
import net.minecraft.block.Block;
import thaumcraft.client.renderers.compat.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
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
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumcraft.common.items.wands.WandManager;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.fx.PacketFXBlockSparkle;
import thaumcraft.common.tiles.TileWarded;
import net.minecraft.util.math.BlockPos;

public class ItemFocusWarding extends ItemFocusBasic implements IArchitect {
   public TextureAtlasSprite iconOrnament;
   TextureAtlasSprite depthIcon = null;
   private static final AspectList cost;
   public static HashMap<String,Long> delay;
   ArrayList<BlockCoordinates> checked = new ArrayList<>();

   public ItemFocusWarding() {
      this.setCreativeTab(Thaumcraft.tabTC);
   }

   public String getSortingHelper(ItemStack itemstack) {
      return "BWA" + super.getSortingHelper(itemstack);
   }

   @SideOnly(Side.CLIENT)
   public void registerIcons(IIconRegister ir) {
      this.depthIcon = ir.registerSprite("thaumcraft:focus_warding_depth");
      this.icon = ir.registerSprite("thaumcraft:focus_warding");
      this.iconOrnament = ir.registerSprite("thaumcraft:focus_warding_orn");
   }

   public TextureAtlasSprite getFocusDepthLayerIcon(ItemStack itemstack) {
      return this.depthIcon;
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

   public int getFocusColor(ItemStack itemstack) {
      return 16771535;
   }

   public AspectList getVisCost(ItemStack itemstack) {
      return cost.copy();
   }

   public ItemStack onFocusRightClick(ItemStack itemstack, World world, EntityPlayer player, RayTraceResult mop) {
      ItemWandCasting wand = (ItemWandCasting)itemstack.getItem();
      player.swingArm(net.minecraft.util.EnumHand.MAIN_HAND);
      if (!world.isRemote && mop != null && mop.typeOfHit == RayTraceResult.Type.BLOCK) {
         String key = mop.getBlockPos().getX() + ":" + mop.getBlockPos().getY() + ":" + mop.getBlockPos().getZ() + ":" + world.provider.getDimension();
         if (delay.containsKey(key) && delay.get(key) > System.currentTimeMillis()) {
            return itemstack;
         }

         delay.put(key, System.currentTimeMillis() + 500L);
         TileEntity tt = world.getTileEntity(mop.getBlockPos());
         boolean solid = world.getBlockState(mop.getBlockPos()).isNormalCube();
         if (tt == null && solid) {
            for(BlockCoordinates c : this.getArchitectBlocks(itemstack, world, mop.getBlockPos().getX(), mop.getBlockPos().getY(), mop.getBlockPos().getZ(), mop.sideHit, player)) {
               if (!wand.consumeAllVis(itemstack, player, this.getVisCost(itemstack), true, false)) {
                  break;
               }

               if (world.getTileEntity(new BlockPos(c.x, c.y, c.z)) == null && world.getBlockState(new BlockPos(c.x, c.y, c.z)).isNormalCube()) {
                  Block bi = world.getBlockState(new BlockPos(c.x, c.y, c.z)).getBlock();
                  int md = world.getBlockState(new BlockPos(c.x, c.y, c.z)).getBlock().getMetaFromState(world.getBlockState(new BlockPos(c.x, c.y, c.z)));
                  int ll = bi.getLightValue(world.getBlockState(new BlockPos(c.x, c.y, c.z)));
                  world.setBlockState(new BlockPos(c.x, c.y, c.z), (ConfigBlocks.blockWarded).getStateFromMeta(md), 3);
                  TileEntity tile = world.getTileEntity(new BlockPos(c.x, c.y, c.z));
                  if (tile instanceof TileWarded) {
                     TileWarded tw = (TileWarded)tile;
                     tw.block = bi;
                     tw.blockMd = (byte)md;
                     tw.light = (byte)ll;
                     tw.owner = player.getName().hashCode();
                     { BlockPos _cp = new BlockPos(c.x, c.y, c.z); net.minecraft.block.state.IBlockState _bs = world.getBlockState(_cp); world.notifyBlockUpdate(_cp, _bs, _bs, 3); }
                     PacketHandler.INSTANCE.sendToAllAround(new PacketFXBlockSparkle(c.x, c.y, c.z, 16556032), new NetworkRegistry.TargetPoint(world.provider.getDimension(), c.x, c.y, c.z, 32.0F));
                  }
               }
            }

            { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:zap")); if (_snd != null) world.playSound(null, mop.getBlockPos().getX() + 0.5, mop.getBlockPos().getY() + 0.5, mop.getBlockPos().getZ() + 0.5, _snd, net.minecraft.util.SoundCategory.NEUTRAL, 0.25F, 1.0F); }
         } else if (tt instanceof TileWarded) {
            TileWarded tw = (TileWarded)tt;
            if (tw.owner == player.getName().hashCode()) {
               for(BlockCoordinates c : this.getArchitectBlocks(itemstack, world, mop.getBlockPos().getX(), mop.getBlockPos().getY(), mop.getBlockPos().getZ(), mop.sideHit, player)) {
                  TileEntity tile = world.getTileEntity(new BlockPos(c.x, c.y, c.z));
                  if (tile instanceof TileWarded) {
                     TileWarded tw2 = (TileWarded)tile;
                     if (tw2.owner == player.getName().hashCode()) {
                        world.setBlockState(new BlockPos(c.x, c.y, c.z), (tw2.block).getStateFromMeta(tw2.blockMd), 3);
                        { BlockPos _cp = new BlockPos(c.x, c.y, c.z); net.minecraft.block.state.IBlockState _bs = world.getBlockState(_cp); world.notifyBlockUpdate(_cp, _bs, _bs, 3); }
                        PacketHandler.INSTANCE.sendToAllAround(new PacketFXBlockSparkle(c.x, c.y, c.z, 16556032), new NetworkRegistry.TargetPoint(world.provider.getDimension(), c.x, c.y, c.z, 32.0F));
                     }
                  }
               }

               { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:zap")); if (_snd != null) world.playSound(null, mop.getBlockPos().getX() + 0.5, mop.getBlockPos().getY() + 0.5, mop.getBlockPos().getZ() + 0.5, _snd, net.minecraft.util.SoundCategory.NEUTRAL, 0.25F, 1.0F); }
            }
         }
      }

      return itemstack;
   }

   public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack itemstack, int rank) {
      switch (rank) {
         case 1:
            return new FocusUpgradeType[]{FocusUpgradeType.frugal};
         case 2:
            return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.architect};
         case 3:
            return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.enlarge};
         case 4:
            return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.enlarge};
         case 5:
            return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.enlarge};
         default:
            return null;
      }
   }

   public boolean canApplyUpgrade(ItemStack focusstack, EntityPlayer player, FocusUpgradeType type, int rank) {
      return !type.equals(FocusUpgradeType.enlarge) || this.isUpgradedWith(focusstack, FocusUpgradeType.architect);
   }

   public int getMaxAreaSize(ItemStack focusstack) {
      return 3 + this.getUpgradeLevel(focusstack, FocusUpgradeType.enlarge);
   }

   public ArrayList<BlockCoordinates> getArchitectBlocks(ItemStack stack, World world, int x, int y, int z, EnumFacing side, EntityPlayer player) {
      ArrayList<BlockCoordinates> out = new ArrayList<>();
      ItemWandCasting wand = (ItemWandCasting)stack.getItem();
      wand.getFocus(stack);
      this.checked.clear();
      boolean tiles = false;
      TileEntity tt = world.getTileEntity(new BlockPos(x, y, z));
      if (tt instanceof TileWarded) {
         tiles = true;
      }

      int sizeX = 0;
      int sizeY = 0;
      int sizeZ = 0;
      if (this.isUpgradedWith(wand.getFocusItem(stack), FocusUpgradeType.architect)) {
         sizeX = WandManager.getAreaX(stack);
         sizeY = WandManager.getAreaY(stack);
         sizeZ = WandManager.getAreaZ(stack);
      }

      int sideIdx = side.getIndex();
      if (sideIdx != 2 && sideIdx != 3) {
         this.checkNeighbours(world, x, y, z, new BlockCoordinates(x, y, z), sideIdx, sizeX, sizeY, sizeZ, out, player, tiles);
      } else {
         this.checkNeighbours(world, x, y, z, new BlockCoordinates(x, y, z), sideIdx, sizeZ, sizeY, sizeX, out, player, tiles);
      }

      return out;
   }

   public void checkNeighbours(World world, int x, int y, int z, BlockCoordinates pos, int side, int sizeX, int sizeY, int sizeZ, ArrayList<BlockCoordinates> list, EntityPlayer player, boolean tiles) {
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

               if (Math.abs(pos.y - y) > sizeY) {
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

               if (Math.abs(pos.z - z) > sizeY) {
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

               if (Math.abs(pos.x - x) > sizeY) {
                  return;
               }
         }

         TileEntity tt = world.getTileEntity(new BlockPos(pos.x, pos.y, pos.z));
         boolean solid = world.getBlockState(new BlockPos(pos.x, pos.y, pos.z)).isNormalCube();
         if (!tiles || tt instanceof TileWarded) {
            if (tiles || tt == null && solid) {
               if (tiles && tt != null && tt instanceof TileWarded) {
                  TileWarded tw2 = (TileWarded)tt;
                  if (tw2.owner != player.getName().hashCode()) {
                     return;
                  }
               }

               if (!world.isAirBlock(new BlockPos(pos.x, pos.y, pos.z))) {
                  list.add(pos);

                  for(EnumFacing dir : EnumFacing.VALUES) {
                     BlockCoordinates cc = new BlockCoordinates(pos.x + dir.getXOffset(), pos.y + dir.getYOffset(), pos.z + dir.getZOffset());
                     this.checkNeighbours(world, x, y, z, cc, side, sizeX, sizeY, sizeZ, list, player, tiles);
                  }

               }
            }
         }
      }
   }

   public boolean showAxis(ItemStack stack, World world, EntityPlayer player, EnumFacing side, IArchitect.EnumAxis axis) {
      int dim = WandManager.getAreaDim(stack);
      if (dim == 0) {
         return true;
      } else {
         switch (side.getIndex()) {
            case 0:
            case 1:
               if (axis == IArchitect.EnumAxis.X && dim == 1 || axis == IArchitect.EnumAxis.Z && dim == 2 || axis == IArchitect.EnumAxis.Y && dim == 3) {
                  return true;
               }
               break;
            case 2:
            case 3:
               if (axis == IArchitect.EnumAxis.Y && dim == 1 || axis == IArchitect.EnumAxis.X && dim == 2 || axis == IArchitect.EnumAxis.Z && dim == 3) {
                  return true;
               }
               break;
            case 4:
            case 5:
               if (axis == IArchitect.EnumAxis.Y && dim == 1 || axis == IArchitect.EnumAxis.Z && dim == 2 || axis == IArchitect.EnumAxis.X && dim == 3) {
                  return true;
               }
         }

         return false;
      }
   }

   static {
      cost = (new AspectList()).add(Aspect.EARTH, 25).add(Aspect.ORDER, 25).add(Aspect.WATER, 10);
      delay = new HashMap<>();
   }
}
