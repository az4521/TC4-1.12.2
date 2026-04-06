package thaumcraft.common.entities.golems;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.List;
import net.minecraft.block.Block;
import thaumcraft.client.renderers.compat.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import thaumcraft.common.Thaumcraft;
import net.minecraft.util.math.BlockPos;

public class ItemGolemPlacer extends Item {
   public TextureAtlasSprite[] iconGolem = new TextureAtlasSprite[8];
   public TextureAtlasSprite iconAdvanced;
   public TextureAtlasSprite iconCore;
   private TextureAtlasSprite iconBlank;

   public ItemGolemPlacer() {
      this.setHasSubtypes(true);
      this.setCreativeTab(Thaumcraft.tabTC);
      this.setMaxStackSize(1);
   }

   @SideOnly(Side.CLIENT)
   public void registerIcons(IIconRegister ir) {
      this.iconGolem[0] = ir.registerSprite("thaumcraft:golem_straw");
      this.iconGolem[1] = ir.registerSprite("thaumcraft:golem_wood");
      this.iconGolem[2] = ir.registerSprite("thaumcraft:golem_tallow");
      this.iconGolem[3] = ir.registerSprite("thaumcraft:golem_clay");
      this.iconGolem[4] = ir.registerSprite("thaumcraft:golem_flesh");
      this.iconGolem[5] = ir.registerSprite("thaumcraft:golem_stone");
      this.iconGolem[6] = ir.registerSprite("thaumcraft:golem_iron");
      this.iconGolem[7] = ir.registerSprite("thaumcraft:golem_thaumium");
      this.iconAdvanced = ir.registerSprite("thaumcraft:golem_over_adv");
      this.iconCore = ir.registerSprite("thaumcraft:golem_over_core");
      this.iconBlank = ir.registerSprite("thaumcraft:blank");
   }

   @SideOnly(Side.CLIENT)
   public int getRenderPasses(int metadata) {
      return 3;
   }

   @SideOnly(Side.CLIENT)
   public TextureAtlasSprite getIconFromDamage(int par1) {
      return this.iconGolem[par1];
   }

   public TextureAtlasSprite getIcon(ItemStack stack, int pass) {
      if (pass == 0) {
         return null;
      } else if (pass == 1 && stack.hasTagCompound() && stack.getTagCompound().hasKey("advanced")) {
         return this.iconAdvanced;
      } else {
         return pass == 2 && stack.hasTagCompound() && stack.getTagCompound().hasKey("core") ? this.iconCore : this.iconBlank;
      }
   }

   @SideOnly(Side.CLIENT)
   public boolean requiresMultipleRenderPasses() {
      return true;
   }

   public String getTranslationKey(ItemStack par1ItemStack) {
      return super.getTranslationKey() + "." + par1ItemStack.getItemDamage();
   }

   public void addInformation(ItemStack stack, World worldIn, List<String> list, net.minecraft.client.util.ITooltipFlag flag) {
      if (stack.hasTagCompound()) {
         if (stack.getTagCompound().hasKey("core")) {
            list.add(I18n.translateToLocal("item.ItemGolemCore.name") + ": §6" + I18n.translateToLocal("item.ItemGolemCore." + stack.getTagCompound().getByte("core") + ".name"));
         }

         if (stack.getTagCompound().hasKey("advanced")) {
            list.add(I18n.translateToLocal("tc.adv"));
         }

         if (stack.getTagCompound().hasKey("upgrades")) {
            byte[] ba = stack.getTagCompound().getByteArray("upgrades");
            StringBuilder text = new StringBuilder("§9");

            for(byte b : ba) {
               if (b > -1) {
                  text.append(I18n.translateToLocal("item.ItemGolemUpgrade." + b + ".name")).append(" ");
               }
            }

            list.add(text.toString());
         }

         if (stack.getTagCompound().hasKey("markers")) {
            NBTTagList tl = stack.getTagCompound().getTagList("markers", 10);
            list.add("§5" + tl.tagCount() + " " + I18n.translateToLocal("tc.markedloc"));
         }

         if (stack.getTagCompound().hasKey("deco")) {
            String decoDesc = "§2";
            String deco = stack.getTagCompound().getString("deco");
            if (deco.contains("H")) {
               decoDesc = decoDesc + I18n.translateToLocal("item.ItemGolemDecoration.0.name") + " ";
            }

            if (deco.contains("G")) {
               decoDesc = decoDesc + I18n.translateToLocal("item.ItemGolemDecoration.1.name") + " ";
            }

            if (deco.contains("B")) {
               decoDesc = decoDesc + I18n.translateToLocal("item.ItemGolemDecoration.2.name") + " ";
            }

            if (deco.contains("F")) {
               decoDesc = decoDesc + I18n.translateToLocal("item.ItemGolemDecoration.3.name") + " ";
            }

            if (deco.contains("R")) {
               decoDesc = decoDesc + I18n.translateToLocal("item.ItemGolemDecoration.4.name") + " ";
            }

            if (deco.contains("V")) {
               decoDesc = decoDesc + I18n.translateToLocal("item.ItemGolemDecoration.5.name") + " ";
            }

            if (deco.contains("P")) {
               decoDesc = decoDesc + I18n.translateToLocal("item.ItemGolemDecoration.6.name") + " ";
            }

            list.add(decoDesc);
         }
      }

   }

   public boolean getShareTag() {
       return super.getShareTag();
   }

   public boolean doesSneakBypassUse(ItemStack stack, net.minecraft.world.IBlockAccess world, BlockPos pos, EntityPlayer player) {
      return true;
   }

   public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing facing, float par8, float par9, float par10, EnumHand hand) {
      ItemStack stack = player.getHeldItem(hand);
      if (!world.isRemote && !player.isSneaking()) {
         Block var11 = world.getBlockState(pos).getBlock();
         int par4 = pos.getX() + facing.getXOffset();
         int par5 = pos.getY() + facing.getYOffset();
         int par6 = pos.getZ() + facing.getZOffset();
         int side = facing.getIndex();
         double var12 = 0.0F;
         if (side == 1 && var11 == Blocks.OAK_FENCE || var11 == Blocks.NETHER_BRICK_FENCE) {
            var12 = 0.5F;
         }

         if (this.spawnCreature(world, (double)par4 + (double)0.5F, (double)par5 + var12, (double)par6 + (double)0.5F, side, stack, player) && !player.capabilities.isCreativeMode) {
            stack.shrink(1);
         }

         return EnumActionResult.SUCCESS;
      } else {
         return EnumActionResult.PASS;
      }
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void getSubItems(CreativeTabs par2CreativeTabs, net.minecraft.util.NonNullList<ItemStack> par3List) {
      for(int a = 0; a <= 7; ++a) {
         par3List.add(new ItemStack(this, 1, a));
      }

   }

   public boolean spawnCreature(World par0World, double par2, double par4, double par6, int side, ItemStack stack, EntityPlayer player) {
      boolean adv = stack.hasTagCompound() && stack.getTagCompound().hasKey("advanced");

       EntityGolemBase golem = new EntityGolemBase(par0World, EnumGolemType.getType(stack.getItemDamage()), adv);
      if (golem != null) {
         golem.setLocationAndAngles(par2, par4, par6, par0World.rand.nextFloat() * 360.0F, 0.0F);
         golem.playLivingSound();
         golem.setHomePosAndDistance(new BlockPos(MathHelper.floor(par2), MathHelper.floor(par4), MathHelper.floor(par6)), 32);
         if (stack.hasTagCompound() && stack.getTagCompound().hasKey("core")) {
            golem.setCore(stack.getTagCompound().getByte("core"));
         }

         if (stack.hasTagCompound() && stack.getTagCompound().hasKey("upgrades")) {
            int ul = golem.upgrades.length;
            golem.upgrades = stack.getTagCompound().getByteArray("upgrades");
            if (ul != golem.upgrades.length) {
               byte[] tt = new byte[ul];

                Arrays.fill(tt, (byte) -1);

               for(int a = 0; a < golem.upgrades.length; ++a) {
                  if (a < ul) {
                     tt[a] = golem.upgrades[a];
                  }
               }

               golem.upgrades = tt;
            }
         }

         String deco = "";
         if (stack.hasTagCompound() && stack.getTagCompound().hasKey("deco")) {
            deco = stack.getTagCompound().getString("deco");
            golem.decoration = deco;
         }

         golem.setup(side);
         par0World.spawnEntity(golem);
         golem.setGolemDecoration(deco);
         golem.setOwner(player.getName());
         golem.setMarkers(ItemGolemBell.getMarkers(stack));
         int a = 0;

         for(byte b : golem.upgrades) {
            golem.setUpgrade(a, b);
            ++a;
         }

         if (stack.hasDisplayName()) {
            golem.setCustomNameTag(stack.getDisplayName());
            golem.enablePersistence();
         }

         if (stack.hasTagCompound() && stack.getTagCompound().hasKey("Inventory")) {
            NBTTagList nbttaglist2 = stack.getTagCompound().getTagList("Inventory", 10);
            golem.inventory.readFromNBT(nbttaglist2);
         }
      }

      return golem != null;
   }
}
