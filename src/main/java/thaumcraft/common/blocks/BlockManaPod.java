package thaumcraft.common.blocks;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import thaumcraft.api.WorldCoordinates;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.client.renderers.block.BlockRenderer;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.items.ItemManaBean;
import thaumcraft.common.tiles.TileManaPod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class BlockManaPod extends Block {
   static HashMap<WorldCoordinates, Aspect> st = new HashMap<>();
   public static final net.minecraft.block.properties.PropertyDirection FACING =
         net.minecraft.block.properties.PropertyDirection.create("facing", net.minecraft.util.EnumFacing.Plane.HORIZONTAL);

   public BlockManaPod() {
      super(Material.PLANTS);
      this.setTickRandomly(true);
      this.blockHardness = 0.5F;
      this.setDefaultState(this.blockState.getBaseState().withProperty(META, 0).withProperty(FACING, net.minecraft.util.EnumFacing.NORTH));
   }

   public static final net.minecraft.block.properties.PropertyInteger META =
         net.minecraft.block.properties.PropertyInteger.create("meta", 0, 15);

   @Override
   public BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, META, FACING);
   }

   @Override
   public IBlockState getStateFromMeta(int meta) {
      return this.getDefaultState().withProperty(META, meta).withProperty(FACING, net.minecraft.util.EnumFacing.NORTH);
   }

   @Override
   public int getMetaFromState(IBlockState state) {
      return state.getValue(META);
   }

   @Override
   public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
      return state.withProperty(FACING, getSupportFacing(worldIn, pos));
   }

   @Override
   public float getBlockHardness(IBlockState state, World world, BlockPos pos) {
      IBlockState s = world.getBlockState(pos);
      float md = (float)(8 - s.getBlock().getMetaFromState(s));
      return super.getBlockHardness(state, world, pos) / md;
   }

   @Override
   public boolean isOpaqueCube(IBlockState state) {
      return false;
   }

   @Override
   public boolean isFullCube(IBlockState state) {
      return false;
   }

   private static AxisAlignedBB getBoundsForMeta(int l) {
      switch (l) {
         case 0:  return new AxisAlignedBB(0.25, BlockRenderer.W12, 0.25, 0.75, 1.0, 0.75);
         case 1:  return new AxisAlignedBB(0.25, BlockRenderer.W10, 0.25, 0.75, 1.0, 0.75);
         case 2:  return new AxisAlignedBB(0.25, BlockRenderer.W8,  0.25, 0.75, 1.0, 0.75);
         case 3:  return new AxisAlignedBB(0.25, BlockRenderer.W6,  0.25, 0.75, 1.0, 0.75);
         case 4:  return new AxisAlignedBB(0.25, BlockRenderer.W5,  0.25, 0.75, 1.0, 0.75);
         case 5:  return new AxisAlignedBB(0.25, BlockRenderer.W4,  0.25, 0.75, 1.0, 0.75);
         case 6:  return new AxisAlignedBB(0.25, BlockRenderer.W3,  0.25, 0.75, 1.0, 0.75);
         case 7:  return new AxisAlignedBB(0.25, BlockRenderer.W2,  0.25, 0.75, 1.0, 0.75);
         default: return FULL_BLOCK_AABB;
      }
   }

   @Override
   public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
      IBlockState s = worldIn.getBlockState(pos);
      int l = s.getBlock().getMetaFromState(s);
      return getBoundsForMeta(l);
   }

   @Override
   public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
      IBlockState s = worldIn.getBlockState(pos);
      int l = s.getBlock().getMetaFromState(s);
      return getBoundsForMeta(l);
   }

   @Override
   public void updateTick(World par1World, BlockPos pos, IBlockState state, Random par5Random) {
      if (!this.canBlockStay(par1World, pos)) {
         dropManaBeans(par1World, pos, state);
         par1World.setBlockToAir(pos);
      } else if (par1World.rand.nextInt(30) == 0) {
         TileEntity tile = par1World.getTileEntity(pos);
         if (tile instanceof TileManaPod) {
            ((TileManaPod) tile).checkGrowth();
         }

         st.remove(new WorldCoordinates(pos.getX(), pos.getY(), pos.getZ(), par1World.provider.getDimension()));
      }
   }

   public boolean canBlockStay(World par1World, BlockPos pos) {
      Biome biome = par1World.getBiome(pos);
      boolean magicBiome = false;
      if (biome != null) {
         magicBiome = BiomeDictionary.hasType(biome, Type.MAGICAL);
      }

      if (!magicBiome) {
         return false;
      }

      return hasValidSupport(par1World, pos);
   }

   @Override
   public boolean canPlaceBlockOnSide(World world, BlockPos pos, net.minecraft.util.EnumFacing side) {
      Biome biome = world.getBiome(pos);
      boolean magicBiome = false;
      if (biome != null) {
         magicBiome = BiomeDictionary.hasType(biome, Type.MAGICAL);
      }

      if (!magicBiome || side.getAxis().isVertical()) {
         return false;
      }

      Block support = world.getBlockState(pos.offset(side.getOpposite())).getBlock();
      return support == Blocks.LOG || support == Blocks.LOG2 || support == ConfigBlocks.blockMagicalLog;
   }

   public static net.minecraft.util.EnumFacing getSupportFacing(IBlockAccess world, BlockPos pos) {
      for(net.minecraft.util.EnumFacing facing : net.minecraft.util.EnumFacing.HORIZONTALS) {
         Block support = world.getBlockState(pos.offset(facing)).getBlock();
         if (support == Blocks.LOG || support == Blocks.LOG2 || support == ConfigBlocks.blockMagicalLog) {
            return facing;
         }
      }

      return net.minecraft.util.EnumFacing.NORTH;
   }

   private static boolean hasValidSupport(IBlockAccess world, BlockPos pos) {
      for(net.minecraft.util.EnumFacing facing : net.minecraft.util.EnumFacing.HORIZONTALS) {
         Block support = world.getBlockState(pos.offset(facing)).getBlock();
         if (support == Blocks.LOG || support == Blocks.LOG2 || support == ConfigBlocks.blockMagicalLog) {
            return true;
         }
      }

      return false;
   }

   @Override
   public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
      IBlockState s = world.getBlockState(pos);
      return s.getBlock().getMetaFromState(s);
   }

   @Override
   public void neighborChanged(IBlockState state, World par1World, BlockPos pos, Block par5, BlockPos fromPos) {
      if (!this.canBlockStay(par1World, pos)) {
         dropManaBeans(par1World, pos, state);
         par1World.setBlockToAir(pos);
      }
   }

   @Override
   public void breakBlock(World world, BlockPos pos, IBlockState state) {
      TileEntity tile = world.getTileEntity(pos);
      if (tile instanceof TileManaPod && ((TileManaPod) tile).aspect != null) {
         st.put(new WorldCoordinates(pos.getX(), pos.getY(), pos.getZ(), world.provider.getDimension()), ((TileManaPod) tile).aspect);
      }

      super.breakBlock(world, pos, state);
   }

   @Override
   public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
      List<ItemStack> dropped = new ArrayList<>();
      int metadata = state.getBlock().getMetaFromState(state);
      if (metadata >= 2) {
         byte b0 = 1;
         if (metadata == 7 && world instanceof World && ((World) world).rand.nextFloat() > 0.33F) {
            b0 = 2;
         }

         Aspect aspect = Aspect.PLANT;
         WorldCoordinates wc = new WorldCoordinates(pos.getX(), pos.getY(), pos.getZ(),
                 world instanceof World ? ((World) world).provider.getDimension() : 0);
         if (st.containsKey(wc)) {
            aspect = st.get(wc);
         } else {
            TileEntity tile = world instanceof IBlockAccess ? ((IBlockAccess) world).getTileEntity(pos) : null;
            if (tile instanceof TileManaPod && ((TileManaPod) tile).aspect != null) {
               aspect = ((TileManaPod) tile).aspect;
            }
         }

         for (int k1 = 0; k1 < b0; ++k1) {
            ItemStack i = new ItemStack(ConfigItems.itemManaBean);
            ((ItemManaBean) i.getItem()).setAspects(i, (new AspectList()).add(aspect, 1));
            dropped.add(i);
         }

         st.remove(wc);
      }
      return dropped;
   }

   private void dropManaBeans(World world, BlockPos pos, IBlockState state) {
      for(ItemStack stack : getDrops(world, pos, state, 0)) {
         Block.spawnAsEntity(world, pos, stack);
      }
   }

   @Override
   @SideOnly(Side.CLIENT)
   public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
      return new ItemStack(ConfigItems.itemManaBean);
   }

   @Override
   public Item getItemDropped(IBlockState state, Random par2Random, int par3) {
      return Item.getItemById(0);
   }

   @Override
   public net.minecraft.util.EnumBlockRenderType getRenderType(IBlockState state) {
      return net.minecraft.util.EnumBlockRenderType.MODEL;
   }

   @Override
   @SideOnly(Side.CLIENT)
   public net.minecraft.util.BlockRenderLayer getRenderLayer() {
      return net.minecraft.util.BlockRenderLayer.CUTOUT;
   }

   @Override
   public boolean hasTileEntity(IBlockState state) {
      return true;
   }

   @Override
   public TileEntity createTileEntity(World world, IBlockState state) {
      return new TileManaPod();
   }
}
