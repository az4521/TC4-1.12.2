package thaumcraft.common.items.equipment;

import com.google.common.collect.ImmutableSet;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.Set;
import net.minecraft.client.Minecraft;
import thaumcraft.client.renderers.compat.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.World;
import thaumcraft.api.IRepairable;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigItems;

public class ItemElementalPickaxe extends ItemPickaxe implements IRepairable {
   public TextureAtlasSprite icon;

   public ItemElementalPickaxe(Item.ToolMaterial enumtoolmaterial) {
      super(enumtoolmaterial);
      this.setCreativeTab(Thaumcraft.tabTC);
   }

   public Set getToolClasses(ItemStack stack) {
      return ImmutableSet.of("pickaxe");
   }

   @SideOnly(Side.CLIENT)
   public void registerIcons(IIconRegister ir) {
      this.icon = ir.registerSprite("thaumcraft:elementalpick");
   }

   @SideOnly(Side.CLIENT)
   public TextureAtlasSprite getIconFromDamage(int par1) {
      return this.icon;
   }

   public EnumRarity getRarity(ItemStack itemstack) {
      return EnumRarity.RARE;
   }

   public boolean getIsRepairable(ItemStack par1ItemStack, ItemStack par2ItemStack) {
      return par2ItemStack.isItemEqual(new ItemStack(ConfigItems.itemResource, 1, 2)) || super.getIsRepairable(par1ItemStack, par2ItemStack);
   }

   public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
      if (!player.world.isRemote && (!(entity instanceof EntityPlayer) || (player.world.getMinecraftServer() != null && player.world.getMinecraftServer().isPVPEnabled()))) {
         entity.setFire(2);
      }

      return super.onLeftClickEntity(stack, player, entity);
   }

   public net.minecraft.util.EnumActionResult onItemUse(EntityPlayer player, World world, net.minecraft.util.math.BlockPos pos, net.minecraft.util.EnumHand hand, net.minecraft.util.EnumFacing facing, float hitX, float hitY, float hitZ) {
      ItemStack itemstack = player.getHeldItem(hand);
      int x = pos.getX(), y = pos.getY(), z = pos.getZ();
      itemstack.damageItem(5, player);
      if (!world.isRemote) {
         { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:wandfail")); if (_snd != null) world.playSound(null, x + 0.5, y + 0.5, z + 0.5, _snd, net.minecraft.util.SoundCategory.NEUTRAL, 0.2F, 0.2F + world.rand.nextFloat() * 0.2F); }
      } else {
         Thaumcraft.instance.renderEventHandler.startScan(player, x, y, z, System.currentTimeMillis() + 5000L, 8);
         player.swingArm(hand);
      }
      return super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
   }
}
