package thaumcraft.common.items;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.List;
import thaumcraft.client.renderers.compat.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.lib.utils.Utils;

public class ItemLootBag extends Item {
   public TextureAtlasSprite[] icon = new TextureAtlasSprite[3];

   public ItemLootBag() {
      this.setMaxStackSize(16);
      this.setHasSubtypes(true);
      this.setMaxDamage(0);
      this.setCreativeTab(Thaumcraft.tabTC);
   }

   @SideOnly(Side.CLIENT)
   public void registerIcons(IIconRegister ir) {
      this.icon[0] = ir.registerSprite("thaumcraft:lootbag");
      this.icon[1] = ir.registerSprite("thaumcraft:lootbagunc");
      this.icon[2] = ir.registerSprite("thaumcraft:lootbagrare");
   }

   @SideOnly(Side.CLIENT)
   public TextureAtlasSprite getIconFromDamage(int par1) {
      return this.icon[par1];
   }

   @Override
   public String getTranslationKey(ItemStack par1ItemStack) {
      return getTranslationKey() + "." + par1ItemStack.getItemDamage();
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void getSubItems(CreativeTabs par2CreativeTabs, net.minecraft.util.NonNullList<ItemStack> par3List) {
      par3List.add(new ItemStack(this, 1, 0));
      par3List.add(new ItemStack(this, 1, 1));
      par3List.add(new ItemStack(this, 1, 2));
   }

   public EnumRarity getRarity(ItemStack stack) {
      switch (stack.getItemDamage()) {
         case 1:
            return EnumRarity.UNCOMMON;
         case 2:
            return EnumRarity.RARE;
         default:
            return EnumRarity.COMMON;
      }
   }

   public void addInformation(ItemStack stack, @javax.annotation.Nullable net.minecraft.world.World worldIn, List list, net.minecraft.client.util.ITooltipFlag flagIn) {
      super.addInformation(stack, worldIn, list, flagIn);
      list.add(I18n.translateToLocal("tc.lootbag"));
   }

   @Override
   public net.minecraft.util.ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, net.minecraft.util.EnumHand hand) {
      ItemStack stack = player.getHeldItem(hand);
      if (!world.isRemote) {
         int q = 8 + world.rand.nextInt(5);

         for(int a = 0; a < q; ++a) {
            ItemStack is = Utils.generateLoot(stack.getItemDamage(), world.rand);
            world.spawnEntity(new EntityItem(world, player.posX, player.posY, player.posZ, is.copy()));
         }

         { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:coins")); if (_snd != null) world.playSound(null, player.posX, player.posY, player.posZ, _snd, net.minecraft.util.SoundCategory.NEUTRAL, 0.75F, 1.0F); };
      }

      stack.shrink(1);
      return new net.minecraft.util.ActionResult<>(net.minecraft.util.EnumActionResult.SUCCESS, stack);
   }
}
