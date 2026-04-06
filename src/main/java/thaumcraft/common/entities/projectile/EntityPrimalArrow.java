package thaumcraft.common.entities.projectile;

import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import io.netty.buffer.ByteBuf;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketChangeGameState;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import thaumcraft.api.damagesource.DamageSourceIndirectThaumcraftEntity;

public class EntityPrimalArrow extends EntityArrow implements IEntityAdditionalSpawnData {
    public int type;
    public int shootingEntityId;

    public EntityPrimalArrow(World world) {
        super(world);
        this.setSize(0.5F, 0.5F);
        this.pickupStatus = PickupStatus.DISALLOWED;
    }

    public EntityPrimalArrow(World world, double x, double y, double z) {
        super(world);
        this.setSize(0.25F, 0.25F);
        this.setPosition(x, y, z);
        this.pickupStatus = PickupStatus.DISALLOWED;
    }

    public EntityPrimalArrow(World world, EntityLivingBase shooter, float velocity, int type) {
        super(world, shooter);
        this.type = type;
        this.pickupStatus = PickupStatus.DISALLOWED;
        if (shooter != null) {
            this.shootingEntityId = shooter.getEntityId();
        }
        Vec3d look = shooter.getLook(1.0F);
        this.shoot(look.x, look.y, look.z, velocity * 1.5F, 1.0F);
    }

    @Override
    protected ItemStack getArrowStack() {
        return new ItemStack(Items.ARROW);
    }

    @Override
    protected void onHit(RayTraceResult mop) {
        if (mop.entityHit != null) {
            inflictDamage(mop);
        } else {
            super.onHit(mop);
        }
    }

    public boolean inflictDamage(RayTraceResult mop) {
        float speed = MathHelper.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
        int i1 = MathHelper.ceil((double)speed * getDamageForType());
        int fire = this.isBurning() && this.type != 2 ? 5 : 0;
        if (this.getIsCritical()) {
            i1 += this.rand.nextInt(i1 / 2 + 2);
        }

        DamageSource damagesource;
        switch (this.type) {
            case 0:
                damagesource = this.shootingEntity == null
                    ? new DamageSourceIndirectThaumcraftEntity("airarrow", this, this).setDamageBypassesArmor().setMagicDamage().setProjectile()
                    : new DamageSourceIndirectThaumcraftEntity("airarrow", this, this.shootingEntity).setDamageBypassesArmor().setMagicDamage().setProjectile();
                break;
            case 1:
                fire += 5;
                damagesource = this.shootingEntity == null
                    ? new DamageSourceIndirectThaumcraftEntity("firearrow", this, this).setFireDamage().setProjectile()
                    : new DamageSourceIndirectThaumcraftEntity("firearrow", this, this.shootingEntity).setFireDamage().setProjectile();
                break;
            case 2:
                if (mop.entityHit instanceof EntityLivingBase) {
                    ((EntityLivingBase)mop.entityHit).addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 200, 4));
                }
                damagesource = this.shootingEntity == null
                    ? new EntityDamageSourceIndirect("arrow", this, this).setProjectile()
                    : new EntityDamageSourceIndirect("arrow", this, this.shootingEntity).setProjectile();
                break;
            case 4:
                if (mop.entityHit instanceof EntityLivingBase) {
                    ((EntityLivingBase)mop.entityHit).addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 200, 4));
                }
                damagesource = this.shootingEntity == null
                    ? new DamageSourceIndirectThaumcraftEntity("orderarrow", this, this).setDamageBypassesArmor().setMagicDamage().setProjectile()
                    : new DamageSourceIndirectThaumcraftEntity("orderarrow", this, this.shootingEntity).setDamageBypassesArmor().setMagicDamage().setProjectile();
                break;
            case 5:
                if (mop.entityHit instanceof EntityLivingBase) {
                    ((EntityLivingBase)mop.entityHit).addPotionEffect(new PotionEffect(MobEffects.WITHER, 100));
                }
                damagesource = this.shootingEntity == null
                    ? new EntityDamageSourceIndirect("arrow", this, this).setProjectile()
                    : new EntityDamageSourceIndirect("arrow", this, this.shootingEntity).setProjectile();
                break;
            default:
                damagesource = this.shootingEntity == null
                    ? new EntityDamageSourceIndirect("arrow", this, this).setProjectile()
                    : new EntityDamageSourceIndirect("arrow", this, this.shootingEntity).setProjectile();
                break;
        }

        if (fire > 0 && !(mop.entityHit instanceof EntityEnderman)) {
            mop.entityHit.setFire(fire);
        }

        boolean hit = mop.entityHit.attackEntityFrom(damagesource, (float)i1);
        if (hit) {
            if (mop.entityHit instanceof EntityLivingBase) {
                EntityLivingBase living = (EntityLivingBase)mop.entityHit;
                if (this.shootingEntity instanceof EntityLivingBase) {
                    EnchantmentHelper.applyThornEnchantments(living, this.shootingEntity);
                    EnchantmentHelper.applyArthropodEnchantments((EntityLivingBase)this.shootingEntity, living);
                }
                if (this.shootingEntity != null && mop.entityHit != this.shootingEntity
                        && mop.entityHit instanceof EntityPlayer && this.shootingEntity instanceof EntityPlayerMP) {
                    ((EntityPlayerMP)this.shootingEntity).connection.sendPacket(new SPacketChangeGameState(6, 0.0F));
                }
            }
            this.playSound(SoundEvents.ENTITY_ARROW_HIT, 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
            if (!(mop.entityHit instanceof EntityEnderman)) {
                this.setDead();
            }
        } else {
            this.motionX *= -0.1F;
            this.motionY *= -0.1F;
            this.motionZ *= -0.1F;
            this.rotationYaw += 180.0F;
            this.prevRotationYaw += 180.0F;
        }
        return hit;
    }

    private double getDamageForType() {
        double base = 2.1;
        switch (this.type) {
            case 3: return base * 1.5;
            case 4: return base * 0.8;
            case 5: return base * 0.8;
            default: return base;
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        tag.setByte("type", (byte)this.type);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        this.type = tag.getByte("type");
    }

    @Override
    public void writeSpawnData(ByteBuf data) {
        data.writeDouble(this.motionX);
        data.writeDouble(this.motionY);
        data.writeDouble(this.motionZ);
        data.writeFloat(this.rotationYaw);
        data.writeFloat(this.rotationPitch);
        data.writeByte(this.type);
        data.writeInt(this.shootingEntityId);
    }

    @Override
    public void readSpawnData(ByteBuf data) {
        this.motionX = data.readDouble();
        this.motionY = data.readDouble();
        this.motionZ = data.readDouble();
        this.rotationYaw = data.readFloat();
        this.rotationPitch = data.readFloat();
        this.prevRotationYaw = this.rotationYaw;
        this.prevRotationPitch = this.rotationPitch;
        this.type = data.readByte();
        this.shootingEntityId = data.readInt();
    }
}
