package thaumcraft.common.blocks;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.lib.utils.InventoryUtils;
import thaumcraft.common.tiles.TileChestHungry;

import java.util.Random;

public class BlockChestHungry extends BlockContainer {
   private Random random = new Random();

   public BlockChestHungry() {
      super(Material.WOOD);
      this.setHardness(2.5F);
      this.setCreativeTab(Thaumcraft.tabTC);
   }

   @Override
   public boolean isOpaqueCube(IBlockState state) {
      return false;
   }

   @Override
   public boolean hasComparatorInputOverride(IBlockState state) {
      return true;
   }

   @Override
   public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos) {
      TileEntity te = world.getTileEntity(pos);
      return te instanceof IInventory ? Container.calcRedstoneFromInventory((IInventory) te) : 0;
   }

   @Override
   public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
      // Metadata-based orientation is no longer valid in 1.12.2 without a property;
      // preserve default behaviour by doing nothing here unless a facing property is added.
   }

   @Override
   public void breakBlock(World world, BlockPos pos, IBlockState state) {
      TileChestHungry te = (TileChestHungry) world.getTileEntity(pos);
      if (te != null) {
         for (int i = 0; i < te.getSizeInventory(); ++i) {
            ItemStack stack = te.getStackInSlot(i);
            if (!stack.isEmpty()) {
               float rx = this.random.nextFloat() * 0.8F + 0.1F;
               float ry = this.random.nextFloat() * 0.8F + 0.1F;
               float rz = this.random.nextFloat() * 0.8F + 0.1F;

               while (stack.getCount() > 0) {
                  int amount = this.random.nextInt(21) + 10;
                  if (amount > stack.getCount()) {
                     amount = stack.getCount();
                  }

                  ItemStack drop = stack.splitStack(amount);
                  EntityItem ei = new EntityItem(world,
                        pos.getX() + rx,
                        pos.getY() + ry,
                        pos.getZ() + rz,
                        drop);
                  float vel = 0.05F;
                  ei.motionX = this.random.nextGaussian() * vel;
                  ei.motionY = this.random.nextGaussian() * vel + 0.2;
                  ei.motionZ = this.random.nextGaussian() * vel;
                  if (drop.hasTagCompound()) {
                     ei.getItem().setTagCompound((NBTTagCompound) drop.getTagCompound().copy());
                  }
                  world.spawnEntity(ei);
               }
            }
         }
      }

      super.breakBlock(world, pos, state);
   }

   @Override
   public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
      float v = 0.0625F;
      return new AxisAlignedBB(v, 0.0, v, 1.0 - v, 1.0 - v, 1.0 - v);
   }

   @Override
   public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
      TileEntity te = world.getTileEntity(pos);
      if (te != null) {
         if (!world.isRemote) {
            if (entity instanceof EntityItem && !entity.isDead) {
               EntityItem ei = (EntityItem) entity;
               ItemStack leftovers = InventoryUtils.placeItemStackIntoInventory(ei.getItem(), (IInventory) te, 1, true);
               if (leftovers == null || leftovers.getCount() != ei.getItem().getCount()) {
                  world.playSound(null, pos,
                        new SoundEvent(new ResourceLocation("minecraft", "entity.generic.eat")),
                        SoundCategory.BLOCKS, 0.25F,
                        (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F + 1.0F);
                  world.addBlockEvent(pos, ConfigBlocks.blockChestHungry, 2, 2);
               }

               if (leftovers != null && !leftovers.isEmpty()) {
                  ei.setItem(leftovers);
               } else {
                  entity.setDead();
               }

               ((TileChestHungry) te).markDirty();
            }
         }
      }
   }

   @Override
   public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
         EntityPlayer player, EnumHand hand, EnumFacing facing,
         float hitX, float hitY, float hitZ) {
      TileEntity te = world.getTileEntity(pos);
      if (te == null) {
         return true;
      } else if (world.isRemote) {
         return true;
      } else {
         player.displayGUIChest((IInventory)te);
         return true;
      }
   }

   @Override
   public TileEntity createNewTileEntity(World world, int meta) {
      return new TileChestHungry();
   }

   @Override
   public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) {
      return net.minecraft.util.EnumBlockRenderType.INVISIBLE;
   }
}
