package thaumcraft.api.crafting;

import java.util.ArrayList;
import java.util.Map;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.AspectList;

public class InfusionEnchantmentRecipe
{
	
	public AspectList aspects;
	public String research;
	public ItemStack[] components;
	public Enchantment enchantment;
	public int recipeXP;
	public int instability;
	
	public InfusionEnchantmentRecipe(String research, Enchantment input, int inst, 
			AspectList aspects2, ItemStack[] recipe) {
		this.research = research;
		this.enchantment = input;
		this.aspects = aspects2;
		this.components = recipe;
		this.instability = inst;
		this.recipeXP = Math.max(1, input.getMinEnchantability(1)/3);
	}

	/**
     * Used to check if a recipe matches current crafting inventory
     * @param player 
     */
	public boolean matches(ArrayList<ItemStack> input, ItemStack central, World world, EntityPlayer player) {
		if (!research.isEmpty() && !ThaumcraftApiHelper.isResearchComplete(player.getName(), research)) {
    		return false;
    	}
		
		if (!enchantment.canApply(central)) {
			return false;
		}

		Map<Enchantment, Integer> map1 = EnchantmentHelper.getEnchantments(central);
        for (Map.Entry<Enchantment, Integer> entry : map1.entrySet()) {
            Enchantment ench = entry.getKey();
            if (ench == enchantment &&
                    entry.getValue() >= ench.getMaxLevel())
                return false;
            if (ench != enchantment &&
                    (!enchantment.isCompatibleWith(ench) ||
                            !ench.isCompatibleWith(enchantment))) {
                return false;
            }
        }
		
		ItemStack i2 = null;
		
		ArrayList<ItemStack> ii = new ArrayList<>();
		for (ItemStack is:input) {
			ii.add(is.copy());
		}
		
		for (ItemStack comp:components) {
			boolean b=false;
			for (int a=0;a<ii.size();a++) {
				 i2 = ii.get(a).copy();
				if (comp.getItemDamage()==OreDictionary.WILDCARD_VALUE) {
					i2.setItemDamage(OreDictionary.WILDCARD_VALUE);
				}
				if (areItemStacksEqual(i2, comp,true)) {
					ii.remove(a);
					b=true;
					break;
				}
			}
			if (!b) return false;
		}
//		System.out.println(ii.size());
		return ii.isEmpty();
    }
	
	protected boolean areItemStacksEqual(ItemStack stack0, ItemStack stack1, boolean fuzzy)
    {
		if (stack0==null && stack1!=null) return false;
		if (stack0!=null && stack1==null) return false;
		if (stack0==null && stack1==null) return true;
		boolean t1=ThaumcraftApiHelper.areItemStackTagsEqualForCrafting(stack0, stack1);
		if (!t1) return false;
		if (fuzzy) {
			int[] ods = OreDictionary.getOreIDs(stack0);
			for (int od : ods) {
				String oreName = OreDictionary.getOreName(od);
				if (!oreName.equals("Unknown")) {
					ItemStack[] ores = OreDictionary.getOres(oreName).toArray(new ItemStack[]{});
					if (ThaumcraftApiHelper.containsMatch(false, new ItemStack[]{stack1}, ores))
						return true;
				}
			}
		}
        return stack0.getItem() == stack1.getItem() && (stack0.getItemDamage() == stack1.getItemDamage() && stack0.getCount() <= stack0.getMaxStackSize());
    }
	
   
    public Enchantment getEnchantment() {
		return enchantment;
    	
    }
    
    public AspectList getAspects() {
		return aspects;
    	
    }
    
    public String getResearch() {
		return research;
    	
    }

	public int calcInstability(ItemStack recipeInput) {
		int i = 0;
		Map<Enchantment, Integer> map1 = EnchantmentHelper.getEnchantments(recipeInput);
        for (Map.Entry<Enchantment, Integer> entry : map1.entrySet()) {
            i += EnchantmentHelper.getEnchantmentLevel(entry.getKey(), recipeInput);
        }
		return (i/2) + instability;
	}

	public int calcXP(ItemStack recipeInput) {
		return recipeXP * (1+EnchantmentHelper.getEnchantmentLevel(enchantment, recipeInput));
	}

	public float getEssentiaMod(ItemStack recipeInput) {
		float mod = EnchantmentHelper.getEnchantmentLevel(enchantment, recipeInput);
		Map<Enchantment, Integer> map1 = EnchantmentHelper.getEnchantments(recipeInput);
        for (Map.Entry<Enchantment, Integer> entry : map1.entrySet()) {
            if (entry.getKey() != enchantment)
                mod += entry.getValue() * .1f;
        }
		return mod;
	}

}
