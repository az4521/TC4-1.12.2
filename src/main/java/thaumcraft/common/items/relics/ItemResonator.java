package thaumcraft.common.items.relics;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.List;
import thaumcraft.client.renderers.compat.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.codechicken.lib.raytracer.RayTracer;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.tiles.TileTubeBuffer;
import net.minecraft.util.math.BlockPos;

public class ItemResonator extends Item {
   private TextureAtlasSprite icon;

   public ItemResonator() {
      this.setMaxStackSize(1);
      this.setHasSubtypes(false);
      this.setMaxDamage(0);
      this.setCreativeTab(Thaumcraft.tabTC);
   }

   @SideOnly(Side.CLIENT)
   public void registerIcons(IIconRegister par1IconRegister) {
      this.icon = par1IconRegister.registerSprite("thaumcraft:resonator");
   }

   public TextureAtlasSprite getIconFromDamage(int par1) {
      return this.icon;
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void getSubItems(CreativeTabs par2CreativeTabs, net.minecraft.util.NonNullList<ItemStack> par3List) {
      par3List.add(new ItemStack(this));
   }

   public EnumRarity getRarity(ItemStack itemstack) {
      return EnumRarity.UNCOMMON;
   }

   public boolean hasEffect(ItemStack par1ItemStack) {
      return par1ItemStack.hasTagCompound();
   }

   @Override
   public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos blockPos, EnumFacing facing, float hitX, float hitY, float hitZ, EnumHand hand) {
      int x = blockPos.getX(), y = blockPos.getY(), z = blockPos.getZ();
      TileEntity tile = world.getTileEntity(blockPos);
      if (tile instanceof IEssentiaTransport) {
         if (world.isRemote) {
            player.swingArm(net.minecraft.util.EnumHand.MAIN_HAND);
            return EnumActionResult.PASS;
         } else {
            IEssentiaTransport et = (IEssentiaTransport)tile;
            EnumFacing face = facing;
            RayTraceResult hit = RayTracer.retraceBlock(world, player, x, y, z);
            if (hit != null && hit.subHit >= 0 && hit.subHit < 6) {
               face = EnumFacing.byIndex(hit.subHit);
            }

            if (!(tile instanceof TileTubeBuffer) && et.getEssentiaType(face) != null) {
               player.sendMessage(new TextComponentTranslation("tc.resonator1", "" + et.getEssentiaAmount(face), et.getEssentiaType(face).getName()));
            } else if (tile instanceof TileTubeBuffer && ((IAspectContainer)tile).getAspects().size() > 0) {
               for(Aspect aspect : ((IAspectContainer)tile).getAspects().getAspectsSorted()) {
                  player.sendMessage(new TextComponentTranslation("tc.resonator1", "" + ((IAspectContainer)tile).getAspects().getAmount(aspect), aspect.getName()));
               }
            }

            String s = I18n.translateToLocal("tc.resonator3");
            if (et.getSuctionType(face) != null) {
               s = et.getSuctionType(face).getName();
            }

            player.sendMessage(new TextComponentTranslation("tc.resonator2", "" + et.getSuctionAmount(face), s));
            { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:alembicknock")); if (_snd != null) world.playSound(null, x, y, z, _snd, net.minecraft.util.SoundCategory.NEUTRAL, 0.5F, 1.9F + world.rand.nextFloat() * 0.1F); }
            return EnumActionResult.SUCCESS;
         }
      } else {
         return EnumActionResult.PASS;
      }
   }
}
