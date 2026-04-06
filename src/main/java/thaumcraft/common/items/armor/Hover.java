package thaumcraft.common.items.armor;

import baubles.api.BaubleType;
import baubles.api.BaublesApi;

import java.util.HashMap;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.world.World;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.blocks.ItemJarFilled;
import thaumcraft.common.config.Config;
import thaumcraft.common.items.baubles.ItemGirdleHover;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.misc.PacketFlyToServer;
import thaumcraft.common.lib.utils.Utils;

import javax.annotation.Nonnull;

import static simpleutils.bauble.BaubleUtils.forEachBaubleWithBaubleType;

public class Hover {
    public static final int EFFICIENCY = 360;
    static HashMap<Integer, Boolean> hovering = new HashMap<>();
    static HashMap<Integer, Long> timing = new HashMap<>();

    public static boolean toggleHover(EntityPlayer player, int playerId, @Nonnull ItemStack armor) {
        boolean hover = hovering.get(playerId) != null && hovering.get(playerId);
        short fuel = 0;
        if (armor.hasTagCompound() && armor.getTagCompound().hasKey("jar")) {
            ItemStack jar = new ItemStack(armor.getTagCompound().getCompoundTag("jar"));
            if (jar != null && jar.getItem() instanceof ItemJarFilled && jar.hasTagCompound()) {
                AspectList aspects = ((ItemJarFilled) jar.getItem()).getAspects(jar);
                if (aspects != null && aspects.size() > 0 && aspects.getAmount(Aspect.ENERGY) > 0) {
                    fuel = (short) aspects.getAmount(Aspect.ENERGY);
                }
            }
        }

        if (!hover && fuel <= 0) {
            return false;
        } else {
            hovering.put(playerId, !hover);
            if (player.world.isRemote) {
                PacketHandler.INSTANCE.sendToServer(new PacketFlyToServer(player, !hover));
                { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation(!hover ? "thaumcraft:hhon" : "thaumcraft:hhoff")); if (_snd != null) player.world.playSound(null, player.posX, player.posY, player.posZ, _snd, net.minecraft.util.SoundCategory.PLAYERS, 0.33F, 1.0F); }
            }

            return !hover;
        }
    }

    public static void setHover(int playerId, boolean hover) {
        hovering.put(playerId, hover);
    }

    public static boolean getHover(int playerId) {
        return hovering.containsKey(playerId) ? hovering.get(playerId) : false;
    }

    public static void handleHoverArmor(EntityPlayer player, ItemStack armor) {
        if (hovering.get(player.getEntityId()) == null && armor.hasTagCompound() && armor.getTagCompound().hasKey("hover")) {
            hovering.put(player.getEntityId(), armor.getTagCompound().getByte("hover") == 1);
            if (player.world.isRemote) {
                PacketHandler.INSTANCE.sendToServer(new PacketFlyToServer(player, armor.getTagCompound().getByte("hover") == 1));
            }
        }

        boolean hover = hovering.get(player.getEntityId()) != null && hovering.get(player.getEntityId());
        World world = player.world;
        player.capabilities.isFlying = hover;
        if (world.isRemote && player instanceof EntityPlayerSP) {
            if (hover && expendCharge(player, armor)) {
                long currenttime = System.currentTimeMillis();
                long time = 0L;
                if (timing.get(player.getEntityId()) != null) {
                    time = timing.get(player.getEntityId());
                }

                if (time < currenttime) {
                    time = currenttime + 1200L;
                    timing.put(player.getEntityId(), time);
                    { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:jacobs")); if (_snd != null) player.world.playSound(null, player.posX, player.posY, player.posZ, _snd, net.minecraft.util.SoundCategory.PLAYERS, 0.05F, 1.0F + player.world.rand.nextFloat() * 0.05F); }
                }

                int haste = EnchantmentHelper.getEnchantmentLevel(Config.enchHaste, armor);
                final float[] mod = {0.7F + 0.075F * (float) haste};
                if (!forEachBaubleWithBaubleType(BaubleType.AMULET, player, ItemGirdleHover.class,
                        (slot, stack, item) -> {
                            mod[0] += 0.21F;
                            return true;
                        })) {
                    forEachBaubleWithBaubleType(BaubleType.TRINKET, player, ItemGirdleHover.class,
                            (slot, stack, item) -> {
                                mod[0] += 0.21F;
                                return true;
                            });
                }
                if (mod[0] > 1.0F) {
                    mod[0] = 1.0F;
                }

                player.motionX *= mod[0];
                player.motionZ *= mod[0];
            } else if (hover) {
                toggleHover(player, player.getEntityId(), armor);
            }
        } else {
            if (armor.hasTagCompound() && !armor.getTagCompound().hasKey("hover")) {
                armor.setTagInfo("hover", new NBTTagByte((byte) (hover ? 1 : 0)));
            }

            if (hover && expendCharge(player, armor)) {
                if (player instanceof EntityPlayerMP) {
                    Utils.resetFloatCounter((EntityPlayerMP) player);
                }

                player.fallDistance = 0.0F;
                if (armor.hasTagCompound()
                        && armor.getTagCompound().hasKey("hover")
                        && armor.getTagCompound().getByte("hover") != 1) {
                    armor.setTagInfo("hover", new NBTTagByte((byte) 1));
                }
            } else {
                if (hover) {
                    toggleHover(player, player.getEntityId(), armor);
                }

                player.fallDistance *= 0.75F;
                if (armor.hasTagCompound() && armor.getTagCompound().hasKey("hover") && armor.getTagCompound().getByte("hover") == 1) {
                    armor.setTagInfo("hover", new NBTTagByte((byte) (hover ? 1 : 0)));
                }
            }
        }

    }

    public static boolean expendCharge(EntityPlayer player, ItemStack is) {
        if (is.hasTagCompound() && is.getTagCompound().hasKey("jar")) {
            ItemStack jar = new ItemStack(is.getTagCompound().getCompoundTag("jar"));
            short fuel = 0;
            if (jar != null && jar.getItem() instanceof ItemJarFilled && jar.hasTagCompound()) {
                AspectList aspects = ((ItemJarFilled) jar.getItem()).getAspects(jar);
                if (aspects != null && aspects.size() > 0 && aspects.getAmount(Aspect.ENERGY) > 0) {
                    fuel = (short) aspects.getAmount(Aspect.ENERGY);
                }
            }

            float mod = 1.0F;
            if (!BaublesApi.getBaubles(player).getStackInSlot(3).isEmpty() && BaublesApi.getBaubles(player).getStackInSlot(3).getItem() instanceof ItemGirdleHover) {
                mod = 0.8F;
            }

            if (!is.getTagCompound().hasKey("charge")) {
                is.setTagInfo("charge", new NBTTagShort((short) 0));
            }

            if (fuel > 0 && is.getTagCompound().hasKey("charge")) {
                short charge = is.getTagCompound().getShort("charge");
                if ((float) charge < 360.0F * mod) {
                    is.setTagInfo("charge", new NBTTagShort((short) (charge + 1)));
                    return true;
                }

                is.setTagInfo("charge", new NBTTagShort((short) 0));
                --fuel;
                if (fuel > 0) {
                    ((ItemJarFilled) jar.getItem()).setAspects(jar, (new AspectList()).add(Aspect.ENERGY, fuel));
                } else {
                    ((ItemJarFilled) jar.getItem()).setAspects(jar, (new AspectList()).remove(Aspect.ENERGY));
                }

                NBTTagCompound var4 = new NBTTagCompound();
                jar.writeToNBT(var4);
                is.setTagInfo("jar", var4);
                return fuel > 0;
            }
        }

        return false;
    }
}
