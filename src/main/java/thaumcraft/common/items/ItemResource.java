package thaumcraft.common.items;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssentiaContainerItem;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.Config;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.entities.EntityAspectOrb;
import thaumcraft.common.entities.projectile.EntityAlumentum;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.playerdata.PacketAspectPool;
import thaumcraft.common.lib.network.playerdata.PacketResearchComplete;
import thaumcraft.common.lib.research.ResearchManager;
import thaumcraft.common.lib.utils.InventoryUtils;

import static thaumcraft.api.aspects.AspectList.addAspectDescriptionToList;

public class ItemResource extends Item implements IEssentiaContainerItem {
   public TextureAtlasSprite[] icon = new TextureAtlasSprite[19];
   public TextureAtlasSprite iconOverlay;

   public ItemResource() {
      this.setMaxStackSize(64);
      this.setHasSubtypes(true);
      this.setMaxDamage(0);
      this.setCreativeTab(Thaumcraft.tabTC);
   }

   @SideOnly(Side.CLIENT)
   public void registerIcons(thaumcraft.client.renderers.compat.IIconRegister ir) {
      this.icon[0] = ir.registerSprite("thaumcraft:alumentum");
      this.icon[1] = ir.registerSprite("thaumcraft:nitor");
      this.icon[2] = ir.registerSprite("thaumcraft:thaumiumingot");
      this.icon[3] = ir.registerSprite("thaumcraft:quicksilver");
      this.icon[4] = ir.registerSprite("thaumcraft:tallow");
      this.icon[5] = ir.registerSprite("thaumcraft:brain");
      this.icon[6] = ir.registerSprite("thaumcraft:amber");
      this.icon[7] = ir.registerSprite("thaumcraft:cloth");
      this.icon[8] = ir.registerSprite("thaumcraft:filter");
      this.icon[9] = ir.registerSprite("thaumcraft:knowledgefragment");
      this.icon[10] = ir.registerSprite("thaumcraft:mirrorglass");
      this.icon[11] = ir.registerSprite("thaumcraft:taint_slime");
      this.icon[12] = ir.registerSprite("thaumcraft:taint_tendril");
      this.icon[13] = ir.registerSprite("thaumcraft:label");
      this.iconOverlay = ir.registerSprite("thaumcraft:label_over");
      this.icon[14] = ir.registerSprite("thaumcraft:dust");
      this.icon[15] = ir.registerSprite("thaumcraft:charm");
      this.icon[16] = ir.registerSprite("thaumcraft:voidingot");
      this.icon[17] = ir.registerSprite("thaumcraft:voidseed");
      this.icon[18] = ir.registerSprite("thaumcraft:coin");
   }

   @SideOnly(Side.CLIENT)
   public TextureAtlasSprite getIconFromDamage(int par1) {
      return this.icon[par1];
   }

   @SideOnly(Side.CLIENT)
   public int getRenderPasses(int metadata) {
      return metadata == 13 ? 2 : 1;
   }

   @SideOnly(Side.CLIENT)
   public TextureAtlasSprite getIcon(ItemStack stack, int pass) {
      return pass != 0 && this.getAspects(stack) != null ? this.iconOverlay : this.getIconFromDamage(stack.getItemDamage());
   }

   @SideOnly(Side.CLIENT)
   public int getColorFromItemStack(ItemStack stack, int par2) {
      return par2 == 1 && stack.getItemDamage() == 13 && this.getAspects(stack) != null ? this.getAspects(stack).getAspects()[0].getColor() : 16777215;
   }

   @SideOnly(Side.CLIENT)
   public boolean requiresMultipleRenderPasses() {
      return true;
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void getSubItems(CreativeTabs par2CreativeTabs, net.minecraft.util.NonNullList<ItemStack> par3List) {
      for(int a = 0; a <= 18; ++a) {
         if (a != 5) {
            par3List.add(new ItemStack(this, 1, a));
         }
      }

   }

   @Override
   public String getTranslationKey(ItemStack par1ItemStack) {
      return getTranslationKey() + "." + par1ItemStack.getItemDamage();
   }

   public void onUpdate(ItemStack stack, World world, Entity entity, int par4, boolean par5) {
      super.onUpdate(stack, world, entity, par4, par5);
      if (!entity.world.isRemote && (stack.getItemDamage() == 11 || stack.getItemDamage() == 12) && entity instanceof EntityLivingBase && !((EntityLivingBase)entity).isEntityUndead() && !((EntityLivingBase)entity).isPotionActive(Potion.getPotionById(Config.potionTaintPoisonID)) && world.rand.nextInt(4321) <= stack.getCount()) {
         ((EntityLivingBase)entity).addPotionEffect(new PotionEffect(Potion.getPotionById(Config.potionTaintPoisonID), 120, 0, false, false));
         if (entity instanceof EntityPlayer) {
            String s = I18n.translateToLocal("tc.taint_item_poison").replace("%s", "§5§o" + stack.getDisplayName() + "§r");
            ((EntityPlayer)entity).sendMessage(new TextComponentTranslation(s));
            InventoryUtils.consumeInventoryItem((EntityPlayer)entity, stack.getItem(), stack.getItemDamage());
         }
      } else if (!entity.world.isRemote && stack.getItemDamage() == 15) {
         int r = world.rand.nextInt(20000);
         if (stack.hasTagCompound() && stack.getTagCompound().hasKey("blurb")) {
            stack.getTagCompound().removeTag("blurb");
         }

         if (r < 20) {
            Aspect aspect = null;
            switch (world.rand.nextInt(6)) {
               case 0:
                  aspect = Aspect.AIR;
                  break;
               case 1:
                  aspect = Aspect.EARTH;
                  break;
               case 2:
                  aspect = Aspect.FIRE;
                  break;
               case 3:
                  aspect = Aspect.WATER;
                  break;
               case 4:
                  aspect = Aspect.ORDER;
                  break;
               case 5:
                  aspect = Aspect.ENTROPY;
            }

            if (aspect != null) {
               EntityAspectOrb orb = new EntityAspectOrb(world, entity.posX, entity.posY, entity.posZ, aspect, 1);
               world.spawnEntity(orb);
            }
         } else if (r == 42 && entity instanceof EntityPlayer && !ResearchManager.isResearchComplete(entity.getName(), "FOCUSPRIMAL") && !ResearchManager.isResearchComplete(entity.getName(), "@FOCUSPRIMAL")) {
            ((EntityPlayer)entity).sendMessage(new TextComponentTranslation("§5§o" + I18n.translateToLocal("tc.primalcharm.trigger")));
            PacketHandler.INSTANCE.sendTo(new PacketResearchComplete("@FOCUSPRIMAL"), (EntityPlayerMP)entity);
            Thaumcraft.proxy.getResearchManager().completeResearch((EntityPlayer)entity, "@FOCUSPRIMAL");
         }
      }

   }

   @Override
   public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
      ItemStack itemstack = player.getHeldItem(hand);
      if (itemstack.getItemDamage() != 1) {
         return super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
      } else {
         int x = pos.getX();
         int y = pos.getY();
         int z = pos.getZ();
         int par7 = facing.getIndex();

         Block var11 = world.getBlockState(new BlockPos(x, y, z)).getBlock();
         if (var11 == Blocks.SNOW_LAYER && (world.getBlockState(new BlockPos(x, y, z)).getBlock().getMetaFromState(world.getBlockState(new BlockPos(x, y, z))) & 7) < 1) {
            par7 = 1;
         } else if (var11 != Blocks.VINE && var11 != Blocks.TALLGRASS && var11 != Blocks.DEADBUSH && !var11.isReplaceable(world, new BlockPos(x, y, z))) {
            if (par7 == 0) {
               --y;
            }

            if (par7 == 1) {
               ++y;
            }

            if (par7 == 2) {
               --z;
            }

            if (par7 == 3) {
               ++z;
            }

            if (par7 == 4) {
               --x;
            }

            if (par7 == 5) {
               ++x;
            }
         }

         if (itemstack.getCount() == 0) {
            return EnumActionResult.FAIL;
         } else if (!player.canPlayerEdit(new BlockPos(x, y, z), facing, itemstack)) {
            return EnumActionResult.FAIL;
         } else if (world.mayPlace(ConfigBlocks.blockAiry, new BlockPos(x, y, z), false, facing, player)) {
            if (this.placeBlockAt(itemstack, player, world, x, y, z, par7, hitX, hitY, hitZ, ConfigBlocks.blockAiry, itemstack.getItemDamage())) {
               { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("minecraft:block.stone.place")); if (_snd != null) world.playSound(null, x + 0.5, y + 0.5, z + 0.5, _snd, net.minecraft.util.SoundCategory.BLOCKS, 0.8F, 0.8F); }
               itemstack.shrink(1);
               return EnumActionResult.SUCCESS;
            } else {
               return EnumActionResult.FAIL;
            }
         } else {
            return EnumActionResult.FAIL;
         }
      }
   }

   public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, Block bid, int metadata) {
      if (!world.setBlockState(new BlockPos(x, y, z), (bid).getStateFromMeta(metadata), 3)) {
         return false;
      } else {
         if (world.getBlockState(new BlockPos(x, y, z)).getBlock() == bid) {
            bid.onBlockPlacedBy(world, new BlockPos(x, y, z), bid.getDefaultState(), player, stack);
            // onPostBlockPlaced removed in 1.12.2
         }

         return true;
      }
   }

   @Override
   public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
      ItemStack itemstack = player.getHeldItem(hand);
      if (itemstack.getItemDamage() == 0) {
         if (!player.capabilities.isCreativeMode) {
            itemstack.shrink(1);
         }

         { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("minecraft:random.bow")); if (_snd != null) world.playSound(null, player.posX, player.posY, player.posZ, _snd, net.minecraft.util.SoundCategory.NEUTRAL, 0.3F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F)); }
         if (!world.isRemote) {
            world.spawnEntity(new EntityAlumentum(world, player));
         }
      } else if (itemstack.getItemDamage() == 9) {
         if (!player.capabilities.isCreativeMode) {
            itemstack.shrink(1);
         }

         if (!world.isRemote) {
            for(Aspect a : Aspect.getPrimalAspects()) {
               short q = (short)(world.rand.nextInt(2) + 1);
               Thaumcraft.proxy.playerKnowledge.addAspectPool(player.getName(), a, q);
               ResearchManager.scheduleSave(player);
               PacketHandler.INSTANCE.sendTo(new PacketAspectPool(a.getTag(), q, Thaumcraft.proxy.playerKnowledge.getAspectPoolFor(player.getName(), a)), (EntityPlayerMP)player);
            }
         }
      }

      return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
   }

   public void addInformation(ItemStack stack, @javax.annotation.Nullable net.minecraft.world.World worldIn, List list, net.minecraft.client.util.ITooltipFlag flagIn) {
      AspectList aspects = this.getAspects(stack);
      addAspectDescriptionToList(aspects, net.minecraft.client.Minecraft.getMinecraft().player, list);

      if (stack.getItemDamage() == 15) {
         Random rand = new Random(stack.hashCode() + (int)(System.currentTimeMillis() / 1000L) / 120);
         int r = rand.nextInt(200);
         if (r < 25) {
            list.add("§6" + I18n.translateToLocal("tc.primalcharm." + rand.nextInt(5)));
         }
      }

      super.addInformation(stack, worldIn, list, flagIn);
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

   public int getItemStackLimit(ItemStack stack) {
      return stack.getItemDamage() == 15 ? 1 : super.getItemStackLimit(stack);
   }
}
