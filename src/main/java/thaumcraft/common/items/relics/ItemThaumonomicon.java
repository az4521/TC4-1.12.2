package thaumcraft.common.items.relics;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.List;
import thaumcraft.client.renderers.compat.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.World;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchCategoryList;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.Config;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.playerdata.PacketSyncAspects;
import thaumcraft.common.lib.network.playerdata.PacketSyncResearch;
import thaumcraft.common.lib.research.ResearchManager;

public class ItemThaumonomicon extends Item {
   @SideOnly(Side.CLIENT)
   public TextureAtlasSprite icon;
   @SideOnly(Side.CLIENT)
   public TextureAtlasSprite iconCheat;

   public ItemThaumonomicon() {
      this.setHasSubtypes(true);
      this.setMaxDamage(0);
      this.setMaxStackSize(1);
      this.setCreativeTab(Thaumcraft.tabTC);
   }

   @SideOnly(Side.CLIENT)
   public void registerIcons(IIconRegister ir) {
      this.icon = ir.registerSprite("thaumcraft:thaumonomicon");
      this.iconCheat = ir.registerSprite("thaumcraft:thaumonomiconcheat");
   }

   @SideOnly(Side.CLIENT)
   public TextureAtlasSprite getIconFromDamage(int par1) {
      return par1 != 42 ? this.icon : this.iconCheat;
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void getSubItems(CreativeTabs par2CreativeTabs, net.minecraft.util.NonNullList<ItemStack> par3List) {
      par3List.add(new ItemStack(this, 1, 0));
      if (Config.allowCheatSheet) {
         par3List.add(new ItemStack(this, 1, 42));
      }

   }

   @Override
   public net.minecraft.util.ActionResult<ItemStack> onItemRightClick(World par2World, EntityPlayer player, net.minecraft.util.EnumHand hand) {
      ItemStack stack = player.getHeldItem(hand);
      if (!par2World.isRemote) {
         if (Config.allowCheatSheet && stack.getItemDamage() == 42) {
            for(ResearchCategoryList cat : ResearchCategories.researchCategories.values()) {
               for(ResearchItem ri : cat.research.values()) {
                  if (!ResearchManager.isResearchComplete(player.getName(), ri.key)) {
                     Thaumcraft.proxy.getResearchManager().completeResearch(player, ri.key);
                  }
               }
            }

            for(Aspect aspect : Aspect.aspects.values()) {
               if (!Thaumcraft.proxy.getPlayerKnowledge().hasDiscoveredAspect(player.getName(), aspect)) {
                  Thaumcraft.proxy.researchManager.completeAspect(player, aspect, (short)50);
               }
            }
         } else {
            for(ResearchCategoryList cat : ResearchCategories.researchCategories.values()) {
               for(ResearchItem ri : cat.research.values()) {
                  if (ResearchManager.isResearchComplete(player.getName(), ri.key) && ri.siblings != null) {
                     for(String sib : ri.siblings) {
                        if (!ResearchManager.isResearchComplete(player.getName(), sib)) {
                           Thaumcraft.proxy.getResearchManager().completeResearch(player, sib);
                        }
                     }
                  }
               }
            }
         }

         PacketHandler.INSTANCE.sendTo(new PacketSyncResearch(player), (EntityPlayerMP)player);
         PacketHandler.INSTANCE.sendTo(new PacketSyncAspects(player), (EntityPlayerMP)player);
      } else {
         { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:page")); if (_snd != null) par2World.playSound(null, player.posX, player.posY, player.posZ, _snd, net.minecraft.util.SoundCategory.NEUTRAL, 1.0F, 1.0F); };
      }

      if (par2World.isRemote) {
         Thaumcraft.proxy.openResearchBrowser();
      }
      return new net.minecraft.util.ActionResult<>(net.minecraft.util.EnumActionResult.SUCCESS, stack);
   }

   public EnumRarity getRarity(ItemStack itemstack) {
      return itemstack.getItemDamage() != 42 ? EnumRarity.UNCOMMON : EnumRarity.EPIC;
   }

   public void addInformation(ItemStack par1ItemStack, @javax.annotation.Nullable net.minecraft.world.World worldIn, List par3List, net.minecraft.client.util.ITooltipFlag flagIn) {
      if (par1ItemStack.getItemDamage() == 42) {
         par3List.add("Cheat Sheet");
      }

      super.addInformation(par1ItemStack, worldIn, par3List, flagIn);
   }
}
