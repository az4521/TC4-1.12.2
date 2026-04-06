package thaumcraft.common.items.equipment;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import thaumcraft.client.renderers.compat.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.EnumHelper;
import thaumcraft.api.IRepairable;
import thaumcraft.api.IWarpingGear;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.lib.utils.BlockUtils;

public class ItemPrimalCrusher extends ItemTool implements IRepairable, IWarpingGear {
   public static Item.ToolMaterial material = EnumHelper.addToolMaterial("PRIMALVOID", 5, 500, 8.0F, 4.0F, 20);
   private static final Set<Block> isEffective;
   public TextureAtlasSprite icon;
   EnumFacing side = EnumFacing.DOWN;

   public ItemPrimalCrusher(Item.ToolMaterial enumtoolmaterial) {
      super(3.5F, -2.8F, enumtoolmaterial, isEffective);
      this.setCreativeTab(Thaumcraft.tabTC);
   }

   public boolean canHarvestBlock(IBlockState state) {
      return state.getMaterial() != net.minecraft.block.material.Material.WOOD
          && state.getMaterial() != net.minecraft.block.material.Material.LEAVES
          && state.getMaterial() != net.minecraft.block.material.Material.PLANTS;
   }

   public float getDestroySpeed(ItemStack stack, IBlockState state) {
      return state.getMaterial() != net.minecraft.block.material.Material.IRON
          && state.getMaterial() != net.minecraft.block.material.Material.ANVIL
          && state.getMaterial() != net.minecraft.block.material.Material.ROCK
          ? super.getDestroySpeed(stack, state)
          : this.efficiency;
   }

   public Set getToolClasses(ItemStack stack) {
      return ImmutableSet.of("shovel", "pickaxe");
   }

   @SideOnly(Side.CLIENT)
   public void registerIcons(IIconRegister ir) {
      this.icon = ir.registerSprite("thaumcraft:primal_crusher");
   }

   @SideOnly(Side.CLIENT)
   public TextureAtlasSprite getIconFromDamage(int par1) {
      return this.icon;
   }

   public EnumRarity getRarity(ItemStack itemstack) {
      return EnumRarity.EPIC;
   }

   public boolean getIsRepairable(ItemStack par1ItemStack, ItemStack par2ItemStack) {
      return par2ItemStack.isItemEqual(new ItemStack(ConfigItems.itemResource, 1, 15)) || super.getIsRepairable(par1ItemStack, par2ItemStack);
   }

   private boolean isEffectiveAgainst(Block block) {
      for(Block b : isEffective) {
         if (b == block) {
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

   public boolean onBlockDestroyed(ItemStack stack, World world, IBlockState biState, BlockPos pos, EntityLivingBase ent) {
      int x = pos.getX(), y = pos.getY(), z = pos.getZ();
      Block bi = biState.getBlock();
      if (ent.isSneaking()) {
         return super.onBlockDestroyed(stack, world, biState, pos, ent);
      } else {
         if (!ent.world.isRemote) {
            if (ForgeHooks.isToolEffective(world, pos, stack) || this.isEffectiveAgainst(bi)) {
               for(int aa = -1; aa <= 1; ++aa) {
                  for(int bb = -1; bb <= 1; ++bb) {
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

                     BlockPos nPos = new BlockPos(x + xx, y + yy, z + zz);
                     if (!(ent instanceof EntityPlayer) || world.isBlockModifiable((EntityPlayer)ent, nPos)) {
                        IBlockState blState = world.getBlockState(nPos);
                        Block bl = blState.getBlock();
                        if (blState.getBlockHardness(world, nPos) >= 0.0F && (ForgeHooks.isToolEffective(world, nPos, stack) || this.isEffectiveAgainst(bl))) {
                           stack.damageItem(1, ent);
                           BlockUtils.harvestBlock(world, (EntityPlayer)ent, x + xx, y + yy, z + zz, true, 2);
                        }
                     }
                  }
               }
            }
         }

         return true;
      }
   }

   public int getItemEnchantability() {
      return 20;
   }

   public int getWarp(ItemStack itemstack, EntityPlayer player) {
      return 2;
   }

   public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
      super.onUpdate(stack, world, entity, itemSlot, isSelected);
      if (stack.isItemDamaged() && entity != null && entity.ticksExisted % 20 == 0 && entity instanceof EntityLivingBase) {
         stack.damageItem(-1, (EntityLivingBase)entity);
      }

   }

   static {
      isEffective = Sets.newHashSet(Blocks.COBBLESTONE, Blocks.DOUBLE_STONE_SLAB, Blocks.STONE_SLAB, Blocks.STONE, Blocks.SANDSTONE, Blocks.MOSSY_COBBLESTONE, Blocks.IRON_ORE, Blocks.IRON_BLOCK, Blocks.COAL_ORE, Blocks.GOLD_BLOCK, Blocks.GOLD_ORE, Blocks.DIAMOND_ORE, Blocks.DIAMOND_BLOCK, Blocks.ICE, Blocks.NETHERRACK, Blocks.LAPIS_ORE, Blocks.LAPIS_BLOCK, Blocks.REDSTONE_ORE, Blocks.LIT_REDSTONE_ORE, Blocks.RAIL, Blocks.DETECTOR_RAIL, Blocks.GOLDEN_RAIL, Blocks.ACTIVATOR_RAIL, Blocks.GRASS, Blocks.DIRT, Blocks.SAND, Blocks.GRAVEL, Blocks.SNOW_LAYER, Blocks.SNOW, Blocks.CLAY, Blocks.FARMLAND, Blocks.SOUL_SAND, Blocks.MYCELIUM, ConfigBlocks.blockTaint, ConfigBlocks.blockTaintFibres, Blocks.OBSIDIAN);
   }
}
