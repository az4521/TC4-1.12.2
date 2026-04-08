package thaumcraft.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
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
      return this.getDefaultState().withProperty(META, 0);
   }

   @Override
   public int getMetaFromState(net.minecraft.block.state.IBlockState state) {
      return 0;
   }


   public BlockEldritchNothing() {
      super(Material.ROCK);
      this.setDefaultState(this.blockState.getBaseState().withProperty(META, 0));
      this.setBlockUnbreakable();
      this.setResistance(6000000.0F);
      this.setLightLevel(0.2F);
      this.setTickRandomly(false);
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

   public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
      return layer == BlockRenderLayer.TRANSLUCENT;
   }

   @Override
   public EnumBlockRenderType getRenderType(IBlockState state) {
      return EnumBlockRenderType.MODEL;
   }

   @Override
   public boolean hasTileEntity(IBlockState state) {
      return false;
   }

   @Override
   public Item getItemDropped(IBlockState state, Random rand, int fortune) {
      return Item.getItemById(0);
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
