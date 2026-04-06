package thaumcraft.common.blocks;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;
import net.minecraft.util.EnumFacing;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigBlocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BlockMagicalLeaves extends Block implements IShearable {
   public static final net.minecraft.block.properties.PropertyEnum<LeafVariant> VARIANT =
         net.minecraft.block.properties.PropertyEnum.create("variant", LeafVariant.class);

   public enum LeafVariant implements net.minecraft.util.IStringSerializable {
      GREATWOOD(0, "greatwood"), SILVERWOOD(1, "silverwood");
      private final int meta;
      private final String name;
      LeafVariant(int meta, String name) { this.meta = meta; this.name = name; }
      public int getMeta() { return meta; }
      @Override public String getName() { return name; }
   }

   @Override
   protected net.minecraft.block.state.BlockStateContainer createBlockState() {
      return new net.minecraft.block.state.BlockStateContainer(this, VARIANT);
   }

   @Override
   public net.minecraft.block.state.IBlockState getStateFromMeta(int meta) {
      return this.getDefaultState().withProperty(VARIANT, (meta & 1) == 0 ? LeafVariant.GREATWOOD : LeafVariant.SILVERWOOD);
   }

   @Override
   public int getMetaFromState(net.minecraft.block.state.IBlockState state) {
      return state.getValue(VARIANT).getMeta();
   }

    public static final String[] leafType = new String[]{"greatwood", "silverwood"};
    int[] adjacentTreeBlocks;

    public BlockMagicalLeaves() {
        super(Material.LEAVES);
        this.setTickRandomly(true);
        this.setCreativeTab(Thaumcraft.tabTC);
        this.setHardness(0.2F);
        this.setLightOpacity(1);
        this.setSoundType(net.minecraft.block.SoundType.PLANT);
    }

    // createBlockState, getStateFromMeta, getMetaFromState defined above via VARIANT property

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(CreativeTabs par2CreativeTabs, NonNullList<ItemStack> par3List) {
        par3List.add(new ItemStack(this, 1, 0));
        par3List.add(new ItemStack(this, 1, 1));
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        IBlockState s = world.getBlockState(pos);
        int meta = s.getBlock().getMetaFromState(s);
        return (meta & 1) == 1 ? 7 : super.getLightValue(state, world, pos);
    }

    @Override
    public void breakBlock(World par1World, BlockPos pos, IBlockState state) {
        int par2 = pos.getX();
        int par3 = pos.getY();
        int par4 = pos.getZ();
        byte var7 = 1;
        int var8 = var7 + 1;
        if (par1World.isAreaLoaded(pos.add(-var8, -var8, -var8), pos.add(var8, var8, var8))) {
            for (int var9 = -var7; var9 <= var7; ++var9) {
                for (int var10 = -var7; var10 <= var7; ++var10) {
                    for (int var11 = -var7; var11 <= var7; ++var11) {
                        BlockPos np = new BlockPos(par2 + var9, par3 + var10, par4 + var11);
                        IBlockState nstate = par1World.getBlockState(np);
                        Block var12 = nstate.getBlock();
                        if (var12 != Blocks.AIR) {
                            var12.beginLeavesDecay(nstate, par1World, np);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void updateTick(World par1World, BlockPos pos, IBlockState state, Random par5Random) {
        if (!par1World.isRemote) {
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();
            int metadata = par1World.getBlockState(pos).getBlock().getMetaFromState(par1World.getBlockState(pos));
            if ((metadata & 8) != 0 && (metadata & 4) == 0) {
                byte chunksRadius = 4;
                int chunksRange = chunksRadius + 1;
                byte var9 = 32;
                int var10 = var9 * var9;
                int var11 = var9 / 2;
                if (this.adjacentTreeBlocks == null) {
                    this.adjacentTreeBlocks = new int[var9 * var9 * var9];
                }

                if (par1World.isAreaLoaded(
                        pos.add(-chunksRange, -chunksRange, -chunksRange),
                        pos.add(chunksRange, chunksRange, chunksRange))) {
                    for (int var12 = -chunksRadius; var12 <= chunksRadius; ++var12) {
                        for (int var13 = -chunksRadius; var13 <= chunksRadius; ++var13) {
                            for (int var14 = -chunksRadius; var14 <= chunksRadius; ++var14) {
                                BlockPos np = new BlockPos(x + var12, y + var13, z + var14);
                                IBlockState nstate = par1World.getBlockState(np);
                                Block block = nstate.getBlock();

                                int i = (var12 + var11) * var10 + (var13 + var11) * var9 + var14 + var11;
                                if (block.canSustainLeaves(nstate, par1World, np)) {
                                    this.adjacentTreeBlocks[i] = 0;
                                } else if (block.isLeaves(nstate, par1World, np)) {
                                    this.adjacentTreeBlocks[i] = -2;
                                } else {
                                    this.adjacentTreeBlocks[i] = -1;
                                }
                            }
                        }
                    }

                    for (int var17 = 1; var17 <= 4; ++var17) {
                        for (int var13 = -chunksRadius; var13 <= chunksRadius; ++var13) {
                            for (int var14 = -chunksRadius; var14 <= chunksRadius; ++var14) {
                                for (int var21 = -chunksRadius; var21 <= chunksRadius; ++var21) {
                                    if (this.adjacentTreeBlocks[(var13 + var11) * var10 + (var14 + var11) * var9 + var21 + var11] == var17 - 1) {
                                        int i = (var13 + var11 - 1) * var10 + (var14 + var11) * var9 + var21 + var11;
                                        if (this.adjacentTreeBlocks[i] == -2) {
                                            this.adjacentTreeBlocks[i] = var17;
                                        }

                                        int i1 = (var13 + var11 + 1) * var10 + (var14 + var11) * var9 + var21 + var11;
                                        if (this.adjacentTreeBlocks[i1] == -2) {
                                            this.adjacentTreeBlocks[i1] = var17;
                                        }

                                        int i2 = (var13 + var11) * var10 + (var14 + var11 - 1) * var9 + var21 + var11;
                                        if (this.adjacentTreeBlocks[i2] == -2) {
                                            this.adjacentTreeBlocks[i2] = var17;
                                        }

                                        int i3 = (var13 + var11) * var10 + (var14 + var11 + 1) * var9 + var21 + var11;
                                        if (this.adjacentTreeBlocks[i3] == -2) {
                                            this.adjacentTreeBlocks[i3] = var17;
                                        }
                                        int i4 = (var13 + var11) * var10 + (var14 + var11) * var9 + (var21 + var11 - 1);
                                        if (this.adjacentTreeBlocks[i4] == -2) {
                                            this.adjacentTreeBlocks[i4] = var17;
                                        }

                                        int i5 = (var13 + var11) * var10 + (var14 + var11) * var9 + var21 + var11 + 1;
                                        if (this.adjacentTreeBlocks[i5] == -2) {
                                            this.adjacentTreeBlocks[i5] = var17;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                int var12 = this.adjacentTreeBlocks[var11 * var10 + var11 * var9 + var11];
                if (var12 >= 0) {
                    // Still connected to a log — clear the "should decay" bit
                    par1World.setBlockState(pos, this.getStateFromMeta(metadata & -9), 4);
                } else {
                    this.removeLeaves(par1World, pos);
                }
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState state, World par1World, BlockPos pos, Random par5Random) {
        int par2 = pos.getX();
        int par3 = pos.getY();
        int par4 = pos.getZ();
        if (par1World.canSeeSky(pos.up()) && !par1World.isSideSolid(pos.down(), net.minecraft.util.EnumFacing.UP) && par5Random.nextInt(15) == 1) {
            double var6 = (float) par2 + par5Random.nextFloat();
            double var8 = (double) par3 - 0.05;
            double var10 = (float) par4 + par5Random.nextFloat();
            par1World.spawnParticle(net.minecraft.util.EnumParticleTypes.DRIP_WATER, var6, var8, var10, 0.0F, 0.0F, 0.0F);
        }

        int md = par1World.getBlockState(pos).getBlock().getMetaFromState(par1World.getBlockState(pos));
        if ((md & 1) == 1 && par5Random.nextInt(500) == 0) {
            Thaumcraft.proxy.sparkle((float) par2 + 0.5F + par1World.rand.nextFloat() - par1World.rand.nextFloat(), (float) par3 + 0.5F + par1World.rand.nextFloat() - par1World.rand.nextFloat(), (float) par4 + 0.5F + par1World.rand.nextFloat() - par1World.rand.nextFloat(), 2.0F, 7, 0.0F);
        }
    }

    private void removeLeaves(World par1World, BlockPos pos) {
        int meta = par1World.getBlockState(pos).getBlock().getMetaFromState(par1World.getBlockState(pos));
        Block.spawnAsEntity(par1World, pos, new ItemStack(this, 1, meta));
        par1World.setBlockToAir(pos);
    }

    public void dropBlockAsItemWithChance(World par1World, BlockPos pos, IBlockState state, float par6, int par7) {
        int meta = par1World.getBlockState(pos).getBlock().getMetaFromState(par1World.getBlockState(pos));
        if (!par1World.isRemote && (meta & 8) != 0 && (meta & 4) == 0) {
            if ((meta & 1) == 0 && par1World.rand.nextInt(200) == 0) {
                Block.spawnAsEntity(par1World, pos, new ItemStack(ConfigBlocks.blockCustomPlant, 1, 0));
            } else if ((meta & 1) == 1 && par1World.rand.nextInt(250) == 0) {
                Block.spawnAsEntity(par1World, pos, new ItemStack(ConfigBlocks.blockCustomPlant, 1, 1));
            }
        }
    }

    @Override
    public void harvestBlock(World par1World, EntityPlayer par2EntityPlayer, BlockPos pos, IBlockState state, net.minecraft.tileentity.TileEntity te, ItemStack stack) {
        super.harvestBlock(par1World, par2EntityPlayer, pos, state, te, stack);
    }

    @Override
    public int damageDropped(IBlockState state) {
        return state.getBlock().getMetaFromState(state) & 1;
    }

    @Override
    public int quantityDropped(Random par1Random) {
        return 0;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random par2Random, int par3) {
        return Item.getItemById(0);
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
    @net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
    public net.minecraft.util.BlockRenderLayer getRenderLayer() {
        return net.minecraft.util.BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    public boolean isShearable(ItemStack item, IBlockAccess world, BlockPos pos) {
        return true;
    }

    @Override
    public List<ItemStack> onSheared(ItemStack item, IBlockAccess world, BlockPos pos, int fortune) {
        List<ItemStack> ret = new ArrayList<>();
        IBlockState s = world.getBlockState(pos);
        int meta = s.getBlock().getMetaFromState(s);
        ret.add(new ItemStack(this, 1, meta & 3));
        return ret;
    }

    @Override
    public void beginLeavesDecay(IBlockState state, World world, BlockPos pos) {
        int meta = world.getBlockState(pos).getBlock().getMetaFromState(world.getBlockState(pos));
        world.setBlockState(pos, this.getStateFromMeta(meta | 8), 4);
    }

    @Override
    public boolean isLeaves(IBlockState state, IBlockAccess world, BlockPos pos) {
        return true;
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        int md = world.getBlockState(pos).getBlock().getMetaFromState(world.getBlockState(pos));
        return new ItemStack(this, 1, md & 1);
    }

    @Override
    public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return 60;
    }

    @Override
    public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return 30;
    }
}
