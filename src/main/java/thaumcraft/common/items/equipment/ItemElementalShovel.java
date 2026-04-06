package thaumcraft.common.items.equipment;

import com.google.common.collect.ImmutableSet;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import thaumcraft.client.renderers.compat.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import thaumcraft.api.BlockCoordinates;
import thaumcraft.api.IArchitect;
import thaumcraft.api.IRepairable;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.lib.utils.BlockUtils;
import thaumcraft.common.lib.utils.InventoryUtils;

public class ItemElementalShovel extends ItemSpade implements IRepairable, IArchitect {
    private static final Block[] isEffective;
    public TextureAtlasSprite icon;
    net.minecraft.util.EnumFacing side = net.minecraft.util.EnumFacing.DOWN;

    public ItemElementalShovel(Item.ToolMaterial enumtoolmaterial) {
        super(enumtoolmaterial);
        this.setCreativeTab(Thaumcraft.tabTC);
    }

    @Override
    public Set getToolClasses(ItemStack stack) {
        return ImmutableSet.of("shovel");
    }

    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister ir) {
        this.icon = ir.registerSprite("thaumcraft:elementalshovel");
    }

    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite getIconFromDamage(int par1) {
        return this.icon;
    }

    @Override
    public EnumRarity getRarity(ItemStack itemstack) {
        return EnumRarity.RARE;
    }

    @Override
    public boolean getIsRepairable(ItemStack par1ItemStack, ItemStack par2ItemStack) {
        return par2ItemStack.isItemEqual(new ItemStack(ConfigItems.itemResource, 1, 2)) || super.getIsRepairable(par1ItemStack, par2ItemStack);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        int xm = facing.getXOffset();
        int ym = facing.getYOffset();
        int zm = facing.getZOffset();
        boolean result = false;
        ItemStack itemstack = player.getHeldItem(hand);
        Block bi = world.getBlockState(new BlockPos(x, y, z)).getBlock();
        int md = world.getBlockState(new BlockPos(x, y, z)).getBlock().getMetaFromState(world.getBlockState(new BlockPos(x, y, z)));
        TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
        if (te == null) {
            for (int aa = -1; aa <= 1; ++aa) {
                for (int bb = -1; bb <= 1; ++bb) {
                    int xx = 0;
                    int yy = 0;
                    int zz = 0;
                    byte o = getOrientation(itemstack);
                    if (o == 1) {
                        yy = bb;
                        if (facing.getIndex() <= 1) {
                            int l = MathHelper.floor((double) (player.rotationYaw * 4.0F / 360.0F) + (double) 0.5F) & 3;
                            if (l != 0 && l != 2) {
                                zz = aa;
                            } else {
                                xx = aa;
                            }
                        } else if (facing.getIndex() <= 3) {
                            zz = aa;
                        } else {
                            xx = aa;
                        }
                    } else if (o == 2) {
                        if (facing.getIndex() <= 1) {
                            int l = MathHelper.floor((double) (player.rotationYaw * 4.0F / 360.0F) + (double) 0.5F) & 3;
                            yy = bb;
                            if (l != 0 && l != 2) {
                                zz = aa;
                            } else {
                                xx = aa;
                            }
                        } else {
                            zz = bb;
                            xx = aa;
                        }
                    } else if (facing.getIndex() <= 1) {
                        xx = aa;
                        zz = bb;
                    } else if (facing.getIndex() <= 3) {
                        xx = aa;
                        yy = bb;
                    } else {
                        zz = aa;
                        yy = bb;
                    }

                    BlockPos targetPos = new BlockPos(x + xx + xm, y + yy + ym, z + zz + zm);
                    Block b2 = world.getBlockState(targetPos).getBlock();
                    if (world.isAirBlock(targetPos) || b2 == Blocks.VINE || b2 == Blocks.TALLGRASS || world.getBlockState(targetPos).getMaterial() == Material.WATER || b2 == Blocks.DEADBUSH || b2.isReplaceable(world, targetPos)) {
                        if (!player.capabilities.isCreativeMode && !InventoryUtils.consumeInventoryItem(player, Item.getItemFromBlock(bi), md)) {
                            if (bi == Blocks.GRASS && (player.capabilities.isCreativeMode || InventoryUtils.consumeInventoryItem(player, Item.getItemFromBlock(Blocks.DIRT), 0))) {
                                world.playSound(null, targetPos, bi.getSoundType(world.getBlockState(targetPos), world, targetPos, null).getPlaceSound(), SoundCategory.BLOCKS, 0.6F, 0.9F + world.rand.nextFloat() * 0.2F);
                                world.setBlockState(targetPos, Blocks.DIRT.getStateFromMeta(0), 3);
                                result = true;
                                itemstack.damageItem(1, player);
                                Thaumcraft.proxy.blockSparkle(world, x + xx + xm, y + yy + ym, z + zz + zm, 3, 4);
                                player.swingArm(EnumHand.MAIN_HAND);
                            }
                        } else {
                            world.playSound(null, targetPos, bi.getSoundType(world.getBlockState(targetPos), world, targetPos, null).getPlaceSound(), SoundCategory.BLOCKS, 0.6F, 0.9F + world.rand.nextFloat() * 0.2F);
                            world.setBlockState(targetPos, bi.getStateFromMeta(md), 3);
                            result = true;
                            itemstack.damageItem(1, player);
                            Thaumcraft.proxy.blockSparkle(world, x + xx + xm, y + yy + ym, z + zz + zm, 8401408, 4);
                            player.swingArm(EnumHand.MAIN_HAND);
                        }
                    }
                }
            }
        }

        return result ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
    }

    private boolean isEffectiveAgainst(Block block) {
        for (Block value : isEffective) {
            if (value == block) {
                return true;
            }
        }

        return false;
    }

    public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, EntityPlayer player) {
        RayTraceResult movingobjectposition = BlockUtils.getTargetBlock(player.world, player, true);
        if (movingobjectposition != null && movingobjectposition.typeOfHit == RayTraceResult.Type.BLOCK) {
            this.side = movingobjectposition.sideHit;
        }

        return super.onBlockStartBreak(itemstack, pos, player);
    }

    public boolean onBlockDestroyed(ItemStack stack, World world, IBlockState state, BlockPos pos, EntityLivingBase ent) {
        Block bi = state.getBlock();
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        if (ent.isSneaking()) {
            return super.onBlockDestroyed(stack, world, state, pos, ent);
        } else {
            if (!ent.world.isRemote) {
                int md = world.getBlockState(new BlockPos(x, y, z)).getBlock().getMetaFromState(world.getBlockState(new BlockPos(x, y, z)));
                if (ForgeHooks.isToolEffective(world, new BlockPos(x, y, z), stack) || this.isEffectiveAgainst(bi)) {
                    for (int aa = -1; aa <= 1; ++aa) {
                        for (int bb = -1; bb <= 1; ++bb) {
                            int xx = 0;
                            int yy = 0;
                            int zz = 0;
                            if (this.side.getIndex() <= 1) {
                                xx = aa;
                                zz = bb;
                            } else if (this.side.getIndex() <= 3) {
                                xx = aa;
                                yy = bb;
                            } else {
                                zz = aa;
                                yy = bb;
                            }

                            BlockPos innerPos = new BlockPos(x + xx, y + yy, z + zz);
                            if (!(ent instanceof EntityPlayer) || world.isBlockModifiable((EntityPlayer) ent, innerPos)) {
                                Block bl = world.getBlockState(innerPos).getBlock();
                                md = world.getBlockState(innerPos).getBlock().getMetaFromState(world.getBlockState(innerPos));
                                if (bl.getBlockHardness(world.getBlockState(innerPos), world, innerPos) >= 0.0F && (ForgeHooks.isToolEffective(world, innerPos, stack) || this.isEffectiveAgainst(bl))) {
                                    stack.damageItem(1, ent);
                                    BlockUtils.harvestBlock(world, (EntityPlayer) ent, x + xx, y + yy, z + zz, true, 3);
                                }
                            }
                        }
                    }
                }
            }

            return true;
        }
    }

    public ArrayList<BlockCoordinates> getArchitectBlocks(ItemStack focusstack, World world, int x, int y, int z, net.minecraft.util.EnumFacing side, EntityPlayer player) {
        ArrayList<BlockCoordinates> b = new ArrayList<>();
        if (!player.isSneaking()) {
            return b;
        } else {
            int xm = side.getXOffset();
            int ym = side.getYOffset();
            int zm = side.getZOffset();

            for (int aa = -1; aa <= 1; ++aa) {
                for (int bb = -1; bb <= 1; ++bb) {
                    int xx = 0;
                    int yy = 0;
                    int zz = 0;
                    byte o = getOrientation(focusstack);
                    if (o == 1) {
                        yy = bb;
                        if (side.getIndex() <= 1) {
                            int l = MathHelper.floor((double) (player.rotationYaw * 4.0F / 360.0F) + (double) 0.5F) & 3;
                            if (l != 0 && l != 2) {
                                zz = aa;
                            } else {
                                xx = aa;
                            }
                        } else if (side.getIndex() <= 3) {
                            zz = aa;
                        } else {
                            xx = aa;
                        }
                    } else if (o == 2) {
                        if (side.getIndex() <= 1) {
                            int l = MathHelper.floor((double) (player.rotationYaw * 4.0F / 360.0F) + (double) 0.5F) & 3;
                            yy = bb;
                            if (l != 0 && l != 2) {
                                zz = aa;
                            } else {
                                xx = aa;
                            }
                        } else {
                            zz = bb;
                            xx = aa;
                        }
                    } else if (side.getIndex() <= 1) {
                        xx = aa;
                        zz = bb;
                    } else if (side.getIndex() <= 3) {
                        xx = aa;
                        yy = bb;
                    } else {
                        zz = aa;
                        yy = bb;
                    }

                    BlockPos targetPos = new BlockPos(x + xx + xm, y + yy + ym, z + zz + zm);
                    Block b2 = world.getBlockState(targetPos).getBlock();
                    if (world.isAirBlock(targetPos) || b2 == Blocks.VINE || b2 == Blocks.TALLGRASS || world.getBlockState(targetPos).getMaterial() == Material.WATER || b2 == Blocks.DEADBUSH || b2.isReplaceable(world, targetPos)) {
                        b.add(new BlockCoordinates(x + xx + xm, y + yy + ym, z + zz + zm));
                    }
                }
            }

            return b;
        }
    }

    public boolean showAxis(ItemStack stack, World world, EntityPlayer player, net.minecraft.util.EnumFacing side, IArchitect.EnumAxis axis) {
        return false;
    }

    public static byte getOrientation(ItemStack stack) {
        return stack.hasTagCompound() && stack.getTagCompound().hasKey("or") ? stack.getTagCompound().getByte("or") : 0;
    }

    public static void setOrientation(ItemStack stack, byte o) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }

        if (stack.hasTagCompound()) {
            stack.getTagCompound().setByte("or", (byte) (o % 3));
        }

    }

    static {
        isEffective = new Block[]{Blocks.GRASS, Blocks.DIRT, Blocks.SAND, Blocks.GRAVEL, Blocks.SNOW_LAYER, Blocks.SNOW, Blocks.CLAY, Blocks.FARMLAND, Blocks.SOUL_SAND, Blocks.MYCELIUM};
    }
}
