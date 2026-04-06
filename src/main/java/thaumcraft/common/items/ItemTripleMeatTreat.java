package thaumcraft.common.items;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.client.renderers.compat.IIconRegister;
import net.minecraft.item.ItemFood;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import thaumcraft.common.Thaumcraft;

public class ItemTripleMeatTreat extends ItemFood {
   public TextureAtlasSprite icon;

   public ItemTripleMeatTreat() {
      super(6, 0.8F, true);
      this.setAlwaysEdible();
      this.setPotionEffect(new PotionEffect(MobEffects.REGENERATION, 100, 0), 0.66F);
      this.setCreativeTab(Thaumcraft.tabTC);
   }

   @SideOnly(Side.CLIENT)
   public void registerIcons(IIconRegister ir) {
      this.icon = ir.registerSprite("thaumcraft:tripletreat");
   }

   @SideOnly(Side.CLIENT)
   public TextureAtlasSprite getIconFromDamage(int meta) {
      return this.icon;
   }
}
