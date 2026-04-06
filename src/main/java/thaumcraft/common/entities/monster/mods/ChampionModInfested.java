package thaumcraft.common.entities.monster.mods;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.entities.monster.EntityTaintSpider;

public class ChampionModInfested implements IChampionModifierEffect {
   public float performEffect(EntityLivingBase boss, EntityLivingBase target, DamageSource source, float amount) {
      if (boss.world.rand.nextFloat() < 0.4F && !boss.world.isRemote) {
         EntityTaintSpider spiderling = new EntityTaintSpider(boss.world);
         spiderling.setLocationAndAngles(boss.posX, boss.posY + (double)(boss.height / 2.0F), boss.posZ, boss.world.rand.nextFloat() * 360.0F, 0.0F);
         boss.world.spawnEntity(spiderling);
         { net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft:gore")); if (_snd != null) boss.playSound(_snd, 0.5F, 1.0F); }
      }

      return amount;
   }

   @SideOnly(Side.CLIENT)
   public void showFX(EntityLivingBase boss) {
      if (boss.world.rand.nextBoolean()) {
         Thaumcraft.proxy.slimeJumpFX(boss, 0);
      }

   }
}
