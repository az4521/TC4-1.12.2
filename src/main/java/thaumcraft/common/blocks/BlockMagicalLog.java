package thaumcraft.common.blocks;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.nodes.INode;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.items.ItemWispEssence;
import thaumcraft.common.tiles.TileNode;

import java.util.List;
import java.util.Random;

public class BlockMagicalLog extends BlockRotatedPillar {
   public static final String[] woodType = new String[]{"greatwood", "silverwood", "silverwoodknot"};
   public static final net.minecraft.block.properties.PropertyEnum<LogVariant> VARIANT =
         net.minecraft.block.properties.PropertyEnum.create("variant", LogVariant.class);

   public BlockMagicalLog() {
      super(Material.WOOD);
      this.setDefaultState(this.blockState.getBaseState()
            .withProperty(AXIS, EnumFacing.Axis.Y)
            .withProperty(VARIANT, LogVariant.GREATWOOD));
      this.setCreativeTab(Thaumcraft.tabTC);
      this.setHardness(2.5F);
      this.setSoundType(net.minecraft.block.SoundType.WOOD);
   }

   @Override
   protected net.minecraft.block.state.BlockStateContainer createBlockState() {
      return new net.minecraft.block.state.BlockStateContainer(this, AXIS, VARIANT);
   }

   @Override
   public IBlockState getStateFromMeta(int meta) {
      int variantMeta = meta & 3;
      int axisMeta = (meta >> 2) & 3;
      EnumFacing.Axis axis = axisMeta == 1 ? EnumFacing.Axis.X : axisMeta == 2 ? EnumFacing.Axis.Z : EnumFacing.Axis.Y;
      LogVariant variant = variantMeta < LogVariant.values().length ? LogVariant.values()[variantMeta] : LogVariant.GREATWOOD;
      return this.getDefaultState().withProperty(AXIS, axis).withProperty(VARIANT, variant);
   }

   @Override
   public int getMetaFromState(IBlockState state) {
      int meta = state.getValue(VARIANT).ordinal();
      EnumFacing.Axis axis = state.getValue(AXIS);
      if (axis == EnumFacing.Axis.X) meta |= 4;
      else if (axis == EnumFacing.Axis.Z) meta |= 8;
      return meta;
   }

   public enum LogVariant implements net.minecraft.util.IStringSerializable {
      GREATWOOD("greatwood"), SILVERWOOD("silverwood");
      private final String name;
      LogVariant(String name) { this.name = name; }
      @Override public String getName() { return name; }
   }

   @Override
   public int quantityDropped(Random par1Random) {
      return 1;
   }

   @Override
   public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
      int x = pos.getX();
      int y = pos.getY();
      int z = pos.getZ();
      byte b0 = 4;
      int i1 = b0 + 1;

      if (worldIn.isAreaLoaded(pos.add(-i1, -i1, -i1), pos.add(i1, i1, i1))) {
         for (int j1 = -b0; j1 <= b0; ++j1) {
            for (int k1 = -b0; k1 <= b0; ++k1) {
               for (int l1 = -b0; l1 <= b0; ++l1) {
                  BlockPos np = new BlockPos(x + j1, y + k1, z + l1);
                  IBlockState nstate = worldIn.getBlockState(np);
                  if (nstate.getBlock().isLeaves(nstate, worldIn, np)) {
                     nstate.getBlock().beginLeavesDecay(nstate, worldIn, np);
                  }
               }
            }
         }
      }
      super.breakBlock(worldIn, pos, state);
   }

   @Override
   public void onBlockHarvested(World par1World, BlockPos pos, IBlockState state, EntityPlayer par6EntityPlayer) {
      int meta = state.getBlock().getMetaFromState(state);
      if (limitToValidMetadata(meta) == 2 && !par1World.isRemote) {
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

   @Override
   public int damageDropped(IBlockState state) {
      int par1 = state.getBlock().getMetaFromState(state);
      return (par1 & 3) == 2 ? 1 : par1 & 3;
   }

   public static int limitToValidMetadata(int par0) {
      return par0 & 3;
   }

   @Override
   @SideOnly(Side.CLIENT)
   public void getSubBlocks(CreativeTabs par2CreativeTabs, NonNullList<ItemStack> par3List) {
      par3List.add(new ItemStack(this, 1, 0));
      par3List.add(new ItemStack(this, 1, 1));
   }

   @Override
   public boolean canSustainLeaves(IBlockState state, IBlockAccess world, BlockPos pos) {
      return true;
   }

   @Override
   public boolean canBeReplacedByLeaves(IBlockState state, IBlockAccess world, BlockPos pos) {
      return false;
   }

   @Override
   public boolean isWood(IBlockAccess world, BlockPos pos) {
      return true;
   }

   @Override
   public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
      IBlockState s = world.getBlockState(pos);
      int meta = s.getBlock().getMetaFromState(s);
      return (meta & 2) == 2 ? 7 : super.getLightValue(state, world, pos);
   }

   @Override
   public boolean hasTileEntity(IBlockState state) {
      int metadata = state.getBlock().getMetaFromState(state);
      return limitToValidMetadata(metadata) == 2 || super.hasTileEntity(state);
   }

   @Override
   public TileEntity createTileEntity(World world, IBlockState state) {
      int metadata = state.getBlock().getMetaFromState(state);
      return limitToValidMetadata(metadata) == 2 ? new TileNode() : super.createTileEntity(world, state);
   }

   @Override
   public boolean addDestroyEffects(World world, BlockPos pos, ParticleManager effectRenderer) {
      IBlockState state = world.getBlockState(pos);
      int meta = state.getBlock().getMetaFromState(state);
      if (limitToValidMetadata(meta) == 2) {
         Thaumcraft.proxy.burst(world, (double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5, 1.0F);
         world.playSound(null, pos,
                 SoundEvent.REGISTRY.getObject(new ResourceLocation("thaumcraft:craftfail")),
                 SoundCategory.BLOCKS, 1.0F, 1.0F);
      }
      return super.addDestroyEffects(world, pos, effectRenderer);
   }

   @Override
   public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
      return 5;
   }

   @Override
   public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
      return 5;
   }
}
