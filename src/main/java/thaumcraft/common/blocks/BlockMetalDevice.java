package thaumcraft.common.blocks;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssentiaContainerItem;
import thaumcraft.api.visnet.VisNetHandler;
import thaumcraft.client.renderers.block.BlockRenderer;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.entities.EntitySpecialItem;
import thaumcraft.common.items.ItemShard;
import thaumcraft.common.lib.utils.InventoryUtils;
import thaumcraft.common.tiles.*;

import java.util.List;
import java.util.Random;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class BlockMetalDevice extends BlockContainer {
   public TextureAtlasSprite[] icon = new TextureAtlasSprite[23];
   public TextureAtlasSprite iconGlow;
   private int delay = 0;

   public BlockMetalDevice() {
      super(Material.IRON);
      this.setHardness(3.0F);
      this.setResistance(17.0F);
      this.setSoundType(net.minecraft.block.SoundType.METAL);
      this.setCreativeTab(Thaumcraft.tabTC);
      this.setDefaultState(this.blockState.getBaseState().withProperty(META, 0));
   }

   public static final net.minecraft.block.properties.PropertyInteger META =
         net.minecraft.block.properties.PropertyInteger.create("meta", 0, 15);

   @Override
   public BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, META);
   }

   @Override
   public IBlockState getStateFromMeta(int meta) {
      return this.getDefaultState().withProperty(META, meta);
   }

   @Override
   public int getMetaFromState(IBlockState state) {
      return state.getValue(META);
   }

   public static boolean addToPlayerInventoryBiased(InventoryPlayer inv, ItemStack s) {
      if (s == null || s.isEmpty() || s.getItem() == null) return false;
      for (ItemStack stack : inv.mainInventory) {
         if (!stack.isEmpty() && stack.isStackable() && stack.getItem() == s.getItem() &&
                 stack.getCount() < Math.min(stack.getMaxStackSize(), inv.getInventoryStackLimit())
                 && (!stack.getHasSubtypes() || stack.getItemDamage() == s.getItemDamage()) &&
                 ItemStack.areItemStackTagsEqual(stack, s)) {
            int toAdd = Math.min(s.getCount(), Math.min(stack.getMaxStackSize(), inv.getInventoryStackLimit()) - stack.getCount());
            s.shrink(toAdd);
            stack.grow(toAdd);
            if (s.isEmpty()) return true;
         }
      }
      if (inv.currentItem >= 0 && inv.currentItem < InventoryPlayer.getHotbarSize() && inv.getCurrentItem().isEmpty()) {
         inv.setInventorySlotContents(inv.currentItem, s);
         return true;
      } else {
         return inv.addItemStackToInventory(s);
      }
   }

   @Override
   @SideOnly(Side.CLIENT)
   public void getSubBlocks(CreativeTabs par2CreativeTabs, NonNullList<ItemStack> par3List) {
      par3List.add(new ItemStack(this, 1, 0));
      par3List.add(new ItemStack(this, 1, 1));
      par3List.add(new ItemStack(this, 1, 2));
      par3List.add(new ItemStack(this, 1, 3));
      par3List.add(new ItemStack(this, 1, 5));
      par3List.add(new ItemStack(this, 1, 7));
      par3List.add(new ItemStack(this, 1, 8));
      par3List.add(new ItemStack(this, 1, 9));
      par3List.add(new ItemStack(this, 1, 12));
      par3List.add(new ItemStack(this, 1, 13));
      par3List.add(new ItemStack(this, 1, 14));
   }

   @Override
   public boolean isOpaqueCube(IBlockState state) {
      return false;
   }

   @Override
   public boolean isFullCube(IBlockState state) {
      return false;
   }

   private int getMetaAt(IBlockAccess world, BlockPos pos) {
      IBlockState s = world.getBlockState(pos);
      return s.getBlock().getMetaFromState(s);
   }

   @Override
   public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
      int metadata = getMetaAt(world, pos);
      if (metadata == 5 || metadata == 6) {
         return new AxisAlignedBB(0.0, 0.8125, 0.0, 1.0, 1.0, 1.0);
      } else if (metadata == 7 || metadata == 8 || metadata == 13) {
         return new AxisAlignedBB(BlockRenderer.W4, BlockRenderer.W2, BlockRenderer.W4, BlockRenderer.W12, BlockRenderer.W14, BlockRenderer.W12);
      } else if (metadata == 10) {
         return new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 2.0, 1.0);
      } else if (metadata == 11) {
         return new AxisAlignedBB(0.0, -1.0, 0.0, 1.0, 1.0, 1.0);
      } else if (metadata == 12) {
         return new AxisAlignedBB(BlockRenderer.W3, BlockRenderer.W3, BlockRenderer.W3, BlockRenderer.W13, BlockRenderer.W13, BlockRenderer.W13);
      } else if (metadata == 2) {
         return new AxisAlignedBB(BlockRenderer.W5, 0.5, BlockRenderer.W5, BlockRenderer.W11, 1.0, BlockRenderer.W11);
      } else if (metadata == 14) {
         TileEntity te = world.getTileEntity(pos);
         if (te instanceof TileVisRelay) {
            switch (EnumFacing.byIndex(((TileVisRelay) te).orientation).getOpposite()) {
               case UP:    return new AxisAlignedBB(BlockRenderer.W5, 0.5, BlockRenderer.W5, BlockRenderer.W11, 1.0, BlockRenderer.W11);
               case DOWN:  return new AxisAlignedBB(BlockRenderer.W5, 0.0, BlockRenderer.W5, BlockRenderer.W11, 0.5, BlockRenderer.W11);
               case EAST:  return new AxisAlignedBB(0.5, BlockRenderer.W5, BlockRenderer.W5, 1.0, BlockRenderer.W11, BlockRenderer.W11);
               case WEST:  return new AxisAlignedBB(0.0, BlockRenderer.W5, BlockRenderer.W5, 0.5, BlockRenderer.W11, BlockRenderer.W11);
               case SOUTH: return new AxisAlignedBB(BlockRenderer.W5, BlockRenderer.W5, 0.5, BlockRenderer.W11, BlockRenderer.W11, 1.0);
               case NORTH: return new AxisAlignedBB(BlockRenderer.W5, BlockRenderer.W5, 0.0, BlockRenderer.W11, BlockRenderer.W11, 0.5);
               default:    break;
            }
         }
      }
      return FULL_BLOCK_AABB;
   }

   @Override
   public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity par7Entity, boolean p_185477_7_) {
      int metadata = getMetaAt(world, pos);
      if (metadata == 0) {
         addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.3125, 1.0));
         float f = 0.125F;
         addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(0.0, 0.0, 0.0, f, 0.85, 1.0));
         addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.85, f));
         addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(1.0 - f, 0.0, 0.0, 1.0, 0.85, 1.0));
         addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(0.0, 0.0, 1.0 - f, 1.0, 0.85, 1.0));
      } else if (metadata == 2) {
         addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(BlockRenderer.W5, 0.5, BlockRenderer.W5, BlockRenderer.W11, 1.0, BlockRenderer.W11));
      } else if (metadata == 5) {
         if (par7Entity != null && !(par7Entity instanceof EntityItem)) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(0.0, 0.8125, 0.0, 1.0, 1.0, 1.0));
         }
      } else if (metadata == 6) {
         addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(0.0, 0.8125, 0.0, 1.0, 1.0, 1.0));
      } else if (metadata != 7 && metadata != 8 && metadata != 13) {
         if (metadata == 12) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(BlockRenderer.W3, BlockRenderer.W3, BlockRenderer.W3, BlockRenderer.W13, BlockRenderer.W13, BlockRenderer.W13));
         } else if (metadata == 14) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileVisRelay) {
               AxisAlignedBB bb;
               switch (EnumFacing.byIndex(((TileVisRelay) te).orientation).getOpposite()) {
                  case UP:    bb = new AxisAlignedBB(BlockRenderer.W5, 0.5, BlockRenderer.W5, BlockRenderer.W11, 1.0, BlockRenderer.W11); break;
                  case DOWN:  bb = new AxisAlignedBB(BlockRenderer.W5, 0.0, BlockRenderer.W5, BlockRenderer.W11, 0.5, BlockRenderer.W11); break;
                  case EAST:  bb = new AxisAlignedBB(0.5, BlockRenderer.W5, BlockRenderer.W5, 1.0, BlockRenderer.W11, BlockRenderer.W11); break;
                  case WEST:  bb = new AxisAlignedBB(0.0, BlockRenderer.W5, BlockRenderer.W5, 0.5, BlockRenderer.W11, BlockRenderer.W11); break;
                  case SOUTH: bb = new AxisAlignedBB(BlockRenderer.W5, BlockRenderer.W5, 0.5, BlockRenderer.W11, BlockRenderer.W11, 1.0); break;
                  default:    bb = new AxisAlignedBB(BlockRenderer.W5, BlockRenderer.W5, 0.0, BlockRenderer.W11, BlockRenderer.W11, 0.5); break;
               }
               addCollisionBoxToList(pos, entityBox, collidingBoxes, bb);
            }
         } else {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, FULL_BLOCK_AABB);
         }
      } else {
         addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(BlockRenderer.W4, BlockRenderer.W2, BlockRenderer.W4, BlockRenderer.W12, BlockRenderer.W14, BlockRenderer.W12));
      }
   }

   @Override
   public TileEntity createNewTileEntity(World var1, int md) {
      return null;
   }

   @Override
   public boolean hasComparatorInputOverride(IBlockState state) {
      return true;
   }

   @Override
   public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos) {
      TileEntity te = world.getTileEntity(pos);
      if (te instanceof TileThaumatorium) {
         return Container.calcRedstoneFromInventory((IInventory) te);
      } else if (te instanceof TileAlembic) {
         float r = (float) ((TileAlembic) te).amount / (float) ((TileAlembic) te).maxAmount;
         return MathHelper.floor(r * 14.0F) + (((TileAlembic) te).amount > 0 ? 1 : 0);
      } else if (te instanceof TileCrucible) {
         float r = (float) ((TileCrucible) te).aspects.visSize() / 100.0F;
         return MathHelper.floor(r * 14.0F) + (((TileCrucible) te).aspects.visSize() > 0 ? 1 : 0);
      } else {
         return 0;
      }
   }

   @Override
   public void neighborChanged(IBlockState state, World world, BlockPos pos, Block nbid, BlockPos fromPos) {
      TileEntity te = world.getTileEntity(pos);
      int md = state.getBlock().getMetaFromState(state);
      if (te instanceof TileCrucible) {
         ((TileCrucible) te).getBellows();
      }

      if (!world.isRemote) {
         if (te instanceof TileAlembic) {
            { net.minecraft.block.state.IBlockState _bs = world.getBlockState(pos); world.notifyBlockUpdate(pos, _bs, _bs, 3); }
         } else if (te instanceof TileArcaneLamp) {
            TileArcaneLamp telb = (TileArcaneLamp) te;
            BlockPos lampFront = pos.add(telb.facing.getXOffset(), telb.facing.getYOffset(), telb.facing.getZOffset());
            if (world.isAirBlock(lampFront)) {
               Block.spawnAsEntity(world, pos, new ItemStack(this, 1, 7));
               world.setBlockToAir(pos);
            }
         } else if (te instanceof TileArcaneLampGrowth) {
            TileArcaneLampGrowth telb = (TileArcaneLampGrowth) te;
            BlockPos lampFront = pos.add(telb.facing.getXOffset(), telb.facing.getYOffset(), telb.facing.getZOffset());
            if (world.isAirBlock(lampFront)) {
               Block.spawnAsEntity(world, pos, new ItemStack(this, 1, 8));
               world.setBlockToAir(pos);
            }
         } else if (te instanceof TileBrainbox) {
            TileBrainbox telb = (TileBrainbox) te;
            BlockPos lampFront = pos.add(telb.facing.getXOffset(), telb.facing.getYOffset(), telb.facing.getZOffset());
            if (world.isAirBlock(lampFront)) {
               Block.spawnAsEntity(world, pos, new ItemStack(this, 1, 12));
               world.setBlockToAir(pos);
            }
         } else if (te instanceof TileVisRelay && md == 14) {
            TileVisRelay telb = (TileVisRelay) te;
            EnumFacing relayFace = EnumFacing.byIndex(telb.orientation).getOpposite();
            BlockPos relayFront = pos.add(relayFace.getXOffset(), relayFace.getYOffset(), relayFace.getZOffset());
            if (world.isAirBlock(relayFront)) {
               Block.spawnAsEntity(world, pos, new ItemStack(this, 1, 14));
               world.setBlockToAir(pos);
            }
         } else if (md == 10) {
            BlockPos above = pos.up();
            BlockPos below = pos.down();
            IBlockState aboveState = world.getBlockState(above);
            IBlockState belowState = world.getBlockState(below);
            if (aboveState.getBlock() != this || aboveState.getBlock().getMetaFromState(aboveState) != 11
                    || belowState.getBlock() != this || belowState.getBlock().getMetaFromState(belowState) != 0) {
               InventoryUtils.dropItems(world, pos.getX(), pos.getY(), pos.getZ());
               world.setBlockToAir(pos);
               world.setBlockState(pos, this.getStateFromMeta(9), 3);
               return;
            }

            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof TileThaumatorium) {
               ((TileThaumatorium) tile).getUpgrades();
            }
         } else if (md == 11) {
            BlockPos below = pos.down();
            IBlockState belowState = world.getBlockState(below);
            if (belowState.getBlock() != this || belowState.getBlock().getMetaFromState(belowState) != 10) {
               world.setBlockToAir(pos);
               world.setBlockState(pos, this.getStateFromMeta(9), 3);
               return;
            }

            TileEntity tile = world.getTileEntity(below);
            if (tile instanceof TileThaumatorium) {
               ((TileThaumatorium) tile).getUpgrades();
            }
         }

         boolean flag = world.isBlockPowered(pos);
         IBlockState fromState = world.getBlockState(fromPos);
         if (flag || nbid.canProvidePower(fromState)) {
            this.onPoweredBlockChange(world, pos, flag);
         }
      }

      super.neighborChanged(state, world, pos, nbid, fromPos);
   }

   @Override
   public void breakBlock(World par1World, BlockPos pos, IBlockState state) {
      InventoryUtils.dropItems(par1World, pos.getX(), pos.getY(), pos.getZ());
      TileEntity te = par1World.getTileEntity(pos);
      if (te instanceof TileCrucible) {
         ((TileCrucible) te).spillRemnants();
      } else if (te instanceof TileAlembic && ((TileAlembic) te).aspectFilter != null) {
         par1World.spawnEntity(new EntityItem(par1World, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(ConfigItems.itemResource, 1, 13)));
      } else if (te instanceof TileArcaneLamp) {
         ((TileArcaneLamp) te).removeLights();
      }

      super.breakBlock(par1World, pos, state);
   }

   @Override
   public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
                                   EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
      int metadata = state.getBlock().getMetaFromState(state);
      ItemStack heldItem = player.getHeldItem(hand);

      if (metadata == 0 && !world.isRemote) {
         // Fill crucible with water from a held fluid container (capability-based)
         if (!heldItem.isEmpty() && heldItem.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
            IFluidHandlerItem handler = heldItem.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            if (handler != null) {
               FluidStack drained = handler.drain(new FluidStack(FluidRegistry.WATER, 1000), false);
               if (drained != null && drained.amount > 0) {
                  TileEntity te = world.getTileEntity(pos);
                  if (te instanceof TileCrucible) {
                     TileCrucible tile = (TileCrucible) te;
                     if (tile.tank.getFluidAmount() < tile.tank.getCapacity()) {
                        int filled = tile.fill(drained, true);
                        if (filled > 0) {
                           handler.drain(new FluidStack(FluidRegistry.WATER, filled), true);
                           ItemStack container = handler.getContainer();
                           if (!container.isEmpty()) {
                              heldItem.shrink(1);
                              if (!addToPlayerInventoryBiased(player.inventory, container)) {
                                 player.dropItem(container, false);
                              }
                           }
                           player.inventoryContainer.detectAndSendChanges();
                           te.markDirty();
                           { net.minecraft.block.state.IBlockState _bs = world.getBlockState(pos); world.notifyBlockUpdate(pos, _bs, _bs, 3); }
                           world.playSound(null, pos,
                                   SoundEvent.REGISTRY.getObject(new ResourceLocation("minecraft:item.bucket.empty")),
                                   SoundCategory.BLOCKS, 0.33F, 1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.3F);
                        }
                     }
                  }
               }
            }
         }
      }

      if (metadata == 1 && !world.isRemote && !player.isSneaking() && heldItem.isEmpty()) {
         TileEntity te = world.getTileEntity(pos);
         if (te instanceof TileAlembic) {
            TileAlembic tile = (TileAlembic) te;
            String msg = "";
            if (tile.aspect != null && tile.amount != 0) {
               if ((double) tile.amount < (double) tile.maxAmount * 0.4) {
                  msg = net.minecraft.client.resources.I18n.format("tile.alembic.msg.2");
               } else if ((double) tile.amount < (double) tile.maxAmount * 0.8) {
                  msg = net.minecraft.client.resources.I18n.format("tile.alembic.msg.3");
               } else if (tile.amount < tile.maxAmount) {
                  msg = net.minecraft.client.resources.I18n.format("tile.alembic.msg.4");
               } else if (tile.amount == tile.maxAmount) {
                  msg = net.minecraft.client.resources.I18n.format("tile.alembic.msg.5");
               }
            } else {
               msg = net.minecraft.client.resources.I18n.format("tile.alembic.msg.1");
            }

            player.sendMessage(new TextComponentTranslation("\u00a73" + msg));
            world.playSound(null, pos,
                    SoundEvent.REGISTRY.getObject(new ResourceLocation("thaumcraft:alembicknock")),
                    SoundCategory.BLOCKS, 0.2F, 1.0F);
         }
      }

      if (metadata == 1) {
         TileEntity te = world.getTileEntity(pos);
         if (te instanceof TileAlembic) {
            if (player.isSneaking() && ((TileAlembic) te).aspectFilter != null) {
               ((TileAlembic) te).aspectFilter = null;
               { net.minecraft.block.state.IBlockState _bs = world.getBlockState(pos); world.notifyBlockUpdate(pos, _bs, _bs, 3); }
               te.markDirty();
               if (world.isRemote) {
                  world.playSound(player, pos,
                          SoundEvent.REGISTRY.getObject(new ResourceLocation("thaumcraft:page")),
                          SoundCategory.BLOCKS, 1.0F, 1.1F);
               } else {
                  world.spawnEntity(new EntityItem(world, pos.getX() + 0.5 + facing.getXOffset() / 3.0, pos.getY() + 0.5, pos.getZ() + 0.5 + facing.getZOffset() / 3.0, new ItemStack(ConfigItems.itemResource, 1, 13)));
               }

               return true;
            }

            if (player.isSneaking() && heldItem.isEmpty()) {
               ((TileAlembic) te).amount = 0;
               ((TileAlembic) te).aspect = null;
               if (world.isRemote) {
                  world.playSound(player, pos,
                          SoundEvent.REGISTRY.getObject(new ResourceLocation("thaumcraft:alembicknock")),
                          SoundCategory.BLOCKS, 0.2F, 1.0F);
                  world.playSound(player, pos,
                          SoundEvent.REGISTRY.getObject(new ResourceLocation("minecraft:item.bucket.empty")),
                          SoundCategory.BLOCKS, 0.5F, 1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.3F);
               }
            } else {
               if (!heldItem.isEmpty() && ((TileAlembic) te).aspectFilter == null && heldItem.getItem() == ConfigItems.itemResource && heldItem.getItemDamage() == 13) {
                  if (((TileAlembic) te).amount == 0 && ((IEssentiaContainerItem) heldItem.getItem()).getAspects(heldItem) == null) {
                     return true;
                  }

                  if (((TileAlembic) te).amount == 0 && ((IEssentiaContainerItem) heldItem.getItem()).getAspects(heldItem) != null) {
                     ((TileAlembic) te).aspect = ((IEssentiaContainerItem) heldItem.getItem()).getAspects(heldItem).getAspects()[0];
                  }

                  heldItem.shrink(1);
                  ((TileAlembic) te).aspectFilter = ((TileAlembic) te).aspect;
                  { net.minecraft.block.state.IBlockState _bs = world.getBlockState(pos); world.notifyBlockUpdate(pos, _bs, _bs, 3); }
                  te.markDirty();
                  if (world.isRemote) {
                     world.playSound(player, pos,
                             SoundEvent.REGISTRY.getObject(new ResourceLocation("thaumcraft:page")),
                             SoundCategory.BLOCKS, 1.0F, 0.9F);
                  }

                  return true;
               }

               if (!heldItem.isEmpty() && ((TileAlembic) te).amount > 0 && (heldItem.getItem() == ConfigItems.itemJarFilled || heldItem.isItemEqual(new ItemStack(ConfigBlocks.blockJar, 1, 0)) || heldItem.isItemEqual(new ItemStack(ConfigBlocks.blockJar, 1, 3)))) {
                  boolean doit = false;
                  ItemStack drop = null;
                  if (!heldItem.isItemEqual(new ItemStack(ConfigBlocks.blockJar, 1, 0)) && !heldItem.isItemEqual(new ItemStack(ConfigBlocks.blockJar, 1, 3))) {
                     drop = heldItem;
                     if ((((ItemJarFilled) drop.getItem()).getAspects(drop) == null || ((ItemJarFilled) drop.getItem()).getAspects(drop).visSize() == 0 || ((ItemJarFilled) drop.getItem()).getAspects(drop).getAmount(((TileAlembic) te).aspect) > 0) && (((ItemJarFilled) drop.getItem()).getFilter(drop) == null || ((ItemJarFilled) drop.getItem()).getFilter(drop) == ((TileAlembic) te).aspect)) {
                        int amount = Math.min(((ItemJarFilled) drop.getItem()).getAspects(drop) == null ? 64 : 64 - ((ItemJarFilled) drop.getItem()).getAspects(drop).visSize(), ((TileAlembic) te).amount);
                        if (drop.getItemDamage() == 3) {
                           amount = ((TileAlembic) te).amount;
                        }

                        if (amount > 0) {
                           ((TileAlembic) te).amount -= amount;
                           AspectList as = ((ItemJarFilled) drop.getItem()).getAspects(drop);
                           if (as == null) {
                              as = new AspectList();
                           }

                           as.add(((TileAlembic) te).aspect, amount);
                           if (as.getAmount(((TileAlembic) te).aspect) > 64) {
                              int q = as.getAmount(((TileAlembic) te).aspect) - 64;
                              as.reduce(((TileAlembic) te).aspect, q);
                           }

                           ((ItemJarFilled) drop.getItem()).setAspects(drop, as);
                           if (((TileAlembic) te).amount <= 0) {
                              ((TileAlembic) te).aspect = null;
                           }

                           doit = true;
                           player.inventory.setInventorySlotContents(player.inventory.currentItem, drop);
                        }
                     }
                  } else {
                     drop = new ItemStack(ConfigItems.itemJarFilled, 1, heldItem.getItemDamage());
                     doit = true;
                     ((ItemJarFilled) drop.getItem()).setAspects(drop, (new AspectList()).add(((TileAlembic) te).aspect, ((TileAlembic) te).amount));
                     ((TileAlembic) te).amount = 0;
                     ((TileAlembic) te).aspect = null;
                     heldItem.shrink(1);
                     if (!addToPlayerInventoryBiased(player.inventory, drop) && !world.isRemote) {
                        world.spawnEntity(new EntityItem(world, player.posX, player.posY, player.posZ, drop));
                     }
                  }

                  if (doit) {
                     te.markDirty();
                     { net.minecraft.block.state.IBlockState _bs = world.getBlockState(pos); world.notifyBlockUpdate(pos, _bs, _bs, 3); }
                     if (world.isRemote) {
                        world.playSound(player, pos,
                                SoundEvent.REGISTRY.getObject(new ResourceLocation("minecraft:item.bucket.empty")),
                                SoundCategory.BLOCKS, 0.5F, 1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.3F);
                     }
                  }

                  return true;
               }
            }
         }
      }

      if (metadata == 5) {
         world.setBlockState(pos, this.getStateFromMeta(6), 2);
         world.playEvent(player, 1003, pos, 0);
         return true;
      } else if (metadata == 6) {
         world.setBlockState(pos, this.getStateFromMeta(5), 2);
         world.playEvent(player, 1003, pos, 0);
         return true;
      } else if (world.isRemote) {
         return true;
      } else {
         if (metadata == 10) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileThaumatorium && !player.isSneaking()) {
               player.openGui(Thaumcraft.instance, 3, world, pos.getX(), pos.getY(), pos.getZ());
               return true;
            }
         }

         if (metadata == 11) {
            TileEntity te = world.getTileEntity(pos.down());
            if (te instanceof TileThaumatorium && !player.isSneaking()) {
               player.openGui(Thaumcraft.instance, 3, world, pos.getX(), pos.getY() - 1, pos.getZ());
               return true;
            }
         }

         if ((metadata == 14 || metadata == 2)
                 && !player.isSneaking()
                 && !heldItem.isEmpty() && heldItem.getItem() instanceof ItemShard) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileVisRelay) {
               TileVisRelay tile = (TileVisRelay) te;
               byte c = (byte) heldItem.getItemDamage();
               if (c != tile.color && c != 6) {
                  tile.color = c;
               } else {
                  tile.color = -1;
               }

               tile.removeThisNode();
               tile.nodeRefresh = true;
               tile.markDirty();
               { net.minecraft.block.state.IBlockState _bs = world.getBlockState(pos); world.notifyBlockUpdate(pos, _bs, _bs, 3); }
               world.playSound(null, pos,
                       SoundEvent.REGISTRY.getObject(new ResourceLocation("thaumcraft:crystal")),
                       SoundCategory.BLOCKS, 0.2F, 1.0F);
            }
         }

         return super.onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ);
      }
   }

   public void onPoweredBlockChange(World par1World, BlockPos pos, boolean flag) {
      IBlockState state = par1World.getBlockState(pos);
      int l = state.getBlock().getMetaFromState(state);
      if (l == 5 && flag) {
         par1World.setBlockState(pos, this.getStateFromMeta(6), 2);
         par1World.playEvent(null, 1003, pos, 0);
      } else if (l == 6 && !flag) {
         par1World.setBlockState(pos, this.getStateFromMeta(5), 2);
         par1World.playEvent(null, 1003, pos, 0);
      }
   }

   @Override
   public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase ent, ItemStack stack) {
      int l = MathHelper.floor((double) (ent.rotationYaw * 4.0F / 360.0F) + 0.5) & 3;
      if (stack.getItemDamage() == 1) {
         TileEntity tile = world.getTileEntity(pos);
         if (tile instanceof TileAlembic) {
            if (l == 0) {
               ((TileAlembic) tile).facing = 2;
            }
            if (l == 1) {
               ((TileAlembic) tile).facing = 5;
            }
            if (l == 2) {
               ((TileAlembic) tile).facing = 3;
            }
            if (l == 3) {
               ((TileAlembic) tile).facing = 4;
            }
         }
      }
   }

   @Override
   public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
      IBlockState s = world.getBlockState(pos);
      int md = s.getBlock().getMetaFromState(s);
      if (md == 3) {
         return 11;
      } else if (md == 7) {
         return 15;
      } else {
         if (md == 8) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileArcaneLampGrowth) {
               return ((TileArcaneLampGrowth) te).charges > 0 ? 15 : 8;
            }
         } else if (md == 13) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileArcaneLampFertility) {
               return ((TileArcaneLampFertility) te).charges > 0 ? 15 : 8;
            }
         } else if (md == 14) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileVisRelay) {
               return VisNetHandler.isNodeValid(((TileVisRelay) te).getParent()) ? 10 : 2;
            }
         }

         return super.getLightValue(state, world, pos);
      }
   }

   @Override
   public int damageDropped(IBlockState state) {
      int metadata = state.getBlock().getMetaFromState(state);
      if (metadata == 6) {
         return 5;
      } else {
         return metadata != 10 && metadata != 11 ? metadata : 9;
      }
   }

   @Override
   public TileEntity createTileEntity(World world, IBlockState state) {
      int metadata = state.getBlock().getMetaFromState(state);
      if (metadata == 0) {
         return new TileCrucible();
      } else if (metadata == 5) {
         return new TileGrate();
      } else if (metadata == 6) {
         return new TileGrate();
      } else if (metadata == 1) {
         return new TileAlembic();
      } else if (metadata == 3) {
         return new TileMetalDevice();
      } else if (metadata == 7) {
         return new TileArcaneLamp();
      } else if (metadata == 8) {
         return new TileArcaneLampGrowth();
      } else if (metadata == 9) {
         return new TileMetalDevice();
      } else if (metadata == 10) {
         return new TileThaumatorium();
      } else if (metadata == 11) {
         return new TileThaumatoriumTop();
      } else if (metadata == 12) {
         return new TileBrainbox();
      } else if (metadata == 13) {
         return new TileArcaneLampFertility();
      } else if (metadata == 14) {
         return new TileVisRelay();
      } else {
         return metadata == 2 ? new TileMagicWorkbenchCharger() : super.createTileEntity(world, state);
      }
   }

   @Override
   public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
      if (!world.isRemote) {
         int metadata = state.getBlock().getMetaFromState(state);
         if (metadata == 0) {
            TileCrucible tile = (TileCrucible) world.getTileEntity(pos);
            if (tile != null && entity instanceof EntityItem && !(entity instanceof EntitySpecialItem) && tile.heat > 150 && tile.tank.getFluidAmount() > 0) {
               tile.attemptSmelt((EntityItem) entity);
            } else {
               ++this.delay;
               if (this.delay < 10) {
                  return;
               }

               this.delay = 0;
               if (entity instanceof EntityLivingBase && tile != null && tile.heat > 150 && tile.tank.getFluidAmount() > 0) {
                  entity.attackEntityFrom(DamageSource.IN_FIRE, 1.0F);
                  world.playSound(null, pos,
                          SoundEvent.REGISTRY.getObject(new ResourceLocation("minecraft:random.fizz")),
                          SoundCategory.BLOCKS, 0.4F, 2.0F + world.rand.nextFloat() * 0.4F);
               }
            }
         }
      }
   }

   @Override
   @SideOnly(Side.CLIENT)
   public void randomDisplayTick(IBlockState state, World w, BlockPos pos, Random r) {
      if (r.nextInt(10) == 0) {
         TileEntity te = w.getTileEntity(pos);
         if (te instanceof TileCrucible && ((TileCrucible) te).tank.getFluidAmount() > 0 && ((TileCrucible) te).heat > 150) {
            w.playSound(null, pos,
                    SoundEvent.REGISTRY.getObject(new ResourceLocation("minecraft:liquid.lavapop")),
                    SoundCategory.BLOCKS, 0.1F + r.nextFloat() * 0.1F, 1.2F + r.nextFloat() * 0.2F);
         }
      }
   }

   @Override
   public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) {
      int meta = state.getValue(META);
      if (meta == 2 || meta == 5 || meta == 6) {
         return net.minecraft.util.EnumBlockRenderType.MODEL;
      }
      return net.minecraft.util.EnumBlockRenderType.INVISIBLE;
   }
}
