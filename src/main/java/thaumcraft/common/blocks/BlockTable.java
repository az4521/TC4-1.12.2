package thaumcraft.common.blocks;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import thaumcraft.api.wands.IWandable;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumcraft.common.lib.utils.InventoryUtils;
import thaumcraft.common.tiles.TileArcaneWorkbench;
import thaumcraft.common.tiles.TileDeconstructionTable;
import thaumcraft.common.tiles.TileResearchTable;
import thaumcraft.common.tiles.TileTable;

import java.util.List;

public class BlockTable extends BlockContainer implements IWandable {

   public static final net.minecraft.block.properties.PropertyInteger META =
         net.minecraft.block.properties.PropertyInteger.create("meta", 0, 15);

   public BlockTable() {
      super(Material.WOOD);
      this.setDefaultState(this.blockState.getBaseState().withProperty(META, 0));
      this.setHardness(2.5F);
      this.setCreativeTab(Thaumcraft.tabTC);
   }

   @Override
   protected net.minecraft.block.state.BlockStateContainer createBlockState() {
      return new net.minecraft.block.state.BlockStateContainer(this, META);
   }

   @Override
   public IBlockState getStateFromMeta(int meta) {
      return this.getDefaultState().withProperty(META, meta);
   }

   @Override
   public int getMetaFromState(IBlockState state) {
      return state.getValue(META);
   }

   @Override
   public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
      return side == EnumFacing.UP || super.isSideSolid(state, world, pos, side);
   }

   @Override
   @SideOnly(Side.CLIENT)
   public void getSubBlocks(CreativeTabs par2CreativeTabs, NonNullList<ItemStack> par3List) {
      par3List.add(new ItemStack(this, 1, 0));
      par3List.add(new ItemStack(this, 1, 14));
      par3List.add(new ItemStack(this, 1, 15));
   }

   @Override
   public TileEntity createTileEntity(World world, IBlockState state) {
      int metadata = state.getBlock().getMetaFromState(state);
      if ((metadata <= 1 || metadata >= 6) && metadata < 14) {
         return new TileTable();
      } else if (metadata == 14) {
         return new TileDeconstructionTable();
      } else {
         return metadata == 15 ? new TileArcaneWorkbench() : new TileResearchTable();
      }
   }

   @Override
   public void onBlockPlacedBy(World par1World, BlockPos pos, IBlockState state, EntityLivingBase par5EntityLiving, ItemStack is) {
      int md = state.getBlock().getMetaFromState(state);
      if (md < 14) {
         int var7 = MathHelper.floor((double)(par5EntityLiving.rotationYaw * 4.0F / 360.0F) + (double)0.5F) & 3;
         int out = var7 == 0 ? 0 : (var7 == 1 ? 1 : (var7 == 2 ? 0 : (var7 == 3 ? 1 : 0)));
         par1World.setBlockState(pos, this.getStateFromMeta(out), 3);
      }
   }

   @Override
   public boolean isOpaqueCube(IBlockState state) {
      return false;
   }

   @Override
   public boolean isFullCube(IBlockState state) {
      return false;
   }

   @Override
   public void breakBlock(World par1World, BlockPos pos, IBlockState state) {
      InventoryUtils.dropItems(par1World, pos.getX(), pos.getY(), pos.getZ());
      super.breakBlock(par1World, pos, state);
   }

   @Override
   public int damageDropped(IBlockState state) {
      int par1 = state.getBlock().getMetaFromState(state);
      if (par1 == 14) {
         return 14;
      } else {
         return par1 == 15 ? 15 : 0;
      }
   }

   @Override
   public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
      TileEntity tile = world.getTileEntity(pos);
      int md = state.getBlock().getMetaFromState(state);
      if (tile instanceof TileResearchTable) {
         BlockPos neighborPos = pos.add(EnumFacing.byIndex(md).getXOffset(), EnumFacing.byIndex(md).getYOffset(), EnumFacing.byIndex(md).getZOffset());
         IBlockState neighborState = world.getBlockState(neighborPos);
         int mm = neighborState.getBlock().getMetaFromState(neighborState);
         if (mm < 6) {
            InventoryUtils.dropItems(world, pos.getX(), pos.getY(), pos.getZ());
            world.setTileEntity(pos, new TileTable());
            world.setBlockState(pos, this.getStateFromMeta(0), 3);
         }
      } else if (md >= 6 && md < 14) {
         BlockPos neighborPos = pos.add(EnumFacing.byIndex(md - 4).getXOffset(), EnumFacing.byIndex(md - 4).getYOffset(), EnumFacing.byIndex(md - 4).getZOffset());
         TileEntity tile2 = world.getTileEntity(neighborPos);
         if (!(tile2 instanceof TileResearchTable)) {
            world.setBlockState(pos, this.getStateFromMeta(0), 3);
         }
      }

      super.neighborChanged(state, world, pos, blockIn, fromPos);
   }

   @Override
   public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
      TileEntity tileEntity = world.getTileEntity(pos);
      int md = state.getBlock().getMetaFromState(state);
      if (md > 1 && tileEntity != null && !player.isSneaking()) {
         if (world.isRemote) {
            return true;
         } else if (tileEntity instanceof TileArcaneWorkbench) {
            player.openGui(Thaumcraft.instance, 13, world, pos.getX(), pos.getY(), pos.getZ());
            return true;
         } else if (tileEntity instanceof TileDeconstructionTable) {
            player.openGui(Thaumcraft.instance, 8, world, pos.getX(), pos.getY(), pos.getZ());
            return true;
         } else {
            if (tileEntity instanceof TileResearchTable) {
               player.openGui(Thaumcraft.instance, 10, world, pos.getX(), pos.getY(), pos.getZ());
            } else {
               for (int a = 2; a < 6; ++a) {
                  BlockPos adjPos = pos.add(EnumFacing.byIndex(a).getXOffset(), EnumFacing.byIndex(a).getYOffset(), EnumFacing.byIndex(a).getZOffset());
                  TileEntity adjTile = world.getTileEntity(adjPos);
                  if (adjTile instanceof TileResearchTable) {
                     player.openGui(Thaumcraft.instance, 10, world, adjPos.getX(), adjPos.getY(), adjPos.getZ());
                     break;
                  }
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }

   @Override
   public TileEntity createNewTileEntity(World var1, int md) {
      return null;
   }

   @Override
   public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World world, BlockPos pos) {
      int md = state.getBlock().getMetaFromState(state);
      switch (md) {
         case 2:
         case 6:
            return new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ() - 1.0, pos.getX() + 1.0, pos.getY() + 1.0, pos.getZ() + 1.0);
         case 3:
         case 7:
            return new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.0, pos.getY() + 1.0, pos.getZ() + 2.0);
         case 4:
         case 8:
            return new AxisAlignedBB(pos.getX() - 1.0, pos.getY(), pos.getZ(), pos.getX() + 1.0, pos.getY() + 1.0, pos.getZ() + 1.0);
         case 5:
         case 9:
            return new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 2.0, pos.getY() + 1.0, pos.getZ() + 1.0);
         default:
            return super.getSelectedBoundingBox(state, world, pos);
      }
   }

   public int onWandRightClick(World world, ItemStack wandstack, EntityPlayer player, int x, int y, int z, int side, int md) {
      if (md <= 1) {
         ItemWandCasting wand = (ItemWandCasting)wandstack.getItem();
         BlockPos pos = new BlockPos(x, y, z);
         world.setBlockState(pos, ConfigBlocks.blockTable.getStateFromMeta(15), 3);
         world.setTileEntity(pos, new TileArcaneWorkbench());
         TileArcaneWorkbench tawb = (TileArcaneWorkbench)world.getTileEntity(pos);
         if (tawb != null && !wand.isStaff(wandstack)) {
            tawb.setInventorySlotContents(10, wandstack.copy());
            player.inventory.setInventorySlotContents(player.inventory.currentItem, ItemStack.EMPTY);
         }

         if (tawb != null) {
            tawb.markDirty();
         }
         { net.minecraft.block.state.IBlockState _bs = world.getBlockState(pos); world.notifyBlockUpdate(pos, _bs, _bs, 3); }
         world.playSound(null, pos,
               new SoundEvent(new ResourceLocation("minecraft", "ui.button.click")),
               SoundCategory.BLOCKS, 0.15F, 0.5F);
         return 0;
      } else {
         return -1;
      }
   }

   public ItemStack onWandRightClick(World world, ItemStack wandstack, EntityPlayer player) {
      return null;
   }

   public void onUsingWandTick(ItemStack wandstack, EntityPlayer player, int count) {
   }

   public void onWandStoppedUsing(ItemStack wandstack, World world, EntityPlayer player, int count) {
   }

   @Override
   public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) {
      return net.minecraft.util.EnumBlockRenderType.INVISIBLE;
   }
}
