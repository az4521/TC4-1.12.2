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
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.entities.IEldritchMob;
import thaumcraft.api.nodes.INode;
import thaumcraft.client.fx.ParticleEngine;
import thaumcraft.client.fx.particles.FXSpark;
import thaumcraft.client.fx.particles.FXSparkle;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.Config;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.entities.projectile.EntityShockOrb;
import thaumcraft.common.items.ItemWispEssence;
import thaumcraft.common.lib.world.ThaumcraftWorldGenerator;
import thaumcraft.common.tiles.*;

import java.util.List;
import java.util.Random;

public class BlockAiry extends BlockContainer {
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


   public BlockAiry() {
      super(Config.airyMaterial);
      this.setSoundType(SoundType.CLOTH);
      this.setCreativeTab(Thaumcraft.tabTC);
      this.setTickRandomly(true);
   }

   @SideOnly(Side.CLIENT)
   @Override
   public boolean addHitEffects(IBlockState state, World world, RayTraceResult target, ParticleManager effectRenderer) {
      int md = state.getBlock().getMetaFromState(state);
      if ((md == 0 || md == 5) && world.rand.nextBoolean()) {
         BlockPos pos = target.getBlockPos();
         UtilsFX.infusedStoneSparkle(world, pos.getX(), pos.getY(), pos.getZ(), 0);
      }

      return super.addHitEffects(state, world, target, effectRenderer);
   }

   @Override
   public boolean addDestroyEffects(World world, BlockPos pos, ParticleManager effectRenderer) {
      int md = world.getBlockState(pos).getBlock().getMetaFromState(world.getBlockState(pos));
      if (md == 0 || md == 5) {
         Thaumcraft.proxy.burst(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1.0F);
         world.playSound(null, pos,
               new SoundEvent(new ResourceLocation("thaumcraft", "craftfail")),
               SoundCategory.BLOCKS, 1.0F, 1.0F);
      }

      return super.addDestroyEffects(world, pos, effectRenderer);
   }

   @Override
   public float getBlockHardness(IBlockState state, World world, BlockPos pos) {
      int md = state.getBlock().getMetaFromState(state);
      if (md != 0 && md != 5) {
         if (md != 10 && md != 11) {
            return md == 12 ? -1.0F : super.getBlockHardness(state, world, pos);
         } else {
            return 100.0F;
         }
      } else {
         return 2.0F;
      }
   }

   @Override
   public float getExplosionResistance(World world, BlockPos pos, Entity exploder, Explosion explosion) {
      int md = world.getBlockState(pos).getBlock().getMetaFromState(world.getBlockState(pos));
      if (md != 0 && md != 5) {
         if (md != 10 && md != 11) {
            return md == 12 ? Float.MAX_VALUE : super.getExplosionResistance(world, pos, exploder, explosion);
         } else {
            return 50.0F;
         }
      } else {
         return 200.0F;
      }
   }

   @Override
   public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
      int md = state.getBlock().getMetaFromState(state);
      if (md != 1 && md != 2 && md != 3) {
         if (md != 4 && md != 12) {
            return md != 0 && md != 5 && md != 10 && md != 11 ? super.getLightValue(state, world, pos) : 8;
         } else {
            return 0;
         }
      } else {
         return 15;
      }
   }

   @Override
   public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
      int md = state.getBlock().getMetaFromState(state);
      if (md != 3 && md != 4 && md != 10 && md != 11 && md != 12) {
         return new AxisAlignedBB(0.3, 0.3, 0.3, 0.7, 0.7, 0.7);
      } else {
         return new AxisAlignedBB(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
      }
   }

   @Override
   public boolean isReplaceable(IBlockAccess world, BlockPos pos) {
      int md = world.getBlockState(pos).getBlock().getMetaFromState(world.getBlockState(pos));
      return md == 2 || md == 3 || md == 4 || md == 10 || md == 11;
   }

   @Override
   public boolean canBeReplacedByLeaves(IBlockState state, IBlockAccess world, BlockPos pos) {
      int md = state.getBlock().getMetaFromState(state);
      return md == 2 || md == 3 || md == 4 || super.canBeReplacedByLeaves(state, world, pos);
   }

   @Override
   public boolean isLeaves(IBlockState state, IBlockAccess world, BlockPos pos) {
      int md = state.getBlock().getMetaFromState(state);
      return md == 2 || md == 3 || super.isLeaves(state, world, pos);
   }

   @Override
   public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos,
         AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entity, boolean p_185477_7_) {
      int metadata = state.getBlock().getMetaFromState(state);
      int x = pos.getX(), y = pos.getY(), z = pos.getZ();
      if (metadata == 4 && entity instanceof EntityLivingBase && !(entity instanceof EntityPlayer)) {
         int a = 1;
         if (world.getBlockState(new BlockPos(x, y - a, z)).getBlock() != ConfigBlocks.blockCosmeticSolid) {
            ++a;
         }

         if (!world.isBlockPowered(new BlockPos(x, y - a, z))) {
            AxisAlignedBB bb = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0).offset(pos);
            if (entityBox.intersects(bb)) collidingBoxes.add(bb);
         }
      } else if (metadata == 12) {
         AxisAlignedBB bb = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0).offset(pos);
         if (entityBox.intersects(bb)) collidingBoxes.add(bb);
      }
   }

   @Override
   public boolean isPassable(IBlockAccess world, BlockPos pos) {
      int metadata = world.getBlockState(pos).getBlock().getMetaFromState(world.getBlockState(pos));
      if (metadata == 4) {
         for (int a = 1; a < 3; ++a) {
            TileEntity te = world.getTileEntity(new BlockPos(pos.getX(), pos.getY() - a, pos.getZ()));
            if (te instanceof TileWardingStone) {
               return te.getWorld().isBlockPowered(new BlockPos(pos.getX(), pos.getY() - a, pos.getZ()));
            }
         }
      }

      return true;
   }

   @Override
   public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
      int metadata = state.getBlock().getMetaFromState(state);
      return metadata != 4 && metadata != 12 ? NULL_AABB : super.getCollisionBoundingBox(state, world, pos);
   }

   @Override
   public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World world, BlockPos pos) {
      int md = state.getBlock().getMetaFromState(state);
      return md != 0 && md != 2 && md != 3 && md != 4 && md != 5 && md != 10 && md != 11 && md != 12
            ? super.getSelectedBoundingBox(state, world, pos)
            : new AxisAlignedBB(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
   }

   @Override
   public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
      return false;
   }

   @Override
   public boolean isFullCube(IBlockState state) {
      return false;
   }

   @Override
   public boolean isOpaqueCube(IBlockState state) {
      return false;
   }

   @Override
   public int damageDropped(IBlockState state) {
      return state.getBlock().getMetaFromState(state);
   }

   @Override
   public Item getItemDropped(IBlockState state, Random rand, int fortune) {
      int par1 = state.getBlock().getMetaFromState(state);
      return par1 == 1 ? ConfigItems.itemResource : Item.getItemById(0);
   }

   @Override
   public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
      int md = state.getBlock().getMetaFromState(state);
      Item item = md == 1 ? ConfigItems.itemResource : Item.getItemById(0);
      return new ItemStack(item);
   }

   @Override
   public void onBlockHarvested(World par1World, BlockPos pos, IBlockState state, EntityPlayer par6EntityPlayer) {
      int par5 = state.getBlock().getMetaFromState(state);
      if (par5 == 0 && !par1World.isRemote) {
         TileEntity te = par1World.getTileEntity(pos);
         if (te instanceof INode && ((INode) te).getAspects().size() > 0) {
            for (Aspect aspect : ((INode) te).getAspects().getAspects()) {
               for (int a = 0; a <= ((INode) te).getAspects().getAmount(aspect) / 10; ++a) {
                  if (((INode) te).getAspects().getAmount(aspect) >= 5) {
                     ItemStack ess = new ItemStack(ConfigItems.itemWispEssence);
                     new AspectList();
                     ((ItemWispEssence) ess.getItem()).setAspects(ess, (new AspectList()).add(aspect, 2));
                     Block.spawnAsEntity(par1World, pos, ess);
                  }
               }
            }
         }
      }

      super.onBlockHarvested(par1World, pos, state, par6EntityPlayer);
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void randomDisplayTick(IBlockState state, World w, BlockPos pos, Random r) {
      int md = state.getBlock().getMetaFromState(state);
      int i = pos.getX(), j = pos.getY(), k = pos.getZ();
      if (md == 1) {
         FXSparkle ef2 = new FXSparkle(w, (float) i + 0.5F, (float) j + 0.5F, (float) k + 0.5F,
               (float) i + 0.5F + (r.nextFloat() - r.nextFloat()) / 3.0F,
               (float) j + 0.5F + (r.nextFloat() - r.nextFloat()) / 3.0F,
               (float) k + 0.5F + (r.nextFloat() - r.nextFloat()) / 3.0F, 1.0F, 6, 3);
         ef2.setGravity(0.05F);
         ef2.setNoClip(true);
         ParticleEngine.instance.addEffect(w, ef2);
      } else if (md == 2 && r.nextInt(500) == 0) {
         int x1 = i + r.nextInt(3) - r.nextInt(3);
         int y1 = j + r.nextInt(3) - r.nextInt(3);
         int z1 = k + r.nextInt(3) - r.nextInt(3);
         int x2 = x1 + r.nextInt(3) - r.nextInt(3);
         int y2 = y1 + r.nextInt(3) - r.nextInt(3);
         int z2 = z1 + r.nextInt(3) - r.nextInt(3);
         Thaumcraft.proxy.wispFX3(w, x1, y1, z1, x2, y2, z2, 0.1F + r.nextFloat() * 0.1F, 7, false,
               r.nextBoolean() ? -0.033F : 0.033F);
      } else if (md == 10 || md == 11) {
         float h = r.nextFloat() * 0.33F;
         FXSpark ef = new FXSpark(w, (float) i + w.rand.nextFloat(), (float) j + 0.1515F + h / 2.0F,
               (float) k + w.rand.nextFloat(), 0.33F + h);
         if (md == 10) {
            ef.setRBGColorF(0.65F + w.rand.nextFloat() * 0.1F, 1.0F, 1.0F);
            ef.setAlphaF(0.8F);
         } else {
            ef.setRBGColorF(0.3F - w.rand.nextFloat() * 0.1F, 0.0F, 0.5F + w.rand.nextFloat() * 0.2F);
         }

         ParticleEngine.instance.addEffect(w, ef);
         if (r.nextInt(50) == 0) {
            w.playSound(null, pos,
                  new SoundEvent(new ResourceLocation("thaumcraft", "jacobs")),
                  SoundCategory.BLOCKS, 0.5F, 1.0F + (r.nextFloat() - r.nextFloat()) * 0.2F);
         }
      }
   }

   @Override
   public TileEntity createTileEntity(World world, IBlockState state) {
      int metadata = state.getBlock().getMetaFromState(state);
      if (metadata == 0) {
         return new TileNode();
      } else if (metadata == 1) {
         return new TileNitor();
      } else if (metadata == 3) {
         return new TileArcaneLampLight();
      } else if (metadata == 4) {
         return new TileWardingStoneFence();
      } else {
         return metadata == 5 ? new TileNodeEnergized() : super.createTileEntity(world, state);
      }
   }

   @Override
   public TileEntity createNewTileEntity(World var1, int md) {
      return null;
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
      list.add(new ItemStack(Item.getItemFromBlock(this), 1, 0));
   }

   @Override
   public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entity, ItemStack stack) {
      if (stack.getItemDamage() == 0 && entity instanceof EntityPlayer) {
         ThaumcraftWorldGenerator.createRandomNodeAt(world, pos.getX(), pos.getY(), pos.getZ(), world.rand, false, false, false);
      }

      super.onBlockPlacedBy(world, pos, state, entity, stack);
   }

   @Override
   public boolean isAir(IBlockState state, IBlockAccess world, BlockPos pos) {
      int md = state.getBlock().getMetaFromState(state);
      return md == 2 || md == 3 || md == 10 || md == 11;
   }

   @Override
   public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
      int md = state.getBlock().getMetaFromState(state);
      if (md == 5) {
         TileEntity te = world.getTileEntity(new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ()));
         if (!world.isBlockPowered(new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ())) && te instanceof TileNodeStabilizer) {
            te = world.getTileEntity(new BlockPos(pos.getX(), pos.getY() + 1, pos.getZ()));
            if (!(te instanceof TileNodeConverter)) {
               explodify(world, pos.getX(), pos.getY(), pos.getZ());
            }
         } else {
            explodify(world, pos.getX(), pos.getY(), pos.getZ());
         }
      }

      super.neighborChanged(state, world, pos, block, fromPos);
   }

   public static void explodify(World world, int x, int y, int z) {
      if (!world.isRemote) {
         world.setBlockToAir(new BlockPos(x, y, z));
         world.createExplosion(null, (double) x + 0.5, (double) y + 0.5, (double) z + 0.5, 3.0F, false);

         for (int a = 0; a < 50; ++a) {
            int xx = x + world.rand.nextInt(8) - world.rand.nextInt(8);
            int yy = y + world.rand.nextInt(8) - world.rand.nextInt(8);
            int zz = z + world.rand.nextInt(8) - world.rand.nextInt(8);
            if (world.isAirBlock(new BlockPos(xx, yy, zz))) {
               if (yy < y) {
                  world.setBlockState(new BlockPos(xx, yy, zz),
                        ConfigBlocks.blockFluxGoo.getStateFromMeta(8), 3);
               } else {
                  world.setBlockState(new BlockPos(xx, yy, zz),
                        ConfigBlocks.blockFluxGas.getStateFromMeta(8), 3);
               }
            }
         }
      }
   }

   @Override
   public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
      int md = state.getBlock().getMetaFromState(state);
      if (md == 10 && entity instanceof EntityLivingBase && EntityShockOrb.canEarthShockHurt(entity)) {
         entity.attackEntityFrom(DamageSource.MAGIC, (float) (1 + world.rand.nextInt(2)));
         entity.motionX *= 0.8;
         entity.motionZ *= 0.8;
         if (!world.isRemote && world.rand.nextInt(100) == 0) {
            world.setBlockToAir(pos);
         }
      } else if (md == 11 && !(entity instanceof IEldritchMob)) {
         if (world.rand.nextInt(100) == 0) {
            entity.attackEntityFrom(DamageSource.WITHER, 1.0F);
         }

         entity.motionX *= 0.66;
         entity.motionZ *= 0.66;
         if (entity instanceof EntityPlayer) {
            ((EntityPlayer) entity).addExhaustion(0.05F);
         }

         if (entity instanceof EntityLivingBase) {
            PotionEffect pe = new PotionEffect(MobEffects.WEAKNESS, 100, 1, true, false);
            ((EntityLivingBase) entity).addPotionEffect(pe);
         }
      }
   }

   @Override
   public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
      super.updateTick(world, pos, state, rand);
      int md = state.getBlock().getMetaFromState(state);
      if ((md == 10 || md == 11) && !world.isRemote) {
         world.setBlockToAir(pos);
      }
   }

   @Override
   public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) {
      return net.minecraft.util.EnumBlockRenderType.INVISIBLE;
   }
}
