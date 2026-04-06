package thaumcraft.common.entities.monster.mods;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import thaumcraft.common.Thaumcraft;

public class ChampionModVampire implements IChampionModifierEffect {
   public float performEffect(EntityLivingBase boss, EntityLivingBase target, DamageSource source, float amount) {
      boss.heal(Math.max(2.0F, amount / 2.0F));
      return amount;
   }

   @SideOnly(Side.CLIENT)
   public void showFX(EntityLivingBase boss) {
      if (!(boss.world.rand.nextFloat() > 0.2F)) {
         float w = boss.world.rand.nextFloat() * boss.width;
         float d = boss.world.rand.nextFloat() * boss.width;
         float h = boss.world.rand.nextFloat() * boss.height;
         Thaumcraft.proxy.drawGenericParticles(boss.world, boss.getEntityBoundingBox().minX + (double)w, boss.getEntityBoundingBox().minY + (double)h, boss.getEntityBoundingBox().minZ + (double)d, 0.0F, 0.0F, 0.0F, 0.9F + boss.world.rand.nextFloat() * 0.1F, 0.0F, 0.0F, 0.9F, false, 147, 4, 1, 8 + boss.world.rand.nextInt(4), 0, 0.5F + boss.world.rand.nextFloat() * 0.2F);
      }
   }
}
