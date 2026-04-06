package thaumcraft.common.items.wands.foci;

import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.ArrayList;
import thaumcraft.client.renderers.compat.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.client.fx.bolt.FXLightningBolt;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.entities.projectile.EntityShockOrb;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumcraft.common.items.wands.WandManager;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.fx.PacketFXZap;
import thaumcraft.common.lib.utils.BlockUtils;
import thaumcraft.common.lib.utils.EntityUtils;

public class ItemFocusShock extends ItemFocusBasic {
   private static final AspectList costBase;
   private static final AspectList costChain;
   private static final AspectList costGround;
   public static FocusUpgradeType chainlightning;
   public static FocusUpgradeType earthshock;

   public ItemFocusShock() {
      this.setCreativeTab(Thaumcraft.tabTC);
   }

   @SideOnly(Side.CLIENT)
   public void registerIcons(IIconRegister ir) {
      this.icon = ir.registerSprite("thaumcraft:focus_shock");
   }

   public String getSortingHelper(ItemStack itemstack) {
      return "BL" + super.getSortingHelper(itemstack);
   }

   public int getFocusColor(ItemStack itemstack) {
      return 10466239;
   }

   public AspectList getVisCost(ItemStack itemstack) {
      return this.isUpgradedWith(itemstack, chainlightning) ? costChain : (this.isUpgradedWith(itemstack, earthshock) ? costGround : costBase);
   }

   public int getActivationCooldown(ItemStack focusstack) {
      return this.isUpgradedWith(focusstack, chainlightning) ? 500 : (this.isUpgradedWith(focusstack, earthshock) ? 1000 : 250);
   }

   public ItemFocusBasic.WandFocusAnimation getAnimation(ItemStack itemstack) {
      return this.isUpgradedWith(itemstack, earthshock) ? ItemFocusBasic.WandFocusAnimation.WAVE : ItemFocusBasic.WandFocusAnimation.CHARGE;
   }

   public static void shootLightning(World world, EntityLivingBase entityplayer, double xx, double yy, double zz, boolean offset) {
      double px = entityplayer.posX;
      double py = entityplayer.posY;
      double pz = entityplayer.posZ;
      if (entityplayer.getEntityId() != FMLClientHandler.instance().getClient().player.getEntityId()) {
         py = entityplayer.getEntityBoundingBox().minY + (double)(entityplayer.height / 2.0F) + (double)0.25F;
      }

      px += -MathHelper.cos((float) (entityplayer.rotationYaw / 180.0F * Math.PI)) * 0.06F;
      py -= 0.06F;
      pz += -MathHelper.sin((float) (entityplayer.rotationYaw / 180.0F * Math.PI)) * 0.06F;
      if (entityplayer.getEntityId() != FMLClientHandler.instance().getClient().player.getEntityId()) {
         py = entityplayer.getEntityBoundingBox().minY + (double)(entityplayer.height / 2.0F) + (double)0.25F;
      }

      Vec3d vec3d = entityplayer.getLook(1.0F);
      px += vec3d.x * 0.3;
      py += vec3d.y * 0.3;
      pz += vec3d.z * 0.3;
      FXLightningBolt bolt = new FXLightningBolt(world, px, py, pz, xx, yy, zz, world.rand.nextLong(), 6, 0.5F, 8);
      bolt.defaultFractal();
      bolt.setType(2);
      bolt.setWidth(0.125F);
      bolt.finalizeBolt();
   }

   public ItemStack onFocusRightClick(ItemStack itemstack, World world, EntityPlayer p, RayTraceResult movingobjectposition) {
      ItemWandCasting wand = (ItemWandCasting)itemstack.getItem();
      if (this.isUpgradedWith(wand.getFocusItem(itemstack), earthshock)) {
         if (wand.consumeAllVis(itemstack, p, this.getVisCost(itemstack), !p.world.isRemote, false)) {
            if (!world.isRemote) {
               EntityShockOrb orb = new EntityShockOrb(world, p);
               orb.area += this.getUpgradeLevel(wand.getFocusItem(itemstack), FocusUpgradeType.enlarge) * 2;
               orb.damage = (int)((double)orb.damage + (double)wand.getFocusPotency(itemstack) * 1.33);
               world.spawnEntity(orb);
               { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:zap")); if (_snd != null) world.playSound(null, orb.posX, orb.posY, orb.posZ, _snd, net.minecraft.util.SoundCategory.NEUTRAL, 1.0F, 1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F); };
            }

            p.swingArm(net.minecraft.util.EnumHand.MAIN_HAND);
         }
      } else {
         p.setActiveHand(net.minecraft.util.EnumHand.MAIN_HAND);
         WandManager.setCooldown(p, -1);
      }

      return itemstack;
   }

   public void onUsingFocusTick(ItemStack stack, EntityPlayer p, int count) {
      this.doLightningBolt(stack, p, count);
   }

   public void doLightningBolt(ItemStack stack, EntityPlayer p, int count) {
      ItemWandCasting wand = (ItemWandCasting)stack.getItem();
      if (!wand.consumeAllVis(stack, p, this.getVisCost(stack), !p.world.isRemote, false)) {
         p.stopActiveHand();
      } else {
         int potency = wand.getFocusPotency(stack);
         Entity pointedEntity = EntityUtils.getPointedEntity(p.world, p, 0.0F, 20.0F, 1.1F);
         if (p.world.isRemote) {
            RayTraceResult mop = BlockUtils.getTargetBlock(p.world, p, false);
            Vec3d v = p.getLook(2.0F);
            double px = p.posX + v.x * (double)10.0F;
            double py = p.posY + v.y * (double)10.0F;
            double pz = p.posZ + v.z * (double)10.0F;
            if (mop != null) {
               px = mop.hitVec.x;
               py = mop.hitVec.y;
               pz = mop.hitVec.z;

               for(int a = 0; a < 5; ++a) {
                  Thaumcraft.proxy.sparkle((float)px + (p.world.rand.nextFloat() - p.world.rand.nextFloat()) * 0.3F, (float)py + (p.world.rand.nextFloat() - p.world.rand.nextFloat()) * 0.3F, (float)pz + (p.world.rand.nextFloat() - p.world.rand.nextFloat()) * 0.3F, 2.0F + p.world.rand.nextFloat(), 2, 0.05F + p.world.rand.nextFloat() * 0.05F);
               }
            }

            if (pointedEntity != null) {
               px = pointedEntity.posX;
               py = pointedEntity.getEntityBoundingBox().minY + (double)(pointedEntity.height / 2.0F);
               pz = pointedEntity.posZ;

               for(int a = 0; a < 5; ++a) {
                  Thaumcraft.proxy.sparkle((float)px + (p.world.rand.nextFloat() - p.world.rand.nextFloat()) * 0.6F, (float)py + (p.world.rand.nextFloat() - p.world.rand.nextFloat()) * 0.6F, (float)pz + (p.world.rand.nextFloat() - p.world.rand.nextFloat()) * 0.6F, 2.0F + p.world.rand.nextFloat(), 2, 0.05F + p.world.rand.nextFloat() * 0.05F);
               }
            }

            shootLightning(p.world, p, px, py, pz, true);
         } else {
            { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:shock")); if (_snd != null) p.world.playSound(null, p.posX, p.posY, p.posZ, _snd, net.minecraft.util.SoundCategory.NEUTRAL, 0.25F, 1.0F); }
            if (pointedEntity instanceof EntityLivingBase && (!(pointedEntity instanceof EntityPlayer) || p.world.getMinecraftServer().isPVPEnabled())) {
               int cl = this.getUpgradeLevel(wand.getFocusItem(stack), chainlightning) * 2;
               pointedEntity.attackEntityFrom(DamageSource.causePlayerDamage(p), (float)((cl > 0 ? 6 : 4) + potency));
               if (cl > 0) {
                  cl += this.getUpgradeLevel(wand.getFocusItem(stack), FocusUpgradeType.enlarge) * 2;
                  EntityLivingBase center = (EntityLivingBase)pointedEntity;
                  ArrayList<Integer> targets = new ArrayList<>();
                  targets.add(pointedEntity.getEntityId());

                  while(cl > 0) {
                     --cl;
                     ArrayList<Entity> list = EntityUtils.getEntitiesInRange(p.world, center.posX, center.posY, center.posZ, p, EntityLivingBase.class, 8.0F);
                     double d = Double.MAX_VALUE;
                     Entity closest = null;

                     for(Entity e : list) {
                        if (!targets.contains(e.getEntityId()) && (!(e instanceof EntityPlayer) || p.world.getMinecraftServer().isPVPEnabled())) {
                           double dd = e.getDistanceSq(center);
                           if (dd < d) {
                              closest = e;
                              d = dd;
                           }
                        }
                     }

                     if (closest != null) {
                        PacketHandler.INSTANCE.sendToAllAround(new PacketFXZap(center.getEntityId(), closest.getEntityId()), new NetworkRegistry.TargetPoint(p.world.provider.getDimension(), center.posX, center.posY, center.posZ, 64.0F));
                        targets.add(closest.getEntityId());
                        closest.attackEntityFrom(DamageSource.causePlayerDamage(p), (float)(4 + potency));
                        center = (EntityLivingBase)closest;
                     }
                  }
               }
            }
         }

      }
   }

   public boolean canApplyUpgrade(ItemStack focusstack, EntityPlayer player, FocusUpgradeType type, int rank) {
      return !type.equals(FocusUpgradeType.enlarge) || this.isUpgradedWith(focusstack, chainlightning) || this.isUpgradedWith(focusstack, earthshock);
   }

   public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack itemstack, int rank) {
      switch (rank) {
         case 1:
            return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency};
         case 2:
            return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency};
         case 3:
            return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency, chainlightning, earthshock};
         case 4:
            return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency, FocusUpgradeType.enlarge};
         case 5:
            return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.potency, FocusUpgradeType.enlarge};
         default:
            return null;
      }
   }

   static {
      costBase = (new AspectList()).add(Aspect.AIR, 25);
      costChain = (new AspectList()).add(Aspect.AIR, 40).add(Aspect.WATER, 10);
      costGround = (new AspectList()).add(Aspect.AIR, 75).add(Aspect.EARTH, 25);
      chainlightning = new FocusUpgradeType(17, new ResourceLocation("thaumcraft", "textures/foci/chainlightning.png"), "focus.upgrade.chainlightning.name", "focus.upgrade.chainlightning.text", (new AspectList()).add(Aspect.WEATHER, 1));
      earthshock = new FocusUpgradeType(18, new ResourceLocation("thaumcraft", "textures/foci/earthshock.png"), "focus.upgrade.earthshock.name", "focus.upgrade.earthshock.text", (new AspectList()).add(Aspect.WEATHER, 1));
   }
}
