package thaumcraft.common.items.equipment;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Objects;

import thaumcraft.client.renderers.compat.IIconRegister;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.DamageSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import thaumcraft.api.IRepairable;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.entities.golems.EntityGolemBase;
import thaumcraft.common.lib.utils.Utils;

public class ItemElementalSword extends ItemSword implements IRepairable {
    public TextureAtlasSprite icon;

    public ItemElementalSword(Item.ToolMaterial enumtoolmaterial) {
        super(enumtoolmaterial);
        this.setCreativeTab(Thaumcraft.tabTC);
    }

    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister ir) {
        this.icon = ir.registerSprite("thaumcraft:elementalsword");
    }

    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite getIconFromDamage(int par1) {
        return this.icon;
    }

    public EnumRarity getRarity(ItemStack itemstack) {
        return EnumRarity.RARE;
    }

    public boolean getIsRepairable(ItemStack par1ItemStack, ItemStack par2ItemStack) {
        return par2ItemStack.isItemEqual(new ItemStack(ConfigItems.itemResource, 1, 2)) || super.getIsRepairable(par1ItemStack, par2ItemStack);
    }

    public void onUsingTick(ItemStack stack, EntityPlayer player, int count) {
        super.onUsingTick(stack, player, count);
        int ticks = this.getMaxItemUseDuration(stack) - count;
        if (player.motionY < (double) 0.0F) {
            player.motionY /= 1.2F;
            player.fallDistance /= 1.2F;
        }

        player.motionY += 0.08F;
        if (player.motionY > (double) 0.5F) {
            player.motionY = 0.2F;
        }

        if (player instanceof EntityPlayerMP) {
            Utils.resetFloatCounter((EntityPlayerMP) player);
        }

        List targets = player.world.getEntitiesWithinAABBExcludingEntity(player, player.getEntityBoundingBox().expand(2.5F, 2.5F, 2.5F));
        if (!targets.isEmpty()) {
            for (Object target : targets) {
                Entity entity = (Entity) target;
                if (!(entity instanceof EntityPlayer) && !entity.isDead && (player.getRidingEntity() == null || player.getRidingEntity() != entity)) {
                    Vec3d p = new Vec3d(player.posX, player.posY, player.posZ);
                    Vec3d t = new Vec3d(entity.posX, entity.posY, entity.posZ);
                    double distance = p.distanceTo(t) + 0.1;
                    Vec3d r = new Vec3d(t.x - p.x, t.y - p.y, t.z - p.z);
                    entity.motionX += r.x / (double) 2.5F / distance;
                    entity.motionY += r.y / (double) 2.5F / distance;
                    entity.motionZ += r.z / (double) 2.5F / distance;
                }
            }
        }

        if (player.world.isRemote) {
            int miny = (int) (player.getEntityBoundingBox().minY - (double) 2.0F);
            if (player.onGround) {
                miny = MathHelper.floor(player.getEntityBoundingBox().minY);
            }

            for (int a = 0; a < 5; ++a) {
                Thaumcraft.proxy.smokeSpiral(player.world, player.posX, player.getEntityBoundingBox().minY + (double) (player.height / 2.0F), player.posZ, 1.5F, player.world.rand.nextInt(360), miny, 14540253);
            }

            if (player.onGround) {
                float r1 = player.world.rand.nextFloat() * 360.0F;
                float mx = -MathHelper.sin(r1 / 180.0F * (float) Math.PI) / 5.0F;
                float mz = MathHelper.cos(r1 / 180.0F * (float) Math.PI) / 5.0F;
                player.world.spawnParticle(net.minecraft.util.EnumParticleTypes.SMOKE_NORMAL, player.posX, player.getEntityBoundingBox().minY + (double) 0.1F, player.posZ, mx, 0.0F, mz);
            }
        } else if (ticks % 20 == 0) {
            { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:wind")); if (_snd != null) player.world.playSound(null, player.posX, player.posY, player.posZ, _snd, net.minecraft.util.SoundCategory.NEUTRAL, 0.5F, 0.9F + player.world.rand.nextFloat() * 0.2F); };
        }

        if (ticks % 20 == 0) {
            stack.damageItem(1, player);
        }

    }

    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
        if (entity.isEntityAlive()) {
            List<Entity> targets = (List<Entity>) player.world.getEntitiesWithinAABBExcludingEntity(player, entity.getEntityBoundingBox().expand(1.2, 1.1, 1.2));
            int count = 0;
            if (targets.size() > 1) {
                for (Entity target : targets) {
                    if (!target.isDead
                            && (!(target instanceof EntityGolemBase) || !Objects.equals(((EntityGolemBase) target).getOwnerName(), player.getName()))
                            && (!(target instanceof EntityTameable) || !Objects.equals(((EntityTameable) target).getOwnerId(), player.getUniqueID()))
                            && target instanceof EntityLiving
                            && target.getEntityId() != entity.getEntityId()) {
                        if (target.isEntityAlive()) {
                            this.attackTargetEntityWithCurrentItem(target, player);
                            ++count;
                        }
                    }
                }

                if (count > 0 && !player.world.isRemote) {
                    { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:swing")); if (_snd != null) player.world.playSound(null, entity.posX, entity.posY, entity.posZ, _snd, net.minecraft.util.SoundCategory.NEUTRAL, 1.0F, 0.9F + player.world.rand.nextFloat() * 0.2F); };
                }
            }
        }

        return super.onLeftClickEntity(stack, player, entity);
    }

    public void attackTargetEntityWithCurrentItem(Entity par1Entity, EntityPlayer player) {
        if (!MinecraftForge.EVENT_BUS.post(new AttackEntityEvent(player, par1Entity))) {
            if (!par1Entity.hitByEntity(player)) {
                float f = (float) player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
                int i = 0;
                float f1 = 0.0F;
                if (par1Entity instanceof EntityLivingBase) {
                    f1 = EnchantmentHelper.getModifierForCreature(player.getHeldItemMainhand(), par1Entity instanceof net.minecraft.entity.EntityLivingBase ? ((net.minecraft.entity.EntityLivingBase) par1Entity).getCreatureAttribute() : net.minecraft.entity.EnumCreatureAttribute.UNDEFINED);
                    i += EnchantmentHelper.getKnockbackModifier(player);
                }

                if (player.isSprinting()) {
                    ++i;
                }

                if (f > 0.0F || f1 > 0.0F) {
                    boolean flag = player.fallDistance > 0.0F && !player.onGround && !player.isOnLadder() && !player.isInWater() && !player.isPotionActive(net.minecraft.init.MobEffects.BLINDNESS) && player.getRidingEntity() == null && par1Entity instanceof EntityLivingBase;
                    if (flag && f > 0.0F) {
                        f *= 1.5F;
                    }

                    f += f1;
                    boolean flag1 = false;
                    int j = EnchantmentHelper.getFireAspectModifier(player);
                    if (par1Entity instanceof EntityLivingBase && j > 0 && !par1Entity.isBurning()) {
                        flag1 = true;
                        par1Entity.setFire(1);
                    }

                    boolean flag2 = par1Entity.attackEntityFrom(DamageSource.causePlayerDamage(player), f);
                    if (flag2) {
                        if (i > 0) {
                            par1Entity.addVelocity(-MathHelper.sin(player.rotationYaw * (float) Math.PI / 180.0F) * (float) i * 0.5F, 0.1, MathHelper.cos(player.rotationYaw * (float) Math.PI / 180.0F) * (float) i * 0.5F);
                            player.motionX *= 0.6;
                            player.motionZ *= 0.6;
                            player.setSprinting(false);
                        }

                        if (flag) {
                            player.onCriticalHit(par1Entity);
                        }

                        if (f1 > 0.0F) {
                            player.onEnchantmentCritical(par1Entity);
                        }

                        if (par1Entity instanceof EntityLivingBase) {
                            EnchantmentHelper.applyThornEnchantments((EntityLivingBase) par1Entity, player);
                        }
                    }

                    ItemStack itemstack = player.getHeldItemMainhand();
                    Object object = par1Entity;
                    if (par1Entity instanceof MultiPartEntityPart) {
                        net.minecraft.entity.IEntityMultiPart multipartParent = ((MultiPartEntityPart) par1Entity).parent;
                        if (multipartParent instanceof EntityLivingBase) {
                            object = (EntityLivingBase) multipartParent;
                        }
                    }

                    if (itemstack != null && object instanceof EntityLivingBase) {
                        itemstack.hitEntity((EntityLivingBase) object, player);
                        if (itemstack.isEmpty()) {
                            player.setHeldItem(net.minecraft.util.EnumHand.MAIN_HAND, net.minecraft.item.ItemStack.EMPTY);
                        }
                    }

                    if (par1Entity instanceof EntityLivingBase) {
                        player.addStat(net.minecraft.stats.StatList.DAMAGE_DEALT, Math.round(f * 10.0F));
                        if (j > 0 && flag2) {
                            par1Entity.setFire(j * 4);
                        } else if (flag1) {
                            par1Entity.extinguish();
                        }
                    }

                    player.addExhaustion(0.3F);
                }
            }

        }
    }
}
