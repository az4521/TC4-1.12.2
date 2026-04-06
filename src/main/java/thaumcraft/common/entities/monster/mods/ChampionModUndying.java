package thaumcraft.common.entities.monster.mods;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import thaumcraft.common.Thaumcraft;

public class ChampionModUndying implements IChampionModifierEffect {
   public float performEffect(EntityLivingBase mob, EntityLivingBase target, DamageSource source, float amount) {
      if (mob.ticksExisted % 20 == 0) {
         mob.heal(1.0F);
      }

      return amount;
   }

   @SideOnly(Side.CLIENT)
   public void showFX(EntityLivingBase boss) {
      if (!boss.world.rand.nextBoolean()) {
         float w = boss.world.rand.nextFloat() * boss.width;
         float d = boss.world.rand.nextFloat() * boss.width;
         float h = boss.world.rand.nextFloat() * boss.height;
         Thaumcraft.proxy.drawGenericParticles(boss.world, boss.getEntityBoundingBox().minX + (double)w, boss.getEntityBoundingBox().minY + (double)h, boss.getEntityBoundingBox().minZ + (double)d, 0.0F, 0.03, 0.0F, 0.1F + boss.world.rand.nextFloat() * 0.1F, 0.8F + boss.world.rand.nextFloat() * 0.2F, 0.1F + boss.world.rand.nextFloat() * 0.1F, 0.9F, true, 21, 4, 1, 4 + boss.world.rand.nextInt(4), 0, 0.5F + boss.world.rand.nextFloat() * 0.2F);
      }
   }
}
