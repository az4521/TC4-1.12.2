package thaumcraft.common.entities.monster.mods;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import thaumcraft.common.Thaumcraft;

public class ChampionModMighty implements IChampionModifierEffect {
   public float performEffect(EntityLivingBase boss, EntityLivingBase target, DamageSource source, float ammount) {
      return 0.0F;
   }

   @SideOnly(Side.CLIENT)
   public void showFX(EntityLivingBase boss) {
      if (!(boss.world.rand.nextFloat() > 0.3F)) {
         float w = boss.world.rand.nextFloat() * boss.width;
         float d = boss.world.rand.nextFloat() * boss.width;
         float h = boss.world.rand.nextFloat() * boss.height;
         int p = 176 + boss.world.rand.nextInt(4) * 3;
         Thaumcraft.proxy.drawGenericParticles(boss.world, boss.getEntityBoundingBox().minX + (double)w, boss.getEntityBoundingBox().minY + (double)h, boss.getEntityBoundingBox().minZ + (double)d, 0.0F, 0.0F, 0.0F, 0.8F + boss.world.rand.nextFloat() * 0.2F, 0.8F + boss.world.rand.nextFloat() * 0.2F, 0.8F + boss.world.rand.nextFloat() * 0.2F, 0.7F, false, p, 3, 1, 4 + boss.world.rand.nextInt(3), 0, 1.0F + boss.world.rand.nextFloat() * 0.3F);
      }
   }
}
