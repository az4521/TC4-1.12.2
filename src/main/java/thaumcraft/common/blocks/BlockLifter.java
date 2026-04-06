package thaumcraft.common.blocks;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.tiles.TileLifter;

import java.util.Random;

public class BlockLifter extends BlockContainer {
   private Random random = new Random();

   public BlockLifter() {
      super(Material.WOOD);
      this.setHardness(2.5F);
      this.setResistance(15.0F);
      this.setSoundType(SoundType.WOOD);
      this.setCreativeTab(Thaumcraft.tabTC);
   }

   // registerBlockIcons removed — textures are handled by JSON models in 1.12.2
   // getIcon removed — textures are handled by JSON models in 1.12.2

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
   public void randomDisplayTick(IBlockState state, World w, BlockPos pos, Random r) {
      TileEntity te = w.getTileEntity(pos);
      if (te instanceof TileLifter && !((TileLifter) te).gettingPower() && ((TileLifter) te).rangeAbove > 0) {
         int i = pos.getX(), j = pos.getY(), k = pos.getZ();
         Thaumcraft.proxy.sparkle((float)i + 0.2F + r.nextFloat() * 0.6F, (float)(j + 1), (float)k + 0.2F + r.nextFloat() * 0.6F, 1.0F, 3, -0.3F);
      }
   }

   @Override
   public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
      return side != EnumFacing.UP && side != EnumFacing.DOWN;
   }

   @Override
   public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
      return side != null && side != EnumFacing.UP && side != EnumFacing.DOWN;
   }

   @Override
   public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
      this.updateLifterStack(world, pos.getX(), pos.getY(), pos.getZ());
      super.onBlockAdded(world, pos, state);
   }

   @Override
   public void breakBlock(World world, BlockPos pos, IBlockState state) {
      this.updateLifterStack(world, pos.getX(), pos.getY(), pos.getZ());
      super.breakBlock(world, pos, state);
   }

   @Override
   public void neighborChanged(IBlockState state, World world, BlockPos pos, Block par5, BlockPos fromPos) {
      TileEntity te = world.getTileEntity(pos);
      if (te instanceof TileLifter && ((TileLifter) te).gettingPower() != ((TileLifter) te).lastPowerState) {
         this.updateLifterStack(world, pos.getX(), pos.getY(), pos.getZ());
      }
      super.neighborChanged(state, world, pos, par5, fromPos);
   }

   private void updateLifterStack(World world, int xCoord, int yCoord, int zCoord) {
      for (int count = 1; world.getBlockState(new BlockPos(xCoord, yCoord - count, zCoord)).getBlock() == this; ++count) {
         TileEntity te = world.getTileEntity(new BlockPos(xCoord, yCoord - count, zCoord));
         if (te instanceof TileLifter) {
            ((TileLifter) te).requiresUpdate = true;
         }
      }
   }

   @Override
   public TileEntity createNewTileEntity(World world, int meta) {
      return new TileLifter();
   }

   @Override
   public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) {
      return net.minecraft.util.EnumBlockRenderType.MODEL;
   }
}
