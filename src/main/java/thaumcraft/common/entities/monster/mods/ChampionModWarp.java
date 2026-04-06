package thaumcraft.common.entities.monster.mods;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import thaumcraft.common.Thaumcraft;

public class ChampionModWarp implements IChampionModifierEffect {
   public float performEffect(EntityLivingBase boss, EntityLivingBase target, DamageSource source, float amount) {
      if (boss.world.rand.nextFloat() < 0.33F && target instanceof EntityPlayer) {
         Thaumcraft.addWarpToPlayer((EntityPlayer)target, 1 + boss.world.rand.nextInt(3), true);
      }

      return amount;
   }

   @SideOnly(Side.CLIENT)
   public void showFX(EntityLivingBase boss) {
      if (!boss.world.rand.nextBoolean()) {
         float w = boss.world.rand.nextFloat() * boss.width;
         float d = boss.world.rand.nextFloat() * boss.width;
         float h = boss.world.rand.nextFloat() * boss.height;
         Thaumcraft.proxy.drawGenericParticles(boss.world, boss.getEntityBoundingBox().minX + (double)w, boss.getEntityBoundingBox().minY + (double)h, boss.getEntityBoundingBox().minZ + (double)d, 0.0F, 0.0F, 0.0F, 0.8F + boss.world.rand.nextFloat() * 0.2F, 0.0F, 0.9F + boss.world.rand.nextFloat() * 0.1F, 0.7F, true, 72, 8, 1, 10 + boss.world.rand.nextInt(4), 0, 0.6F + boss.world.rand.nextFloat() * 0.4F);
      }
   }
}
