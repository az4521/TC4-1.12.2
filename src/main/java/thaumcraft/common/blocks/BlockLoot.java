package thaumcraft.common.blocks;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import thaumcraft.client.renderers.block.BlockRenderer;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.lib.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class BlockLoot extends Block {
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

   String iconPre = "urn";
   int renderType = 0;

   public BlockLoot(Material mat, String ip, int rt) {
      super(mat);
      this.setHardness(0.15F);
      this.setResistance(0.0F);
      this.iconPre = ip;
      this.renderType = rt;
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

   @Override
   public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
      if (this.renderType == 1) {
         return new AxisAlignedBB(BlockRenderer.W2, BlockRenderer.W1, BlockRenderer.W2,
               BlockRenderer.W14, BlockRenderer.W13, BlockRenderer.W14);
      } else {
         return new AxisAlignedBB(BlockRenderer.W1, 0.0F, BlockRenderer.W1,
               BlockRenderer.W15, BlockRenderer.W14, BlockRenderer.W15);
      }
   }

   @Override
   public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World world, BlockPos pos) {
      return getBoundingBox(state, world, pos).offset(pos);
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
      list.add(new ItemStack(Item.getItemFromBlock(this), 1, 0));
      list.add(new ItemStack(Item.getItemFromBlock(this), 1, 1));
      list.add(new ItemStack(Item.getItemFromBlock(this), 1, 2));
   }

   @Override
   public int damageDropped(IBlockState state) {
      return this.getMetaFromState(state);
   }

   @Override
   public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
      ArrayList<ItemStack> ret = new ArrayList<>();
      int md = this.getMetaFromState(state);
      int q = 1 + md + ((World) world).rand.nextInt(3);

      for (int a = 0; a < q; ++a) {
         ItemStack is = Utils.generateLoot(md, ((World) world).rand);
         if (is != null) {
            ret.add(is.copy());
         }
      }

      return ret;
   }
}
