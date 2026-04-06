package thaumcraft.common.items;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import thaumcraft.client.renderers.compat.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssentiaContainerItem;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.playerdata.PacketAspectPool;
import thaumcraft.common.lib.research.ResearchManager;
import thaumcraft.common.tiles.TileManaPod;

import static thaumcraft.api.aspects.AspectList.addAspectDescriptionToList;

public class ItemManaBean extends ItemFood implements IEssentiaContainerItem {
   public final int itemUseDuration = 10;
   public TextureAtlasSprite icon;
   Random rand = new Random();
   static Aspect[] displayAspects;

   public ItemManaBean() {
      super(1, 0.5F, true);
      this.setMaxStackSize(64);
      this.setHasSubtypes(true);
      this.setMaxDamage(0);
      this.setCreativeTab(Thaumcraft.tabTC);
      this.setAlwaysEdible();
   }

   public int getMaxItemUseDuration(ItemStack par1ItemStack) {
      return this.itemUseDuration;
   }

   protected void onFoodEaten(ItemStack stack, World world, EntityPlayer player) {
      if (!world.isRemote) {
         Potion[] potionArr = ForgeRegistries.POTIONS.getValuesCollection().toArray(new Potion[0]);
         Potion p = potionArr.length > 0 ? potionArr[world.rand.nextInt(potionArr.length)] : null;
         if (p != null) {
            if (p.isInstant()) {
               p.affectEntity(null, null, player, 2, 3.0D);
            } else {
               player.addPotionEffect(new PotionEffect(p, 160 + world.rand.nextInt(80), 0));
            }
         }

         if (world.rand.nextFloat() < 0.25F) {
            AspectList al = ((ItemManaBean)stack.getItem()).getAspects(stack);
            if (al != null && al.size() > 0) {
               Thaumcraft.proxy.playerKnowledge.addAspectPool(player.getName(), al.getAspects()[0], (short)1);
               ResearchManager.scheduleSave(player);
               PacketHandler.INSTANCE.sendTo(new PacketAspectPool(al.getAspects()[0].getTag(), (short) 1, Thaumcraft.proxy.playerKnowledge.getAspectPoolFor(player.getName(), al.getAspects()[0])), (EntityPlayerMP)player);
            }
         }
      }

   }

   @SideOnly(Side.CLIENT)
   public void registerIcons(IIconRegister ir) {
      this.icon = ir.registerSprite("thaumcraft:mana_bean");
   }

   @SideOnly(Side.CLIENT)
   public TextureAtlasSprite getIconFromDamage(int par1) {
      return this.icon;
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void getSubItems(CreativeTabs par2CreativeTabs, net.minecraft.util.NonNullList<ItemStack> par3List) {
      par3List.add(new ItemStack(this, 1, 0));
   }

   public void addInformation(ItemStack stack, @javax.annotation.Nullable net.minecraft.world.World worldIn, List list, net.minecraft.client.util.ITooltipFlag flagIn) {
      AspectList aspects = this.getAspects(stack);
      addAspectDescriptionToList(aspects, net.minecraft.client.Minecraft.getMinecraft().player, list);

      super.addInformation(stack, worldIn, list, flagIn);
   }

   @SideOnly(Side.CLIENT)
   public int getColorFromItemStack(ItemStack stack, int par2) {
      if (this.getAspects(stack) != null) {
         return this.getAspects(stack).getAspects()[0].getColor();
      } else {
         int idx = (int)(System.currentTimeMillis() / 500L % (long)displayAspects.length);
         return displayAspects[idx].getColor();
      }
   }

   public void onUpdate(ItemStack par1ItemStack, World par2World, Entity par3Entity, int par4, boolean par5) {
      if (!par2World.isRemote && !par1ItemStack.hasTagCompound()) {
         this.setAspects(par1ItemStack, (new AspectList()).add(displayAspects[this.rand.nextInt(displayAspects.length)], 1));
      }

      super.onUpdate(par1ItemStack, par2World, par3Entity, par4, par5);
   }

   public void onCreated(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
      if (!par1ItemStack.hasTagCompound()) {
         this.setAspects(par1ItemStack, (new AspectList()).add(displayAspects[this.rand.nextInt(displayAspects.length)], 1));
      }

   }

   public AspectList getAspects(ItemStack itemstack) {
      if (itemstack.hasTagCompound()) {
         AspectList aspects = new AspectList();
         aspects.readFromNBT(itemstack.getTagCompound());
         return aspects.size() > 0 ? aspects : null;
      } else {
         return null;
      }
   }

   public void setAspects(ItemStack itemstack, AspectList aspects) {
      if (!itemstack.hasTagCompound()) {
         itemstack.setTagCompound(new NBTTagCompound());
      }

      aspects.writeToNBT(itemstack.getTagCompound());
   }

   @Override
   public EnumActionResult onItemUse(EntityPlayer par2EntityPlayer, World par3World, BlockPos pos, EnumHand hand, EnumFacing facing, float par8, float par9, float par10) {
      ItemStack par1ItemStack = par2EntityPlayer.getHeldItem(hand);
      int par4 = pos.getX();
      int par5 = pos.getY();
      int par6 = pos.getZ();
      if (par2EntityPlayer.canPlayerEdit(pos, facing, par1ItemStack) && facing == EnumFacing.DOWN) {
         Biome biome = par3World.getBiome(pos);
         boolean magicBiome = false;
         if (biome != null) {
            magicBiome = BiomeDictionary.hasType(biome, Type.MAGICAL);
         }

         if (!magicBiome) {
            return EnumActionResult.FAIL;
         } else {
            Block i1 = par3World.getBlockState(pos).getBlock();
            if (i1 != Blocks.LOG && i1 != Blocks.LOG2 && i1 != ConfigBlocks.blockMagicalLog) {
               return EnumActionResult.FAIL;
            } else {
               --par5;
               BlockPos placePos = new BlockPos(par4, par5, par6);
               if (par3World.isAirBlock(placePos)) {
                  par3World.setBlockState(placePos, ConfigBlocks.blockManaPod.getDefaultState(), 2);
                  TileEntity tile = par3World.getTileEntity(placePos);
                  if (tile instanceof TileManaPod && this.getAspects(par1ItemStack) != null && this.getAspects(par1ItemStack).size() > 0) {
                     ((TileManaPod)tile).aspect = this.getAspects(par1ItemStack).getAspects()[0];
                  }

                  if (!par2EntityPlayer.capabilities.isCreativeMode) {
                     par1ItemStack.shrink(1);
                  }
               }

               return EnumActionResult.SUCCESS;
            }
         }
      } else {
         return EnumActionResult.FAIL;
      }
   }

   static {
      displayAspects = Aspect.aspects.values().toArray(new Aspect[0]);
   }
}
