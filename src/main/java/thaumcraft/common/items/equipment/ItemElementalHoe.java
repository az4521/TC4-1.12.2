package thaumcraft.common.items.equipment;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import thaumcraft.client.renderers.compat.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.World;
import thaumcraft.api.IRepairable;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.blocks.BlockCustomPlant;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.lib.utils.Utils;
import net.minecraft.util.math.BlockPos;

public class ItemElementalHoe extends ItemHoe implements IRepairable {
   public TextureAtlasSprite icon;

   public ItemElementalHoe(Item.ToolMaterial enumtoolmaterial) {
      super(enumtoolmaterial);
      this.setCreativeTab(Thaumcraft.tabTC);
   }

   @SideOnly(Side.CLIENT)
   public void registerIcons(IIconRegister ir) {
      this.icon = ir.registerSprite("thaumcraft:elementalhoe");
   }

   @SideOnly(Side.CLIENT)
   public TextureAtlasSprite getIconFromDamage(int par1) {
      return this.icon;
   }

   public int getItemEnchantability() {
      return 5;
   }

   public EnumRarity getRarity(ItemStack itemstack) {
      return EnumRarity.RARE;
   }

   public boolean getIsRepairable(ItemStack par1ItemStack, ItemStack par2ItemStack) {
      return par2ItemStack.isItemEqual(new ItemStack(ConfigItems.itemResource, 1, 2)) || super.getIsRepairable(par1ItemStack, par2ItemStack);
   }

   public net.minecraft.util.EnumActionResult onItemUse(EntityPlayer player, World world, net.minecraft.util.math.BlockPos pos, net.minecraft.util.EnumHand hand, net.minecraft.util.EnumFacing facing, float hitX, float hitY, float hitZ) {
      ItemStack stack = player.getHeldItem(hand);
      int x = pos.getX(), y = pos.getY(), z = pos.getZ();
      if (player.isSneaking()) {
         return super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
      } else {
         boolean did = false;

         for(int xx = -1; xx <= 1; ++xx) {
            for(int zz = -1; zz <= 1; ++zz) {
               if (super.onItemUse(player, world, new net.minecraft.util.math.BlockPos(x + xx, y, z + zz), hand, facing, hitX, hitY, hitZ) == net.minecraft.util.EnumActionResult.SUCCESS) {
                  Thaumcraft.proxy.blockSparkle(world, x + xx, y, z + zz, 8401408, 2);
                  if (!did) {
                     did = true;
                  }
               }
            }
         }

         if (!did) {
            did = Utils.useBonemealAtLoc(world, player, x, y, z);
            if (!did) {
               Block bi = world.getBlockState(pos).getBlock();
               int md = world.getBlockState(pos).getBlock().getMetaFromState(world.getBlockState(pos));
               if (bi == ConfigBlocks.blockCustomPlant && md == 0 && stack.getItemDamage() + 20 <= stack.getMaxDamage()) {
                  ((BlockCustomPlant)bi).growGreatTree(world, x, y, z, world.rand);
                  stack.damageItem(5, player);
                  Thaumcraft.proxy.blockSparkle(world, x, y, z, 0, 2);
                  did = true;
               } else if (bi == ConfigBlocks.blockCustomPlant && md == 1 && stack.getItemDamage() + 150 <= stack.getMaxDamage()) {
                  ((BlockCustomPlant)bi).growSilverTree(world, x, y, z, world.rand);
                  stack.damageItem(25, player);
                  Thaumcraft.proxy.blockSparkle(world, x, y, z, 0, 2);
                  did = true;
               }
            } else {
               stack.damageItem(1, player);
               Thaumcraft.proxy.blockSparkle(world, x, y, z, 0, 3);
            }

            if (did) {
               { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:wand")); if (_snd != null) world.playSound(null, x + 0.5, y + 0.5, z + 0.5, _snd, net.minecraft.util.SoundCategory.NEUTRAL, 0.75F, 0.9F + world.rand.nextFloat() * 0.2F); }
            }
         }

         return did ? net.minecraft.util.EnumActionResult.SUCCESS : net.minecraft.util.EnumActionResult.PASS;
      }
   }
}
