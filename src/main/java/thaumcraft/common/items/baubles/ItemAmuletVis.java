package thaumcraft.common.items.baubles;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.List;
import thaumcraft.client.renderers.compat.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.text.translation.I18n;
import thaumcraft.api.IRunicArmor;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumcraft.common.lib.research.ResearchManager;
import thaumcraft.common.tiles.TileVisRelay;

public class ItemAmuletVis extends Item implements IBauble, IRunicArmor {
   public TextureAtlasSprite[] icon = new TextureAtlasSprite[2];
   DecimalFormat myFormatter = new DecimalFormat("#######.##");

   public ItemAmuletVis() {
      this.maxStackSize = 1;
      this.canRepair = false;
      this.setMaxDamage(0);
      this.setCreativeTab(Thaumcraft.tabTC);
      this.setHasSubtypes(true);
   }

   @SideOnly(Side.CLIENT)
   public void registerIcons(IIconRegister ir) {
      this.icon[0] = ir.registerSprite("thaumcraft:vis_amulet_lesser");
      this.icon[1] = ir.registerSprite("thaumcraft:vis_amulet");
   }

   @SideOnly(Side.CLIENT)
   public TextureAtlasSprite getIconFromDamage(int par1) {
      return par1 >= this.icon.length ? this.icon[0] : this.icon[par1];
   }

   public EnumRarity getRarity(ItemStack itemstack) {
      return itemstack.getItemDamage() == 0 ? EnumRarity.UNCOMMON : EnumRarity.RARE;
   }

   @Override
   public String getTranslationKey(ItemStack stack) {
      return getTranslationKey() + "." + stack.getItemDamage();
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void getSubItems(CreativeTabs par2CreativeTabs, net.minecraft.util.NonNullList<ItemStack> par3List) {
      par3List.add(new ItemStack(this, 1, 0));
      par3List.add(new ItemStack(this, 1, 1));
   }

   public BaubleType getBaubleType(ItemStack itemstack) {
      return BaubleType.AMULET;
   }

   public void onWornTick(ItemStack itemstack, EntityLivingBase player) {
      if (!player.world.isRemote && player.ticksExisted % 5 == 0) {
         if (!player.getHeldItemMainhand().isEmpty() && player.getHeldItemMainhand().getItem() instanceof ItemWandCasting) {
            ItemWandCasting wand = (ItemWandCasting)player.getHeldItemMainhand().getItem();
            AspectList al = wand.getAspectsWithRoom(player.getHeldItemMainhand());

            for(Aspect aspect : al.getAspects()) {
               if (aspect != null && this.getVis(itemstack, aspect) > 0) {
                  int amt = Math.min(5, wand.getMaxVis(player.getHeldItemMainhand()) - wand.getVis(player.getHeldItemMainhand(), aspect));
                  amt = Math.min(amt, this.getVis(itemstack, aspect));
                  this.storeVis(itemstack, aspect, this.getVis(itemstack, aspect) - amt);
                  wand.storeVis(player.getHeldItemMainhand(), aspect, this.getVis(player.getHeldItemMainhand(), aspect) + amt);
               }
            }
         }

         if (TileVisRelay.nearbyPlayers.containsKey(player.getEntityId())) {
            if (((WeakReference)TileVisRelay.nearbyPlayers.get(player.getEntityId())).get() != null && ((TileVisRelay)((WeakReference)TileVisRelay.nearbyPlayers.get(player.getEntityId())).get()).getPos().distanceSqToCenter(player.posX, player.posY, player.posZ) < (double)(26.0F * 26.0F)) {
               AspectList al = this.getAspectsWithRoom(itemstack);

               for(Aspect aspect : al.getAspects()) {
                  if (aspect != null) {
                     int amt = ((TileVisRelay)((WeakReference)TileVisRelay.nearbyPlayers.get(player.getEntityId())).get()).consumeVis(aspect, Math.min(5, this.getMaxVis(itemstack) - this.getVis(itemstack, aspect)));
                     if (amt > 0) {
                        this.addRealVis(itemstack, aspect, amt, true);
                        ((TileVisRelay)((WeakReference)TileVisRelay.nearbyPlayers.get(player.getEntityId())).get()).triggerConsumeEffect(aspect);
                     }
                  }
               }
            } else {
               TileVisRelay.nearbyPlayers.remove(player.getEntityId());
            }
         }
      }

   }

   public void onEquipped(ItemStack itemstack, EntityLivingBase player) {
   }

   public void onUnequipped(ItemStack itemstack, EntityLivingBase player) {
   }

   public void addInformation(ItemStack stack, @javax.annotation.Nullable net.minecraft.world.World worldIn, List list, net.minecraft.client.util.ITooltipFlag flagIn) {
      if (stack.getItemDamage() == 0) {
         list.add(TextFormatting.AQUA + I18n.translateToLocal("item.ItemAmuletVis.text"));
      }

      list.add(TextFormatting.GOLD + I18n.translateToLocal("item.capacity.text") + " " + this.getMaxVis(stack) / 100);
      if (stack.hasTagCompound()) {
         for(Aspect aspect : Aspect.getPrimalAspects()) {
            if (stack.getTagCompound().hasKey(aspect.getTag())) {
               String amount = this.myFormatter.format((float)stack.getTagCompound().getInteger(aspect.getTag()) / 100.0F);
               list.add(" §" + aspect.getChatcolor() + aspect.getName() + "§r x " + amount);
            }
         }
      }

   }

   public int getMaxVis(ItemStack stack) {
      return stack.getItemDamage() == 1 ? 25000 : 2500;
   }

   public int getVis(ItemStack is, Aspect aspect) {
      int out = 0;
      if (is.hasTagCompound() && is.getTagCompound().hasKey(aspect.getTag())) {
         out = is.getTagCompound().getInteger(aspect.getTag());
      }

      return out;
   }

   public void storeVis(ItemStack is, Aspect aspect, int amount) {
      is.setTagInfo(aspect.getTag(), new NBTTagInt(amount));
   }

   public AspectList getAspectsWithRoom(ItemStack wandstack) {
      AspectList out = new AspectList();
      AspectList cur = this.getAllVis(wandstack);

      for(Aspect aspect : cur.getAspects()) {
         if (cur.getAmount(aspect) < this.getMaxVis(wandstack)) {
            out.add(aspect, 1);
         }
      }

      return out;
   }

   public AspectList getAllVis(ItemStack is) {
      AspectList out = new AspectList();

      for(Aspect aspect : Aspect.getPrimalAspects()) {
         if (is.hasTagCompound() && is.getTagCompound().hasKey(aspect.getTag())) {
            out.merge(aspect, is.getTagCompound().getInteger(aspect.getTag()));
         } else {
            out.merge(aspect, 0);
         }
      }

      return out;
   }

   public boolean consumeAllVis(ItemStack is, EntityPlayer player, AspectList aspects, boolean doit, boolean crafting) {
      if (aspects != null && aspects.size() != 0) {
         for(Aspect aspect : aspects.getAspects()) {
            if (this.getVis(is, aspect) < aspects.getAmount(aspect)) {
               return false;
            }
         }

         if (doit) {
            for(Aspect aspect : aspects.getAspects()) {
               this.storeVis(is, aspect, this.getVis(is, aspect) - aspects.getAmount(aspect));
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public int addVis(ItemStack is, Aspect aspect, int amount, boolean doit) {
      if (!aspect.isPrimal()) {
         return 0;
      } else {
         int storeAmount = this.getVis(is, aspect) + amount * 100;
         int leftover = Math.max(storeAmount - this.getMaxVis(is), 0);
         if (doit) {
            this.storeVis(is, aspect, Math.min(storeAmount, this.getMaxVis(is)));
         }

         return leftover / 100;
      }
   }

   public int addRealVis(ItemStack is, Aspect aspect, int amount, boolean doit) {
      if (!aspect.isPrimal()) {
         return 0;
      } else {
         int storeAmount = this.getVis(is, aspect) + amount;
         int leftover = Math.max(storeAmount - this.getMaxVis(is), 0);
         if (doit) {
            this.storeVis(is, aspect, Math.min(storeAmount, this.getMaxVis(is)));
         }

         return leftover;
      }
   }

   public boolean canEquip(ItemStack itemstack, EntityLivingBase player) {
      return itemstack.getItemDamage() != 1 || ResearchManager.isResearchComplete(player.getName(), "VISAMULET");
   }

   public boolean canUnequip(ItemStack itemstack, EntityLivingBase player) {
      return true;
   }

   public int getRunicCharge(ItemStack itemstack) {
      return 0;
   }
}
