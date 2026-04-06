package thaumcraft.common.blocks;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.tiles.TileMirror;
import thaumcraft.common.tiles.TileMirrorEssentia;

import java.util.ArrayList;
import java.util.List;

public class BlockMirror extends BlockContainer {
   public static final net.minecraft.block.properties.PropertyInteger META =
         net.minecraft.block.properties.PropertyInteger.create("meta", 0, 15);

   @Override
   protected net.minecraft.block.state.BlockStateContainer createBlockState() {
      return new net.minecraft.block.state.BlockStateContainer(this, META);
   }

   @Override
   public net.minecraft.block.state.IBlockState getStateFromMeta(int meta) {
      return this.getDefaultState().withProperty(META, meta);
   }

   @Override
   public int getMetaFromState(net.minecraft.block.state.IBlockState state) {
      return state.getValue(META);
   }


   public BlockMirror() {
      super(Material.GLASS);
      this.setHardness(1.0F);
      this.setResistance(10.0F);
      this.setSoundType(new CustomStepSound("jar", 0.5F, 2.0F));
      this.setCreativeTab(Thaumcraft.tabTC);
   }

   @Override
   public int damageDropped(IBlockState state) {
      return state.getBlock().getMetaFromState(state);
   }

   @Override
   @SideOnly(Side.CLIENT)
   public void getSubBlocks(CreativeTabs par2CreativeTabs, NonNullList<ItemStack> par3List) {
      par3List.add(new ItemStack(this, 1, 0));
      par3List.add(new ItemStack(this, 1, 6));
   }

   @Override
   public TileEntity createTileEntity(World world, IBlockState state) {
      int metadata = state.getBlock().getMetaFromState(state);
      if (metadata <= 5) {
         return new TileMirror();
      }
      return metadata > 5 && metadata <= 11 ? new TileMirrorEssentia() : null;
   }

   @Override
   public TileEntity createNewTileEntity(World var1, int md) {
      return new TileMirror();
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
   public void onBlockHarvested(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
      int meta = state.getBlock().getMetaFromState(state);
      Block.spawnAsEntity(world, pos, new ItemStack(this, 1, meta));
      super.onBlockHarvested(world, pos, state, player);
   }

   @Override
   public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
      ArrayList<ItemStack> drops = new ArrayList<>();
      int md = state.getBlock().getMetaFromState(state);
      if (md < 6) {
         TileEntity rawTe = world.getTileEntity(pos);
         if (rawTe instanceof TileMirror) {
            TileMirror tm = (TileMirror) rawTe;
            ItemStack drop = new ItemStack(this, 1, 0);
            if (tm.linked) {
               drop.setTagInfo("linkX", new NBTTagInt(tm.linkX));
               drop.setTagInfo("linkY", new NBTTagInt(tm.linkY));
               drop.setTagInfo("linkZ", new NBTTagInt(tm.linkZ));
               drop.setTagInfo("linkDim", new NBTTagInt(tm.linkDim));
               drop.setTagInfo("dimname", new NBTTagString(((World) world).provider.getDimensionType().getName()));
               drop.setItemDamage(1);
               tm.invalidateLink();
            }
            drops.add(drop);
         }
      } else {
         TileEntity rawTe = world.getTileEntity(pos);
         if (rawTe instanceof TileMirrorEssentia) {
            TileMirrorEssentia tm = (TileMirrorEssentia) rawTe;
            ItemStack drop = new ItemStack(this, 1, 6);
            if (tm.linked) {
               drop.setTagInfo("linkX", new NBTTagInt(tm.linkX));
               drop.setTagInfo("linkY", new NBTTagInt(tm.linkY));
               drop.setTagInfo("linkZ", new NBTTagInt(tm.linkZ));
               drop.setTagInfo("linkDim", new NBTTagInt(tm.linkDim));
               drop.setTagInfo("dimname", new NBTTagString(((World) world).provider.getDimensionType().getName()));
               drop.setItemDamage(7);
               tm.invalidateLink();
            }
            drops.add(drop);
         }
      }
      return drops;
   }

   @Override
   public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
      int md = state.getBlock().getMetaFromState(state);
      if (md < 6 && !world.isRemote && entity instanceof EntityItem && !entity.isDead && entity.timeUntilPortal == 0) {
         TileMirror taf = (TileMirror) world.getTileEntity(pos);
         if (taf != null) {
            taf.transport((EntityItem) entity);
         }
      }
   }

   @Override
   public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
      if (!world.isRemote) {
         int i1 = state.getBlock().getMetaFromState(state);
         int i = pos.getX();
         int j = pos.getY();
         int k = pos.getZ();
         boolean flag = !world.isSideSolid(pos.add(-1, 0, 0), EnumFacing.byIndex(5)) && i1 % 6 == 5;

         if (!world.isSideSolid(pos.add(1, 0, 0), EnumFacing.byIndex(4)) && i1 % 6 == 4) {
            flag = true;
         }
         if (!world.isSideSolid(pos.add(0, 0, -1), EnumFacing.byIndex(3)) && i1 % 6 == 3) {
            flag = true;
         }
         if (!world.isSideSolid(pos.add(0, 0, 1), EnumFacing.byIndex(2)) && i1 % 6 == 2) {
            flag = true;
         }
         if (!world.isSideSolid(pos.add(0, -1, 0), EnumFacing.byIndex(1)) && i1 % 6 == 1) {
            flag = true;
         }
         if (!world.isSideSolid(pos.add(0, 1, 0), EnumFacing.byIndex(0)) && i1 % 6 == 0) {
            flag = true;
         }

         if (flag) {
            Block.spawnAsEntity(world, pos, new ItemStack(this, 1, i1));
            world.setBlockToAir(pos);
         }
      }
   }

   @Override
   public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side) {
      int l = side.getIndex();
      if (l == 0 && world.isSideSolid(pos.up(), EnumFacing.byIndex(0))) {
         return true;
      } else if (l == 1 && world.isSideSolid(pos.down(), EnumFacing.byIndex(1))) {
         return true;
      } else if (l == 2 && world.isSideSolid(pos.south(), EnumFacing.byIndex(2))) {
         return true;
      } else if (l == 3 && world.isSideSolid(pos.north(), EnumFacing.byIndex(3))) {
         return true;
      } else if (l == 4 && world.isSideSolid(pos.east(), EnumFacing.byIndex(4))) {
         return true;
      } else {
         return l == 5 && world.isSideSolid(pos.west(), EnumFacing.byIndex(5));
      }
   }

   @Override
   public boolean canPlaceBlockAt(World world, BlockPos pos) {
      if (world.isSideSolid(pos.west(), EnumFacing.byIndex(5))) {
         return true;
      } else if (world.isSideSolid(pos.east(), EnumFacing.byIndex(4))) {
         return true;
      } else if (world.isSideSolid(pos.north(), EnumFacing.byIndex(3))) {
         return true;
      } else if (world.isSideSolid(pos.south(), EnumFacing.byIndex(2))) {
         return true;
      } else {
         return world.isSideSolid(pos.down(), EnumFacing.byIndex(1)) || world.isSideSolid(pos.up(), EnumFacing.byIndex(0));
      }
   }

   @Override
   public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
      return true;
   }

   @Override
   public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
      return NULL_AABB;
   }

   @Override
   public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
      int meta = state.getBlock().getMetaFromState(state);
      return getBoundsForMeta(meta);
   }

   private static AxisAlignedBB getBoundsForMeta(int par1) {
      float w = 0.0625F;
      switch (par1 % 6) {
         case 0: return new AxisAlignedBB(0.0F, 1.0F - w, 0.0F, 1.0F, 1.0F, 1.0F);
         case 1: return new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, w, 1.0F);
         case 2: return new AxisAlignedBB(0.0F, 0.0F, 1.0F - w, 1.0F, 1.0F, 1.0F);
         case 3: return new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, w);
         case 4: return new AxisAlignedBB(1.0F - w, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
         case 5:
         default: return new AxisAlignedBB(0.0F, 0.0F, 0.0F, w, 1.0F, 1.0F);
      }
   }

   @Override
   public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) {
      return net.minecraft.util.EnumBlockRenderType.MODEL;
   }
}
