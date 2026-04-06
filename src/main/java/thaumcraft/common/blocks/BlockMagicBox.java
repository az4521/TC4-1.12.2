package thaumcraft.common.blocks;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.lib.utils.InventoryUtils;
import thaumcraft.common.tiles.TileMagicBox;

import java.util.List;

public class BlockMagicBox extends BlockContainer {

   public BlockMagicBox() {
      super(Material.WOOD);
      this.setHardness(2.5F);
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

   @SideOnly(Side.CLIENT)
   @Override
   public void getSubBlocks(CreativeTabs par2CreativeTabs, net.minecraft.util.NonNullList<ItemStack> par3List) {
   }

   @Override
   public void breakBlock(World world, BlockPos pos, IBlockState state) {
      InventoryUtils.dropItems(world, pos.getX(), pos.getY(), pos.getZ());
      super.breakBlock(world, pos, state);
   }

   @Override
   public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
         EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
      TileEntity te = world.getTileEntity(pos);
      if (te == null) {
         return true;
      } else if (world.isRemote) {
         return true;
      } else {
         player.openGui(Thaumcraft.instance, 18, world, pos.getX(), pos.getY(), pos.getZ());
         return true;
      }
   }

   @Override
   public TileEntity createNewTileEntity(World world, int meta) {
      return new TileMagicBox();
   }

   @Override
   public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) {
      return net.minecraft.util.EnumBlockRenderType.MODEL;
   }
}
