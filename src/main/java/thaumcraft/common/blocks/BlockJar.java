package thaumcraft.common.blocks;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleManager;
import thaumcraft.client.fx.ParticleSpellCustom;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import tc4tweak.network.NetworkedConfiguration;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssentiaContainerItem;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.tiles.TileJarBrain;
import thaumcraft.common.tiles.TileJarFillable;
import thaumcraft.common.tiles.TileJarFillableVoid;
import thaumcraft.common.tiles.TileJarNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static tc4tweak.modules.blockJar.EntityCollisionBox.SMALLER_PARAMETERS;
import static tc4tweak.modules.blockJar.EntityCollisionBox.VANILLA_PARAMETERS;

public class BlockJar extends BlockContainer {
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


   /** Retained for renderer compatibility; populated by the JSON/model system when ported. */
   public TextureAtlasSprite iconLiquid = null;

   public BlockJar() {
      super(Material.GLASS);
      this.setHardness(0.3F);
      this.setSoundType(new CustomStepSound("jar", 1.0F, 1.0F));
      this.setCreativeTab(Thaumcraft.tabTC);
      this.setLightLevel(0.66F);
   }

   @Override
   @SideOnly(Side.CLIENT)
   public void getSubBlocks(CreativeTabs par2CreativeTabs, NonNullList<ItemStack> par3List) {
      par3List.add(new ItemStack(this, 1, 0));
      par3List.add(new ItemStack(this, 1, 1));
      par3List.add(new ItemStack(this, 1, 3));
   }

   @Override
   public TileEntity createTileEntity(World world, IBlockState state) {
      int metadata = state.getBlock().getMetaFromState(state);
      if (metadata == 0) {
         return new TileJarFillable();
      } else if (metadata == 1) {
         return new TileJarBrain();
      } else if (metadata == 2) {
         return new TileJarNode();
      } else {
         return metadata == 3 ? new TileJarFillableVoid() : null;
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
   @SideOnly(Side.CLIENT)
   public net.minecraft.util.BlockRenderLayer getRenderLayer() {
      return net.minecraft.util.BlockRenderLayer.TRANSLUCENT;
   }

   @Override
   public void onBlockHarvested(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
      int metadata = state.getBlock().getMetaFromState(state);
      Block.spawnAsEntity(world, pos, new ItemStack(this, 1, metadata));
      super.onBlockHarvested(world, pos, state, player);
   }

   public static void playJarSound(World world, BlockPos pos, float volume) {
      world.playSound(null, pos,
            thaumcraft.common.lib.SoundsTC.get("thaumcraft:jar"),
            net.minecraft.util.SoundCategory.BLOCKS, volume, 1.0F);
   }

   /** Convenience overload kept for existing callers that still pass int coords. */
   public static void playJarSound(World world, int x, int y, int z, float volume) {
      playJarSound(world, new BlockPos(x, y, z), volume);
   }

   @Override
   public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
      List<ItemStack> drops = new ArrayList<>();
      int md = state.getBlock().getMetaFromState(state);
      if (md != 0 && md != 3) {
         if (md == 2) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileJarNode && ((TileJarNode) te).drop && ((TileJarNode) te).getAspects() != null) {
               ItemStack drop = new ItemStack(ConfigItems.itemJarNode);
               ((ItemJarNode) drop.getItem()).setAspects(drop, ((TileJarNode) te).getAspects().copy());
               ((ItemJarNode) drop.getItem()).setNodeAttributes(drop, ((TileJarNode) te).getNodeType(), ((TileJarNode) te).getNodeModifier(), ((TileJarNode) te).getId());
               drops.add(drop);
            }
            return drops;
         } else {
            return super.getDrops(world, pos, state, fortune);
         }
      } else {
         TileEntity te = world.getTileEntity(pos);
         if (te instanceof TileJarFillable) {
            ItemStack drop = new ItemStack(ConfigItems.itemJarFilled);
            if (((TileJarFillable) te).amount <= 0 && ((TileJarFillable) te).aspectFilter == null) {
               drop = new ItemStack(this);
            }

            if (te instanceof TileJarFillableVoid) {
               drop.setItemDamage(3);
            }

            if (((TileJarFillable) te).amount > 0) {
               ((ItemJarFilled) drop.getItem()).setAspects(drop, (new AspectList()).add(((TileJarFillable) te).aspect, ((TileJarFillable) te).amount));
            }

            if (((TileJarFillable) te).aspectFilter != null) {
               if (!drop.hasTagCompound()) {
                  drop.setTagCompound(new NBTTagCompound());
               }
               drop.getTagCompound().setString("AspectFilter", ((TileJarFillable) te).aspectFilter.getTag());
            }

            drops.add(drop);
         }
         return drops;
      }
   }

   @Override
   public int damageDropped(IBlockState state) {
      return state.getBlock().getMetaFromState(state);
   }

   @Override
   @SideOnly(Side.CLIENT)
   public boolean addDestroyEffects(World world, BlockPos pos, ParticleManager effectRenderer) {
      IBlockState state = world.getBlockState(pos);
      int meta = state.getBlock().getMetaFromState(state);
      return meta != 15;
   }

   @Override
   public void breakBlock(World world, BlockPos pos, IBlockState state) {
      int md = state.getBlock().getMetaFromState(state);
      if (md == 1 && !world.isRemote) {
         TileEntity te = world.getTileEntity(pos);
         if (te instanceof TileJarBrain) {
            int xp = ((TileJarBrain) te).xp;
            while (xp > 0) {
               int var2 = EntityXPOrb.getXPSplit(xp);
               xp -= var2;
               world.spawnEntity(new EntityXPOrb(world, pos.getX(), pos.getY(), pos.getZ(), var2));
            }
         }
      }
      super.breakBlock(world, pos, state);
   }

   @Override
   public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase ent, ItemStack stack) {
      int l = MathHelper.floor((double) (ent.rotationYaw * 4.0F / 360.0F) + (double) 0.5F) & 3;
      TileEntity tile = world.getTileEntity(pos);
      if (tile instanceof TileJarFillable) {
         if (l == 0) {
            ((TileJarFillable) tile).facing = 2;
         }
         if (l == 1) {
            ((TileJarFillable) tile).facing = 5;
         }
         if (l == 2) {
            ((TileJarFillable) tile).facing = 3;
         }
         if (l == 3) {
            ((TileJarFillable) tile).facing = 4;
         }
      }
   }

   @Override
   public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
                                   EntityPlayer player, EnumHand hand, EnumFacing facing,
                                   float hitX, float hitY, float hitZ) {
      int side = facing.getIndex();
      TileEntity te = world.getTileEntity(pos);
      if (te instanceof TileJarBrain) {
         ((TileJarBrain) te).eatDelay = 40;
         if (!world.isRemote) {
            int var6 = world.rand.nextInt(Math.min(((TileJarBrain) te).xp + 1, 64));
            if (var6 > 0) {
               ((TileJarBrain) te).xp -= var6;
               int xp = var6;
               while (xp > 0) {
                  int var2 = EntityXPOrb.getXPSplit(xp);
                  xp -= var2;
                  world.spawnEntity(new EntityXPOrb(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, var2));
               }
               { net.minecraft.block.state.IBlockState _bs = world.getBlockState(pos); world.notifyBlockUpdate(pos, _bs, _bs, 3); }
               te.markDirty();
            }
         } else {
            playJarSound(world, pos, .2f);
         }
      }
      if (te instanceof TileJarFillable) {
         TileJarFillable fillableJar = (TileJarFillable) te;
         ItemStack heldItem = player.getHeldItem(hand);
         if (player.isSneaking()) {
            if (fillableJar.aspectFilter != null && side == fillableJar.facing) {
               // remove and drop jar filter
               fillableJar.aspectFilter = null;
               { net.minecraft.block.state.IBlockState _bs = world.getBlockState(pos); world.notifyBlockUpdate(pos, _bs, _bs, 3); }
               te.markDirty();
               if (world.isRemote) {
                  playJarSound(world, pos, 1.f);
               } else {
                  EnumFacing fd = EnumFacing.byIndex(side);
                  world.spawnEntity(new EntityItem(world,
                        (float) pos.getX() + 0.5F + (float) fd.getXOffset() / 3.0F,
                        (float) pos.getY() + 0.5F,
                        (float) pos.getZ() + 0.5F + (float) fd.getZOffset() / 3.0F,
                        new ItemStack(ConfigItems.itemResource, 1, 13)));
               }
            } else if (heldItem.isEmpty()) {
               // clear jar
               fillableJar.amount = 0;
               if (((TileJarFillable) te).aspectFilter == null) {
                  fillableJar.aspect = null;
               }
               if (world.isRemote) {
                  playJarSound(world, pos, .4f);
                  world.playSound(player, pos,
                        thaumcraft.common.lib.SoundsTC.get("minecraft:entity.generic.swim"),
                        net.minecraft.util.SoundCategory.BLOCKS,
                        0.5F, 1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.3F);
               }
            }
         } else if (!heldItem.isEmpty()
               && fillableJar.aspectFilter == null
               && heldItem.getItem() == ConfigItems.itemResource
               && heldItem.getItemDamage() == 13) {
            if (((TileJarFillable) te).amount == 0
                  && ((IEssentiaContainerItem) heldItem.getItem()).getAspects(heldItem) == null) {
               return true;
            }
            if (((TileJarFillable) te).amount == 0
                  && ((IEssentiaContainerItem) heldItem.getItem()).getAspects(heldItem) != null) {
               fillableJar.aspect = ((IEssentiaContainerItem) heldItem.getItem()).getAspects(heldItem).getAspects()[0];
            }
            heldItem.shrink(1);
            this.onBlockPlacedBy(world, pos, state, player, null);
            fillableJar.aspectFilter = fillableJar.aspect;
            { net.minecraft.block.state.IBlockState _bs = world.getBlockState(pos); world.notifyBlockUpdate(pos, _bs, _bs, 3); }
            te.markDirty();
            if (world.isRemote) {
               playJarSound(world, pos, .4f);
            }
         }
      }
      return true;
   }

   @Override
   public TileEntity createNewTileEntity(World var1, int md) {
      return null;
   }

   @Override
   public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
      return new AxisAlignedBB(0.1875, 0.0, 0.1875, 0.8125, 0.75, 0.8125);
   }

   @Override
   public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos,
                                     AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes,
                                     Entity entityIn, boolean isActualState) {
      float[] params = NetworkedConfiguration.isSmallerJar() ? SMALLER_PARAMETERS : VANILLA_PARAMETERS;
      AxisAlignedBB box = new AxisAlignedBB(params[0], params[1], params[2], params[3], params[4], params[5]);
      addCollisionBoxToList(pos, entityBox, collidingBoxes, box);
   }

   @Override
   public float getEnchantPowerBonus(World world, BlockPos pos) {
      TileEntity te = world.getTileEntity(pos);
      return te instanceof TileJarBrain ? 2.0F : super.getEnchantPowerBonus(world, pos);
   }

   @Override
   public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
      int md = state.getBlock().getMetaFromState(state);
      return md == 2 ? 11 : super.getLightValue(state, world, pos);
   }

   @Override
   @SideOnly(Side.CLIENT)
   public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
      TileEntity tile = world.getTileEntity(pos);
      if (tile instanceof TileJarBrain && ((TileJarBrain) tile).xp >= ((TileJarBrain) tile).xpMax) {
         double xx = pos.getX() + 0.3 + (double) (rand.nextFloat() * 0.4F);
         double yy = pos.getY() + 0.9;
         double zz = pos.getZ() + 0.3 + (double) (rand.nextFloat() * 0.4F);
         ParticleSpellCustom var21 = new ParticleSpellCustom(world, xx, yy, zz, 0.0F, 0.0F, 0.0F);
         var21.setAlphaF(0.5F);
         var21.setRBGColorF(0.0F, 0.4F + world.rand.nextFloat() * 0.1F, 0.3F + world.rand.nextFloat() * 0.2F);
         thaumcraft.client.fx.ParticleEngine.instance.addEffect(world, var21);
      }
   }

   @Override
   public boolean hasComparatorInputOverride(IBlockState state) {
      return true;
   }

   @Override
   public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos) {
      TileEntity tile = world.getTileEntity(pos);
      if (tile instanceof TileJarBrain) {
         float r = (float) ((TileJarBrain) tile).xp / (float) ((TileJarBrain) tile).xpMax;
         return MathHelper.floor(r * 14.0F) + (((TileJarBrain) tile).xp > 0 ? 1 : 0);
      } else if (tile instanceof TileJarFillable) {
         float r = (float) ((TileJarFillable) tile).amount / (float) ((TileJarFillable) tile).maxAmount;
         return MathHelper.floor(r * 14.0F) + (((TileJarFillable) tile).amount > 0 ? 1 : 0);
      } else {
         return super.getComparatorInputOverride(state, world, pos);
      }
   }

   @Override
   public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) {
      return net.minecraft.util.EnumBlockRenderType.MODEL;
   }
}
