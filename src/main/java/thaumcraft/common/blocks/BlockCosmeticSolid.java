package thaumcraft.common.blocks;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.nodes.INode;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.items.ItemWispEssence;
import thaumcraft.common.tiles.TileNode;
import thaumcraft.common.tiles.TileWardingStone;

import java.util.List;
import java.util.Random;

//tile.blockCosmeticSolid.0.name=Obsidian Totem
//tile.blockCosmeticSolid.1.name=Obsidian Tile
//tile.blockCosmeticSolid.2.name=Paving Stone of Travel
//tile.blockCosmeticSolid.3.name=Paving Stone of Warding
//tile.blockCosmeticSolid.4.name=Thaumium Block
//tile.blockCosmeticSolid.5.name=Tallow Block
//tile.blockCosmeticSolid.6.name=Arcane Stone Block
//tile.blockCosmeticSolid.7.name=Arcane Stone Bricks
//tile.blockCosmeticSolid.8.name=Charged Obsidian Totem
//tile.blockCosmeticSolid.9.name=Golem Fetter
//tile.blockCosmeticSolid.10.name=Active Golem Fetter
//tile.blockCosmeticSolid.11.name=Ancient Stone
//tile.blockCosmeticSolid.12.name=Ancient Rock
//tile.blockCosmeticSolid.13.name=Ancient Stone
//tile.blockCosmeticSolid.14.name=Crusted Stone
//tile.blockCosmeticSolid.15.name=Ancient Stone Pedestal
public class BlockCosmeticSolid extends Block {
   public static final net.minecraft.block.properties.PropertyEnum<SolidVariant> VARIANT =
         net.minecraft.block.properties.PropertyEnum.create("variant", SolidVariant.class);

   public enum SolidVariant implements net.minecraft.util.IStringSerializable {
      OBSIDIANTOTEM(0, "obsidiantotem"), OBSIDIANTILE(1, "obsidiantile"), PAVINGTRAVEL(2, "pavingtravel"), PAVINGWARDING(3, "pavingwarding"), THAUMIUMBLOCK(4, "thaumiumblock"), TALLOWBLOCK(5, "tallowblock"), ARCANESTONE(6, "arcanestone"), ARCANESTONEBRICK(7, "arcanestonebrick"), OBSIDIANTOTEMCHARGED(8, "obsidiantotemcharged"), GOLEMFETTER(9, "golemfetter"), GOLEMFETTERACTIVE(10, "golemfetteractive"), ANCIENTSTONE(11, "ancientstone"), ANCIENTROCK(12, "ancientrock"), FOCALMANIPULATOR(13, "focalmanipulator"), CRUSTEDSTONE(14, "crustedstone"), PEDESTALSTONE(15, "pedestalstone");
      private final int meta;
      private final String name;
      SolidVariant(int meta, String name) { this.meta = meta; this.name = name; }
      public int getMeta() { return meta; }
      @Override public String getName() { return name; }
      public static SolidVariant byMeta(int m) {
         for (SolidVariant v : values()) if (v.meta == m) return v;
         return OBSIDIANTOTEM;
      }
   }

   @Override
   protected net.minecraft.block.state.BlockStateContainer createBlockState() {
      return new net.minecraft.block.state.BlockStateContainer(this, VARIANT);
   }

   @Override
   public net.minecraft.block.state.IBlockState getStateFromMeta(int meta) {
      return this.getDefaultState().withProperty(VARIANT, SolidVariant.byMeta(meta));
   }

   @Override
   public int getMetaFromState(net.minecraft.block.state.IBlockState state) {
      return state.getValue(VARIANT).getMeta();
   }


   public BlockCosmeticSolid() {
      super(Material.ROCK);
      this.setResistance(10.0F);
      this.setHardness(2.0F);
      this.setSoundType(SoundType.STONE);
      this.setCreativeTab(Thaumcraft.tabTC);
      this.setTickRandomly(true);
   }

   // registerBlockIcons removed -- use ModelLoader/JSON models in 1.12.2

   @SideOnly(Side.CLIENT)
   @Override
   public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
      list.add(new ItemStack(this, 1, 0));
      list.add(new ItemStack(this, 1, 1));
      list.add(new ItemStack(this, 1, 2));
      list.add(new ItemStack(this, 1, 3));
      list.add(new ItemStack(this, 1, 4));
      list.add(new ItemStack(this, 1, 5));
      list.add(new ItemStack(this, 1, 6));
      list.add(new ItemStack(this, 1, 7));
      list.add(new ItemStack(this, 1, 8));
      list.add(new ItemStack(this, 1, 9));
      list.add(new ItemStack(this, 1, 11));
      list.add(new ItemStack(this, 1, 12));
      list.add(new ItemStack(this, 1, 14));
      list.add(new ItemStack(this, 1, 15));
   }

   @Override
   public float getBlockHardness(IBlockState state, World world, BlockPos pos) {
      if (world.getBlockState(pos).getBlock() != this) {
         return 4.0F;
      } else {
         int md = state.getBlock().getMetaFromState(state);
         if (md > 1 && md != 8) {
            return md != 4 && md != 6 && md != 7 ? super.getBlockHardness(state, world, pos) : 4.0F;
         } else {
            return 30.0F;
         }
      }
   }

   @Override
   public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
      if (world.getBlockState(pos).getBlock() != this) {
         return 0;
      } else {
         int md = state.getBlock().getMetaFromState(state);
         if (md == 2) {
            return 9;
         } else {
            return md == 14 ? 4 : super.getLightValue(state, world, pos);
         }
      }
   }

   @Override
   public float getExplosionResistance(World world, BlockPos pos, Entity exploder, net.minecraft.world.Explosion explosion) {
      if (world.getBlockState(pos).getBlock() != this) {
         return 20.0F;
      } else {
         int md = world.getBlockState(pos).getBlock().getMetaFromState(world.getBlockState(pos));
         if (md > 1 && md != 8) {
            return md != 4 && md != 6 && md != 7 ? super.getExplosionResistance(world, pos, exploder, explosion) : 20.0F;
         } else {
            return 999.0F;
         }
      }
   }

   @Override
   public int quantityDropped(Random par1Random) {
      return 1;
   }

   @Override
   public int damageDropped(IBlockState state) {
      int par1 = state.getBlock().getMetaFromState(state);
      return par1 == 8 ? 1 : (par1 == 10 ? 9 : par1);
   }

   @Override
   public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity e) {
      if (world.getBlockState(pos).getBlock() == this) {
         int md = state.getBlock().getMetaFromState(state);
         if (md == 2 && e instanceof EntityLivingBase) {
            if (world.isRemote) {
               Thaumcraft.proxy.blockSparkle(world, pos.getX(), pos.getY(), pos.getZ(), 32768, 5);
            }
            ((EntityLivingBase)e).addPotionEffect(new PotionEffect(MobEffects.SPEED, 40, 1));
            ((EntityLivingBase)e).addPotionEffect(new PotionEffect(MobEffects.JUMP_BOOST, 40, 0));
         }
      }
   }

   @Override
   public boolean hasTileEntity(IBlockState state) {
      int metadata = state.getBlock().getMetaFromState(state);
      if (metadata == 3) {
         return true;
      } else {
         return metadata == 8 || super.hasTileEntity(state);
      }
   }

   @Override
   public TileEntity createTileEntity(World world, IBlockState state) {
      int metadata = state.getBlock().getMetaFromState(state);
      if (metadata == 3) {
         return new TileWardingStone();
      } else {
         return metadata == 8 ? new TileNode() : super.createTileEntity(world, state);
      }
   }

   @Override
   public boolean addDestroyEffects(World world, BlockPos pos, ParticleManager effectRenderer) {
      int meta = world.getBlockState(pos).getBlock().getMetaFromState(world.getBlockState(pos));
      if (meta == 8) {
         Thaumcraft.proxy.burst(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1.0F);
         world.playSound(null, pos,
               new SoundEvent(new ResourceLocation("thaumcraft", "craftfail")),
               SoundCategory.BLOCKS, 1.0F, 1.0F);
      }
      return super.addDestroyEffects(world, pos, effectRenderer);
   }

   @Override
   public void onBlockHarvested(World par1World, BlockPos pos, IBlockState state, EntityPlayer par6EntityPlayer) {
      if (par1World.getBlockState(pos).getBlock() == this) {
         int par5 = state.getBlock().getMetaFromState(state);
         if (par5 == 8 && !par1World.isRemote) {
            TileEntity te = par1World.getTileEntity(pos);
            if (te instanceof INode && ((INode) te).getAspects().size() > 0) {
               for (Aspect aspect : ((INode)te).getAspects().getAspects()) {
                  for (int a = 0; a <= ((INode)te).getAspects().getAmount(aspect) / 10; ++a) {
                     if (((INode)te).getAspects().getAmount(aspect) >= 5) {
                        ItemStack ess = new ItemStack(ConfigItems.itemWispEssence);
                        new AspectList();
                        ((ItemWispEssence)ess.getItem()).setAspects(ess, (new AspectList()).add(aspect, 2));
                        Block.spawnAsEntity(par1World, pos, ess);
                     }
                  }
               }
            }
         }
         super.onBlockHarvested(par1World, pos, state, par6EntityPlayer);
      }
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random random) {
      if (world.getBlockState(pos).getBlock() == this) {
         int md = state.getBlock().getMetaFromState(state);
         int x = pos.getX(), y = pos.getY(), z = pos.getZ();
         if (md == 3) {
            if (world.isBlockPowered(pos)) {
               for (int a = 0; a < Thaumcraft.proxy.particleCount(2); ++a) {
                  Thaumcraft.proxy.blockRunes(world, x, (float)y + 0.7F, z, 0.2F + world.rand.nextFloat() * 0.4F, world.rand.nextFloat() * 0.3F, 0.8F + world.rand.nextFloat() * 0.2F, 20, -0.02F);
               }
            } else if (world.getBlockState(pos.up()).getBlock() != ConfigBlocks.blockAiry && !world.getBlockState(pos.up()).getBlock().isPassable(world, pos.up())
                  || world.getBlockState(pos.up(2)).getBlock() != ConfigBlocks.blockAiry && !world.getBlockState(pos.up()).getBlock().isPassable(world, pos.up())) {
               for (int a = 0; a < Thaumcraft.proxy.particleCount(3); ++a) {
                  Thaumcraft.proxy.blockRunes(world, x, (float)y + 0.7F, z, 0.9F + world.rand.nextFloat() * 0.1F, world.rand.nextFloat() * 0.3F, world.rand.nextFloat() * 0.3F, 24, -0.02F);
               }
            } else {
               List<Entity> list = (List<Entity>)world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1).expand(1.0, 1.0, 1.0));
               if (!list.isEmpty()) {
                  for (Entity entity : list) {
                     if (entity instanceof EntityLivingBase && !(entity instanceof EntityPlayer)) {
                        Thaumcraft.proxy.blockRunes(world, x, (float)y + 0.6F + world.rand.nextFloat() * Math.max(0.8F, entity.getEyeHeight()), z, 0.6F + world.rand.nextFloat() * 0.4F, 0.0F, 0.3F + world.rand.nextFloat() * 0.7F, 20, 0.0F);
                        break;
                     }
                  }
               }
            }
         }
      }
   }

   @Override
   public boolean isBeaconBase(IBlockAccess world, BlockPos pos, BlockPos beacon) {
      return world.getBlockState(pos).getBlock() == this;
   }

   @Override
   public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
      if (world.getBlockState(pos).getBlock() == this) {
         int md = state.getBlock().getMetaFromState(state);
         if (md == 9 && world.isBlockPowered(pos)) {
            world.setBlockState(pos, this.getStateFromMeta(10), 3);
         } else if (md == 10 && !world.isBlockPowered(pos)) {
            world.setBlockState(pos, this.getStateFromMeta(9), 3);
         }
      }
      super.neighborChanged(state, world, pos, block, fromPos);
   }

   @Override
   public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, EntityLiving.SpawnPlacementType type) {
      int md = state.getBlock().getMetaFromState(state);
      return md != 2 && md != 3 && md != 13 && super.canCreatureSpawn(state, world, pos, type);
   }
}
