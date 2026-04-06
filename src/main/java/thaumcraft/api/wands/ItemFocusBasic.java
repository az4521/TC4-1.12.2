package thaumcraft.api.wands;

import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemFocusBasic extends Item {
	
	public ItemFocusBasic ()
    {
        super();
        maxStackSize = 1;
        canRepair=false;
        this.setMaxDamage(0);
    }
	
	public TextureAtlasSprite icon;
	
	@SideOnly(Side.CLIENT)
	public TextureAtlasSprite getIconFromDamage(int par1) {
		return icon;
	}
	
	@Override
	public boolean isDamageable() {
		return false;
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<String> list, ITooltipFlag flag) {
		AspectList al = this.getVisCost(stack);
		if (al!=null && al.size()>0) {
			list.add(I18n.translateToLocal(isVisCostPerTick(stack)?"item.Focus.cost2":"item.Focus.cost1"));
			for (Aspect aspect:al.getAspectsSorted()) {
				DecimalFormat myFormatter = new DecimalFormat("#####.##");
				String amount = myFormatter.format(al.getAmount(aspect)/100f);
				list.add(" §"+aspect.getChatcolor()+aspect.getName()+"§r x "+ amount);
			}
		}
		addFocusInformation(stack, list, flag);
	}

	public void addFocusInformation(ItemStack focusstack, List<String> list, ITooltipFlag flag) {
		LinkedHashMap<Short, Integer> map = new LinkedHashMap<>();
		for (short id:this.getAppliedUpgrades(focusstack)) {
			if (id>=0) {
				int amt = 1;
				if (map.containsKey(id)) {
					amt = map.get(id) + 1;				
				}
				map.put(id, amt);
			}
		}
		for (Short id:map.keySet()) {	
			list.add(TextFormatting.DARK_PURPLE +FocusUpgradeType.types[id].getLocalizedName()+
					(map.get(id)>1?" "+I18n.translateToLocal("enchantment.level." + map.get(id)):""));
		}
	}
	
	/**
	 * Purely for display on the focus tooltip (see addInformation method above)
	 */
	public boolean isVisCostPerTick(ItemStack focusstack) {
		return false;
	}
	
	@Override
	public EnumRarity getRarity(ItemStack focusstack)
    {
        return EnumRarity.RARE;
    }	
	
	/**
	 * What color will the focus orb be rendered on the held wand
	 */
	public int getFocusColor(ItemStack focusstack) {
		return 0;
	}

	
	/**
	 * Does the focus have ornamentation like the focus of the nine hells. Ornamentation is a standard icon rendered in a cross around the focus
	 */	
	public TextureAtlasSprite getOrnament(ItemStack focusstack) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * An icon to be rendered inside the focus itself
	 */
	public TextureAtlasSprite getFocusDepthLayerIcon(ItemStack focusstack) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public enum WandFocusAnimation {
		WAVE, CHARGE
    }

	public WandFocusAnimation getAnimation(ItemStack focusstack) {
		return WandFocusAnimation.WAVE;
	}
	
	/**
	 * Just insert two alphanumeric characters before this string in your focus item class
	 */
	public String getSortingHelper(ItemStack focusstack) {		
		StringBuilder out= new StringBuilder();
		for (short id:this.getAppliedUpgrades(focusstack)) {
			out.append(id);
		}
		return out.toString();
	}
	
	
	/**
	 * How much vis does this focus consume per activation. 
	 */
	public AspectList getVisCost(ItemStack focusstack) {
		return null;
	}
	
	/**
	 * This returns how many milliseconds must pass before the focus can be activated again.
	 */	
	public int getActivationCooldown(ItemStack focusstack) {
		return 0;
	}	
	
	/**
	 * Used by foci like equal trade to determine their area in artchitect mode 
	 */
	public int getMaxAreaSize(ItemStack focusstack) {
		return 1;
	}
	
	/**
	 * What upgrades can be applied to this focus for ranks 1 to 5
	 */
	public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack focusstack, int rank) {
		return null;
	}
	
	/**
	 * What upgrades does the focus currently have
	 */
	public short[] getAppliedUpgrades(ItemStack focusstack) {
		short[] l = new short[] {-1,-1,-1,-1,-1};
		NBTTagList nbttaglist = getFocusUpgradeTagList(focusstack);
        if (nbttaglist != null) {
            for (int j = 0; j < nbttaglist.tagCount(); ++j) {
                if (j >= 5) break;
                l[j] = nbttaglist.getCompoundTagAt(j).getShort("id");
            }

        }
        return l;
    }
	
	public boolean applyUpgrade(ItemStack focusstack, FocusUpgradeType type, int rank) {
		short[] upgrades = getAppliedUpgrades(focusstack);
		if (upgrades[rank-1]!=-1 || rank<1 || rank>5) {
			return false;
		}
		upgrades[rank-1] = type.id;
		setFocusUpgradeTagList(focusstack, upgrades);
		return true;
	}
	
	/**
	 * Use this method to define custom logic about which upgrades can be applied. This can be used to set up upgrade "trees" 
	 * that make certain upgrades available only when others are unlocked first, when certain research is completed, or similar logic.
	 * 
	 */
	public boolean canApplyUpgrade(ItemStack focusstack, EntityPlayer player, FocusUpgradeType type, int rank) {
		return true;
	}

	/**
	 * Does this focus have the passed upgrade type
	 */
	public boolean isUpgradedWith(ItemStack focusstack, FocusUpgradeType focusUpgradetype) {
		return getUpgradeLevel(focusstack,focusUpgradetype)>0;
	}

	/**
	 * What level is the passed upgrade type on the focus. If it is not present it returns 0
	 */
	public int getUpgradeLevel(ItemStack focusstack, FocusUpgradeType focusUpgradetype) {
		short[] list = getAppliedUpgrades(focusstack);
		int level=0;
		for (short id:list) {
			if (id == focusUpgradetype.id)
            {
                level++;
            }
		}
        return level;
	}	
	
	public ItemStack onFocusRightClick(ItemStack wandstack, World world,EntityPlayer player, RayTraceResult movingobjectposition) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void onUsingFocusTick(ItemStack wandstack, EntityPlayer player,int count) {
		// TODO Auto-generated method stub		
	}
	
	public void onPlayerStoppedUsingFocus(ItemStack wandstack, World world,	EntityPlayer player, int count) {
		// TODO Auto-generated method stub
		
	}
	
	public boolean onFocusBlockStartBreak(ItemStack wandstack, int x, int y,int z, EntityPlayer player) {
		// TODO Auto-generated method stub
		return false;
	}
	
	/**
	 * Internal helper methods
	 */
	private NBTTagList getFocusUpgradeTagList(ItemStack focusstack)
    {
        return focusstack.getTagCompound() == null ? null : focusstack.getTagCompound().getTagList("upgrade", 10);
    }
	
	private void setFocusUpgradeTagList(ItemStack focusstack, short[] upgrades) {
		if (!focusstack.hasTagCompound()) 
			focusstack.setTagCompound(new NBTTagCompound());
		NBTTagCompound nbttagcompound = focusstack.getTagCompound();
		NBTTagList tlist = new NBTTagList();
		nbttagcompound.setTag("upgrade", tlist);
		for (short id : upgrades) {		
			NBTTagCompound f = new NBTTagCompound();
			f.setShort("id", id);
			tlist.appendTag(f);
		}
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
		if (stack.getTagCompound() != null && stack.getTagCompound().hasKey("ench")) {
			stack.getTagCompound().removeTag("ench");
		}
		super.onUpdate(stack, world, entity, itemSlot, isSelected);
	}

	
	
}
