package thaumcraft.common.items;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.block.Block;
import thaumcraft.client.renderers.compat.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.World;
import tc4tweak.ConfigurationHandler;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssentiaContainerItem;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.tiles.TileAlembic;
import thaumcraft.common.tiles.TileJarFillable;

import static thaumcraft.api.aspects.AspectList.addAspectDescriptionToList;
import net.minecraft.util.math.BlockPos;

public class ItemEssence extends Item implements IEssentiaContainerItem {
   public TextureAtlasSprite icon;
   public TextureAtlasSprite iconOverlay;

   public ItemEssence() {
      this.setMaxStackSize(64);
      this.setHasSubtypes(true);
      this.setMaxDamage(0);
      this.setCreativeTab(Thaumcraft.tabTC);
   }

   @SideOnly(Side.CLIENT)
   public void registerIcons(IIconRegister ir) {
      this.icon = ir.registerSprite("thaumcraft:phial");
      this.iconOverlay = ir.registerSprite("thaumcraft:essence");
   }

   public TextureAtlasSprite getIconFromDamage(int par1) {
      return this.icon;
   }

   @SideOnly(Side.CLIENT)
   public int getRenderPasses(int metadata) {
      return metadata == 0 ? 1 : 2;
   }

   @SideOnly(Side.CLIENT)
   public TextureAtlasSprite getIconFromDamageForRenderPass(int par1, int par2) {
      return par1 != 0 && par2 != 0 ? this.iconOverlay : this.icon;
   }

   @SideOnly(Side.CLIENT)
   public int getColorFromItemStack(ItemStack stack, int par2) {
      if (stack.getItemDamage() != 0 && par2 != 0) {
         return stack.getItemDamage() == 1 && this.getAspects(stack) != null ? this.getAspects(stack).getAspects()[0].getColor() : 16777215;
      } else {
         return 16777215;
      }
   }

   @SideOnly(Side.CLIENT)
   public boolean requiresMultipleRenderPasses() {
      return true;
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void getSubItems(CreativeTabs par2CreativeTabs, net.minecraft.util.NonNullList<ItemStack> stacks) {
      stacks.add(new ItemStack(this, 1, 0));

      for(Aspect tag : Aspect.aspects.values()) {
         ItemStack i = new ItemStack(this, 1, 1);
         this.setAspects(i, (new AspectList()).add(tag, 8));
         stacks.add(i);
      }

   }

   @Override
   public String getTranslationKey(ItemStack par1ItemStack) {
      return getTranslationKey() + "." + par1ItemStack.getItemDamage();
   }

   public void addInformation(ItemStack stack, @javax.annotation.Nullable net.minecraft.world.World worldIn, List list, net.minecraft.client.util.ITooltipFlag flagIn) {
      AspectList aspects = this.getAspects(stack);
      addAspectDescriptionToList(aspects, net.minecraft.client.Minecraft.getMinecraft().player, list);

      super.addInformation(stack, worldIn, list, flagIn);
   }

   public static boolean addItemStackToInventory_tweaked(InventoryPlayer inv, ItemStack itemStack) {
      if (!ConfigurationHandler.INSTANCE.isAlternativeAddStack()) {
         return inv.addItemStackToInventory(itemStack);
      }
      // first check if a partial stack exists
      for (ItemStack stack : inv.mainInventory) {
         // empty or same stack
         if (stack == null || !stack.isItemEqual(itemStack) || !ItemStack.areItemStackTagsEqual(itemStack, stack) ||
                 // space left
                 stack.isEmpty() || stack.getCount() >= stack.getMaxStackSize()) {
            continue;
         }
         int toAdd = Math.min(stack.getMaxStackSize() - stack.getCount(), itemStack.getCount());
         itemStack.shrink(toAdd);
         stack.grow(toAdd);
         if (itemStack.isEmpty()) {
            return true;
         }
      }
      // then try to add to current active slot if it's now empty
      ItemStack currentStack = inv.mainInventory.get(inv.currentItem);
      if (currentStack == null || currentStack.getItem() == null || currentStack.isEmpty()) {
         inv.mainInventory.set(inv.currentItem, itemStack);
         return true;
      }
      // fallback to vanilla logic if both failed
      return inv.addItemStackToInventory(itemStack);
   }
   public net.minecraft.util.EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos blockPos, net.minecraft.util.EnumFacing facing, float f1, float f2, float f3, net.minecraft.util.EnumHand hand) {
      ItemStack itemstack = player.getHeldItem(hand);
      int x = blockPos.getX(), y = blockPos.getY(), z = blockPos.getZ();
      Block bi = world.getBlockState(blockPos).getBlock();
      int md = world.getBlockState(blockPos).getBlock().getMetaFromState(world.getBlockState(blockPos));
      if (itemstack.getItemDamage() == 0 && bi == ConfigBlocks.blockMetalDevice && md == 1) {
         TileAlembic tile = (TileAlembic)world.getTileEntity(blockPos);
         if (tile.amount >= 8) {
            if (world.isRemote) {
               player.swingArm(net.minecraft.util.EnumHand.MAIN_HAND);
               return net.minecraft.util.EnumActionResult.PASS;
            }

            ItemStack phial = new ItemStack(this, 1, 1);
            this.setAspects(phial, (new AspectList()).add(tile.aspect, 8));
            if (tile.takeFromContainer(tile.aspect, 8)) {
               itemstack.shrink(1);
               if (!addItemStackToInventory_tweaked(player.inventory,phial)) {
                  world.spawnEntity(new EntityItem(world, (float)x + 0.5F, (float)y + 0.5F, (float)z + 0.5F, phial));
               }

               { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("minecraft:game.neutral.swim")); if (_snd != null) world.playSound(null, player.posX, player.posY, player.posZ, _snd, net.minecraft.util.SoundCategory.NEUTRAL, 0.25F, 1.0F); };
               player.inventoryContainer.detectAndSendChanges();
               return net.minecraft.util.EnumActionResult.SUCCESS;
            }
         }
      }

      if (itemstack.getItemDamage() == 0 && bi == ConfigBlocks.blockJar && (md == 0 || md == 3)) {
         TileJarFillable tile = (TileJarFillable)world.getTileEntity(blockPos);
         if (tile.amount >= 8) {
            if (world.isRemote) {
               player.swingArm(net.minecraft.util.EnumHand.MAIN_HAND);
               return net.minecraft.util.EnumActionResult.PASS;
            }

            Aspect asp = Aspect.getAspect(tile.aspect.getTag());
            if (tile.takeFromContainer(asp, 8)) {
               itemstack.shrink(1);
               ItemStack phial = new ItemStack(this, 1, 1);
               this.setAspects(phial, (new AspectList()).add(asp, 8));
               if (!addItemStackToInventory_tweaked(player.inventory,phial)) {
                  world.spawnEntity(new EntityItem(world, (float)x + 0.5F, (float)y + 0.5F, (float)z + 0.5F, phial));
               }

               { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("minecraft:game.neutral.swim")); if (_snd != null) world.playSound(null, player.posX, player.posY, player.posZ, _snd, net.minecraft.util.SoundCategory.NEUTRAL, 0.25F, 1.0F); };
               player.inventoryContainer.detectAndSendChanges();
               return net.minecraft.util.EnumActionResult.SUCCESS;
            }
         }
      }

      AspectList al = this.getAspects(itemstack);
      if (al != null && al.size() == 1) {
         Aspect aspect = al.getAspects()[0];
         if (itemstack.getItemDamage() != 0 && bi == ConfigBlocks.blockJar && (md == 0 || md == 3)) {
            TileJarFillable tile = (TileJarFillable)world.getTileEntity(blockPos);
            if (tile.amount <= tile.maxAmount - 8 && tile.doesContainerAccept(aspect)) {
               if (world.isRemote) {
                  player.swingArm(net.minecraft.util.EnumHand.MAIN_HAND);
                  return net.minecraft.util.EnumActionResult.PASS;
               }

               if (tile.addToContainer(aspect, 8) == 0) {
                  { net.minecraft.block.state.IBlockState _bs = world.getBlockState(blockPos); world.notifyBlockUpdate(blockPos, _bs, _bs, 3); }
                  tile.markDirty();
                  itemstack.shrink(1);
                  if (!addItemStackToInventory_tweaked(player.inventory,new ItemStack(this, 1, 0))) {
                     world.spawnEntity(new EntityItem(world, (float)x + 0.5F, (float)y + 0.5F, (float)z + 0.5F, new ItemStack(this, 1, 0)));
                  }

                  { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("minecraft:game.neutral.swim")); if (_snd != null) world.playSound(null, player.posX, player.posY, player.posZ, _snd, net.minecraft.util.SoundCategory.NEUTRAL, 0.25F, 1.0F); };
                  player.inventoryContainer.detectAndSendChanges();
                  return net.minecraft.util.EnumActionResult.SUCCESS;
               }
            }
         }
      }

      return net.minecraft.util.EnumActionResult.PASS;
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
}
