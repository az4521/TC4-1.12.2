package thaumcraft.common.blocks;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.BlockBush;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import thaumcraft.client.fx.ParticleEngine;
import thaumcraft.client.fx.particles.FXWisp;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.lib.world.WorldGenGreatwoodTrees;
import thaumcraft.common.lib.world.WorldGenSilverwoodTrees;
import thaumcraft.common.tiles.TileEtherealBloom;

import java.util.Random;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import thaumcraft.client.renderers.compat.IIconRegister;

public class BlockCustomPlant extends BlockBush {
   // --- Variant property for 1.12.2 blockstate system ---
   public static final net.minecraft.block.properties.PropertyEnum<PlantVariant> VARIANT =
         net.minecraft.block.properties.PropertyEnum.create("variant", PlantVariant.class);

   public enum PlantVariant implements net.minecraft.util.IStringSerializable {
      GREATWOODSAP(0, "greatwoodsap"), SILVERWOODSAP(1, "silverwoodsap"), SHIMMERLEAF(2, "shimmerleaf"), CINDERPEARL(3, "cinderpearl"), ETHEREALBLOOM(4, "etherealbloom"), MANASHROOM(5, "manashroom");
      private final int meta;
      private final String name;
      PlantVariant(int meta, String name) { this.meta = meta; this.name = name; }
      public int getMeta() { return meta; }
      @Override public String getName() { return name; }
      public static PlantVariant byMeta(int m) {
         for (PlantVariant v : values()) if (v.meta == m) return v;
         return SHIMMERLEAF;
      }
   }

   @Override
   protected net.minecraft.block.state.BlockStateContainer createBlockState() {
      return new net.minecraft.block.state.BlockStateContainer(this, VARIANT);
   }

   @Override
   public net.minecraft.block.state.IBlockState getStateFromMeta(int meta) {
      return this.getDefaultState().withProperty(VARIANT, PlantVariant.byMeta(meta));
   }

   @Override
   public int getMetaFromState(net.minecraft.block.state.IBlockState state) {
      return state.getValue(VARIANT).getMeta();
   }
   // --- End variant property ---


   @SideOnly(Side.CLIENT)
   public TextureAtlasSprite iconLeaves;
   @SideOnly(Side.CLIENT)
   public TextureAtlasSprite iconStalk;

   @SideOnly(Side.CLIENT)
   public void registerBlockIcons(IIconRegister ir) {
      this.iconLeaves = ir.registerSprite("thaumcraft:bloom_leaves");
      this.iconStalk  = ir.registerSprite("thaumcraft:bloom_stalk");
   }

   public BlockCustomPlant() {
      super(Material.PLANTS);
      this.setCreativeTab(Thaumcraft.tabTC);
   }

   @Override
   public void getSubBlocks(CreativeTabs par2CreativeTabs, net.minecraft.util.NonNullList<ItemStack> par3List) {
      for(int var4 = 0; var4 <= 5; ++var4) {
         par3List.add(new ItemStack(this, 1, var4));
      }
   }

   @Override
   public boolean hasTileEntity(IBlockState state) {
      int metadata = this.getMetaFromState(state);
      return metadata == 4 || super.hasTileEntity(state);
   }

   @Override
   public TileEntity createTileEntity(World world, IBlockState state) {
      int metadata = this.getMetaFromState(state);
      return metadata == 4 ? new TileEtherealBloom() : super.createTileEntity(world, state);
   }

   public int damageDropped(IBlockState state) {
      return this.getMetaFromState(state);
   }

   public Item getItemDropped(IBlockState state, Random par2Random, int par3) {
      return Item.getItemFromBlock(this);
   }

   public EnumPlantType getPlantType(IBlockAccess world, BlockPos pos) {
      int md = this.getMetaFromState(world.getBlockState(pos));
      if (md == 3) {
         return EnumPlantType.Desert;
      } else {
         return md == 4 ? EnumPlantType.Cave : EnumPlantType.Plains;
      }
   }

   @Override
   public boolean canPlaceBlockAt(World par1World, BlockPos pos) {
      return true;
   }

   @Override
   public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
      if (!world.isRemote) {
         super.updateTick(world, pos, state, random);
         int l = this.getMetaFromState(state);
         if (l == 0 && world.getLightFromNeighbors(pos.up()) >= 9 && random.nextInt(25) == 0) {
            this.growGreatTree(world, pos.getX(), pos.getY(), pos.getZ(), random);
         } else if (l == 1 && world.getLightFromNeighbors(pos.up()) >= 9 && random.nextInt(50) == 0) {
            this.growSilverTree(world, pos.getX(), pos.getY(), pos.getZ(), random);
         }
      }
   }

   public void growGreatTree(World world, int i, int j, int k, Random random) {
      if (world != null && world.provider != null) {
         if (!world.isRemote) {
            world.setBlockToAir(new BlockPos(i, j, k));
            WorldGenGreatwoodTrees obj = new WorldGenGreatwoodTrees(true);
            if (!obj.generate(world, random, i, j, k, false)) {
               world.setBlockState(new BlockPos(i, j, k), this.getDefaultState(), 0);
            }
         }
      }
   }

   public void growSilverTree(World world, int i, int j, int k, Random random) {
      if (world != null && world.provider != null) {
         if (!world.isRemote) {
            world.setBlockToAir(new BlockPos(i, j, k));
            WorldGenSilverwoodTrees obj = new WorldGenSilverwoodTrees(true, 7, 5);
            if (!obj.generate(world, random, new BlockPos(i, j, k))) {
               world.setBlockState(new BlockPos(i, j, k), this.getStateFromMeta(1), 0);
            }
         }
      }
   }

   @Override
   public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
      int md = this.getMetaFromState(state);
      if (md != 1 && md != 2 && md != 3 && md != 5) {
         return md == 4 ? 15 : super.getLightValue(state, world, pos);
      } else {
         return 8;
      }
   }

   @Override
   public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
      int md = this.getMetaFromState(state);
      if (md == 5 && entity instanceof EntityLivingBase) {
         ((EntityLivingBase)entity).addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 200, 0));
      }
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random random) {
      int md = this.getMetaFromState(state);
      int i = pos.getX(), j = pos.getY(), k = pos.getZ();
      if (md == 2 && random.nextInt(3) == 0) {
         float cr = 0.3F + world.rand.nextFloat() * 0.3F;
         float cg = 0.7F + world.rand.nextFloat() * 0.3F;
         float cb = 0.7F + world.rand.nextFloat() * 0.3F;
         float xr = (float)i + 0.5F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.1F;
         float yr = (float)j + 0.5F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.15F;
         float zr = (float)k + 0.5F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.1F;
         FXWisp ef = new FXWisp(world, xr, yr, zr, 0.2F, cr, cg, cb);
         ef.tinkle = false;
         ParticleEngine.instance.addEffect(world, ef);
      } else if (md == 3 && random.nextBoolean()) {
         float xr = (float)i + 0.5F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.1F;
         float yr = (float)j + 0.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.1F;
         float zr = (float)k + 0.5F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.1F;
         world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, xr, yr, zr, 0.0F, 0.0F, 0.0F);
         world.spawnParticle(EnumParticleTypes.FLAME, xr, yr, zr, 0.0F, 0.0F, 0.0F);
      } else if (md == 5 && random.nextInt(3) == 0) {
         float xr = (float)i + 0.5F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.4F;
         float yr = (float)j + 0.3F;
         float zr = (float)k + 0.5F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.4F;
         FXWisp ef = new FXWisp(world, xr, yr, zr, 0.1F, 0.5F, 0.3F, 0.8F);
         ef.tinkle = false;
         ef.shrink = true;
         ef.setGravity(0.015F);
         ParticleEngine.instance.addEffect(world, ef);
      }
   }

   public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
      return 100;
   }

   public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
      return 60;
   }
}
