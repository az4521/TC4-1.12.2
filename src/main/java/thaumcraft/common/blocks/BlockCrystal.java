package thaumcraft.common.blocks;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import thaumcraft.api.crafting.IInfusionStabiliser;
import thaumcraft.client.fx.ParticleEngine;
import thaumcraft.client.fx.particles.FXSpark;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.tiles.TileCrystal;
import thaumcraft.common.tiles.TileEldritchCrystal;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class BlockCrystal extends BlockContainer implements IInfusionStabiliser {
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

   private Random random = new Random();

   public BlockCrystal() {
      super(Material.GLASS);
      this.setHardness(0.7F);
      this.setResistance(1.0F);
      this.setLightLevel(0.5F);
      // setStepSound removed in 1.12.2 — sound is now data-driven
      this.setCreativeTab(Thaumcraft.tabTC);
   }

   @Override
   @SideOnly(Side.CLIENT)
   public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
      for (int var4 = 0; var4 <= 6; ++var4) {
         list.add(new ItemStack(this, 1, var4));
      }
   }

   // registerBlockIcons removed — textures are handled by JSON models in 1.12.2
   // getIcon removed — textures are handled by JSON models in 1.12.2

   @Override
   public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) {
      return net.minecraft.util.EnumBlockRenderType.INVISIBLE;
   }
   // renderAsNormalBlock() removed in 1.12.2

   @Override
   public boolean isOpaqueCube(IBlockState state) {
      return false;
   }

   @Override
   public boolean isFullCube(IBlockState state) {
      return false;
   }

   @Override
   @SideOnly(Side.CLIENT)
   public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random random) {
      int md = this.getMetaFromState(state);
      int i = pos.getX(), j = pos.getY(), k = pos.getZ();
      if (md <= 6 && random.nextInt(17) == 0) {
         FXSpark ef = new FXSpark(world,
               (double)i + 0.3 + (double)(world.rand.nextFloat() * 0.4F),
               (double)j + 0.3 + (double)(world.rand.nextFloat() * 0.4F),
               (double)k + 0.3 + (double)(world.rand.nextFloat() * 0.4F),
               0.2F + random.nextFloat() * 0.1F);
         Color c = new Color(md == 6 ? BlockCustomOreItem.colors[random.nextInt(6) + 1] : BlockCustomOreItem.colors[md + 1]);
         ef.setRBGColorF((float)c.getRed() / 255.0F, (float)c.getGreen() / 255.0F, (float)c.getBlue() / 255.0F);
         ef.setAlphaF(0.8F);
         ParticleEngine.instance.addEffect(world, ef);
      }
   }

   // colorMultiplier removed in 1.12.2 — tinting is handled by IBlockColor

   @Override
   public boolean hasTileEntity(IBlockState state) {
      int metadata = this.getMetaFromState(state);
      return metadata <= 7;
   }

   @Override
   public TileEntity createTileEntity(World world, IBlockState state) {
      int metadata = this.getMetaFromState(state);
      if (metadata <= 6) {
         return new TileCrystal();
      } else {
         return metadata == 7 ? new TileEldritchCrystal() : null;
      }
   }

   @Override
   public TileEntity createNewTileEntity(World var1, int md) {
      return null;
   }

   @Override
   public int damageDropped(IBlockState state) {
      return this.getMetaFromState(state);
   }

   @Override
   public java.util.List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
      ArrayList<ItemStack> ret = new ArrayList<>();
      int md = this.getMetaFromState(state);
      if (md < 6) {
         for (int t = 0; t < 6; ++t) {
            ret.add(new ItemStack(ConfigItems.itemShard, 1, md));
         }
         return ret;
      } else if (md == 6) {
         for (int t = 0; t < 6; ++t) {
            ret.add(new ItemStack(ConfigItems.itemShard, 1, t));
         }
         return ret;
      } else if (md == 7) {
         ret.add(new ItemStack(ConfigItems.itemShard, 1, 6));
         return ret;
      } else {
         return super.getDrops(world, pos, state, fortune);
      }
   }

   @Override
   public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
      super.neighborChanged(state, world, pos, block, fromPos);
      int md = this.getMetaFromState(state);
      if (md <= 6 && this.checkIfAttachedToBlock(world, pos, state)) {
         TileCrystal tes = (TileCrystal) world.getTileEntity(pos);
         if (tes == null) return;
         int i1 = tes.orientation;
         boolean flag = !world.isSideSolid(pos.west(),  EnumFacing.byIndex(5)) && i1 == 5;
         if (!world.isSideSolid(pos.east(),  EnumFacing.byIndex(4)) && i1 == 4) flag = true;
         if (!world.isSideSolid(pos.north(), EnumFacing.byIndex(3)) && i1 == 3) flag = true;
         if (!world.isSideSolid(pos.south(), EnumFacing.byIndex(2)) && i1 == 2) flag = true;
         if (!world.isSideSolid(pos.down(),  EnumFacing.byIndex(1)) && i1 == 1) flag = true;
         if (!world.isSideSolid(pos.up(),    EnumFacing.byIndex(0)) && i1 == 0) flag = true;
         if (flag) {
            Block.spawnAsEntity(world, pos, new ItemStack(this, 1, md));
            world.setBlockToAir(pos);
         }
      } else if (md == 7) {
         TileCrystal tes = (TileCrystal) world.getTileEntity(pos);
         if (tes == null) return;
         EnumFacing fd = EnumFacing.byIndex(tes.orientation).getOpposite();
         BlockPos behind = pos.add(fd.getXOffset(), fd.getYOffset(), fd.getZOffset());
         if (world.isAirBlock(behind)) {
            Block.spawnAsEntity(world, pos, new ItemStack(this, 1, md));
            world.setBlockToAir(pos);
         }
      }
   }

   private boolean checkIfAttachedToBlock(World world, BlockPos pos, IBlockState state) {
      if (!this.canPlaceBlockAt(world, pos)) {
         int md = this.getMetaFromState(state);
         Block.spawnAsEntity(world, pos, new ItemStack(this, 1, md));
         world.setBlockToAir(pos);
         return false;
      } else {
         return true;
      }
   }

   @Override
   public boolean canPlaceBlockAt(World world, BlockPos pos) {
      return world.isSideSolid(pos.west(),  EnumFacing.byIndex(5))
          || world.isSideSolid(pos.east(),  EnumFacing.byIndex(4))
          || world.isSideSolid(pos.north(), EnumFacing.byIndex(3))
          || world.isSideSolid(pos.south(), EnumFacing.byIndex(2))
          || world.isSideSolid(pos.down(),  EnumFacing.byIndex(1))
          || world.isSideSolid(pos.up(),    EnumFacing.byIndex(0));
   }

   @Override
   public boolean canStabaliseInfusion(World world, int x, int y, int z) {
      return true;
   }
}
