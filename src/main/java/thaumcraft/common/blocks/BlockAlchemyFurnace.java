package thaumcraft.common.blocks;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import thaumcraft.client.fx.ParticleEngine;
import thaumcraft.client.fx.particles.FXSlimyBubble;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.tiles.TileAlchemyFurnaceAdvanced;
import thaumcraft.common.tiles.TileAlchemyFurnaceAdvancedNozzle;

import java.util.List;
import java.util.Random;

public class BlockAlchemyFurnace extends BlockContainer {
    public static final net.minecraft.block.properties.PropertyInteger META =
            net.minecraft.block.properties.PropertyInteger.create("meta", 0, 15);

    public BlockAlchemyFurnace() {
        super(Material.IRON);
        this.setHardness(3.0F);
        this.setResistance(17.0F);
        this.setSoundType(net.minecraft.block.SoundType.METAL);
        this.setDefaultState(this.blockState.getBaseState().withProperty(META, 0));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(CreativeTabs par2CreativeTabs, net.minecraft.util.NonNullList<ItemStack> par3List) {
        par3List.add(new ItemStack(this, 1, 0));
    }

    @Override
    public net.minecraft.block.state.BlockStateContainer createBlockState() {
        return new net.minecraft.block.state.BlockStateContainer(this, META);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(META, meta);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(META);
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
    public net.minecraft.util.BlockRenderLayer getRenderLayer() {
        return net.minecraft.util.BlockRenderLayer.CUTOUT;
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        int metadata = world.getBlockState(pos).getBlock().getMetaFromState(world.getBlockState(pos));
        if (metadata == 0) {
            TileAlchemyFurnaceAdvanced tile = (TileAlchemyFurnaceAdvanced) world.getTileEntity(pos);
            if (tile != null && tile.heat > 100) {
                return (int) ((float) tile.heat / (float) tile.maxPower * 12.0F);
            }
        }
        return super.getLightValue(state, world, pos);
    }

    @Override
    public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
        if (!world.isRemote) {
            int metadata = state.getBlock().getMetaFromState(state);
            if (metadata == 0) {
                TileAlchemyFurnaceAdvanced tile = (TileAlchemyFurnaceAdvanced) world.getTileEntity(pos);
                if (tile != null
                        && entity instanceof EntityItem
                        && tile.process(((EntityItem) entity).getItem())) {
                    ItemStack s = ((EntityItem) entity).getItem();
                    s.shrink(1);
                    world.playSound(null, entity.posX, entity.posY, entity.posZ,
                            net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:bubble")),
                            net.minecraft.util.SoundCategory.BLOCKS,
                            0.2F, 1.0F + world.rand.nextFloat() * 0.4F);
                    if (s.getCount() <= 0) {
                        entity.setDead();
                    } else {
                        ((EntityItem) entity).setItem(s);
                    }
                }
            }
        }
    }

    @Override
    public Item getItemDropped(IBlockState state, Random par2Random, int par3) {
        int md = state.getBlock().getMetaFromState(state);
        return md == 0 ? Item.getItemFromBlock(ConfigBlocks.blockStoneDevice) :
                (md != 1 && md != 2 && md != 3 && md != 4 ? Item.getItemById(0) : Item.getItemFromBlock(ConfigBlocks.blockMetalDevice));
    }

    @Override
    public int damageDropped(IBlockState state) {
        int metadata = state.getBlock().getMetaFromState(state);
        if (metadata != 1 && metadata != 4) {
            if (metadata == 3) {
                return 9;
            } else {
                return metadata == 2 ? 1 : 0;
            }
        } else {
            return 3;
        }
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        int metadata = state.getBlock().getMetaFromState(state);
        if (metadata == 0) {
            return new TileAlchemyFurnaceAdvanced();
        } else {
            return metadata == 1 ? new TileAlchemyFurnaceAdvancedNozzle() : super.createTileEntity(world, state);
        }
    }

    @Override
    public boolean hasComparatorInputOverride(IBlockState state) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileAlchemyFurnaceAdvancedNozzle) {
            if (((TileAlchemyFurnaceAdvancedNozzle) te).furnace != null) {
                float r = (float) ((TileAlchemyFurnaceAdvancedNozzle) te).furnace.vis / (float) ((TileAlchemyFurnaceAdvancedNozzle) te).furnace.maxVis;
                return MathHelper.floor(r * 14.0F) + ((TileAlchemyFurnaceAdvancedNozzle) te).furnace.vis > 0 ? 1 : 0;
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int md) {
        if (md == 0) {
            return new TileAlchemyFurnaceAdvanced();
        } else if (md == 1) {
            return new TileAlchemyFurnaceAdvancedNozzle();
        }
        return null;
    }

    private interface FunctionForEachBlock {
       boolean apply(int xOffset, int yOffset, int zOffset);
    }

    private static void forEachBlockInStruct(FunctionForEachBlock doSomeThing) {
       for (int a = -1; a <= 1; ++a) {
          for (int b = -1; b <= 1; ++b) {
             for (int c = -1; c <= 1; ++c) {
                if (doSomeThing.apply(a, b, c)) {
                   return;
                }
             }
          }
       }
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        if (!world.isRemote) {
            int md = state.getBlock().getMetaFromState(state);
            if (md != 0) {
               forEachBlockInStruct((xOffset, yOffset, zOffset) -> {
                  BlockPos neighborPos = pos.add(xOffset, yOffset, zOffset);
                  IBlockState neighborState = world.getBlockState(neighborPos);
                  Block blockCurrent = neighborState.getBlock();
                  int metaCurrent = blockCurrent.getMetaFromState(neighborState);
                  if (blockCurrent == BlockAlchemyFurnace.this && metaCurrent == 0) {
                     TileAlchemyFurnaceAdvanced tile =
                             (TileAlchemyFurnaceAdvanced) world.getTileEntity(neighborPos);
                     if (tile != null) {
                        tile.destroy = true;
                        return true;
                     }
                  }
                  return false;
               });
            }
        }
        super.breakBlock(world, pos, state);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
        int meta = state.getBlock().getMetaFromState(state);
        if (meta == 0) {
            TileAlchemyFurnaceAdvanced tile = (TileAlchemyFurnaceAdvanced) world.getTileEntity(pos);
            if (tile != null && tile.vis > 0) {
                FXSlimyBubble ef = new FXSlimyBubble(world, (float) pos.getX() + rand.nextFloat(), pos.getY() + 1, (float) pos.getZ() + rand.nextFloat(), 0.06F + rand.nextFloat() * 0.06F);
                ef.setAlphaF(0.8F);
                ef.setRBGColorF(0.6F - rand.nextFloat() * 0.2F, 0.0F, 0.6F + rand.nextFloat() * 0.2F);
                ParticleEngine.instance.addEffect(world, ef);
                if (rand.nextInt(50) == 0) {
                    double var21 = (float) pos.getX() + rand.nextFloat();
                    double var22 = (double) pos.getY() + 1.0;
                    double var23 = (float) pos.getZ() + rand.nextFloat();
                    world.playSound(null, var21, var22, var23,
                            net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("liquid.lavapop")),
                            net.minecraft.util.SoundCategory.BLOCKS,
                            0.1F + rand.nextFloat() * 0.1F, 0.9F + rand.nextFloat() * 0.15F);
                }

                int q = rand.nextInt(2);
                int w = rand.nextInt(2);
                FXSlimyBubble ef2 = new FXSlimyBubble(world, (double) pos.getX() - 0.6 + (double) rand.nextFloat() * 0.2 + (double) (q * 2), pos.getY() + 2, (double) pos.getZ() - 0.6 + (double) rand.nextFloat() * 0.2 + (double) (w * 2), 0.06F + rand.nextFloat() * 0.06F);
                ef2.setAlphaF(0.8F);
                ef2.setRBGColorF(0.6F - rand.nextFloat() * 0.2F, 0.0F, 0.6F + rand.nextFloat() * 0.2F);
                ParticleEngine.instance.addEffect(world, ef2);
            }
        }

        super.randomDisplayTick(state, world, pos, rand);
    }

   @Override
   public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) {
      return net.minecraft.util.EnumBlockRenderType.MODEL;
   }
}
