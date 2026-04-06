package thaumcraft.common.blocks;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraft.util.EnumFacing;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.IEssentiaContainerItem;
import thaumcraft.client.renderers.block.BlockRenderer;
import thaumcraft.codechicken.lib.raytracer.IndexedCuboid6;
import thaumcraft.codechicken.lib.raytracer.RayTracer;
import thaumcraft.codechicken.lib.vec.BlockCoord;
import thaumcraft.codechicken.lib.vec.Vector3;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.items.relics.ItemResonator;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumcraft.common.tiles.*;

import java.util.LinkedList;
import java.util.List;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class BlockTube extends BlockContainer {
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

   public TextureAtlasSprite[] icon = new TextureAtlasSprite[8];
   public TextureAtlasSprite iconValve;
   private RayTracer rayTracer = new RayTracer();

   public BlockTube() {
      super(Material.IRON);
      this.setHardness(0.5F);
      this.setResistance(5.0F);
      // setStepSound removed in 1.12.2 — sound is now data-driven
      // setBlockBounds removed in 1.12.2 — use getBoundingBox
      this.setCreativeTab(Thaumcraft.tabTC);
   }

   // registerBlockIcons removed — textures are handled by JSON models in 1.12.2
   // getIcon removed — textures are handled by JSON models in 1.12.2

   @Override
   @SideOnly(Side.CLIENT)
   public void getSubBlocks(CreativeTabs par2CreativeTabs, NonNullList<ItemStack> par3List) {
      par3List.add(new ItemStack(this, 1, 0));
      par3List.add(new ItemStack(this, 1, 1));
      par3List.add(new ItemStack(this, 1, 2));
      par3List.add(new ItemStack(this, 1, 3));
      par3List.add(new ItemStack(this, 1, 4));
      par3List.add(new ItemStack(this, 1, 5));
      par3List.add(new ItemStack(this, 1, 6));
      par3List.add(new ItemStack(this, 1, 7));
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
   @SideOnly(Side.CLIENT)
   public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World world, BlockPos pos) {
      int metadata = state.getBlock().getMetaFromState(state);
      boolean noDoodads = Minecraft.getMinecraft().player == null
            || Minecraft.getMinecraft().player.getHeldItemMainhand().isEmpty()
            || (!(Minecraft.getMinecraft().player.getHeldItemMainhand().getItem() instanceof ItemWandCasting)
                  && !(Minecraft.getMinecraft().player.getHeldItemMainhand().getItem() instanceof ItemResonator));
      if ((metadata == 0 || metadata == 1 || metadata == 3 || metadata == 5 || metadata == 6) && noDoodads) {
         float minx = BlockRenderer.W6;
         float maxx = BlockRenderer.W10;
         float miny = BlockRenderer.W5;
         float maxy = BlockRenderer.W11;
         float minz = BlockRenderer.W5;
         float maxz = BlockRenderer.W11;

         for (int side = 0; side < 6; ++side) {
            EnumFacing fd = EnumFacing.byIndex(side);
            TileEntity te = ThaumcraftApiHelper.getConnectableTile(world, pos.getX(), pos.getY(), pos.getZ(), fd);
            if (te != null) {
               switch (side) {
                  case 0: miny = 0.0F; break;
                  case 1: maxy = 1.0F; break;
                  case 2: minz = 0.0F; break;
                  case 3: maxz = 1.0F; break;
                  case 4: minx = 0.0F; break;
                  case 5: maxx = 1.0F; break;
               }
            }
         }
         return new AxisAlignedBB(minx, miny, minz, maxx, maxy, maxz).offset(pos);
      }

      if (metadata == 4 && noDoodads) {
         return new AxisAlignedBB(0.25, 0.25, 0.25, 0.75, 0.75, 0.75).offset(pos);
      }

      if (metadata == 7) {
         return new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0).offset(pos);
      }

      return super.getSelectedBoundingBox(state, world, pos);
   }

   @Override
   public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
      int metadata = state.getBlock().getMetaFromState(state);
      if (metadata == 2) {
         return new AxisAlignedBB(0.25, 0.0, 0.25, 0.75, 1.0, 0.75);
      } else if (metadata == 7) {
         return FULL_BLOCK_AABB;
      }
      return FULL_BLOCK_AABB;
   }

   @Override
   public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos,
         AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity par7Entity, boolean isActualState) {
      int metadata = state.getBlock().getMetaFromState(state);
      if (metadata != 0 && metadata != 1 && metadata != 3 && metadata != 4 && metadata != 5 && metadata != 6) {
         addCollisionBoxToList(pos, entityBox, collidingBoxes, FULL_BLOCK_AABB);
      } else {
         float minx = BlockRenderer.W6;
         float maxx = BlockRenderer.W10;
         float miny = BlockRenderer.W6;
         float maxy = BlockRenderer.W10;
         float minz = BlockRenderer.W6;
         float maxz = BlockRenderer.W10;

         for (int side = 0; side < 6; ++side) {
            EnumFacing fd = EnumFacing.byIndex(side);
            TileEntity te = ThaumcraftApiHelper.getConnectableTile(world, pos.getX(), pos.getY(), pos.getZ(), fd);
            if (te != null) {
               switch (side) {
                  case 0: miny = 0.0F; break;
                  case 1: maxy = 1.0F; break;
                  case 2: minz = 0.0F; break;
                  case 3: maxz = 1.0F; break;
                  case 4: minx = 0.0F; break;
                  case 5: maxx = 1.0F; break;
               }
            }
         }
         addCollisionBoxToList(pos, entityBox, collidingBoxes,
               new AxisAlignedBB(minx, miny, minz, maxx, maxy, maxz));
      }
   }

   @Override
   public int damageDropped(IBlockState state) {
      return state.getBlock().getMetaFromState(state);
   }

   @Override
   public TileEntity createTileEntity(World world, IBlockState state) {
      int metadata = state.getBlock().getMetaFromState(state);
      if (metadata == 0) {
         return new TileTube();
      } else if (metadata == 1) {
         return new TileTubeValve();
      } else if (metadata == 2) {
         return new TileCentrifuge();
      } else if (metadata == 3) {
         return new TileTubeFilter();
      } else if (metadata == 4) {
         return new TileTubeBuffer();
      } else if (metadata == 5) {
         return new TileTubeRestrict();
      } else if (metadata == 6) {
         return new TileTubeOneway();
      } else {
         return metadata == 7 ? new TileEssentiaCrystalizer() : super.createTileEntity(world, state);
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
      if (te instanceof TileTubeBuffer) {
         float var10000 = (float)((TileTubeBuffer)te).aspects.visSize();
         ((TileTubeBuffer)te).getClass();
         float r = var10000 / 8.0F;
         return MathHelper.floor(r * 14.0F) + (((TileTubeBuffer)te).aspects.visSize() > 0 ? 1 : 0);
      } else {
         return 0;
      }
   }

   @Override
   public void breakBlock(World world, BlockPos pos, IBlockState state) {
      TileEntity te = world.getTileEntity(pos);
      if (te instanceof TileTubeFilter && ((TileTubeFilter) te).aspectFilter != null && !world.isRemote) {
         world.spawnEntity(new EntityItem(world,
               pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
               new ItemStack(ConfigItems.itemResource, 1, 13)));
      }
      super.breakBlock(world, pos, state);
   }

   @Override
   public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
         EnumHand hand, EnumFacing facing, float par7, float par8, float par9) {
      int metadata = state.getBlock().getMetaFromState(state);
      ItemStack heldItem = player.getHeldItem(hand);

      if (metadata == 1) {
         if (!heldItem.isEmpty() && (heldItem.getItem() instanceof ItemWandCasting
               || heldItem.getItem() instanceof ItemResonator
               || heldItem.getItem() == Item.getItemFromBlock(this))) {
            return false;
         }

         TileEntity te = world.getTileEntity(pos);
         if (te instanceof TileTubeValve) {
            ((TileTubeValve)te).allowFlow = !((TileTubeValve)te).allowFlow;
            { net.minecraft.block.state.IBlockState _bs = world.getBlockState(pos); world.notifyBlockUpdate(pos, _bs, _bs, 3); }
            if (!world.isRemote) {
               world.playSound(null, pos,
                     net.minecraft.util.SoundEvent.REGISTRY.getObject(
                           new net.minecraft.util.ResourceLocation("thaumcraft", "squeek")),
                     net.minecraft.util.SoundCategory.BLOCKS,
                     0.7F, 0.9F + world.rand.nextFloat() * 0.2F);
            }
            return true;
         }
      }

      if (metadata == 3) {
         TileEntity te = world.getTileEntity(pos);
         if (te instanceof TileTubeFilter && player.isSneaking()
               && ((TileTubeFilter) te).aspectFilter != null) {
            ((TileTubeFilter)te).aspectFilter = null;
            { net.minecraft.block.state.IBlockState _bs = world.getBlockState(pos); world.notifyBlockUpdate(pos, _bs, _bs, 3); }
            if (!world.isRemote) {
               world.spawnEntity(new EntityItem(world,
                     pos.getX() + 0.5 + (double)facing.getXOffset() / 3.0,
                     pos.getY() + 0.5,
                     pos.getZ() + 0.5 + (double)facing.getZOffset() / 3.0,
                     new ItemStack(ConfigItems.itemResource, 1, 13)));
            }
            return true;
         }

         if (te instanceof TileTubeFilter && !heldItem.isEmpty()
               && ((TileTubeFilter) te).aspectFilter == null
               && heldItem.getItem() == ConfigItems.itemResource
               && heldItem.getItemDamage() == 13) {
            if (((IEssentiaContainerItem) heldItem.getItem()).getAspects(heldItem) != null) {
               ((TileTubeFilter)te).aspectFilter =
                     ((IEssentiaContainerItem) heldItem.getItem()).getAspects(heldItem).getAspects()[0];
               heldItem.shrink(1);
               { net.minecraft.block.state.IBlockState _bs = world.getBlockState(pos); world.notifyBlockUpdate(pos, _bs, _bs, 3); }
            }
            return true;
         }
      }

      return super.onBlockActivated(world, pos, state, player, hand, facing, par7, par8, par9);
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void onBlockHighlight(DrawBlockHighlightEvent event) {
      RayTraceResult target = event.getTarget();
      EntityPlayer player = event.getPlayer();
      if (target.typeOfHit == RayTraceResult.Type.BLOCK) {
         BlockPos hitPos = target.getBlockPos();
         IBlockState hitState = player.world.getBlockState(hitPos);
         if (hitState.getBlock() == this
               && hitState.getBlock().getMetaFromState(hitState) != 7) {
            ItemStack held = player.getHeldItemMainhand();
            if (!held.isEmpty() && (held.getItem() instanceof ItemWandCasting
                  || held.getItem() instanceof ItemResonator)) {
               RayTracer.retraceBlock(player.world, player, hitPos.getX(), hitPos.getY(), hitPos.getZ());
            }
         }
      }
   }

   @Override
   public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d start, Vec3d end) {
      TileEntity tile = world.getTileEntity(pos);
      if ((tile instanceof TileTube || tile instanceof TileTubeBuffer)) {
         List<IndexedCuboid6> cuboids = new LinkedList<>();
         if (tile instanceof TileTube) {
            ((TileTube)tile).addTraceableCuboids(cuboids);
         } else if (tile instanceof TileTubeBuffer) {
            ((TileTubeBuffer)tile).addTraceableCuboids(cuboids);
         }
         return this.rayTracer.rayTraceCuboids(new Vector3(start), new Vector3(end), cuboids, pos, this);
      } else {
         return super.collisionRayTrace(state, world, pos, start, end);
      }
   }

   @Override
   public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) {
      return net.minecraft.util.EnumBlockRenderType.INVISIBLE;
   }
}
