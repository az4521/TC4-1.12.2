package thaumcraft.common.blocks;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.tiles.TileEssentiaReservoir;

public class BlockEssentiaReservoir extends BlockContainer {
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


   public BlockEssentiaReservoir() {
      super(Material.IRON);
      this.setHardness(2.0F);
      this.setResistance(17.0F);
      this.setSoundType(net.minecraft.block.SoundType.METAL);
      this.setCreativeTab(Thaumcraft.tabTC);
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
      return state.getBlock().getMetaFromState(state);
   }

   @Override
   public TileEntity createTileEntity(World world, IBlockState state) {
      int metadata = state.getBlock().getMetaFromState(state);
      return metadata == 0 ? new TileEssentiaReservoir() : super.createTileEntity(world, state);
   }

   @Override
   public TileEntity createNewTileEntity(World var1, int md) {
      return null;
   }

   @Override
   public boolean hasComparatorInputOverride(IBlockState state) {
      return true;
   }

   @Override
   public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos) {
      TileEntity te = world.getTileEntity(pos);
      if (te instanceof TileEssentiaReservoir) {
         float r = (float) ((TileEssentiaReservoir) te).essentia.visSize()
               / (float) ((TileEssentiaReservoir) te).maxAmount;
         return MathHelper.floor(r * 14.0F)
               + (((TileEssentiaReservoir) te).essentia.visSize() > 0 ? 1 : 0);
      } else {
         return 0;
      }
   }

   @Override
   public void breakBlock(World world, BlockPos pos, IBlockState state) {
      TileEntity te = world.getTileEntity(pos);
      if (te instanceof TileEssentiaReservoir) {
         int sz = ((TileEssentiaReservoir) te).essentia.visSize() / 16;
         int q = 0;
         if (sz > 0) {
            world.createExplosion(null,
                  pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                  1.0F, false);

            for (int a = 0; a < 50; ++a) {
               int xx = pos.getX() + world.rand.nextInt(5) - world.rand.nextInt(5);
               int yy = pos.getY() + world.rand.nextInt(5) - world.rand.nextInt(5);
               int zz = pos.getZ() + world.rand.nextInt(5) - world.rand.nextInt(5);
               BlockPos target = new BlockPos(xx, yy, zz);
               if (world.isAirBlock(target)) {
                  if (yy < pos.getY()) {
                     world.setBlockState(target, ConfigBlocks.blockFluxGoo.getDefaultState(), 3);
                  } else {
                     world.setBlockState(target, ConfigBlocks.blockFluxGas.getDefaultState(), 3);
                  }
                  if (q++ >= sz) {
                     break;
                  }
               }
            }
         }
      }
      super.breakBlock(world, pos, state);
   }

   @Override
   public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) {
      return net.minecraft.util.EnumBlockRenderType.MODEL;
   }

   public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
      return layer == BlockRenderLayer.TRANSLUCENT;
   }
}
