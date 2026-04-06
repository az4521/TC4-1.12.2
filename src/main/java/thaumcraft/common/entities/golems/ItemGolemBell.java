package thaumcraft.common.entities.golems;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.ArrayList;

import fromhodgepodge.mixins.hooks.ThaumcraftMixinMethods;
import thaumcraft.client.renderers.compat.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import thaumcraft.client.lib.PlayerNotifications;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigItems;

public class ItemGolemBell extends Item {
   public TextureAtlasSprite icon;

   public ItemGolemBell() {
      this.setHasSubtypes(false);
      this.setCreativeTab(Thaumcraft.tabTC);
      this.setMaxStackSize(1);
   }

   @SideOnly(Side.CLIENT)
   public void registerIcons(IIconRegister ir) {
      this.icon = ir.registerSprite("thaumcraft:ironbell");
   }

   @SideOnly(Side.CLIENT)
   public TextureAtlasSprite getIconFromDamage(int par1) {
      return this.icon;
   }

   public boolean getShareTag() {
       return super.getShareTag();
   }

   public static int getGolemId(ItemStack stack) {
      return stack.hasTagCompound() && stack.getTagCompound().hasKey("golemid") ? stack.getTagCompound().getInteger("golemid") : -1;
   }

   public static int getGolemHomeFace(ItemStack stack) {
      return stack.hasTagCompound() && stack.getTagCompound().hasKey("golemhomeface") ? stack.getTagCompound().getInteger("golemhomeface") : -1;
   }

   public static BlockPos getGolemHomeCoords(ItemStack stack) {
      return stack.hasTagCompound() && stack.getTagCompound().hasKey("golemhomex") ? new BlockPos(stack.getTagCompound().getInteger("golemhomex"), stack.getTagCompound().getInteger("golemhomey"), stack.getTagCompound().getInteger("golemhomez")) : null;
   }

   public static ArrayList<Marker> getMarkers(ItemStack stack) {
      ArrayList<Marker> markers = new ArrayList<>();
      if (stack.hasTagCompound() && stack.getTagCompound().hasKey("markers")) {
         NBTTagList tl = stack.getTagCompound().getTagList("markers", 10);

         for(int i = 0; i < tl.tagCount(); ++i) {
            NBTTagCompound nbttagcompound1 = tl.getCompoundTagAt(i);
            int x = nbttagcompound1.getInteger("x");
            int y = nbttagcompound1.getInteger("y");
            int z = nbttagcompound1.getInteger("z");
            int dim = nbttagcompound1.getInteger("dim");
            byte s = nbttagcompound1.getByte("side");
            byte c = nbttagcompound1.getByte("color");
            markers.add(new Marker(x, y, z, (byte)dim, s, c));
         }
      }

      if (stack.hasTagCompound()) {
         NBTTagList nbtTagList = stack.getTagCompound().getTagList("markers", 10);
         return ThaumcraftMixinMethods.overwriteMarkersDimID(nbtTagList, markers);
      }
      return markers;
   }

   public static void resetMarkers(ItemStack stack, World world, EntityPlayer player) {
      Entity golem = null;
      int gid = getGolemId(stack);
      if (gid > -1) {
         golem = world.getEntityByID(gid);
         if (golem instanceof EntityGolemBase) {
            stack.setTagInfo("markers", new NBTTagList());
            ((EntityGolemBase)golem).setMarkers(new ArrayList());
            { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("random.orb")); if (_snd != null) world.playSound(null, player.posX, player.posY, player.posZ, _snd, net.minecraft.util.SoundCategory.NEUTRAL, 0.7F, 1.0F + world.rand.nextFloat() * 0.1F); }
         }
      }

   }

   public static void changeMarkers(ItemStack stack, EntityPlayer player, World world, int par4, int par5, int par6, int side) {
      Entity golem = null;
      ArrayList<Marker> markers = getMarkers(stack);
      boolean markMultipleColors = false;
      int gid = getGolemId(stack);
      if (gid > -1) {
         golem = world.getEntityByID(gid);
         if (golem instanceof EntityGolemBase && ((EntityGolemBase) golem).getUpgradeAmount(4) > 0) {
            markMultipleColors = true;
         }
      }

      int count = markers.size();
      int index = -1;
      int color = 0;
      if (!markMultipleColors) {
         index = markers.indexOf(new Marker(par4, par5, par6, world.provider.getDimension(), (byte)side, (byte)-1));
      } else {
         for(int a = -1; a < 16; ++a) {
            index = markers.indexOf(new Marker(par4, par5, par6, world.provider.getDimension(), (byte)side, (byte)a));
            color = a;
            if (index != -1) {
               break;
            }
         }
      }

      if (index >= 0) {
         markers.remove(index);
         if (markMultipleColors && !player.isSneaking()) {
            ++color;
            if (color <= 15) {
               markers.add(new Marker(par4, par5, par6, world.provider.getDimension(), (byte)side, (byte)color));
               ++count;
               if (world.isRemote) {
                  String text = I18n.translateToLocal("tc.markerchange");
                  if (color > -1) {
                     text = text.replaceAll("%n", UtilsFX.colorNames[color]);
                  } else {
                     text = I18n.translateToLocal("tc.markerchangeany");
                  }

                  PlayerNotifications.addNotification(text);
               }
            }
         }
      } else {
         markers.add(new Marker(par4, par5, par6, world.provider.getDimension(), (byte)side, (byte)-1));
      }

      if (count != markers.size()) {
         NBTTagList tl = new NBTTagList();

         for(Marker l : markers) {
            NBTTagCompound nbtc = new NBTTagCompound();
            nbtc.setInteger("x", l.x);
            nbtc.setInteger("y", l.y);
            nbtc.setInteger("z", l.z);
            nbtc.setInteger("dim", l.dim);
            nbtc.setByte("side", l.side);
            nbtc.setByte("color", l.color);
            tl.appendTag(nbtc);
         }

         stack.setTagInfo("markers", tl);
         if (gid > -1) {
            if (golem instanceof EntityGolemBase) {
               ((EntityGolemBase)golem).setMarkers(markers);
            } else {
               stack.getTagCompound().removeTag("golemid");
               stack.getTagCompound().removeTag("markers");
               stack.getTagCompound().removeTag("golemhomex");
               stack.getTagCompound().removeTag("golemhomey");
               stack.getTagCompound().removeTag("golemhomez");
               stack.getTagCompound().removeTag("golemhomeface");
            }
         }
      }

      { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("random.orb")); if (_snd != null) world.playSound(null, (double)par4 + 0.5, (double)par5 + 0.5, (double)par6 + 0.5, _snd, net.minecraft.util.SoundCategory.NEUTRAL, 0.7F, 1.0F + world.rand.nextFloat() * 0.1F); }
   }

   public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing facing, float par8, float par9, float par10, EnumHand hand) {
      ItemStack stack = player.getHeldItem(hand);
      RayTraceResult movingobjectposition = this.rayTrace(world, player, true);
      if (movingobjectposition == null) {
         return EnumActionResult.PASS;
      } else {
         if (movingobjectposition.typeOfHit == RayTraceResult.Type.BLOCK) {
            int i = movingobjectposition.getBlockPos().getX();
            int j = movingobjectposition.getBlockPos().getY();
            int k = movingobjectposition.getBlockPos().getZ();
            changeMarkers(stack, player, world, i, j, k, movingobjectposition.sideHit.getIndex());
         }

         return world.isRemote ? EnumActionResult.PASS : EnumActionResult.SUCCESS;
      }
   }

   public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase target, EnumHand hand) {
      if (target instanceof EntityGolemBase) {
         if (stack.hasTagCompound()) {
            stack.getTagCompound().removeTag("golemid");
            stack.getTagCompound().removeTag("markers");
            stack.getTagCompound().removeTag("golemhomex");
            stack.getTagCompound().removeTag("golemhomey");
            stack.getTagCompound().removeTag("golemhomez");
            stack.getTagCompound().removeTag("golemhomeface");
         }

         if (target.world.isRemote) {
            if (player != null) {
               player.swingArm(net.minecraft.util.EnumHand.MAIN_HAND);
            }
         } else {
            ArrayList<Marker> markers = ((EntityGolemBase)target).getMarkers();
            NBTTagList tl = new NBTTagList();

            for(Marker l : markers) {
               NBTTagCompound nbtc = new NBTTagCompound();
               nbtc.setInteger("x", l.x);
               nbtc.setInteger("y", l.y);
               nbtc.setInteger("z", l.z);
               nbtc.setInteger("dim", l.dim);
               nbtc.setByte("side", l.side);
               nbtc.setByte("color", l.color);
               tl.appendTag(nbtc);
            }

            stack.setTagInfo("markers", tl);
            stack.getTagCompound().setInteger("golemid", target.getEntityId());
            stack.getTagCompound().setInteger("golemhomex", ((EntityGolemBase)target).getHomePosition().getX());
            stack.getTagCompound().setInteger("golemhomey", ((EntityGolemBase)target).getHomePosition().getY());
            stack.getTagCompound().setInteger("golemhomez", ((EntityGolemBase)target).getHomePosition().getZ());
            stack.getTagCompound().setInteger("golemhomeface", ((EntityGolemBase)target).homeFacing);
            { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("random.orb")); if (_snd != null) target.world.playSound(null, target.posX, target.posY, target.posZ, _snd, net.minecraft.util.SoundCategory.NEUTRAL, 0.7F, 1.0F + target.world.rand.nextFloat() * 0.1F); }
            if (player != null && player.capabilities.isCreativeMode) {
               player.setHeldItem(net.minecraft.util.EnumHand.MAIN_HAND, stack.copy());
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
      if (entity instanceof EntityTravelingTrunk && !entity.isDead) {
         byte upgrade = (byte)((EntityTravelingTrunk)entity).getUpgrade();
         if (upgrade == 3 && !((EntityTravelingTrunk)entity).getOwnerName().equals(player.getName())) {
            return false;
         } else if (entity.world.isRemote && entity instanceof EntityLiving) {
            ((EntityLiving)entity).spawnExplosionParticle();
            return false;
         } else {
            ItemStack dropped = new ItemStack(ConfigItems.itemTrunkSpawner);
            if (player.isSneaking()) {
               if (upgrade > -1 && entity.world.rand.nextBoolean()) {
                  entity.entityDropItem(new ItemStack(ConfigItems.itemGolemUpgrade, 1, upgrade), 0.5F);
               }
            } else {
               if (((EntityTravelingTrunk)entity).hasCustomName()) {
                  dropped.setStackDisplayName(((EntityTravelingTrunk)entity).getCustomNameTag());
               }

               dropped.setTagInfo("upgrade", new NBTTagByte(upgrade));
               if (upgrade == 4) {
                  dropped.setTagInfo("inventory", ((EntityTravelingTrunk)entity).inventory.writeToNBT(new NBTTagList()));
               }
            }

            entity.entityDropItem(dropped, 0.5F);
            if (upgrade != 4 || player.isSneaking()) {
               ((EntityTravelingTrunk)entity).inventory.dropAllItems();
            }

            { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft", "zap")); if (_snd != null) entity.world.playSound(null, entity.posX, entity.posY, entity.posZ, _snd, net.minecraft.util.SoundCategory.NEUTRAL, 0.5F, 1.0F); }
            entity.setDead();
            return true;
         }
      } else if (entity instanceof EntityGolemBase && !entity.isDead) {
         if (entity.world.isRemote && entity instanceof EntityLiving) {
            ((EntityLiving)entity).spawnExplosionParticle();
            return false;
         } else {
            int type = ((EntityGolemBase)entity).golemType.ordinal();
            String deco = ((EntityGolemBase)entity).decoration;
            byte core = ((EntityGolemBase)entity).getCore();
            byte[] upgrades = ((EntityGolemBase)entity).upgrades;
            boolean advanced = ((EntityGolemBase)entity).advanced;
            ItemStack dropped = new ItemStack(ConfigItems.itemGolemPlacer, 1, type);
            if (advanced) {
               dropped.setTagInfo("advanced", new NBTTagByte((byte)1));
            }

            if (player.isSneaking()) {
               if (core > -1) {
                  entity.entityDropItem(new ItemStack(ConfigItems.itemGolemCore, 1, core), 0.5F);
               }

               for(byte b : upgrades) {
                  if (b > -1 && entity.world.rand.nextBoolean()) {
                     entity.entityDropItem(new ItemStack(ConfigItems.itemGolemUpgrade, 1, b), 0.5F);
                  }
               }
            } else {
               if (((EntityGolemBase)entity).hasCustomName()) {
                  dropped.setStackDisplayName(((EntityGolemBase)entity).getCustomNameTag());
               }

               if (!deco.isEmpty()) {
                  dropped.setTagInfo("deco", new NBTTagString(deco));
               }

               if (core > -1) {
                  dropped.setTagInfo("core", new NBTTagByte(core));
               }

               dropped.setTagInfo("upgrades", new NBTTagByteArray(upgrades));
               ArrayList<Marker> markers = ((EntityGolemBase)entity).getMarkers();
               NBTTagList tl = new NBTTagList();

               for(Marker l : markers) {
                  NBTTagCompound nbtc = new NBTTagCompound();
                  nbtc.setInteger("x", l.x);
                  nbtc.setInteger("y", l.y);
                  nbtc.setInteger("z", l.z);
                  nbtc.setInteger("dim", l.dim);
                  nbtc.setByte("side", l.side);
                  nbtc.setByte("color", l.color);
                  tl.appendTag(nbtc);
               }

               dropped.setTagInfo("markers", tl);
               dropped.setTagInfo("Inventory", ((EntityGolemBase)entity).inventory.writeToNBT(new NBTTagList()));
            }

            entity.entityDropItem(dropped, 0.5F);
            ((EntityGolemBase)entity).dropStuff();
            { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft", "zap")); if (_snd != null) entity.world.playSound(null, entity.posX, entity.posY, entity.posZ, _snd, net.minecraft.util.SoundCategory.NEUTRAL, 0.5F, 1.0F); }
            entity.setDead();
            return true;
         }
      } else {
         return false;
      }
   }
}
