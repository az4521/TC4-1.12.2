package thaumcraft.common.blocks;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.client.resources.I18n;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.items.baubles.ItemAmuletVis;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumcraft.common.lib.utils.InventoryUtils;
import thaumcraft.common.tiles.*;

import java.util.List;
import java.util.Random;
import net.minecraft.util.math.BlockPos;

public class BlockStoneDevice extends BlockContainer {
   public static final net.minecraft.block.properties.PropertyInteger META =
         net.minecraft.block.properties.PropertyInteger.create("meta", 0, 15);
   public static final net.minecraft.block.properties.PropertyDirection FACING =
         net.minecraft.block.properties.PropertyDirection.create("facing", net.minecraft.util.EnumFacing.Plane.HORIZONTAL);

   private static boolean isAlchemyFurnaceMeta(int meta) {
      return meta == 0 || meta == 6 || meta == 7 || meta == 15;
   }

   @Override
   protected net.minecraft.block.state.BlockStateContainer createBlockState() {
      return new net.minecraft.block.state.BlockStateContainer(this, META, FACING);
   }

   @Override
   public net.minecraft.block.state.IBlockState getStateFromMeta(int meta) {
      if (isAlchemyFurnaceMeta(meta)) {
         return this.getDefaultState()
               .withProperty(META, 0)
               .withProperty(FACING, getFacingForAlchemyFurnaceMeta(meta));
      }
      return this.getDefaultState().withProperty(META, meta).withProperty(FACING, net.minecraft.util.EnumFacing.SOUTH);
   }

   @Override
   public int getMetaFromState(net.minecraft.block.state.IBlockState state) {
      int meta = state.getValue(META);
      if (meta == 0) {
         return getAlchemyFurnaceMetaForFacing(state.getValue(FACING));
      }
      return meta;
   }

   private static net.minecraft.util.EnumFacing getFacingForAlchemyFurnaceMeta(int meta) {
      switch (meta) {
         case 6:
            return net.minecraft.util.EnumFacing.WEST;
         case 7:
            return net.minecraft.util.EnumFacing.NORTH;
         case 15:
            return net.minecraft.util.EnumFacing.EAST;
         case 0:
         default:
            return net.minecraft.util.EnumFacing.SOUTH;
      }
   }

   public static int getAlchemyFurnaceMetaForFacing(net.minecraft.util.EnumFacing facing) {
      switch (facing) {
         case WEST:
            return 6;
         case NORTH:
            return 7;
         case EAST:
            return 15;
         case SOUTH:
         default:
            return 0;
      }
   }


   public BlockStoneDevice() {
      super(Material.ROCK);
      this.setHardness(3.0F);
      this.setResistance(25.0F);
      this.setCreativeTab(Thaumcraft.tabTC);
      this.setDefaultState(this.blockState.getBaseState().withProperty(META, 0).withProperty(FACING, net.minecraft.util.EnumFacing.SOUTH));
   }

   @Override
   public boolean isOpaqueCube(IBlockState state) {
      return false;
   }

   @Override
   public boolean isFullCube(IBlockState state) {
      return false;
   }

   @Override
   @SideOnly(Side.CLIENT)
   public void getSubBlocks(CreativeTabs par2CreativeTabs, NonNullList<ItemStack> par3List) {
      par3List.add(new ItemStack(this, 1, 0));
      par3List.add(new ItemStack(this, 1, 1));
      par3List.add(new ItemStack(this, 1, 2));
      par3List.add(new ItemStack(this, 1, 5));
      par3List.add(new ItemStack(this, 1, 8));
      par3List.add(new ItemStack(this, 1, 9));
      par3List.add(new ItemStack(this, 1, 10));
      par3List.add(new ItemStack(this, 1, 11));
      par3List.add(new ItemStack(this, 1, 12));
      par3List.add(new ItemStack(this, 1, 13));
      par3List.add(new ItemStack(this, 1, 14));
   }

   @Override
   @SideOnly(Side.CLIENT)
   public void randomDisplayTick(IBlockState state, World w, BlockPos pos, Random r) {
      TileEntity te = w.getTileEntity(pos);
      if (te instanceof TileAlchemyFurnace && ((TileAlchemyFurnace) te).isBurning()) {
         float f = (float) pos.getX() + 0.5F;
         float f1 = (float) pos.getY() + 0.2F + r.nextFloat() * 5.0F / 16.0F;
         float f2 = (float) pos.getZ() + 0.5F;
         float f3 = 0.52F;
         float f4 = r.nextFloat() * 0.5F - 0.25F;
         w.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, f - f3, f1, f2 + f4, 0.0F, 0.0F, 0.0F);
         w.spawnParticle(EnumParticleTypes.FLAME, f - f3, f1, f2 + f4, 0.0F, 0.0F, 0.0F);
         w.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, f + f3, f1, f2 + f4, 0.0F, 0.0F, 0.0F);
         w.spawnParticle(EnumParticleTypes.FLAME, f + f3, f1, f2 + f4, 0.0F, 0.0F, 0.0F);
         w.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, f + f4, f1, f2 - f3, 0.0F, 0.0F, 0.0F);
         w.spawnParticle(EnumParticleTypes.FLAME, f + f4, f1, f2 - f3, 0.0F, 0.0F, 0.0F);
         w.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, f + f4, f1, f2 + f3, 0.0F, 0.0F, 0.0F);
         w.spawnParticle(EnumParticleTypes.FLAME, f + f4, f1, f2 + f3, 0.0F, 0.0F, 0.0F);
      }
   }

   @Override
   public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
      int meta = state.getBlock().getMetaFromState(state);
      if (isAlchemyFurnaceMeta(meta)) {
         TileEntity te = world.getTileEntity(pos);
         if (te instanceof TileAlchemyFurnace && ((TileAlchemyFurnace) te).isBurning()) {
            return 12;
         }
      } else if (meta == 2) {
         return 10;
      }
      return super.getLightValue(state, world, pos);
   }

   @Override
   public int damageDropped(IBlockState state) {
      int metadata = state.getBlock().getMetaFromState(state);
      if (isAlchemyFurnaceMeta(metadata)) {
         return 0;
      }
      return metadata == 3 ? 7 : (metadata == 4 ? 6 : metadata);
   }

   @Override
   public Item getItemDropped(IBlockState state, Random par2Random, int par3) {
      int metadata = state.getBlock().getMetaFromState(state);
      return metadata != 3 && metadata != 4 ? super.getItemDropped(state, par2Random, par3) : Item.getItemFromBlock(ConfigBlocks.blockCosmeticSolid);
   }

   @Override
   public TileEntity createTileEntity(World world, IBlockState state) {
      int metadata = state.getBlock().getMetaFromState(state);
      if (isAlchemyFurnaceMeta(metadata)) {
         return new TileAlchemyFurnace();
      } else if (metadata == 1) {
         return new TilePedestal();
      } else if (metadata == 2) {
         return new TileInfusionMatrix();
      } else if (metadata == 3) {
         return new TileInfusionPillar();
      } else if (metadata == 5) {
         return new TileWandPedestal();
      } else if (metadata == 9 || metadata == 10) {
         return new TileNodeStabilizer();
      } else if (metadata == 11) {
         return new TileNodeConverter();
      } else if (metadata == 12) {
         return new TileSpa();
      } else if (metadata == 13) {
         return new TileFocalManipulator();
      } else {
         return metadata == 14 ? new TileFluxScrubber() : null;
      }
   }

   @Override
   public boolean hasComparatorInputOverride(IBlockState state) {
      return true;
   }

   @Override
   public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos) {
      TileEntity te = world.getTileEntity(pos);
      if (!(te instanceof TilePedestal) && !(te instanceof TileAlchemyFurnace)) {
         if (te instanceof TileWandPedestal && ((TileWandPedestal) te).getAspects() != null && !((TileWandPedestal) te).getStackInSlot(0).isEmpty() && ((TileWandPedestal) te).getStackInSlot(0).getItem() instanceof ItemWandCasting) {
            ItemWandCasting wand = (ItemWandCasting) ((TileWandPedestal) te).getStackInSlot(0).getItem();
            float r = (float) wand.getAllVis(((TileWandPedestal) te).getStackInSlot(0)).visSize() / ((float) wand.getMaxVis(((TileWandPedestal) te).getStackInSlot(0)) * 6.0F);
            return MathHelper.floor(r * 14.0F) + 1;
         } else {
            return 0;
         }
      } else {
         return Container.calcRedstoneFromInventory((IInventory) te);
      }
   }

   @Override
   public TileEntity createNewTileEntity(World var1, int md) {
      return null;
   }

   @Override
   public void breakBlock(World world, BlockPos pos, IBlockState state) {
      InventoryUtils.dropItems(world, pos.getX(), pos.getY(), pos.getZ());
      TileEntity tileEntity = world.getTileEntity(pos);
      if (tileEntity instanceof TileInfusionMatrix && ((TileInfusionMatrix) tileEntity).crafting) {
         world.createExplosion(null, (double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5, 2.0F, true);
      }
      super.breakBlock(world, pos, state);
   }

   @Override
   public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
      TileEntity te = world.getTileEntity(pos);
      int metadata = state.getBlock().getMetaFromState(state);
      if (te instanceof TileAlchemyFurnace) {
         ((TileAlchemyFurnace) te).getBellows();
      } else if (te instanceof TileNodeConverter) {
         ((TileNodeConverter) te).checkStatus();
      } else {
         if (metadata == 1) {
            if (!world.isAirBlock(pos.up())) {
               InventoryUtils.dropItems(world, pos.getX(), pos.getY(), pos.getZ());
            }
         } else if (metadata == 5) {
            BlockPos above = pos.up();
            IBlockState aboveState = world.getBlockState(above);
            if (!world.isAirBlock(above) && (aboveState.getBlock() != this || aboveState.getBlock().getMetaFromState(aboveState) != 8)) {
               InventoryUtils.dropItems(world, pos.getX(), pos.getY(), pos.getZ());
            }
         } else if (metadata == 3) {
            BlockPos above = pos.up();
            IBlockState aboveState = world.getBlockState(above);
            if (aboveState.getBlock() != this || aboveState.getBlock().getMetaFromState(aboveState) != 4) {
               Block.spawnAsEntity(world, pos, new ItemStack(this, 1, metadata));
               world.setBlockToAir(pos);
            }
         } else if (metadata == 4) {
            BlockPos below = pos.down();
            IBlockState belowState = world.getBlockState(below);
            if (belowState.getBlock() != this || belowState.getBlock().getMetaFromState(belowState) != 3) {
               Block.spawnAsEntity(world, pos, new ItemStack(this, 1, metadata));
               world.setBlockToAir(pos);
            }
         }
      }
   }

   @Override
   public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
      if (world.isRemote) {
         return true;
      }
      int metadata = state.getBlock().getMetaFromState(state);
      TileEntity tileEntity = world.getTileEntity(pos);
      if (isAlchemyFurnaceMeta(metadata) && tileEntity instanceof TileAlchemyFurnace && !player.isSneaking()) {
         player.openGui(Thaumcraft.instance, 9, world, pos.getX(), pos.getY(), pos.getZ());
         return true;
      } else {
         if (metadata == 1 && tileEntity instanceof TilePedestal) {
            TilePedestal ped = (TilePedestal) tileEntity;
            if (!ped.getStackInSlot(0).isEmpty()) {
               InventoryUtils.dropItemsAtEntity(world, pos.getX(), pos.getY(), pos.getZ(), player);
               world.playSound(null, pos,
                     new SoundEvent(new net.minecraft.util.ResourceLocation("minecraft", "entity.item.pickup")),
                     SoundCategory.BLOCKS, 0.2F, ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.0F) * 1.5F);
               return true;
            }

            ItemStack held = player.getHeldItem(hand);
            if (!held.isEmpty()) {
               ItemStack i = held.copy();
               i.setCount(1);
               ped.setInventorySlotContents(0, i);
               held.shrink(1);
               player.inventory.markDirty();
               world.playSound(null, pos,
                     new SoundEvent(new net.minecraft.util.ResourceLocation("minecraft", "entity.item.pickup")),
                     SoundCategory.BLOCKS, 0.2F, ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.0F) * 1.6F);
               return true;
            }
         }

         BlockPos wandPos = pos;
         int wandMeta = metadata;
         TileEntity wandTe = tileEntity;
         if (metadata == 8) {
            wandPos = pos.down();
            IBlockState downState = world.getBlockState(wandPos);
            wandMeta = downState.getBlock().getMetaFromState(downState);
            wandTe = world.getTileEntity(wandPos);
         }

         if (wandMeta == 5 && wandTe instanceof TileWandPedestal) {
            if (!player.getHeldItem(hand).isEmpty() && player.getHeldItem(hand).isItemEqual(new ItemStack(this, 1, 8))) {
               return false;
            }

            TileWandPedestal ped = (TileWandPedestal) wandTe;
            if (!ped.getStackInSlot(0).isEmpty()) {
               InventoryUtils.dropItemsAtEntity(world, wandPos.getX(), wandPos.getY(), wandPos.getZ(), player);
               { net.minecraft.block.state.IBlockState _bs = world.getBlockState(wandPos); world.notifyBlockUpdate(wandPos, _bs, _bs, 3); }
               ped.markDirty();
               world.playSound(null, wandPos,
                     new SoundEvent(new net.minecraft.util.ResourceLocation("minecraft", "entity.item.pickup")),
                     SoundCategory.BLOCKS, 0.2F, ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.0F) * 1.5F);
               return true;
            }

            ItemStack heldItem = player.getHeldItem(hand);
            if (!heldItem.isEmpty() && (heldItem.getItem() instanceof ItemWandCasting || heldItem.getItem() instanceof ItemAmuletVis)) {
               ItemStack i = heldItem.copy();
               i.setCount(1);
               ped.setInventorySlotContents(0, i);
               heldItem.shrink(1);
               player.inventory.markDirty();
               { net.minecraft.block.state.IBlockState _bs = world.getBlockState(wandPos); world.notifyBlockUpdate(wandPos, _bs, _bs, 3); }
               ped.markDirty();
               world.playSound(null, wandPos,
                     new SoundEvent(new net.minecraft.util.ResourceLocation("minecraft", "entity.item.pickup")),
                     SoundCategory.BLOCKS, 0.2F, ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.0F) * 1.6F);
               return true;
            }
         }

         if (metadata == 12 && tileEntity instanceof TileSpa && !player.isSneaking()) {
            // FluidContainerRegistry removed; spa fluid filling must be handled via capability elsewhere
            player.openGui(Thaumcraft.instance, 19, world, pos.getX(), pos.getY(), pos.getZ());
            return true;
         } else if (metadata == 13 && tileEntity instanceof TileFocalManipulator && !player.isSneaking()) {
            if (ThaumcraftApiHelper.isResearchComplete(player.getName(), "FOCALMANIPULATION")) {
               player.openGui(Thaumcraft.instance, 20, world, pos.getX(), pos.getY(), pos.getZ());
            } else if (!world.isRemote) {
               player.sendMessage(new TextComponentString(TextFormatting.RED + I18n.format("tc.researchmissing")));
            }
            return true;
         } else {
            return false;
         }
      }
   }

   @Override
   public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
      int metadata = state.getBlock().getMetaFromState(state);
      if (metadata == 1) {
         return new AxisAlignedBB(0.25, 0.0, 0.25, 0.75, 0.99, 0.75);
      } else if (metadata == 5) {
         return new AxisAlignedBB(0.25, 0.0, 0.25, 0.75, 1.0, 0.75);
      } else if (metadata == 3) {
         return new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.5, 1.0);
      } else if (metadata == 4) {
         return new AxisAlignedBB(0.0, -1.0, 0.0, 1.0, -0.5, 1.0);
      } else if (metadata == 8) {
         return new AxisAlignedBB(0.0625, 0.0, 0.0625, 0.9375, 0.4375, 0.9375);
      } else {
         return FULL_BLOCK_AABB;
      }
   }

   @Override
   public boolean eventReceived(IBlockState state, World world, BlockPos pos, int eventID, int eventParam) {
      if (eventID == 1) {
         if (world.isRemote) {
            Thaumcraft.proxy.blockSparkle(world, pos.getX(), pos.getY(), pos.getZ(), 11960575, 2);
            world.playEvent(2001, pos, Block.getStateId(Blocks.STONEBRICK.getDefaultState()));
         }
         return true;
      } else {
         return super.eventReceived(state, world, pos, eventID, eventParam);
      }
   }

   @Override
   public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
      int meta = state.getBlock().getMetaFromState(state);
      if (meta == 11 && side == EnumFacing.UP) {
         return true;
      } else {
         return meta == 12 || super.isSideSolid(state, world, pos, side);
      }
   }

   @Override
   public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) {
      // The single-block alchemical furnace and spa use normal baked block models in 1.12.
      int meta = state.getValue(META);
      if (meta == 0 || meta == 8 || meta == 12) {
         return net.minecraft.util.EnumBlockRenderType.MODEL;
      }
      // The remaining stone devices still rely on TESR-style rendering or custom bounds.
      return net.minecraft.util.EnumBlockRenderType.INVISIBLE;
   }
}
