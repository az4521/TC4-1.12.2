package thaumcraft.common.items;

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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.nodes.NodeModifier;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.playerdata.PacketResearchComplete;
import thaumcraft.common.lib.research.ResearchManager;
import thaumcraft.common.tiles.TileNode;
import net.minecraft.util.math.BlockPos;

public class ItemEldritchObject extends Item {
   public TextureAtlasSprite[] icon = new TextureAtlasSprite[5];

   public ItemEldritchObject() {
      this.setMaxStackSize(1);
      this.setHasSubtypes(true);
      this.setMaxDamage(0);
      this.setCreativeTab(Thaumcraft.tabTC);
   }

   @SideOnly(Side.CLIENT)
   public void registerIcons(IIconRegister ir) {
      this.icon[0] = ir.registerSprite("thaumcraft:eldritch_object");
      this.icon[1] = ir.registerSprite("thaumcraft:crimson_rites");
      this.icon[2] = ir.registerSprite("thaumcraft:eldritch_object_2");
      this.icon[3] = ir.registerSprite("thaumcraft:eldritch_object_3");
      this.icon[4] = ir.registerSprite("thaumcraft:ob_placer");
   }

   @SideOnly(Side.CLIENT)
   public TextureAtlasSprite getIconFromDamage(int par1) {
      return par1 < this.icon.length ? this.icon[par1] : this.icon[0];
   }

   @Override
   public String getTranslationKey(ItemStack par1ItemStack) {
      return getTranslationKey() + "." + par1ItemStack.getItemDamage();
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void getSubItems(CreativeTabs par2CreativeTabs, net.minecraft.util.NonNullList<ItemStack> par3List) {
      par3List.add(new ItemStack(this, 1, 0));
      par3List.add(new ItemStack(this, 1, 1));
      par3List.add(new ItemStack(this, 1, 2));
      par3List.add(new ItemStack(this, 1, 3));
      par3List.add(new ItemStack(this, 1, 4));
   }

   public EnumRarity getRarity(ItemStack stack) {
      switch (stack.getItemDamage()) {
         case 2:
            return EnumRarity.RARE;
         case 3:
            return EnumRarity.EPIC;
         default:
            return EnumRarity.UNCOMMON;
      }
   }

   public void addInformation(ItemStack stack, @javax.annotation.Nullable net.minecraft.world.World worldIn, List list, net.minecraft.client.util.ITooltipFlag flagIn) {
      super.addInformation(stack, worldIn, list, flagIn);
      if (stack != null) {
         switch (stack.getItemDamage()) {
            case 0:
               list.add(TextFormatting.DARK_PURPLE + I18n.translateToLocal("item.ItemEldritchObject.text.1"));
               break;
            case 1:
               list.add(TextFormatting.DARK_PURPLE + I18n.translateToLocal("item.ItemEldritchObject.text.2"));
               list.add(TextFormatting.DARK_BLUE + I18n.translateToLocal("item.ItemEldritchObject.text.3"));
               break;
            case 2:
               list.add(TextFormatting.DARK_PURPLE + I18n.translateToLocal("item.ItemEldritchObject.text.4"));
               break;
            case 3:
               list.add(TextFormatting.DARK_PURPLE + I18n.translateToLocal("item.ItemEldritchObject.text.5"));
               list.add(TextFormatting.DARK_PURPLE + I18n.translateToLocal("item.ItemEldritchObject.text.6"));
               break;
            case 4:
               list.add("§oCreative Mode Only");
         }
      }

   }

   public static void unlockResearchForPlayer(World world,EntityPlayerMP player,String research,String... preRequsites) {
      for (String preReq : preRequsites) {
         if (!ResearchManager.isResearchComplete(player.getName(), preReq)){return;}
      }
      if (ResearchManager.isResearchComplete(player.getName(), research)){return;}
      PacketHandler.INSTANCE.sendTo(new PacketResearchComplete(research), player);
      Thaumcraft.proxy.getResearchManager().completeResearch(player, research);
      { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:learn")); if (_snd != null) world.playSound(null, player.posX, player.posY, player.posZ, _snd, net.minecraft.util.SoundCategory.NEUTRAL, 0.75F, 1.0F); };
   }

   @Override
   public net.minecraft.util.ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, net.minecraft.util.EnumHand hand) {
      ItemStack stack = player.getHeldItem(hand);
      if (!world.isRemote){
         if (player instanceof EntityPlayerMP) {
            EntityPlayerMP playerMP = (EntityPlayerMP) player;
            if (stack.getItemDamage() == 1) {
               unlockResearchForPlayer(world, playerMP, "CRIMSON");
            }

            //primal pearl research bug fix
            //anyway they got pearl.give research
            if (stack.getItemDamage() == 3){
               unlockResearchForPlayer(world, playerMP, "PRIMPEARL", "ELDRITCHMINOR");
            }
         }
      }

      return new net.minecraft.util.ActionResult<>(net.minecraft.util.EnumActionResult.SUCCESS, stack);
   }

   @Override
   public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos blockPos, EnumFacing facing, float hitX, float hitY, float hitZ, EnumHand hand) {
      int x = blockPos.getX(), y = blockPos.getY(), z = blockPos.getZ();
      ItemStack itemstack = player.getHeldItem(hand);
      if (itemstack.getItemDamage() != 3) {
         if (facing == EnumFacing.UP && itemstack.getItemDamage() == 4) {
            player.swingArm(net.minecraft.util.EnumHand.MAIN_HAND);

            for(int a = 1; a <= 6; ++a) {
               if (!world.isAirBlock(new BlockPos(x, y + a, z))) {
                  return EnumActionResult.FAIL;
               }
            }
        world.setBlockState(new net.minecraft.util.math.BlockPos(x, y + 1, z), (ConfigBlocks.blockEldritch).getStateFromMeta(0), 3);
        world.setBlockState(new net.minecraft.util.math.BlockPos(x, y + 3, z), (ConfigBlocks.blockEldritch).getStateFromMeta(1), 3);
        world.setBlockState(new net.minecraft.util.math.BlockPos(x, y + 4, z), (ConfigBlocks.blockEldritch).getStateFromMeta(2), 3);
        world.setBlockState(new net.minecraft.util.math.BlockPos(x, y + 5, z), (ConfigBlocks.blockEldritch).getStateFromMeta(2), 3);
        world.setBlockState(new net.minecraft.util.math.BlockPos(x, y + 6, z), (ConfigBlocks.blockEldritch).getStateFromMeta(2), 3);
        world.setBlockState(new net.minecraft.util.math.BlockPos(x, y + 7, z), (ConfigBlocks.blockEldritch).getStateFromMeta(2), 3);
            return world.isRemote ? EnumActionResult.PASS : EnumActionResult.SUCCESS;
         } else {
            return EnumActionResult.PASS;
         }
      } else {
         TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
         if (te instanceof TileNode) {
            player.swingArm(net.minecraft.util.EnumHand.MAIN_HAND);
            if (!world.isRemote) {
               itemstack.shrink(1);
               TileNode node = (TileNode)te;
               boolean research = ThaumcraftApiHelper.isResearchComplete(player.getName(), "PRIMNODE");

               for(Aspect a : node.getAspects().getAspects()) {
                  int m = node.getNodeVisBase(a);
                  if (!a.isPrimal()) {
                     if (world.rand.nextBoolean()) {
                        node.setNodeVisBase(a, (short)(m - 1));
                     }
                  } else {
                     m = m - 2 + world.rand.nextInt(research ? 9 : 6);
                     node.setNodeVisBase(a, (short)m);
                  }
               }

               for(Aspect a : Aspect.getPrimalAspects()) {
                  int m = node.getNodeVisBase(a);
                  int r = world.rand.nextInt(research ? 4 : 3);
                  if (r > 0 && r > m) {
                     node.setNodeVisBase(a, (short)r);
                     node.addToContainer(a, 1);
                  }
               }

               if (node.getNodeModifier() == NodeModifier.FADING && world.rand.nextBoolean()) {
                  node.setNodeModifier(NodeModifier.PALE);
               } else if (node.getNodeModifier() == NodeModifier.PALE && world.rand.nextBoolean()) {
                  node.setNodeModifier(null);
               } else if (node.getNodeModifier() == null && world.rand.nextInt(5) == 0) {
                  node.setNodeModifier(NodeModifier.BRIGHT);
               }

               { net.minecraft.block.state.IBlockState _bs = world.getBlockState(new BlockPos(x, y, z)); world.notifyBlockUpdate(new BlockPos(x, y, z), _bs, _bs, 3); }
               node.markDirty();
               world.createExplosion(null, (double)x + (double)0.5F, (double)y + (double)1.5F, (double)z + (double)0.5F, 3.0F + world.rand.nextFloat() * (float)(research ? 3 : 5), true);

               for(int a = 0; a < 33; ++a) {
                  int xx = x + world.rand.nextInt(6) - world.rand.nextInt(6);
                  int yy = y + world.rand.nextInt(6) - world.rand.nextInt(6);
                  int zz = z + world.rand.nextInt(6) - world.rand.nextInt(6);
                  if (world.isAirBlock(new BlockPos(xx, yy, zz))) {
                     if (yy < y) {
        world.setBlockState(new net.minecraft.util.math.BlockPos(xx, yy, zz), (ConfigBlocks.blockFluxGoo).getStateFromMeta(8), 3);
                     } else {
        world.setBlockState(new net.minecraft.util.math.BlockPos(xx, yy, zz), (ConfigBlocks.blockFluxGas).getStateFromMeta(8), 3);
                     }
                  }
               }

               return EnumActionResult.SUCCESS;
            }
         }

         return EnumActionResult.PASS;
      }
   }
}
