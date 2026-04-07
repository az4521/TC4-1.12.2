package thaumcraft.common.blocks;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraft.util.EnumFacing;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.tiles.TileWarded;

import java.util.Random;

public class BlockWarded extends BlockContainer {
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


   public BlockWarded() {
      super(Material.ROCK);
      // setStepSound removed in 1.12.2 — sound is now data-driven
      this.disableStats();
      this.setResistance(999.0F);
      this.setBlockUnbreakable();
   }

   // registerBlockIcons removed — textures handled by JSON models in 1.12.2
   // getIcon removed — textures handled by JSON models in 1.12.2

   @Override
   public boolean isOpaqueCube(IBlockState state) {
      return false;
   }

   @Override
   public boolean isFullCube(IBlockState state) {
      return false;
   }

   @Override
   public boolean addHitEffects(IBlockState state, World world, RayTraceResult target, ParticleManager effectRenderer) {
      BlockPos pos = target.getBlockPos();
      float f  = (float)target.hitVec.x - (float)pos.getX();
      float f1 = (float)target.hitVec.y - (float)pos.getY();
      float f2 = (float)target.hitVec.z - (float)pos.getZ();
      Thaumcraft.proxy.blockWard(world, pos.getX(), pos.getY(), pos.getZ(), target.sideHit, f, f1, f2);
      return true;
   }

   // getRenderType() removed — defaults to MODEL in 1.12.2

   public Block getBlock(World world, int x, int y, int z) {
      if (this.sc > 5) {
         this.sc = 0;
         return Blocks.STONE;
      } else {
         ++this.sc;
         TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
         if (tile instanceof TileWarded) {
            this.sc = 0;
            return ((TileWarded)tile).block;
         } else {
            return Blocks.STONE;
         }
      }
   }

   public Block getBlock(IBlockAccess world, int x, int y, int z) {
      if (this.sc > 5) {
         this.sc = 0;
         return Blocks.STONE;
      } else {
         ++this.sc;
         TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
         if (tile instanceof TileWarded) {
            this.sc = 0;
            return ((TileWarded)tile).block;
         } else {
            return Blocks.STONE;
         }
      }
   }

   int sc = 0;

   @Override
   public Item getItemDropped(IBlockState state, Random rand, int fortune) {
      return Item.getItemById(0);
   }

   @Override
   public int damageDropped(IBlockState state) {
      return state.getBlock().getMetaFromState(state);
   }

   // getMobilityFlag() removed in 1.12.2 — use getPushReaction(IBlockState)
   @Override
   public net.minecraft.block.material.EnumPushReaction getPushReaction(IBlockState state) {
      return net.minecraft.block.material.EnumPushReaction.BLOCK;
   }

   @Override
   public TileEntity createNewTileEntity(World var1, int md) {
      return new TileWarded();
   }

   // canBeReplacedByLeaves(IBlockAccess,int,int,int) not available in 1.12.2 with those params;
   // override the IBlockState variant:
   @Override
   public boolean canBeReplacedByLeaves(IBlockState state, IBlockAccess world, BlockPos pos) {
      return false;
   }

   @Override
   public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos, Entity entity) {
      return false;
   }

   @Override
   public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos,
         net.minecraft.entity.EntityLiving.SpawnPlacementType type) {
      return false;
   }

   @Override
   public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
      TileEntity tile = world.getTileEntity(pos);
      return tile instanceof TileWarded ? ((TileWarded)tile).light : 0;
   }

   @Override
   public boolean isLadder(IBlockState state, IBlockAccess world, BlockPos pos, EntityLivingBase entity) {
      return this.getBlock(world, pos.getX(), pos.getY(), pos.getZ())
            .isLadder(world.getBlockState(pos), world, pos, entity);
   }

   @Override
   public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
      return this.getBlock(world, pos.getX(), pos.getY(), pos.getZ())
            .isSideSolid(world.getBlockState(pos), world, pos, side);
   }

   @Override
   public boolean canSustainLeaves(IBlockState state, IBlockAccess world, BlockPos pos) {
      return this.getBlock(world, pos.getX(), pos.getY(), pos.getZ())
            .canSustainLeaves(world.getBlockState(pos), world, pos);
   }

   @Override
   public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos,
         EntityPlayer player) {
      return this.getBlock(world, pos.getX(), pos.getY(), pos.getZ())
            .getPickBlock(world.getBlockState(pos), target, world, pos, player);
   }

   @Override
   public boolean isFoliage(IBlockAccess world, BlockPos pos) {
      return this.getBlock(world, pos.getX(), pos.getY(), pos.getZ())
            .isFoliage(world, pos);
   }

   @Override
   public boolean canSustainPlant(IBlockState state, IBlockAccess world, BlockPos pos,
         EnumFacing direction, IPlantable plant) {
      return this.getBlock(world, pos.getX(), pos.getY(), pos.getZ())
            .canSustainPlant(world.getBlockState(pos), world, pos, direction, plant);
   }

   @Override
   public boolean isFertile(World world, BlockPos pos) {
      return this.getBlock(world, pos.getX(), pos.getY(), pos.getZ())
            .isFertile(world, pos);
   }

   @Override
   public int getLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos) {
      return this.getBlock(world, pos.getX(), pos.getY(), pos.getZ())
            .getLightOpacity(world.getBlockState(pos), world, pos);
   }

   @Override
   public boolean isBeaconBase(IBlockAccess world, BlockPos pos, BlockPos beacon) {
      return this.getBlock(world, pos.getX(), pos.getY(), pos.getZ())
            .isBeaconBase(world, pos, beacon);
   }

   @Override
   public float getEnchantPowerBonus(World world, BlockPos pos) {
      return this.getBlock(world, pos.getX(), pos.getY(), pos.getZ())
            .getEnchantPowerBonus(world, pos);
   }

   @Override
   public boolean canHarvestBlock(IBlockAccess world, BlockPos pos, EntityPlayer player) {
      return true;
   }

   @Override
   public void onEntityWalk(World world, BlockPos pos, Entity entity) {
      Block block = this.getBlock(world, pos.getX(), pos.getY(), pos.getZ());
      block.onEntityWalk(world, pos, entity);
   }

   @Override
   public void onBlockExploded(World world, BlockPos pos, Explosion explosion) {
      // warded blocks are immune to explosions
   }

   @Override
   public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) {
      return net.minecraft.util.EnumBlockRenderType.MODEL;
   }
}
