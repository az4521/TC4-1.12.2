package thaumcraft.common.blocks;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import thaumcraft.api.crafting.IInfusionStabiliser;
import thaumcraft.common.Thaumcraft;

import java.util.Random;

public class BlockCandle extends Block implements IInfusionStabiliser {
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


   public BlockCandle() {
      super(Material.CIRCUITS);
      this.setHardness(0.1F);
      // setStepSound removed in 1.12.2 — sound is now data-driven
      this.setCreativeTab(Thaumcraft.tabTC);
      this.setLightLevel(0.95F);
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
      for (int var4 = 0; var4 < 16; ++var4) {
         list.add(new ItemStack(this, 1, var4));
      }
   }

   // registerBlockIcons removed — textures are handled by JSON models in 1.12.2
   // getIcon removed — textures are handled by JSON models in 1.12.2
   // getRenderColor removed — tinting is handled by IBlockColor in 1.12.2

   @Override
   public boolean canPlaceBlockAt(World world, BlockPos pos) {
      return world.isSideSolid(pos.down(), EnumFacing.UP);
   }

   @Override
   public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
      boolean canPlace = world.isSideSolid(pos.down(), EnumFacing.UP);
      if (!canPlace) {
         int meta = this.getMetaFromState(state);
         Block.spawnAsEntity(world, pos, new ItemStack(this, 1, meta));
         world.setBlockToAir(pos);
      } else {
         super.neighborChanged(state, world, pos, block, fromPos);
      }
   }

   // canPlaceBlockOnSide — covered by canPlaceBlockAt override above

   public int damageDropped(IBlockState state) {
      return this.getMetaFromState(state);
   }

   @Override
   public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
      return new AxisAlignedBB(0.375, 0.0, 0.375, 0.625, 0.5, 0.625);
   }

   @Override
   public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
      return NULL_AABB;
   }

   @Override
   public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side) {
      return false;
   }

   @Override
   public boolean isOpaqueCube(IBlockState state) {
      return false;
   }

   @Override
   public boolean isFullCube(IBlockState state) {
      return false;
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
      double var7 = pos.getX() + 0.5;
      double var9 = pos.getY() + 0.7;
      double var11 = pos.getZ() + 0.5;
      world.spawnParticle(net.minecraft.util.EnumParticleTypes.SMOKE_NORMAL, var7, var9, var11, 0.0, 0.0, 0.0);
      world.spawnParticle(net.minecraft.util.EnumParticleTypes.FLAME, var7, var9, var11, 0.0, 0.0, 0.0);
   }

   @Override
   public boolean canStabaliseInfusion(World world, int x, int y, int z) {
      return true;
   }
}
