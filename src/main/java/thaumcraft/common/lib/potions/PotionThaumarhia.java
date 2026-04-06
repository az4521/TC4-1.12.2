package thaumcraft.common.lib.potions;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.ResourceLocation;
import thaumcraft.common.config.ConfigBlocks;
import net.minecraft.util.math.BlockPos;

public class PotionThaumarhia extends Potion {
   public static PotionThaumarhia instance = null;
   private int statusIconIndex = -1;
   static final ResourceLocation rl = new ResourceLocation("thaumcraft", "textures/misc/potions.png");

   public PotionThaumarhia(boolean par2, int par3) {
      super(par2, par3);
      this.setIconIndex(0, 0);
   }

   public static void init() {
      instance.setPotionName("potion.thaumarhia");
      instance.setIconIndex(7, 2);
      instance.setEffectiveness(0.25F);
   }

   public boolean isBadEffect() {
      return true;
   }

   @SideOnly(Side.CLIENT)
   public int getStatusIconIndex() {
      Minecraft.getMinecraft().renderEngine.bindTexture(rl);
      return super.getStatusIconIndex();
   }

   public void performEffect(EntityLivingBase target, int par2) {
      if (!target.world.isRemote && target.world.rand.nextInt(15) == 0) {
         int x = MathHelper.floor(target.posX);
         int y = MathHelper.floor(target.posY);
         int z = MathHelper.floor(target.posZ);
         if (target.world.isAirBlock(new BlockPos(x, y, z))) {
            target.
        world.setBlockState(new net.minecraft.util.math.BlockPos(x, y, z), (ConfigBlocks.blockFluxGoo).getDefaultState(), 3);
         }
      }

   }

   public boolean isReady(int par1, int par2) {
      return par1 % 20 == 0;
   }
}
