package thaumcraft.common.items;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.List;
import thaumcraft.client.renderers.compat.IIconRegister;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.playerdata.PacketResearchComplete;
import thaumcraft.common.lib.research.ResearchManager;
import thaumcraft.common.lib.research.ResearchNoteData;

public class ItemResearchNotes extends Item {
   @SideOnly(Side.CLIENT)
   public TextureAtlasSprite iconNote;
   @SideOnly(Side.CLIENT)
   public TextureAtlasSprite iconNoteOver;
   @SideOnly(Side.CLIENT)
   public TextureAtlasSprite iconDiscovery;
   @SideOnly(Side.CLIENT)
   public TextureAtlasSprite iconDiscoveryOver;

   public ItemResearchNotes() {
      this.setHasSubtypes(true);
      this.setMaxDamage(0);
      this.setMaxStackSize(1);
   }

   @SideOnly(Side.CLIENT)
   public void registerIcons(IIconRegister ir) {
      this.iconNote = ir.registerSprite("thaumcraft:researchnotes");
      this.iconNoteOver = ir.registerSprite("thaumcraft:researchnotesoverlay");
      this.iconDiscovery = ir.registerSprite("thaumcraft:discovery");
      this.iconDiscoveryOver = ir.registerSprite("thaumcraft:discoveryoverlay");
   }

   @SideOnly(Side.CLIENT)
   public TextureAtlasSprite getIconFromDamage(int par1) {
      return par1 / 64 == 0 ? this.iconNote : this.iconDiscovery;
   }

   @SideOnly(Side.CLIENT)
   public TextureAtlasSprite getIconFromDamageForRenderPass(int par1, int renderPass) {
      return renderPass == 0 ? (par1 / 64 == 0 ? this.iconNote : this.iconDiscovery) : (par1 / 64 == 0 ? this.iconNoteOver : this.iconDiscoveryOver);
   }

   @Override
   public net.minecraft.util.ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, net.minecraft.util.EnumHand hand) {
      ItemStack stack = player.getHeldItem(hand);
      if (!world.isRemote) {
         if (ResearchManager.getData(stack) != null && ResearchManager.getData(stack).isComplete() && !ResearchManager.isResearchComplete(player.getName(), ResearchManager.getData(stack).key)) {
            if (ResearchManager.doesPlayerHaveRequisites(player.getName(), ResearchManager.getData(stack).key)) {
               PacketHandler.INSTANCE.sendTo(new PacketResearchComplete(ResearchManager.getData(stack).key), (EntityPlayerMP)player);
               Thaumcraft.proxy.getResearchManager().completeResearch(player, ResearchManager.getData(stack).key);
               if (ResearchCategories.getResearch(ResearchManager.getData(stack).key).siblings != null) {
                  for(String sibling : ResearchCategories.getResearch(ResearchManager.getData(stack).key).siblings) {
                     if (!ResearchManager.isResearchComplete(player.getName(), sibling) && ResearchManager.doesPlayerHaveRequisites(player.getName(), sibling)) {
                        PacketHandler.INSTANCE.sendTo(new PacketResearchComplete(sibling), (EntityPlayerMP)player);
                        Thaumcraft.proxy.getResearchManager().completeResearch(player, sibling);
                     }
                  }
               }

               stack.shrink(1);
               { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:learn")); if (_snd != null) world.playSound(null, player.posX, player.posY, player.posZ, _snd, net.minecraft.util.SoundCategory.NEUTRAL, 0.75F, 1.0F); };
            } else {
               player.sendMessage(new TextComponentTranslation(I18n.translateToLocal("tc.researcherror")));
            }
         } else if (stack.getItemDamage() == 42 || stack.getItemDamage() == 24) {
            String key = ResearchManager.findHiddenResearch(player);
            if (key.equals("FAIL")) {
               stack.shrink(1);
               EntityItem entityItem = new EntityItem(world, player.posX, player.posY + (double)(player.getEyeHeight() / 2.0F), player.posZ, new ItemStack(ConfigItems.itemResource, 7 + world.rand.nextInt(3), 9));
               world.spawnEntity(entityItem);
               { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:erase")); if (_snd != null) world.playSound(null, player.posX, player.posY, player.posZ, _snd, net.minecraft.util.SoundCategory.NEUTRAL, 0.75F, 1.0F); };
            } else {
               stack.setItemDamage(0);
               stack.setTagCompound(ResearchManager.createNote(stack, key, player.world).getTagCompound());
               { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:write")); if (_snd != null) world.playSound(null, player.posX, player.posY, player.posZ, _snd, net.minecraft.util.SoundCategory.NEUTRAL, 0.75F, 1.0F); };
            }
         }
      }

      return new net.minecraft.util.ActionResult<>(net.minecraft.util.EnumActionResult.SUCCESS, stack);
   }

   @SideOnly(Side.CLIENT)
   public int getColorFromItemStack(ItemStack stack, int par2) {
      if (par2 == 1) {
         int c = 10066329;
         ResearchNoteData rd = ResearchManager.getData(stack);
         if (rd != null) {
            c = rd.color;
         }

         return c;
      } else {
         return 0xFFFFFF;
      }
   }

   @SideOnly(Side.CLIENT)
   public boolean requiresMultipleRenderPasses() {
      return true;
   }

   public boolean getShareTag() {
       return super.getShareTag();
   }

   public String getItemStackDisplayName(ItemStack itemstack) {
      String name = itemstack.getItemDamage() < 64 ? I18n.translateToLocal("item.researchnotes.name") : I18n.translateToLocal("item.discovery.name");
      return name;
   }

   public void addInformation(ItemStack stack, @javax.annotation.Nullable net.minecraft.world.World worldIn, List list, net.minecraft.client.util.ITooltipFlag flagIn) {
      if (stack.getItemDamage() == 24 || stack.getItemDamage() == 42) {
         list.add(TextFormatting.GOLD + I18n.translateToLocal("item.researchnotes.unknown.1"));
         list.add(TextFormatting.BLUE + I18n.translateToLocal("item.researchnotes.unknown.2"));
      }

      ResearchNoteData rd = ResearchManager.getData(stack);
      if (rd != null && rd.key != null && ResearchCategories.getResearch(rd.key) != null) {
         list.add("§6" + ResearchCategories.getResearch(rd.key).getName());
         list.add("§o" + ResearchCategories.getResearch(rd.key).getText());
         int warp = ThaumcraftApi.getWarp(rd.key);
         if (warp > 0) {
            if (warp > 5) {
               warp = 5;
            }

            String ws = I18n.translateToLocal("tc.forbidden");
            String wr = I18n.translateToLocal("tc.forbidden.level." + warp);
            String wte = ws.replaceAll("%n", wr);
            list.add(TextFormatting.DARK_PURPLE + wte);
         }
      }

   }

   public EnumRarity getRarity(ItemStack itemstack) {
      return itemstack.getItemDamage() < 64 ? EnumRarity.RARE : EnumRarity.EPIC;
   }
}
