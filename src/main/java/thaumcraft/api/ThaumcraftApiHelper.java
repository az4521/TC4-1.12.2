package thaumcraft.api;

import java.util.HashMap;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.oredict.OreDictionary;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssentiaTransport;
import net.minecraft.util.math.BlockPos;

public class ThaumcraftApiHelper {

    public static AspectList cullTags(AspectList temp) {
        AspectList temp2 = new AspectList();
        for (Aspect tag : temp.getAspects()) {
            if (tag != null)
                temp2.add(tag, temp.getAmount(tag));
        }
        while (temp2.size() > 6) {
            Aspect lowest = null;
            float low = Short.MAX_VALUE;
            for (Aspect tag : temp2.getAspects()) {
                if (tag == null) continue;
                float ta = temp2.getAmount(tag);
                if (tag.isPrimal()) {
                    ta *= .9f;
                } else {
                    if (!tag.getComponents()[0].isPrimal()) {
                        ta *= 1.1f;
                        if (!tag.getComponents()[0].getComponents()[0].isPrimal()) {
                            ta *= 1.05f;
                        }
                        if (!tag.getComponents()[0].getComponents()[1].isPrimal()) {
                            ta *= 1.05f;
                        }
                    }
                    if (!tag.getComponents()[1].isPrimal()) {
                        ta *= 1.1f;
                        if (!tag.getComponents()[1].getComponents()[0].isPrimal()) {
                            ta *= 1.05f;
                        }
                        if (!tag.getComponents()[1].getComponents()[1].isPrimal()) {
                            ta *= 1.05f;
                        }
                    }
                }

                if (ta < low) {
                    low = ta;
                    lowest = tag;
                }
            }
            temp2.aspects.remove(lowest);
        }
        return temp2;
    }

    public static boolean areItemsEqual(ItemStack s1, ItemStack s2) {
        if (s1.isItemStackDamageable() && s2.isItemStackDamageable()) {
            return s1.getItem() == s2.getItem();
        } else
            return s1.getItem() == s2.getItem() && s1.getItemDamage() == s2.getItemDamage();
    }

    public static boolean isResearchComplete(String username, String researchkey) {
        return ThaumcraftApi.internalMethods.isResearchComplete(username, researchkey);
    }

    public static boolean hasDiscoveredAspect(String username, Aspect aspect) {
        return ThaumcraftApi.internalMethods.hasDiscoveredAspect(username, aspect);
    }

    public static AspectList getDiscoveredAspects(String username) {
        return ThaumcraftApi.internalMethods.getDiscoveredAspects(username);
    }

    public static ItemStack getStackInRowAndColumn(Object instance, int row, int column) {
        return ThaumcraftApi.internalMethods.getStackInRowAndColumn(instance, row, column);
    }

    public static AspectList getObjectAspects(ItemStack is) {
        return ThaumcraftApi.internalMethods.getObjectAspects(is);
    }

    public static AspectList getBonusObjectTags(ItemStack is, AspectList ot) {
        return ThaumcraftApi.internalMethods.getBonusObjectTags(is, ot);
    }

    public static AspectList generateTags(Item item, int meta) {
        return ThaumcraftApi.internalMethods.generateTags(item, meta);
    }

    public static boolean containsMatch(boolean strict, ItemStack[] inputs, ItemStack... targets) {
        for (ItemStack input : inputs) {
            for (ItemStack target : targets) {
                if (itemMatches(target, input, strict)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean areItemStackTagsEqualForCrafting(ItemStack slotItem, ItemStack recipeItem) {
        if (recipeItem == null || slotItem == null) return false;
        if (recipeItem.hasTagCompound() && !slotItem.hasTagCompound()) return false;
        if (!recipeItem.hasTagCompound()) return true;

        for (String s : recipeItem.getTagCompound().getKeySet()) {
            if (slotItem.getTagCompound().hasKey(s)) {
                if (!slotItem.getTagCompound().getTag(s).toString().equals(
                        recipeItem.getTagCompound().getTag(s).toString())) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    public static boolean itemMatches(ItemStack target, ItemStack input, boolean strict) {
        if (input == null && target != null || input != null && target == null) {
            return false;
        }
        return (target.getItem() == input.getItem() &&
                ((target.getItemDamage() == OreDictionary.WILDCARD_VALUE && !strict) || target.getItemDamage() == input.getItemDamage()));
    }


    public static TileEntity getConnectableTile(World world, int x, int y, int z, EnumFacing face) {
        TileEntity te = world.getTileEntity(new BlockPos(x + face.getXOffset(), y + face.getYOffset(), z + face.getZOffset()));
        if (te instanceof IEssentiaTransport && ((IEssentiaTransport) te).isConnectable(face.getOpposite()))
            return te;
        else
            return null;
    }

    public static TileEntity getConnectableTile(IBlockAccess world, int x, int y, int z, EnumFacing face) {
        TileEntity te = world.getTileEntity(new BlockPos(x + face.getXOffset(), y + face.getYOffset(), z + face.getZOffset()));
        if (te instanceof IEssentiaTransport && ((IEssentiaTransport) te).isConnectable(face.getOpposite()))
            return te;
        else
            return null;
    }

    private static HashMap<Integer, AspectList> allAspects = new HashMap<>();
    private static HashMap<Integer, AspectList> allCompoundAspects = new HashMap<>();

    public static AspectList getAllAspects(int amount) {
        if (allAspects.get(amount) == null) {
            AspectList al = new AspectList();
            for (Aspect aspect : Aspect.aspects.values()) {
                al.add(aspect, amount);
            }
            allAspects.put(amount, al);
        }
        return allAspects.get(amount);
    }

    public static AspectList getAllCompoundAspects(int amount) {
        if (allCompoundAspects.get(amount) == null) {
            AspectList al = new AspectList();
            for (Aspect aspect : Aspect.getCompoundAspects()) {
                al.add(aspect, amount);
            }
            allCompoundAspects.put(amount, al);
        }
        return allCompoundAspects.get(amount);
    }


    /**
     * Use to subtract vis from a wand for most operations
     * Wands store vis differently so "real" vis costs need to be multiplied by 100 before calling this method
     *
     * @param wand     the wand itemstack
     * @param player   the player using the wand
     * @param cost     the cost of the operation.
     * @param doit     actually subtract the vis from the wand if true - if false just simulate the result
     * @param crafting is this a crafting operation or not - if
     *                 false then things like frugal and potency will apply to the costs
     * @return was the vis successfully subtracted
     */
    public static boolean consumeVisFromWand(ItemStack wand, EntityPlayer player,
                                             AspectList cost, boolean doit, boolean crafting) {
        return ThaumcraftApi.internalMethods.consumeVisFromWand(wand, player, cost, doit, crafting);
    }

    /**
     * Subtract vis for use by a crafting mechanic. Costs are calculated slightly
     * differently and things like the frugal enchant is ignored
     * Must NOT be multiplied by 100 - send the actual vis cost
     *
     * @param wand   the wand itemstack
     * @param player the player using the wand
     * @param cost   the cost of the operation.
     * @param doit   actually subtract the vis from the wand if true - if false just simulate the result
     * @return was the vis successfully subtracted
     */
    public static boolean consumeVisFromWandCrafting(ItemStack wand, EntityPlayer player,
                                                     AspectList cost, boolean doit) {
        return ThaumcraftApi.internalMethods.consumeVisFromWandCrafting(wand, player, cost, doit);
    }

    /**
     * Subtract vis from a wand the player is carrying. Works like consumeVisFromWand in that actual vis
     * costs should be multiplied by 100. The costs are handled like crafting however and things like
     * frugal don't effect them
     *
     * @param player the player using the wand
     * @param cost   the cost of the operation.
     * @return was the vis successfully subtracted
     */
    public static boolean consumeVisFromInventory(EntityPlayer player, AspectList cost) {
        return ThaumcraftApi.internalMethods.consumeVisFromInventory(player, cost);
    }


    /**
     * This adds permanents or temporary warp to a player. It will automatically be synced clientside
     *
     * @param player    the player using the wand
     * @param amount    how much warp to add. Negative amounts are only valid for temporary warp
     * @param temporary add temporary warp instead of permanent
     */
    public static void addWarpToPlayer(EntityPlayer player, int amount, boolean temporary) {
        ThaumcraftApi.internalMethods.addWarpToPlayer(player, amount, temporary);
    }

    /**
     * This "sticky" warp to a player. Sticky warp is permanent warp that can be removed.
     * It will automatically be synced clientside
     *
     * @param player the player using the wand
     * @param amount how much warp to add. Can have negative amounts.
     */
    public static void addStickyWarpToPlayer(EntityPlayer player, int amount) {
        ThaumcraftApi.internalMethods.addStickyWarpToPlayer(player, amount);
    }

    /**
     * Performs a ray trace from v1 to v2, skipping the block the source position is inside.
     * Delegates to World.rayTraceBlocks() in 1.12.2.
     *
     * @param world   the world
     * @param v1      start position
     * @param v2      end position
     * @param bool1   stopOnLiquid
     * @param bool2   ignoreBlockWithoutBoundingBox
     * @param bool3   returnLastUncollidableBlock
     */
    public static RayTraceResult rayTraceIgnoringSource(World world, Vec3d v1, Vec3d v2,
                                                              boolean bool1, boolean bool2, boolean bool3) {
        return world.rayTraceBlocks(v1, v2, bool1, bool2, bool3);
    }
}
