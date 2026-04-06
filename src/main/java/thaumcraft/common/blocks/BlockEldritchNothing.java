package thaumcraft.common.blocks;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import thaumcraft.common.lib.utils.BlockUtils;
import thaumcraft.common.tiles.TileEldritchNothing;

import java.util.Random;

public class BlockEldritchNothing extends Block {
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


   public BlockEldritchNothing() {
      super(Material.ROCK);
      this.setBlockUnbreakable();
      this.setResistance(6000000.0F);
      this.setLightLevel(0.2F);
      this.setTickRandomly(true);
   }

   // registerBlockIcons removed — textures are handled by JSON models in 1.12.2
   // getIcon removed — textures are handled by JSON models in 1.12.2

   @Override
   public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
      return ItemStack.EMPTY;
   }

   @Override
   public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World world, BlockPos pos) {
      return new AxisAlignedBB(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
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
   public boolean hasTileEntity(IBlockState state) {
      return this.getMetaFromState(state) == 1;
   }

   @Override
   public TileEntity createTileEntity(World world, IBlockState state) {
      return this.getMetaFromState(state) == 1 ? new TileEldritchNothing() : null;
   }

   @Override
   public Item getItemDropped(IBlockState state, Random rand, int fortune) {
      return Item.getItemById(0);
   }

   @Override
   public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
      int x = pos.getX(), y = pos.getY(), z = pos.getZ();
      if (BlockUtils.isBlockExposed(world, x, y, z)) {
         world.setBlockState(pos, this.getStateFromMeta(1), 3);
      } else {
         world.setBlockState(pos, this.getStateFromMeta(0), 3);
      }

      super.neighborChanged(state, world, pos, block, fromPos);
   }

   @Override
   public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
      if (entity.ticksExisted > 20 && (!(entity instanceof EntityPlayer) || !((EntityPlayer)entity).capabilities.isCreativeMode)) {
         entity.attackEntityFrom(DamageSource.OUT_OF_WORLD, 8.0F);
      }
   }

   @Override
   public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
      float f = 0.125F;
      return new AxisAlignedBB(f, f, f, 1.0F - f, 1.0F - f, 1.0F - f);
   }
}
