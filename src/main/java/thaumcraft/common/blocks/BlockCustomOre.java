package thaumcraft.common.blocks;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;

import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigItems;

import java.util.ArrayList;
import java.util.Random;

public class BlockCustomOre extends Block {
   // --- Variant property for 1.12.2 blockstate system ---
   public static final net.minecraft.block.properties.PropertyEnum<OreVariant> VARIANT =
         net.minecraft.block.properties.PropertyEnum.create("variant", OreVariant.class);

   public enum OreVariant implements net.minecraft.util.IStringSerializable {
      CINNABAR(0, "cinnabar"), INFUSEDAIR(1, "infusedair"), INFUSEDFIRE(2, "infusedfire"), INFUSEDWATER(3, "infusedwater"), INFUSEDEARTH(4, "infusedearth"), INFUSEDORDER(5, "infusedorder"), INFUSEDENTROPY(6, "infusedentropy"), AMBER(7, "amber");
      private final int meta;
      private final String name;
      OreVariant(int meta, String name) { this.meta = meta; this.name = name; }
      public int getMeta() { return meta; }
      @Override public String getName() { return name; }
      public static OreVariant byMeta(int m) {
         for (OreVariant v : values()) if (v.meta == m) return v;
         return CINNABAR;
      }
   }

   @Override
   protected net.minecraft.block.state.BlockStateContainer createBlockState() {
      return new net.minecraft.block.state.BlockStateContainer(this, VARIANT);
   }

   @Override
   public net.minecraft.block.state.IBlockState getStateFromMeta(int meta) {
      return this.getDefaultState().withProperty(VARIANT, OreVariant.byMeta(meta));
   }

   @Override
   public int getMetaFromState(net.minecraft.block.state.IBlockState state) {
      return state.getValue(VARIANT).getMeta();
   }
   // --- End variant property ---

   private Random rand = new Random();

   public BlockCustomOre() {
      super(Material.ROCK);
      this.setResistance(5.0F);
      this.setHardness(1.5F);
      this.setCreativeTab(Thaumcraft.tabTC);
      this.setTickRandomly(true);
   }

   @Override
   public net.minecraft.util.BlockRenderLayer getRenderLayer() {
      return net.minecraft.util.BlockRenderLayer.CUTOUT_MIPPED;
   }

   // registerBlockIcons removed — textures are handled by JSON models in 1.12.2
   // getIcon removed — textures are handled by JSON models in 1.12.2

   @Override
   public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
      return true;
   }

   @Override
   public int damageDropped(IBlockState state) {
      return this.getMetaFromState(state);
   }

   @Override
   @SideOnly(Side.CLIENT)
   public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
      list.add(new ItemStack(this, 1, 0));
      list.add(new ItemStack(this, 1, 1));
      list.add(new ItemStack(this, 1, 2));
      list.add(new ItemStack(this, 1, 3));
      list.add(new ItemStack(this, 1, 4));
      list.add(new ItemStack(this, 1, 5));
      list.add(new ItemStack(this, 1, 6));
      list.add(new ItemStack(this, 1, 7));
   }

   @Override
   @SideOnly(Side.CLIENT)
   public boolean addHitEffects(IBlockState state, World world, RayTraceResult target, ParticleManager effectRenderer) {
      BlockPos pos = target.getBlockPos();
      int md = this.getMetaFromState(state);
      if (md != 0 && md < 6) {
         UtilsFX.infusedStoneSparkle(world, pos.getX(), pos.getY(), pos.getZ(), md);
      }
      return super.addHitEffects(state, world, target, effectRenderer);
   }

   @Override
   @SideOnly(Side.CLIENT)
   public boolean addDestroyEffects(World world, BlockPos pos, ParticleManager effectRenderer) {
      return super.addDestroyEffects(world, pos, effectRenderer);
   }

   // setBlockBoundsBasedOnState removed — full cube is default in 1.12.2
   // addCollisionBoxesToList — only override if non-standard bounds; full cube is default

   @Override
   public java.util.List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
      ArrayList<ItemStack> ret = new ArrayList<>();
      int md = this.getMetaFromState(state);
      if (md == 0) {
         ret.add(new ItemStack(ConfigBlocks.blockCustomOre, 1, 0));
      } else if (md == 7) {
         ret.add(new ItemStack(ConfigItems.itemResource, 1 + ((World) world).rand.nextInt(fortune + 1), 6));
      } else {
         int q = 1 + ((World) world).rand.nextInt(2 + fortune);
         for (int a = 0; a < q; ++a) {
            ret.add(new ItemStack(ConfigItems.itemShard, 1, md - 1));
         }
      }
      return ret;
   }

   public int getExpDrop(IBlockAccess world, IBlockState state, int fortune) {
      int md = this.getMetaFromState(state);
      if (md == 0) return 0;
      if (md == 7) return this.rand.nextInt(4) + 1;
      return this.rand.nextInt(4);
   }

   @Override
   public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side) {
      return true;
   }

   // renderAsNormalBlock() removed in 1.12.2
   // getRenderType() removed — defaults to MODEL in 1.12.2
}
