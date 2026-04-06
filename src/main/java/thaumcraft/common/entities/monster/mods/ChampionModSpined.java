package thaumcraft.common.entities.monster.mods;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import thaumcraft.common.Thaumcraft;

public class ChampionModSpined implements IChampionModifierEffect {
   public float performEffect(EntityLivingBase boss, EntityLivingBase target, DamageSource source, float amount) {
      if (target != null && !source.damageType.equalsIgnoreCase("thorns")) {
         target.attackEntityFrom(DamageSource.causeThornsDamage(boss), (float)(1 + boss.world.rand.nextInt(3)));
         { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("minecraft:enchant.thorns.hit")); if (_snd != null) target.playSound(_snd, 0.5F, 1.0F); }
      }
       return amount;
   }

   @SideOnly(Side.CLIENT)
   public void showFX(EntityLivingBase boss) {
      if (!boss.world.rand.nextBoolean()) {
         float w = boss.world.rand.nextFloat() * boss.width;
         float d = boss.world.rand.nextFloat() * boss.width;
         float h = boss.world.rand.nextFloat() * boss.height;
         int p = 176 + boss.world.rand.nextInt(4) * 3;
         Thaumcraft.proxy.drawGenericParticles(boss.world, boss.getEntityBoundingBox().minX + (double)w, boss.getEntityBoundingBox().minY + (double)h, boss.getEntityBoundingBox().minZ + (double)d, 0.0F, 0.0F, 0.0F, 0.5F + boss.world.rand.nextFloat() * 0.2F, 0.1F + boss.world.rand.nextFloat() * 0.2F, 0.1F + boss.world.rand.nextFloat() * 0.2F, 0.7F, false, p, 3, 1, 3, 0, 1.2F + boss.world.rand.nextFloat() * 0.3F);
      }
   }
}
