package thaumcraft.common.items.armor;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.List;
import thaumcraft.client.renderers.compat.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import thaumcraft.api.IRepairable;
import thaumcraft.api.IRunicArmor;
import thaumcraft.api.IVisDiscountGear;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigItems;
import net.minecraft.util.math.BlockPos;

public class ItemRobeArmor extends ItemArmor implements IRepairable, IVisDiscountGear, IRunicArmor {
   public TextureAtlasSprite iconChest;
   public TextureAtlasSprite iconLegs;
   public TextureAtlasSprite iconBoots;
   public TextureAtlasSprite iconChestOver;
   public TextureAtlasSprite iconLegsOver;
   public TextureAtlasSprite iconBootsOver;

   private static net.minecraft.inventory.EntityEquipmentSlot slotFromIndex(int k) {
      switch(k) { case 0: return net.minecraft.inventory.EntityEquipmentSlot.HEAD; case 1: return net.minecraft.inventory.EntityEquipmentSlot.CHEST; case 2: return net.minecraft.inventory.EntityEquipmentSlot.LEGS; default: return net.minecraft.inventory.EntityEquipmentSlot.FEET; }
   }

   public ItemRobeArmor(ItemArmor.ArmorMaterial enumarmormaterial, int j, int k) {
      super(enumarmormaterial, j, slotFromIndex(k));
      this.setCreativeTab(Thaumcraft.tabTC);
   }

   public int getRunicCharge(ItemStack itemstack) {
      return 0;
   }

   @SideOnly(Side.CLIENT)
   public void registerIcons(IIconRegister ir) {
      this.iconChest = ir.registerSprite("thaumcraft:clothchest");
      this.iconLegs = ir.registerSprite("thaumcraft:clothlegs");
      this.iconBoots = ir.registerSprite("thaumcraft:clothboots");
      this.iconChestOver = ir.registerSprite("thaumcraft:clothchestover");
      this.iconLegsOver = ir.registerSprite("thaumcraft:clothlegsover");
      this.iconBootsOver = ir.registerSprite("thaumcraft:clothbootsover");
   }

   @SideOnly(Side.CLIENT)
   public TextureAtlasSprite getIconFromDamage(int par1) {
      return this.armorType == net.minecraft.inventory.EntityEquipmentSlot.CHEST ? this.iconChest : (this.armorType == net.minecraft.inventory.EntityEquipmentSlot.LEGS ? this.iconLegs : this.iconBoots);
   }

   @SideOnly(Side.CLIENT)
   public boolean requiresMultipleRenderPasses() {
      return true;
   }

   public boolean hasColor(ItemStack par1ItemStack) {
      return true;
   }

   public TextureAtlasSprite getIconFromDamageForRenderPass(int par1, int par2) {
      return par2 == 0 ? (this.armorType == net.minecraft.inventory.EntityEquipmentSlot.CHEST ? this.iconChest : (this.armorType == net.minecraft.inventory.EntityEquipmentSlot.LEGS ? this.iconLegs : this.iconBoots)) : (this.armorType == net.minecraft.inventory.EntityEquipmentSlot.CHEST ? this.iconChestOver : (this.armorType == net.minecraft.inventory.EntityEquipmentSlot.LEGS ? this.iconLegsOver : this.iconBootsOver));
   }

   public int getColor(ItemStack par1ItemStack) {
      NBTTagCompound nbttagcompound = par1ItemStack.getTagCompound();
      if (nbttagcompound == null) {
         return 6961280;
      } else {
         NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("display");
         return nbttagcompound1 == null ? 6961280 : (nbttagcompound1.hasKey("color") ? nbttagcompound1.getInteger("color") : 6961280);
      }
   }

   public void removeColor(ItemStack par1ItemStack) {
      NBTTagCompound nbttagcompound = par1ItemStack.getTagCompound();
      if (nbttagcompound != null) {
         NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("display");
         if (nbttagcompound1.hasKey("color")) {
            nbttagcompound1.removeTag("color");
         }
      }

   }

   public void setColor(ItemStack par1ItemStack, int par2) {
      NBTTagCompound nbttagcompound = par1ItemStack.getTagCompound();
      if (nbttagcompound == null) {
         nbttagcompound = new NBTTagCompound();
         par1ItemStack.setTagCompound(nbttagcompound);
      }

      NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("display");
      if (!nbttagcompound.hasKey("display")) {
         nbttagcompound.setTag("display", nbttagcompound1);
      }

      nbttagcompound1.setInteger("color", par2);
   }

   public String getArmorTexture(ItemStack stack, Entity entity, int slot, String type) {
      if (stack.getItem() != ConfigItems.itemChestRobe && stack.getItem() != ConfigItems.itemBootsRobe) {
         if (stack.getItem() == ConfigItems.itemLegsRobe) {
            return type == null ? "thaumcraft:textures/models/robes_2.png" : "thaumcraft:textures/models/robes_2_overlay.png";
         } else {
            return type == null ? "thaumcraft:textures/models/robes_1.png" : "thaumcraft:textures/models/robes_1_overlay.png";
         }
      } else {
         return type == null ? "thaumcraft:textures/models/robes_1.png" : "thaumcraft:textures/models/robes_1_overlay.png";
      }
   }

   public net.minecraft.item.EnumRarity getRarity(ItemStack itemstack) {
      return net.minecraft.item.EnumRarity.UNCOMMON;
   }

   public boolean getIsRepairable(ItemStack par1ItemStack, ItemStack par2ItemStack) {
      return par2ItemStack.isItemEqual(new ItemStack(ConfigItems.itemResource, 1, 7)) || super.getIsRepairable(par1ItemStack, par2ItemStack);
   }

   public int getVisDiscount(ItemStack stack, EntityPlayer player, Aspect aspect) {
      return this.armorType == net.minecraft.inventory.EntityEquipmentSlot.FEET ? 1 : 2;
   }

   public void addInformation(ItemStack stack, @javax.annotation.Nullable net.minecraft.world.World worldIn, List list, net.minecraft.client.util.ITooltipFlag flagIn) {
      list.add(TextFormatting.DARK_PURPLE + I18n.translateToLocal("tc.visdiscount") + ": " + this.getVisDiscount(stack, null, null) + "%");
   }

   public net.minecraft.util.EnumActionResult onItemUseFirst(EntityPlayer player, net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos, net.minecraft.util.EnumFacing side, float hitX, float hitY, float hitZ, net.minecraft.util.EnumHand hand) {
      ItemStack stack = player.getHeldItem(hand);
      int x = pos.getX(), y = pos.getY(), z = pos.getZ();
      if (!world.isRemote && world.getBlockState(new BlockPos(x, y, z)).getBlock() == Blocks.CAULDRON &&
        world.getBlockState(new net.minecraft.util.math.BlockPos(x, y, z)).getBlock().getMetaFromState(world.getBlockState(new net.minecraft.util.math.BlockPos(x, y, z))) > 0) {
         this.removeColor(stack);
         { net.minecraft.util.math.BlockPos _p = new net.minecraft.util.math.BlockPos(x, y, z); int _m = world.getBlockState(_p).getBlock().getMetaFromState(world.getBlockState(_p)); world.setBlockState(_p, world.getBlockState(_p).getBlock().getStateFromMeta(_m - 1), 2); }
         world.notifyNeighborsOfStateChange(new net.minecraft.util.math.BlockPos(x, y, z), Blocks.CAULDRON, false);
         return net.minecraft.util.EnumActionResult.SUCCESS;
      } else {
         return net.minecraft.util.EnumActionResult.PASS;
      }
   }
}
