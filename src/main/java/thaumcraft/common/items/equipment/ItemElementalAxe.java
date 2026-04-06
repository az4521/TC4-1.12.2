package thaumcraft.common.items.equipment;

import com.google.common.collect.ImmutableSet;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.block.Block;
import thaumcraft.client.renderers.compat.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import thaumcraft.api.IRepairable;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.entities.EntityFollowingItem;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.fx.PacketFXBlockBubble;
import thaumcraft.common.lib.utils.BlockUtils;
import thaumcraft.common.lib.utils.EntityUtils;
import thaumcraft.common.lib.utils.Utils;
import net.minecraft.util.math.BlockPos;

public class ItemElementalAxe extends ItemAxe implements IRepairable {
    public TextureAtlasSprite icon;
    boolean alternateServer = false;
    boolean alternateClient = false;
    public static final List<List<Integer>> oreDictLogs = new ArrayList<>();

    public ItemElementalAxe(Item.ToolMaterial enumtoolmaterial) {
        super(enumtoolmaterial, 9.0F, -3.0F);
        this.setCreativeTab(Thaumcraft.tabTC);
    }

    @Override
    public Set<String> getToolClasses(ItemStack stack) {
        return ImmutableSet.of("axe");
    }

    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister ir) {
        this.icon = ir.registerSprite("thaumcraft:elementalaxe");
    }

    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite getIconFromDamage(int par1) {
        return this.icon;
    }

    @Override
    public EnumRarity getRarity(ItemStack itemstack) {
        return EnumRarity.RARE;
    }

    @Override
    public boolean getIsRepairable(ItemStack par1ItemStack, ItemStack par2ItemStack) {
        return par2ItemStack.isItemEqual(new ItemStack(ConfigItems.itemResource, 1, 2)) || super.getIsRepairable(par1ItemStack, par2ItemStack);
    }

    @Override
    public EnumAction getItemUseAction(ItemStack itemstack) {
        return EnumAction.BOW;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public net.minecraft.util.ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer player, net.minecraft.util.EnumHand hand) {
        ItemStack itemStackIn = player.getHeldItem(hand);
        player.setActiveHand(hand);
        return new net.minecraft.util.ActionResult<>(net.minecraft.util.EnumActionResult.SUCCESS, itemStackIn);
    }

    @Override
    public void onUsingTick(ItemStack stack, net.minecraft.entity.EntityLivingBase living, int count) {
        if (!(living instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) living;
        ArrayList<Entity> stuff = EntityUtils.getEntitiesInRange(player.world, player.posX, player.posY, player.posZ, player, EntityItem.class, 10.0F);
        if (stuff != null && !stuff.isEmpty()) {
            for (Entity e : stuff) {
                if ((!(e instanceof EntityFollowingItem) || ((EntityFollowingItem) e).target == null) && !e.isDead && e instanceof EntityItem) {
                    double d6 = e.posX - player.posX;
                    double d8 = e.posY - player.posY + (double) (player.height / 2.0F);
                    double d10 = e.posZ - player.posZ;
                    double d11 = MathHelper.sqrt(d6 * d6 + d8 * d8 + d10 * d10);
                    d6 /= d11;
                    d8 /= d11;
                    d10 /= d11;
                    double d13 = 0.3;
                    e.motionX -= d6 * d13;
                    e.motionY -= d8 * d13;
                    e.motionZ -= d10 * d13;
                    if (e.motionX > 0.35) {
                        e.motionX = 0.35;
                    }

                    if (e.motionX < -0.35) {
                        e.motionX = -0.35;
                    }

                    if (e.motionY > 0.35) {
                        e.motionY = 0.35;
                    }

                    if (e.motionY < -0.35) {
                        e.motionY = -0.35;
                    }

                    if (e.motionZ > 0.35) {
                        e.motionZ = 0.35;
                    }

                    if (e.motionZ < -0.35) {
                        e.motionZ = -0.35;
                    }

                    Thaumcraft.proxy.crucibleBubble(player.world, (float) e.posX + (player.world.rand.nextFloat() - player.world.rand.nextFloat()) * 0.125F, (float) e.posY + (player.world.rand.nextFloat() - player.world.rand.nextFloat()) * 0.125F, (float) e.posZ + (player.world.rand.nextFloat() - player.world.rand.nextFloat()) * 0.125F, 0.33F, 0.33F, 1.0F);
                }
            }
        }

    }

    @Override
    public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, EntityPlayer player) {
        World world = player.world;
        int x = pos.getX(), y = pos.getY(), z = pos.getZ();
        Block bi = world.getBlockState(pos).getBlock();
        if (!player.isSneaking() && Utils.isWoodLog(world, x, y, z)) {
            if (!world.isRemote) {
                BlockUtils.breakFurthestBlock(world, x, y, z, bi, player, true, 10);
                PacketHandler.INSTANCE.sendToAllAround(new PacketFXBlockBubble(x, y, z, (new Color(0.33F, 0.33F, 1.0F)).getRGB()), new NetworkRegistry.TargetPoint(world.provider.getDimension(), x, y, z, 32.0F));
                { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:bubble")); if (_snd != null) world.playSound(null, x, y, z, _snd, net.minecraft.util.SoundCategory.NEUTRAL, 0.15F, 1.0F); }
            }

            itemstack.damageItem(1, player);
            return true;
        } else {
            return super.onBlockStartBreak(itemstack, pos, player);
        }
    }
}
