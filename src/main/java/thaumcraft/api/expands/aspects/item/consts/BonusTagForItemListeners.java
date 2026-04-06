package thaumcraft.api.expands.aspects.item.consts;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.init.Enchantments;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagList;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssentiaContainerItem;
import thaumcraft.api.expands.UnmodifiableAspectList;
import thaumcraft.api.expands.aspects.item.listeners.BonusTagForItemListener;
import thaumcraft.common.config.Config;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

public class BonusTagForItemListeners {

    public static final BonusTagForItemListener DEFAULT_ON_ESSENTIA_CONTAINER = new BonusTagForItemListener(10) {
        @Override
        @ParametersAreNonnullByDefault
        public void onItem(@Nullable Item item, ItemStack itemstack, UnmodifiableAspectList sourceTags, AspectList currentAspects) {
            if (item instanceof IEssentiaContainerItem) {
                AspectList aspectsFromContainer = ((IEssentiaContainerItem) item).getAspects(itemstack);
                if (aspectsFromContainer != null && aspectsFromContainer.size() > 0) {
                    for (Aspect tag : aspectsFromContainer.copy().getAspects()) {
                        int amountInContainer = currentAspects.getAmount(tag);
                        if (amountInContainer > 0) {
                            currentAspects.add(tag, amountInContainer);
                        }
                    }
                }
            }
        }
    };

    public static final BonusTagForItemListener DEFAULT_ON_ARMOR = new BonusTagForItemListener(20) {
        @Override
        @ParametersAreNonnullByDefault
        public void onItem(@Nullable Item item, ItemStack itemstack, UnmodifiableAspectList sourceTags, AspectList currentAspects) {
            if (item instanceof ItemArmor) {
                currentAspects.merge(Aspect.ARMOR, ((ItemArmor) item).damageReduceAmount);
            }
        }
    };

    public static final BonusTagForItemListener DEFAULT_ON_SWORD = new BonusTagForItemListener(30) {
        @Override
        @ParametersAreNonnullByDefault
        public void onItem(@Nullable Item item, ItemStack itemstack, UnmodifiableAspectList sourceTags, AspectList currentAspects) {
            if (item instanceof ItemSword) {
                com.google.common.collect.Multimap<String, net.minecraft.entity.ai.attributes.AttributeModifier> attrs = item.getAttributeModifiers(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, itemstack);
                java.util.Collection<net.minecraft.entity.ai.attributes.AttributeModifier> mods = attrs.get(net.minecraft.entity.SharedMonsterAttributes.ATTACK_DAMAGE.getName());
                if (!mods.isEmpty()) {
                    double dmg = mods.iterator().next().getAmount();
                    if (dmg + 1.0 > 0.0) {
                        currentAspects.merge(Aspect.WEAPON, (int)(dmg + 1.0));
                    }
                }
            }
        }
    };

    public static final BonusTagForItemListener DEFAULT_ON_BOW = new BonusTagForItemListener(40) {
        @Override
        @ParametersAreNonnullByDefault
        public void onItem(@Nullable Item item, ItemStack itemstack, UnmodifiableAspectList sourceTags, AspectList currentAspects) {
            if (item instanceof ItemBow) {
                currentAspects.merge(Aspect.WEAPON, 3).merge(Aspect.FLIGHT, 1);
            }
        }
    };

    public static final BonusTagForItemListener DEFAULT_ON_PICKAXE = new BonusTagForItemListener(50) {
        @Override
        @ParametersAreNonnullByDefault
        public void onItem(@Nullable Item item, ItemStack itemstack, UnmodifiableAspectList sourceTags, AspectList currentAspects) {
            if (item instanceof ItemPickaxe) {
                String mat = ((ItemTool) item).getToolMaterialName();

                for (Item.ToolMaterial tm : Item.ToolMaterial.values()) {
                    if (tm.toString().equals(mat)) {
                        currentAspects.merge(Aspect.MINE, tm.getHarvestLevel() + 1);
                    }
                }
            }
        }
    };
    public static final BonusTagForItemListener DEFAULT_ON_TOOL = new BonusTagForItemListener(60) {
        @Override
        @ParametersAreNonnullByDefault
        public void onItem(@Nullable Item item, ItemStack itemstack, UnmodifiableAspectList sourceTags, AspectList currentAspects) {
            if (item instanceof ItemTool) {
                String mat = ((ItemTool) item).getToolMaterialName();

                for (Item.ToolMaterial tm : Item.ToolMaterial.values()) {
                    if (tm.toString().equals(mat)) {
                        currentAspects.merge(Aspect.TOOL, tm.getHarvestLevel() + 1);
                    }
                }
            }
        }
    };

    public static final BonusTagForItemListener DEFAULT_ON_SHEARS = new BonusTagForItemListener(70) {
        @Override
        @ParametersAreNonnullByDefault
        public void onItem(@Nullable Item item, ItemStack itemstack, UnmodifiableAspectList sourceTags, AspectList currentAspects) {
            if (item instanceof ItemShears) {
                if (item.getMaxDamage() <= Item.ToolMaterial.WOOD.getMaxUses()) {
                    currentAspects.merge(Aspect.HARVEST, 1);
                } else if (item.getMaxDamage() > Item.ToolMaterial.STONE.getMaxUses() && item.getMaxDamage() > Item.ToolMaterial.GOLD.getMaxUses()) {
                    if (item.getMaxDamage() <= Item.ToolMaterial.IRON.getMaxUses()) {
                        currentAspects.merge(Aspect.HARVEST, 3);
                    } else {
                        currentAspects.merge(Aspect.HARVEST, 4);
                    }
                } else {
                    currentAspects.merge(Aspect.HARVEST, 2);
                }
            }
        }
    };
    public static final BonusTagForItemListener DEFAULT_ON_HOE = new BonusTagForItemListener(70) {
        @Override
        @ParametersAreNonnullByDefault
        public void onItem(@Nullable Item item, ItemStack itemstack, UnmodifiableAspectList sourceTags, AspectList currentAspects) {
            if (item instanceof ItemHoe) {
                if (item.getMaxDamage() <= Item.ToolMaterial.WOOD.getMaxUses()) {
                    currentAspects.merge(Aspect.HARVEST, 1);
                } else if (item.getMaxDamage() > Item.ToolMaterial.STONE.getMaxUses() && item.getMaxDamage() > Item.ToolMaterial.GOLD.getMaxUses()) {
                    if (item.getMaxDamage() <= Item.ToolMaterial.IRON.getMaxUses()) {
                        currentAspects.merge(Aspect.HARVEST, 3);
                    } else {
                        currentAspects.merge(Aspect.HARVEST, 4);
                    }
                } else {
                    currentAspects.merge(Aspect.HARVEST, 2);
                }
            }
        }
    };

    //hint:if you want to check you enchantment,you add your own listener.
    public static final BonusTagForItemListener DEFAULT_ENCHANTMENTS = new BonusTagForItemListener(80){

        @Override
        @ParametersAreNonnullByDefault
        public void onItem(@Nullable Item item, ItemStack itemstack, UnmodifiableAspectList sourceTags, AspectList currentAspects) {
            if (item != null) {

                NBTTagList ench = itemstack.getEnchantmentTagList();
                if (item instanceof ItemEnchantedBook) {
                    ench = ItemEnchantedBook.getEnchantments(itemstack);
                }

                if (ench != null) {
                    int var5 = 0;

                    for (int var3 = 0; var3 < ench.tagCount(); ++var3) {
                        short eid = ench.getCompoundTagAt(var3).getShort("id");
                        short lvl = ench.getCompoundTagAt(var3).getShort("lvl");
                        Enchantment enchObj = Enchantment.getEnchantmentByID(eid & 0xFFFF);
                        if (enchObj == Enchantments.AQUA_AFFINITY) {
                            currentAspects.merge(Aspect.WATER, lvl);
                        } else if (enchObj == Enchantments.BANE_OF_ARTHROPODS) {
                            currentAspects.merge(Aspect.BEAST, lvl);
                        } else if (enchObj == Enchantments.BLAST_PROTECTION) {
                            currentAspects.merge(Aspect.ARMOR, lvl);
                        } else if (enchObj == Enchantments.EFFICIENCY) {
                            currentAspects.merge(Aspect.TOOL, lvl);
                        } else if (enchObj == Enchantments.FEATHER_FALLING) {
                            currentAspects.merge(Aspect.FLIGHT, lvl);
                        } else if (enchObj == Enchantments.FIRE_ASPECT) {
                            currentAspects.merge(Aspect.FIRE, lvl);
                        } else if (enchObj == Enchantments.FIRE_PROTECTION) {
                            currentAspects.merge(Aspect.ARMOR, lvl);
                        } else if (enchObj == Enchantments.FLAME) {
                            currentAspects.merge(Aspect.FIRE, lvl);
                        } else if (enchObj == Enchantments.FORTUNE) {
                            currentAspects.merge(Aspect.GREED, lvl);
                        } else if (enchObj == Enchantments.INFINITY) {
                            currentAspects.merge(Aspect.CRAFT, lvl);
                        } else if (enchObj == Enchantments.KNOCKBACK) {
                            currentAspects.merge(Aspect.AIR, lvl);
                        } else if (enchObj == Enchantments.LOOTING) {
                            currentAspects.merge(Aspect.GREED, lvl);
                        } else if (enchObj == Enchantments.POWER) {
                            currentAspects.merge(Aspect.WEAPON, lvl);
                        } else if (enchObj == Enchantments.PROJECTILE_PROTECTION) {
                            currentAspects.merge(Aspect.ARMOR, lvl);
                        } else if (enchObj == Enchantments.PROTECTION) {
                            currentAspects.merge(Aspect.ARMOR, lvl);
                        } else if (enchObj == Enchantments.PUNCH) {
                            currentAspects.merge(Aspect.AIR, lvl);
                        } else if (enchObj == Enchantments.RESPIRATION) {
                            currentAspects.merge(Aspect.AIR, lvl);
                        } else if (enchObj == Enchantments.SHARPNESS) {
                            currentAspects.merge(Aspect.WEAPON, lvl);
                        } else if (enchObj == Enchantments.SILK_TOUCH) {
                            currentAspects.merge(Aspect.EXCHANGE, lvl);
                        } else if (enchObj == Enchantments.THORNS) {
                            currentAspects.merge(Aspect.WEAPON, lvl);
                        } else if (enchObj == Enchantments.SMITE) {
                            currentAspects.merge(Aspect.ENTROPY, lvl);
                        } else if (enchObj == Enchantments.UNBREAKING) {
                            currentAspects.merge(Aspect.EARTH, lvl);
                        } else if (enchObj == Enchantments.LUCK_OF_THE_SEA) {
                            currentAspects.merge(Aspect.GREED, lvl);
                        } else if (enchObj == Enchantments.LURE) {
                            currentAspects.merge(Aspect.BEAST, lvl);
                        } else if (enchObj == Config.enchHaste) {
                            currentAspects.merge(Aspect.MOTION, lvl);
                        } else if (enchObj == Config.enchRepair) {
                            currentAspects.merge(Aspect.TOOL, lvl);
                        }

                        var5 += lvl;
                    }

                    if (var5 > 0) {
                        currentAspects.merge(Aspect.MAGIC, var5);
                    }
                }
            }
        }
    };
}
