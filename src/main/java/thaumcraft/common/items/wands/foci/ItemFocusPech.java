package thaumcraft.common.items.wands.foci;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.client.renderers.compat.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.entities.projectile.EntityPechBlast;
import thaumcraft.common.items.wands.ItemWandCasting;

public class ItemFocusPech extends ItemFocusBasic {
   TextureAtlasSprite depthIcon = null;
   private static final AspectList cost;
   private static final AspectList costAll;
   public static FocusUpgradeType nightshade;

   public ItemFocusPech() {
      this.setCreativeTab(Thaumcraft.tabTC);
   }

   public String getSortingHelper(ItemStack itemstack) {
      return "PP" + super.getSortingHelper(itemstack);
   }

   @SideOnly(Side.CLIENT)
   public void registerIcons(IIconRegister ir) {
      this.icon = ir.registerSprite("thaumcraft:focus_pech");
      this.depthIcon = ir.registerSprite("thaumcraft:focus_pech_depth");
   }

   public TextureAtlasSprite getFocusDepthLayerIcon(ItemStack itemstack) {
      return this.depthIcon;
   }

   public int getActivationCooldown(ItemStack focusstack) {
      return 250;
   }

   public ItemStack onFocusRightClick(ItemStack itemstack, World world, EntityPlayer p, RayTraceResult mob) {
      ItemWandCasting wand = (ItemWandCasting)itemstack.getItem();
      EntityPechBlast blast = new EntityPechBlast(world, p, wand.getFocusPotency(itemstack), wand.getFocusExtend(itemstack), this.isUpgradedWith(wand.getFocusItem(itemstack), nightshade));
      if (!world.isRemote && wand.consumeAllVis(itemstack, p, this.getVisCost(itemstack), true, false)) {
         world.spawnEntity(blast);
         { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:ice")); if (_snd != null) world.playSound(null, blast.posX, blast.posY, blast.posZ, _snd, net.minecraft.util.SoundCategory.NEUTRAL, 0.4F, 1.0F + world.rand.nextFloat() * 0.1F); };
      }

      p.swingArm(net.minecraft.util.EnumHand.MAIN_HAND);
      return itemstack;
   }

   public int getFocusColor(ItemStack itemstack) {
      return 2267460;
   }

   public AspectList getVisCost(ItemStack itemstack) {
      return this.isUpgradedWith(itemstack, nightshade) ? costAll : cost;
   }

   public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack itemstack, int rank) {
      switch (rank) {
         case 1:
            return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency};
         case 2:
            return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency, FocusUpgradeType.extend};
         case 3:
            return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency};
         case 4:
            return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency, FocusUpgradeType.extend};
         case 5:
            return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency, nightshade};
         default:
            return null;
      }
   }

   static {
      cost = (new AspectList()).add(Aspect.EARTH, 10).add(Aspect.ENTROPY, 10).add(Aspect.WATER, 10);
      costAll = (new AspectList()).add(Aspect.AIR, 10).add(Aspect.FIRE, 10).add(Aspect.EARTH, 10).add(Aspect.ORDER, 10).add(Aspect.ENTROPY, 10).add(Aspect.WATER, 10);
      nightshade = new FocusUpgradeType(15, new ResourceLocation("thaumcraft", "textures/foci/nightshade.png"), "focus.upgrade.nightshade.name", "focus.upgrade.nightshade.text", (new AspectList()).add(Aspect.LIFE, 1).add(Aspect.POISON, 1).add(Aspect.MAGIC, 1));
   }
}
