package thaumcraft.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import thaumcraft.common.config.Config;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.tiles.TileEldritchPortal;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class BlockEldritchPortal extends Block {

   public BlockEldritchPortal() {
      super(Config.airyMaterial);
   }

   @Override
   public float getBlockHardness(IBlockState state, World world, BlockPos pos) {
      return -1.0F;
   }

   @Override
   public float getExplosionResistance(World world, BlockPos pos, @Nullable Entity exploder, Explosion explosion) {
      return 200000.0F;
   }

   @Override
   public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
      return 15;
   }

   @Override
   public boolean isReplaceable(IBlockAccess world, BlockPos pos) {
      return false;
   }

   @Override
   public boolean canBeReplacedByLeaves(IBlockState state, IBlockAccess world, BlockPos pos) {
      return false;
   }

   @Override
   @Nullable
   public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
      return NULL_AABB;
   }

   @Override
   public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entity, boolean isActualState) {
   }

   @Override
   public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World world, BlockPos pos) {
      return new AxisAlignedBB(0, 0, 0, 0, 0, 0);
   }

   @Override
   public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
      return false;
   }

   @Override
   public EnumBlockRenderType getRenderType(IBlockState state) {
      return EnumBlockRenderType.INVISIBLE;
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
   public int damageDropped(IBlockState state) {
      return 0;
   }

   @Override
   public Item getItemDropped(IBlockState state, Random rand, int fortune) {
      return Item.getItemById(0);
   }

   @Override
   public TileEntity createTileEntity(World world, IBlockState state) {
      return new TileEldritchPortal();
   }

   @Override
   public boolean hasTileEntity(IBlockState state) {
      return true;
   }

   @Override
   public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighbor, BlockPos fromPos) {
      if (world.getBlockState(pos.up()).getBlock() != ConfigBlocks.blockEldritch || world.getBlockState(pos.down()).getBlock() != ConfigBlocks.blockEldritch) {
         world.setBlockToAir(pos);
      }
      super.neighborChanged(state, world, pos, neighbor, fromPos);
   }
}
