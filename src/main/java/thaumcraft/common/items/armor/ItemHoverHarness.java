package thaumcraft.common.items.armor;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.client.model.ModelBiped;
import thaumcraft.client.renderers.compat.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import thaumcraft.api.IRepairable;
import thaumcraft.api.IRunicArmor;
import thaumcraft.api.IVisDiscountGear;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.client.renderers.models.gear.ModelHoverHarness;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.blocks.ItemJarFilled;

import static thaumcraft.api.aspects.AspectList.addAspectDescriptionToList;

public class ItemHoverHarness extends ItemArmor implements IRepairable, IVisDiscountGear, IRunicArmor {
   ModelBiped model = null;
   public TextureAtlasSprite icon;
   public TextureAtlasSprite iconLightningRing;

   private static net.minecraft.inventory.EntityEquipmentSlot slotFromIndex(int k) {
      switch(k) { case 0: return net.minecraft.inventory.EntityEquipmentSlot.HEAD; case 1: return net.minecraft.inventory.EntityEquipmentSlot.CHEST; case 2: return net.minecraft.inventory.EntityEquipmentSlot.LEGS; default: return net.minecraft.inventory.EntityEquipmentSlot.FEET; }
   }

   public ItemHoverHarness(ItemArmor.ArmorMaterial enumarmormaterial, int j, int k) {
      super(enumarmormaterial, j, slotFromIndex(k));
      this.setMaxDamage(400);
      this.setCreativeTab(Thaumcraft.tabTC);
   }

   @SideOnly(Side.CLIENT)
   public net.minecraft.client.model.ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, net.minecraft.inventory.EntityEquipmentSlot armorSlot, net.minecraft.client.model.ModelBiped _default) {
      if (this.model == null) {
         this.model = new ModelHoverHarness();
      }

      return this.model;
   }

   public int getRunicCharge(ItemStack itemstack) {
      return 0;
   }

   @SideOnly(Side.CLIENT)
   public void registerIcons(IIconRegister ir) {
      this.icon = ir.registerSprite("thaumcraft:hoverharness");
      this.iconLightningRing = ir.registerSprite("thaumcraft:lightningring");
   }

   @SideOnly(Side.CLIENT)
   public TextureAtlasSprite getIconFromDamage(int par1) {
      return this.icon;
   }

   public String getArmorTexture(ItemStack stack, Entity entity, int slot, String type) {
      return "thaumcraft:textures/models/hoverharness.png";
   }

   public net.minecraft.item.EnumRarity getRarity(ItemStack itemstack) {
      return net.minecraft.item.EnumRarity.EPIC;
   }

   public boolean getIsRepairable(ItemStack par1ItemStack, ItemStack par2ItemStack) {
      return par2ItemStack.isItemEqual(new ItemStack(Items.GOLD_INGOT)) || super.getIsRepairable(par1ItemStack, par2ItemStack);
   }

   public int getVisDiscount(ItemStack stack, EntityPlayer player, Aspect aspect) {
      return aspect == Aspect.AIR ? 5 : 2;
   }

   @Override
   public net.minecraft.util.ActionResult<ItemStack> onItemRightClick(World par2World, EntityPlayer par3EntityPlayer, net.minecraft.util.EnumHand hand) {
      ItemStack par1ItemStack = par3EntityPlayer.getHeldItem(hand);
      if (!par2World.isRemote) {
         par3EntityPlayer.openGui(Thaumcraft.instance, 17, par2World, MathHelper.floor(par3EntityPlayer.posX), MathHelper.floor(par3EntityPlayer.posY), MathHelper.floor(par3EntityPlayer.posZ));
      }

      return new net.minecraft.util.ActionResult<>(net.minecraft.util.EnumActionResult.SUCCESS, par1ItemStack);
   }

   public void onArmorTick(World world, EntityPlayer player, ItemStack itemStack) {
      if (!player.capabilities.isCreativeMode) {
         Hover.handleHoverArmor(player, player.inventory.armorItemInSlot(2));
      }

   }

   public void addInformation(ItemStack stack, @javax.annotation.Nullable net.minecraft.world.World worldIn, List list, net.minecraft.client.util.ITooltipFlag flagIn) {
      super.addInformation(stack, worldIn, list, flagIn);
      if (stack.hasTagCompound() && stack.getTagCompound().hasKey("jar")) {
         ItemStack jar = new ItemStack(stack.getTagCompound().getCompoundTag("jar"));

         try {
            AspectList aspects = ((ItemJarFilled)jar.getItem()).getAspects(jar);
            addAspectDescriptionToList(aspects, net.minecraft.client.Minecraft.getMinecraft().player, list);
         } catch (Exception ignored) {
         }
      }

      list.add(TextFormatting.DARK_PURPLE + I18n.translateToLocal("tc.visdiscount") + ": " + this.getVisDiscount(stack, null, null) + "%");
      list.add(TextFormatting.DARK_PURPLE + I18n.translateToLocal("tc.visdiscount") + " (Aer): " + this.getVisDiscount(stack, null, Aspect.AIR) + "%");
   }
}
