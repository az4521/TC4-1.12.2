package thaumcraft.common.entities.monster.mods;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import thaumcraft.client.fx.ParticleEngine;
import thaumcraft.client.fx.particles.FXSpark;

public class ChampionModBold implements IChampionModifierEffect {
   public float performEffect(EntityLivingBase boss, EntityLivingBase target, DamageSource source, float ammount) {
      return 0.0F;
   }

   @SideOnly(Side.CLIENT)
   public void showFX(EntityLivingBase boss) {
      if (!boss.world.rand.nextBoolean()) {
         float w = boss.world.rand.nextFloat() * boss.width;
         float d = boss.world.rand.nextFloat() * boss.width;
         float h = boss.world.rand.nextFloat() * boss.height / 3.0F;
         FXSpark ef = new FXSpark(boss.world, boss.getEntityBoundingBox().minX + (double)w, boss.getEntityBoundingBox().minY + (double)h, boss.getEntityBoundingBox().minZ + (double)d, 0.2F);
         ef.setRBGColorF(0.3F - boss.world.rand.nextFloat() * 0.1F, 0.0F, 0.8F + boss.world.rand.nextFloat() * 0.2F);
         ParticleEngine.instance.addEffect(boss.world, ef);
      }
   }
}
