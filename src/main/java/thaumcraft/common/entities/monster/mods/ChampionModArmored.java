package thaumcraft.common.entities.monster.mods;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import thaumcraft.common.Thaumcraft;

public class ChampionModArmored implements IChampionModifierEffect {
   public float performEffect(EntityLivingBase mob, EntityLivingBase target, DamageSource source, float amount) {
      if (!source.isUnblockable()) {
         float f1 = amount * 19.0F;
         amount = f1 / 25.0F;
      }

      return amount;
   }

   public void showFX(EntityLivingBase boss) {
      if (boss.world.rand.nextInt(4) == 0) {
         float w = boss.world.rand.nextFloat() * boss.width;
         float d = boss.world.rand.nextFloat() * boss.width;
         float h = boss.world.rand.nextFloat() * boss.height;
         Thaumcraft.proxy.drawGenericParticles(boss.world, boss.getEntityBoundingBox().minX + (double)w, boss.getEntityBoundingBox().minY + (double)h, boss.getEntityBoundingBox().minZ + (double)d, 0.0F, 0.0F, 0.0F, 0.9F, 0.9F, 0.9F + boss.world.rand.nextFloat() * 0.1F, 0.7F, false, 112, 9, 1, 5 + boss.world.rand.nextInt(4), 0, 0.6F + boss.world.rand.nextFloat() * 0.2F);
      }
   }
}
