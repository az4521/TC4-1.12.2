package thaumcraft.common.blocks;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidClassic;
import thaumcraft.client.fx.ParticleEngine;
import thaumcraft.client.fx.particles.FXBubble;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.Config;
import thaumcraft.common.config.ConfigBlocks;

import java.util.Random;

public class BlockFluidPure extends BlockFluidClassic {

   public BlockFluidPure() {
      super(ConfigBlocks.FLUIDPURE, Material.WATER);
      this.setCreativeTab(Thaumcraft.tabTC);
   }

   @Override
   public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
      if (!world.isRemote
            && this.isSourceBlock(world, pos)
            && entity instanceof EntityPlayer
            && !((EntityPlayer) entity).isPotionActive(Potion.getPotionById(Config.potionWarpWardID))) {
         int warp = Thaumcraft.proxy.getPlayerKnowledge().getWarpPerm(entity.getName());
         int div = 1;
         if (warp > 0) {
            div = (int) Math.sqrt(warp);
            if (div < 1) {
               div = 1;
            }
         }
         ((EntityPlayer) entity).addPotionEffect(
               new PotionEffect(Potion.getPotionById(Config.potionWarpWardID),
                     Math.min(32000, 200000 / div), 0, true, false));
         world.setBlockToAir(pos);
      }
   }

   @Override
   @SideOnly(Side.CLIENT)
   public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
      int meta = this.getQuantaValue(world, pos);
      FXBubble fb = new FXBubble(world,
            (float) pos.getX() + rand.nextFloat(),
            (float) pos.getY() + 0.125F * (float) (8 - meta),
            (float) pos.getZ() + rand.nextFloat(),
            0.0F, 0.0F, 0.0F, 0);
      fb.setAlphaF(0.25F);
      fb.setRGB(1.0F, 1.0F, 1.0F);
      ParticleEngine.instance.addEffect(world, fb);
      if (rand.nextInt(25) == 0) {
         double var21 = pos.getX() + rand.nextFloat();
         double var22 = pos.getY() + this.getQuantaPercentage(world, pos);
         double var23 = pos.getZ() + rand.nextFloat();
         world.playSound(null, var21, var22, var23,
               thaumcraft.common.lib.SoundsTC.get("minecraft:block.lava.pop"),
               net.minecraft.util.SoundCategory.BLOCKS,
               0.1F + rand.nextFloat() * 0.1F,
               0.9F + rand.nextFloat() * 0.15F);
      }
      super.randomDisplayTick(state, world, pos, rand);
   }
}
