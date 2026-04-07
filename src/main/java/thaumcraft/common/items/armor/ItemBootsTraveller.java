package thaumcraft.common.items.armor;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.client.renderers.compat.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.World;
import thaumcraft.api.IRepairable;
import thaumcraft.api.IRunicArmor;
import thaumcraft.common.Thaumcraft;

public class ItemBootsTraveller extends ItemArmor implements IRepairable, IRunicArmor {
   public TextureAtlasSprite icon;

   private static net.minecraft.inventory.EntityEquipmentSlot slotFromIndex(int k) {
      switch(k) { case 0: return net.minecraft.inventory.EntityEquipmentSlot.HEAD; case 1: return net.minecraft.inventory.EntityEquipmentSlot.CHEST; case 2: return net.minecraft.inventory.EntityEquipmentSlot.LEGS; default: return net.minecraft.inventory.EntityEquipmentSlot.FEET; }
   }

   public ItemBootsTraveller(ItemArmor.ArmorMaterial enumarmormaterial, int j, int k) {
      super(enumarmormaterial, j, slotFromIndex(k));
      this.setMaxDamage(350);
      this.setCreativeTab(Thaumcraft.tabTC);
   }

   @SideOnly(Side.CLIENT)
   public void registerIcons(IIconRegister ir) {
      this.icon = ir.registerSprite("thaumcraft:bootstraveler");
   }

   @SideOnly(Side.CLIENT)
   public TextureAtlasSprite getIconFromDamage(int par1) {
      return this.icon;
   }

   public String getArmorTexture(ItemStack stack, Entity entity, net.minecraft.inventory.EntityEquipmentSlot slot, String type) {
      return "thaumcraft:textures/models/bootstraveler.png";
   }

   public net.minecraft.item.EnumRarity getRarity(ItemStack itemstack) {
      return net.minecraft.item.EnumRarity.RARE;
   }

   public int getRunicCharge(ItemStack itemstack) {
      return 0;
   }

   public void onArmorTick(World world, EntityPlayer player, ItemStack itemStack) {
      if (!player.capabilities.isFlying && player.moveForward > 0.0F) {
         if (player.world.isRemote && !player.isSneaking()) {
            if (!Thaumcraft.instance.entityEventHandler.prevStep.containsKey(player.getEntityId())) {
               Thaumcraft.instance.entityEventHandler.prevStep.put(player.getEntityId(), player.stepHeight);
            }

            player.stepHeight = 1.0F;
         }

         if (player.onGround) {
            float bonus = 0.055F;
            if (player.isInWater()) {
               bonus /= 4.0F;
            }

            player.moveRelative(0.0F, 0.0F, 1.0F, bonus);
         } else if (Hover.getHover(player.getEntityId())) {
            player.jumpMovementFactor = 0.03F;
         } else {
            player.jumpMovementFactor = 0.05F;
         }
      }

      if (player.fallDistance > 0.0F) {
         player.fallDistance -= 0.25F;
      }

   }
}
