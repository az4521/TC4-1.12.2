package thaumcraft.common.blocks;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.Config;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.tiles.TileOwned;

import java.util.List;
import java.util.Random;

public class BlockCosmeticOpaque extends BlockContainer {
   // --- Variant property for 1.12.2 blockstate system ---
   public static final net.minecraft.block.properties.PropertyEnum<OpaqueVariant> VARIANT =
         net.minecraft.block.properties.PropertyEnum.create("variant", OpaqueVariant.class);

   public enum OpaqueVariant implements net.minecraft.util.IStringSerializable {
      AMBER(0, "amber"), AMBERBRICK(1, "amberbrick"), WARDEDGLASS(2, "wardedglass");
      private final int meta;
      private final String name;
      OpaqueVariant(int meta, String name) { this.meta = meta; this.name = name; }
      public int getMeta() { return meta; }
      @Override public String getName() { return name; }
      public static OpaqueVariant byMeta(int m) {
         for (OpaqueVariant v : values()) if (v.meta == m) return v;
         return AMBER;
      }
   }

   @Override
   protected net.minecraft.block.state.BlockStateContainer createBlockState() {
      return new net.minecraft.block.state.BlockStateContainer(this, VARIANT);
   }

   @Override
   public net.minecraft.block.state.IBlockState getStateFromMeta(int meta) {
      return this.getDefaultState().withProperty(VARIANT, OpaqueVariant.byMeta(meta));
   }

   @Override
   public int getMetaFromState(net.minecraft.block.state.IBlockState state) {
      return state.getValue(VARIANT).getMeta();
   }
   // --- End variant property ---

   public TextureAtlasSprite[] icon = new TextureAtlasSprite[3];
   public static TextureAtlasSprite[] wardedGlassIcon = new TextureAtlasSprite[47];
   public int currentPass;

   public BlockCosmeticOpaque() {
      super(net.minecraft.block.material.Material.ROCK);
      this.setResistance(5.0F);
      this.setHardness(1.5F);
      this.setSoundType(SoundType.STONE);
      this.setCreativeTab(Thaumcraft.tabTC);
   }

   // registerBlockIcons removed -- use ModelLoader/JSON models in 1.12.2

   @Override
   public boolean addHitEffects(IBlockState state, World world, RayTraceResult target, ParticleManager effectRenderer) {
      int md = state.getBlock().getMetaFromState(state);
      if (md == 2) {
         BlockPos pos = target.getBlockPos();
         float f = (float)target.hitVec.x - (float)pos.getX();
         float f1 = (float)target.hitVec.y - (float)pos.getY();
         float f2 = (float)target.hitVec.z - (float)pos.getZ();
         Thaumcraft.proxy.blockWard(world, pos.getX(), pos.getY(), pos.getZ(), target.sideHit, f, f1, f2);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public int getLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos) {
      int md = state.getBlock().getMetaFromState(state);
      return md <= 1 ? 3 : 255;
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
   public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
      list.add(new ItemStack(this, 1, 0));
      list.add(new ItemStack(this, 1, 1));
      list.add(new ItemStack(this, 1, 2));
   }

   @Override
   public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
      return true;
   }

   @Override
   public int quantityDropped(Random par1Random) {
      return 1;
   }

   @Override
   public int damageDropped(IBlockState state) {
      return state.getBlock().getMetaFromState(state);
   }

   @Override
   public TileEntity createTileEntity(World world, IBlockState state) {
      int metadata = state.getBlock().getMetaFromState(state);
      return metadata == 2 ? new TileOwned() : super.createTileEntity(world, state);
   }

   @Override
   public boolean hasTileEntity(IBlockState state) {
      int metadata = state.getBlock().getMetaFromState(state);
      return metadata == 2 || super.hasTileEntity(state);
   }

   @Override
   public TileEntity createNewTileEntity(World var1, int var2) {
      return null;
   }

   @Override
   public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos, Entity entity) {
      int md = state.getBlock().getMetaFromState(state);
      return md != 2 && super.canEntityDestroy(state, world, pos, entity);
   }

   @Override
   public void onBlockExploded(World world, BlockPos pos, Explosion explosion) {
      int md = world.getBlockState(pos).getBlock().getMetaFromState(world.getBlockState(pos));
      if (md != 2) {
         super.onBlockExploded(world, pos, explosion);
      }
   }

   @Override
   public boolean canDropFromExplosion(Explosion explosion) {
      return false;
   }

   @Override
   public void onBlockPlacedBy(World w, BlockPos pos, IBlockState state, EntityLivingBase p, ItemStack is) {
      TileEntity tile = w.getTileEntity(pos);
      if (tile instanceof TileOwned && p instanceof EntityPlayer) {
         ((TileOwned)tile).owner = p.getName();
         tile.markDirty();
      }
      super.onBlockPlacedBy(w, pos, state, p, is);
   }

   @Override
   public float getBlockHardness(IBlockState state, World world, BlockPos pos) {
      int md = state.getBlock().getMetaFromState(state);
      if (md == 2) {
         return Config.wardedStone ? -1.0F : 5.0F;
      } else {
         return super.getBlockHardness(state, world, pos);
      }
   }

   @Override
   public float getExplosionResistance(World world, BlockPos pos, Entity exploder, Explosion explosion) {
      int md = world.getBlockState(pos).getBlock().getMetaFromState(world.getBlockState(pos));
      return md == 2 ? 999.0F : super.getExplosionResistance(world, pos, exploder, explosion);
   }

   @Override
   public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) {
      return net.minecraft.util.EnumBlockRenderType.MODEL;
   }
}
