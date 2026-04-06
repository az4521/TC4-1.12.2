package thaumcraft.common.blocks;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import thaumcraft.api.aspects.IEssentiaContainerItem;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.Config;
import thaumcraft.common.lib.utils.InventoryUtils;
import thaumcraft.common.items.ItemEssence;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumcraft.common.tiles.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BlockWoodenDevice extends Block {
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

   private Random random = new Random();

   public BlockWoodenDevice() {
      super(Material.WOOD);
      this.setDefaultState(this.blockState.getBaseState().withProperty(META, 0));
      this.setHardness(2.5F);
      this.setResistance(10.0F);
      this.setTickRandomly(true);
      this.setCreativeTab(Thaumcraft.tabTC);
   }

   public int tickRate() {
      return 20;
   }

   @Override
   @SideOnly(Side.CLIENT)
   public void getSubBlocks(CreativeTabs par2CreativeTabs, NonNullList<ItemStack> par3List) {
      par3List.add(new ItemStack(this, 1, 0));
      par3List.add(new ItemStack(this, 1, 1));
      par3List.add(new ItemStack(this, 1, 2));
      par3List.add(new ItemStack(this, 1, 4));
      par3List.add(new ItemStack(this, 1, 5));
      par3List.add(new ItemStack(this, 1, 6));
      par3List.add(new ItemStack(this, 1, 7));
      par3List.add(new ItemStack(this, 1, 8));

      for (int a = 0; a < 16; ++a) {
         ItemStack banner = new ItemStack(this, 1, 8);
         banner.setTagCompound(new NBTTagCompound());
         banner.getTagCompound().setByte("color", (byte) a);
         par3List.add(banner);
      }
   }

   @Override
   public int damageDropped(IBlockState state) {
      int par1 = state.getBlock().getMetaFromState(state);
      return par1 == 3 ? 2 : par1;
   }

   @Override
   public Item getItemDropped(IBlockState state, Random par2Random, int par3) {
      int par1 = state.getBlock().getMetaFromState(state);
      if (!Config.wardedStone || par1 != 2 && par1 != 3) {
         return par1 == 8 ? Item.getItemById(0) : super.getItemDropped(state, par2Random, par3);
      } else {
         return Item.getItemById(0);
      }
   }

   @Override
   public float getBlockHardness(IBlockState state, World world, BlockPos pos) {
      int md = state.getBlock().getMetaFromState(state);
      if (md != 2 && md != 3) {
         return super.getBlockHardness(state, world, pos);
      } else {
         return Config.wardedStone ? -1.0F : 2.0F;
      }
   }

   @Override
   public float getExplosionResistance(World world, BlockPos pos, Entity par1Entity, Explosion explosion) {
      IBlockState state = world.getBlockState(pos);
      int md = state.getBlock().getMetaFromState(state);
      return md != 2 && md != 3 ? super.getExplosionResistance(world, pos, par1Entity, explosion) : 999.0F;
   }

   @Override
   public void onBlockExploded(World world, BlockPos pos, Explosion explosion) {
      IBlockState state = world.getBlockState(pos);
      int md = state.getBlock().getMetaFromState(state);
      if (md != 2 && md != 3) {
         super.onBlockExploded(world, pos, explosion);
      }
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
   public boolean hasTileEntity(IBlockState state) {
      return true;
   }

   @Override
   public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World worldIn, BlockPos pos) {
      int meta = state.getBlock().getMetaFromState(state);
      if (meta == 0) {
         return new AxisAlignedBB(pos.getX() + 0.1, pos.getY(), pos.getZ() + 0.1,
               pos.getX() + 0.9, pos.getY() + 1.0, pos.getZ() + 0.9);
      }
      return super.getSelectedBoundingBox(state, worldIn, pos);
   }

   @Override
   public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess par1iBlockAccess, BlockPos pos) {
      int meta = state.getBlock().getMetaFromState(state);
      if (meta == 0) {
         return new AxisAlignedBB(0.1, 0.0, 0.1, 0.9, 1.0, 0.9);
      } else if (meta == 2) {
         float var6 = 0.0625F;
         return new AxisAlignedBB(var6, 0.0F, var6, 1.0F - var6, 0.0625F, 1.0F - var6);
      } else if (meta == 3) {
         float var6 = 0.0625F;
         return new AxisAlignedBB(var6, 0.0F, var6, 1.0F - var6, 0.03125F, 1.0F - var6);
      } else if (meta == 5) {
         EnumFacing dir = EnumFacing.UP;
         TileEntity tile = par1iBlockAccess.getTileEntity(pos);
         if (tile instanceof TileArcaneBore) {
            dir = ((TileArcaneBore) tile).orientation;
         }
         return new AxisAlignedBB(
               (dir.getXOffset() < 0 ? -1 : 0),
               (dir.getYOffset() < 0 ? -1 : 0),
               (dir.getZOffset() < 0 ? -1 : 0),
               1 + (dir.getXOffset() > 0 ? 1 : 0),
               1 + (dir.getYOffset() > 0 ? 1 : 0),
               1 + (dir.getZOffset() > 0 ? 1 : 0));
      } else if (meta == 8) {
         TileEntity tile = par1iBlockAccess.getTileEntity(pos);
         if (tile instanceof TileBanner) {
            if (((TileBanner) tile).getWall()) {
               switch (((TileBanner) tile).getFacing()) {
                  case 0:
                     return new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 2.0, 0.25);
                  case 4:
                     return new AxisAlignedBB(0.75, 0.0, 0.0, 1.0, 2.0, 1.0);
                  case 8:
                     return new AxisAlignedBB(0.0, 0.0, 0.75, 1.0, 2.0, 1.0);
                  case 12:
                     return new AxisAlignedBB(0.0, 0.0, 0.0, 0.25, 2.0, 1.0);
               }
            } else {
               return new AxisAlignedBB(0.33, 0.0, 0.33, 0.66, 2.0, 0.66);
            }
         }
      }
      return FULL_BLOCK_AABB;
   }

   @Override
   public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
      int meta = state.getBlock().getMetaFromState(state);
      if (meta == 2 || meta == 3 || meta == 8) {
         return NULL_AABB;
      } else if (meta == 0) {
         return new AxisAlignedBB(0.1, 0.0, 0.1, 0.9, 1.0, 0.9);
      } else if (meta == 5) {
         EnumFacing dir = EnumFacing.UP;
         TileEntity tile = worldIn.getTileEntity(pos);
         if (tile instanceof TileArcaneBore) {
            dir = ((TileArcaneBore) tile).orientation;
         }
         return new AxisAlignedBB(
               (dir.getXOffset() < 0 ? -1 : 0),
               (dir.getYOffset() < 0 ? -1 : 0),
               (dir.getZOffset() < 0 ? -1 : 0),
               1 + (dir.getXOffset() > 0 ? 1 : 0),
               1 + (dir.getYOffset() > 0 ? 1 : 0),
               1 + (dir.getZOffset() > 0 ? 1 : 0));
      }
      return FULL_BLOCK_AABB;
   }

   @Override
   public void neighborChanged(IBlockState state, World world, BlockPos pos, Block par5, BlockPos fromPos) {
      int meta = state.getBlock().getMetaFromState(state);
      if (meta == 1) {
         TileEntity tile = world.getTileEntity(pos);
         if (tile instanceof TileSensor) {
            ((TileSensor) tile).updateTone();
         }
      } else if (meta == 5) {
         TileArcaneBore tile = (TileArcaneBore) world.getTileEntity(pos);
         if (tile instanceof TileArcaneBore) {
            EnumFacing d = tile.baseOrientation.getOpposite();
            BlockPos neighborPos = pos.add(d.getXOffset(), d.getYOffset(), d.getZOffset());
            IBlockState neighborState = world.getBlockState(neighborPos);
            Block block = neighborState.getBlock();
            if (block != thaumcraft.common.config.ConfigBlocks.blockWoodenDevice
                  || !block.isSideSolid(neighborState, world, neighborPos, tile.baseOrientation)) {
               InventoryUtils.dropItems(world, pos.getX(), pos.getY(), pos.getZ());
               Block.spawnAsEntity(world, pos, new ItemStack(this, 1, 5));
               world.setBlockToAir(pos);
            }
         }
      }

      super.neighborChanged(state, world, pos, par5, fromPos);
   }

   @Override
   public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
      int meta = state.getBlock().getMetaFromState(state);
      return meta == 4 || meta == 6 || meta == 7 || super.isSideSolid(state, world, pos, side);
   }

   @Override
   public boolean onBlockActivated(World w, BlockPos pos, IBlockState state, EntityPlayer p, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
      int meta = state.getBlock().getMetaFromState(state);
      if (meta != 4 && meta != 6 && meta != 7) {
         if (w.isRemote) {
            return true;
         } else if (meta != 5 || !p.getHeldItem(hand).isEmpty() && p.getHeldItem(hand).getItem() instanceof ItemWandCasting) {
            if (meta == 1) {
               TileSensor var6 = (TileSensor) w.getTileEntity(pos);
               if (var6 != null) {
                  var6.changePitch();
                  var6.triggerNote(w, pos.getX(), pos.getY(), pos.getZ(), true);
               }
            } else if (meta != 2 && meta != 3) {
               if (meta == 8 && (p.isSneaking() || !p.getHeldItem(hand).isEmpty() && p.getHeldItem(hand).getItem() instanceof ItemEssence)) {
                  TileBanner te = (TileBanner) w.getTileEntity(pos);
                  if (te != null && te.getColor() >= 0) {
                     if (p.isSneaking()) {
                        te.setAspect(null);
                     } else if (((IEssentiaContainerItem) p.getHeldItem(hand).getItem()).getAspects(p.getHeldItem(hand)) != null) {
                        te.setAspect(((IEssentiaContainerItem) p.getHeldItem(hand).getItem()).getAspects(p.getHeldItem(hand)).getAspects()[0]);
                        p.getHeldItem(hand).shrink(1);
                     }

                     { net.minecraft.block.state.IBlockState _bs = w.getBlockState(pos); w.notifyBlockUpdate(pos, _bs, _bs, 3); }
                     te.markDirty();
                     w.playSound(null, pos,
                           new SoundEvent(new net.minecraft.util.ResourceLocation("minecraft", "block.cloth.step")),
                           SoundCategory.BLOCKS, 1.0F, 1.0F);
                  }
               }
            } else {
               TileArcanePressurePlate var6 = (TileArcanePressurePlate) w.getTileEntity(pos);
               if (var6 != null && (var6.owner.equals(p.getName()) || var6.accessList.contains("1" + p.getName()))) {
                  ++var6.setting;
                  if (var6.setting > 2) {
                     var6.setting = 0;
                  }

                  switch (var6.setting) {
                     case 0:
                        p.sendMessage(new TextComponentTranslation("It will now trigger on everything."));
                        break;
                     case 1:
                        p.sendMessage(new TextComponentTranslation("It will now trigger on everything except you."));
                        break;
                     case 2:
                        p.sendMessage(new TextComponentTranslation("It will now trigger on just you."));
                  }

                  w.playSound(null, pos,
                        new SoundEvent(new net.minecraft.util.ResourceLocation("minecraft", "ui.button.click")),
                        SoundCategory.BLOCKS, 0.1F, 0.9F);
                  { net.minecraft.block.state.IBlockState _bs = w.getBlockState(pos); w.notifyBlockUpdate(pos, _bs, _bs, 3); }
                  var6.markDirty();
               }
            }

            return true;
         } else {
            p.openGui(Thaumcraft.instance, 15, w, pos.getX(), pos.getY(), pos.getZ());
            return true;
         }
      } else {
         return false;
      }
   }

   @Override
   public void onBlockHarvested(World par1World, BlockPos pos, IBlockState state, EntityPlayer par6EntityPlayer) {
      int md = state.getBlock().getMetaFromState(state);
      if (md == 8) {
         // Banner drops are handled via getDrops; no special drop action needed here
      }
      super.onBlockHarvested(par1World, pos, state, par6EntityPlayer);
   }

   @Override
   public java.util.List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
      int md = state.getBlock().getMetaFromState(state);
      if (md != 8) {
         return super.getDrops(world, pos, state, fortune);
      } else {
         ArrayList<ItemStack> drops = new ArrayList<>();
         TileEntity te = world.getTileEntity(pos);
         if (te instanceof TileBanner) {
            ItemStack drop = new ItemStack(this, 1, 8);
            if (((TileBanner) te).getColor() >= 0 || ((TileBanner) te).getAspect() != null) {
               drop.setTagCompound(new NBTTagCompound());
               if (((TileBanner) te).getAspect() != null) {
                  drop.getTagCompound().setString("aspect", ((TileBanner) te).getAspect().getTag());
               }
               drop.getTagCompound().setByte("color", ((TileBanner) te).getColor());
            }
            drops.add(drop);
         }
         return drops;
      }
   }

   @Override
   public void onBlockPlacedBy(World w, BlockPos pos, IBlockState state, EntityLivingBase p, ItemStack s) {
      TileEntity tile = w.getTileEntity(pos);
      if (tile instanceof TileOwned && p instanceof EntityPlayer) {
         ((TileOwned) tile).owner = p.getName();
         tile.markDirty();
      }
      super.onBlockPlacedBy(w, pos, state, p, s);
   }

   @Override
   public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
      super.onBlockAdded(world, pos, state);
      int meta = state.getBlock().getMetaFromState(state);
      if (meta == 1) {
         TileEntity tile = world.getTileEntity(pos);
         if (tile instanceof TileSensor) {
            ((TileSensor) tile).updateTone();
            tile.markDirty();
         }
      }
   }

   public boolean canConnectRedstone(IBlockAccess world, BlockPos pos, EnumFacing side) {
      IBlockState state = world.getBlockState(pos);
      int meta = state.getBlock().getMetaFromState(state);
      if (meta == 0) {
         return false;
      } else {
         return meta == 1 || meta == 2 || meta == 3 || meta == 4 || meta == 5;
      }
   }

   @Override
   public TileEntity createTileEntity(World world, IBlockState state) {
      int metadata = state.getBlock().getMetaFromState(state);
      if (metadata == 0) {
         return new TileBellows();
      } else if (metadata == 1) {
         return new TileSensor();
      } else if (metadata == 2) {
         return new TileArcanePressurePlate();
      } else if (metadata == 3) {
         return new TileArcanePressurePlate();
      } else if (metadata == 4) {
         return new TileArcaneBoreBase();
      } else if (metadata == 5) {
         return new TileArcaneBore();
      } else if (metadata == 6 || metadata == 7) {
         return new thaumcraft.common.tiles.TilePlank();
      } else {
         return metadata == 8 ? new TileBanner() : null;
      }
   }

   @Override
   public boolean eventReceived(IBlockState state, World par1World, BlockPos pos, int par5, int par6) {
      float var7 = (float) Math.pow(2.0F, (double) (par6 - 12) / (double) 12.0F);
      if (par5 <= 4) {
         if (par5 >= 0) {
            String var8 = "harp";
            if (par5 == 1) var8 = "bd";
            if (par5 == 2) var8 = "snare";
            if (par5 == 3) var8 = "hat";
            if (par5 == 4) var8 = "bassattack";

            par1World.playSound(null, pos,
                  new SoundEvent(new net.minecraft.util.ResourceLocation("minecraft", "block.note." + var8)),
                  SoundCategory.BLOCKS, 3.0F, var7);
         }

         par1World.spawnParticle(net.minecraft.util.EnumParticleTypes.NOTE,
               pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5,
               (double) par6 / 24.0, 0.0, 0.0);
         return true;
      } else {
         return super.eventReceived(state, par1World, pos, par5, par6);
      }
   }

   @Override
   public void updateTick(World par1World, BlockPos pos, IBlockState state, Random par5Random) {
      if (!par1World.isRemote && state.getBlock().getMetaFromState(state) == 3) {
         this.setStateIfMobInteractsWithPlate(par1World, pos);
      }
   }

   @Override
   public void onEntityCollision(World par1World, BlockPos pos, IBlockState state, Entity par5Entity) {
      if (!par1World.isRemote && state.getBlock().getMetaFromState(state) == 2) {
         this.setStateIfMobInteractsWithPlate(par1World, pos);
      }
   }

   private void setStateIfMobInteractsWithPlate(World world, BlockPos pos) {
      IBlockState state = world.getBlockState(pos);
      int meta = state.getBlock().getMetaFromState(state);
      boolean var5 = meta == 3;
      boolean var6 = false;
      float var7 = 0.125F;
      List<Entity> var8 = null;
      String username = "";
      byte setting = 0;
      ArrayList<String> accessList = new ArrayList<>();
      TileEntity tile = world.getTileEntity(pos);
      if (tile instanceof TileArcanePressurePlate) {
         setting = ((TileArcanePressurePlate) tile).setting;
         username = ((TileArcanePressurePlate) tile).owner;
         accessList = ((TileArcanePressurePlate) tile).accessList;
      }

      int x = pos.getX();
      int y = pos.getY();
      int z = pos.getZ();

      if (setting == 0) {
         var8 = world.getEntitiesWithinAABBExcludingEntity(null,
               new AxisAlignedBB((float) x + var7, y, (float) z + var7,
                     (float) (x + 1) - var7, (double) y + 0.25, (float) (z + 1) - var7));
      }

      if (setting == 1) {
         var8 = world.getEntitiesWithinAABB(Entity.class,
               new AxisAlignedBB((float) x + var7, y, (float) z + var7,
                     (float) (x + 1) - var7, (double) y + 0.25, (float) (z + 1) - var7));
      }

      if (setting == 2) {
         var8 = world.getEntitiesWithinAABB(EntityPlayer.class,
               new AxisAlignedBB((float) x + var7, y, (float) z + var7,
                     (float) (x + 1) - var7, (double) y + 0.25, (float) (z + 1) - var7));
      }

      if (var8 != null && !var8.isEmpty()) {
         for (Entity var10 : var8) {
            if (!var10.doesEntityNotTriggerPressurePlate()
                  && (setting != 1 || !(var10 instanceof EntityPlayer)
                        || !var10.getName().equals(username)
                              && !accessList.contains("0" + var10.getName())
                              && !accessList.contains("1" + var10.getName()))
                  && (setting != 2 || !(var10 instanceof EntityPlayer)
                        || var10.getName().equals(username)
                              || accessList.contains("0" + var10.getName())
                              || accessList.contains("1" + var10.getName()))) {
               var6 = true;
               break;
            }
         }
      }

      if (var6 && !var5) {
         // Switch metadata: meta 2 = unpowered, meta 3 = powered
         world.setBlockState(pos, state.getBlock().getStateFromMeta(3), 2);
         world.notifyNeighborsOfStateChange(pos, this, false);
         world.notifyNeighborsOfStateChange(pos.down(), this, false);
         { net.minecraft.block.state.IBlockState _bs = world.getBlockState(pos); world.notifyBlockUpdate(pos, _bs, _bs, 3); }
         world.playSound(null, pos,
               new SoundEvent(new net.minecraft.util.ResourceLocation("minecraft", "ui.button.click")),
               SoundCategory.BLOCKS, 0.2F, 0.6F);
      }

      if (!var6 && var5) {
         world.setBlockState(pos, state.getBlock().getStateFromMeta(2), 2);
         world.notifyNeighborsOfStateChange(pos, this, false);
         world.notifyNeighborsOfStateChange(pos.down(), this, false);
         { net.minecraft.block.state.IBlockState _bs = world.getBlockState(pos); world.notifyBlockUpdate(pos, _bs, _bs, 3); }
         world.playSound(null, pos,
               new SoundEvent(new net.minecraft.util.ResourceLocation("minecraft", "ui.button.click")),
               SoundCategory.BLOCKS, 0.2F, 0.5F);
      }

      if (var6) {
         world.scheduleUpdate(pos, this, this.tickRate());
      }
   }

   @Override
   public void breakBlock(World par1World, BlockPos pos, IBlockState state) {
      int par6 = state.getBlock().getMetaFromState(state);
      if (par6 == 3) {
         par1World.notifyNeighborsOfStateChange(pos, this, false);
         par1World.notifyNeighborsOfStateChange(pos.down(), this, false);
      } else if (par6 == 5) {
         InventoryUtils.dropItems(par1World, pos.getX(), pos.getY(), pos.getZ());
      }

      super.breakBlock(par1World, pos, state);
   }

   @Override
   public int getStrongPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
      int meta = state.getBlock().getMetaFromState(state);
      if (meta == 1) {
         TileEntity tile = world.getTileEntity(pos);
         if (tile instanceof TileSensor) {
            return ((TileSensor) tile).redstoneSignal > 0 ? 15 : 0;
         } else {
            return super.getStrongPower(state, world, pos, side);
         }
      } else {
         return meta == 2 ? 0 : (side == EnumFacing.UP && meta == 3 ? 15 : 0);
      }
   }

   @Override
   public int getWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
      int meta = state.getBlock().getMetaFromState(state);
      if (meta == 1) {
         TileEntity tile = world.getTileEntity(pos);
         if (tile instanceof TileSensor) {
            return ((TileSensor) tile).redstoneSignal > 0 ? 15 : 0;
         }
      } else if (meta == 3) {
         return 15;
      }
      return super.getStrongPower(state, world, pos, side);
   }

   @Override
   public boolean canProvidePower(IBlockState state) {
      return true;
   }

   @Override
   public int getLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos) {
      int meta = state.getBlock().getMetaFromState(state);
      return meta != 6 && meta != 7 ? super.getLightOpacity(state, world, pos) : 255;
   }

   @Override
   public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos, Entity entity) {
      int meta = state.getBlock().getMetaFromState(state);
      return meta != 2 && meta != 3 && super.canEntityDestroy(state, world, pos, entity);
   }

   @Override
   public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
      IBlockState state = world.getBlockState(pos);
      int meta = state.getBlock().getMetaFromState(state);
      return meta != 6 && meta != 7 ? 0 : 20;
   }

   @Override
   public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
      IBlockState state = world.getBlockState(pos);
      int meta = state.getBlock().getMetaFromState(state);
      return meta != 6 && meta != 7 ? 0 : 5;
   }

   @Override
   public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) {
      return net.minecraft.util.EnumBlockRenderType.INVISIBLE;
   }
}
