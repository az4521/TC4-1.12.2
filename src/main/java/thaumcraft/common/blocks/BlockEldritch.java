package thaumcraft.common.blocks;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.Explosion;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;

import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import thaumcraft.client.fx.ParticleEngine;
import thaumcraft.client.fx.particles.FXSpark;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.items.ItemEldritchObject;
import thaumcraft.common.tiles.*;

import java.util.ArrayList;
import java.util.Random;

public class BlockEldritch extends BlockContainer {
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

   private Random rand = new Random();

   public BlockEldritch() {
      super(Material.ROCK);
      this.setResistance(20000.0F);
      this.setHardness(50.0F);
      // setStepSound removed in 1.12.2 — sound is now data-driven
      this.setTickRandomly(true);
      // setBlockBounds removed in 1.12.2 — full cube is the default
      this.setLightOpacity(0);
      this.setCreativeTab(Thaumcraft.tabTC);
   }

   // registerBlockIcons removed — textures are handled by JSON models in 1.12.2
   // getIcon removed — textures are handled by JSON models in 1.12.2

   @Override
   @SideOnly(Side.CLIENT)
   public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
      list.add(new ItemStack(this, 1, 4));
   }

   @Override
   public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
      int meta = this.getMetaFromState(state);
      if (meta == 4 || meta == 5 || meta == 7) return 12;
      if (meta == 6 || meta == 8) return 5;
      if (meta == 9) return 4;
      if (meta == 10) return 0;
      return 8;
   }

   // canCreatureSpawn — use canCreatureSpawn(IBlockState, IBlockAccess, BlockPos, EnumCreatureType) in 1.12.2 if needed

   // setBlockBoundsBasedOnState removed — use getBoundingBox(IBlockState, IBlockAccess, BlockPos) in 1.12.2
   // addCollisionBoxesToList — only override if non-standard bounds; full cube is default

   @Override
   public boolean hasTileEntity(IBlockState state) {
      int metadata = this.getMetaFromState(state);
      return metadata == 0 || metadata == 1 || metadata == 3 || metadata == 8 || metadata == 9 || metadata == 10;
   }

   @Override
   public TileEntity createTileEntity(World world, IBlockState state) {
      int metadata = this.getMetaFromState(state);
      if (metadata == 0) return new TileEldritchAltar();
      if (metadata == 1) return new TileEldritchObelisk();
      if (metadata == 3) return new TileEldritchCap();
      if (metadata == 8) return new TileEldritchLock();
      if (metadata == 9) return new TileEldritchCrabSpawner();
      if (metadata == 10) return new TileEldritchTrap();
      return null;
   }

   @Override
   public TileEntity createNewTileEntity(World worldIn, int meta) {
      return null;
   }

   // getRenderType() removed — defaults to MODEL in 1.12.2
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
   public Item getItemDropped(IBlockState state, Random rand, int fortune) {
      int md = this.getMetaFromState(state);
      return md == 4 ? Item.getItemFromBlock(this) : (md == 5 ? ConfigItems.itemResource : Item.getItemById(0));
   }

   @Override
   public int damageDropped(IBlockState state) {
      int metadata = this.getMetaFromState(state);
      return metadata == 2 ? 1 : metadata;
   }

   public int getExpDrop(IBlockAccess world, IBlockState state, int fortune) {
      int metadata = this.getMetaFromState(state);
      if (metadata == 5 || metadata == 10) return this.rand.nextInt(4) + 1;
      if (metadata == 9) return this.rand.nextInt(5) + 6;
      return 0;
   }

   @Override
   public java.util.List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
      ArrayList<ItemStack> ret = new ArrayList<>();
      int md = this.getMetaFromState(state);
      if (md == 5) {
         ret.add(new ItemStack(ConfigItems.itemResource, 1, 9));
         return ret;
      } else {
         return super.getDrops(world, pos, state, fortune);
      }
   }

   @Override
   public void breakBlock(World world, BlockPos pos, IBlockState state) {
      int meta = this.getMetaFromState(state);
      int x = pos.getX(), y = pos.getY(), z = pos.getZ();
      if (!world.isRemote && meta < 4) {
         for (int xx = x - 3; xx <= x + 3; ++xx) {
            for (int yy = y - 2; yy <= y + 2; ++yy) {
               for (int zz = z - 3; zz <= z + 3; ++zz) {
                  BlockPos checkPos = new BlockPos(xx, yy, zz);
                  IBlockState checkState = world.getBlockState(checkPos);
                  if (checkState.getBlock() == this && this.getMetaFromState(checkState) < 4) {
                     world.setBlockToAir(checkPos);
                  }
               }
            }
         }
         world.createExplosion(null, x + 0.5, y + 0.5, z + 0.5, 1.0F, false);
      }
      super.breakBlock(world, pos, state);
   }

   @Override
   public float getBlockHardness(IBlockState state, World world, BlockPos pos) {
      int meta = this.getMetaFromState(state);
      if (meta == 4 || meta == 5) return 2.0F;
      if (meta == 6) return 4.0F;
      if (meta == 7 || meta == 8) return -1.0F;
      if (meta == 9 || meta == 10) return 15.0F;
      return super.getBlockHardness(state, world, pos);
   }

   @Override
   public float getExplosionResistance(World world, BlockPos pos, Entity exploder, Explosion explosion) {
      IBlockState state = world.getBlockState(pos);
      int meta = this.getMetaFromState(state);
      if (meta == 4 || meta == 5 || meta == 9 || meta == 10) return 30.0F;
      if (meta == 6) return 100.0F;
      if (meta == 7 || meta == 8) return Float.MAX_VALUE;
      return super.getExplosionResistance(world, pos, exploder, explosion);
   }

   @Override
   public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
                                   EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
      int metadata = this.getMetaFromState(state);
      ItemStack heldItem = player.getHeldItem(hand);

      if (metadata == 0 && !world.isRemote && !player.isSneaking()
            && !heldItem.isEmpty() && heldItem.getItem() instanceof ItemEldritchObject
            && heldItem.getItemDamage() == 0) {
         TileEntity te = world.getTileEntity(pos);
         if (te instanceof TileEldritchAltar) {
            TileEldritchAltar tile = (TileEldritchAltar) te;
            if (tile.getEyes() < 4) {
               if (tile.getEyes() >= 2) {
                  tile.setSpawner(true);
                  tile.setSpawnType((byte) 1);
               }
               tile.setEyes((byte) (tile.getEyes() + 1));
               tile.checkForMaze();
               heldItem.shrink(1);
               tile.markDirty();
               { net.minecraft.block.state.IBlockState _bs = world.getBlockState(pos); world.notifyBlockUpdate(pos, _bs, _bs, 3); }
               world.playSound(null, pos,
                     thaumcraft.common.lib.SoundsTC.get("thaumcraft:crystal"),
                     net.minecraft.util.SoundCategory.BLOCKS, 0.2F, 1.0F);
            }
         }
      }

      ItemStack currentItem = player.inventory.getCurrentItem();
      if (metadata == 8 && !currentItem.isEmpty()
            && currentItem.getItem() instanceof ItemEldritchObject
            && currentItem.getItemDamage() == 2) {
         TileEntity te = world.getTileEntity(pos);
         if (te instanceof TileEldritchLock && ((TileEldritchLock) te).count < 0) {
            ((TileEldritchLock) te).count = 0;
            { net.minecraft.block.state.IBlockState _bs = world.getBlockState(pos); world.notifyBlockUpdate(pos, _bs, _bs, 3); }
            te.markDirty();
            player.getHeldItem(hand).shrink(1);
            world.playSound(null, pos,
                  thaumcraft.common.lib.SoundsTC.get("thaumcraft:runicShieldCharge"),
                  net.minecraft.util.SoundCategory.BLOCKS, 1.0F, 1.0F);
         }
      }

      return super.onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ);
   }

   @Override
   @SideOnly(Side.CLIENT)
   public void randomDisplayTick(IBlockState state, World w, BlockPos pos, Random r) {
      int md = this.getMetaFromState(state);
      int i = pos.getX(), j = pos.getY(), k = pos.getZ();
      if (md == 8) {
         TileEntity te = w.getTileEntity(pos);
         if (!(te instanceof TileEldritchLock) || ((TileEldritchLock) te).count < 0) {
            return;
         }
         FXSpark ef = new FXSpark(w,
               i + w.rand.nextFloat(), j + w.rand.nextFloat(), k + w.rand.nextFloat(), 0.5F);
         ef.setRBGColorF(0.65F + w.rand.nextFloat() * 0.1F, 1.0F, 1.0F);
         ef.setAlphaF(0.8F);
         ParticleEngine.instance.addEffect(w, ef);
      } else if (md == 10) {
         int x = i + r.nextInt(2) - r.nextInt(2);
         int y = j + r.nextInt(2) - r.nextInt(2);
         int z = k + r.nextInt(2) - r.nextInt(2);
         if (w.isAirBlock(new BlockPos(x, y, z))) {
            Thaumcraft.proxy.blockRunes(w,
                  x + r.nextFloat(), y + r.nextFloat(), z + r.nextFloat(),
                  0.5F + r.nextFloat() * 0.5F, r.nextFloat() * 0.3F,
                  0.9F + r.nextFloat() * 0.1F, 16 + r.nextInt(4), 0.0F);
         }
      }
   }

   @Override
   public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) {
      int meta = state.getValue(META);
      // Solid blocks (4-6,9,10) use block model; TESR blocks (0-3,7,8) use INVISIBLE
      if (meta >= 4 && meta <= 6 || meta == 9 || meta == 10) return net.minecraft.util.EnumBlockRenderType.MODEL;
      return net.minecraft.util.EnumBlockRenderType.INVISIBLE;
   }
}
